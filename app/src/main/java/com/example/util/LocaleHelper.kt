package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    const val LANGUAGE_EN = "en"
    const val LANGUAGE_UZ = "uz"
    const val LANGUAGE_RU = "ru"

    fun getLocale(languageCode: String): Locale {
        return when (languageCode.lowercase().trim()) {
            LANGUAGE_UZ -> Locale.forLanguageTag("uz")
            LANGUAGE_RU -> Locale.forLanguageTag("ru")
            else -> Locale.forLanguageTag("en")
        }
    }

    fun applyLocaleContext(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return context.createConfigurationContext(config)
    }
}
