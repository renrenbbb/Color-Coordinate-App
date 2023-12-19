package com.example.colorapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * カラーピッカーダイアログ
 */
class CustomDialog : DialogFragment(),
    View.OnClickListener,
    View.OnTouchListener,
    SeekBar.OnSeekBarChangeListener,
    ViewTreeObserver.OnGlobalLayoutListener,
    TextureView.SurfaceTextureListener {

    //region インターフェース
    interface DialogResultListener {
        fun onDialogResult(targetImageView: ImageView?, rgb: RGB)
    }
    //endregion

    //region 定数・変数
    private var dialogResultListener: DialogResultListener? = null
    private var parentContext: Context? = null
    private var targetImageView: ImageView? = null
    private var initRedColor: Int = 0
    private var initGreenColor: Int = 0
    private var initBlueColor: Int = 0
    private var season: Season = Season.SS
    private var camera: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var newCoroutineScope: CoroutineScope? = null

    //    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var longPressing = false
    private var isButtonCamera1Visible = true
    private val coroutineScope_blink = CoroutineScope(Dispatchers.Main)

    //点滅間隔（ミリ秒）
    private val cameraBlinkTime: Long = 500

    //サンプルカラーリスト
    private val sampleColorList: Array<String> =
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
    private var textViewValueRed: TextView? = null
    private var textViewValueGreen: TextView? = null
    private var textViewValueBlue: TextView? = null
    private var seekBarRed: SeekBar? = null
    private var seekBarGreen: SeekBar? = null
    private var seekBarBlue: SeekBar? = null
    private var imageViewSelectedColor: ImageView? = null
    private var buttonDecided: Button? = null
    private var buttonCamera1: Button? = null
    private var buttonCamera2: Button? = null
    private var textureViewCamera: TextureView? = null
    private var buttonLeftRed: Button? = null
    private var buttonLeftGreen: Button? = null
    private var buttonLeftBlue: Button? = null
    private var buttonRightRed: Button? = null
    private var buttonRightGreen: Button? = null
    private var buttonRightBlue: Button? = null

    private lateinit var imageViewPaletteList: MutableList<ImageView?>
    //endregion

    //region 静的メンバー
    /**
     * ダイアログリスナーの設定
     */
    fun setDialogResultListener(listener: DialogResultListener) {
        dialogResultListener = listener
    }

    /**
     * ターゲットイメージビューの設定
     */
    fun setTargetImageView(imageView: ImageView?) {
        targetImageView = imageView
        val rGB = Utility.getRGBFromImageView((targetImageView))

        initRedColor = rGB.red
        initGreenColor = rGB.green
        initBlueColor = rGB.blue
    }

    /**
     * 季節設定
     */
    fun setSeason(season: Season) {
        this.season = season
    }
    //endregion

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
        textViewValueRed = dialog.findViewById<TextView>(R.id.textViewValueRed)
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
        buttonLeftRed = dialog.findViewById<Button>(R.id.buttonLeftRed)
        buttonLeftGreen = dialog.findViewById<Button>(R.id.buttonLeftGreen)
        buttonLeftBlue = dialog.findViewById<Button>(R.id.buttonLeftBlue)
        buttonRightRed = dialog.findViewById<Button>(R.id.buttonRightRed)
        buttonRightGreen = dialog.findViewById<Button>(R.id.buttonRightGreen)
        buttonRightBlue = dialog.findViewById<Button>(R.id.buttonRightBlue)

        if (season == Season.SS) {
            buttonDecided?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )
            val colorStateList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )
            seekBarRed?.thumbTintList = colorStateList
            seekBarGreen?.thumbTintList = colorStateList
            seekBarBlue?.thumbTintList = colorStateList
            seekBarRed?.progressTintList = colorStateList
            seekBarGreen?.progressTintList = colorStateList
            seekBarBlue?.progressTintList = colorStateList
        } else {
            buttonDecided?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.smokeblue
                )
            )
            val colorStateList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.smokeblue))
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
        buttonRightBlue?.setOnClickListener(this)
        //長押しのためタッチリスナーも設定
        buttonLeftRed?.setOnTouchListener(this)
        buttonLeftGreen?.setOnTouchListener(this)
        buttonLeftBlue?.setOnTouchListener(this)
        buttonRightRed?.setOnTouchListener(this)
        buttonRightGreen?.setOnTouchListener(this)
        buttonRightBlue?.setOnTouchListener(this)

        //イメージビューを設定
        imageViewPaletteList = mutableListOf<ImageView?>()
        for (i in 0 until sampleColorList.size) {
            val imageViewId =
                resources.getIdentifier("imageViewPalette$i", "id", parentContext?.packageName)
            val imageViewPalette = dialog.findViewById<ImageView>(imageViewId)
            imageViewPalette.setOnClickListener(this)
            imageViewPaletteList.add(imageViewPalette)
        }

        seekBarRed?.setOnSeekBarChangeListener(this)
        seekBarGreen?.setOnSeekBarChangeListener(this)
        seekBarBlue?.setOnSeekBarChangeListener(this)

        textureViewCamera?.surfaceTextureListener = this
        textureViewCamera?.setOnTouchListener { view, motionEvent ->
            //テクスチャービュー押下時
            textureViewCameraClick(view, motionEvent)
            true
        }

        //コントロールがinflateされたことを通知する
        val vto = imageViewSelectedColor?.viewTreeObserver
        vto?.addOnGlobalLayoutListener(this)
    }

    /**
     * onDismiss
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        //CoroutineScopeをキャンセルする
        coroutineScope_blink.cancel()
    }
    //endregion

    //region ボタン押下時イベント
    override fun onClick(v: View) {
        try {
            //二度押しを禁止
            v.isEnabled = false

            when (v.id) {
                buttonDecided?.id -> {
                    //OKボタン押下時
                    buttonDecidedClick()
                }

                buttonCamera1?.id,
                buttonCamera2?.id -> {
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

                else -> {
                    //カラーパレット押下時
                    colorPaletteClick(v as ImageView)
                }
            }
        } finally {
            v.isEnabled = true
        }
    }

    /**
     * OKボタン押下時
     */
    private fun buttonDecidedClick(): Unit {
        //ビットマップを取得する
        val bitmap = (imageViewSelectedColor?.drawable as BitmapDrawable).bitmap

        val rgb: RGB = Utility.getRGBFromBitmap(bitmap, 0, 0)
        //メインアクテビティへ選択結果を返却
        dialogResultListener?.onDialogResult(targetImageView, rgb)

        dismiss()
    }

    /**
     * カメラボタン押下時
     */
    private fun buttonCameraClick(): Unit {
        // カメラの権限が許可されていない場合はトースト表示する
        if (!Utility.checkCameraPermission(parentContext as Activity)) {
            Utility.requestCameraPermission(parentContext as Activity)
        } else {
            //カメラのテクスチャビューを表示(裏で常に起動している)
            textureViewCamera?.visibility = View.VISIBLE
        }
    }

    /**
     * 左ボタン押下時
     */
    private fun buttonLeftClick(id: Int): Unit {
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
        imageViewSelectedColor?.setImageBitmap(
            Utility.createBitmap(
                imageViewSelectedColor,
                seekBarRed?.progress ?: 0,
                seekBarGreen?.progress ?: 0,
                seekBarBlue?.progress ?: 0
            )
        )
    }

    /**
     * 右ボタン押下時
     */
    private fun buttonRightClick(id: Int): Unit {
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
        imageViewSelectedColor?.setImageBitmap(
            Utility.createBitmap(
                imageViewSelectedColor,
                seekBarRed?.progress ?: 0,
                seekBarGreen?.progress ?: 0,
                seekBarBlue?.progress ?: 0
            )
        )
    }

    /**
     * カラーパレット押下時
     */
    private fun colorPaletteClick(imageView: ImageView?) {
        //ImageViewからRGBを取得
        val rGB = Utility.getRGBFromImageView((imageView))

        initRedColor = rGB.red
        initGreenColor = rGB.green
        initBlueColor = rGB.blue

        //イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(
            Utility.createBitmap(
                imageViewSelectedColor,
                initRedColor,
                initGreenColor,
                initBlueColor
            )
        )
        //シークバーへも反映
        seekBarRed?.progress = initRedColor
        seekBarGreen?.progress = initGreenColor
        seekBarBlue?.progress = initBlueColor
    }

    /**
     * テクスチャービュー押下時
     */
    private fun textureViewCameraClick(view: View, event: MotionEvent): Unit {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                //ビットマップ作成(キャプチャ)
                val bitmap = (view as TextureView).getBitmap()

                var colorIntList = mutableListOf<Int>()

                //タッチした場所とその周りの色を取得
                if (bitmap != null) {
                    colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt() - 1))
                    colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt()))
                    colorIntList.add(bitmap.getPixel(x.toInt(), y.toInt() + 1))
                    colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt() - 1))
                    colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt()))
                    colorIntList.add(bitmap.getPixel(x.toInt() - 1, y.toInt() + 1))
                    colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt() - 1))
                    colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt()))
                    colorIntList.add(bitmap.getPixel(x.toInt() + 1, y.toInt() + 1))
                }

                var touchRedColorList = mutableSetOf<Int>()
                var touchGreenColorList = mutableSetOf<Int>()
                var touchBlueColorList = mutableSetOf<Int>()

                //最終的なカラーリストへ格納
                for (color in colorIntList) {
                    touchRedColorList.add(Color.red(color))
                    touchGreenColorList.add(Color.green(color))
                    touchBlueColorList.add(Color.blue(color))
                }

                // リストの要素数を取得する
                val colorCnt = touchRedColorList.size
                // 平均を取得する
                val redColor = (touchRedColorList.sum().toDouble() / colorCnt).toInt()
                val greenColor = (touchGreenColorList.sum().toDouble() / colorCnt).toInt()
                val blueColor = (touchBlueColorList.sum().toDouble() / colorCnt).toInt()

                //イメージビューに色を反映
                imageViewSelectedColor?.setImageBitmap(
                    Utility.createBitmap(
                        imageViewSelectedColor,
                        redColor,
                        greenColor,
                        blueColor
                    )
                )
                //シークバーへも反映
                seekBarRed?.progress = redColor
                seekBarGreen?.progress = greenColor
                seekBarBlue?.progress = blueColor
            }
        }
        //カメラのテクスチャビューを非表示する
        textureViewCamera?.visibility = View.INVISIBLE
    }
    //endregion

    //region ボタン長押しイベント
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            buttonLeftRed?.id,
            buttonLeftGreen?.id,
            buttonLeftBlue?.id -> {
                //左ボタン押下時
                buttonRightLeftLongClick((v as View).id, event as MotionEvent, Sign.Minus)
            }

            buttonRightRed?.id,
            buttonRightGreen?.id,
            buttonRightBlue?.id -> {
                //右ボタン押下時
                buttonRightLeftLongClick((v as View).id, event as MotionEvent, Sign.Plus)
            }
        }

        return true
    }

    /**
     * 右ボタン左ボタン長押し時
     */
    private fun buttonRightLeftLongClick(id: Int, event: MotionEvent, sign: Sign) {
        // 新しいCoroutineScopeを作成
        newCoroutineScope = CoroutineScope(Dispatchers.Main)

        newCoroutineScope?.launch {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 長押しが開始されたときの処理
                    longPressing = true

                    while (isActive && longPressing) {
                        // 50ミリ秒待機
                        delay(50)

                        //色シークバーの増減
                        increaseDecreaseSeekBar(id, sign)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 長押しが解除されたときの処理
                    longPressing = false
                    // CoroutineScopeをキャンセル
                    newCoroutineScope?.cancel()
                }
            }
        }
    }

    /**
     * 色シークバーの増減
     */
    private fun increaseDecreaseSeekBar(id: Int, sign: Sign) {
        //引数によって増減を変更
        val amount = if (sign == Sign.Plus) {
            1
        } else {
            -1
        }

        when (id) {
            buttonRightRed?.id,
            buttonLeftRed?.id -> {
                (seekBarRed as SeekBar).progress += amount
                textViewValueRed?.text = seekBarRed?.progress.toString()
            }

            buttonRightGreen?.id,
            buttonLeftGreen?.id -> {
                (seekBarGreen as SeekBar).progress += amount
                textViewValueGreen?.text = seekBarGreen?.progress.toString()
            }

            buttonRightBlue?.id,
            buttonLeftBlue?.id -> {
                (seekBarBlue as SeekBar).progress += amount
                textViewValueBlue?.text = seekBarBlue?.progress.toString()
            }
        }

        // イメージビューに色を反映
        imageViewSelectedColor?.setImageBitmap(
            Utility.createBitmap(
                imageViewSelectedColor,
                seekBarRed?.progress ?: 0,
                seekBarGreen?.progress ?: 0,
                seekBarBlue?.progress ?: 0
            )
        )
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
            imageViewSelectedColor?.setImageBitmap(
                Utility.createBitmap(
                    imageViewSelectedColor,
                    seekBarRed?.progress ?: 0,
                    seekBarGreen?.progress ?: 0,
                    seekBarBlue?.progress ?: 0
                )
            )
        } finally {
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
        imageViewSelectedColor?.setImageBitmap(
            Utility.createBitmap(
                imageViewSelectedColor,
                initRedColor,
                initGreenColor,
                initBlueColor
            )
        )
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
    private fun setColorPalette() {
        //カラーパレットへサンプルカラーを設定
        imageViewPaletteList.forEachIndexed { index: Int, element: ImageView? ->
            val palette = Utility.getColorCodeToRGB(sampleColorList[index])
            element?.setImageBitmap(
                Utility.createBitmap(
                    element,
                    palette.red,
                    palette.green,
                    palette.blue
                )
            )
        }
    }

    /**
     * カメラボタン点滅開始
     */
    private fun startButtonCameraBlinking() {
        coroutineScope_blink.launch {
            while (isActive) {
                if (isButtonCamera1Visible) {
                    buttonCamera1?.visibility = Button.INVISIBLE
                    buttonCamera2?.visibility = Button.VISIBLE
                } else {
                    buttonCamera1?.visibility = Button.VISIBLE
                    buttonCamera2?.visibility = Button.INVISIBLE
                }

                isButtonCamera1Visible = !isButtonCamera1Visible

                // 次の点滅を予約
                delay(cameraBlinkTime)
            }
        }
    }
    //endregion

    //region TextureView関連
    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        //カメラのセットアップ
        setupCamera()
        //カメラ起動
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
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

        if (ActivityCompat.checkSelfPermission(
                parentContext as Context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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

        captureRequestBuilder =
            (camera as CameraDevice).createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        camera?.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
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
            },
            null
        )
    }

    private fun updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        val captureRequest = captureRequestBuilder.build()

        cameraCaptureSession.setRepeatingRequest(captureRequest, null, null)
    }
    //endregion
}