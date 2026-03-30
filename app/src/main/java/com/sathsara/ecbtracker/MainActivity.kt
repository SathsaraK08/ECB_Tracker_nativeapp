package com.sathsara.ecbtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.notifications.NotificationHelper
import com.sathsara.ecbtracker.ui.navigation.EcbNavHost
import com.sathsara.ecbtracker.ui.theme.EcbTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannel(this)

        setContent {
            val isDarkMode by dataStoreManager.isDarkMode.collectAsState(initial = true)

            EcbTrackerTheme(isDarkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EcbNavHost()
                }
            }
        }
    }
}
