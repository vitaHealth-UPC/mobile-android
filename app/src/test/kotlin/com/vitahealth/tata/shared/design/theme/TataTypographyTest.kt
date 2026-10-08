package com.vitahealth.tata.shared.design.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TataTypographyTest {
    @Test
    fun withoutReadingAssistanceTheMaterialDefaultsAreKept() {
        val typography = tataTypography(readingAssistance = false)

        assertEquals(androidx.compose.material3.Typography().bodyMedium, typography.bodyMedium)
    }

    @Test
    fun readingAssistanceWidensTheLineHeightOfEveryBodyStyle() {
        val plain = tataTypography(readingAssistance = false)
        val assisted = tataTypography(readingAssistance = true)

        assertTrue(assisted.bodyLarge.lineHeight.value > plain.bodyLarge.lineHeight.value)
        assertTrue(assisted.bodyMedium.lineHeight.value > plain.bodyMedium.lineHeight.value)
        assertTrue(assisted.bodySmall.lineHeight.value > plain.bodySmall.lineHeight.value)
    }

    @Test
    fun readingAssistanceKeepsTheTextAtLeastMedium() {
        val assisted = tataTypography(readingAssistance = true)

        assertTrue(assisted.bodyMedium.fontWeight!!.weight >= FontWeight.Medium.weight)
        assertTrue(assisted.labelSmall.fontWeight!!.weight >= FontWeight.Medium.weight)
    }

    @Test
    fun aStyleThatIsAlreadyBoldIsNotLightened() {
        val assisted = tataTypography(readingAssistance = true)

        assertEquals(FontWeight.Medium, assisted.titleMedium.fontWeight)
        assertTrue(assisted.titleLarge.fontWeight!!.weight >= FontWeight.Normal.weight)
    }

    @Test
    fun fontSizesAreNotChangedByReadingAssistance() {
        val plain = tataTypography(readingAssistance = false)
        val assisted = tataTypography(readingAssistance = true)

        assertEquals(plain.bodyLarge.fontSize, assisted.bodyLarge.fontSize)
        assertEquals(plain.titleLarge.fontSize, assisted.titleLarge.fontSize)
    }
}
