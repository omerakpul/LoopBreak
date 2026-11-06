package com.loopbreak.data.usage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Packages that must never count as loops: our own app, launchers, system UI and keyboards,
 * plus whatever the user excluded in Settings. Computed on demand because launchers and
 * keyboards can change.
 */
@Singleton
class ExcludedPackages @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Excluded no matter what; not offered in the Settings list. */
    fun systemDefaults(): Set<String> {
        val pm = context.packageManager
        val out = mutableSetOf(context.packageName)
        out += SYSTEM_NOISE

        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        // The default launcher, plus any other installed launcher. Some system apps (notably
        // Settings) register a "FallbackHome" activity for the HOME category; those are not launchers.
        pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
            ?.let { out += it }
        pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY)
            .filterNot { it.activityInfo.name.endsWith("FallbackHome") }
            .forEach { out += it.activityInfo.packageName }

        context.getSystemService(InputMethodManager::class.java)
            ?.enabledInputMethodList
            ?.forEach { out += it.packageName }

        return out
    }

    private companion object {
        /** System surfaces that show up as short foreground sessions but are not user behaviour. */
        val SYSTEM_NOISE = setOf(
            "android",
            "com.android.systemui",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.android.settings.intelligence",
            "com.google.android.gms",
        )
    }

    fun get(userExcluded: Set<String> = emptySet()): Set<String> = systemDefaults() + userExcluded
}
