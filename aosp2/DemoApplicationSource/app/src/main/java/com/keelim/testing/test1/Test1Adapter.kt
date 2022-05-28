package com.keelim.testing.test1

import android.content.Context
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.keelim.testing.R
import kotlinx.android.synthetic.main.content_main_recycler_item.view.*


class Test1Adapter internal constructor(list: ArrayList<String>?) :
    RecyclerView.Adapter<Test1Adapter.ViewHolder>() {
    private var mData: ArrayList<String>? = null

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textView1: TextView = itemView.main_text1

    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context: Context = parent.context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.content_main_recycler_item, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val text = mData!![position]
        holder.textView1.text = text
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData!!.size
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    init {
        mData = list
    }


}