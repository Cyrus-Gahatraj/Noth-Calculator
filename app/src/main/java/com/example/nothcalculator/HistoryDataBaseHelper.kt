package com.example.nothcalculator

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.P)
class HistoryDataBaseHelper(context: Context?)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        const val DATABASE_NAME = "calculator.db"
        const val DATABASE_VERSION = 1
        const val HISTORY = "HISTORY_TABLE"
        const val ID = "ID"
        const val EXPRESSION = "EXPRESSION"
        const val RESULT = "RESULT"
        const val DATE = "DATE"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $HISTORY ($ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$EXPRESSION TEXT, " +
                "$RESULT TEXT, " +
                "$DATE TEXT)"

        db?.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        db?.execSQL("DROP TABLE IF EXISTS $HISTORY")
        onCreate(db)
    }

    fun addCalculation(expression: String, result: String, date: String){
        val db = writableDatabase
        val cv = android.content.ContentValues()
        with(cv){
            put(EXPRESSION,expression)
            put(RESULT, result)
            put(DATE, date)
        }
        db.insert(HISTORY, null, cv)
        db.close()
    }

    fun getHistory(): ArrayList<CalculationHistory> {
        val list = ArrayList<CalculationHistory>()
        val stringQuery = "SELECT * FROM $HISTORY"
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(stringQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val expression = cursor.getString(cursor.getColumnIndexOrThrow(EXPRESSION))
                val result = cursor.getString(cursor.getColumnIndexOrThrow(RESULT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DATE))

                val historyItem = CalculationHistory(
                    expression = expression,
                    result = result,
                    date = date
                )
                list.add(historyItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list
    }

    fun clearHistory(){
        val db = writableDatabase
        db.delete(HISTORY, null, null)
        db.close()
    }

}