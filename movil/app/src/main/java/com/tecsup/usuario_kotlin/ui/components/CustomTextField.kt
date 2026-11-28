package com.tecsup.usuario_kotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun CustomTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {

    Column(modifier = modifier) {

        // Etiqueta del campo
        Text(
            text = label,
            color = ProfileColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto reutilizable
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = ProfileColors.TextSecondary,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileColors.AccentCyan
                )
            },
            colors = customTextFieldColors()
        )
    }
}

// Colores compartidos para campos de texto
@Composable
fun customTextFieldColors(): TextFieldColors {

    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ProfileColors.CardElevated,
        unfocusedContainerColor = ProfileColors.CardElevated,
        disabledContainerColor = ProfileColors.CardElevated,
        errorContainerColor = ProfileColors.CardElevated,

        focusedBorderColor = ProfileColors.AccentCyan,
        unfocusedBorderColor = ProfileColors.BorderColor,
        disabledBorderColor = ProfileColors.DisabledBorderColor,
        errorBorderColor = ProfileColors.ErrorColor,

        focusedTextColor = ProfileColors.TextPrimary,
        unfocusedTextColor = ProfileColors.TextPrimary,
        disabledTextColor = ProfileColors.TextSecondary,
        errorTextColor = ProfileColors.TextPrimary,

        cursorColor = ProfileColors.AccentCyan,
        errorCursorColor = ProfileColors.ErrorColor
    )
}