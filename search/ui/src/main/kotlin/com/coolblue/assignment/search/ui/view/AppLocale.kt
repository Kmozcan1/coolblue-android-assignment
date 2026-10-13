package com.coolblue.assignment.search.ui.view

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.formatCount
import java.util.Locale

@Composable
internal fun appLocale(): Locale {
    val appLanguage = stringResource(R.string.language_tag)
    val phoneLocale = LocalConfiguration.current.locales[0]
    val isPhoneInAppLanguage = phoneLocale.language == appLanguage
    return if (isPhoneInAppLanguage) phoneLocale else Locale.forLanguageTag(appLanguage)
}

// Not pluralStringResource: it picks the plural form by the phone's language, so a French phone reads "0 review".
@Composable
internal fun appPluralString(@PluralsRes id: Int, count: Int): String {
    val locale = appLocale()
    return rememberResourcesIn(locale).getQuantityString(id, count, formatCount(count, locale))
}

@SuppressLint("AppBundleLocaleChanges") // the app module keeps every language in the base APK
@Composable
private fun rememberResourcesIn(locale: Locale): Resources {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration, locale) {
        context.createConfigurationContext(Configuration(configuration).apply { setLocale(locale) }).resources
    }
}
