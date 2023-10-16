package com.example.colorapp

import android.graphics.Color
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 最適な色を抽出するクラス
 */
class ColorSelect {
    //region 定数・変数
    /**
     * 閾値(黒色)
     */
    private val blackThreshold : Int = 70
    /**
     * 閾値(白色)
     */
    private val whiteThreshold : Int = 235
    /**
     * 閾値(モノトーン)
     */
    private val monotoneThreshold : Int = 10
    /**
     * 閾値(トーン)：255, 255,255の合計の半分の値
     */
    private val toneThreshold: Int = 383
    /**
     * 提案する色の最大数
     */
    private val suggestMaxCount : Int = 30
    /**
     * 提案する色の最小数
     */
    private val suggestMinCount : Int = 10
    //endregion

    //region カラーリスト
    /**
     * 赤系
     */
    private val redColor : Array<String> =
        arrayOf(
            "#FF0000",
            "#800000",
            "#8B0000",
            "#A52A2A",
            "#B22222",
            "#CD5C5C",
            "#DC143C",
            "#B1063A",
            "#F03232",
            "#F00A23",
            "#FF3C3C"
        )
    /**
     * 緑系
     */
    private val greenColor : Array<String> =
        arrayOf(
            "#32CD32",
            "#006400",
            "#98FB98",
            "#90EE90",
            "#00FF7F",
            "#ADFF2F",
            "#BEC800",
            "#918D40",
            "#00AA00",
            "#007800",
            "#228B22",
            "#2E8B57",
            "#3CB371",
            "#556B2F",
            "#6B8E23",
            "#808000"
        )
    /**
     * 青系
     */
    private val blueColor : Array<String> =
        arrayOf(
            "#708090",
            "#165E83",
            "#233B6C",
            "#778899",
            "#F0F8FF",
            "#4682B4",
            "#B0C4DE",
            "#5F9EA0",
            "#40E0D0",
            "#48D1CC",
            "#0000FF",
            "#191970",
            "#000080",
            "#0000CD",
            "#00008B",
            "#4169E1",
            "#6495ED",
            "#7B68EE",
            "#ADD8E6",
            "#1E90FF",
            "#E1FFFF",
            "#E1F0FF",
            "#001932",
            "#140032",
            "#000069",
            "#0050FF",
            "#0096FF",
            "#0037FF",
            "#E0FFFF"
        )
    /**
     * ピンク系
     */
    private val pinkColor : Array<String> =
        arrayOf(
            "#FF69B4",
            "#FFB6C1",
            "#F08080",
            "#FA8072",
            "#FF78A5",
            "#FF32A5",
            "#FF96A5",
            "#FAEBD7"
        )
    /**
     * 黄色系
     */
    private val yellowColor : Array<String> =
        arrayOf(
            "#F8B400",
            "#FFD700",
            "#FFFF46",
            "#FFFF78",
            "#FFFF1E",
            "#FFFF0A",
            "#FFFF96"
        )
    /**
     * オレンジ系
     */
    private val orangeColor : Array<String> =
        arrayOf(
            "#FFA500",
            "#FFA564",
            "#FF3700",
            "#FF6400",
            "#FFA000",
            "#FFAA50"
        )
    /**
     * 紫系
     */
    private val purpleColor : Array<String> =
        arrayOf(
            "#BC8F8F",
            "#B3424A",
            "#B13546",
            "#745399",
            "#6A4C9C",
            "#411445",
            "#9932CC",
            "#DDA0DD",
            "#D8BFD8",
            "#6A5ACD",
            "#4B0082",
            "#483D8B",
            "#DA70D6",
            "#EE82EE",
            "#DB7093",
            "#C896A5"
        )
    /**
     * ベージュ系
     */
    private val beigeColor : Array<String> =
        arrayOf(
            "#DEB887",
            "#F5DEB3",
            "#FAFAD2",
            "#EEE8AA",
            "#FFE4C4",
            "#BDB76B"
        )
    /**
     * ブラウン系
     */
    private val brownColor : Array<String> =
        arrayOf(
            "#CD853F",
            "#8B4513",
            "#A0522D",
            "#D2B48C"
        )
    /**
     * 白系
     */
    private val whiteColor : Array<String> =
        arrayOf(
            "#FFFFFF",
            "#FFFFDC",
            "#F5F5FF",
            "#FFFFCD"
        )
    /**
     * 黒系
     */
    private val blackColor : Array<String> =
        arrayOf(
            "#000000",
            "#323232",
            "#1E191E",
            "#002300",
            "#1E1E00",
            "#190000"
        )
    /**
     * グレー系
     */
    private val grayColor : Array<String> =
        arrayOf(
            "#464646",
            "#E6E6E6",
            "#414141",
            "#464646",
            "#464646",
            "#F0F0F0"
        )
    //endregion

    //region 列挙型
    /**
     * 色み(色相)
     */
    private enum class hue{
        //赤
        RED,
        //緑
        GREEN,
        //赤
        BLUE,
        //黄
        YELLOW,
        //シアン
        CYAN,
        //マゼンタ
        MAGENTA
    }
    //endregion

    //region その他関数
    /**
     * 最適な色のリストを返却
     */
    fun getColorList(season:Utility.season,item : Utility.item, otherRGBList:MutableList<Triple<Int, Int, Int>>,sortFlg:Boolean): MutableList<Int> {

        val colorList: MutableList<Int> = mutableListOf()
        try {
            val isMonotoneColor1 = checkMonotone(otherRGBList[0])
            val isMonotoneColor2 = checkMonotone(otherRGBList[0])
            val isBlackColor1 = checkBlackColor(otherRGBList[0])
            val isBlackColor2 = checkBlackColor(otherRGBList[1])
            val isWhiteColor1 = checkWhiteColor(otherRGBList[0])
            val isWhiteColor2 = checkWhiteColor(otherRGBList[1])

            var suggestColorList : MutableList<String> = mutableListOf()
            val resultColorList : MutableList<String> = mutableListOf()

            var colorCount = Random(System.currentTimeMillis())
            val min = 1
            var max  :Int? = null
            var suggestCount :Int? = null
            var uniqueRandomNumbers = mutableSetOf<Int>()

            var suggestColorListList : MutableList<Array<String>> = mutableListOf()

            if(((isBlackColor1 && isBlackColor2) || (isWhiteColor1 && isWhiteColor2)) ||
                ((isBlackColor1 && isWhiteColor2) || (isWhiteColor1 && isBlackColor2)))
            {
                //▼両方黒色、または両方白色、または両方黒色か白色の場合
                //すべての色を使用
                suggestColorListList.add(redColor)
                suggestColorListList.add(blueColor)
                suggestColorListList.add(pinkColor)
                suggestColorListList.add(yellowColor)
                suggestColorListList.add(orangeColor)
                suggestColorListList.add(purpleColor)
                suggestColorListList.add(beigeColor)
                suggestColorListList.add(brownColor)
                suggestColorListList.add(blackColor)
                suggestColorListList.add(whiteColor)
                suggestColorListList.add(grayColor)
            }
            else if ((isMonotoneColor1 && isMonotoneColor2) ||
                ((isBlackColor1 && isMonotoneColor2) || (isWhiteColor1 && isMonotoneColor2) ||
                        (isBlackColor2 && isMonotoneColor1) || (isWhiteColor2 && isMonotoneColor1)))
            {
                //両方モノトーン、片方が黒色か白色でもう片方がモノトーンの場合
                suggestColorListList.add(redColor)
                suggestColorListList.add(blueColor)
                suggestColorListList.add(pinkColor)
                suggestColorListList.add(yellowColor)
                suggestColorListList.add(orangeColor)
                suggestColorListList.add(purpleColor)
                suggestColorListList.add(beigeColor)
                suggestColorListList.add(brownColor)
                suggestColorListList.add(blackColor)
                suggestColorListList.add(whiteColor)
                suggestColorListList.add(grayColor)
            }
            else
            {
                //その他(色が限定されるパターン)

                //それぞれの色みを取得
                val hue1 = getHue(Utility.getColorCodeFromRGB(otherRGBList[0]))
                val hue2 = getHue(Utility.getColorCodeFromRGB(otherRGBList[1]))

                val isHighTone1 = isHighToneColor(Utility.getColorCodeFromRGB(otherRGBList[0]))
                val isHighTone2 = isHighToneColor(Utility.getColorCodeFromRGB(otherRGBList[1]))

                if(hue1 == hue2)
                {
                    val otherColorList: MutableList<String> = mutableListOf()

                    when(hue1) {
                        hue.RED-> {
                            //選択中の色
                            suggestColorListList.add(redColor)
                            suggestColorListList.add(orangeColor)

                            //選択した色以外
                            otherColorList.add(getOneColorCode(greenColor))
                            otherColorList.add(getOneColorCode(blueColor))
                            otherColorList.add(getOneColorCode(yellowColor))
                            otherColorList.add(getOneColorCode(pinkColor))
                            otherColorList.add(getOneColorCode(purpleColor))
                            otherColorList.add(getOneColorCode(beigeColor))
                            otherColorList.add(getOneColorCode(brownColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                        hue.GREEN-> {
                            suggestColorListList.add(greenColor)

                            otherColorList.add(getOneColorCode(redColor))
                            otherColorList.add(getOneColorCode(orangeColor))
                            otherColorList.add(getOneColorCode(blueColor))
                            otherColorList.add(getOneColorCode(yellowColor))
                            otherColorList.add(getOneColorCode(pinkColor))
                            otherColorList.add(getOneColorCode(purpleColor))
                            otherColorList.add(getOneColorCode(beigeColor))
                            otherColorList.add(getOneColorCode(brownColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                        hue.BLUE-> {
                            suggestColorListList.add(blueColor)

                            otherColorList.add(getOneColorCode(redColor))
                            otherColorList.add(getOneColorCode(orangeColor))
                            otherColorList.add(getOneColorCode(greenColor))
                            otherColorList.add(getOneColorCode(yellowColor))
                            otherColorList.add(getOneColorCode(pinkColor))
                            otherColorList.add(getOneColorCode(purpleColor))
                            otherColorList.add(getOneColorCode(beigeColor))
                            otherColorList.add(getOneColorCode(brownColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                        hue.YELLOW-> {
                            suggestColorListList.add(yellowColor)
                            suggestColorListList.add(beigeColor)
                            suggestColorListList.add(brownColor)

                            otherColorList.add(getOneColorCode(redColor))
                            otherColorList.add(getOneColorCode(orangeColor))
                            otherColorList.add(getOneColorCode(greenColor))
                            otherColorList.add(getOneColorCode(blueColor))
                            otherColorList.add(getOneColorCode(pinkColor))
                            otherColorList.add(getOneColorCode(purpleColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                        hue.CYAN-> {
                            suggestColorListList.add(blueColor)
                            suggestColorListList.add(greenColor)

                            otherColorList.add(getOneColorCode(redColor))
                            otherColorList.add(getOneColorCode(orangeColor))
                            otherColorList.add(getOneColorCode(yellowColor))
                            otherColorList.add(getOneColorCode(pinkColor))
                            otherColorList.add(getOneColorCode(purpleColor))
                            otherColorList.add(getOneColorCode(beigeColor))
                            otherColorList.add(getOneColorCode(brownColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                        hue.MAGENTA-> {
                            suggestColorListList.add(pinkColor)
                            suggestColorListList.add(purpleColor)

                            otherColorList.add(getOneColorCode(redColor))
                            otherColorList.add(getOneColorCode(orangeColor))
                            otherColorList.add(getOneColorCode(greenColor))
                            otherColorList.add(getOneColorCode(blueColor))
                            otherColorList.add(getOneColorCode(yellowColor))
                            otherColorList.add(getOneColorCode(beigeColor))
                            otherColorList.add(getOneColorCode(brownColor))

                            suggestColorListList.add(otherColorList.toTypedArray())
                        }
                    }

                    //モノトーン追加
                    suggestColorListList.add(blackColor)
                    suggestColorListList.add(whiteColor)
                    suggestColorListList.add(grayColor)
                }
                else {
                    when(hue1) {
                        hue.RED-> {
                            suggestColorListList.add(redColor)
                            suggestColorListList.add(orangeColor)
                        }
                        hue.GREEN-> {
                            suggestColorListList.add(greenColor)
                        }
                        hue.BLUE-> {
                            suggestColorListList.add(blueColor)
                        }
                        hue.YELLOW-> {
                            suggestColorListList.add(yellowColor)
                            suggestColorListList.add(beigeColor)
                            suggestColorListList.add(brownColor)
                        }
                        hue.CYAN-> {
                            suggestColorListList.add(blueColor)
                            suggestColorListList.add(greenColor)
                        }
                        hue.MAGENTA-> {
                            suggestColorListList.add(pinkColor)
                            suggestColorListList.add(purpleColor)
                        }
                    }

                    when(hue2) {
                        hue.RED-> {
                            suggestColorListList.add(redColor)
                            suggestColorListList.add(orangeColor)
                        }
                        hue.GREEN-> {
                            suggestColorListList.add(greenColor)
                        }
                        hue.BLUE-> {
                            suggestColorListList.add(blueColor)
                        }
                        hue.YELLOW-> {
                            suggestColorListList.add(yellowColor)
                            suggestColorListList.add(beigeColor)
                            suggestColorListList.add(brownColor)
                        }
                        hue.CYAN-> {
                            suggestColorListList.add(blueColor)
                            suggestColorListList.add(greenColor)
                        }
                        hue.MAGENTA-> {
                            suggestColorListList.add(pinkColor)
                            suggestColorListList.add(purpleColor)
                        }
                    }

                    //モノトーン追加
                    if (isHighTone1 && isHighTone2) {
                        val otherColorList: MutableList<String> = mutableListOf()

                        otherColorList.add(getOneColorCode(blackColor))
                        otherColorList.add(getOneColorCode(whiteColor))
                        otherColorList.add(getOneColorCode(grayColor))
                        suggestColorListList.add(otherColorList.toTypedArray())
                    }
                    else
                    {
                        suggestColorListList.add(blackColor)
                        suggestColorListList.add(whiteColor)
                        suggestColorListList.add(grayColor)
                    }
                }
            }

            if(!sortFlg)
            {
                //ソートなしであれば続けて追加
                for (listItem in suggestColorListList) {
                    createSuggestColorList(suggestColorList,listItem)
                }

                //色の提案リストの要素数
                max = suggestColorList.count()
                //色の提案リストの要素数が提案する色の最大数より小さければ、色の提案リストの要素数を最大数とする
                val suggestMax = if (max < suggestMaxCount) max else suggestMaxCount

                //提案する色の数をランダムで決定
                suggestCount = colorCount.nextInt(suggestMinCount, suggestMax + 1)

                //ランダムで選定(重複なし)
                while (uniqueRandomNumbers.size < suggestCount) {
                    val randomNumber = colorCount.nextInt(min - 1, max )
                    uniqueRandomNumbers.add(randomNumber)
                }

                //最終的なカラーリストへ格納
                for (num in uniqueRandomNumbers) {
                    resultColorList.add(suggestColorList[num])
                }
            }
            else
            {
                // 色のリストの順番をランダムに入れ替える
                suggestColorListList.shuffle()

                //ソートありであれば、各色のリストを並べて追加
                for (listItem in suggestColorListList) {

                    //初期化する
                    suggestColorList = mutableListOf()
                    colorCount = Random(System.currentTimeMillis())
                    uniqueRandomNumbers = mutableSetOf<Int>()

                    createSuggestColorList(suggestColorList, listItem)

                    //色の提案リストの要素数
                    max = suggestColorList.count()
                    //色の提案リストの要素数が提案する色の最大閾値より小さければ、色の提案リストの要素数を最大数とする
                    val suggestMax = if (max < suggestMaxCount) max else suggestMaxCount

                    //提案する色の数をランダムで決定
                    suggestCount = colorCount.nextInt(min, suggestMax + 1)

                    //ランダムで選定(重複なし)
                    while (uniqueRandomNumbers.size < suggestCount) {
                        val randomNumber = colorCount.nextInt(min - 1, max )
                        uniqueRandomNumbers.add(randomNumber)
                    }

                    //最終的なカラーリストへ格納
                    for (num in uniqueRandomNumbers) {
                        resultColorList.add(suggestColorList[num])
                    }
                }

                //提案する色の最大閾値より多い場合は余分な要素を削除
                if(resultColorList.count() > suggestMaxCount)
                {
                    val removeCount = resultColorList.count() - suggestMaxCount
                    for (i in 0 until removeCount) {
                        resultColorList.removeAt(Random.nextInt(resultColorList.size))
                    }
                }
            }

            //最終的なカラーリストをRGBへ変換
            for(i in resultColorList.orEmpty()){
                colorList.add(Color.parseColor(i))
            }
        }
        catch(e: Exception)
        {
            //例外エラー発生時は白
            println(e)
            colorList.add(Color.parseColor(whiteColor[0]))
        }

        return colorList
    }

    /**
     * 2つの色の距離を取得
     */
    private fun getColorDistance(color1: Triple<Int, Int, Int>, color2: Triple<Int, Int, Int>): Double {
        val redDiff = color1.component1() - color2.component1()
        val greenDiff = color1.component2() - color2.component2()
        val blueDiff = color1.component3() - color2.component3()

        //ユークリッド距離を計算
        return sqrt((redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff).toDouble())
    }

    /**
     * 提案する色のリスト作成
     */
    private fun createSuggestColorList(suggestColorList: MutableList<String>,targetColorList: Array<String>): Unit {
        for (value in targetColorList) {
            suggestColorList.add(value)
        }
    }

    /**
     * 色みを返却
     */
    private fun getHue(colorCode: String): hue {
        // カラーコードをRGB成分に分解して値へ変換
        val red = colorCode.substring(1, 3)
        val green = colorCode.substring(3, 5)
        val blue = colorCode.substring(5)

        val redValue = red.toInt(16)
        val greenValue = green.toInt(16)
        val blueValue = blue.toInt(16)

        val colorValueRank = listOf(redValue, greenValue, blueValue).sortedDescending()
        //同率1位フラグ
        val amountsFirstFlg = if (colorValueRank[0] == colorValueRank[1]) true else false
        //色みランク
        var colorRank: MutableList<hue> = mutableListOf()

        for (color in colorValueRank) {
            if (color == redValue && !colorRank.contains(hue.RED)) {
                colorRank.add(hue.RED)
            } else if (color == greenValue && !colorRank.contains(hue.GREEN)) {
                colorRank.add(hue.GREEN)
            } else if (color == blueValue && !colorRank.contains(hue.BLUE)) {
                colorRank.add(hue.BLUE)
            }
        }

        if (!amountsFirstFlg) {
            //同率1位ではない場合はランク1位を返却
            return colorRank[0]
        }
        else
        {
            if ((colorRank[0] == hue.RED && colorRank[1] == hue.GREEN) ||
                (colorRank[0] == hue.GREEN && colorRank[1] == hue.RED)
            ) {
                //赤と緑が同率1位であれば黄色を返却
                return hue.YELLOW
            } else if ((colorRank[0] == hue.GREEN && colorRank[1] == hue.BLUE) ||
                (colorRank[0] == hue.BLUE && colorRank[1] == hue.GREEN)
            ) {
                //緑と青が同率1位であればシアンを返却
                return hue.CYAN
            } else if ((colorRank[0] == hue.BLUE && colorRank[1] == hue.RED) ||
                (colorRank[0] == hue.RED && colorRank[1] == hue.BLUE)
            ) {
                //青と赤が同率1位であればマゼンタを返却
                return hue.MAGENTA
            }
        }

        return hue.RED
    }

    /**
     * カラーコードをランダムで1つ返却
     */
    private fun getOneColorCode(colorCodeList: Array<String>): String {

        var colorCount = Random(System.currentTimeMillis())
        val randomNumber = colorCount.nextInt(0, colorCodeList.count() - 1)
        //カラーリストからランダムで1つ返す
        return colorCodeList[randomNumber]
    }
    //endregion

    //region チェック処理
    /**
     * 黒色チェック
     */
    private fun checkBlackColor(color: Triple<Int, Int, Int>): Boolean {
        //RGBが235以上、かつモノトーンであれば黒色
        if(!checkMonotone(color)) return false
        if(color.component1() > blackThreshold) return false
        if(color.component2() > blackThreshold) return false
        if(color.component3() > blackThreshold) return false
        return true
    }

    /**
     * 白色チェック
     */
    private fun checkWhiteColor(color: Triple<Int, Int, Int>): Boolean {
        //RGBが235以上、かつモノトーンであれば白色
        if(!checkMonotone(color)) return false
        if(color.component1() < whiteThreshold) return false
        if(color.component2() < whiteThreshold) return false
        if(color.component3() < whiteThreshold) return false
        return true
    }

    /**
     * モノトーンチェック
     */
    private fun checkMonotone(color: Triple<Int, Int, Int>):Boolean {
        //RGBの差が10未満であればモノトーン
        if(Math.abs( color.component1() - color.component2()) > monotoneThreshold) return false
        if(Math.abs( color.component2() - color.component3()) > monotoneThreshold) return false
        if(Math.abs( color.component3() - color.component1()) > monotoneThreshold) return false
        return true
    }

    /**
     * トーンチェック
     */
    private fun isHighToneColor(colorCode: String): Boolean {
        // カラーコードをRGB成分に分解して値へ変換
        val red = colorCode.substring(1, 3)
        val green = colorCode.substring(3, 5)
        val blue = colorCode.substring(5)

        val redValue = red.toInt(16)
        val greenValue = green.toInt(16)
        val blueValue = blue.toInt(16)

        // RGB成分の合計を計算
        val totalValue = redValue + greenValue + blueValue

        // 合計が閾値以上であればハイトーンと判断
        return totalValue >= toneThreshold
    }
    //endregion
}