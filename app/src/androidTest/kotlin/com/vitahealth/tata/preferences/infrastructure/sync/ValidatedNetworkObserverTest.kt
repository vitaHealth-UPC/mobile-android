package com.vitahealth.tata.preferences.infrastructure.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Runs the actual Android registration path without changing device connectivity. */
@RunWith(AndroidJUnit4::class)
class ValidatedNetworkObserverTest {
    @Test
    fun initialSnapshotMatchesAndroidAndRepeatedCancellationReleasesCallbacks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val observer = ValidatedNetworkObserver(context)
        // Android limits outstanding network callbacks per UID. More than 100 short collections
        // would exhaust that limit if first() did not unregister each callback on cancellation.
        withTimeout(30_000) {
            repeat(110) {
                val expected =
                    manager
                        .getNetworkCapabilities(manager.activeNetwork)
                        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
                assertEquals(expected, observer.observe().first())
            }
        }
    }
}
