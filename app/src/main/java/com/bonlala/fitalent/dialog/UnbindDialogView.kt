package com.bonlala.fitalent.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.blala.blalable.listener.OnCommBackDataListener
import com.bonlala.fitalent.R
import com.bonlala.fitalent.listeners.OnItemClickListener
import com.hjq.shape.view.ShapeTextView

/**
 * Created by Admin
 *Date 2023/3/3
 */
class UnbindDialogView : AppCompatDialog ,View.OnClickListener{


    private var onCommClickListener : OnItemClickListener ?= null

    fun setOnCommClick(onClick : OnItemClickListener){
        this.onCommClickListener = onClick
    }

    //同步后解绑
    private var syncUnBindTv : ShapeTextView ?= null
    //直接解绑
    private var unbindTv : ShapeTextView ?= null
    //取消
    private var unbindCancelTv : ShapeTextView ?= null


    constructor(context: Context) : super (context){

    }


    constructor(context: Context,theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_unbind_layout)

        intiViews()

    }


    private fun intiViews(){
        syncUnBindTv = findViewById(R.id.syncUnBindTv)
        unbindTv = findViewById(R.id.unbindTv)
        unbindCancelTv = findViewById(R.id.unbindCancelTv)

        syncUnBindTv?.setOnClickListener(this)
        unbindTv?.setOnClickListener(this)
        unbindCancelTv?.setOnClickListener(this)

    }


    //是否显示同步解绑，W561B没有同步解绑
    fun setIsSyncBind(isSync : Boolean){
        syncUnBindTv?.visibility = if(isSync) View.GONE else View.VISIBLE
    }



    override fun onClick(p0: View?) {
        val id = p0?.id

        when(id){
            //同步解绑
            R.id.syncUnBindTv->{
                onCommClickListener?.onIteClick(0x00)
            }
            //直接解绑
            R.id.unbindTv->{
                onCommClickListener?.onIteClick(0x01)
            }
            //关闭
            R.id.unbindCancelTv->{
                onCommClickListener?.onIteClick(0x02)
            }
        }
    }
}