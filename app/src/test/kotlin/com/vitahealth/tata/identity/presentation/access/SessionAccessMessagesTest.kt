package com.vitahealth.tata.identity.presentation.access

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionAccessMessagesTest {
    @Test
    fun requiredFieldsAskForEmailAndPassword() {
        assertEquals("Completa tu correo y contraseña.", sessionAccessMessage("REQUIRED_FIELDS"))
    }

    @Test
    fun invalidCredentialsShareTheSameCopyAsWrongPin() {
        val credentials = sessionAccessMessage("INVALID_CREDENTIALS")
        assertEquals(credentials, sessionAccessMessage("INVALID_PIN"))
        assertEquals(credentials, sessionAccessMessage("PIN_INCORRECT"))
    }

    @Test
    fun unknownCodesFallBackToAGenericMessage() {
        assertTrue(sessionAccessMessage("SOMETHING_NEW").contains("No pudimos completar el acceso"))
    }
}
