package com.example.colorapp

import android.app.Dialog
import android.content.Context.CAMERA_SERVICE
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import android.view.*

/**
 * カラーピッカーダイアログ
 */
class CustomDialog: DialogFragment(),
    View.OnClickListener ,
    View.OnTouchListener,
    SeekBar.OnSeekBarChangeListener,
    ViewTreeObserver.OnGlobalLayoutListener ,
    TextureView.SurfaceTextureListener {

    //region インターフェース
    interface DialogResultListener {
        fun onDialogResult(targetImageView:  ImageView?, rgb: Triple<Int, Int, Int>)
    }
    //endregion

    //region 定数・変数
    private var dialogResultListener : DialogResultListener? = null
    private var parentContext : Context? = null
    private var targetImageView : ImageView? = null
    private var initRedColor : Int = 0
    private var initGreenColor : Int = 0
    private var initBlueColor : Int = 0
    private var season : Utility.season = Utility.season.SS
    private var camera: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private val handler = Handler(Looper.getMainLooper())
    private var longPressing = false
    private var isButtonCamera1Visible = true
    private val cameraBlinkHandler = Handler(Looper.getMainLooper())
    //点滅間隔（ミリ秒）
    private val cameraBlinkTime: Long = 500
    //サンプルカラーリスト
    private val sampleColorList : Array<String> =
        arrayOf(
            "#FFC0CB",
            "#FF69B4",
            "#FF1493",
            "#FF00FF",
            "#FA8072",
            "#FF0000",
            "#B22222",
            "#8B0000",
            "#FFFACD",
            "#FFD700",
            "#FFA500",
            "#98FB98",
            "#008000",
            "#6B8E23",
            "#808000",
            "#556B2F",
            "#E0FFFF",
            "#E6E6FA",
            "#ADD8E6",
            "#B0C4DE",
            "#0000FF",
            "#191970",
            "#DDA0DD",
            "#EE82EE",
            "#800080",
            "#F5DEB3",
            "#BDB76B",
            "#D2B48C",
            "#CD853F",
            "#8B4513",
            "#C8C8C8",
            "#808080",
            "#282828"
        )
    //endregion

    //region 画面項目
    private var textViewValueRed : TextView? = null
    private var textViewValueGreen : TextView? = null
    private var textViewValueBlue : TextView? = null
    private var seekBarRed : SeekBar? = null
    private var seekBarGreen : SeekBar? = null
    private var seekBarBlue : SeekBar? = null
    private var imageViewSelectedColor : ImageView? = null
    private var buttonDecided : Button? = null
    private var buttonCamera1 : Button? = null
    private var buttonCamera2 : Button? = null
    private var textureViewCamera : TextureView? = null
    private var buttonLeftRed : Button? = null
    private var buttonLeftGreen : Button? = null
    private var buttonLeftBlue : Button? = null
    private var buttonRightRed : Button? = null
    private var buttonRightGreen : Button? = null
    private var buttonRightBlue : Button? = null

    private var imageViewPalette0 :ImageView? = null
    private var imageViewPalette1 :ImageView? = null
    private var imageViewPalette2 :ImageView? = null
    private var imageViewPalette3 :ImageView? = null
    private var imageViewPalette4 :ImageView? = null
    private var imageViewPalette5 :ImageView? = null
    private var imageViewPalette6 :ImageView? = null
    private var imageViewPalette7 :ImageView? = null
    private var imageViewPalette8 :ImageView? = null
    private var imageViewPalette9 :ImageView? = null
    private var imageViewPalette10 :ImageView? = null
    private var imageViewPalette11 :ImageView? = null
    private var imageViewPalette12 :ImageView? = null
    private var imageViewPalette13 :ImageView? = null
    private var imageViewPalette14 :ImageView? = null
    private var imageViewPalette15 :ImageView? = null
    private var imageViewPalette16 :ImageView? = null
    private var imageViewPalette17 :ImageView? = null
    private var imageViewPalette18 :ImageView? = null
    private var imageViewPalette19 :ImageView? = null
    private var imageViewPalette20 :ImageView? = null
    private var imageViewPalette21 :ImageView? = null
    private var imageViewPalette22 :ImageView? = null
    private var imageViewPalette23 :ImageView? = null
    private var imageViewPalette24 :ImageView? = null
    private var imageViewPalette25 :ImageView? = null
    private var imageViewPalette26 :ImageView? = null
    private var imageViewPalette27 :ImageView? = null
    private var imageViewPalette28 :ImageView? = null
    private var imageViewPalette29 :ImageView? = null
    private var imageViewPalette30 :ImageView? = null
    private var imageViewPalette31 :ImageView? = null
    private var imageViewPalette32 :ImageView? = null
    //endregion

    fun setDialogResultListener(listener: DialogResultListener) {
        dialogResultListener = listener
    }

    fun setTargetImageView(imageView: ImageView?) {
        targetImageView = imageView
        val rGB = Utility.getRGBFromImageView((targetImageView))

        initRedColor = rGB.component1()
        initGreenColor = rGB.component2()
        initBlueColor = rGB.component3()
    }

    //region 季節設定
    fun setSeason(season: Utility.season) {
        this.season = season
    }

    //region ロード処理
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 親Activityのコンテキストを取得
        parentContext = context
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())

        // カスタムしたレイアウトをわたす
        dialog.setContentView(R.layout.colorpickerdialog)
        //コントロールの設定
        setControl(dialog)

        //カメラボタン点滅開始
        startButtonCameraBlinking()

        return dialog
    }

    /**
     * コントロールの設定
     */
    private fun setControl(dialog: Dialog) {
        //各コントロールを設定
        textViewValueRed  =  dialog.findViewById<TextView>(R.id.textViewValueRed)
        textViewValueGreen = dialog.findViewById<TextView>(R.id.textViewValueGreen)
        textViewValueBlue = dialog.findViewById<TextView>(R.id.textViewValueBlue)
        seekBarRed = dialog.findViewById<SeekBar>(R.id.seekBarRed)
        seekBarGreen = dialog.findViewById<SeekBar>(R.id.seekBarGreen)
        seekBarBlue = dialog.findViewById<SeekBar>(R.id.seekBarBlue)
        imageViewSelectedColor = dialog.findViewById<ImageView>(R.id.imageViewSelectedColor)
        buttonDecided = dialog.findViewById<Button>(R.id.buttonDecided)
        buttonCamera1 = dialog.findViewById<Button>(R.id.buttonCamera1)
        buttonCamera2 = dialog.findViewById<Button>(R.id.buttonCamera2)
        textureViewCamera = dialog.findViewById<TextureView>(R.id.textureViewCamera)
        buttonLeftRed  = dialog.findViewById<Button>(R.id.buttonLeftRed)
        buttonLeftGreen = dialog.findViewById<Button>(R.id.buttonLeftGreen)
        buttonLeftBlue= dialog.findViewById<Button>(R.id.buttonLeftBlue)
        buttonRightRed = dialog.findViewById<Button>(R.id.buttonRightRed)
        buttonRightGreen = dialog.findViewById<Button>(R.id.buttonRightGreen)
        buttonRightBlue = dialog.findViewById<Button>(R.id.buttonRightBlue)

        imageViewPalette0 = dialog.findViewById<ImageView>(R.id.imageViewPalette0)
        imageViewPalette1 = dialog.findViewById<ImageView>(R.id.imageViewPalette1)
        imageViewPalette2 = dialog.findViewById<ImageView>(R.id.imageViewPalette2)
        imageViewPalette3 = dialog.findViewById<ImageView>(R.id.imageViewPalette3)
        imageViewPalette4 = dialog.findViewById<ImageView>(R.id.imageViewPalette4)
        imageViewPalette5 = dialog.findViewById<ImageView>(R.id.imageViewPalette5)
        imageViewPalette6 = dialog.findViewById<ImageView>(R.id.imageViewPalette6)
        imageViewPalette7 = dialog.findViewById<ImageView>(R.id.imageViewPalette7)
        imageViewPalette8 = dialog.findViewById<ImageView>(R.id.imageViewPalette8)
        imageViewPalette9 = dialog.findViewById<ImageView>(R.id.imageViewPalette9)
        imageViewPalette10 = dialog.findViewById<ImageView>(R.id.imageViewPalette10)
        imageViewPalette11 = dialog.findViewById<ImageView>(R.id.imageViewPalette11)
        imageViewPalette12 = dialog.findViewById<ImageView>(R.id.imageViewPalette12)
        imageViewPalette13 = dialog.findViewById<ImageView>(R.id.imageViewPalette13)
        imageViewPalette14 = dialog.findViewById<ImageView>(R.id.imageViewPalette14)
        imageViewPalette15 = dialog.findViewById<ImageView>(R.id.imageViewPalette15)
        imageViewPalette16 = dialog.findViewById<ImageView>(R.id.imageViewPalette16)
        imageViewPalette17 = dialog.findViewById<ImageView>(R.id.imageViewPalette17)
        imageViewPalette18 = dialog.findViewById<ImageView>(R.id.imageViewPalette18)
        imageViewPalette19 = dialog.findViewById<ImageView>(R.id.imageViewPalette19)
        imageViewPalette20 = dialog.findViewById<ImageView>(R.id.imageViewPalette20)
        imageViewPalette21 = dialog.findViewById<ImageView>(R.id.imageViewPalette21)
        imageViewPalette22 = dialog.findViewById<ImageView>(R.id.imageViewPalette22)
        imageViewPalette23 = dialog.findViewById<ImageView>(R.id.imageViewPalette23)
        imageViewPalette24 = dialog.findViewById<ImageView>(R.id.imageViewPalette24)
        imageViewPalette25 = dialog.findViewById<ImageView>(R.id.imageViewPalette25)
        imageViewPalette26 = dialog.findViewById<ImageView>(R.id.imageViewPalette26)
        imageViewPalette27 = dialog.findViewById<ImageView>(R.id.imageViewPalette27)
        imageViewPalette28 = dialog.findViewById<ImageView>(R.id.imageViewPalette28)
        imageViewPalette29 = dialog.findViewById<ImageView>(R.id.imageViewPalette29)
        imageViewPalette30 = dialog.findViewById<ImageView>(R.id.imageViewPalette30)
        imageViewPalette31 = dialog.findViewById<ImageView>(R.id.imageViewPalette31)
        imageViewPalette32 = dialog.findViewById<ImageView>(R.id.imageViewPalette32)

        if(season == Utility.season.SS)
        {
            buttonDecided?.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorAccent))
            seekBarRed?.thumbTintList = colorStateList
            seekBarGreen?.thumbTintList = colorStateList
            seekBarBlue?.thumbTintList = colorStateList
            seekBarRed?.progressTintList = colorStateList
            seekBarGreen?.progressTintList = colorStateList
            seekBarBlue?.progressTintList = colorStateList
        }else{
            buttonDecided?.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.smokeblue))
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.smokeblue))
            seekBarRed?.thumbTintList = colorStateList
            seekBarGreen?.thumbTintList = colorStateList
            seekBarBlue?.thumbTintList = colorStateList
            seekBarRed?.progressTintList = colorStateList
            seekBarGreen?.progressTintList = colorStateList
            seekBarBlue?.progressTintList = colorStateList
        }

        textViewValueRed?.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        textViewValueGreen?.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        textViewValueBlue?.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)

        buttonDecided?.setOnClickListener(this)
        buttonCamera1?.setOnClickListener(this)
        buttonCamera2?.setOnClickListener(this)
        buttonLeftRed?.setOnClickListener(this)
        buttonLeftGreen?.setOnClickListener(this)
        buttonLeftBlue?.setOnClickListener(this)
        buttonRightRed?.setOnClickListener(this)
        buttonRightGreen?.setOnClickListener(this)
        buttonRightBlue ?.setOnClickListener(this)
        //長押しのためタッチリスナーも設定
        buttonLeftRed?.setOnTouchListener(this)
        buttonLeftGreen?.setOnTouchListener(this)
        buttonLeftBlue?.setOnTouchListener(this)
        buttonRightRed?.setOnTouchListener(this)
        buttonRightGreen?.setOnTouchListener(this)
        buttonRightBlue ?.setOnTouchListener(this)

        imageViewPalette0?.setOnClickListener(this)
        imageViewPalette1?.setOnClickListener(this)
        imageViewPalette2?.setOnClickListener(this)
        imageViewPalette3?.setOnClickListener(this)
        imageViewPalette4?.setOnClickListener(this)
        imageViewPalette5?.setOnClickListener(this)
        imageViewPalette6?.setOnClickListener(this)
        imageViewPalette7?.setOnClickListener(this)
        imageViewPalette8?.setOnClickListener(this)
        imageViewPalette9?.setOnClickListener(this)
        imageViewPalette10?.setOnClickListener(this)
        imageViewPalette11?.setOnClickListener(this)
        imageViewPalette12?.setOnClickListener(this)
        imageViewPalette13?.setOnClickListener(this)
        imageViewPalette14?.setOnClickListener(this)
        imageViewPalette15?.setOnClickListener(this)
        imageViewPalette16?.setOnClickListener(this)
        imageViewPalette17?.setOnClickListener(this)
        imageViewPalette18?.setOnClickListener(this)
        imageViewPalette19?.setOnClickListener(this)
        imageViewPalette20?.setOnClickListener(this)
        imageViewPalette21?.setOnClickListener(this)
        imageViewPalette22?.setOnClickListener(this)
        imageViewPalette23?.setOnClickListener(this)
        imageViewPalette24?.setOnClickListener(this)
        imageViewPalette25?.setOnClickListener(this)
        imageViewPalette26?.setOnClickListener(this)
        imageViewPalette27?.setOnClickListener(this)
        imageViewPalette28?.setOnClickListener(this)
        imageViewPalette29?.setOnClickListener(this)
        imageViewPalette30?.setOnClickListener(this)
        imageViewPalette31?.setOnClickListener(this)
        imageViewPalette32?.setOnClickListener(this)

        seekBarRed?.setOnSeekBarChangeListener(this)
        seekBarGreen?.setOnSeekBarChangeListener(this)
        seekBarBlue?.setOnSeekBarChangeListener(this)

        textureViewCamera?.surfaceTextureListener = this
        textureViewCamera?.setOnTouchListener { view, motionEvent ->
            //テクスチャービュー押下時
            textureViewCameraClick(view,motionEvent)
            true
        }

        //コントロールがinflateされたことを通知する
        val vto = imageViewSelectedColor?.viewTreeObserver
        vto?.addOnGlobalLayoutListener(this)
    }

    //region ボタン押下時イベント
    override fun onClick(v: View) {
        try {
            //二度押しを禁止
            v.isEnabled = false

            when(v.id) {
                buttonDecided?.id -> {
                    //OKボタン押下時
                    buttonDecidedClick()
                }
                buttonCamera1?.id,
                buttonCamera2?.id-> {
                    //カメラボタン押下時
                    buttonCameraClick()
                }
                buttonLeftRed?.id,
                buttonLeftGreen?.id,
                buttonLeftBlue?.id -> {
                    //左ボタン押下時
                    buttonLeftClick(v.id)
                }
                buttonRightRed?.id,
                buttonRightGreen?.id,
                buttonRightBlue?.id -> {
                    //右ボタン押下時
                    buttonRightClick(v.id)
                }
                imageViewPalette0?.id,
                imageViewPalette1?.id,
                imageViewPalette2?.id,
                imageViewPalette3?.id,
                imageViewPalette4?.id,
                imageViewPalette5?.id,
                imageViewPalette6?.id,
                imageViewPalette7?.id,
                imageViewPalette8?.id,
                imageViewPalette9?.id,
                imageViewPalette10?.id,
                imageViewPalette11?.id,
                imageViewPalette12?.id,
                imageViewPalette13?.id,
                imageViewPalette14?.id,
                imageViewPalette15?.id,
                imageViewPalette16?.id,
                imageViewPalette17?.id,
                imageViewPalette18?.id,
                imageViewPalette19?.id,
                imageViewPalette20?.id,
                imageViewPalette21?.id,
                imageViewPalette22?.id,
                imageViewPalette23?.id,
                imageViewPalette24?.id,
                imageViewPalette25?.id,
                imageViewPalette26?.id,
                imageViewPalette27?.id,
                imageViewPalette28?.id,
                imageViewPalette29?.id,
                imageViewPalette30?.id,
                imageViewPalette31?.id,
                imageViewPalette32?.id -> {
                    //カラーパレット押下時
                    colorPaletteClick(v as ImageView)
                }
            }
        }
        finally {
            v.isEnabled = true
        }
    }

    /**
     * OKボタン押下時
     */
    private fun buttonDecidedClick() : Unit {
        //ビットマップを取得する
        val bitmap = (imageViewSelectedColor?.drawable as BitmapDrawable).bitmap

        val rgb :Triple<Int, Int, Int> = Utility.getRGBFromBitmap(bitmap,0,0)
        //メインアクテビティへ選択結果を返却
        dialogResultListener?.onDialogResult(targetImageView,rgb)

        dismiss()
    }

    /**
     * カメラボタン押下時
     */
    private fun buttonCameraClick() : Unit {
        //カメラのテクスチャビューを表示(裏で常に起動している)
        textureViewCamera?.visibility =  View.VISIBLE
    }

    /**
     * 左ボタン押下時
     */
    private fun buttonLeftClick(id : Int) : Unit {
        //該当のシークバーを-1する
        when (id) {
            buttonLeftRed?.id -> {
                (seekBarRed as SeekBar).progress -= 1
                textViewValueRed?.text = seekBarRed?.progress.toString()
            }
            buttonLeftGreen?.id -> {
                (seekBarGreen as SeekBar).progress -= 1
                textViewValueGreen?.text = seekBarGreen?.progress.toString()
            }
            buttonLeftBlue?.id -> {
                (seekBarBlue as SeekBar).progress -= 1
                textViewValueBlue?.text = seekBarBlue?.progress.toString()
            }
        }

        //イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor,seekBarRed?.progress ?: 0,seekBarGreen?.progress ?: 0,  seekBarBlue?.progress ?: 0))
    }

    /**
     * 右ボタン押下時
     */
    private fun buttonRightClick(id : Int) : Unit {
        //該当のシークバーを+1する
        when (id) {
            buttonRightRed?.id -> {
                (seekBarRed as SeekBar).progress += 1
                textViewValueRed?.text = seekBarRed?.progress.toString()
            }
            buttonRightGreen?.id -> {
                (seekBarGreen as SeekBar).progress += 1
                textViewValueGreen?.text = seekBarGreen?.progress.toString()
            }
            buttonRightBlue?.id -> {
                (seekBarBlue as SeekBar).progress += 1
                textViewValueBlue?.text = seekBarBlue?.progress.toString()
            }
        }

        //イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor,seekBarRed?.progress ?: 0,seekBarGreen?.progress ?: 0,  seekBarBlue?.progress ?: 0))
    }

    /**
     * カラーパレット押下時
     */
    private fun colorPaletteClick(imageView: ImageView?) {
        //ImageViewからRGBを取得
        val rGB = Utility.getRGBFromImageView((imageView))

        initRedColor = rGB.component1()
        initGreenColor = rGB.component2()
        initBlueColor = rGB.component3()

        //イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor, initRedColor, initGreenColor,   initBlueColor))
        //シークバーへも反映
        seekBarRed?.progress = initRedColor
        seekBarGreen?.progress = initGreenColor
        seekBarBlue?.progress = initBlueColor
    }

    /**
     * テクスチャービュー押下時
     */
    private fun textureViewCameraClick(view: View, event: MotionEvent) : Unit {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                //ビットマップ作成(キャプチャ)
                val bitmap = (view as TextureView).getBitmap()

                var colorIntList = mutableListOf<Int>()

                //タッチした場所とその周りの色を取得
                colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt() - 1))
                colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt()))
                colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt() + 1))
                colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt() - 1))
                colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt()))
                colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt() + 1))
                colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt() - 1))
                colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt()))
                colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt() + 1))

                var touchRedColorList = mutableSetOf<Int>()
                var touchGreenColorList = mutableSetOf<Int>()
                var touchBlueColorList = mutableSetOf<Int>()

                //最終的なカラーリストへ格納
                for (color in colorIntList) {
                    touchRedColorList.add(Color.red(color))
                    touchGreenColorList.add( Color.green(color))
                    touchBlueColorList.add(Color.blue(color))
                }

                // リストの要素数を取得する
                val colorCnt = touchRedColorList.size
                // 平均を取得する
                val redColor =  (touchRedColorList.sum().toDouble()/ colorCnt).toInt()
                val greenColor = (touchGreenColorList.sum().toDouble() / colorCnt).toInt()
                val blueColor = (touchBlueColorList.sum().toDouble() / colorCnt).toInt()

                //イメージビューに色を反映
                imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor, redColor, greenColor,   blueColor))
                //シークバーへも反映
                seekBarRed?.progress = redColor
                seekBarGreen?.progress = greenColor
                seekBarBlue?.progress = blueColor
            }
        }
        //カメラのテクスチャビューを非表示する
        textureViewCamera?.visibility =  View.INVISIBLE
    }
    //endregion

    //region ボタン長押しイベント

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(v?.id) {
            buttonLeftRed?.id,
            buttonLeftGreen?.id,
            buttonLeftBlue?.id -> {
                //左ボタン押下時
                buttonLeftLongClick((v as View).id, event as MotionEvent)
            }
            buttonRightRed?.id,
            buttonRightGreen?.id,
            buttonRightBlue?.id -> {
                //右ボタン押下時
                buttonRightLongClick((v as View).id, event as MotionEvent)
            }
        }

        return true
    }

    /**
     * 左ボタン長押し時
     */
    fun buttonLeftLongClick(id : Int, event: MotionEvent) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // 長押しが開始されたときの処理
                longPressing = true

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        when (id) {
                            buttonLeftRed?.id -> {
                                (seekBarRed as SeekBar).progress -= 1
                                textViewValueRed?.text = seekBarRed?.progress.toString()
                            }
                            buttonLeftGreen?.id -> {
                                (seekBarGreen as SeekBar).progress -= 1
                                textViewValueGreen?.text = seekBarGreen?.progress.toString()
                            }
                            buttonLeftBlue?.id -> {
                                (seekBarBlue as SeekBar).progress -= 1
                                textViewValueBlue?.text = seekBarBlue?.progress.toString()
                            }
                        }

                        //イメージビューに色を反映
                        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor,seekBarRed?.progress ?: 0,seekBarGreen?.progress ?: 0,  seekBarBlue?.progress ?: 0))

                        // 再帰呼び出し
                        buttonLeftLongClick(id, event)
                    }
                }, 50)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 長押しが解除されたときの処理
                longPressing = false
                // タイマーを停止
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    /**
     * 右ボタン長押し時
     */
    fun buttonRightLongClick(id : Int, event: MotionEvent) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // 長押しが開始されたときの処理
                longPressing = true

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        when (id) {
                            buttonRightRed?.id -> {
                                (seekBarRed as SeekBar).progress += 1
                                textViewValueRed?.text = seekBarRed?.progress.toString()
                            }
                            buttonRightGreen?.id -> {
                                (seekBarGreen as SeekBar).progress += 1
                                textViewValueGreen?.text = seekBarGreen?.progress.toString()
                            }
                            buttonRightBlue?.id -> {
                                (seekBarBlue as SeekBar).progress += 1
                                textViewValueBlue?.text = seekBarBlue?.progress.toString()
                            }
                        }

                        //イメージビューに色を反映
                        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor,seekBarRed?.progress ?: 0,seekBarGreen?.progress ?: 0,  seekBarBlue?.progress ?: 0))

                        // 再帰呼び出し
                        buttonRightLongClick(id, event)
                    }
                }, 50)

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 長押しが解除されたときの処理
                longPressing = false
                // タイマーを停止
                handler.removeCallbacksAndMessages(null)
            }
        }
    }
    //endregion

    //region シークバー関連
    /**
     * 値が変更された時に呼ばれる
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        try {
            seekBar?.isEnabled = false

            when (seekBar?.id) {
                seekBarRed?.id -> {
                    textViewValueRed?.text = progress.toString()
                }
                seekBarGreen?.id -> {
                    textViewValueGreen?.text = progress.toString()
                }
                seekBarBlue?.id -> {
                    textViewValueBlue?.text = progress.toString()
                }
            }

            //イメージビューに色を反映
            imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor,seekBarRed?.progress ?: 0,seekBarGreen?.progress ?: 0,  seekBarBlue?.progress ?: 0))
        }
        finally {
            seekBar?.isEnabled = true
        }
    }

    /**
     * つまみがタッチされた時に呼ばれる
     */
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    /**
     * つまみが離された時に呼ばれる
     */
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
    //endregion

    //region その他関数
    /**
     * コントロールがinflateされたことを通知
     */
    override fun onGlobalLayout() {
        //イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(Utility.createBitmap(imageViewSelectedColor, initRedColor, initGreenColor,   initBlueColor))
        //シークバーへも反映
        seekBarRed?.progress = initRedColor
        seekBarGreen?.progress = initGreenColor
        seekBarBlue?.progress = initBlueColor

        //カラーパレット設定
        setColorPalette()
    }

    /**
     * カラーパレット設定
     */
    private fun setColorPalette(): Unit {
        val palette0 = Utility.getColorCodeToRGB(sampleColorList[0])
        val palette1 = Utility.getColorCodeToRGB(sampleColorList[1])
        val palette2 = Utility.getColorCodeToRGB(sampleColorList[2])
        val palette3 = Utility.getColorCodeToRGB(sampleColorList[3])
        val palette4 = Utility.getColorCodeToRGB(sampleColorList[4])
        val palette5 = Utility.getColorCodeToRGB(sampleColorList[5])
        val palette6 = Utility.getColorCodeToRGB(sampleColorList[6])
        val palette7 = Utility.getColorCodeToRGB(sampleColorList[7])
        val palette8 = Utility.getColorCodeToRGB(sampleColorList[8])
        val palette9 = Utility.getColorCodeToRGB(sampleColorList[9])
        val palette10 = Utility.getColorCodeToRGB(sampleColorList[10])
        val palette11 = Utility.getColorCodeToRGB(sampleColorList[11])
        val palette12 = Utility.getColorCodeToRGB(sampleColorList[12])
        val palette13 = Utility.getColorCodeToRGB(sampleColorList[13])
        val palette14 = Utility.getColorCodeToRGB(sampleColorList[14])
        val palette15 = Utility.getColorCodeToRGB(sampleColorList[15])
        val palette16 = Utility.getColorCodeToRGB(sampleColorList[16])
        val palette17 = Utility.getColorCodeToRGB(sampleColorList[17])
        val palette18 = Utility.getColorCodeToRGB(sampleColorList[18])
        val palette19 = Utility.getColorCodeToRGB(sampleColorList[19])
        val palette20 = Utility.getColorCodeToRGB(sampleColorList[20])
        val palette21 = Utility.getColorCodeToRGB(sampleColorList[21])
        val palette22 = Utility.getColorCodeToRGB(sampleColorList[22])
        val palette23 = Utility.getColorCodeToRGB(sampleColorList[23])
        val palette24 = Utility.getColorCodeToRGB(sampleColorList[24])
        val palette25 = Utility.getColorCodeToRGB(sampleColorList[25])
        val palette26 = Utility.getColorCodeToRGB(sampleColorList[26])
        val palette27 = Utility.getColorCodeToRGB(sampleColorList[27])
        val palette28 = Utility.getColorCodeToRGB(sampleColorList[28])
        val palette29 = Utility.getColorCodeToRGB(sampleColorList[29])
        val palette30 = Utility.getColorCodeToRGB(sampleColorList[30])
        val palette31 = Utility.getColorCodeToRGB(sampleColorList[31])
        val palette32 = Utility.getColorCodeToRGB(sampleColorList[32])

        //カラーパレットへサンプルカラーを設定
        imageViewPalette0?.setImageBitmap(Utility.createBitmap(imageViewPalette0, palette0.component1(),palette0.component2(),  palette0.component3()))
        imageViewPalette1?.setImageBitmap(Utility.createBitmap(imageViewPalette1, palette1.component1(),palette1.component2(),  palette1.component3()))
        imageViewPalette2?.setImageBitmap(Utility.createBitmap(imageViewPalette2, palette2.component1(),palette2.component2(),  palette2.component3()))
        imageViewPalette3?.setImageBitmap(Utility.createBitmap(imageViewPalette3, palette3.component1(),palette3.component2(),  palette3.component3()))
        imageViewPalette4?.setImageBitmap(Utility.createBitmap(imageViewPalette4, palette4.component1(),palette4.component2(),  palette4.component3()))
        imageViewPalette5?.setImageBitmap(Utility.createBitmap(imageViewPalette5, palette5.component1(),palette5.component2(),  palette5.component3()))
        imageViewPalette6?.setImageBitmap(Utility.createBitmap(imageViewPalette6, palette6.component1(),palette6.component2(),  palette6.component3()))
        imageViewPalette7?.setImageBitmap(Utility.createBitmap(imageViewPalette7, palette7.component1(),palette7.component2(),  palette7.component3()))
        imageViewPalette8?.setImageBitmap(Utility.createBitmap(imageViewPalette8, palette8.component1(),palette8.component2(),  palette8.component3()))
        imageViewPalette9?.setImageBitmap(Utility.createBitmap(imageViewPalette9, palette9.component1(),palette9.component2(),  palette9.component3()))
        imageViewPalette10?.setImageBitmap(Utility.createBitmap(imageViewPalette10, palette10.component1(),palette10.component2(),  palette10.component3()))
        imageViewPalette11?.setImageBitmap(Utility.createBitmap(imageViewPalette11, palette11.component1(),palette11.component2(),  palette11.component3()))
        imageViewPalette12?.setImageBitmap(Utility.createBitmap(imageViewPalette12, palette12.component1(),palette12.component2(),  palette12.component3()))
        imageViewPalette13?.setImageBitmap(Utility.createBitmap(imageViewPalette13, palette13.component1(),palette13.component2(),  palette13.component3()))
        imageViewPalette14?.setImageBitmap(Utility.createBitmap(imageViewPalette14, palette14.component1(),palette14.component2(),  palette14.component3()))
        imageViewPalette15?.setImageBitmap(Utility.createBitmap(imageViewPalette15, palette15.component1(),palette15.component2(),  palette15.component3()))
        imageViewPalette16?.setImageBitmap(Utility.createBitmap(imageViewPalette16, palette16.component1(),palette16.component2(),  palette16.component3()))
        imageViewPalette17?.setImageBitmap(Utility.createBitmap(imageViewPalette17, palette17.component1(),palette17.component2(),  palette17.component3()))
        imageViewPalette18?.setImageBitmap(Utility.createBitmap(imageViewPalette18, palette18.component1(),palette18.component2(),  palette18.component3()))
        imageViewPalette19?.setImageBitmap(Utility.createBitmap(imageViewPalette19, palette19.component1(),palette19.component2(),  palette19.component3()))
        imageViewPalette20?.setImageBitmap(Utility.createBitmap(imageViewPalette20, palette20.component1(),palette20.component2(),  palette20.component3()))
        imageViewPalette21?.setImageBitmap(Utility.createBitmap(imageViewPalette21, palette21.component1(),palette21.component2(),  palette21.component3()))
        imageViewPalette22?.setImageBitmap(Utility.createBitmap(imageViewPalette22, palette22.component1(),palette22.component2(),  palette22.component3()))
        imageViewPalette23?.setImageBitmap(Utility.createBitmap(imageViewPalette23, palette23.component1(),palette23.component2(),  palette23.component3()))
        imageViewPalette24?.setImageBitmap(Utility.createBitmap(imageViewPalette24, palette24.component1(),palette24.component2(),  palette24.component3()))
        imageViewPalette25?.setImageBitmap(Utility.createBitmap(imageViewPalette25, palette25.component1(),palette25.component2(),  palette25.component3()))
        imageViewPalette26?.setImageBitmap(Utility.createBitmap(imageViewPalette26, palette26.component1(),palette26.component2(),  palette26.component3()))
        imageViewPalette27?.setImageBitmap(Utility.createBitmap(imageViewPalette27, palette27.component1(),palette27.component2(),  palette27.component3()))
        imageViewPalette28?.setImageBitmap(Utility.createBitmap(imageViewPalette28, palette28.component1(),palette28.component2(),  palette28.component3()))
        imageViewPalette29?.setImageBitmap(Utility.createBitmap(imageViewPalette29, palette29.component1(),palette29.component2(),  palette29.component3()))
        imageViewPalette30?.setImageBitmap(Utility.createBitmap(imageViewPalette30, palette30.component1(),palette30.component2(),  palette30.component3()))
        imageViewPalette31?.setImageBitmap(Utility.createBitmap(imageViewPalette31, palette31.component1(),palette31.component2(),  palette31.component3()))
        imageViewPalette32?.setImageBitmap(Utility.createBitmap(imageViewPalette32, palette32.component1(),palette32.component2(),  palette32.component3()))
    }

    /**
     * カメラボタン点滅開始
     */
    private fun startButtonCameraBlinking() {
        handler.post(object : Runnable {
            override fun run() {
                if (isButtonCamera1Visible) {
                    buttonCamera1?.visibility = Button.INVISIBLE
                    buttonCamera2?.visibility = Button.VISIBLE
                } else {
                    buttonCamera1?.visibility = Button.VISIBLE
                    buttonCamera2?.visibility = Button.INVISIBLE
                }

                isButtonCamera1Visible = !isButtonCamera1Visible

                // 次の点滅を予約
                cameraBlinkHandler.postDelayed(this, cameraBlinkTime)
            }
        })
    }
    //endregion

    //region TextureView関連
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        //カメラのセットアップ
        setupCamera()
        //カメラ起動
        openCamera()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    private fun setupCamera() {
        val cameraManager = parentContext?.getSystemService(CAMERA_SERVICE) as CameraManager
        //カメラのIDを選択
        val cameraId = cameraManager.cameraIdList[0]

        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        // プレビューサイズを選択（ここで適切なサイズを選択する必要があります）
        val previewSize = map?.getOutputSizes(SurfaceTexture::class.java)?.first()
    }

    private fun openCamera() {
        val cameraManager = parentContext?.getSystemService(CAMERA_SERVICE) as CameraManager
        //カメラのIDを選択
        val cameraId = cameraManager.cameraIdList[0]

        if (ActivityCompat.checkSelfPermission(parentContext as Context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    camera = cameraDevice
                    createCameraPreview()
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {
                    cameraDevice.close()
                }

                override fun onError(cameraDevice: CameraDevice, error: Int) {
                    cameraDevice.close()
                    camera = null
                }
            }, null)
        }
    }

    private fun createCameraPreview() {
        val texture = textureViewCamera?.surfaceTexture
        val surface = Surface(texture)

        captureRequestBuilder =(camera as CameraDevice).createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        camera?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                if (camera == null) {
                    return
                }

                cameraCaptureSession = session
                updatePreview()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                // キャプチャーセッションのセットアップに失敗したときの処理
            }
        }, null)
    }

    private fun updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        val captureRequest = captureRequestBuilder.build()

        cameraCaptureSession.setRepeatingRequest(captureRequest, null, null)
    }
    //endregion
}