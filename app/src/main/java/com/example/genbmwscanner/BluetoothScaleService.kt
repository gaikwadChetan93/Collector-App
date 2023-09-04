package com.example.genbmwscanner

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Random
import java.util.UUID


class BluetoothScaleService : Service() {
    private lateinit var weight: String

    // Binder given to clients.
    private val binder = BluetoothScaleBinder()

    var mmOutputStream: OutputStream? = null
    var mmInputStream: InputStream? = null
    var workerThread: Thread? = null
    lateinit var readBuffer: ByteArray
    var readBufferPosition = 0
    var counter = 0

    @Volatile
    var stopWorker = false
    lateinit var sock: BluetoothSocket
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var sockFallback: BluetoothSocket

    // Random number generator.
    private val mGenerator = Random()
    lateinit var bleAddress: String

    /** Method for clients.  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    val currentWeight: String
        get() = weight.trim().toFloat().div(1000).toString()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class BluetoothScaleBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): BluetoothScaleService = this@BluetoothScaleService
    }

    override fun onBind(intent: Intent): IBinder {
        bleAddress = intent.getStringExtra("ble_address").toString()
        try {
            startConnection()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d("****", "Onrebind")
    }
    @Throws(IOException::class)
    public fun startConnection() {
        val remoteDevice = bleAddress
        val LOG = "****"

        if ("" == remoteDevice) {

            // log error
            Log.e(LOG, "No Bluetooth device has been selected.")
        } else {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val dev = btAdapter.getRemoteDevice(remoteDevice)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            /* * Establish Bluetooth connection * */
            Log.d(LOG, "Stopping Bluetooth discovery.")
            btAdapter.cancelDiscovery()
            try {
                // Instantiate a BluetoothSocket for the remote
                // device and connect it.
                sock = dev.createRfcommSocketToServiceRecord(MY_UUID)
                sock.connect()
            } catch (e1: Exception) {
                Log.e(
                    "startConnection", "There was an error" +
                            ",Falling back..", e1
                )
                val clazz: Class<*> = sock?.remoteDevice!!::class.java
                val paramTypes = arrayOf<Class<*>>(Integer.TYPE)
                try {
                    /************Fallback method 1 */
                    val m = clazz.getMethod(
                        "createRfcommSocket", *paramTypes
                    )
                    val params = arrayOf<Any>(Integer.valueOf(1))
                    sockFallback = m.invoke(
                        sock.getRemoteDevice(), params
                    ) as BluetoothSocket
                    sockFallback!!.connect()
                    sock = sockFallback
                    Log.e("", "Connected")
                } catch (e2: Exception) {
                    sendMessageToActivity(false)
                    stopSelf()
                    Log.e("startConnection", "Stopping app..", e2)
                    throw IOException()
                }
            }
            Toast.makeText(this, "Scale connected", Toast.LENGTH_SHORT).show()
            Log.i("BT Terminal", "connected")
            sendMessageToActivity(true)
            mmOutputStream = sock.outputStream
            mmInputStream = sock.inputStream
            beginListenForData()
        }
    }

    fun beginListenForData() {
        val handler = Handler()
        val delimiter: Byte = 10 //This is the ASCII code for a newline character
        stopWorker = false
        readBufferPosition = 0
        readBuffer = ByteArray(1024)
        workerThread = Thread {
            while (!Thread.currentThread().isInterrupted && !stopWorker) {
                try {
                    val bytesAvailable = mmInputStream!!.available()
                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        mmInputStream!!.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b = packetBytes[i]
                            if (b == delimiter) {
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(
                                    readBuffer,
                                    0,
                                    encodedBytes,
                                    0,
                                    encodedBytes.size
                                )
                                val data = String(encodedBytes, charset("US-ASCII"))
                                readBufferPosition = 0
                                handler.post(Runnable {
                                    Log.d("****", "$data")
                                    weight = data
                                })
                            } else {
                                readBuffer[readBufferPosition++] = b
                            }
                        }
                    }
                } catch (ex: IOException) {
                    stopWorker = true
                    stopSelf()
                    sendMessageToActivity(false)
                }
            }
        }
        workerThread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeBT()
    }

    @Throws(IOException::class)
    fun closeBT() {
        stopWorker = true
        mmOutputStream!!.close()
        mmInputStream!!.close()
        sock.close()
    }

    private fun sendMessageToActivity(connected: Boolean) {
        Log.d("****", "Sending connection status $connected")
        val intent = Intent(getString(R.string.bluetooth_state))
        intent.putExtra(getString(R.string.bluetooth_status), connected);
        sendBroadcast(intent)
    }

}