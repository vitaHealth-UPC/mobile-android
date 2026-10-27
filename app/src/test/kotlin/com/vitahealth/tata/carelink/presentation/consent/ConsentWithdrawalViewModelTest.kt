package com.vitahealth.tata.carelink.presentation.consent

import com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentWithdrawalViewModelTest {
    @Test
    fun cancelDoesNotWriteAndRepeatedConfirmCannotDuplicatePendingWithdrawal() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            var writes = 0
            lateinit var receipt: Continuation<AppResult<Unit>>
            val vm =
                ConsentWithdrawalViewModel(
                    "adult",
                    ConsentWithdrawal {
                        writes++
                        suspendCoroutine { receipt = it }
                    },
                )
            vm.confirm()
            assertEquals(0, writes)
            vm.request()
            vm.cancel()
            vm.confirm()
            assertEquals(0, writes)
            vm.request()
            vm.confirm()
            vm.confirm()
            vm.cancel()
            assertEquals(1, writes)
            assertTrue(vm.state.value.busy)
            receipt.resume(AppResult.Success(Unit))
            assertTrue(vm.state.value.withdrawn)
            vm.request()
            vm.confirm()
            assertEquals(1, writes)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectedRequestKeepsConfirmationAndAllowsExplicitRetry() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val vm =
                ConsentWithdrawalViewModel(
                    "adult",
                    ConsentWithdrawal {
                        AppResult.Failure("Denied", code = "RESOURCE_ACCESS_DENIED")
                    },
                )
            vm.request()
            vm.confirm()
            assertFalse(vm.state.value.withdrawn)
            assertFalse(vm.state.value.busy)
            assertEquals("RESOURCE_ACCESS_DENIED", vm.state.value.errorCode)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
