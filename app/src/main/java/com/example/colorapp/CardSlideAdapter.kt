package com.example.colorapp

import android.os.Handler
import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

// リスト設定
class CardRowData{
    var backgroundColor : Int = 0
}

class CardViewHolder: RecyclerView.ViewHolder,
    View.OnClickListener{

    private val hideControlRunnable = Runnable {
        linearLayoutSub1?.visibility = View.INVISIBLE
        linearLayoutSub2?.visibility = View.INVISIBLE

        imageViewHand?.clearAnimation()
    }

    private val handler = Handler()

    var context :Context

    //region 画面項目
    var linearLayoutSub1: LinearLayout? = null
    var linearLayoutSub2: LinearLayout? = null
    var imageViewSampleColor: ImageView? = null
    var textViewCurrentPage :TextView? = null
    var textViewSlash :TextView? = null
    var textViewPageCnt :TextView? = null
    var textViewExplanation :TextView? = null
    var imageViewHand :ImageView? = null
    //endregion

    constructor(itemView: View, context:Context,initFlg : Boolean):super(itemView){
        this.context = context

        this.linearLayoutSub1 = itemView.findViewById(R.id.linearLayoutSub1)
        this.linearLayoutSub2 = itemView.findViewById(R.id.linearLayoutSub2)
        this.imageViewSampleColor = itemView.findViewById(R.id.imageViewSampleColor)
        this.textViewCurrentPage = itemView.findViewById(R.id.textViewCurrentPage)
        this.textViewSlash = itemView.findViewById(R.id.textViewSlash)
        this.textViewPageCnt = itemView.findViewById(R.id.textViewPageCnt)
        this.textViewExplanation = itemView.findViewById(R.id.textViewExplanation)
        this.imageViewHand = itemView.findViewById(R.id.imageViewHand)

        linearLayoutSub1?.visibility = View.INVISIBLE
        linearLayoutSub1?.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
        linearLayoutSub1?.alpha = 0.25f

        if(initFlg)
        {
            //初期画面の場合

            textViewExplanation?.setText(R.string.explanation_sidebar)
            textViewExplanation?.setTextColor(ContextCompat.getColor(context,R.color.spcontrolthemecolor))

            //用意したイメージ画像の関係で角度変更しておく
            imageViewHand?.rotation = 315f
            imageViewHand?.setImageResource(R.drawable.hand_red)

            linearLayoutSub2?.visibility = View.VISIBLE
            textViewCurrentPage?.visibility = View.INVISIBLE
            textViewSlash?.visibility = View.INVISIBLE
            textViewPageCnt?.visibility = View.INVISIBLE

            //アニメーションを設定
            imageViewHand?.startAnimation(Utility.createAnimation(300f))
        }
        else
        {
            //検索後の場合

            //用意したイメージ画像の関係で角度変更しておく
            imageViewHand?.rotation = 240f
            imageViewHand?.setImageResource(R.drawable.hand)
            imageViewSampleColor?.setOnClickListener(this)

            textViewExplanation?.setText(R.string.explanation_color)
            textViewExplanation?.setTextColor(ContextCompat.getColor(context,R.color.white))

            linearLayoutSub2?.visibility = View.INVISIBLE
        }
   }

    //region 押下時イベント
    override fun onClick(v: View) {
        try {
            v.isEnabled = false

            when(v.id) {
                imageViewSampleColor?.id -> {
                    //画面押下時
                    relativeLayoutSubClick()
                }
            }
        }
        finally {
            v.isEnabled = true
        }
    }

    /**
     * 画面押下時
     */
    private fun relativeLayoutSubClick() : Unit {
        //画面押下時は説明を表示する
        if (linearLayoutSub1?.visibility == View.VISIBLE) {
            linearLayoutSub1?.visibility = View.INVISIBLE
            linearLayoutSub2?.visibility = View.INVISIBLE
        }
        else
        {
            linearLayoutSub1?.visibility = View.VISIBLE
            linearLayoutSub2?.visibility = View.VISIBLE

            //アニメーションを設定
            imageViewHand?.startAnimation(Utility.createAnimation(200f))

            // 5秒後に非表示にする
            handler.postDelayed(hideControlRunnable, 5000)
        }
    }
    //endregion
}

class CardSlideAdapter : RecyclerView.Adapter<CardViewHolder>{
    var list:MutableList<CardRowData>
    var context: Context
    var initFlg: Boolean

    constructor(list:MutableList<CardRowData>, context: Context?,initFlg : Boolean){
        this.list = list
        this.context = context as Context
        this.initFlg = initFlg
    }

    override fun onCreateViewHolder(vh: ViewGroup, p1: Int): CardViewHolder {
        val inflate:View = LayoutInflater.from(vh.context).inflate(R.layout.colorcardview, vh, false)
        return CardViewHolder(inflate,context,initFlg)
    }

    override fun onBindViewHolder(vh: CardViewHolder, p1: Int) {
        vh.imageViewSampleColor?.setBackgroundColor(list.get(p1).backgroundColor)
        vh.textViewCurrentPage?.text = (p1 + 1).toString()
        vh.textViewPageCnt?.text = getItemCount().toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
