package off.kys.backtalk.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit

class BacktalkPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("backtalk_settings", Context.MODE_PRIVATE)
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_LOCK_ENABLED = "lock_enabled"
        const val KEY_SECURE_SCREEN = "secure_screen"
    }

    /**
     * Registers a callback that triggers whenever a preference is modified.
     * Don't forget that SharedPreferences keeps a weak reference to the listener,
     * so we store it in a class property to prevent the Garbage Collector from
     * eating it immediately.
     */
    fun observeChanges(onChanged: (String) -> Unit) {
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            key?.let { onChanged(it) }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Call this when the lifecycle ends, unless you enjoy memory leaks.
     */
    fun unregisterObserver() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
        listener = null
    }

    var lockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_LOCK_ENABLED, value) }

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(
            prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name
        )
        set(value) = prefs.edit { putString(KEY_THEME_MODE, value.name) }


    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }


    var secureScreenEnabled: Boolean
        get() = prefs.getBoolean(KEY_SECURE_SCREEN, false)
        set(value) = prefs.edit { putBoolean(KEY_SECURE_SCREEN, value) }

}