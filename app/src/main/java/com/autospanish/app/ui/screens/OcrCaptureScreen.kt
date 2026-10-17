package com.autospanish.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.autospanish.app.data.TranslationDirection
import com.autospanish.app.ocr.OcrTextExtractor
import com.autospanish.app.translate.rememberIsOnline
import com.autospanish.app.ui.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrCaptureScreen(
    viewModel: AppViewModel,
    targetListId: Long?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val online by rememberIsOnline()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var candidateWords by remember { mutableStateOf(listOf<String>()) }
    var selectedWords by remember { mutableStateOf(setOf<String>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                val stream = context.contentResolver.openInputStream(uri)
                bitmap = stream?.use { BitmapFactory.decodeStream(it) }
                candidateWords = emptyList()
                isProcessing = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) {
            val uri = createCaptureUri(context)
            photoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(bitmap) {
        val bmp = bitmap ?: return@LaunchedEffect
        val words = OcrTextExtractor.extractWords(bmp)
        candidateWords = words
        isProcessing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan text") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        permissionDenied = false
                        val uri = createCaptureUri(context)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Take photo")
            }

            if (permissionDenied) {
                Text(
                    "Camera permission is required to scan text. Enable it in system settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            if (isProcessing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        "Reading text…",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }

            if (!isProcessing && bitmap != null && candidateWords.isEmpty()) {
                Text(
                    "No words found in that photo. Try a clearer, closer shot.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (candidateWords.isNotEmpty()) {
                Text("Tap the words you want to add:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(candidateWords) { word ->
                        FilterChip(
                            selected = word in selectedWords,
                            onClick = { selectedWords = if (word in selectedWords) selectedWords - word else selectedWords + word },
                            label = { Text(word) },
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.addWords(selectedWords.toList(), TranslationDirection.AUTO, online, targetListId) { _, _, _ -> }
                        onBack()
                    },
                    enabled = selectedWords.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Add ${selectedWords.size} selected") }
            }
        }
    }
}

private fun createCaptureUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "ocr").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "com.autospanish.app.fileprovider", file)
}
