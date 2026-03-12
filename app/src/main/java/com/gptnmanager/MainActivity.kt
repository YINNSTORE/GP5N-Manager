package com.gptnmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.gptnmanager.ui.GPTNApp
import com.gptnmanager.ui.theme.GPTNManagerTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPTNManagerTheme(darkTheme = viewModel.isDarkMode) {
                GPTNApp(viewModel = viewModel)
            }
        }
    }
}