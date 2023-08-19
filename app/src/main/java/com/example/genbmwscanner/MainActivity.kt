package com.example.genbmwscanner

//import android.R
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {
    private val dbHandler = DBHelper(this, null)
    var scannedBarcodeValue = "000000000000000"
    var weight = "0.000"
    var recordtime = ""
    var bagcolor = "Y"
    var macid = ""
    var bsize = 0
    var colorPlace = 0
    //var serverUrl = "https://www.greenearthnetwork.in/app_data.php"
    var serverUrl = "https://aurangabad.greenearthnetwork.in/app_data.php"
    //var serverUrl = "https://doctors2.envirovigil.org/app_data.php"
    var popMessage = "Ready"
    var camera:Boolean = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
//        getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.ic_launcher);// set drawable icon
//        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);


        val messageBar = findViewById<TextView>(R.id.messageBar)

        val vKG = findViewById(R.id.kg_number) as EditText

        //Connect to the database
        val cursor = dbHandler.getSettings()
        cursor!!.moveToFirst()
        // get the server url from the local database
        serverUrl = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_6))
        //popMessage = "Server: " + serverUrl
        //setToast(popMessage)

        // get the mac id of the current device from the database
        macid = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_7))
        bsize = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_8)).toInt()
        colorPlace = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_9)).toInt()

        btn_scan.setOnClickListener {
            if (setupPermissions()){
            val scanner = IntentIntegrator(this)
            scanner.setPrompt(getString(R.string.setBarcodeWindow))
            scanner.initiateScan()
            } else{
                popMessage = getString(R.string.cameraPermissionMessage)
                setToast(popMessage)
            }
        }

        btn_submit.setOnClickListener {
            //get the weight
            var KG = vKG.text.toString().toFloatOrNull()

            if (KG == null) {
                KG = "0.0".toFloat()
                popMessage = "Weight is 0"
            }
                weight = "%.3f".format(KG.toFloat()).toString()
                //recordtime = LocalDateTime.now().toString()
                val curtime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                recordtime = curtime.format(Date()).toString()
                /*val parsedDate = LocalDateTime.parse(recordtime, DateTimeFormatter.ISO_DATE_TIME)
                val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) */
                bagcolor = scannedBarcodeValue[colorPlace-1].toString()
                if ((scannedBarcodeValue != "000000000000000") && (scannedBarcodeValue != "") && (scannedBarcodeValue.length == bsize)) {
                    dbHandler.insertRow(
                        scannedBarcodeValue,
                        weight,
                        recordtime,
                        bagcolor,
                        macid
                    )

                    var sendStat = send()
                    if (sendStat == 0) {
                        popMessage = getString(R.string.data_added)
                    } else {
                        popMessage = getString(R.string.data_failed)
                    }
                } else {
                    popMessage = getString(R.string.scan_first)
                }
            vKG.setText("")
            //displayBarcode("000000000000000");
            setToast(popMessage)
        }
        checkNetworkConnection()

        /*btn_submit.setOnClickListener {
            var fValue = first_number.value
            var sValue = second_number.value * 10
            val currentDateTime = LocalDateTime.now()


           Toast.makeText(this, "Curent Date :" + currentDateTime, Toast.LENGTH_LONG).show();
            //Log.i("scroller","Values are " + fValue + " and " + sValue)
           // var tValue = fValue.toString() + "." + sValue.toString()
            displayWeight(fValue, sValue)
        }*/

        /* btn_submit.setOnClickListener{
             val intent = Intent(this, DetailsActivity::class.java)
             startActivity(intent)
         }*/
        btn_list_records.setOnClickListener {
            val intent = Intent(this, recordsActivity::class.java)
            startActivity(intent)
        }

        btn_list_batch.setOnClickListener {
            val intent = Intent(this, batchActivity::class.java)
            startActivity(intent)
            finish()
        }

        //val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val nowdate = sdf.format(Date())
        var newdate = "2030-12-31"
        val format = SimpleDateFormat("yyyy-MM-dd")
        val Today: Date = format.parse(nowdate)
        val Future: Date = format.parse(newdate)

        if (Today.compareTo(Future) <= 0) {
            println("earlier")
        }
        else{
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Account Expired")
                setMessage(getString(R.string.appExpired))
                setCancelable(false)
                setPositiveButton("OK"){dialogInterface, which ->
                    finish()
                }
                //setNegativeButton(android.R.string.no, null)
                //setNeutralButton("Maybe", null)
                create()
                show()
            }
        }
//            println("later")

        /*var Today = LocalDateTime.now().toLocalDate().toEpochDay().toInt()
        var Future = LocalDate.parse("2020-05-31").toEpochDay().toInt()*/
/*
        var dateDiff = Future - Today
        if (dateDiff < 1){
                val builder = AlertDialog.Builder(this)
                with(builder)
                {
                    setTitle("Account Expired")
                    setMessage(getString(R.string.appExpired))
                    setCancelable(false)
                    setPositiveButton("OK"){dialogInterface, which ->
                        finish()
                    }
                    //setNegativeButton(android.R.string.no, null)
                    //setNeutralButton("Maybe", null)
                    create()
                    show()
                }
        }
*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.getContents() == null) {
                    //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    displayBarcode("Cancelled")
                } else {
                    var tempbarcode = result.contents
                    if (tempbarcode.length == bsize)
                        displayBarcode(result.contents)
                    else
                        displayBarcode("Invalid: " + bsize + ":" + tempbarcode.length)
                    //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private fun displayBarcode(barcode: String) {
        val barView = findViewById(R.id.barcodeValue) as TextView
        scannedBarcodeValue = barcode.toString()
        barView.text = scannedBarcodeValue
        bagcolor = scannedBarcodeValue[colorPlace-1].toString()
        if (bagcolor == 'R'.toString()){
            barView.setBackgroundColor(Color.RED)
        }
        if (bagcolor == 'Y'.toString()){
            barView.setBackgroundColor(Color.YELLOW)
        }
        if (bagcolor == 'B'.toString()){
            barView.setBackgroundColor(Color.BLUE)
        }

    }

    private fun checkNetworkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        val isConnected: Boolean = if (networkInfo != null) networkInfo.isConnected() else false
        if (networkInfo != null && isConnected) {
            // show "Connected" & type of network                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        "WIFI or MOBILE"
            messageBar.text = "Connected to " + networkInfo.typeName
            // change background color to red
            messageBar.setBackgroundColor(Color.LTGRAY)
        } else {
            // show "Not Connected"
            messageBar.text = "Not Connected"
            // change background color to green
            messageBar.setBackgroundColor(-0x00a000)
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
        //Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        // clear text result
        messageBar.setText("")
        var rInt = 0
        if (scannedBarcodeValue != "000000000000000"){
            if (checkNetworkConnection())
                lifecycleScope.launch {
                    val result = httpPost(serverUrl)
                    if (result != "OK") {
                        messageBar.setBackgroundColor(-0x00a000)
                        rInt = 0
                    } else {
                        messageBar.setBackgroundColor(Color.LTGRAY)
                        rInt = 1
                    }
                    messageBar.text = result
                }
            else {
                messageBar.setBackgroundColor(-0x00a000)
                messageBar.text = getString(R.string.nwError)
                rInt = 0
            }
        }
        else{
            messageBar.setBackgroundColor(-0x00a000)
            messageBar.text = getString(R.string.bError)
            rInt = 0
        }


        return rInt
        //Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()
    }

    //Toast function to display a toast
    //accepts the message to be sent as string.
    fun setToast(message: String) {
        val MyToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        MyToast.setGravity(Gravity.CENTER_VERTICAL, 0, -60)
        MyToast.show()
    }

    //Check for Camera permissions
    //Returns false if permission is not been set
    private fun setupPermissions(): Boolean {

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            setToast(getString(R.string.cameraPermissionMessage))
            camera = false
        }
        else
            camera = true

        return camera

    }

}
