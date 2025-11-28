package net.otuskotlin.ingredientscan.core.common.models

enum class IsCommand {
    NONE,
    // Composition operations
    COMPOSITION_CREATE_MANUAL,
    COMPOSITION_CREATE_PHOTOS,
    COMPOSITION_GET,
    // Analysis operations
    ANALYSIS_GET,
    ANALYSIS_REGENERATE,
    // Component operations
    COMPONENT_GET,
    COMPONENT_SEARCH,
    // Scan operations
    SCAN_GET,
    // File operations
    DOWNLOAD_FILE,
    UPLOADER_UPLOAD,
    // System operations
    INIT,
    FINISH
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