package net.otuskotlin.ingredientscan.core.common.external.models

enum class IsCommand {
    NONE,
    COMPOSITION_CREATE_MANUAL,
    COMPOSITION_CREATE_PHOTOS,
    COMPOSITION_GET,
    COMPOSITION_CONTEXT_GET,
    ANALYSIS_GET,
    ANALYSIS_CREATE,
    ANALYSIS_REGENERATE,
    DOWNLOAD_FILE,
}

enum class IsSubCommand {
    NONE,
    COMPOSITION_CREATE,
    OCR_RECOGNITION,
    COMPOSITION_VALIDATE,
    ANALYSIS_CREATE,
    ANALYSIS_REGENERATE,
    ANALYSIS_VALIDATE,
    READY
}

enum class IsLightCommand {
    NONE,
    COMPOSITION_VALIDATION,
    COMPOSITION_SAVE,
    OCR_RECOGNITION,
    ANALYZER,
    ANALYSIS_SAVE,
}

enum class InternalCommand {
    NONE,
    ANALYSIS_FIND,
    ANALYSIS_SAVE,
    COMPOSITION_FIND,
    COMPOSITION_SAVE,
}

enum class IsState {
    NONE,
    RUNNING,
    FAILING,
    FINISHING
}

enum class IsWorkMode {
    PROD,
    TEST,
    STUB
}

enum class IsScanType {
    NONE,
    MANUAL,
    PHOTO
}

enum class IsRiskLevel {
    NONE,
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class IsColor {
    NONE,
    VERY_DARK_RED,
    DARK_RED,
    DEEP_RED,
    RED,
    LIGHT_RED,
    RED_ORANGE,
    ORANGE,
    LIGHT_ORANGE,
    DARK_YELLOW,
    YELLOW,
    LIGHT_YELLOW,
    YELLOW_GREEN,
    PALE_GREEN,
    LIGHT_GREEN,
    GREEN,
    MEDIUM_GREEN,
    BRIGHT_GREEN,
    VIBRANT_GREEN,
    FRESH_GREEN,
    BRILLIANT_GREEN
}