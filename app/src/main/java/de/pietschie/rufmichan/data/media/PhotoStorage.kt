package de.pietschie.rufmichan.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

class PhotoStorage(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "contact_photos").also { it.mkdirs() }

    /** Copies and compresses the image at [sourceUri] to internal storage.
     *  Returns the absolute file path, or null on failure. */
    fun savePhoto(sourceUri: Uri): String? {
        return try {
            val dest = File(photosDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val original = BitmapFactory.decodeStream(input) ?: return null
                val scaled = Bitmap.createScaledBitmap(original, 512, 512, true)
                dest.outputStream().use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                if (scaled !== original) original.recycle()
                scaled.recycle()
            }
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Deletes the photo file at [path] if it exists inside our photos directory. */
    fun deletePhoto(path: String) {
        val file = File(path)
        if (file.parentFile?.canonicalPath == photosDir.canonicalPath) {
            file.delete()
        }
    }
}
