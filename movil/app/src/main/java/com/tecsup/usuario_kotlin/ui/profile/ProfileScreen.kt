package com.tecsup.usuario_kotlin.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel

// Colors
private val ScreenBg = Color(0xFF06141B)
private val CardBg = Color(0xFF091A23)
private val PrimaryCyan = Color(0xFF14A3A7)
private val SubtitleGray = Color(0xFF9BB0BC)
private val DividerColor = Color(0xFF142732)
private val NotificationBadgeYellow = Color(0xFFFBBF24)
private val BottomNavBg = Color(0xFF040D12)
private val NavUnselected = Color(0xFF586B76)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = remember { AuthViewModel() },
    onLogoutClick: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val user = authViewModel.currentUser
    val avatarInitial = user.username.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar(
                selectedIndex = 2,
                onTabSelected = { index ->
                    onNavigateToTab(index)
                }
            )
        },
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // --- TOP TITLE ---
            Text(
                text = "Mi cuenta",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- USER HEADER CARD ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Avatar Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PrimaryCyan, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitial,
                        style = TextStyle(
                            color = ScreenBg,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // User Name & Email
                Column {
                    Text(
                        text = user.username,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = TextStyle(
                            color = SubtitleGray,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- OPTIONS LIST CARD CONTAINER ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardBg
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Item 1: Mi perfil
                    ProfileOptionItem(
                        icon = { UserIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Mi perfil",
                        onClick = { /* Action */ }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)

                    // Item 2: Configuración
                    ProfileOptionItem(
                        icon = { SettingsSlidersIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Configuración",
                        onClick = { /* Action */ }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)

                    // Item 3: Notificaciones
                    ProfileOptionItem(
                        icon = { BellOptionIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Notificaciones",
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(NotificationBadgeYellow, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "3",
                                    style = TextStyle(
                                        color = ScreenBg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        },
                        onClick = { /* Action */ }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)

                    // Item 4: Modo oscuro
                    ProfileOptionItem(
                        icon = { MoonIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Modo oscuro",
                        trailing = {
                            Switch(
                                checked = authViewModel.isDarkMode,
                                onCheckedChange = { authViewModel.toggleDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryCyan,
                                    uncheckedThumbColor = SubtitleGray,
                                    uncheckedTrackColor = Color(0xFF0E232E)
                                )
                            )
                        },
                        onClick = { authViewModel.toggleDarkMode(!authViewModel.isDarkMode) }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)

                    // Item 5: Panel de administración
                    ProfileOptionItem(
                        icon = { ShieldIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Panel de administración",
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, color = PrimaryCyan, shape = RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Solo admin",
                                    style = TextStyle(
                                        color = PrimaryCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        },
                        onClick = { /* Action */ }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)

                    // Item 6: Cerrar sesión
                    ProfileOptionItem(
                        icon = { LogoutIcon(tint = PrimaryCyan, modifier = Modifier.size(22.dp)) },
                        title = "Cerrar sesión",
                        titleColor = PrimaryCyan,
                        onClick = {
                            authViewModel.logout()
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: @Composable () -> Unit,
    title: String,
    titleColor: Color = Color.White,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        if (trailing != null) {
            trailing()
        }
    }
}

// --- BOTTOM NAVIGATION BAR FOR PROFILE SCREEN ---
@Composable
private fun ProfileBottomNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BottomNavBg,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Traducir
            ProfileNavTabItem(
                title = "Traducir",
                isSelected = selectedIndex == 0,
                onClick = { onTabSelected(0) },
                icon = { tint -> CameraNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )

            // Tab 1: Señas
            ProfileNavTabItem(
                title = "Señas",
                isSelected = selectedIndex == 1,
                onClick = { onTabSelected(1) },
                icon = { tint -> BookNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )

            // Tab 2: Perfil
            ProfileNavTabItem(
                title = "Perfil",
                isSelected = selectedIndex == 2,
                onClick = { onTabSelected(2) },
                icon = { tint -> ProfileNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )
        }
    }
}

@Composable
private fun ProfileNavTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val tint = if (isSelected) PrimaryCyan else NavUnselected
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        icon(tint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = TextStyle(
                color = tint,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// --- CUSTOM CANVAS ICONS ---

@Composable
private fun UserIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = stroke))
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.52f),
            size = Size(w * 0.64f, h * 0.4f),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun SettingsSlidersIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Line 1
        drawLine(tint, Offset(w * 0.1f, h * 0.3f), Offset(w * 0.9f, h * 0.3f), stroke, StrokeCap.Round)
        drawCircle(tint, radius = 3.dp.toPx(), center = Offset(w * 0.35f, h * 0.3f))

        // Line 2
        drawLine(tint, Offset(w * 0.1f, h * 0.7f), Offset(w * 0.9f, h * 0.7f), stroke, StrokeCap.Round)
        drawCircle(tint, radius = 3.dp.toPx(), center = Offset(w * 0.65f, h * 0.7f))
    }
}

@Composable
private fun BellOptionIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val bellPath = Path().apply {
            moveTo(w * 0.3f, h * 0.75f)
            lineTo(w * 0.7f, h * 0.75f)
            cubicTo(w * 0.8f, h * 0.75f, w * 0.85f, h * 0.65f, w * 0.8f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.35f)
            cubicTo(w * 0.75f, h * 0.2f, w * 0.63f, h * 0.1f, w * 0.5f, h * 0.1f)
            cubicTo(w * 0.37f, h * 0.1f, w * 0.25f, h * 0.2f, w * 0.25f, h * 0.35f)
            lineTo(w * 0.2f, h * 0.5f)
            cubicTo(w * 0.15f, h * 0.65f, w * 0.2f, h * 0.75f, w * 0.3f, h * 0.75f)
            close()
        }
        drawPath(path = bellPath, color = tint, style = Stroke(width = stroke))
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.42f, h * 0.78f),
            size = Size(w * 0.16f, h * 0.14f),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun MoonIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val moonPath = Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            cubicTo(w * 0.3f, h * 0.15f, w * 0.15f, h * 0.35f, w * 0.15f, h * 0.65f)
            cubicTo(w * 0.15f, h * 0.85f, w * 0.3f, h * 0.95f, w * 0.5f, h * 0.95f)
            cubicTo(w * 0.25f, h * 0.85f, w * 0.3f, h * 0.45f, w * 0.75f, h * 0.35f)
            cubicTo(w * 0.85f, h * 0.35f, w * 0.95f, h * 0.4f, w * 0.95f, h * 0.4f)
            cubicTo(w * 0.9f, h * 0.25f, w * 0.78f, h * 0.15f, w * 0.65f, h * 0.15f)
            close()
        }
        drawPath(moonPath, color = tint, style = Stroke(width = stroke))
    }
}

@Composable
private fun ShieldIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.22f)
            lineTo(w * 0.85f, h * 0.52f)
            cubicTo(w * 0.85f, h * 0.75f, w * 0.65f, h * 0.9f, w * 0.5f, h * 0.95f)
            cubicTo(w * 0.35f, h * 0.9f, w * 0.15f, h * 0.75f, w * 0.15f, h * 0.52f)
            lineTo(w * 0.15f, h * 0.22f)
            close()
        }
        drawPath(shieldPath, color = tint, style = Stroke(width = stroke))
        // Checkmark inside shield
        val checkPath = Path().apply {
            moveTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.62f)
            lineTo(w * 0.68f, h * 0.38f)
        }
        drawPath(checkPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

@Composable
private fun LogoutIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Door frame
        val doorPath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.2f, h * 0.15f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.5f, h * 0.85f)
        }
        drawPath(doorPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))

        // Arrow pointing right
        drawLine(tint, Offset(w * 0.4f, h * 0.5f), Offset(w * 0.85f, h * 0.5f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.68f, h * 0.35f), Offset(w * 0.85f, h * 0.5f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.68f, h * 0.65f), Offset(w * 0.85f, h * 0.5f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CameraNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.25f),
            size = Size(w * 0.52f, h * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = stroke)
        )
        val camTriangle = Path().apply {
            moveTo(w * 0.62f, h * 0.4f)
            lineTo(w * 0.9f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.75f)
            lineTo(w * 0.62f, h * 0.6f)
            close()
        }
        drawPath(camTriangle, color = tint, style = Stroke(width = stroke))
    }
}

@Composable
private fun BookNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val bookLeft = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.15f, h * 0.2f, w * 0.1f, h * 0.25f)
            lineTo(w * 0.1f, h * 0.75f)
            cubicTo(w * 0.15f, h * 0.7f, w * 0.35f, h * 0.65f, w * 0.5f, h * 0.75f)
            close()
        }
        val bookRight = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.65f, h * 0.15f, w * 0.85f, h * 0.2f, w * 0.9f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.75f)
            cubicTo(w * 0.85f, h * 0.7f, w * 0.65f, h * 0.65f, w * 0.5f, h * 0.75f)
            close()
        }
        drawPath(bookLeft, color = tint, style = Stroke(width = stroke))
        drawPath(bookRight, color = tint, style = Stroke(width = stroke))
    }
}

@Composable
private fun ProfileNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = stroke))
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.52f),
            size = Size(w * 0.64f, h * 0.4f),
            style = Stroke(width = stroke)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
