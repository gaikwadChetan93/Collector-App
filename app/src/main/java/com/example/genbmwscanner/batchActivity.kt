package com.example.genbmwscanner

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.DatabaseUtils
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.genbmwscanner.printer.async.AsyncBluetoothEscPosPrint
import com.example.genbmwscanner.printer.async.AsyncEscPosPrint
import com.example.genbmwscanner.printer.async.AsyncEscPosPrinter
import kotlinx.android.synthetic.main.activity_batch.btn_connect_printer
import kotlinx.android.synthetic.main.activity_batch.btn_delbatch
import kotlinx.android.synthetic.main.activity_batch.btn_print
import kotlinx.android.synthetic.main.activity_records.btn_back
import java.text.SimpleDateFormat
import java.util.Date

class batchActivity : AppCompatActivity() {
    val dbHandler = DBHelper(this, null)
    var dataList = ArrayList<HashMap<String, String>>()
    var selectedDevice: BluetoothConnection? = null
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
        btn_connect_printer.setOnClickListener {
            browseBluetoothDevice()
        }
        btn_print.setOnClickListener {
            printBatch()
        }
    }

    private fun printBatch() {
        AsyncBluetoothEscPosPrint(
            this,
            object : AsyncEscPosPrint.OnPrintFinished() {
                override fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int) {
                    Log.e(
                        "Async.OnPrintFinished",
                        "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                    )
                }

                override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                    Log.i(
                        "Async.OnPrintFinished",
                        "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                    )
                }
            }
        )
            .execute(this.getAsyncEscPosPrinter(selectedDevice))
    }

    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?): AsyncEscPosPrinter? {
        val format = SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss")
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
        return printer.addTextToPrint(
            """
            [L]
            [C]<u><font size='big'>ORDER N°045</font></u>
            [L]
            """.trimIndent()
        )
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


    private fun browseBluetoothDevice() {
            val bluetoothDevicesList =
                BluetoothPrintersConnections().list
            if (bluetoothDevicesList != null) {
                val alertDialog =
                    AlertDialog.Builder(this)
                alertDialog.setTitle("Bluetooth printer selection")
                alertDialog.setItems(
                    bluetoothDevicesList.map {
                    it.device.name
                }.toTypedArray()
                ) { dialogInterface: DialogInterface?, i1: Int ->
                    selectedDevice = bluetoothDevicesList[i1]
                    val button =
                        findViewById<View>(R.id.btn_connect_printer) as Button
                    button.text = selectedDevice?.device?.name ?: ""
                }
                val alert = alertDialog.create()
                alert.setCanceledOnTouchOutside(false)
                alert.show()
            }
    }
}

