package com.example.colorapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.colorapp.Config.getGoogleMapsApiKey
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.util.Properties

/**
 * 他のクラスから呼び出し可能な関数群
 */

/**
 * 季節
 */
enum class Season {
    //春夏
    SS,

    //秋冬
    AW
}

/**
 * アイテム
 */
enum class Item {
    //トップス
    TOPS,

    //ボトムス
    BOTTOMS,

    //シューズ
    SHOES
}

/**
 * 季節
 */
enum class MsgLevel {
    //インフォメーション
    INFORMATION,

    //エラー
    ERROR
}

/**
 * カードスライダービューの状態
 */
enum class CarouselStatus {
    //初期状態
    Initial,

    //それ以外
    ElseState
}

/**
 * 符号
 */
enum class Sign {
    //プラス
    Plus,

    //マイナス
    Minus
}

/**
 * 季節
 */
enum class OpenWeatherMainField {
    //晴れ
    Clear,

    //曇り
    Clouds,

    //雨
    Rain,

    //小雨
    Drizzle,

    //雷雨
    Thunderstorm,

    //雪
    Snow,

    //その他
    Other
}

/**
 * コンフィグクラス
 */
object Config {
    private val properties = Properties()

    init {
        val inputStream = Config::class.java.classLoader.getResourceAsStream("config.properties")
        properties.load(inputStream)
    }

    /**
     * OpenWeatherのAPIキーを取得
     */
    fun getOpenWeatherApiKey(): String {
        return properties.getProperty("openweather.api.key")
    }

    /**
     * GoogleMapsのAPIキーを取得
     */
    fun getGoogleMapsApiKey(): String {
        return properties.getProperty("googlemaps.api.key")
    }
}

/**
 * 共通
 */
class Utility {

    //region 静的メンバー
    companion object {

        //region 定数・変数
        val CAMERA_PERMISSION_REQUEST = 1001
        val LOCATION_PERMISSION_REQUEST_CODE = 1002
        //endregion

        fun createBitmap(imageView: ImageView?, red: Int, green: Int, blue: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(
                imageView?.width ?: 0,
                imageView?.height ?: 0,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            val color = Color.rgb(red, green, blue)
            canvas.drawColor(color)

            return bitmap
        }

        /**
         * 天気の名称を取得
         */
        fun getWeatherName(weather: String, context: Activity): String {
            var result = context.resources.getString(R.string.other)

            when (weather) {
                OpenWeatherMainField.Clear.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_clear)
                }

                OpenWeatherMainField.Clouds.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_clouds)
                }

                OpenWeatherMainField.Rain.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_rain)
                }

                OpenWeatherMainField.Drizzle.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_drizzle)
                }

                OpenWeatherMainField.Thunderstorm.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_thunderstorm)
                }

                OpenWeatherMainField.Snow.name -> {
                    //画面押下時
                    result = context.resources.getString(R.string.weather_snow)
                }
            }
            return result
        }

        /**
         * アニメーションを作成
         */
        fun createAnimation(fromXDelta: Float): Animation {
            var animation: Animation
            animation = TranslateAnimation(fromXDelta, 0f, 0f, 0f)
            animation.duration = 800
            animation.repeatCount = Animation.INFINITE
            animation.repeatMode = Animation.RESTART

            return animation
        }

        /**
         * ビットマップからRGBを取得
         */
        fun getRGBFromBitmap(bitmap: Bitmap, x: Int, y: Int): RGB {
            val pixel = bitmap.getPixel(x, y)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            return RGB(red, green, blue)
        }

        /**
         * RGBからカラーコードを取得
         */
        fun getColorCodeFromRGB(rgb: RGB): String {
            val color = Color.rgb(rgb.red, rgb.green, rgb.blue)
            return String.format("#%06X", 0xFFFFFF and color)
        }

        /**
         * カラーコードからRGBを取得
         */
        fun getColorCodeToRGB(colorCode: String): RGB {
            val hex = colorCode.removePrefix("#")
            val red = hex.substring(0, 2).toInt(16)
            val green = hex.substring(2, 4).toInt(16)
            val blue = hex.substring(4, 6).toInt(16)

            return RGB(red, green, blue)
        }

        /**
         * ImageViewからRGBを取得
         */
        fun getRGBFromImageView(imageView: ImageView?): RGB {

            val drawable = imageView?.drawable

            if (drawable == null) {
                return RGB(0, 0, 0)
            } else {
                val bitmap = (drawable as BitmapDrawable).bitmap
                val pixel = bitmap.getPixel(0, 0)
                //選択時の色を設定
                return RGB(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            }
        }

        /**
         * トーストを表示する
         */
        fun showToast(message: String, context: Activity) {
            context.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }

        //region 権限関連
        /**
         * カメラの権限を確認する
         */
        fun checkCameraPermission(context: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * カメラの権限をリクエストする
         */
        fun requestCameraPermission(context: Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }

        /**
         * 位置情報の権限を確認する
         */
        fun checkLocationPermission(context: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * 位置情報の権限をリクエストする
         */
        fun requestLocationPermission(context: Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        //endregion

        /**
         * Google Maps APIを利用して座標から都道府県名を取得
         */
        fun getCityName(latitude: Double, longitude: Double, context: Activity): String {
            //APIキーを取得
            val apiKey = getGoogleMapsApiKey()
            val geocodingApiUrl =
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"

            try {
                val response = URL(geocodingApiUrl).readText()
                val jsonObject = JSONObject(response)

                if (jsonObject.getString("status") == "OK") {
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val addressComponents =
                            results.getJSONObject(0).getJSONArray("address_components")
                        for (i in 0 until addressComponents.length()) {
                            val component = addressComponents.getJSONObject(i)
                            val types = component.getJSONArray("types")
                            if (types.toString().contains("administrative_area_level_1")) {
                                // 都道府県名を返却
                                return convertToShiftJIS(component.getString("long_name"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }

            //失敗した場合は「不明」を返却
            return context.resources.getString(R.string.unknown)
        }

        /**
         * UTF-8からShift-JISに変換
         */
        private fun convertToShiftJIS(input: String): String {
            return String(input.toByteArray(Charset.forName("UTF-8")), Charset.forName("Shift-JIS"))
        }
    }
    //endregion
}

/**
 * RGBクラス
 */
data class RGB(val red: Int, val green: Int, val blue: Int)

/**
 * 位置情報クラス
 */
data class LocationInfo(val latitude: Double, val longitude: Double)

/**
 * 天気情報クラス
 */
data class WeatherInfo(val city: String, val weather: String, val temperature: Int)