package com.tecsup.usuario_kotlin.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tecsup.usuario_kotlin.data.model.TranslationHistoryDto
import com.tecsup.usuario_kotlin.data.model.UserProfileDto

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "senasapp.db"
        private const val DATABASE_VERSION = 1

        // Table Users
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "username"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"
        private const val COLUMN_USER_IS_ADMIN = "is_admin"

        // Table Translations
        private const val TABLE_TRANSLATIONS = "translations"
        private const val COLUMN_TRANS_ID = "id"
        private const val COLUMN_TRANS_PHRASE = "phrase"
        private const val COLUMN_TRANS_TIMESTAMP = "timestamp"
        private const val COLUMN_TRANS_CONFIDENCE = "confidence"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_IS_ADMIN + " INTEGER DEFAULT 0" + ")")

        val createTranslationsTable = ("CREATE TABLE " + TABLE_TRANSLATIONS + "("
                + COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TRANS_PHRASE + " TEXT,"
                + COLUMN_TRANS_TIMESTAMP + " TEXT,"
                + COLUMN_TRANS_CONFIDENCE + " REAL" + ")")

        db.execSQL(createUsersTable)
        db.execSQL(createTranslationsTable)

        // Seed Initial Default Users
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSLATIONS")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // User 1: Magy R
        val user1 = ContentValues().apply {
            put(COLUMN_USER_NAME, "Magy R")
            put(COLUMN_USER_EMAIL, "magy@gmail.com")
            put(COLUMN_USER_PASSWORD, "password123")
            put(COLUMN_USER_IS_ADMIN, 1)
        }
        db.insert(TABLE_USERS, null, user1)

        // User 2: Admin
        val user2 = ContentValues().apply {
            put(COLUMN_USER_NAME, "Admin")
            put(COLUMN_USER_EMAIL, "admin@senasapp.com")
            put(COLUMN_USER_PASSWORD, "admin123")
            put(COLUMN_USER_IS_ADMIN, 1)
        }
        db.insert(TABLE_USERS, null, user2)

        // Initial Translations
        val t1 = ContentValues().apply {
            put(COLUMN_TRANS_PHRASE, "Buenos días, ¿cómo estás?")
            put(COLUMN_TRANS_TIMESTAMP, "Hace 5 min")
            put(COLUMN_TRANS_CONFIDENCE, 0.95)
        }
        val t2 = ContentValues().apply {
            put(COLUMN_TRANS_PHRASE, "Necesito ir al baño")
            put(COLUMN_TRANS_TIMESTAMP, "Hace 20 min")
            put(COLUMN_TRANS_CONFIDENCE, 0.89)
        }
        val t3 = ContentValues().apply {
            put(COLUMN_TRANS_PHRASE, "Gracias por tu ayuda")
            put(COLUMN_TRANS_TIMESTAMP, "Ayer")
            put(COLUMN_TRANS_CONFIDENCE, 0.94)
        }
        db.insert(TABLE_TRANSLATIONS, null, t1)
        db.insert(TABLE_TRANSLATIONS, null, t2)
        db.insert(TABLE_TRANSLATIONS, null, t3)
    }

    // --- USER DATABASE OPERATIONS ---

    fun insertUser(username: String, email: String, pass: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, username)
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, pass)
            put(COLUMN_USER_IS_ADMIN, 0)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun getUserByEmailOrUsername(identifier: String): UserProfileDto? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_IS_ADMIN),
            "$COLUMN_USER_EMAIL = ? OR $COLUMN_USER_NAME = ?",
            arrayOf(identifier, identifier),
            null, null, null
        )

        var user: UserProfileDto? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            val isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ADMIN)) == 1
            user = UserProfileDto(username = name, email = email, isAdmin = isAdmin)
        }
        cursor.close()
        return user
    }

    fun validateUser(identifier: String, pass: String): UserProfileDto? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_IS_ADMIN),
            "($COLUMN_USER_EMAIL = ? OR $COLUMN_USER_NAME = ?) AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(identifier, identifier, pass),
            null, null, null
        )

        var user: UserProfileDto? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            val isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ADMIN)) == 1
            user = UserProfileDto(username = name, email = email, isAdmin = isAdmin)
        }
        cursor.close()
        return user
    }

    // --- TRANSLATION DATABASE OPERATIONS ---

    fun getAllTranslations(): List<TranslationHistoryDto> {
        val list = mutableListOf<TranslationHistoryDto>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSLATIONS,
            arrayOf(COLUMN_TRANS_ID, COLUMN_TRANS_PHRASE, COLUMN_TRANS_TIMESTAMP, COLUMN_TRANS_CONFIDENCE),
            null, null, null, null, "$COLUMN_TRANS_ID DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANS_ID))
                val phrase = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANS_PHRASE))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANS_TIMESTAMP))
                val confidence = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANS_CONFIDENCE))
                list.add(TranslationHistoryDto(id, phrase, timestamp, confidence))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertTranslation(phrase: String, timestamp: String = "Ahora", confidence: Double = 0.92): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRANS_PHRASE, phrase)
            put(COLUMN_TRANS_TIMESTAMP, timestamp)
            put(COLUMN_TRANS_CONFIDENCE, confidence)
        }
        val result = db.insert(TABLE_TRANSLATIONS, null, values)
        return result != -1L
    }
}
