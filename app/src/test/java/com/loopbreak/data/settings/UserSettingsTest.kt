package com.loopbreak.data.settings

import com.loopbreak.domain.detect.DetectionConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `defaults of the settings screen match the detection defaults`() {
        // The two live in different places; this keeps them from drifting apart.
        val fromSettings = UserSettings().toConfig()
        val defaults = DetectionConfig()
        assertEquals(defaults.reOpenMinCount, fromSettings.reOpenMinCount)
        assertEquals(defaults.reOpenWindowMs, fromSettings.reOpenWindowMs)
        assertEquals(defaults.quickPeekMaxMs, fromSettings.quickPeekMaxMs)
    }

    @Test
    fun `settings units are converted to milliseconds`() {
        val config = UserSettings(reOpenMinCount = 4, reOpenWindowMin = 15, quickPeekMaxSec = 8).toConfig()
        assertEquals(4, config.reOpenMinCount)
        assertEquals(15 * 60_000L, config.reOpenWindowMs)
        assertEquals(8_000L, config.quickPeekMaxMs)
    }
}
