package com.vitahealth.tata.app

import com.google.gson.Gson
import com.vitahealth.tata.monitoring.domain.model.ContactChannelType
import com.vitahealth.tata.monitoring.infrastructure.remote.ContactResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.OlderAdultProfileResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.toDomain
import org.junit.Assert.assertEquals
import org.junit.Test

/** `ContactChannelResource` and `OlderAdultProfileResource` as the backend Swagger publishes them. */
class ContactContractTest {
    private val gson = Gson()

    @Test fun phoneChannelMapsToTheDomain() {
        val channel = gson.fromJson("""{ "type": "PHONE", "value": "+51 999 888 777" }""", ContactResponse::class.java).toDomain()

        assertEquals(ContactChannelType.PHONE, channel.type)
        assertEquals("+51 999 888 777", channel.value)
    }

    @Test fun whatsappChannelMapsToTheDomain() {
        val channel = gson.fromJson("""{ "type": "WHATSAPP", "value": "+51 999 888 777" }""", ContactResponse::class.java).toDomain()

        assertEquals(ContactChannelType.WHATSAPP, channel.type)
        assertEquals("51999888777", channel.digits)
    }

    @Test fun profileCarriesTheFullName() {
        val json = """
            {
              "id": "adult-1",
              "registeredByCaregiverId": "caregiver-1",
              "fullName": "Rosa Vargas",
              "birthDate": "1958-05-12",
              "emergencyContactName": "Diego",
              "emergencyContactRelationship": "Hijo",
              "emergencyContactPhone": "+51 999 888 777",
              "createdAt": "2026-10-01T10:00:00Z"
            }
        """.trimIndent()

        assertEquals("Rosa Vargas", gson.fromJson(json, OlderAdultProfileResponse::class.java).fullName)
    }
}
