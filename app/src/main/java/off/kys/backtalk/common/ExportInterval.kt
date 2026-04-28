package off.kys.backtalk.common

import off.kys.backtalk.R

/**
 * Represents the intervals at which auto-exports can occur.
 */
enum class ExportInterval(val titleResId: Int, val days: Int) {
    DAILY(R.string.daily, 1),
    WEEKLY(R.string.weekly, 7),
    MONTHLY(R.string.monthly, 30)
}
