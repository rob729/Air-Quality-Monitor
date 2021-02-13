package com.robin729.aqi.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.parse.ParseObject
import com.parse.ParseQuery
import com.robin729.aqi.utils.PreferenceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main.immediate).launch {
            updateAQIKey()
            Intent(baseContext, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }

    }

    private suspend fun updateAQIKey() = withContext(Dispatchers.Default) {
        val keyFetchTime = preferenceRepository.getKeyFetchTime().first() ?: 0
        val timeDiff =
            TimeUnit.MILLISECONDS.toHours(
                System.currentTimeMillis() - keyFetchTime
            )
        val apiKey = preferenceRepository.getApiKey().first()
        if (timeDiff > 5 || apiKey.isNullOrEmpty()) {
            val parseQuery = ParseQuery.getQuery<ParseObject>("AQI_KEY").get("HoVICsIfuo")
            preferenceRepository.apply {
                setApiKey(parseQuery["key"] as String)
                setKeyFetchTime(System.currentTimeMillis())
            }
        }
    }
}