package com.bonlala.sport.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bonlala.fitalent.R
import com.bonlala.sport.OnSportCommItemClickListener
import com.bonlala.sport.model.SportTypeBean

/**
 * Create by sjh
 * @Date 2024/4/10
 * @Desc
 */
class SportHomeTypeAdapter(
    private val context: Context,
    private val list: MutableList<SportTypeBean>
) : RecyclerView.Adapter<SportHomeTypeAdapter.SportTypeViewHolder>() {


    private var click :  OnSportCommItemClickListener?= null

    fun setItemClick(c : OnSportCommItemClickListener){
        this.click = c
    }


    class SportTypeViewHolder( view: View) : ViewHolder(view) {
        val itemSportTypeImg: ImageView = view.findViewById(R.id.itemSportTypeImg)
        val itemSportTypeNameTv: TextView = view.findViewById(R.id.itemSportTypeNameTv)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportTypeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_sport_type_select_layout, parent, false)
        return SportTypeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SportTypeViewHolder, position: Int) {
        val bean = list[position]
        holder.itemSportTypeNameTv.text = bean.typeName
        holder.itemSportTypeImg.setImageDrawable(bean.typeImg)
        holder.itemSportTypeImg?.isSelected  = bean.isChecked
        holder.itemSportTypeNameTv.setTextColor(
            if (!bean.isChecked) context.resources.getColor(R.color.sport_type_normal_color) else context.resources.getColor(
                R.color.sport_type_checked_color
            )
        )

        holder.itemView.setOnClickListener {
            val index = holder.layoutPosition
            click?.onItemClick(index)
        }
    }
}