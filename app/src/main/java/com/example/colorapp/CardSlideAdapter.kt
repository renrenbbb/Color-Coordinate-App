package com.example.colorapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * リスト設定
 */
class CardRowData {
    var backgroundColor: Int = 0
}

/**
 * カードビューの設定(ホルダー)
 */
class CardViewHolder : RecyclerView.ViewHolder,
    View.OnClickListener {

    //region 定数・変数
    private var context: Context
    //endregion

    //region 画面項目
    var linearLayoutSub1: LinearLayout? = null
    var linearLayoutSub2: LinearLayout? = null
    var imageViewSampleColor: ImageView? = null
    var textViewCurrentPage: TextView? = null
    var textViewSlash: TextView? = null
    var textViewPageCnt: TextView? = null
    var textViewExplanation: TextView? = null
    var imageViewHand: ImageView? = null
    //endregion

    //region コンストラクタ
    constructor(
        itemView: View,
        context: Context,
        carouselStatus: CarouselStatus
    ) : super(itemView) {
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
        linearLayoutSub1?.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        linearLayoutSub1?.alpha = 0.25f

        if (carouselStatus == CarouselStatus.Initial) {
            //▼初期画面の場合

            textViewExplanation?.setText(R.string.explanation_sidebar)
            textViewExplanation?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.spcontrolthemecolor
                )
            )

            //用意したイメージ画像の関係で角度変更しておく
            imageViewHand?.rotation = 315f
            imageViewHand?.setImageResource(R.drawable.hand_red)

            linearLayoutSub2?.visibility = View.VISIBLE
            textViewCurrentPage?.visibility = View.INVISIBLE
            textViewSlash?.visibility = View.INVISIBLE
            textViewPageCnt?.visibility = View.INVISIBLE

            //アニメーションを設定
            imageViewHand?.startAnimation(Utility.createAnimation(300f))
        } else {
            //▼検索後の場合

            //用意したイメージ画像の関係で角度変更しておく
            imageViewHand?.rotation = 240f
            imageViewHand?.setImageResource(R.drawable.hand)
            imageViewSampleColor?.setOnClickListener(this)

            textViewExplanation?.setText(R.string.explanation_color)
            textViewExplanation?.setTextColor(ContextCompat.getColor(context, R.color.white))

            linearLayoutSub2?.visibility = View.INVISIBLE
        }
    }
    //endregion

    //region 押下時イベント
    override fun onClick(v: View) {
        try {
            //二度押しを禁止
            v.isEnabled = false

            when (v.id) {
                imageViewSampleColor?.id -> {
                    //画面押下時
                    relativeLayoutSubClick()
                }
            }
        } finally {
            v.isEnabled = true
        }
    }

    /**
     * 画面押下時
     */
    private fun relativeLayoutSubClick(): Unit {
        //画面押下時は説明を表示する
        if (linearLayoutSub1?.visibility == View.VISIBLE) {
            linearLayoutSub1?.visibility = View.INVISIBLE
            linearLayoutSub2?.visibility = View.INVISIBLE
        } else {
            linearLayoutSub1?.visibility = View.VISIBLE
            linearLayoutSub2?.visibility = View.VISIBLE

            //アニメーションを設定
            imageViewHand?.startAnimation(Utility.createAnimation(200f))

            // CoroutineScopeを作成
            val coroutineScope = CoroutineScope(Dispatchers.Main)

            // 5秒後に非表示にする
            coroutineScope.launch {
                delay(5000) // 5秒待機

                linearLayoutSub1?.visibility = View.INVISIBLE
                linearLayoutSub2?.visibility = View.INVISIBLE

                imageViewHand?.clearAnimation()
            }
        }
    }
    //endregion
}

/**
 * カードビューのアダプター
 */
class CardSlideAdapter : RecyclerView.Adapter<CardViewHolder> {

    //region 定数・変数
    private var list: MutableList<CardRowData>
    private var context: Context
    private var carouselStatus: CarouselStatus
    //endregion

    //region コンストラクタ
    constructor(list: MutableList<CardRowData>, context: Context?, carouselStatus: CarouselStatus) {
        this.list = list
        this.context = context as Context
        this.carouselStatus = carouselStatus
    }
    //endregion

    override fun onCreateViewHolder(vh: ViewGroup, p1: Int): CardViewHolder {
        val inflate: View =
            LayoutInflater.from(vh.context).inflate(R.layout.colorcardview, vh, false)
        return CardViewHolder(inflate, context, carouselStatus)
    }

    override fun onBindViewHolder(vh: CardViewHolder, p1: Int) {
        //背景色を設定
        vh.imageViewSampleColor?.setBackgroundColor(list.get(p1).backgroundColor)
        //現在の位置を表示
        vh.textViewCurrentPage?.text = (p1 + 1).toString()
        //全ページ数を表示
        vh.textViewPageCnt?.text = getItemCount().toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
