package com.example.genbmwscanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomAdapter(private val context: Context,
                    private val dataList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    private val inflater: LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int { return dataList.size }
    override fun getItem(position: Int): Int { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var dataitem = dataList[position]

        val rowView = inflater.inflate(R.layout.list_row, parent, false)
        rowView.findViewById<TextView>(R.id.row_barcode).text = dataitem["barcode"]
        rowView.findViewById<TextView>(R.id.row_weight).text = "Weight: " + dataitem["weight"]
        rowView.findViewById<TextView>(R.id.row_recordtime).text = "Date/Time: " + dataitem["recordtime"]
        rowView.findViewById<TextView>(R.id.row_location).text = "Location: " + dataitem["location"]

        rowView.tag = position
        return rowView
    }
}