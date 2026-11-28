package com.tecsup.usuario_kotlin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

private const val MAX_IMAGE_SIZE = 512
private const val MAX_DATA_URL_LENGTH = 300_000
private const val JPEG_PREFIX = "data:image/jpeg;base64,"

// Preparar una imagen para enviarla al backend
suspend fun prepararFotoPerfil(
    context: Context,
    uri: Uri
): String = withContext(Dispatchers.IO) {

    val original = cargarImagen(context, uri)
        ?: throw IllegalArgumentException(
            "No se pudo abrir la imagen seleccionada"
        )

    val reducida = redimensionarImagen(original)
    val imagen = quitarTransparencia(reducida)

    // Comprimir hasta respetar el límite del servidor
    for (calidad in 85 downTo 25 step 10) {

        val bytes = ByteArrayOutputStream().use { output ->

            if (!imagen.compress(
                    Bitmap.CompressFormat.JPEG,
                    calidad,
                    output
                )
            ) {
                throw IllegalArgumentException(
                    "No se pudo comprimir la imagen"
                )
            }

            output.toByteArray()
        }

        val dataUrl = JPEG_PREFIX +
                Base64.encodeToString(bytes, Base64.NO_WRAP)

        if (dataUrl.length <= MAX_DATA_URL_LENGTH) {
            return@withContext dataUrl
        }
    }

    throw IllegalArgumentException(
        "La imagen es demasiado grande. Selecciona otra fotografía."
    )
}

// Cargar una imagen desde el dispositivo
private fun cargarImagen(
    context: Context,
    uri: Uri
): Bitmap? {

    val resolver = context.contentResolver

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        val source = ImageDecoder.createSource(resolver, uri)

        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->

            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

            val ancho = info.size.width
            val alto = info.size.height
            val mayorDimension = maxOf(ancho, alto)

            if (mayorDimension > MAX_IMAGE_SIZE) {

                val escala =
                    MAX_IMAGE_SIZE.toFloat() / mayorDimension

                decoder.setTargetSize(
                    (ancho * escala).roundToInt().coerceAtLeast(1),
                    (alto * escala).roundToInt().coerceAtLeast(1)
                )
            }
        }

    } else {

        // Compatibilidad con Android 7 y 8
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calcularSampleSize(
                bounds.outWidth,
                bounds.outHeight
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: return null

        corregirOrientacion(context, uri, bitmap)
    }
}

// Calcular el tamaño de muestreo de una imagen
private fun calcularSampleSize(
    ancho: Int,
    alto: Int
): Int {

    var sampleSize = 1

    while (
        maxOf(ancho, alto) / sampleSize > MAX_IMAGE_SIZE * 2
    ) {
        sampleSize *= 2
    }

    return sampleSize
}

// Corregir la orientación de fotografías
private fun corregirOrientacion(
    context: Context,
    uri: Uri,
    bitmap: Bitmap
): Bitmap {

    val orientacion = try {

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    val matrix = Matrix()

    when (orientacion) {

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
            matrix.setScale(-1f, 1f)

        ExifInterface.ORIENTATION_ROTATE_180 ->
            matrix.setRotate(180f)

        ExifInterface.ORIENTATION_FLIP_VERTICAL ->
            matrix.setScale(1f, -1f)

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_ROTATE_90 ->
            matrix.setRotate(90f)

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_ROTATE_270 ->
            matrix.setRotate(-90f)
    }

    return if (matrix.isIdentity) {
        bitmap
    } else {
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

// Reducir dimensiones conservando las proporciones
private fun redimensionarImagen(
    bitmap: Bitmap
): Bitmap {

    val mayorDimension = maxOf(bitmap.width, bitmap.height)

    if (mayorDimension <= MAX_IMAGE_SIZE) {
        return bitmap
    }

    val escala = MAX_IMAGE_SIZE.toFloat() / mayorDimension

    return Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * escala).roundToInt().coerceAtLeast(1),
        (bitmap.height * escala).roundToInt().coerceAtLeast(1),
        true
    )
}

// Agregar un fondo blanco a imágenes transparentes
private fun quitarTransparencia(bitmap: Bitmap): Bitmap {

    if (!bitmap.hasAlpha()) return bitmap

    return Bitmap.createBitmap(
        bitmap.width,
        bitmap.height,
        Bitmap.Config.ARGB_8888
    ).also { fondo ->

        val canvas = Canvas(fondo)

        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}

// Convertir una imagen Base64 recibida del backend
suspend fun decodificarFotoPerfil(
    foto: String?
): Bitmap? = withContext(Dispatchers.IO) {

    if (
        foto.isNullOrBlank() ||
        foto.length > MAX_DATA_URL_LENGTH
    ) {
        return@withContext null
    }

    if (!foto.startsWith("data:image/", ignoreCase = true)) {
        return@withContext null
    }

    val separador = foto.indexOf(',')

    if (separador < 0) {
        return@withContext null
    }

    val cabecera = foto.substring(0, separador)

    if (!cabecera.endsWith(";base64", ignoreCase = true)) {
        return@withContext null
    }

    try {

        val bytes = Base64.decode(
            foto.substring(separador + 1),
            Base64.DEFAULT
        )

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size, bounds
        )

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return@withContext null
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calcularSampleSize(
                bounds.outWidth,
                bounds.outHeight
            )
        }

        BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size, options
        )?.let {
            redimensionarImagen(it)
        }

    } catch (e: Exception) {
        null
    }
}