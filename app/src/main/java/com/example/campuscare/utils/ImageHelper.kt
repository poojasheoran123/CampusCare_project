package com.example.campuscare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageHelper {

    private const val TAG = "ImageHelper"
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 78

    /**
     * Reads an image from sourceUri (camera file or gallery content),
     * corrects EXIF rotation, scales down to max 1280px, compresses to JPEG ~82%,
     * and saves permanently into internal app storage (filesDir/report_photos).
     *
     * Returns the persistent "file://..." URI string, which survives app kills/restarts.
     */
    fun saveAndCompressImage(context: Context, sourceUri: Uri): String? {
        return try {
            val rotationDegrees = getExifRotation(context, sourceUri)

            // Step 1: Decode image bounds
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream(context, sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            val origWidth = bounds.outWidth
            val origHeight = bounds.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                Log.w(TAG, "Failed to decode image bounds from $sourceUri")
                return null
            }

            // Step 2: Compute inSampleSize to prevent high-res OOM
            var inSampleSize = 1
            while ((origWidth / (inSampleSize * 2)) >= MAX_DIMENSION &&
                (origHeight / (inSampleSize * 2)) >= MAX_DIMENSION
            ) {
                inSampleSize *= 2
            }

            // Step 3: Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val rawBitmap = openStream(context, sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Step 4: Scale to exact max dimension & apply rotation
            val currentWidth = rawBitmap.width
            val currentHeight = rawBitmap.height
            val maxSide = maxOf(currentWidth, currentHeight)
            val scale = if (maxSide > MAX_DIMENSION) MAX_DIMENSION.toFloat() / maxSide else 1.0f

            val matrix = Matrix()
            if (scale < 1.0f) {
                matrix.postScale(scale, scale)
            }
            if (rotationDegrees != 0f) {
                matrix.postRotate(rotationDegrees)
            }

            val finalBitmap = if (!matrix.isIdentity) {
                val transformed = Bitmap.createBitmap(rawBitmap, 0, 0, currentWidth, currentHeight, matrix, true)
                if (transformed != rawBitmap) {
                    rawBitmap.recycle()
                }
                transformed
            } else {
                rawBitmap
            }

            // Step 5: Save into permanent internal storage
            val reportsDir = File(context.filesDir, "report_photos").apply {
                if (!exists()) mkdirs()
            }
            val destFile = File(reportsDir, "report_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
            FileOutputStream(destFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            finalBitmap.recycle()

            Log.d(TAG, "Image saved permanently: ${destFile.absolutePath} (${destFile.length() / 1024} KB)")
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing and saving image", e)
            null
        }
    }

    /**
     * Retrieves compressed JPEG byte array suitable for background email attachment.
     * Guaranteed to be compact (< 400 KB) so JavaMail never times out over SMTP.
     */
    fun getCompressedImageBytes(context: Context, imageUriString: String?): ByteArray? {
        if (imageUriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(imageUriString)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    // If already compressed and small (< 1 MB), read directly
                    if (file.length() < 1024 * 1024) {
                        return file.readBytes()
                    }
                }
            }

            // If not yet compressed or content URI, compress into memory
            val rotationDegrees = getExifRotation(context, uri)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            val origWidth = bounds.outWidth
            val origHeight = bounds.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                return openStream(context, uri)?.use { it.readBytes() }
            }

            var inSampleSize = 1
            while ((origWidth / (inSampleSize * 2)) >= MAX_DIMENSION &&
                (origHeight / (inSampleSize * 2)) >= MAX_DIMENSION
            ) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val rawBitmap = openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val currentWidth = rawBitmap.width
            val currentHeight = rawBitmap.height
            val maxSide = maxOf(currentWidth, currentHeight)
            val scale = if (maxSide > MAX_DIMENSION) MAX_DIMENSION.toFloat() / maxSide else 1.0f

            val matrix = Matrix()
            if (scale < 1.0f) matrix.postScale(scale, scale)
            if (rotationDegrees != 0f) matrix.postRotate(rotationDegrees)

            val finalBitmap = if (!matrix.isIdentity) {
                val transformed = Bitmap.createBitmap(rawBitmap, 0, 0, currentWidth, currentHeight, matrix, true)
                if (transformed != rawBitmap) rawBitmap.recycle()
                transformed
            } else {
                rawBitmap
            }

            val outStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outStream)
            finalBitmap.recycle()
            outStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting compressed bytes for email", e)
            try {
                val uri = Uri.parse(imageUriString)
                openStream(context, uri)?.use { it.readBytes() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun openStream(context: Context, uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                if (file.exists()) file.inputStream() else null
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getExifRotation(context: Context, uri: Uri): Float {
        return try {
            val stream = openStream(context, uri) ?: return 0f
            val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(stream)
            } else {
                null
            }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            stream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (_: Exception) {
            0f
        }
    }
}
