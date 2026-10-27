package com.vitahealth.tata.identity.infrastructure.external

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicContactDestinationTest {
    @Test fun acceptsOnlyExplicitValidPublicDestinations() {
        assertEquals(
            PublicContactDestination.Kind.WEB,
            PublicContactDestination.parse("https://example.org/support")?.kind,
        )
        assertEquals(
            PublicContactDestination.Kind.EMAIL,
            PublicContactDestination.parse("mailto:service@example.org")?.kind,
        )
        assertEquals(
            PublicContactDestination.Kind.PHONE,
            PublicContactDestination.parse("tel:+51987654321")?.kind,
        )
    }

    @Test fun rejectsUnverifiedOrDangerousDestinations() {
        listOf(
            null, "", " ", "mailto:", "tel:555", "tel:911", "tel:+123",
            "http://example.org", "https://", "https://example",
            "https://someone@example.org/support", "https://example.org:8080/",
            "mailto:service@example.org?body=untrusted", "mailto:bad%0A@example.org",
            "file:///sdcard/secret", "content://private.provider/data",
            "javascript:alert(1)", "intent://foo#Intent;end",
            "https://example.org/#unsafe",
        ).forEach { raw ->
            assertNull("Should reject: $raw", PublicContactDestination.parse(raw))
        }
    }
}
