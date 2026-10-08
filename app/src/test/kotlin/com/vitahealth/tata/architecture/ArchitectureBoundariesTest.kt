package com.vitahealth.tata.architecture

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

/** Preserves context and domain boundaries in the single app source set. */
class ArchitectureBoundariesTest {
    private val root = Path.of("src/main/kotlin/com/vitahealth/tata")

    @Test
    fun domainDoesNotImportFrameworksOrUpperLayers() {
        val forbidden = Regex("import (android\\.|androidx\\.|retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.(application|infrastructure|presentation)\\.)")
        val violations = kotlinFiles().filter { path ->
            path.toString().replace('\\', '/').contains("/domain/") && forbidden.containsMatchIn(path.toFile().readText())
        }
        assertTrue("Domain imports upper layers: $violations", violations.isEmpty())
    }

    @Test
    fun featurePackagesDoNotImportAnotherContext() {
        val imports = Regex("import com\\.vitahealth\\.tata\\.(\\w+)\\.")
        val violations = mutableListOf<String>()
        kotlinFiles().forEach { path ->
            val owner = root.relativize(path).getName(0).toString()
            if (owner == "app") return@forEach
            imports.findAll(path.toFile().readText()).forEach { match ->
                val target = match.groupValues[1]
                if (target != owner && target != "shared") violations += "$path -> $target"
            }
        }
        assertTrue("Private cross-context imports: $violations", violations.isEmpty())
    }

    @Test
    fun applicationDoesNotImportAndroidOrItsAdapters() {
        val forbidden = Regex("import (android\\.|androidx\\.|retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.(infrastructure|presentation)\\.)")
        val violations = kotlinFiles().filter { path ->
            path.toString().replace('\\', '/').contains("/application/") &&
                forbidden.containsMatchIn(path.toFile().readText())
        }
        assertTrue("Application depends on UI or adapters: $violations", violations.isEmpty())
    }

    @Test
    fun presentationUsesContractsInsteadOfInfrastructureImplementations() {
        val forbidden = Regex("import (retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.infrastructure\\.)")
        val violations = kotlinFiles().filter { path ->
            path.toString().replace('\\', '/').contains("/presentation/") &&
                forbidden.containsMatchIn(path.toFile().readText())
        }
        assertTrue("Presentation imports infrastructure implementations: $violations", violations.isEmpty())
    }

    private fun kotlinFiles(): List<Path> {
        assertTrue("App source root is missing", Files.isDirectory(root))
        return Files.walk(root).use { paths -> paths.filter { it.toString().endsWith(".kt") }.toList() }
    }
}
