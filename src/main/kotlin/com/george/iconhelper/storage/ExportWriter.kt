package com.george.iconhelper.storage

import android.content.Context
import org.json.JSONObject
import java.io.File

class ExportWriter(private val context: Context) {

    companion object {
        private const val EXPORT_FOLDER = "iconhelper"
    }

    private fun getBaseDirectory(): File {
        val externalDir = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("External storage is not available")
        val dir = File(externalDir, EXPORT_FOLDER)
        dir.mkdirs()
        return dir
    }

    fun getExportDirectory(): File {
        val dir = File(getBaseDirectory(), "icons")
        dir.mkdirs()
        return dir
    }

    fun writeLabelsToJson(labels: Map<String, String>) {
        val jsonObject = JSONObject(labels)
        File(getBaseDirectory(), "labels.json").writeText(jsonObject.toString(2))
    }

    fun writeCategoriesToJson(categories: Map<String, String>) {
        val jsonObject = JSONObject(categories)
        File(getBaseDirectory(), "categories.json").writeText(jsonObject.toString(2))
    }

    fun clearExportDirectory() {
        getBaseDirectory().deleteRecursively()
    }
}
