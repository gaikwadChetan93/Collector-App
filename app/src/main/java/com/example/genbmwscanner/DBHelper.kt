package com.example.genbmwscanner

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_1 TEXT, $COLUMN_2 TEXT, $COLUMN_3 TEXT, $COLUMN_4 TEXT, $COLUMN_5 TEXT)"
        )
        db.execSQL(
            "CREATE TABLE $SETTINGS_TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_6 TEXT, $COLUMN_7 TEXT, $COLUMN_8 INTEGER, $COLUMN_9 INTEGER)"
        )

        db.execSQL(
            "CREATE TABLE $BATCH_TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_1 TEXT, $COLUMN_2 TEXT, $COLUMN_3 TEXT, $COLUMN_4 TEXT, $COLUMN_5 TEXT)"
        )

        db.execSQL(
            /*"INSERT INTO $SETTINGS_TABLE_NAME VALUES" +
                    "('1','https://www.greenearthnetwork.in/app_data.php', ABS(RANDOM() % 999999999999), '15', '9')" */
            "INSERT INTO $SETTINGS_TABLE_NAME VALUES" +
                    "('1','https://aurangabad.greenearthnetwork.in/app_data.php', ABS(RANDOM() % 999999999999), '15', '9')"
            /*"INSERT INTO $SETTINGS_TABLE_NAME VALUES" +
                    "('1','https://doctors2.envirovigil.org/app_data.php', ABS(RANDOM() % 999999999999), '15', '9')" */
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $BATCH_TABLE_NAME")
        onCreate(db)
    }

    fun insertRow(barcode: String, weight:String, rectime: String, bagcolor: String, macid: String) {
        val values = ContentValues()
        values.put(COLUMN_1, barcode)
        values.put(COLUMN_2, weight)
        values.put(COLUMN_3, rectime)
        values.put(COLUMN_4, bagcolor)
        values.put(COLUMN_5, macid)

        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.insert(BATCH_TABLE_NAME, null, values)
        db.close()
    }

    fun updateRow(row_id: String, barcode: String, weight:String, rectime: String, bagcolor: String) {
        val values = ContentValues()
        values.put(COLUMN_1, barcode)
        values.put(COLUMN_2, weight)
        values.put(COLUMN_3, rectime)
        values.put(COLUMN_4, bagcolor)

        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(row_id))
        db.update(BATCH_TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(row_id))
        db.close()
    }

    fun deleteRow(row_id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(row_id))
        db.delete(BATCH_TABLE_NAME, "$COLUMN_ID = ?", arrayOf(row_id))
        db.close()
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME,null,null)
        db.delete(BATCH_TABLE_NAME,null,null)
        db.close()
    }

    fun colorWise(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT bagcolor, count(*) as bagCount, sum(weight) as bagSum FROM $BATCH_TABLE_NAME group by bagcolor", null)
    }

    fun getAllRow(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun deleteBatch() {
        val db = this.writableDatabase
        db.delete(BATCH_TABLE_NAME,null,null)
        db.close()
    }

    fun getSettings(): Cursor? {
        val db = this.readableDatabase
        //return db.rawQuery("SELECT * FROM $SETTINGS_TABLE_NAME", null)
        return db.rawQuery("SELECT * FROM $SETTINGS_TABLE_NAME LIMIT 1", null)
    }

    fun updateSettings(serverUrl: String, macid:String, bsize:Int, colorPlace:Int) {
        val values = ContentValues()
        values.put(COLUMN_6, serverUrl)
        values.put(COLUMN_7, macid)
        values.put(COLUMN_8, bsize)
        values.put(COLUMN_9, colorPlace)

        val db = this.writableDatabase
        db.update(SETTINGS_TABLE_NAME, values,null,null)
        db.close()
    }

    companion object {
        const val DATABASE_VERSION = 7
        const val DATABASE_NAME = "myDBfile.db"
        const val TABLE_NAME = "barcodes"
        const val SETTINGS_TABLE_NAME = "settings"
        const val BATCH_TABLE_NAME = "batch"
        const val COLUMN_ID = "id"
        const val COLUMN_1 = "barcode"
        const val COLUMN_2 = "weight"
        const val COLUMN_3 = "recordtime"
        const val COLUMN_4 = "bagcolor"
        const val COLUMN_5 = "macid"
        const val COLUMN_6 = "serverUrl"
        const val COLUMN_7 = "macid"
        const val COLUMN_8 = "bsize"
        const val COLUMN_9 = "colorPlace"
        const val COLUMN_10 = "bagCount"
        const val COLUMN_11 = "bagSum"
    }


}