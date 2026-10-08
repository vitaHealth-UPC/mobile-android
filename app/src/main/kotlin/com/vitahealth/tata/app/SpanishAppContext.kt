package com.vitahealth.tata.app

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList

/** The production prototype is Spanish; English resources remain available for the EN preview. */
internal fun Context.withSpanishAppLocale(): Context {
    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList.forLanguageTags("es-419"))
    return createConfigurationContext(configuration)
}
