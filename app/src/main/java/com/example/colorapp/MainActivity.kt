package com.example.colorapp

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import android.content.res.ColorStateList
import android.view.WindowManager

class MainActivity : AppCompatActivity() ,
    View.OnClickListener ,
    RadioGroup.OnCheckedChangeListener ,
    CustomDialog.DialogResultListener,
    CompoundButton.OnCheckedChangeListener{

    //region 画面項目
    var textViewMessage : TextView? = null
    var buttonSearch : Button? = null
    var radioButtonTargetTops  : RadioButton ? = null
    var radioButtonTargetBottoms :RadioButton ? = null
    var radioButtonTargetShoes :RadioButton ? = null
    var radioGroupTargetItem : RadioGroup? = null
    var imageViewTopsColor : ImageView? = null
    var imageViewBottomsColor : ImageView? = null
    var imageViewShoesColor : ImageView? = null
    var checkBoxSort :CheckBox? = null
    var viewPager2SampleColor: ViewPager2? = null
    var switchSeason: Switch? = null
    var navigationViewMain : NavigationView? = null
    var drawerLayoutMain : DrawerLayout? = null
    //endregion

    //region 定数・変数
    //endregion

    //region ロード処理
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ナビゲーションバーの透過に必要
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //レイアウトの設定
        setContentView(R.layout.activity_main)
        //コントロールの設定
        setControl()
        //カルーセル設定
        setCarousel(true)
    }

    /**
     * コントロールの設定
     */
    private fun setControl() {
        textViewMessage  =  findViewById<TextView>(R.id.textViewMessage)
        buttonSearch  =  findViewById<Button>(R.id.buttonSearch)
        radioButtonTargetTops =  findViewById<RadioButton>(R.id.radioButtonTargetTops)
        radioButtonTargetBottoms  =  findViewById<RadioButton>(R.id.radioButtonTargetBottoms)
        radioButtonTargetShoes=  findViewById<RadioButton>(R.id.radioButtonTargetShoes)
        radioGroupTargetItem  = findViewById<RadioGroup>(R.id.radioGroupTargetItem)
        imageViewTopsColor  = findViewById<ImageView>(R.id.imageViewTopsColor)
        imageViewBottomsColor  = findViewById<ImageView>(R.id.imageViewBottomsColor)
        imageViewShoesColor  = findViewById<ImageView>(R.id.imageViewShoesColor)
        checkBoxSort = findViewById<CheckBox>(R.id.checkBoxSort)
        viewPager2SampleColor = findViewById<ViewPager2>(R.id.viewPager2SampleColor)
        switchSeason = findViewById<Switch>(R.id.switchSeason)
        navigationViewMain = findViewById<NavigationView>(R.id.navigationViewMain)
        drawerLayoutMain = findViewById<DrawerLayout>(R.id.drawerLayoutMain)

        buttonSearch?.setOnClickListener(this)
        imageViewTopsColor?.setOnClickListener(this)
        imageViewBottomsColor?.setOnClickListener(this)
        imageViewShoesColor?.setOnClickListener(this)
        radioGroupTargetItem?.setOnCheckedChangeListener(this)
        switchSeason?.setOnCheckedChangeListener(this)
    }
    //endregion

    //region ボタン押下時イベント
    override fun onClick(v: View) {
        try {
            v.isEnabled = false

            when(v.id) {
                buttonSearch?.id -> {
                    //検索ボタン押下時
                    buttonSearchClick()
                }
                imageViewTopsColor?.id ,imageViewBottomsColor?.id,imageViewShoesColor?.id -> {
                    //カラーピッカーダイアログ表示
                    showColorPickerDialog(v as ImageView?)
                }
            }
        }
        finally {
            v.isEnabled = true
        }
    }

    /**
     * 検索ボタン押下時
     */
    private fun buttonSearchClick() : Unit {
        //カルーセル設定
        val result = setCarousel()

        //ドロワーを閉じる
        if(result)drawerLayoutMain?.closeDrawer(Gravity.RIGHT)
    }
    //endregion

    //region イベント
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {


    }

    override fun onCheckedChanged(v: CompoundButton?, isChecked: Boolean) {
        try {
            v?.isEnabled = false

            when(v?.id) {
                switchSeason?.id -> {
                    if (!isChecked) {
                        //春夏の場合
                        buttonSearch?.setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent))
                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.colorAccent))
                        checkBoxSort?.buttonTintList = colorStateList
                        radioButtonTargetTops?.buttonTintList = colorStateList
                        radioButtonTargetBottoms?.buttonTintList = colorStateList
                        radioButtonTargetShoes?.buttonTintList = colorStateList

                    } else {
                        //秋冬の場合
                        buttonSearch?.setBackgroundColor(ContextCompat.getColor(this,R.color.smokeblue))
                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.smokeblue))
                        checkBoxSort?.buttonTintList = colorStateList
                        radioButtonTargetTops?.buttonTintList = colorStateList
                        radioButtonTargetBottoms?.buttonTintList = colorStateList
                        radioButtonTargetShoes?.buttonTintList = colorStateList

                    }
                }
            }
        }
        finally {
            v?.isEnabled = true
        }
    }
    //endregion

    //region その他関数
    /**
     * カルーセル設定
     */
    fun setCarousel(initFlg :Boolean = false) : Boolean  {
        val cardRowDatas : MutableList<CardRowData> = mutableListOf()

        if(initFlg)
        {
            val cardRowData = CardRowData()
            cardRowData.backgroundColor = getColor(R.color.white)
            cardRowDatas.add(cardRowData)

            viewPager2SampleColor?.offscreenPageLimit = 1
        }
        else
        {
            var season : Utility.season
            if(!(switchSeason as Switch).isChecked)
            {
                season = Utility.season.SS
            }
            else
            {
                season = Utility.season.AW
            }

            //どのラジオボタンがチェックされているか判定
            var item : Utility.item = Utility.item.TOPS
            var otherRGBList:MutableList<Triple<Int, Int, Int>> = mutableListOf()
            when(radioGroupTargetItem?.checkedRadioButtonId) {
                radioButtonTargetTops?.id -> {
                    item = Utility.item.TOPS

                    //色を選択していない場合はエラーメッセージを表示して終了
                    if(imageViewBottomsColor?.drawable == null || imageViewShoesColor?.drawable == null)
                    {
                        DisplayMessage(resources.getString(R.string.error_selectedcolor),Utility.msgLevel.ERROR)
                        return false
                    }

                    //他のアイテムでRGBリスト作成
                    val bitmapBottoms = (imageViewBottomsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapBottoms,0,0))
                    val bitmapShoes = (imageViewShoesColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapShoes,0,0))

                }
                radioButtonTargetBottoms?.id  -> {
                    item = Utility.item.BOTTOMS

                    //色を選択していない場合はエラーメッセージを表示して終了
                    if(imageViewTopsColor?.drawable == null || imageViewShoesColor?.drawable == null)
                    {
                        DisplayMessage(resources.getString(R.string.error_selectedcolor),Utility.msgLevel.ERROR)
                        return false
                    }

                    val bitmapTops = (imageViewTopsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapTops,0,0))
                    val bitmapShoes = (imageViewShoesColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapShoes,0,0))
                }
                radioButtonTargetShoes?.id  -> {
                    item = Utility.item.SHOES

                    if(imageViewTopsColor?.drawable == null || imageViewBottomsColor?.drawable == null)
                    {
                        DisplayMessage(resources.getString(R.string.error_selectedcolor),Utility.msgLevel.ERROR)
                        return false
                    }

                    val bitmapTops = (imageViewTopsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapTops,0,0))
                    val bitmapBottoms = (imageViewBottomsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapBottoms,0,0))
                }
            }
            //最適な色を取得する
            val colorSelect = ColorSelect()
            val colorList:MutableList<Int> = colorSelect.getColorList(season,item,otherRGBList,(checkBoxSort as CheckBox).isChecked)

            //取得した色をカードビューで表示
            for(i in 0..colorList.count() -1) {
                val cardRowData = CardRowData()

                cardRowData.backgroundColor = colorList[i]
                cardRowDatas.add(cardRowData)
            }

            viewPager2SampleColor?.offscreenPageLimit =cardRowDatas.count() - 1
        }

        viewPager2SampleColor?.adapter = CardSlideAdapter(cardRowDatas, this, initFlg)

        DisplayMessage(null,Utility.msgLevel.INFORMATION)
        return true
    }

    /**
     * カラーピッカーダイアログ表示
     */
    private fun showColorPickerDialog(targetImageView : ImageView?) {
        val dialog = CustomDialog()
        val manager: FragmentManager = supportFragmentManager

        dialog.setDialogResultListener(this)
        dialog.setTargetImageView(targetImageView)

        var season : Utility.season
        if(!(switchSeason as Switch).isChecked){
            season = Utility.season.SS
        }else{
            season = Utility.season.AW
        }
        dialog.setSeason(season)

        //ダイアログの外を押下しても閉じないようにする
        dialog.isCancelable = false
       //ダイアログを表示する
        dialog.show(manager, "simple")
    }

    /**
     * メッセージ表示
     */
    private fun DisplayMessage(message: String? ,msgLevel:Utility.msgLevel) : Unit {
        textViewMessage?.text = message

        var color :Int
        if(msgLevel == Utility.msgLevel.INFORMATION)
        {
            color = R.color.black
        }
        else
        {
            color = R.color.red
        }
        textViewMessage?.setTextColor(ContextCompat.getColor(this, color))
    }
    //endregion

    //region CustomDialog関連
    override fun onDialogResult(targetImageView:  ImageView?, rgb: Triple<Int, Int, Int>) {
        //ダイアログの終了を検知
        //選択した色を反映
        targetImageView?.setImageBitmap(Utility.createBitmap(targetImageView,rgb.component1(), rgb.component2(),  rgb.component3()))
    }
    //endregion
}
