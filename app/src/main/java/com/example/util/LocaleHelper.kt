package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.activity.result.ActivityResultRegistryOwner
import java.util.Locale

class LocalizedContextWrapper(
    baseContext: Context,
    private val localizedResourcesContext: Context
) : ContextWrapper(baseContext) {
    override fun getResources(): Resources {
        return localizedResourcesContext.resources
    }
}

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

        val configContext = context.createConfigurationContext(config)
        return LocalizedContextWrapper(context, configContext)
    }
}

fun Context.findActivityResultRegistryOwner(): ActivityResultRegistryOwner? {
    var currentContext: Context? = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ActivityResultRegistryOwner) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return currentContext as? ActivityResultRegistryOwner
}
