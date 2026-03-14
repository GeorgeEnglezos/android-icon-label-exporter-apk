package com.george.iconhelper.extraction

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream

class IconExtractor(private val context: Context) {

    companion object {
        private const val MAX_ICON_SIZE = 192
    }

    fun extractIconToFile(appInfo: ApplicationInfo, outputFile: File): Boolean {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(appInfo)
            val bitmap = drawableToBitmap(drawable)
            saveBitmapToPng(bitmap, outputFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            return if (bmp.width <= MAX_ICON_SIZE && bmp.height <= MAX_ICON_SIZE) {
                bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, false)
            } else {
                Bitmap.createScaledBitmap(bmp, MAX_ICON_SIZE, MAX_ICON_SIZE, true)
            }
        }

        val width = drawable.intrinsicWidth.coerceIn(1, MAX_ICON_SIZE)
        val height = drawable.intrinsicHeight.coerceIn(1, MAX_ICON_SIZE)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun saveBitmapToPng(bitmap: Bitmap, file: File) {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
    }
}
