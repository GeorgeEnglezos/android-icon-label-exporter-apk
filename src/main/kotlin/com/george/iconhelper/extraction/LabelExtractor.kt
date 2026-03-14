package com.george.iconhelper.extraction

import android.content.Context
import android.content.pm.ApplicationInfo

class LabelExtractor(private val context: Context) {

    fun getLabel(appInfo: ApplicationInfo): String {
        return try {
            val label = context.packageManager.getApplicationLabel(appInfo).toString()
            label.ifEmpty { appInfo.packageName }
        } catch (e: Exception) {
            appInfo.packageName
        }
    }
}
