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
class Utility {

    //region 列挙型
    /**
     * 季節
     */
    enum class season {
        //春夏
        SS,

        //秋冬
        AW
    }

    /**
     * アイテム
     */
    enum class item {
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
    enum class msgLevel {
        //インフォメーション
        INFORMATION,

        //エラー
        ERROR
    }
    //endregion

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
        fun getRGBFromBitmap(bitmap: Bitmap, x: Int, y: Int): Triple<Int, Int, Int> {
            val pixel = bitmap.getPixel(x, y)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            return Triple(red, green, blue)
        }

        /**
         * RGBからカラーコードを取得
         */
        fun getColorCodeFromRGB(rgb: Triple<Int, Int, Int>): String {
            val color =
                android.graphics.Color.rgb(rgb.component1(), rgb.component2(), rgb.component3())
            return String.format("#%06X", 0xFFFFFF and color)
        }

        /**
         * カラーコードからRGBを取得
         */
        fun getColorCodeToRGB(colorCode: String): Triple<Int, Int, Int> {
            val hex = colorCode.removePrefix("#")
            val red = hex.substring(0, 2).toInt(16)
            val green = hex.substring(2, 4).toInt(16)
            val blue = hex.substring(4, 6).toInt(16)

            return Triple(red, green, blue)
        }

        /**
         * ImageViewからRGBを取得
         */
        fun getRGBFromImageView(imageView: ImageView?): Triple<Int, Int, Int> {

            val drawable = imageView?.drawable

            if (drawable == null) {
                return Triple(0, 0, 0)
            } else {
                val bitmap = (drawable as BitmapDrawable).bitmap
                val pixel = bitmap.getPixel(0, 0)
                //選択時の色を設定
                return Triple(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            }
        }
    }
    //endregion
}