package com.tecsup.usuario_kotlin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.translation.SenasBottomBar

private val BgDark = Color(0xFF08141A)
private val CardDark = Color(0xFF0F2129)
private val CardElevated = Color(0xFF132A34)
private val AccentCyan = Color(0xFF22B8CF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9BA8)
private val BadgeYellow = Color(0xFFF5B82E)

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    notificationCount: Int = 3,
    onNavigateToTraducir: () -> Unit,
    onNavigateToSenas: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
) {

    // Mostrar los datos reales del usuario autenticado
    val displayName = userName.ifBlank { "Usuario" }
    val displayEmail = userEmail.ifBlank { "Correo no disponible" }

    val initial = displayName
        .trim()
        .firstOrNull()
        ?.uppercase() ?: "U"

    var darkModeEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            SenasBottomBar(
                selectedTab = 2,
                onSelectTraducir = onNavigateToTraducir,
                onSelectSenas = onNavigateToSenas,
                onSelectPerfil = {}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Cabecera "Mi cuenta"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Mi cuenta",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Avatar + Nombre + Correo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentCyan),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = initial,
                        color = BgDark,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {

                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = displayEmail,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Tarjeta contenedora de opciones
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardElevated)
            ) {

                Column {

                    // Navegación hacia la edición del perfil
                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "Mi perfil",
                        onClick = onNavigateToEditProfile
                    )

                    HorizontalDivider(
                        color = CardElevated,
                        thickness = 1.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Tune,
                        title = "Configuración",
                        onClick = {}
                    )

                    HorizontalDivider(
                        color = CardElevated,
                        thickness = 1.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificaciones",
                        trailing = {

                            if (notificationCount > 0) {

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BadgeYellow),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Text(
                                        text = notificationCount.toString(),
                                        color = BgDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        onClick = {}
                    )

                    HorizontalDivider(
                        color = CardElevated,
                        thickness = 1.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "Modo oscuro",
                        trailing = {

                            Switch(
                                checked = darkModeEnabled,
                                onCheckedChange = {
                                    darkModeEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextPrimary,
                                    checkedTrackColor = AccentCyan,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CardElevated
                                )
                            )
                        },
                        onClick = {
                            darkModeEnabled = !darkModeEnabled
                        }
                    )

                    HorizontalDivider(
                        color = CardElevated,
                        thickness = 1.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Security,
                        title = "Panel de administración",
                        trailing = {

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .border(
                                        1.dp,
                                        AccentCyan.copy(alpha = 0.6f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                            ) {

                                Text(
                                    text = "Solo admin",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {}
                    )

                    HorizontalDivider(
                        color = CardElevated,
                        thickness = 1.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "Cerrar sesión",
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

// Componente reutilizable para las opciones del perfil
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AccentCyan,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (trailing != null) {
            trailing()
        }
    }
}