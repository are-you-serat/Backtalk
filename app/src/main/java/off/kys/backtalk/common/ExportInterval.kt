package off.kys.backtalk.common

import off.kys.backtalk.R

/**
 * Represents the intervals at which auto-exports can occur.
 */
enum class ExportInterval(val titleResId: Int, val days: Int) {
    DAILY(R.string.common_daily, 1),
    WEEKLY(R.string.common_weekly, 7),
    MONTHLY(R.string.common_monthly, 30)
}
