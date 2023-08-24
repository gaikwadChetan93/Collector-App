package com.example.genbmwscanner

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_records.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

class recordsActivity : AppCompatActivity() {
    var scannedBarcodeValue = "000000000000000"
    var weight = ""
    var recordtime = ""
    var locationTxt = ""
    var macid = ""
    var serverUrl = "http://www.greenearthnetwork.in/app_data.php"


    val dbHandler = DBHelper(this, null)
    var dataList = ArrayList<HashMap<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        btn_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_sync.setOnClickListener {
            syncData()
        }

        val cursor = dbHandler.getSettings()
        cursor!!.moveToFirst()
        // get the server url from the local database
        serverUrl = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_6))
        // get the mac id of the current device from the database
        macid = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_7))
    }


    fun loadIntoList() {
        dataList.clear()
        val cursor = dbHandler.getAllRow()
        cursor!!.moveToFirst()

        while (!cursor.isAfterLast) {
            val map = HashMap<String, String>()
            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            map["barcode"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_1))
            map["weight"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_2))
            map["recordtime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_3))
            map["location"] = cursor.getString(cursor.getColumnIndex(DBHelper.LOCATION))
            dataList.add(map)

            cursor.moveToNext()
        }
        findViewById<ListView>(R.id.listView).adapter =
            CustomAdapter(this@recordsActivity, dataList)
        findViewById<ListView>(R.id.listView).setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("id", dataList[+i]["id"])
            intent.putExtra("barcode", dataList[+i]["barcode"])
            intent.putExtra("weight", dataList[+i]["weight"])
            intent.putExtra("recordtime", dataList[+i]["recordtime"])
            startActivity(intent)
        }
    }

    /*fun fabClicked(v: View) {
        val intent = Intent(this, DetailsActivity::class.java)
        startActivity(intent)
    }*/
    fun syncData() {
        dataList.clear()
        var syncerror = 0
        var syncsuccess = 0
        val cursor = dbHandler.getAllRow()
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            val map = HashMap<String, String>()
            scannedBarcodeValue = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_1))
            weight = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_2))
            recordtime = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_3))
            locationTxt = cursor.getString(cursor.getColumnIndex(DBHelper.LOCATION))
            dataList.add(map)
            var syncResult = send()
            if (syncResult == 0) {
                syncsuccess++
                Thread.sleep(500L)
            } else if (syncResult == 1) {
                syncerror++
                Thread.sleep(500L)
            } else{
                setToast("Network Error")
                Thread.sleep(500L)
            }

            cursor.moveToNext()
        }
            setToast("Data sync done success/error: "+ syncsuccess + "/" + syncerror)
    }

    private fun checkNetworkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        val isConnected: Boolean = if (networkInfo != null) networkInfo.isConnected() else false
        if (networkInfo != null && isConnected) {
            setToast("Connected to " + networkInfo.typeName)
        } else {
            setToast("Not Connected")
        }
        return isConnected
    }

    @Throws(JSONException::class)
    private fun buidJsonObject(): JSONObject {

        val jsonObject = JSONObject()
        jsonObject.accumulate("barcode", scannedBarcodeValue)
        jsonObject.accumulate("weight", weight)
        jsonObject.accumulate("recordtime", recordtime)
        jsonObject.accumulate("macid", macid)
        jsonObject.accumulate("location", locationTxt)

        return jsonObject
    }

    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {
        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        Log.i(MainActivity::class.java.toString(), jsonObject.toString())
        writer.flush()
        writer.close()
        os.close()
    }

    @Throws(IOException::class, JSONException::class)
    private suspend fun httpPost(myUrl: String): String {

        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // 1. create HttpURLConnection
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 2. build JSON object
            val jsonObject = buidJsonObject()

            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject)

            // 4. make POST request to the given URL
            conn.connect()

            // 5. return response message
            conn.responseMessage + ""
        }
        return result
    }


    //sends data to the server defined in @serverUrl
    //Returns 0 if unsuccessful, 1 if successful
    public fun send(): Int {
        var rInt = 0
        if (checkNetworkConnection())
            lifecycleScope.launch {
                val result = runBlocking { httpPost(serverUrl) }
                if (result != "OK") {
                    rInt = 1
                    //exitProcess(1)
                }
            }
        else {
            rInt = 2
        }

        return rInt
    }

    fun setToast(message: String) {
        val MyToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        MyToast.setGravity(Gravity.CENTER_VERTICAL, 0, -60)
        MyToast.show()
    }

    public override fun onResume() {
        super.onResume()
        loadIntoList()
    }
}
