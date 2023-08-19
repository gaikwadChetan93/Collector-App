package com.example.genbmwscanner

import android.content.Intent
import android.database.DatabaseUtils
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_batch.*
import kotlinx.android.synthetic.main.activity_records.*
import kotlinx.android.synthetic.main.activity_records.btn_back

class batchActivity : AppCompatActivity() {
    val dbHandler = DBHelper(this, null)
    var dataList = ArrayList<HashMap<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch)
        val mActivity = MainActivity() as MainActivity

        btn_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_delbatch.setOnClickListener {
            dbHandler.deleteBatch()
            Toast.makeText(this, getString(R.string.deleteBatchMessage), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun loadIntoList() {
        dataList.clear()

        val cursor = dbHandler.colorWise()
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor))
        cursor!!.moveToFirst()

        while (!cursor.isAfterLast) {
            val map = HashMap<String, String>()
            map["bagcolor"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_4))
            map["bagCount"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_10))
            map["bagSum"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_11))
            dataList.add(map)

            cursor.moveToNext()
        }
        findViewById<ListView>(R.id.batchView).adapter = CustomBatchAdapter(this@batchActivity, dataList)
    }

    public override fun onResume() {
        super.onResume()
        loadIntoList()
    }


}

