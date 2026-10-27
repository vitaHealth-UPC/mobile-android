package com.vitahealth.tata.identity.presentation.recovery

import com.vitahealth.tata.identity.application.ports.PasswordRecovery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {
    @Test
    fun onlyServerReceiptsAdvanceStepsAndSuccessfulResetErasesAllCredentials() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            var requests = 0
            var completions = 0
            val remote =
                object : PasswordRecovery {
                    override suspend fun request(email: String): AppResult<Unit> {
                        requests++
                        assertEquals("user@example.com", email)
                        return AppResult.Success(Unit)
                    }

                    override suspend fun complete(
                        email: String,
                        code: String,
                        newPassword: String,
                    ): AppResult<Unit> {
                        completions++
                        assertEquals("012345", code)
                        assertEquals(" new-password ", newPassword)
                        return AppResult.Success(Unit)
                    }
                }
            val vm = PasswordRecoveryViewModel(remote)
            vm.email("invalid")
            vm.request()
            assertEquals(0, requests)
            vm.email(" user@example.com ")
            vm.request()
            assertEquals(RecoveryStep.Code, vm.state.value.step)
            vm.code("012345")
            vm.password("short")
            vm.complete()
            assertEquals(0, completions)
            vm.password(" new-password ")
            vm.complete()
            assertEquals(RecoveryStep.Complete, vm.state.value.step)
            assertEquals("", vm.state.value.email)
            assertEquals("", vm.state.value.password)
            assertEquals("", vm.state.value.code)
            assertEquals(1, requests)
            assertEquals(1, completions)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectedCodeStaysOnFormAndErasesPasswordForExplicitRetry() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val vm =
                PasswordRecoveryViewModel(
                    object : PasswordRecovery {
                        override suspend fun request(email: String) = AppResult.Success(Unit)

                        override suspend fun complete(
                            email: String,
                            code: String,
                            newPassword: String,
                        ) = AppResult.Failure("Rejected", code = "INVALID_RECOVERY_CODE")
                    }
                )
            vm.email("user@example.com")
            vm.request()
            vm.code("012345")
            vm.password("new-password")
            vm.complete()
            assertEquals(RecoveryStep.Code, vm.state.value.step)
            assertEquals("INVALID_RECOVERY_CODE", vm.state.value.error)
            assertEquals("", vm.state.value.password)
            assertFalse(vm.state.value.toString().contains("user@example.com"))
        } finally {
            Dispatchers.resetMain()
        }
    }
}
