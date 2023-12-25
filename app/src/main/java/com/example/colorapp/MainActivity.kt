package com.example.colorapp

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * メインアクテビティ
 */
class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    RadioGroup.OnCheckedChangeListener,
    CustomDialog.DialogResultListener,
    CompoundButton.OnCheckedChangeListener {

    //region 定数・変数
    //APIキーなしの場合は天気取得しない
    private val OPENWEATHER_API_KEY = Config.getOpenWeatherApiKey()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //東京都の緯度・経度
    private val TOKYO_LATITUDE = 35.6895
    private val TOKYO_LONGITUDE = 139.6917
    //endregion

    //region 画面項目
    private var textViewMessage: TextView? = null
    private var textViewCity: TextView? = null
    private var textViewWeather: TextView? = null
    private var textViewTemperature: TextView? = null
    private var buttonSearch: Button? = null
    private var buttonClose: Button? = null
    private var radioButtonTargetTops: RadioButton? = null
    private var radioButtonTargetBottoms: RadioButton? = null
    private var radioButtonTargetShoes: RadioButton? = null
    private var radioGroupTargetItem: RadioGroup? = null
    private var imageViewTopsColor: ImageView? = null
    private var imageViewBottomsColor: ImageView? = null
    private var imageViewShoesColor: ImageView? = null
    private var checkBoxSort: CheckBox? = null
    private var viewPager2SampleColor: ViewPager2? = null
    private var switchSeason: Switch? = null
    private var navigationViewMain: NavigationView? = null
    private var drawerLayoutMain: DrawerLayout? = null
    //endregion

    //region ロード処理
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            //ナビゲーションバーの透過に必要
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            //レイアウトの設定
            setContentView(R.layout.activity_main)

            //コントロールの設定
            setControl()

            //カルーセル設定
            setCarousel(CarouselStatus.Initial)

            // fusedLocationClient の初期化を追加
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // 位置情報の権限が許可されていない場合はユーザーに許可を求める
            if (!Utility.checkLocationPermission(this)) {
                Utility.requestLocationPermission(this)
            } else {
                lifecycleScope.launch {
                    //現在の位置情報を取得する
                    val locationInfo = requestLocation()
                    //現在位置から天気を取得する
                    getWeatherCurrentLocation(locationInfo.latitude, locationInfo.longitude)
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    /**
     * コントロールの設定
     */
    private fun setControl() {
        textViewMessage = findViewById<TextView>(R.id.textViewMessage)
        textViewCity = findViewById<TextView>(R.id.textViewCity)
        textViewWeather = findViewById<TextView>(R.id.textViewWeather)
        textViewTemperature = findViewById<TextView>(R.id.textViewTemperature)
        buttonSearch = findViewById<Button>(R.id.buttonSearch)
        buttonClose = findViewById<Button>(R.id.buttonClose)
        radioButtonTargetTops = findViewById<RadioButton>(R.id.radioButtonTargetTops)
        radioButtonTargetBottoms = findViewById<RadioButton>(R.id.radioButtonTargetBottoms)
        radioButtonTargetShoes = findViewById<RadioButton>(R.id.radioButtonTargetShoes)
        radioGroupTargetItem = findViewById<RadioGroup>(R.id.radioGroupTargetItem)
        imageViewTopsColor = findViewById<ImageView>(R.id.imageViewTopsColor)
        imageViewBottomsColor = findViewById<ImageView>(R.id.imageViewBottomsColor)
        imageViewShoesColor = findViewById<ImageView>(R.id.imageViewShoesColor)
        checkBoxSort = findViewById<CheckBox>(R.id.checkBoxSort)
        viewPager2SampleColor = findViewById<ViewPager2>(R.id.viewPager2SampleColor)
        switchSeason = findViewById<Switch>(R.id.switchSeason)
        navigationViewMain = findViewById<NavigationView>(R.id.navigationViewMain)
        drawerLayoutMain = findViewById<DrawerLayout>(R.id.drawerLayoutMain)

        buttonSearch?.setOnClickListener(this)
        buttonClose?.setOnClickListener(this)
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
            //二度押しを禁止
            v.isEnabled = false

            when (v.id) {
                buttonSearch?.id -> {
                    //検索ボタン押下時
                    buttonSearchClick()
                }

                buttonClose?.id -> {
                    //閉じるボタン押下時
                    buttonCloseClick()
                }

                imageViewTopsColor?.id, imageViewBottomsColor?.id, imageViewShoesColor?.id -> {
                    //カラーピッカーダイアログ表示
                    showColorPickerDialog(v as ImageView?)
                }
            }
        } finally {
            v.isEnabled = true
        }
    }

    /**
     * 検索ボタン押下時
     */
    private fun buttonSearchClick() {
        //カルーセル設定
        val result = setCarousel(CarouselStatus.ElseState)

        //ドロワーを閉じる
        if (result) drawerLayoutMain?.closeDrawer(Gravity.RIGHT)
    }

    /**
     * 閉じるボタン押下時
     */
    private fun buttonCloseClick() {
        //ドロワーを閉じる
        drawerLayoutMain?.closeDrawer(Gravity.RIGHT)
    }
    //endregion

    //region イベント
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
    }

    override fun onCheckedChanged(v: CompoundButton?, isChecked: Boolean) {
        try {
            v?.isEnabled = false

            when (v?.id) {
                switchSeason?.id -> {
                    if (!isChecked) {
                        //▼春夏の場合

                        //コントロールを赤色にする
                        buttonSearch?.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.colorAccent
                            )
                        )
                        val colorStateList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this,
                                R.color.colorAccent
                            )
                        )
                        checkBoxSort?.buttonTintList = colorStateList
                        radioButtonTargetTops?.buttonTintList = colorStateList
                        radioButtonTargetBottoms?.buttonTintList = colorStateList
                        radioButtonTargetShoes?.buttonTintList = colorStateList

                    } else {
                        //▼秋冬の場合

                        //コントロールを青色にする
                        buttonSearch?.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.smokeblue
                            )
                        )
                        val colorStateList =
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.smokeblue))
                        checkBoxSort?.buttonTintList = colorStateList
                        radioButtonTargetTops?.buttonTintList = colorStateList
                        radioButtonTargetBottoms?.buttonTintList = colorStateList
                        radioButtonTargetShoes?.buttonTintList = colorStateList
                    }
                }
            }
        } finally {
            v?.isEnabled = true
        }
    }
    //endregion

    //region 天気取得処理
    /**
     * OpenWeatherから天気を取得
     */
    fun getWeather(apiUrl: String): String? {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                val response = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                return response.toString()
            } else {
                return null
            }
        } catch (e: Exception) {
            println(e.message)
            return null
        }
    }

    /**
     * JSONから変換
     */
    private fun parseWeather(response: String): WeatherInfo {
        val jsonObject = JSONObject(response)

        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        //天気の名称を取得
//        val weather = Utility.getWeatherName(weatherObject.getString("main"), this)
        val weather = weatherObject.getString("main")

        val mainObject = jsonObject.getJSONObject("main")
        //気温は変換する
        val temperature = Math.floor(mainObject.getDouble("temp") - 273.15).toInt()

        return WeatherInfo(resources.getString(R.string.tokyo), weather, temperature)
    }
    //endregion

    //region その他関数
    /**
     * カルーセル設定
     */
    private fun setCarousel(carouselStatus: CarouselStatus): Boolean {
        val cardRowDataList: MutableList<CardRowData> = mutableListOf()

        if (carouselStatus == CarouselStatus.Initial) {
            //▼初期状態
            val cardRowData = CardRowData()
            cardRowData.backgroundColor = getColor(R.color.white)
            cardRowDataList.add(cardRowData)

            viewPager2SampleColor?.offscreenPageLimit = 1
        } else {
            var season: Season
            if (!(switchSeason as Switch).isChecked) {
                season = Season.SS
            } else {
                season = Season.AW
            }

            //どのラジオボタンがチェックされているか判定
            var item: Item = Item.TOPS
            var otherRGBList: MutableList<RGB> = mutableListOf()
            when (radioGroupTargetItem?.checkedRadioButtonId) {
                radioButtonTargetTops?.id -> {
                    item = Item.TOPS

                    //色を選択していない場合はエラーメッセージを表示して終了
                    if (imageViewBottomsColor?.drawable == null || imageViewShoesColor?.drawable == null) {
                        displayMessage(
                            resources.getString(R.string.error_selectedcolor),
                            MsgLevel.ERROR
                        )
                        return false
                    }

                    //他のアイテムでRGBリスト作成
                    val bitmapBottoms = (imageViewBottomsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapBottoms, 0, 0))
                    val bitmapShoes = (imageViewShoesColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapShoes, 0, 0))
                }

                radioButtonTargetBottoms?.id -> {
                    item = Item.BOTTOMS

                    //色を選択していない場合はエラーメッセージを表示して終了
                    if (imageViewTopsColor?.drawable == null || imageViewShoesColor?.drawable == null) {
                        displayMessage(
                            resources.getString(R.string.error_selectedcolor),
                            MsgLevel.ERROR
                        )
                        return false
                    }

                    val bitmapTops = (imageViewTopsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapTops, 0, 0))
                    val bitmapShoes = (imageViewShoesColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapShoes, 0, 0))
                }

                radioButtonTargetShoes?.id -> {
                    item = Item.SHOES

                    if (imageViewTopsColor?.drawable == null || imageViewBottomsColor?.drawable == null) {
                        displayMessage(
                            resources.getString(R.string.error_selectedcolor),
                            MsgLevel.ERROR
                        )
                        return false
                    }

                    val bitmapTops = (imageViewTopsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapTops, 0, 0))
                    val bitmapBottoms = (imageViewBottomsColor?.drawable as BitmapDrawable).bitmap
                    otherRGBList.add(Utility.getRGBFromBitmap(bitmapBottoms, 0, 0))
                }
            }
            //最適な色を取得する
            val colorSelect = ColorSelect()
            val colorList: MutableList<Int> = colorSelect.getColorList(
                season,
                item,
                otherRGBList,
                (checkBoxSort as CheckBox).isChecked
            )

            //取得した色をカードビューで表示
            for (i in 0..colorList.count() - 1) {
                val cardRowData = CardRowData()

                cardRowData.backgroundColor = colorList[i]
                cardRowDataList.add(cardRowData)
            }

            viewPager2SampleColor?.offscreenPageLimit = cardRowDataList.count() - 1
        }

        //提案した色をカードビューに表示
        viewPager2SampleColor?.adapter = CardSlideAdapter(cardRowDataList, this, carouselStatus)

        //メッセージを初期化しておく
        displayMessage(null, MsgLevel.INFORMATION)
        return true
    }

    /**
     * カラーピッカーダイアログ表示
     */
    private fun showColorPickerDialog(targetImageView: ImageView?) {
        val dialog = CustomDialog()
        val manager: FragmentManager = supportFragmentManager

        dialog.setDialogResultListener(this)
        dialog.setTargetImageView(targetImageView)

        var season: Season
        if (!(switchSeason as Switch).isChecked) {
            season = Season.SS
        } else {
            season = Season.AW
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
    private fun displayMessage(message: String?, msgLevel: MsgLevel): Unit {
        textViewMessage?.text = message

        var color: Int
        //メッセージレベルを設定
        if (msgLevel == MsgLevel.INFORMATION) {
            color = R.color.black
        } else {
            color = R.color.red
        }
        textViewMessage?.setTextColor(ContextCompat.getColor(this, color))
    }

    /**
     * 現在の位置情報を取得する
     */
    suspend fun requestLocation(): LocationInfo = withContext(Dispatchers.Default) {
        try {
            suspendCancellableCoroutine { continuation ->
                // 権限チェック済み
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            // 現在の位置情報を取得できた場合の処理
                            continuation.resume(LocationInfo(it.latitude, it.longitude))
                        } ?: run {
                            // 現在の位置情報を取得できなかった場合の処理
                            continuation.resume(LocationInfo(TOKYO_LATITUDE, TOKYO_LONGITUDE))
                        }
                    }
                    .addOnFailureListener { exception ->
                        // エラーが発生した場合の処理
                        continuation.resumeWithException(exception)
                    }
            }
        } catch (e: Exception) {
            LocationInfo(TOKYO_LATITUDE, TOKYO_LONGITUDE)
        }
    }

    /**
     * 現在位置から天気を取得する
     */
    private fun getWeatherCurrentLocation(latitude: Double, longitude: Double) {
        val apiUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$OPENWEATHER_API_KEY"

        //非同期で天気を取得
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = getWeather(apiUrl)
                if (response != null) {
                    val weatherInfo = parseWeather(response)

                    //Google Maps APIを利用して座標から都道府県名を取得
                    var cityName = Utility.getCityName(latitude, longitude, this@MainActivity)

                    //UIへの変更はメインスレッドで行う
                    runOnUiThread {
                        textViewCity?.text = cityName
                        textViewWeather?.text = weatherInfo.weather
                        textViewTemperature?.text = weatherInfo.temperature.toString() + "°C"
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
    //endregion

    //region CustomDialog関連
    override fun onDialogResult(targetImageView: ImageView?, rgb: RGB) {
        //ダイアログの終了を検知
        //選択した色を反映
        targetImageView?.setImageBitmap(
            Utility.createBitmap(
                targetImageView,
                rgb.red,
                rgb.green,
                rgb.blue
            )
        )
    }
    //endregion

    //region onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Utility.CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //カメラの権限を許可した場合
                } else {
                    //カメラの権限を拒否した場合
                }
            }

            Utility.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //位置情報の権限を許可した場合
                    lifecycleScope.launch {
                        //現在の位置情報を取得する
                        val locationInfo = requestLocation()
                        //現在位置から天気を取得する
                        getWeatherCurrentLocation(locationInfo.latitude, locationInfo.longitude)
                    }
                } else {
                    //位置情報の権限を拒否した場合
                }
            }
        }
    }
    //endregion
}
