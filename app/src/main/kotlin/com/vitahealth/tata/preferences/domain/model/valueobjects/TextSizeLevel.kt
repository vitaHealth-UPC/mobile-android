package com.vitahealth.tata.preferences.domain.model.valueobjects

/** Supported text-size choices and their proportional scaling factors. */
enum class TextSizeLevel(val scaleFactor: Float) {
    SMALL(0.9f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f),
}
