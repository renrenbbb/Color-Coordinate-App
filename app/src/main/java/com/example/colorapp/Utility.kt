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