package com.breatheonline.breathe.ui.screens

/**
 * Pure data for the FAQ screen.
 * Extracted here to keep FaqScreen.kt focused on rendering,
 * and to make content easy to find, update, and eventually localise.
 */

internal data class FaqItem(val q: String, val a: String)
internal data class FaqCategory(val id: String, val iconKey: String, val label: String, val items: List<FaqItem>)

internal val FAQ_DATA = listOf(
    FaqCategory(
        id = "basics", iconKey = "sprout", label = "Getting Started",
        items = listOf(
            FaqItem(
                "What is Breathe?",
                "Breathe is a guided breathing and meditation app. You follow an animated orb that expands and contracts - inhaling when it grows, exhaling when it shrinks. No prior experience needed.",
            ),
            FaqItem(
                "Do I need to create an account?",
                "No. You can use the breathing sessions completely free without an account. Creating an account lets you save your session history, track streaks, and join the community.",
            ),
            FaqItem(
                "How long should my first session be?",
                "Start with 3-5 minutes. Even 2 minutes of controlled breathing creates a measurable shift in your nervous system. Build up gradually - most users settle into 10-15 minute daily sessions after a week.",
            ),
            FaqItem(
                "What time of day is best?",
                "Morning sessions set a calm baseline for the day. Evening sessions help you wind down. The best time is simply when you can be consistent - even a lunch break works great.",
            ),
        ),
    ),
    FaqCategory(
        id = "techniques", iconKey = "waves", label = "Techniques",
        items = listOf(
            FaqItem(
                "What is Box Breathing (4-4-4-4)?",
                "Inhale for 4 seconds, hold for 4, exhale for 4, hold again for 4. Used by Navy SEALs and athletes to regain focus under pressure. Excellent for stress and pre-performance anxiety.",
            ),
            FaqItem(
                "What is 4-7-8 breathing?",
                "Inhale for 4 seconds, hold for 7, then exhale slowly for 8. The extended exhale activates your parasympathetic nervous system - lowering heart rate and cortisol. Most people feel drowsy within 2-3 cycles.",
            ),
            FaqItem(
                "What is the Wim Hof Method?",
                "30 deep power breaths followed by a breath retention. This temporarily floods your body with oxygen and adrenaline, creating an energised, alert state. Not recommended before driving or in water.",
            ),
            FaqItem(
                "What is Coherent Breathing?",
                "Breathing at ~5.5 breaths per minute synchronises your heart rate variability (HRV) to its resonant frequency - associated with reduced anxiety, improved mood, and better cardiovascular health.",
            ),
            FaqItem(
                "Can I create a custom pattern?",
                "Yes. On the breathing screen you can set your own inhale, hold, exhale, and hold-out durations. Your custom pattern is used for the session immediately.",
            ),
        ),
    ),
    FaqCategory(
        id = "science", iconKey = "brain", label = "The Science",
        items = listOf(
            FaqItem(
                "Why does slow breathing reduce stress?",
                "Slow, controlled exhales stimulate the vagus nerve, which directly signals your brain to reduce the stress response. Heart rate drops, cortisol decreases, and prefrontal cortex activity increases.",
            ),
            FaqItem(
                "What is heart rate variability (HRV)?",
                "HRV is the variation in time between heartbeats. Higher HRV indicates a more resilient nervous system. Breathing exercises - especially coherent breathing - are one of the most effective ways to improve HRV over time.",
            ),
            FaqItem(
                "How many sessions until I feel a difference?",
                "Most people notice a shift in their anxiety levels after 5-7 consistent sessions. Structural benefits to HRV and baseline calm typically appear after 3-4 weeks of daily practice.",
            ),
        ),
    ),
    FaqCategory(
        id = "account", iconKey = "user", label = "Account",
        items = listOf(
            FaqItem(
                "How are my sessions saved?",
                "Sessions are automatically saved when you stop a breathing session. You can add a mood rating and notes in the feedback modal that appears after stopping.",
            ),
            FaqItem(
                "What does mood tracking do?",
                "After each session you can log your mood before and after on a 1-10 scale. Over time the Stats page shows your mood trends so you can see how meditation is shifting your baseline.",
            ),
            FaqItem(
                "Can I delete my session history?",
                "In-app session deletion is not yet available. To remove your session history, contact support and we will process the request for you.",
            ),
            FaqItem(
                "How do I delete my account?",
                "Account deletion is handled by our support team. Contact us and we will permanently remove all your data including sessions, mood logs, and streaks.",
            ),
        ),
    ),
    FaqCategory(
        id = "community", iconKey = "globe", label = "Community",
        items = listOf(
            FaqItem(
                "Who can read community posts?",
                "Everyone - you do not need an account to read posts and comments. Creating an account is only required to write posts, leave comments, or like content.",
            ),
            FaqItem(
                "What can I post?",
                "Share your experiences, ask questions about techniques, celebrate streaks and milestones, or post tips you have discovered. Keep it supportive and on-topic.",
            ),
            FaqItem(
                "How do I report inappropriate content?",
                "Tap the flag icon on any post to report it. Reported posts are reviewed manually. We aim to act on reports within 24 hours.",
            ),
        ),
    ),
)
