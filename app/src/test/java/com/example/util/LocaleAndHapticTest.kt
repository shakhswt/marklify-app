package com.example.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LocaleAndHapticTest {

    @Before
    fun setUp() {
        HapticManager.resetCooldown()
    }

    @Test
    fun testLocaleResolution() {
        val uzLocale = LocaleHelper.getLocale("uz")
        assertEquals("uz", uzLocale.language)

        val ruLocale = LocaleHelper.getLocale("ru")
        assertEquals("ru", ruLocale.language)

        val enLocale = LocaleHelper.getLocale("en")
        assertEquals("en", enLocale.language)

        // Fallback for unknown languages or uppercase/whitespace
        val fallbackLocale = LocaleHelper.getLocale("unknown")
        assertEquals("en", fallbackLocale.language)

        val trimmedUz = LocaleHelper.getLocale("  UZ  ")
        assertEquals("uz", trimmedUz.language)
    }

    @Test
    fun testHapticDebounceWithinWindow() {
        val t0 = 100000L
        // First occurrence: should NOT debounce (allowed)
        assertFalse(HapticManager.shouldDebounce(t0))

        // Trigger within 500ms (less than 1800ms cooldown): MUST debounce
        assertTrue(HapticManager.shouldDebounce(t0 + 500L))

        // Trigger at 1799ms: MUST debounce
        assertTrue(HapticManager.shouldDebounce(t0 + 1799L))
    }

    @Test
    fun testHapticDebounceAfterWindow() {
        val t0 = 100000L
        assertFalse(HapticManager.shouldDebounce(t0))

        // Trigger at exactly 1800ms: should be allowed (not debounced)
        assertFalse(HapticManager.shouldDebounce(t0 + 1800L))

        // After another 2000ms: allowed again
        assertFalse(HapticManager.shouldDebounce(t0 + 3800L))
    }

    @Test
    fun testHapticCooldownReset() {
        val t0 = 100000L
        assertFalse(HapticManager.shouldDebounce(t0))
        assertTrue(HapticManager.shouldDebounce(t0 + 100L))

        // Reset cooldown
        HapticManager.resetCooldown()

        // After reset, immediate trigger is allowed
        assertFalse(HapticManager.shouldDebounce(t0 + 200L))
    }
}
