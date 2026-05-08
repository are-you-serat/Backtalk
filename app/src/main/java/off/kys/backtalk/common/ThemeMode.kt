package off.kys.backtalk.common

/**
 * Represents the theme mode of the application.
 */
enum class ThemeMode {
    /**
     * Forced light theme.
     */
    LIGHT,

    /**
     * Forced dark theme.
     */
    DARK,

    /**
     * Theme follows the system setting.
     */
    AUTO;

    /**
     * Determines if dark theme should be applied based on the [ThemeMode]
     * and the current system setting.
     */
    fun isDark(systemInDark: Boolean): Boolean = when (this) {
        DARK -> true
        LIGHT -> false
        AUTO -> systemInDark
    }
}
