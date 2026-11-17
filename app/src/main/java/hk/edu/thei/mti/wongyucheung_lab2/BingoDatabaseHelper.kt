package hk.edu.thei.mti.wongyucheung_lab2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Data class representing a single recorded Bingo match event.
 * Stores game series identification, match index, user handle, performance stats,
 * winning numbers, and the precise timestamp.
 */
data class BingoRecord(
    val id: Int,
    val gameNumber: Int,
    val matchNumber: Int,
    val username: String,
    val roundNumber: Int,
    val bingoNumbers: String,
    val systemTime: String
)

/**
 * SQLiteOpenHelper manager class responsible for establishing the local database,
 * executing table creation schemas, and handling historical record persistence.
 */
class BingoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BingoGame.db"
        // Version 2 incorporates game_number and match_number schema attributes
        private const val DATABASE_VERSION = 2

        // Table and column definitions
        const val TABLE_NAME = "bingo_history"
        const val COL_ID = "id"
        const val COL_GAME_NUMBER = "game_number"
        const val COL_MATCH_NUMBER = "match_number"
        const val COL_USERNAME = "username"
        const val COL_ROUND_NUMBER = "round_number"
        const val COL_BINGO_NUMBERS = "bingo_numbers"
        const val COL_SYSTEM_TIME = "system_time"
    }

    /**
     * Called when the database file is created for the first time.
     * Constructs the bingo_history table with auto-incrementing primary keys.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_GAME_NUMBER INTEGER NOT NULL,
                $COL_MATCH_NUMBER INTEGER NOT NULL,
                $COL_USERNAME TEXT NOT NULL,
                $COL_ROUND_NUMBER INTEGER NOT NULL,
                $COL_BINGO_NUMBERS TEXT NOT NULL,
                $COL_SYSTEM_TIME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    /**
     * Handles schema upgrades by dropping legacy tables and rebuilding.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Inserts a newly completed match record into the database table.
     *
     * @return The row ID of the newly inserted entry, or -1L if insertion failed.
     */
    fun insertBingoRecord(
        gameNumber: Int,
        matchNumber: Int,
        username: String,
        roundNumber: Int,
        fiveNumbers: List<Int>,
        systemTime: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GAME_NUMBER, gameNumber)
            put(COL_MATCH_NUMBER, matchNumber)
            put(COL_USERNAME, username)
            put(COL_ROUND_NUMBER, roundNumber)
            put(COL_BINGO_NUMBERS, fiveNumbers.joinToString(", "))
            put(COL_SYSTEM_TIME, systemTime)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    /**
     * Queries and returns all stored Bingo match records sorted descending by ID (newest first).
     */
    fun getAllRecords(): List<BingoRecord> {
        val records = mutableListOf<BingoRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COL_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                records.add(
                    BingoRecord(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        gameNumber = it.getInt(it.getColumnIndexOrThrow(COL_GAME_NUMBER)),
                        matchNumber = it.getInt(it.getColumnIndexOrThrow(COL_MATCH_NUMBER)),
                        username = it.getString(it.getColumnIndexOrThrow(COL_USERNAME)),
                        roundNumber = it.getInt(it.getColumnIndexOrThrow(COL_ROUND_NUMBER)),
                        bingoNumbers = it.getString(it.getColumnIndexOrThrow(COL_BINGO_NUMBERS)),
                        systemTime = it.getString(it.getColumnIndexOrThrow(COL_SYSTEM_TIME))
                    )
                )
            }
        }
        return records
    }

    /**
     * Deletes all records stored in the bingo_history table.
     */
    fun clearAllRecords() {
        writableDatabase.delete(TABLE_NAME, null, null)
    }
}