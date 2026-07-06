package com.breatheonline.breathe.ui.screens

/**
 * Pure content data for the Privacy Policy screen.
 * Keeping text here (rather than inline in composables) makes updates easy,
 * and prepares the content for future string-resource localisation.
 */

internal const val PRIVACY_LAST_UPDATED = "March 23, 2026"
internal const val PRIVACY_CONTACT_EMAIL = "privacy@breatheapp.co"

/** A single bullet-level entry within a policy section. */
internal sealed class PolicyItem {
    /** Regular paragraph text. */
    data class Plain(val text: String) : PolicyItem()
    /** Text with a bold prefix followed by normal text. */
    data class BoldPrefix(val bold: String, val rest: String) : PolicyItem()
    /** A bullet wrapping either a Plain or BoldPrefix item. */
    data class Bullet(val inner: PolicyItem) : PolicyItem()
}

/** Identifies the Material icon to use for a policy section. Resolved to ImageVector in the screen. */
internal enum class PolicyIcon { STORAGE, VISIBILITY, LOCK, LANGUAGE, HOW_TO_REG, DELETE, SECURITY, EMAIL }

internal data class PolicySectionData(
    val icon: PolicyIcon,
    val title: String,
    val items: List<PolicyItem>,
)

internal val PRIVACY_SECTIONS: List<PolicySectionData> = listOf(
    PolicySectionData(
        icon  = PolicyIcon.STORAGE,
        title = "Information We Collect",
        items = listOf(
            PolicyItem.Plain("We collect only what is necessary to provide Breathe's features:"),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Account data", " — email address and display name, provided voluntarily when you sign up. Google OAuth provides only your name and email.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Session data", " — breathing technique used, session duration, phase timings, and optional notes you write.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Biometric data (opt-in)", " — heart rate readings from your device, collected only with your explicit consent via the Data Consent screen.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Globe pins", " — latitude, longitude, and optional public message if you choose to drop a pin on the community globe.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Usage data", " — anonymous analytics such as page views and feature interactions. No personally identifiable information is attached.")),
            PolicyItem.Plain("You can use all core breathing features without creating an account. Account creation is optional."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.VISIBILITY,
        title = "How We Use Your Data",
        items = listOf(
            PolicyItem.Plain("Your data is used solely to operate and improve Breathe:"),
            PolicyItem.Bullet(PolicyItem.Plain("Saving and displaying your session history and streaks.")),
            PolicyItem.Bullet(PolicyItem.Plain("Personalising your breathing statistics and progress charts.")),
            PolicyItem.Bullet(PolicyItem.Plain("Powering the community globe (only pins you explicitly create are shown publicly).")),
            PolicyItem.Bullet(PolicyItem.Plain("Sending optional push notifications for streak reminders (only if you enable them).")),
            PolicyItem.Bullet(PolicyItem.Plain("Improving app performance and fixing bugs using anonymised usage analytics.")),
            PolicyItem.BoldPrefix("We never", " sell your data, use it for advertising, or share it with third parties for their own purposes."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.LOCK,
        title = "Data Storage & Security",
        items = listOf(
            PolicyItem.Bullet(PolicyItem.Plain("All data is encrypted in transit via HTTPS/TLS and encrypted at rest in our database.")),
            PolicyItem.Bullet(PolicyItem.Plain("Passwords are hashed using bcrypt. We never store plaintext passwords.")),
            PolicyItem.Bullet(PolicyItem.Plain("Biometric (heart rate) data is stored separately and access-controlled so only you can read it.")),
            PolicyItem.Bullet(PolicyItem.Plain("We use industry-standard practices to protect against unauthorised access, alteration, and deletion.")),
            PolicyItem.Plain("Despite our best efforts, no system is 100% secure. If you discover a security issue, please contact us at $PRIVACY_CONTACT_EMAIL."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.LANGUAGE,
        title = "Third-Party Services",
        items = listOf(
            PolicyItem.Plain("Breathe uses a small number of third-party services:"),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Google OAuth", " — for social sign-in. Google receives only the authentication request. We store only your name and email.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("MongoDB Atlas", " — our database host. Data is stored in encrypted clusters with strict access controls.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Web Speech API", " — voice guidance runs entirely in your browser. No audio is sent to any server.")),
            PolicyItem.Plain("No data is shared with advertising networks, data brokers, or social media platforms."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.HOW_TO_REG,
        title = "Your Rights",
        items = listOf(
            PolicyItem.Plain("You have full control over your data:"),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Access", " — view all your session history and stats from your Profile page at any time.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Delete sessions", " — remove individual sessions from your session history.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Delete biometric data", " — revoke heart-rate consent and erase all stored readings from Profile → Data & Privacy.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Delete account", " — permanently erase your account and all associated data. Contact us at the email below.")),
            PolicyItem.Bullet(PolicyItem.BoldPrefix("Export", " — request a copy of your data at any time by emailing us.")),
            PolicyItem.Plain("If you are in the EU/EEA, you also have rights under GDPR including the right to object to processing and to lodge a complaint with your supervisory authority."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.DELETE,
        title = "Data Retention",
        items = listOf(
            PolicyItem.Bullet(PolicyItem.Plain("Session data is retained for as long as your account is active or until you delete it.")),
            PolicyItem.Bullet(PolicyItem.Plain("If you delete your account, all personal data is permanently removed within 30 days.")),
            PolicyItem.Bullet(PolicyItem.Plain("Anonymised, aggregated analytics data (no personal identifiers) may be retained indefinitely to improve the service.")),
            PolicyItem.Bullet(PolicyItem.Plain("Globe pins you have posted are removed immediately when you delete them or your account.")),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.SECURITY,
        title = "Children's Privacy",
        items = listOf(
            PolicyItem.Plain("Breathe is not directed at children under 13. We do not knowingly collect personal information from children under 13. If you believe a child has provided us personal data, please contact us and we will delete it promptly."),
        )
    ),
    PolicySectionData(
        icon  = PolicyIcon.EMAIL,
        title = "Contact Us",
        items = listOf(
            PolicyItem.Plain("For privacy questions, data requests, or to report a concern:"),
            PolicyItem.Plain("We aim to respond to all privacy-related requests within 5 business days."),
        )
    ),
)
