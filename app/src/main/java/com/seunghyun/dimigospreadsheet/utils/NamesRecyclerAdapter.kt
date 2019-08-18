package com.seunghyun.dimigospreadsheet.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.slidetodelete.enableSlideToDelete
import kotlinx.android.synthetic.main.name_item.view.*

class NamesRecyclerAdapter(private val nameList: ArrayList<String>, private val deleteCallback: (View, () -> Unit) -> Unit) : RecyclerView.Adapter<NamesRecyclerAdapter.NamesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.name_item, parent, false)
        return NamesViewHolder(view, deleteCallback)
    }

    override fun onBindViewHolder(holder: NamesViewHolder, position: Int) {
        holder.nameTV.text = nameList[position]
    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    class NamesViewHolder(val view: View, private val deleteCallback: (View, () -> Unit) -> Unit) : RecyclerView.ViewHolder(view) {
        private val container: RelativeLayout by lazy { view.container }
        private val deletedTV by lazy { container.deletedTV }
        val nameTV: TextView by lazy { container.nameTV }

        init {
            initView()
        }

        private fun initView() {
            deletedTV.enableSlideToDelete(container, nameTV, 2000) {
                setDeleted()
                deleteCallback.invoke(it.nameTV) {
                    //onFailed
                    undoSetDeleted()
                }
            }
        }

        private fun setDeleted() {
            deletedTV.apply {
                setOnTouchListener(null)
                setText(R.string.deleted)
                setBackgroundColor(Color.parseColor("#29B600"))
            }
        }

        private fun undoSetDeleted() {
            initView()
            nameTV.apply {
                x = 0f
                alpha = 1f
            }
            deletedTV.apply {
                setText(R.string.deleting)
                setBackgroundColor(Color.parseColor("#ff4545"))
            }
        }
    }
}