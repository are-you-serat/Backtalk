package off.kys.backtalk.common

import java.util.concurrent.TimeUnit

object Constants {

    // Define constants for grouping logic
    val TIME_GAP_FOR_HEADER = TimeUnit.HOURS.toMillis(1)
    val TIME_GAP_FOR_GROUPING = TimeUnit.MINUTES.toMillis(1)

    const val BACKTALK_MIT_LICENSE_RAW_URL: String = "https://raw.githubusercontent.com/kys0ff/Backtalk/refs/heads/master/LICENSE"

}