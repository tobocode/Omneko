package dev.tobo.omneko

const val PREFERENCE_KEY_FIRST_RUN = "first_run"
const val PREFERENCE_DEFAULT_FIRST_RUN = true

// Preference categories
const val PREFERENCE_CATEGORY_GENERAL = "category_general"
const val PREFERENCE_CATEGORY_DOWNLOAD = "category_download"

// Button preferences
const val PREFERENCE_KEY_UPDATE_YOUTUBEDL = "button_update_youtubedl"
const val PREFERENCE_KEY_LINK_ASSOCIATION = "button_link_association"
const val PREFERENCE_KEY_LANGUAGE = "button_language"

// Preference footers
const val PREFERENCE_FOOTER_CUSTOM_DOWNLOAD_QUALITY = "footer_custom_download_quality"

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

// Download quality preference
const val PREFERENCE_DOWNLOAD_QUALITY_BEST = "best"
const val PREFERENCE_DOWNLOAD_QUALITY_WORST = "worst"
const val PREFERENCE_DOWNLOAD_QUALITY_CUSTOM = "custom"
val PREFERENCE_VALUES_DOWNLOAD_QUALITY = listOf(
    PREFERENCE_DOWNLOAD_QUALITY_BEST,
    PREFERENCE_DOWNLOAD_QUALITY_WORST,
    PREFERENCE_DOWNLOAD_QUALITY_CUSTOM
)

const val PREFERENCE_KEY_DOWNLOAD_QUALITY = "download_quality"
const val PREFERENCE_DEFAULT_DOWNLOAD_QUALITY = PREFERENCE_DOWNLOAD_QUALITY_BEST

const val PREFERENCE_DOWNLOAD_QUALITY_BEST_FORMAT = ""
const val PREFERENCE_DOWNLOAD_QUALITY_WORST_FORMAT = "+size,+br,+res,+fps"

// Custom download quality preference
const val PREFERENCE_KEY_CUSTOM_DOWNLOAD_QUALITY = "custom_download_quality"
const val PREFERENCE_DEFAULT_CUSTOM_DOWNLOAD_QUALITY = ""