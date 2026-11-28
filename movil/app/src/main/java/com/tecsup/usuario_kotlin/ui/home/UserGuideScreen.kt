package com.tecsup.usuario_kotlin.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

// Modelo interno para las secciones de la guía
private data class GuideSection(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val steps: List<String>,
    val tip: String? = null
)

@Composable
fun UserGuideScreen(
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    val sections = remember {
        obtenerSeccionesGuia()
    }

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 24.dp
            )
        ) {
            // Cabecera de navegación
            item(key = "header") {
                ScreenHeader(
                    title = "Guía de uso",
                    onBackClick = onBackClick
                )
            }

            // Presentación de la guía
            item(key = "welcome") {
                GuideWelcomeCard()
            }

            item(key = "instructions") {
                Text(
                    text = "Selecciona una sección para " +
                            "consultar sus instrucciones.",
                    color = ProfileColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            // Secciones desplegables
            items(
                items = sections,
                key = { it.id }
            ) { section ->

                GuideSectionCard(
                    section = section,
                    initiallyExpanded =
                        section.id == "translator"
                )
            }

            // Información final
            item(key = "footer") {
                Text(
                    text = "SeñasApp es una herramienta de apoyo " +
                            "para la comunicación y el aprendizaje. " +
                            "Los resultados del reconocimiento pueden " +
                            "variar según la iluminación, la posición " +
                            "de las manos y las señas disponibles.",
                    color = ProfileColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 19.sp
                )
            }

            // Regresar al perfil
            item(key = "back_button") {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        ProfileColors.AccentCyan
                    ),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor =
                                ProfileColors.AccentCyan
                        )
                ) {
                    Text(
                        text = "Volver a mi cuenta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Tarjeta de presentación
@Composable
private fun GuideWelcomeCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileColors.CardDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            ProfileColors.AccentCyan.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        ProfileColors.AccentCyan.copy(
                            alpha = 0.12f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = ProfileColors.AccentCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "Aprende a utilizar SeñasApp",
                color = ProfileColors.TextPrimary,
                fontSize = 21.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Conoce las herramientas de traducción, " +
                        "aprendizaje y personalización de la aplicación.",
                color = ProfileColors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

// Tarjeta desplegable de cada sección
@Composable
private fun GuideSectionCard(
    section: GuideSection,
    initiallyExpanded: Boolean = false
) {
    var expanded by rememberSaveable(section.id) {
        mutableStateOf(initiallyExpanded)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileColors.CardDark,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (expanded) {
                ProfileColors.AccentCyan.copy(
                    alpha = 0.4f
                )
            } else {
                ProfileColors.CardElevated
            }
        )
    ) {
        Column {
            // Encabezado interactivo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        stateDescription = if (expanded) {
                            "Expandido"
                        } else {
                            "Contraído"
                        }
                    }
                    .clickable(role = Role.Button) {
                        expanded = !expanded
                    }
                    .heightIn(min = 56.dp)
                    .padding(14.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            ProfileColors.CardElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = section.title,
                    color = ProfileColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = ProfileColors.TextSecondary
                )
            }

            // Contenido desplegable
            if (expanded) {
                HorizontalDivider(
                    color = ProfileColors.CardElevated
                )

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    section.steps.forEachIndexed {
                            index, step ->

                        GuideStep(
                            number = index + 1,
                            description = step
                        )
                    }

                    section.tip?.let { tip ->
                        GuideTip(text = tip)
                    }
                }
            }
        }
    }
}

// Paso numerado
@Composable
private fun GuideStep(
    number: Int,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .sizeIn(
                    minWidth = 26.dp,
                    minHeight = 26.dp
                )
                .clip(CircleShape)
                .background(ProfileColors.CardElevated)
                .padding(
                    horizontal = 6.dp,
                    vertical = 3.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = ProfileColors.AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = description,
            color = ProfileColors.TextPrimary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// Tarjeta de recomendaciones
@Composable
private fun GuideTip(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileColors.AccentCyan.copy(
            alpha = 0.08f
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Recomendación",
                    color = ProfileColors.AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = text,
                    color = ProfileColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Contenido de la guía de usuario
private fun obtenerSeccionesGuia(): List<GuideSection> {
    return listOf(

        GuideSection(
            id = "translator",
            title = "¿Cómo utilizar el traductor?",
            icon = Icons.Outlined.Videocam,
            steps = listOf(
                "Ingresa a la sección Traducir desde " +
                        "el menú inferior.",

                "Autoriza el acceso a la cámara cuando " +
                        "la aplicación lo solicite.",

                "Coloca una o ambas manos dentro " +
                        "del marco de detección.",

                "Realiza una seña disponible en el catálogo " +
                        "y espera a que sea reconocida.",

                "Realiza otras señas para construir una frase. " +
                        "Retira las manos brevemente si deseas " +
                        "repetir una misma seña.",

                "Utiliza el botón de retroceso de la tarjeta " +
                        "de traducción para borrar la última seña, " +
                        "o Limpiar frase para borrar todas."
            ),
            tip = "Utiliza un ambiente bien iluminado y " +
                    "mantén las manos visibles frente a la cámara. " +
                    "Para reconocer movimientos, realiza la seña " +
                    "siguiendo su referencia visual."
        ),

        GuideSection(
            id = "output",
            title = "Modos de salida",
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            steps = listOf(
                "Selecciona Texto para visualizar la frase " +
                        "sin reproducir audio.",

                "Pulsa Guardar frase para añadirla " +
                        "al historial en modo Texto.",

                "Selecciona Voz para reproducir la frase " +
                        "mediante el motor de voz.",

                "Selecciona Ambos para visualizar la frase " +
                        "y poder reproducirla en voz alta.",

                "En los modos Voz y Ambos, pulsa " +
                        "Decir en voz alta para escuchar la frase."
            ),
            tip = "Puedes cambiar el modo de salida desde " +
                    "el traductor o desde Configuración."
        ),

        GuideSection(
            id = "history",
            title = "Historial de traducciones",
            icon = Icons.Outlined.History,
            steps = listOf(
                "Desplázate hasta Frases recientes " +
                        "en la pantalla del traductor.",

                "Consulta las frases guardadas y sus fechas.",

                "Pulsa Repetir frase para recuperar " +
                        "una traducción anterior.",

                "Utiliza el icono de eliminar para borrar " +
                        "una frase individual.",

                "Selecciona Limpiar historial y confirma " +
                        "si deseas eliminar todas las frases."
            ),
            tip = "La aplicación conserva hasta 10 frases " +
                    "recientes por cuenta en este dispositivo."
        ),

        GuideSection(
            id = "catalog",
            title = "Catálogo de señas",
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            steps = listOf(
                "Ingresa a Señas desde el menú inferior.",

                "Utiliza el buscador para encontrar " +
                        "una seña o frase.",

                "Selecciona una categoría para filtrar " +
                        "el catálogo.",

                "Pulsa una seña para consultar su nombre, " +
                        "descripción y referencias disponibles.",

                "Reproduce el video de referencia cuando " +
                        "esté disponible.",

                "Selecciona Practicar seña para iniciar " +
                        "el entrenamiento."
            ),
            tip = "El catálogo necesita conexión con el " +
                    "servidor para consultar las señas registradas."
        ),

        GuideSection(
            id = "practice",
            title = "Entrenamiento de señas",
            icon = Icons.Rounded.PanTool,
            steps = listOf(
                "Selecciona una seña del catálogo.",

                "Observa su referencia visual antes " +
                        "de comenzar.",

                "Pulsa Practicar seña y coloca las manos " +
                        "frente a la cámara.",

                "Realiza la seña indicada y espera las " +
                        "confirmaciones del reconocimiento.",

                "Cuando aparezca ¡Seña reconocida!, " +
                        "la práctica se habrá completado.",

                "Selecciona Practicar nuevamente si deseas " +
                        "realizar otro intento."
            ),
            tip = "Si el reconocimiento no coincide, " +
                    "observa nuevamente la referencia. Puedes " +
                    "detener la práctica en cualquier momento."
        ),

        GuideSection(
            id = "progress",
            title = "Consulta tu progreso",
            icon = Icons.Outlined.EmojiEvents,
            steps = listOf(
                "Ingresa a la sección Señas.",

                "Selecciona Mi progreso.",

                "Consulta la cantidad de señas diferentes " +
                        "practicadas correctamente.",

                "Revisa el total de prácticas exitosas " +
                        "y el porcentaje del catálogo disponible.",

                "Consulta la fecha de tu última práctica " +
                        "de cada seña."
            ),
            tip = "Los intentos incorrectos o detenidos " +
                    "no se registran como prácticas exitosas. " +
                    "El progreso es local e independiente " +
                    "para cada cuenta."
        ),

        GuideSection(
            id = "settings",
            title = "Configuración y accesibilidad",
            icon = Icons.Outlined.Tune,
            steps = listOf(
                "Ingresa a Perfil y selecciona Configuración.",

                "Elige tu modo de salida preferido.",

                "Activa o desactiva el modo oscuro.",

                "Modifica el tamaño del texto entre " +
                        "Normal, Grande y Extra grande.",

                "Selecciona Restablecer preferencias " +
                        "si deseas recuperar los valores originales."
            ),
            tip = "Las preferencias se guardan automáticamente. " +
                    "Restablecerlas no elimina tu historial, " +
                    "progreso ni notificaciones."
        ),

        GuideSection(
            id = "notifications",
            title = "Notificaciones",
            icon = Icons.Outlined.Notifications,
            steps = listOf(
                "Pulsa la campana del traductor o la opción " +
                        "Notificaciones del perfil.",

                "Consulta los avisos de bienvenida " +
                        "y de prácticas completadas.",

                "Selecciona una notificación para " +
                        "marcarla como leída.",

                "Utiliza Marcar todas como leídas " +
                        "para actualizar el contador.",

                "Elimina individualmente las notificaciones " +
                        "o utiliza Eliminar todas."
            ),
            tip = "Las notificaciones son internas de SeñasApp. " +
                    "El contador representa únicamente los avisos " +
                    "que todavía no has leído."
        ),

        GuideSection(
            id = "account",
            title = "Administración de mi cuenta",
            icon = Icons.Outlined.Person,
            steps = listOf(
                "Ingresa a Perfil desde el menú inferior.",

                "Selecciona Mi perfil para consultar " +
                        "o editar tus datos personales.",

                "Utiliza las opciones disponibles para " +
                        "actualizar tu fotografía de perfil.",

                "Selecciona Cambiar contraseña si deseas " +
                        "modificar tu contraseña actual.",

                "Pulsa Cerrar sesión cuando termines " +
                        "de utilizar tu cuenta."
            ),
            tip = "Cerrar sesión conserva las preferencias, " +
                    "el progreso y los registros locales de tu cuenta."
        ),

        GuideSection(
            id = "help",
            title = "Problemas frecuentes",
            icon = Icons.Outlined.HelpOutline,
            steps = listOf(
                "Si la cámara no funciona, comprueba " +
                        "los permisos de SeñasApp en Android.",

                "Si no se detectan las manos, mejora " +
                        "la iluminación y ajusta su posición.",

                "Si la seña no es reconocida, revisa " +
                        "la referencia disponible en el catálogo.",

                "Si aparece un error de conexión, comprueba " +
                        "que el dispositivo tenga acceso al servidor.",

                "Si no se reproduce el audio, verifica " +
                        "el volumen multimedia y la configuración " +
                        "de síntesis de voz de Android.",

                "Si el catálogo no carga, utiliza " +
                        "el botón de actualización."
            ),
            tip = "Si el problema continúa, solicita ayuda " +
                    "al responsable de la aplicación."
        )
    )
}