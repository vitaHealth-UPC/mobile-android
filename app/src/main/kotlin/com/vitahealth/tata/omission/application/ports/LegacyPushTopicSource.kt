package com.vitahealth.tata.omission.application.ports

/** Topics older app versions may have subscribed to; retained until SDK retirement succeeds. */
fun interface LegacyPushTopicSource {
    fun topics(): Set<String>
}
