package com.loopbreak.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi

/**
 * The app's own language, independent of the phone's. Uses the platform per-app language API,
 * which is available from Android 13; older phones simply follow the system language.
 * Changing it makes Android recreate the activity, so no restart logic is needed.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object AppLanguage {
    /** Index 0 = follow the system, then the languages in the order they appear in the picker. */
    private val tags = listOf<String?>(null, "en", "tr")

    fun currentIndex(context: Context): Int {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) return 0
        return tags.indexOf(locales[0].language).takeIf { it > 0 } ?: 0
    }

    fun select(context: Context, index: Int) {
        val tag = tags.getOrNull(index)
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
    }
}
