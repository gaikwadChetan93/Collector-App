package com.example.genbmwscanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomBatchAdapter(private val context: Context,
                         private val dataList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    private val inflater: LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int { return dataList.size }
    override fun getItem(position: Int): Int { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var dataitem = dataList[position]

        val rowView = inflater.inflate(R.layout.batch_row, parent, false)
        rowView.findViewById<TextView>(R.id.row_bagcolor).text = dataitem["bagcolor"]
        rowView.findViewById<TextView>(R.id.row_bagCount).text = dataitem["bagCount"]
        rowView.findViewById<TextView>(R.id.row_bagSum).text = dataitem["bagSum"]

        rowView.tag = position
        return rowView
    }
}