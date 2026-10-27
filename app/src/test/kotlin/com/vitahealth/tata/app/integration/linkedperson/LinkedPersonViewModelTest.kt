package com.vitahealth.tata.app.integration.linkedperson

import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonProfileCommandHandler
import com.vitahealth.tata.carelink.application.ports.LinkedPersonProfileUpdater
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LinkedPersonViewModelTest {
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val snapshot =
        LinkedPersonSnapshot(
            MonitoredPerson("adult-1", "Persona real", LocalDate.of(1958, 5, 12)),
            FamilySummary(
                "Persona real",
                LocalDate.now(),
                AdherenceCounts(3, 5),
                AdherenceCounts(1, 2),
                null,
                emptyList(),
                emptyList(),
                true,
            ),
            emptyList(),
        )

    @Test
    fun emergencyContactRequiresCompleteFieldsAndRefreshesOnlyAfterReceipt() {
        var writes = 0
        var reads = 0
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                LinkedPersonSnapshotReader { _, _, _ ->
                    reads++
                    AppResult.Success(snapshot)
                },
                ContactOptionReader { AppResult.Failure("Unavailable") },
                UpdateLinkedPersonProfileCommandHandler(
                    LinkedPersonProfileUpdater { _, _, _, _ ->
                        fail("Contact editing must not write basic profile")
                        AppResult.Success(Unit)
                    }
                ),
                com.vitahealth.tata.carelink.application.commands
                    .UpdateLinkedPersonEmergencyContactCommandHandler(
                        com.vitahealth.tata.carelink.application.ports
                            .LinkedPersonEmergencyContactUpdater {
                                caregiver,
                                adult,
                                name,
                                relation,
                                phone ->
                                assertEquals(
                                    listOf("caregiver-1", "adult-1", "Ana", "Hija", "+51912345678"),
                                    listOf(caregiver, adult, name, relation, phone),
                                )
                                writes++
                                AppResult.Success(Unit)
                            }
                    ),
            )
        vm.beginEdit()
        vm.beginContactEdit()
        vm.saveEdit()
        assertEquals(0, writes)
        assertNotNull(vm.state.value.editError)
        vm.setContactField("name", "Ana")
        vm.setContactField("relationship", "Hija")
        vm.setContactField("phone", "+51912345678")
        vm.saveEdit()
        assertEquals(1, writes)
        assertEquals(2, reads)
        assertFalse(vm.state.value.editing)
        vm.viewModelScope.cancel()
    }

    @Test
    fun lateEditReceiptCannotRestoreContentAfterContactDenialOrRevokedRefresh() {
        for (denyByContact in listOf(true, false)) {
            lateinit var receipt: Continuation<AppResult<Unit>>
            var reads = 0
            var revoked = false
            val vm =
                LinkedPersonViewModel(
                    "caregiver-1",
                    "adult-1",
                    "hint",
                    LinkedPersonSnapshotReader { _, _, _ ->
                        reads++
                        if (revoked)
                            AppResult.Failure("Revoked", code = "CARE_RELATIONSHIP_REQUIRED")
                        else AppResult.Success(snapshot)
                    },
                    ContactOptionReader {
                        AppResult.Failure("Session changed", code = "SESSION_CHANGED")
                    },
                    UpdateLinkedPersonProfileCommandHandler(
                        LinkedPersonProfileUpdater { _, _, _, _ ->
                            suspendCoroutine { receipt = it }
                        }
                    ),
                )
            vm.beginEdit()
            vm.setEditName("Nombre editado")
            vm.saveEdit()
            assertTrue(vm.state.value.editSaving)
            if (denyByContact) vm.contact(ContactChannelType.PHONE)
            else {
                revoked = true
                vm.refresh()
            }
            val denied = vm.state.value
            assertTrue(denied.accessDenied)
            assertNull(denied.snapshot)
            receipt.resume(AppResult.Success(Unit))
            assertEquals(denied, vm.state.value)
            assertEquals(if (denyByContact) 1 else 2, reads)
            vm.viewModelScope.cancel()
        }
    }

    @Test
    fun revocationClearsPreviouslyRenderedMedicalContent() {
        var revoked = false
        val reader = LinkedPersonSnapshotReader { caregiver, adult, _ ->
            assertEquals("caregiver-1", caregiver)
            assertEquals("adult-1", adult)
            if (revoked) AppResult.Failure("revocado", code = "CARE_RELATIONSHIP_REQUIRED")
            else AppResult.Success(snapshot)
        }
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                reader,
                ContactOptionReader { AppResult.Success(ContactOption(null, null)) },
            )
        assertEquals(snapshot, vm.state.value.snapshot)
        revoked = true
        vm.refresh()
        assertNull(vm.state.value.snapshot)
        assertTrue(vm.state.value.accessDenied)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun unavailableChannelCannotLaunchUnrelatedContact() {
        var calls = 0
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(snapshot) },
                ContactOptionReader {
                    calls++
                    AppResult.Success(
                        ContactOption(ContactChannel(ContactChannelType.PHONE, "999555444"), null)
                    )
                },
            )
        vm.contact(ContactChannelType.WHATSAPP)
        assertEquals(1, calls)
        assertNull(vm.state.value.pendingContact)
        assertNotNull(vm.state.value.contactError)
        vm.contact(ContactChannelType.PHONE)
        assertEquals(ContactChannelType.PHONE, vm.state.value.pendingContact?.type)
        vm.contactHandled(false)
        assertNull(vm.state.value.pendingContact)
        assertNotNull(vm.state.value.contactError)
    }

    @Test
    fun contactRevocationRemovesClinicalSnapshot() {
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(snapshot) },
                ContactOptionReader { AppResult.Failure("sesion", code = "SESSION_CHANGED") },
            )
        vm.contact(ContactChannelType.PHONE)
        assertTrue(vm.state.value.accessDenied)
        assertNull(vm.state.value.snapshot)
        assertNull(vm.state.value.pendingContact)
    }

    @Test
    fun editSuccessRefreshesProfileRatherThanFabricatingLocalResults() {
        var latest = snapshot
        var writes = 0
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(latest) },
                ContactOptionReader { AppResult.Success(ContactOption(null, null)) },
                UpdateLinkedPersonProfileCommandHandler(
                    LinkedPersonProfileUpdater { caregiver, adult, name, birthDate ->
                        assertEquals("caregiver-1", caregiver)
                        assertEquals("adult-1", adult)
                        writes++
                        latest =
                            latest.copy(
                                person = latest.person.copy(fullName = name, birthDate = birthDate)
                            )
                        AppResult.Success(Unit)
                    }
                ),
            )
        vm.beginEdit()
        assertTrue(vm.state.value.editing)
        vm.setEditName("Rosa Editada")
        vm.saveEdit()
        assertEquals(1, writes)
        assertFalse(vm.state.value.editing)
        assertEquals("Rosa Editada", vm.state.value.snapshot?.person?.fullName)
    }

    @Test
    fun editInvalidDateNeverWritesAndRevocationErasesSnapshot() {
        var writes = 0
        var revoke = false
        val vm =
            LinkedPersonViewModel(
                "caregiver-1",
                "adult-1",
                "hint",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(snapshot) },
                ContactOptionReader { AppResult.Success(ContactOption(null, null)) },
                UpdateLinkedPersonProfileCommandHandler(
                    LinkedPersonProfileUpdater { _, _, _, _ ->
                        writes++
                        if (revoke) AppResult.Failure("denied", code = "RESOURCE_ACCESS_DENIED")
                        else AppResult.Success(Unit)
                    }
                ),
            )
        vm.beginEdit()
        vm.setEditBirthDate("2030-01-01")
        vm.saveEdit()
        assertEquals(0, writes)
        assertNotNull(vm.state.value.editError)
        vm.setEditBirthDate("1958-05-12")
        vm.setEditName("Rosa Editada")
        revoke = true
        vm.saveEdit()
        assertEquals(1, writes)
        assertTrue(vm.state.value.accessDenied)
        assertNull(vm.state.value.snapshot)
        assertFalse(vm.state.value.editing)
    }
}
