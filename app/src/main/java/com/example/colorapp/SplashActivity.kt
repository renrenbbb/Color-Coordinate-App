package com.example.colorapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

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
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashTimeOut)
    }
    //endregion
}
