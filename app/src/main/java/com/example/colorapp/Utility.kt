package com.example.colorapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView

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
 * 共通
 */
class Utility {


    //region 静的メンバー
    companion object {
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
    }
    //endregion
}

/**
 * RGBクラス
 */
data class RGB(val red: Int, val green: Int, val blue: Int)