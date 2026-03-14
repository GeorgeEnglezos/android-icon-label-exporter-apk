package com.george.iconhelper.extraction

import android.content.pm.ApplicationInfo

class CategoryExtractor {

    companion object {
        private val CATEGORY_NAMES = mapOf(
            ApplicationInfo.CATEGORY_GAME to "Games",
            ApplicationInfo.CATEGORY_AUDIO to "Audio",
            ApplicationInfo.CATEGORY_VIDEO to "Video",
            ApplicationInfo.CATEGORY_IMAGE to "Image",
            ApplicationInfo.CATEGORY_SOCIAL to "Social",
            ApplicationInfo.CATEGORY_NEWS to "News",
            ApplicationInfo.CATEGORY_MAPS to "Maps",
            ApplicationInfo.CATEGORY_PRODUCTIVITY to "Productivity",
        )
    }

    fun getCategory(appInfo: ApplicationInfo): String? {
        return CATEGORY_NAMES[appInfo.category]
    }
}
