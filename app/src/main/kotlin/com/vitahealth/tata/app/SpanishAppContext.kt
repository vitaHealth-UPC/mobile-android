package com.vitahealth.tata.app

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList

/** Applies the verified account language before an Activity inflates localized resources. */
internal fun Context.withTataAppLocale(languageTag: String): Context {
    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList.forLanguageTags(languageTag))
    return createConfigurationContext(configuration)
}

/** Default Spanish experience, also used by deterministic visual test fixtures. */
internal fun Context.withSpanishAppLocale() = withTataAppLocale("es-419")
