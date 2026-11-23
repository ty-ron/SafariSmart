package com.safarismart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.safarismart.app.ui.navigation.AppNavigation
import com.safarismart.app.ui.theme.SafariSmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SafariSmartTheme { AppNavigation() } }
    }
}