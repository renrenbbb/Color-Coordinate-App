package com.example.colorapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * スプラッシュ用アクテビティ
 */
class SplashActivity : AppCompatActivity() {
    //region 定数・変数
    //表示時間
    private val splashTimeOut: Long = 3000
    //endregion

    //region ロード処理
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ナビゲーションバーの透過に必要
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //レイアウトの設定
        setContentView(R.layout.activity_splash)


        // スプラッシュ画面が表示された後にメインアクティビティに遷移する
        MainScope().launch {
            delay(splashTimeOut)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    //endregion
}


