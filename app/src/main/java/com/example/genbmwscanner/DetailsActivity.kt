package com.example.genbmwscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class DetailsActivity : AppCompatActivity() {
    private val dbHandler = DBHelper(this, null)
    lateinit var barcodeEditText:EditText
    lateinit var weightEditText:EditText
    lateinit var timeEditText:TextView
    lateinit var modifyId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        barcodeEditText = findViewById(R.id.barcode)
        weightEditText = findViewById(R.id.weight)
        timeEditText = findViewById(R.id.recordtime)

        /* Check  if activity opened from List Item Click */
        if(intent.hasExtra("id")){
            modifyId = intent.getStringExtra("id")
            barcodeEditText.setText(intent.getStringExtra("barcode"))
            weightEditText.setText(intent.getStringExtra("weight"))
            timeEditText.setText(intent.getStringExtra("recordtime"))
        }

    }

    fun add(view: View){
        val barcode = barcodeEditText.text.toString()
        val weight = weightEditText.text.toString()
        val recordtime = timeEditText.text.toString()
        val bagcolor = "R"
        val macid = "abcdefg"
        dbHandler.insertRow(barcode, weight, recordtime, bagcolor, macid)
        Toast.makeText(this, getString(R.string.data_added), Toast.LENGTH_SHORT).show()
        finish()
    }

    fun update(view: View){
        val barcode = barcodeEditText.text.toString()
        val weight = weightEditText.text.toString()
        val recordtime = timeEditText.text.toString()
        val bagcolor = "R"
        dbHandler.updateRow(modifyId, barcode, weight, recordtime, bagcolor )
        Toast.makeText(this, getString(R.string.data_updated), Toast.LENGTH_SHORT).show()
        finish()
    }

    fun delete(view: View){
        dbHandler.deleteRow(modifyId)
        Toast.makeText(this, getString(R.string.data_deleted), Toast.LENGTH_SHORT).show()
        finish()
    }
}