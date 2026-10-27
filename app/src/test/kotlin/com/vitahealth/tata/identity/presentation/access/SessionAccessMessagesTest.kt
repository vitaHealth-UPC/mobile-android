package com.vitahealth.tata.identity.presentation.access

import com.vitahealth.tata.R
import com.vitahealth.tata.identity.presentation.access.mappers.sessionAccessMessageRes
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAccessMessagesTest {
    @Test
    fun requiredFieldsAskForEmailAndPassword() {
        assertEquals(
            R.string.access_error_required_fields,
            sessionAccessMessageRes("REQUIRED_FIELDS"),
        )
    }

    @Test
    fun invalidCredentialsShareTheSameCopyAsWrongPin() {
        val credentials = sessionAccessMessageRes("INVALID_CREDENTIALS")
        assertEquals(credentials, sessionAccessMessageRes("INVALID_PIN"))
        assertEquals(credentials, sessionAccessMessageRes("PIN_INCORRECT"))
    }

    @Test
    fun authenticationRequiredOffersAnExplicitSignInRetry() {
        assertEquals(
            R.string.access_error_authentication_required,
            sessionAccessMessageRes("AUTHENTICATION_REQUIRED"),
        )
    }

    @Test
    fun unknownCodesFallBackToAGenericMessage() {
        assertEquals(R.string.access_error_generic, sessionAccessMessageRes("SOMETHING_NEW"))
    }
}
