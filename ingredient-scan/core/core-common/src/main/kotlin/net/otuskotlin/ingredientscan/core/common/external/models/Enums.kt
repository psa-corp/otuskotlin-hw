package net.otuskotlin.ingredientscan.core.common.external.models

enum class IsCommand {
    NONE,
    // Composition operations
    COMPOSITION_CREATE_MANUAL,
    COMPOSITION_CREATE_PHOTOS,
    COMPOSITION_GET,
    COMPOSITION_CONTEXT_GET,
    // Analysis operations
    ANALYSIS_GET,
    ANALYSIS_REGENERATE,
    DOWNLOAD_FILE,
}

enum class IsSubCommand {
    NONE,
    // Composition operations
    COMPOSITION_CREATE,
    OCR_RECOGNITION,
    COMPOSITION_VALIDATE,
    COMPOSITION_SAVE,
    COMPOSITION_OUTPUT,
    // Analysis operations
    ANALYSIS_CREATE,
    ANALYSIS_REGENERATE,
    ANALYSIS_OUTPUT
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
    DARK_RED,
    RED,
    ORANGE,
    YELLOW,
    LIGHT_YELLOW,
    LIGHT_GREEN,
    GREEN,
    DARK_GREEN
}