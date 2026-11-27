package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.components.CustomTextField
import com.tecsup.usuario_kotlin.ui.components.ErrorMessage
import com.tecsup.usuario_kotlin.ui.components.PrimaryButton
import com.tecsup.usuario_kotlin.ui.components.SecondaryButton
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun PersonalDataCard(
    nombre: String,
    email: String,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    isUpdating: Boolean = false,
    errorMessage: String? = null
) {
    val enabled = !isBusy && !isUpdating

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Datos personales",
                color = ProfileColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Actualiza la información de tu cuenta.",
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo nombre
            CustomTextField(
                label = "Nombre completo",
                placeholder = "Ingresa tu nombre",
                value = nombre,
                onValueChange = onNombreChange,
                icon = Icons.Outlined.Person,
                enabled = enabled
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${nombre.length}/50 caracteres",
                modifier = Modifier.align(Alignment.End),
                color = if (nombre.length > 50) {
                    ProfileColors.ErrorColor
                } else {
                    ProfileColors.TextSecondary
                },
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo correo electrónico
            CustomTextField(
                label = "Correo electrónico",
                placeholder = "nombre@correo.com",
                value = email,
                onValueChange = onEmailChange,
                icon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                enabled = enabled
            )

            // Mensaje de validación o error
            ErrorMessage(
                message = errorMessage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Guardar datos personales
            PrimaryButton(
                text = "Guardar cambios",
                onClick = onSave,
                enabled = enabled,
                isLoading = isUpdating,
                loadingText = "Guardando..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "Cancelar",
                onClick = onCancel,
                enabled = enabled
            )
        }
    }
}