package com.example.breathe

import com.breatheonline.breathe.utils.SessionCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class SessionCalculationsTest {

    // ── computeCurrentStreak ──────────────────────────────────────────────────

    @Test
    fun `currentStreak is 0 when no dates`() {
        val streak = SessionCalculations.computeCurrentStreak(emptySet(), LocalDate.now())
        assertEquals(0, streak)
    }

    @Test
    fun `currentStreak is 1 when only today`() {
        val today = LocalDate.of(2026, 3, 30)
        val streak = SessionCalculations.computeCurrentStreak(setOf(today), today)
        assertEquals(1, streak)
    }

    @Test
    fun `currentStreak counts consecutive days ending today`() {
        val today = LocalDate.of(2026, 3, 30)
        val dates = setOf(
            today,
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(3),
        )
        assertEquals(4, SessionCalculations.computeCurrentStreak(dates, today))
    }

    @Test
    fun `currentStreak counts from yesterday when no session today`() {
        val today = LocalDate.of(2026, 3, 30)
        val dates = setOf(
            today.minusDays(1),
            today.minusDays(2),
        )
        assertEquals(2, SessionCalculations.computeCurrentStreak(dates, today))
    }

    @Test
    fun `currentStreak is 0 when last session is two days ago`() {
        val today = LocalDate.of(2026, 3, 30)
        val dates = setOf(today.minusDays(2), today.minusDays(3))
        assertEquals(0, SessionCalculations.computeCurrentStreak(dates, today))
    }

    @Test
    fun `currentStreak stops at gap in dates`() {
        val today = LocalDate.of(2026, 3, 30)
        val dates = setOf(
            today,
            today.minusDays(1),
            // gap on day -2
            today.minusDays(3),
            today.minusDays(4),
        )
        assertEquals(2, SessionCalculations.computeCurrentStreak(dates, today))
    }

    // ── computeLongestStreak ──────────────────────────────────────────────────

    @Test
    fun `longestStreak is 0 for empty set`() {
        assertEquals(0, SessionCalculations.computeLongestStreak(emptySet()))
    }

    @Test
    fun `longestStreak is 1 for single date`() {
        assertEquals(1, SessionCalculations.computeLongestStreak(setOf(LocalDate.of(2026, 1, 1))))
    }

    @Test
    fun `longestStreak picks the longest of two runs`() {
        val base = LocalDate.of(2026, 1, 1)
        val dates = setOf(
            base,
            base.plusDays(1),
            base.plusDays(2),         // run of 3
            base.plusDays(10),
            base.plusDays(11),         // run of 2
        )
        assertEquals(3, SessionCalculations.computeLongestStreak(dates))
    }

    @Test
    fun `longestStreak handles all dates consecutive`() {
        val base = LocalDate.of(2026, 3, 1)
        val dates = (0L until 7L).map { base.plusDays(it) }.toSet()
        assertEquals(7, SessionCalculations.computeLongestStreak(dates))
    }

    @Test
    fun `longestStreak handles scattered single days`() {
        val dates = setOf(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 10),
        )
        assertEquals(1, SessionCalculations.computeLongestStreak(dates))
    }

    // ── formatElapsed ─────────────────────────────────────────────────────────

    @Test
    fun `formatElapsed returns 0 colon 00 for zero seconds`() {
        assertEquals("0:00", SessionCalculations.formatElapsed(0))
    }

    @Test
    fun `formatElapsed pads seconds with zero`() {
        assertEquals("1:05", SessionCalculations.formatElapsed(65))
    }

    @Test
    fun `formatElapsed handles exactly one minute`() {
        assertEquals("1:00", SessionCalculations.formatElapsed(60))
    }

    @Test
    fun `formatElapsed handles large values`() {
        assertEquals("30:00", SessionCalculations.formatElapsed(1800))
    }

    // ── computeBmi ───────────────────────────────────────────────────────────

    @Test
    fun `computeBmi returns null for zero height`() {
        assertNull(SessionCalculations.computeBmi(0f, 70f))
    }

    @Test
    fun `computeBmi returns null for zero weight`() {
        assertNull(SessionCalculations.computeBmi(175f, 0f))
    }

    @Test
    fun `computeBmi calculates correctly`() {
        // 70kg / (1.75m)^2 = 22.86
        val bmi = SessionCalculations.computeBmi(175f, 70f)!!
        assertEquals(22.86f, bmi, 0.01f)
    }

    // ── bmiLabel ──────────────────────────────────────────────────────────────

    @Test
    fun `bmiLabel underweight`() {
        assertEquals("Underweight", SessionCalculations.bmiLabel(17f))
    }

    @Test
    fun `bmiLabel normal`() {
        assertEquals("Normal", SessionCalculations.bmiLabel(22f))
    }

    @Test
    fun `bmiLabel overweight`() {
        assertEquals("Overweight", SessionCalculations.bmiLabel(27f))
    }

    @Test
    fun `bmiLabel obese`() {
        assertEquals("Obese", SessionCalculations.bmiLabel(32f))
    }

    @Test
    fun `bmiLabel boundary 18_5 is Normal`() {
        assertEquals("Normal", SessionCalculations.bmiLabel(18.5f))
    }

    @Test
    fun `bmiLabel boundary 25 is Overweight`() {
        assertEquals("Overweight", SessionCalculations.bmiLabel(25f))
    }
}
