package com.example.breathe

import com.example.breathe.viewmodel.BREATH_PRESETS
import com.example.breathe.viewmodel.BreathPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the integrity of the BREATH_PRESETS catalogue.
 * These presets are user-facing and must remain stable across releases.
 */
class BreathPresetsTest {

    @Test
    fun `BREATH_PRESETS contains exactly 6 presets`() {
        assertEquals(6, BREATH_PRESETS.size)
    }

    @Test
    fun `all presets have unique keys`() {
        val keys = BREATH_PRESETS.map { it.key }
        assertEquals("Duplicate preset keys found", keys.size, keys.distinct().size)
    }

    @Test
    fun `all presets have non-blank labels`() {
        BREATH_PRESETS.forEach { preset ->
            assertTrue("Preset '${preset.key}' has a blank label", preset.label.isNotBlank())
        }
    }

    @Test
    fun `all presets have positive inhale and exhale durations`() {
        BREATH_PRESETS.forEach { preset ->
            assertTrue("${preset.key}: inhaleS must be >= 1", preset.inhaleS >= 1)
            assertTrue("${preset.key}: exhaleS must be >= 1", preset.exhaleS >= 1)
        }
    }

    @Test
    fun `all hold durations are non-negative`() {
        BREATH_PRESETS.forEach { preset ->
            assertTrue("${preset.key}: hold1S must be >= 0", preset.hold1S >= 0)
            assertTrue("${preset.key}: hold2S must be >= 0", preset.hold2S >= 0)
        }
    }

    @Test
    fun `all phase durations are within 1-10 second safe range`() {
        BREATH_PRESETS.forEach { preset ->
            listOf(
                "inhaleS" to preset.inhaleS,
                "exhaleS" to preset.exhaleS,
            ).forEach { (name, value) ->
                assertTrue("${preset.key}: $name=$value must be in 1..10", value in 1..10)
            }
            listOf(
                "hold1S" to preset.hold1S,
                "hold2S" to preset.hold2S,
            ).forEach { (name, value) ->
                assertTrue("${preset.key}: $name=$value must be in 0..10", value in 0..10)
            }
        }
    }

    @Test
    fun `box breathing preset is 4-4-4-4`() {
        val box = BREATH_PRESETS.first { it.key == "box" }
        assertEquals(BreathPreset("box", "Box", 4, 4, 4, 4), box)
    }

    @Test
    fun `4-7-8 preset is correct`() {
        val p = BREATH_PRESETS.first { it.key == "4-7-8" }
        assertEquals(4, p.inhaleS)
        assertEquals(7, p.hold1S)
        assertEquals(8, p.exhaleS)
        assertEquals(0, p.hold2S)
    }

    @Test
    fun `coherent preset has equal inhale and exhale`() {
        val p = BREATH_PRESETS.first { it.key == "coherent" }
        assertEquals("Coherent breathing must have equal inhale/exhale", p.inhaleS, p.exhaleS)
    }
}
