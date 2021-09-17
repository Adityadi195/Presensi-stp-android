package com.example.presensi_android_stp.views.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.views.login.LoginActivity
import com.example.presensi_android_stp.views.main.MainActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        afterDelayGoToLogin()
    }

    private fun afterDelayGoToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkIsLogin()
        },1200)
    }

    private fun checkIsLogin() {
        val isLogin = HawkStorage.instance(this).isLogin()
        if (isLogin){
            startActivity<MainActivity>()
            finishAffinity()
        } else {
            startActivity<LoginActivity>()
            finishAffinity()
        }
    }
}