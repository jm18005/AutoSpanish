package com.autospanish.app.grammar

/**
 * Several hundred high-frequency Spanish words (function words, top verbs, nouns, adjectives,
 * adverbs), used as a commonness signal when picking between multiple dictionary entries that
 * gloss the same English word. The scraped dictionary carries no frequency data of its own, and
 * its synonym cross-links are too noisy (often mutual, sometimes backwards) to infer commonness
 * from — this fills that gap.
 */
val COMMON_SPANISH_WORDS: Set<String> = """
el la los las un una unos unas de a en que y es se no te lo le da su por con para
mi tu su nos les como más pero sus le ya o este sí porque esta entre cuando muy
sin sobre también me hasta hay donde quien desde todo nos durante todos uno les
ni contra otros ese eso ante ellos e esto mí antes algunos qué unos yo también
soy eres somos son era eras eramos eran fui fuiste fue fuimos fueron seré serás
sera seremos serán tengo tienes tiene tenemos tienen tenía tenías tenía teníamos
tenían tuve tuviste tuvo tuvimos tuvieron voy vas va vamos van iba ibas iba
ibamos iban fui fuiste fue fuimos fueron iré irás irá iremos irán hago haces
hace hacemos hacen hacía hacías hacíamos hacían hice hiciste hizo hicimos
hicieron haré harás hará haremos harán digo dices dice decimos dicen decía
decías decíamos decían dije dijiste dijo dijimos dijeron diré dirás dirá
diremos dirán veo ves ve vemos ven veía veías veíamos veían vi viste vio
vimos vieron veré verás verá veremos verán quiero quieres quiere queremos
quieren quería querías queríamos querían quise quisiste quiso quisimos
quisieron podré podrás podrá podremos podrán puedo puedes puede podemos
pueden podía podías podíamos podían pude pudiste pudo pudimos pudieron
sé sabes sabe sabemos saben sabía sabías sabíamos sabían supe supiste supo
supimos supieron vengo vienes viene venimos vienen venía venías veníamos
venían vine viniste vino vinimos vinieron llego llegas llega llegamos llegan
llegaba llegabas llegábamos llegaban llegué llegaste llegó llegamos llegaron
casa perro gato libro agua amigo amiga ciudad país hombre mujer mesa silla
puerta ventana comida tiempo día noche año mundo vida mano ojo cabeza sol luna
estrella árbol flor escuela colegio trabajo empleo dinero familia madre padre
hermano hermana calle rey reina guerra paz amor nombre palabra música leche
pan arroz sal fuego aire tierra mar montaña río lluvia nieve viento hora semana
mes maestro profesor médico doctor estudiante alumno hospital hotel restaurante
juego equipo carta teléfono computadora historia cuento voz cara corazón sangre
hueso piel pelo cabello brazo pie boca nariz diente correr comer beber hablar
tener ir venir ver mirar saber conocer pensar creer entender comprender leer
escribir dormir caminar andar nadar cantar bailar jugar estudiar aprender
enseñar comprar vender pagar cocinar limpiar lavar abrir cerrar ayudar
necesitar preguntar pedir responder contestar llamar llegar entrar vivir morir
crecer cambiar llevar traer dar recibir encontrar perder ganar romper construir
caer sonreír reír llorar sentir recordar olvidar decidir explicar viajar seguir
volver regresar conducir manejar empujar tirar lanzar tocar besar abrazar odiar
amar casarse nacer cortar pintar dibujar gritar escuchar oír oler probar contar
medir pesar llenar vaciar vestirse ducharse bañarse afeitarse hornear freír
hervir congelar quemar cubrir esconder revelar descubrir inventar crear
destruir reparar arreglar plantar alimentar levantar bajar colgar atar
conectar empezar comenzar terminar acabar parar detener continuar repetir
quedarse salir mudarse visitar saludar presentar invitar aceptar rechazar
prometer mentir advertir sugerir recomendar quejarse disculparse perdonar
agradecer felicitar grande pequeño bueno malo nuevo viejo joven hermoso feo
caliente frío alto corto largo pobre fácil difícil importante diferente lleno
oscuro dulce valiente cansado enfermo libre verdadero cierto falso posible
necesario moderno público privado especial rojo azul verde amarillo negro
blanco limpio mojado seco suave duro profundo ancho estrecho grueso delgado
áspero afilado ruidoso callado tranquilo silencioso famoso popular caro barato
generoso amable cruel honesto amistoso cortés grosero orgulloso celoso curioso
perezoso activo calmado nervioso preocupado confundido sorprendido aburrido
cómodo incómodo saludable sano redondo cuadrado recto vacío pesado ligero
fuerte débil rico casado soltero embarazada vivo muerto despierto dormido
hambriento sediento gordo flaco rápido lento siempre nunca aquí allí hoy
mañana ayer bien otra vez también ya todavía pronto tarde temprano lejos cerca
finalmente probablemente casi juntos solo afuera dentro arriba abajo adelante
atrás quien que donde cuando por como pero porque si aunque uno dos tres cuatro
cinco seis siete ocho nueve diez cien mil primero segundo tercero último mucho
poco todo nada algo alguien nadie cualquier cada otro mismo tal tanto varios
ese esa aquel aquella este esta cual cuyo mío tuyo suyo nuestro vuestro
manera modo forma camino vía paso derecho justo trabajo empleo poder línea fin
final parte lado momento cosa persona gente idea número punto lugar sitio nivel
razón motivo cuidado esfuerzo policía precio pueblo aldea fiesta partido
mercado feliz contento triste alegre enojado furioso asustado sorprendido
carne pollo pescado huevo queso mantequilla yogur cereal sopa ensalada postre
pastel galleta chocolate helado café té vino cerveza jugo zumo refresco
tenedor cuchillo cuchara plato taza vaso botella servilleta mantel
camisa pantalón pantalones falda vestido chaqueta chaleco abrigo suéter
bufanda guantes gorra sombrero cinturón corbata calcetín media ropa
zapato bota sandalia pijama traje
sofá sillón cama armario cómoda escritorio estante lámpara alfombra cortina
espejo cuadro reloj televisor radio nevera refrigerador horno estufa
lavadora secadora aspiradora
perro gato pájaro caballo vaca cerdo oveja cabra pollo gallina pato ganso
conejo ratón rata león tigre oso lobo zorro ciervo elefante jirafa mono
serpiente lagarto rana tortuga pez tiburón ballena delfín pulpo cangrejo
abeja hormiga mosca mosquito araña gusano
rojo azul verde amarillo negro blanco gris marrón naranja morado rosa
violeta dorado plateado
uno dos tres cuatro cinco seis siete ocho nueve diez once doce trece catorce
quince veinte treinta cuarenta cincuenta cien mil millón primero segundo
tercero cuarto quinto último próximo pasado siguiente
lunes martes miércoles jueves viernes sábado domingo
enero febrero marzo abril mayo junio julio agosto septiembre octubre
noviembre diciembre primavera verano otoño invierno
madre padre mamá papá hijo hija hermano hermana abuelo abuela nieto nieta
tío tía primo prima sobrino sobrina esposo esposa novio novia bebé
médico doctor enfermero abogado ingeniero policía bombero soldado piloto
cocinero camarero mesero granjero pescador carpintero peluquero
cabeza cara ojo oreja nariz boca diente lengua cuello hombro brazo mano
dedo pierna rodilla tobillo pie espalda pecho estómago vientre
enfermo sano cansado dormido despierto hambriento sediento contento triste
enojado asustado sorprendido nervioso tranquilo aburrido emocionado
avión barco tren autobús bicicleta motocicleta camión taxi metro
aeropuerto estación puerto carretera autopista
sol luna estrella cielo nube lluvia nieve viento tormenta trueno relámpago
niebla hielo calor frío húmedo seco
grande pequeño alto bajo largo corto ancho estrecho pesado ligero
nuevo viejo joven bonito guapo lindo feo rico pobre limpio sucio
fácil difícil rápido lento fuerte débil caliente frío duro blando
mucho poco todo nada bien mal más menos siempre nunca hoy ayer mañana
temprano tarde pronto ahora antes después aquí allí cerca lejos dentro fuera
computadora ordenador internet correo mensaje aplicación pantalla
teclado ratón impresora cámara batería cargador
casa apartamento edificio piso techo pared suelo escalera ascensor jardín
garaje patio balcón terraza
cuarto poder café chico maestro hecho fin cuidado cerdo vestido vaso abrigo
pesado llenar empezar salir rechazar perdonar hermoso famoso saludable
rápidamente pronto finalmente afuera aunque quedar qué habitación derecho
razón niño policía teléfono dormitorio colina hierba pasto hoja tomate carne
vestirse casarse divorciarse nacer escuchar desvestirse ducharse bañarse
afeitarse descubrir romper rasgar bajar dejar caer cerrar llave abrir
quedarse discrepar disculparse profundo ruidoso
recto embarazada juntos solo atrás arriba doquiera dondequiera
universidad facultad matriculado profesor profesora nación mente
princesa príncipe lentamente despacio desvestir ducha duchar afeitar
embarazado
""".trim().split(Regex("""\s+""")).toSet()
