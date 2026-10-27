package com.vitahealth.tata.carelink.integration

import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonEmergencyContactCommand
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonEmergencyContactCommandHandler
import com.vitahealth.tata.carelink.infrastructure.remote.*
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteAlertHistoryReader
import com.vitahealth.tata.shared.application.session.*
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.io.File
import java.net.URI
import java.time.Instant
import java.util.Properties
import kotlinx.coroutines.runBlocking

/** Opt-in real HTTP probe. Fixture credentials stay in a private local file, never task output. */
object LocalBackendContractProbe {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        require(args.size == 1) { "Supply the private local fixture path" }
        val file = File(args[0])
        val context = Properties().apply { file.inputStream().use { load(it) } }
        val baseUrl = context.getProperty("baseUrl")
        require(URI(baseUrl).host in setOf("127.0.0.1", "localhost")) {
            "This probe only accepts an isolated local backend"
        }
        val adult = context.getProperty("adultId")
        val caregiver = context.getProperty("caregiverId")
        val store =
            ProbeSessionStore(
                StoredSession(
                    context.getProperty("caregiverToken"),
                    Instant.now().plusSeconds(300),
                    SessionOwner(caregiver, "CAREGIVER"),
                )
            )
        val retrofit = ApiClientFactory.create(baseUrl) { store.accessToken() }
        try {
            ClinicalJourneyContractProbe.verify(
                retrofit,
                ApiClientFactory.create(baseUrl) { context.getProperty("adultToken") },
                caregiver,
                adult,
            )
            val profileApi = retrofit.create(CareLinkApiService::class.java)
            val before = profileApi.getOlderAdult(adult)
            check(before.code() == 200) { "Authorized profile read failed: HTTP ${before.code()}" }
            val original = requireNotNull(before.body())
            val command =
                UpdateLinkedPersonEmergencyContactCommandHandler(
                    AuthorizedLinkedPersonEmergencyContactUpdater(store, caregiver, baseUrl)
                )
            check(
                command(
                    UpdateLinkedPersonEmergencyContactCommand(
                        caregiver,
                        adult,
                        "Ana Local",
                        "Hija",
                        "+51912345678",
                    )
                )
                    is AppResult.Success
            ) {
                "Android contact mutation did not succeed"
            }
            val updated = profileApi.getOlderAdult(adult)
            check(updated.code() == 200)
            val saved = requireNotNull(updated.body())
            check(
                saved.emergencyContactName == "Ana Local" &&
                    saved.emergencyContactRelationship == "Hija" &&
                    saved.emergencyContactPhone == "+51912345678"
            ) {
                "Contact was not persisted"
            }
            check(
                saved.fullName == original.fullName &&
                    saved.birthDate == original.birthDate &&
                    saved.createdAt == original.createdAt
            ) {
                "Contact mutation changed basic profile evidence"
            }
            val events =
                RemoteAlertHistoryReader(retrofit.create(FamilyMonitoringApiService::class.java))
                    .read(caregiver, adult, context.getProperty("alertId").toLong())
            check(
                events is AppResult.Success &&
                    events.value.map { it.type } ==
                        listOf(
                            RecordedAlertEvent.Type.INTAKE_SCHEDULED,
                            RecordedAlertEvent.Type.ALERT_GENERATED,
                        )
            ) {
                "Persisted alert timeline did not match the Android contract"
            }
            store.value =
                StoredSession(
                    context.getProperty("adultToken"),
                    Instant.now().plusSeconds(300),
                    SessionOwner(adult, "OLDER_ADULT"),
                )
            check(
                AuthorizedConsentWithdrawal(store, baseUrl).withdraw(adult) is AppResult.Success
            ) {
                "Android consent withdrawal did not succeed"
            }
            val current =
                ApiClientFactory.create(baseUrl) { context.getProperty("adultToken") }
                    .create(ConsentWithdrawalApi::class.java)
                    .current()
            check(current.code() == 401) {
                "Withdrawn adult session remains usable: HTTP ${current.code()}"
            }
            val revoked =
                ApiClientFactory.create(baseUrl) { context.getProperty("caregiverToken") }
                    .create(FamilyMonitoringApiService::class.java)
                    .status(adult, caregiver)
            check(revoked.code() == 403) {
                "Caregiver can still read revoked monitoring: HTTP ${revoked.code()}"
            }
            println(
                "LOCAL_BACKEND_CONTRACTS_PASS: persisted contact, unchanged basic profile, alert timeline, adult withdrawal, revoked adult session and caregiver access"
            )
        } finally {
            File(file.path + ".done").writeText("finished")
        }
    }

    private class ProbeSessionStore(var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun save(accessToken: String, expiresAt: String) =
            error("Probe credentials cannot be overwritten")

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) =
            error("Probe credentials cannot be overwritten")

        override fun clear() {
            value = null
        }
    }
}
