package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 3 to 4.
 * Adds `voicePath`, `voiceDuration`, and `waveformData` columns to the `messages` table.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN voicePath TEXT")
        db.execSQL("ALTER TABLE messages ADD COLUMN voiceDuration INTEGER")
        db.execSQL("ALTER TABLE messages ADD COLUMN waveformData TEXT")
    }
}
