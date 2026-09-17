package com.bazyak.walletplus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bazyak.walletplus.data.model.Pass

/**
 * Room database for storing passes.
 * Singleton pattern ensures single database instance.
 */
@Database(
    entities = [Pass::class],
    version = 5,
    exportSchema = false,
)
@TypeConverters(PassTypeConverters::class)
abstract class PassDatabase : RoomDatabase() {
    abstract fun passDao(): PassDao

    companion object {
        @Volatile
        private var INSTANCE: PassDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new image path columns
                database.execSQL("ALTER TABLE passes ADD COLUMN logoPath TEXT")
                database.execSQL("ALTER TABLE passes ADD COLUMN stripPath TEXT")
                database.execSQL("ALTER TABLE passes ADD COLUMN backgroundPath TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add displayOrder column with default value 0
                database.execSQL("ALTER TABLE passes ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")

                // Assign sequential order based on existing importedDate DESC to preserve current order
                // Using a temporary table approach for reliable ordering
                database.execSQL(
                    """
                    UPDATE passes
                    SET displayOrder = (
                        SELECT COUNT(*)
                        FROM passes AS p2
                        WHERE p2.importedDate > passes.importedDate
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lastRefreshDate column (nullable, default null)
                database.execSQL("ALTER TABLE passes ADD COLUMN lastRefreshDate INTEGER")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add autoRefreshEnabled column (default true = 1)
                database.execSQL("ALTER TABLE passes ADD COLUMN autoRefreshEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): PassDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                PassDatabase::class.java,
                "pass_database",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
