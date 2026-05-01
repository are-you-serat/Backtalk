package off.kys.backtalk.domain.model

/**
 * Represents information about a library used in the application.
 *
 * @property name The name of the library.
 * @property version The version of the library.
 * @property license The license under which the library is distributed.
 */
data class LibraryInfo(
    val name: String,
    val version: String,
    val license: String,
)
