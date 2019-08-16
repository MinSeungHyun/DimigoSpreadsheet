package com.seunghyun.dimigospreadsheet.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.name_item.view.*

class NamesRecyclerAdapter(private val nameList: ArrayList<String>) : RecyclerView.Adapter<NamesRecyclerAdapter.NamesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.name_item, parent, false)
        return NamesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NamesViewHolder, position: Int) {
        holder.nameTV.text = nameList[position]
    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    class NamesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameTV by lazy { view.nameTV }
    }
}