package com.tecsup.usuario_kotlin.ui.translation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun SenasBottomBar(
    selectedTab: Int,
    onSelectTraducir: () -> Unit,
    onSelectSenas: () -> Unit,
    onSelectPerfil: () -> Unit
) {
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = ProfileColors.AccentCyan,
        selectedTextColor = ProfileColors.AccentCyan,
        unselectedIconColor = ProfileColors.TextSecondary,
        unselectedTextColor = ProfileColors.TextSecondary,
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        containerColor = ProfileColors.BgDark,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = onSelectTraducir,
            icon = {
                Icon(
                    Icons.Outlined.Videocam,
                    contentDescription = "Traducir"
                )
            },
            label = { Text("Traducir", fontSize = 12.sp) },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = onSelectSenas,
            icon = {
                Icon(
                    Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Señas"
                )
            },
            label = { Text("Señas", fontSize = 12.sp) },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = onSelectPerfil,
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil", fontSize = 12.sp) },
            colors = navColors
        )
    }
}