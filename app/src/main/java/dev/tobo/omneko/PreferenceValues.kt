package dev.tobo.omneko

// Theme preference
const val PREFERENCE_THEME_SYSTEM = "system"
const val PREFERENCE_THEME_DARK = "dark"
const val PREFERENCE_THEME_LIGHT = "light"
val PREFERENCE_VALUES_THEME = listOf(
    PREFERENCE_THEME_SYSTEM,
    PREFERENCE_THEME_DARK,
    PREFERENCE_THEME_LIGHT
)

const val PREFERENCE_KEY_THEME = "theme"
const val PREFERENCE_DEFAULT_THEME = PREFERENCE_THEME_SYSTEM

// Max comments preference
const val PREFERENCE_KEY_MAX_COMMENTS = "max_comments"
val PREFERENCE_LIST_MAX_COMMENTS = listOf(0, 10, 50, 100, 1000)
const val PREFERENCE_DEFAULT_MAX_COMMENTS = 100

// Aria2c preference
const val PREFERENCE_KEY_USE_ARIA2C = "use_aria2c"
const val PREFERENCE_DEFAULT_USE_ARIA2C = false