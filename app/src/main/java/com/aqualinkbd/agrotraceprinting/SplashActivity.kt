package com.aqualinkbd.agrotraceprinting

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.charityright.bd.Utils.CustomSharedPref
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        CustomSharedPref.init(this)

        lifecycleScope.launch {
            delay(3000)

            val token = CustomSharedPref.read("Token","")

            if (token?.isEmpty() == true){
                startActivity(
                    Intent(
                        this@SplashActivity,
                        LoginActivity::class.java
                    )
                )
                finish()
            }else{
                startActivity(
                    Intent(
                        this@SplashActivity,
                        MainActivity::class.java
                    )
                )
                finish()
            }

        }

    }
}