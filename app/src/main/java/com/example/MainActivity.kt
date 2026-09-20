package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MarklifyTheme
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.LocaleHelper
import com.example.util.findActivityResultRegistryOwner

class MainActivity : ComponentActivity() {

    private val viewModel: MarklifyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.appSettings.collectAsState()
            val localizedContext = remember(settings.appLanguage) {
                LocaleHelper.applyLocaleContext(this, settings.appLanguage)
            }
            val activityResultRegistryOwner = remember(localizedContext) {
                localizedContext.findActivityResultRegistryOwner() ?: this
            }

            val isDarkTheme = when (settings.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalOnBackPressedDispatcherOwner provides this,
            ) {
                MarklifyTheme(darkTheme = isDarkTheme) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
