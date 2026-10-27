package com.vitahealth.tata.omission.application.policies

import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import java.time.Instant

/**
 * Prevents delayed messages from displaying after logout or under a different recipient account.
 */
class OmissionPushAccessPolicy(
    private val sessions: SessionStore,
    private val targets: PushTargetStore,
    private val now: () -> Instant = Instant::now,
) {
    /** Revalidates a stored notification tap instead of trusting its earlier display decision. */
    fun allowsDestination(destination: PushDestination): Boolean {
        val session = sessions.session() ?: return false
        return when (destination) {
            is PushDestination.CaregiverAlerts ->
                session.owner?.subjectId == destination.caregiverId &&
                    allows(
                        OmissionPush(
                            OmissionPushKind.CAREGIVER_ALERT,
                            destination.olderAdultId,
                            null,
                        ),
                        session,
                    )
            is PushDestination.OlderAdultHome ->
                allows(
                    OmissionPush(
                        OmissionPushKind.REINFORCED_REMINDER,
                        destination.olderAdultId,
                        null,
                    ),
                    session,
                )
            is PushDestination.OlderAdultDose ->
                allows(
                    OmissionPush(
                        OmissionPushKind.REINFORCED_REMINDER,
                        destination.olderAdultId,
                        null,
                    ),
                    session,
                )
        }
    }

    fun allows(push: OmissionPush): Boolean = sessions.session()?.let { allows(push, it) } ?: false

    private fun allows(push: OmissionPush, session: StoredSession): Boolean {
        val owner = session.owner ?: return false
        if (!session.expiresAt.isAfter(now())) return false
        return when (push.kind) {
            OmissionPushKind.REINFORCED_REMINDER ->
                owner.role == "OLDER_ADULT" && owner.subjectId == push.olderAdultId
            OmissionPushKind.CAREGIVER_ALERT ->
                owner.role == "CAREGIVER" &&
                    targets.caregiverFor(push.olderAdultId) == owner.subjectId
        }
    }
}
