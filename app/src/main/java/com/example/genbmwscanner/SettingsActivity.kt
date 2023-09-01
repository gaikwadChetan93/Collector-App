package com.example.genbmwscanner

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_records.*
import java.util.ArrayList

class SettingsActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    lateinit var urlEditText: EditText
    lateinit var macidEditText: TextView
    lateinit var bsizeEditText: EditText
    lateinit var cplaceEditText: EditText
    lateinit var scaleData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        urlEditText = findViewById(R.id.serverUrl) as EditText
        macidEditText = findViewById(R.id.macid) as TextView
        bsizeEditText = findViewById(R.id.barcodesize) as EditText
        cplaceEditText = findViewById(R.id.colorplace) as EditText
        scaleData = findViewById(R.id.scale)

        //Connect to the database
        val cursor = dbHandler.getSettings()
        cursor!!.moveToFirst()
        // get the server url from the local database
        val serverFullUrl = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_6))
        val serverUrl = serverFullUrl.removePrefix("https://")
        // get the mac id of the current device from the database
        var macid = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_7))
        // get the size of the barcode
        val bsize = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_8))
        // get the mac id of the current device from the database
        var colorPlace = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_9))

        urlEditText.setText(serverUrl)
        macidEditText.setText(macid)
        bsizeEditText.setText(bsize)
        cplaceEditText.setText(colorPlace)
        if (SharedPrefUtils.getStringData(this, SharedPrefUtils.BLUETOOTH_ID) != null) {
            scaleData.text = String.format("%s (%s) >",
                SharedPrefUtils.getStringData(this, SharedPrefUtils.BLUETOOTH_NAME),
                SharedPrefUtils.getStringData(this, SharedPrefUtils.BLUETOOTH_ID)
            )
        }
        btn_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        scaleData.setOnClickListener {
            getAllDeviceAddress()
        }


    }

    fun getAllDeviceAddress() {
        val deviceStrs: ArrayList<String> = ArrayList()
        val devices: ArrayList<String> = ArrayList()
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = btAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                deviceStrs.add(
                    """
                    ${device.name}
                    ${device.address}
                    """.trimIndent()
                )
                devices.add(device.address)
            }
        }
        showDeviceSelecterDialog(deviceStrs, devices)
    }

    private fun showDeviceSelecterDialog(
        deviceStrs: ArrayList<*>, devices: ArrayList<*>
    ) {
        // show list
        val alertDialog = AlertDialog.Builder(this)
        val adapter: ArrayAdapter<*> = ArrayAdapter(
            this,
            android.R.layout.select_dialog_singlechoice,
            deviceStrs.toTypedArray()
        )
        alertDialog.setSingleChoiceItems(
            adapter, -1
        ) { dialog, which ->
            dialog.dismiss()
            val position = (dialog as AlertDialog)
                .listView
                .checkedItemPosition
            val deviceAddress = devices[position] as String
            val deviceName = deviceStrs[position].toString().replace(deviceAddress, "")
            SharedPrefUtils.saveData(this, SharedPrefUtils.BLUETOOTH_ID, deviceAddress)
            SharedPrefUtils.saveData(this, SharedPrefUtils.BLUETOOTH_NAME, deviceName)
            scaleData.text = String.format("%s (%s) >",
                SharedPrefUtils.getStringData(this, SharedPrefUtils.BLUETOOTH_NAME),
                SharedPrefUtils.getStringData(this, SharedPrefUtils.BLUETOOTH_ID)
            )
            Toast.makeText(this, "Scale saved", Toast.LENGTH_SHORT).show()

        }
        alertDialog.setTitle("Choose Bluetooth device")
        alertDialog.show()
    }

    fun update(view: View) {
        val serverUrl = "https://"+urlEditText.text.toString()
        val macid = macidEditText.text.toString()
        val bsize = bsizeEditText.text.toString()
        val colorPlace = cplaceEditText.text.toString()
        dbHandler.updateSettings(serverUrl, macid, bsize.toInt(), colorPlace.toInt())
        Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun deleteAll(view: View) {
        dbHandler.deleteAll()
        val dialogBuilder = AlertDialog.Builder(this)
        val myToast = Toast.makeText(this, getString(R.string.deleteAllMessage), Toast.LENGTH_SHORT)
        myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 60)
        myToast.show()
    }

    fun deleterecords(view: View) {

        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(getString(R.string.deleteAll_alertHead))
            setMessage(getString(R.string.deleteAll_alert))
            setCancelable(false)
            setPositiveButton("Yes"){dialogInterface, which ->
               deleteAll(view)
            }
            setNegativeButton(android.R.string.no, null)
            //setNeutralButton("Maybe", null)
            create()
            show()
        }
    }
}
