package com.vitahealth.tata.architecture

import com.vitahealth.tata.inventory.presentation.inventory.screens.InventoryScreen
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

/** Preserves context and domain boundaries in the single app source set. */
class ArchitectureBoundariesTest {
    private val root = Path.of("src/main/kotlin/com/vitahealth/tata")

    @Test
    fun domainDoesNotImportFrameworksOrUpperLayers() {
        val forbidden =
            Regex(
                "import (android\\.|androidx\\.|retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.(application|infrastructure|interfaces|presentation)\\.)"
            )
        val violations =
            kotlinFiles().filter { path ->
                path.toString().replace('\\', '/').contains("/domain/") &&
                    forbidden.containsMatchIn(path.toFile().readText())
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
        val forbidden =
            Regex(
                "import (android\\.|androidx\\.|retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.(infrastructure|interfaces|presentation)\\.)"
            )
        val violations =
            kotlinFiles().filter { path ->
                path.toString().replace('\\', '/').contains("/application/") &&
                    forbidden.containsMatchIn(path.toFile().readText())
            }
        assertTrue("Application depends on UI or adapters: $violations", violations.isEmpty())
    }

    @Test
    fun presentationUsesContractsInsteadOfInfrastructureImplementations() {
        val forbidden =
            Regex("import (retrofit2\\.|com\\.vitahealth\\.tata\\.\\w+\\.infrastructure\\.)")
        val violations =
            kotlinFiles().filter { path ->
                path.toString().replace('\\', '/').contains("/presentation/") &&
                    forbidden.containsMatchIn(path.toFile().readText())
            }
        assertTrue(
            "Presentation imports infrastructure implementations: $violations",
            violations.isEmpty(),
        )
    }

    @Test
    fun navigationEntryPointsDoNotConstructAdaptersOrReadTheApplication() {
        val forbidden =
            Regex(
                "import (com\\.vitahealth\\.tata\\.(app\\.|\\w+\\.infrastructure\\.)|retrofit2\\.|android\\.content\\.)"
            )
        val violations =
            kotlinFiles().filter { path ->
                path.toString().replace('\\', '/').contains("/presentation/navigation/") &&
                    forbidden.containsMatchIn(path.toFile().readText())
            }
        assertTrue(
            "Navigation entry points depend on composition or adapters: $violations",
            violations.isEmpty(),
        )
    }

    @Test
    fun inventoryCqrsHasSeparateCommandAndQueryBoundaries() {
        val inventoryApplication = root.resolve("inventory/application")
        val commands = inventoryApplication.resolve("commands")
        val queries = inventoryApplication.resolve("queries")

        val expectedCommands =
            listOf(
                "RegisterInitialInventoryCommand.kt",
                "RegisterInitialInventoryCommandHandler.kt",
                "RegisterReplenishmentCommand.kt",
                "RegisterReplenishmentCommandHandler.kt",
            )
        val expectedQueries =
            listOf("GetInventoryStockQuery.kt", "GetInventoryStockQueryHandler.kt")

        assertTrue("Missing inventory Commands package", Files.isDirectory(commands))
        assertTrue("Missing inventory Queries package", Files.isDirectory(queries))
        assertTrue(
            "Missing or misplaced inventory Commands",
            expectedCommands.all { Files.isRegularFile(commands.resolve(it)) },
        )
        assertTrue(
            "Missing or misplaced inventory Queries",
            expectedQueries.all { Files.isRegularFile(queries.resolve(it)) },
        )

        val commandImport =
            Regex("import com\\.vitahealth\\.tata\\.inventory\\.application\\.commands\\.")
        val violations =
            Files.walk(queries).use { paths ->
                paths.filter { it.toString().endsWith(".kt") }
                    .filter { commandImport.containsMatchIn(it.toFile().readText()) }
                    .toList()
            }
        assertTrue("Queries must not depend on mutation commands: $violations", violations.isEmpty())
    }

    @Test
    fun inventoryFeatureFlowOwnsItsScreenStateAndWidgets() {
        val presentation = root.resolve("inventory/presentation")
        val flow = presentation.resolve("inventory")
        val required =
            listOf(
                "InventoryRoute.kt",
                "screens/InventoryScreen.kt",
                "viewmodel/InventoryViewModel.kt",
                "viewmodel/InventoryUiState.kt",
                "mappers/InventoryUiMapper.kt",
                "screens/InventoryPreviews.kt",
                "ui/InventoryFormBodies.kt",
                "ui/InventoryStockCards.kt",
                "ui/InventoryWidgets.kt",
            )

        assertTrue("Inventory must expose a cohesive presentation flow", Files.isDirectory(flow))
        assertTrue("Missing Inventory flow files", required.all {
            Files.isRegularFile(flow.resolve(it))
        })
        assertTrue(
            "Legacy Inventory screen tree should not remain",
            !Files.exists(presentation.resolve("screens/inventory/InventoryScreen.kt")),
        )
        assertTrue(
            "Legacy Inventory ViewModel tree should not remain",
            !Files.exists(presentation.resolve("viewmodel/inventory/InventoryViewModel.kt")),
        )

        val securityAdapter =
            root.resolve("inventory/infrastructure/security/SessionScopedInventoryRepository.kt")
        assertTrue("Session adapter belongs to security infrastructure", Files.isRegularFile(securityAdapter))
    }

    @Test
    fun treatmentCqrsAndPresentationFlowsHaveExplicitBoundaries() {
        val treatment = root.resolve("treatment")
        val commands = treatment.resolve("application/commands")
        val queries = treatment.resolve("application/queries")

        assertTrue("Treatment commands missing", Files.isDirectory(commands))
        assertTrue("Treatment queries missing", Files.isDirectory(queries))
        assertTrue(
            "Legacy usecases package must not remain",
            !Files.exists(treatment.resolve("application/usecases")),
        )
        val commandSources = Files.list(commands).use { it.toList() }
        val querySources = Files.list(queries).use { it.toList() }
        assertTrue("Unexpected Treatment command count", commandSources.size == 19)
        assertTrue("Unexpected Treatment query count", querySources.size == 7)
        assertTrue(
            "A read-only query must not import a mutation command",
            querySources.none {
                it.toFile().readText().contains(
                    "import com.vitahealth.tata.treatment.application.commands."
                )
            },
        )

        val flowScreens =
            mapOf(
                "medication" to
                    listOf("MedicationManagement", "MedicationRegistration", "MyMedications"),
                "treatment" to
                    listOf(
                        "TreatmentList",
                        "TreatmentCreation",
                        "TreatmentDetail",
                        "TreatmentDoseFrequency",
                        "TreatmentLifecycle",
                        "TreatmentReminder",
                        "TreatmentScheduleInstructions",
                    ),
            )
        for ((flow, names) in flowScreens) {
            val folder = treatment.resolve("presentation").resolve(flow)
            for (name in names) {
                assertTrue("Missing " + name + " Route", Files.isRegularFile(folder.resolve(name + "Route.kt")))
                assertTrue("Missing " + name + " Screen", Files.isRegularFile(folder.resolve("screens/" + name + "Screen.kt")))
            }
        }
        assertTrue(
            "Treatment flow screens and ViewModels must be separated",
            flowScreens.all { (flow, names) ->
                val folder = treatment.resolve("presentation").resolve(flow)
                names.all { name ->
                    Files.isRegularFile(folder.resolve("viewmodel/" + name + "ViewModel.kt"))
                }
            },
        )
        assertTrue("Old Screen tree remains", !Files.exists(treatment.resolve("presentation/screens")))
        assertTrue("Old ViewModel tree remains", !Files.exists(treatment.resolve("presentation/viewmodel")))
        assertTrue(
            "Treatment session reader must be isolated in security",
            Files.isRegularFile(
                treatment.resolve(
                    "infrastructure/security/SessionScopedMyMedicationsOverviewReader.kt"
                )
            ),
        )
    }

    @Test
    fun treatmentHostRegistersAllTenRoutesExactlyOnce() {
        val nav = root.resolve("app/navigation/graphs")
        val wrapper = nav.resolve("TreatmentNavigationGraph.kt").toFile().readText()
        assertTrue("Catalog graph not composed", wrapper.contains("treatmentCatalogDestinations(navController, container)"))
        assertTrue("Medication graph not composed", wrapper.contains("medicationDestinations(navController, container)"))
        assertTrue("Wizard graph not composed", wrapper.contains("treatmentWizardDestinations(navController, container)"))

        val graphFiles =
            listOf(
                "TreatmentCatalogNavigationGraph.kt",
                "MedicationNavigationGraph.kt",
                "TreatmentRegimenNavigationGraph.kt",
                "TreatmentActivationNavigationGraph.kt",
            )
        val all = graphFiles.joinToString("\n") { name ->
            val file = nav.resolve(name)
            assertTrue("Missing graph: " + name, Files.isRegularFile(file))
            file.toFile().readText()
        }
        val destinations =
            listOf(
                "TreatmentList",
                "MedicationManagement",
                "MedicationRegistration",
                "TreatmentCreation",
                "TreatmentDoseFrequency",
                "TreatmentScheduleInstructions",
                "TreatmentReminders",
                "TreatmentLifecycle",
                "TreatmentDetail",
                "MyMedications",
            )
        for (name in destinations) {
            val pattern =
                Regex("composable\\(\\s*(?:route\\s*=\\s*)?RootDestination\\." + name + "\\.route")
            assertTrue(
                "Expected one registration for " + name,
                pattern.findAll(all).count() == 1,
            )
        }
    }

    @Test
    fun identityUsesCqrsAndPresentationIsGroupedByFlow() {
        val identity = root.resolve("identity")
        val commands = identity.resolve("application/commands")
        val queries = identity.resolve("application/queries")
        assertTrue("Missing Identity commands", Files.isDirectory(commands))
        assertTrue("Missing Identity queries", Files.isDirectory(queries))
        assertTrue(
            "Old Identity usecases package remains",
            !Files.exists(identity.resolve("application/usecases")),
        )

        val commandCount = Files.list(commands).use { paths -> paths.count() }
        val queryCount = Files.list(queries).use { paths -> paths.count() }
        assertTrue("Missing Identity command/handler pairs", commandCount >= 20L)
        assertTrue(
            "Missing social sign-in CQRS handler",
            Files.isRegularFile(commands.resolve("SignInSocialCommandHandler.kt")),
        )
        assertTrue("Missing Identity query/handler pairs", queryCount == 6L)
        val flowRoots =
            mapOf(
                "access" to listOf("screens", "viewmodel", "ui", "mappers"),
                "registration" to listOf("screens", "viewmodel"),
                "subscription" to listOf("screens", "viewmodel", "mappers"),
                "onboarding" to listOf("screens"),
            )
        for ((flow, parts) in flowRoots) {
            val flowPath = identity.resolve("presentation").resolve(flow)
            assertTrue("Identity flow missing: $flow", Files.isDirectory(flowPath))
            for (part in parts) {
                assertTrue("Missing $flow/$part", Files.isDirectory(flowPath.resolve(part)))
            }
        }
        assertTrue(
            "Presentation root must not keep parallel screen trees",
            !Files.exists(identity.resolve("presentation/screens")),
        )
    }

    @Test
    fun identityHostInjectsCompositionRatherThanDiscoveringGlobalApplication() {
        val nav = root.resolve("app/navigation/graphs")
        val graphs =
            listOf("IdentityNavigationGraph.kt", "SubscriptionNavigationGraph.kt")
        for (name in graphs) {
            val source = nav.resolve(name).toFile().readText()
            assertTrue("$name must accept an AppContainer", source.contains("container: AppContainer"))
            assertTrue("$name must not load TataApplication", !source.contains("TataApplication"))
            assertTrue("$name must not use LocalContext", !source.contains("LocalContext"))
        }
    }

    @Test
    fun identityViewModelDependsOnHandlersNotSessionPersistence() {
        val source =
            root.resolve(
                "identity/presentation/access/viewmodel/SessionAccessViewModel.kt"
            ).toFile().readText()

        assertTrue(
            "Identity ViewModel must not import session repository ports",
            !source.contains("application.ports.SessionAccessRepository"),
        )
        assertTrue(
            "Identity ViewModel must use application CommandHandlers",
            listOf(
                "SignInCommandHandler",
                "RegisterPinCommandHandler",
                "RestoreSessionCommandHandler",
            ).all(source::contains),
        )
    }

    @Test
    fun careLinkOwnsExplicitCqrsPresentationAndSessionAdapters() {
        val bc = root.resolve("carelink")
        val commands = bc.resolve("application/commands")
        val queries = bc.resolve("application/queries")
        assertTrue("Care Link commands missing", Files.isDirectory(commands))
        assertTrue("Care Link queries missing", Files.isDirectory(queries))
        assertTrue("Catch-all usecases directory remains", !Files.exists(bc.resolve("application/usecases")))
        assertTrue(
            "Care Link session adapter not isolated",
            Files.isRegularFile(
                bc.resolve("infrastructure/security/SessionScopedCaregiverProfilesRepository.kt")
            ),
        )
        for ((flow, stem) in listOf("link" to "CareLink", "profiles" to "CaregiverProfiles")) {
            val folder = bc.resolve("presentation").resolve(flow)
            assertTrue("Missing " + stem + " Route", Files.isRegularFile(folder.resolve(stem + "Route.kt")))
            assertTrue(
                "Missing " + stem + " Screen",
                Files.isRegularFile(folder.resolve("screens/" + stem + "Screen.kt")),
            )
            assertTrue(
                "Missing " + stem + " ViewModel",
                Files.isRegularFile(folder.resolve("viewmodel/" + stem + "ViewModel.kt")),
            )
        }
        val profilesVm =
            bc.resolve("presentation/profiles/viewmodel/CaregiverProfilesViewModel.kt")
                .toFile().readText()
        assertTrue(
            "Profiles ViewModel should inject CQRS handlers rather than repository ports",
            !profilesVm.contains("application.ports.CaregiverProfilesRepository")
        )
        val host = root.resolve("app/navigation/graphs/CareLinkNavigationGraph.kt").toFile().readText()
        assertTrue("Host needs container injection", host.contains("container: AppContainer"))
        assertTrue("Host must not fetch TataApplication", !host.contains("TataApplication"))
    }

    @Test
    fun intakeCqrsIsSeparatedWithoutBreakingOfflineOwnership() {
        val intake = root.resolve("intake")
        val commands = intake.resolve("application/commands")
        val queries = intake.resolve("application/queries")
        assertTrue("Missing Intake Commands", Files.isDirectory(commands))
        assertTrue("Missing Intake Queries", Files.isDirectory(queries))
        assertTrue(
            "Legacy Intake usecases package remains",
            !Files.exists(intake.resolve("application/usecases")),
        )
        for ((flow, stem) in listOf(
            "agenda" to "IntakeAgenda",
            "detail" to "DoseDetail",
            "home" to "NextDoseHome",
            "voice" to "VoiceConfirmation",
        )) {
            val path = intake.resolve("presentation").resolve(flow)
            assertTrue("Missing $flow Route", Files.isRegularFile(path.resolve(stem + "Route.kt")))
            assertTrue(
                "Missing $flow Screen",
                Files.isRegularFile(path.resolve("screens/" + stem + "Screen.kt")),
            )
            assertTrue(
                "Missing $flow ViewModel",
                Files.isRegularFile(path.resolve("viewmodel/" + stem + "ViewModel.kt")),
            )
        }
        for (name in listOf(
            "local/RoomIntakeLocalStore.kt",
            "local/room/IntakeDao.kt",
            "local/room/PendingConfirmationEntity.kt",
            "sync/ReplayPendingConfirmations.kt",
            "sync/IntakeSessionRecovery.kt",
        )) {
            assertTrue("Offline-first and replay contract missing: $name",
                Files.isRegularFile(intake.resolve("infrastructure").resolve(name)))
        }
        val agendaVm =
            intake.resolve("presentation/agenda/viewmodel/IntakeAgendaViewModel.kt")
                .toFile().readText()
        assertTrue(
            "Intake agenda ViewModel should only invoke a validated QueryHandler",
            !agendaVm.contains("application.ports.IntakeAgendaRepository")
                && agendaVm.contains("GetIntakeAgendaQueryHandler"),
        )
        val host = root.resolve("app/navigation/graphs/IntakeNavigationGraph.kt").toFile().readText()
        assertTrue("Intake host must accept app container", host.contains("container: AppContainer"))
        assertTrue("Intake host must not look up TataApplication", !host.contains("TataApplication"))
    }

    @Test
    fun monitoringUsesTypedCqrsAndFlowLocalUiBoundaries() {
        val monitoring = root.resolve("monitoring")
        assertTrue(
            "Monitoring must separate commands",
            Files.isDirectory(monitoring.resolve("application/commands")),
        )
        assertTrue(
            "Monitoring must separate queries",
            Files.isDirectory(monitoring.resolve("application/queries")),
        )
        assertTrue(
            "Legacy monitoring usecases directory remains",
            !Files.exists(monitoring.resolve("application/usecases")),
        )
        for ((flow, screens) in mapOf(
            "alerts" to listOf("Alerts", "AlertDetail"),
            "notes" to listOf("Notes"),
            "personalnotes" to listOf("PersonalNotes"),
            "summary" to listOf("FamilySummary"),
        )) {
            val folder = monitoring.resolve("presentation").resolve(flow)
            assertTrue("Missing $flow screens", Files.isDirectory(folder.resolve("screens")))
            assertTrue("Missing $flow ViewModel", Files.isDirectory(folder.resolve("viewmodel")))
            for (screen in screens) {
                assertTrue(
                    "Missing $screen Route",
                    Files.isRegularFile(folder.resolve(screen + "Route.kt")),
                )
                assertTrue(
                    "Missing $screen Screen",
                    Files.isRegularFile(folder.resolve("screens/" + screen + "Screen.kt")),
                )
            }
        }
        val adapters = listOf(
            "CaregiverSessionBoundary.kt",
            "SessionScopedAlertsRepository.kt",
            "SessionScopedFamilyMonitoringRepository.kt",
            "SessionScopedPersonalNotesRepository.kt",
            "SessionScopedNotesRepository.kt",
            "SessionScopedContactOptionReader.kt",
        )
        for (name in adapters) {
            assertTrue(
                "Missing monitoring security boundary $name",
                Files.isRegularFile(monitoring.resolve("infrastructure/security").resolve(name)),
            )
        }
        val uiPaths = listOf(
            "presentation/summary/viewmodel/FamilySummaryViewModel.kt",
            "presentation/personalnotes/viewmodel/PersonalNotesViewModel.kt",
        )
        for (path in uiPaths) {
            val source = monitoring.resolve(path).toFile().readText()
            assertTrue(
                "ViewModel uses repository instead of application handlers: $path",
                !source.contains("import com.vitahealth.tata.monitoring.domain.repositories")
                    && !source.contains("import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository"),
            )
        }
        val graph = root.resolve("app/navigation/graphs/MonitoringNavigationGraph.kt").toFile().readText()
        assertTrue("Monitoring host needs container", graph.contains("container: AppContainer"))
        assertTrue("Monitoring host must not fetch TataApplication", !graph.contains("TataApplication"))
    }

    @Test
    fun preferencesSeparatesCqrsFlowsAndPreservesOwnerScopedPersistence() {
        val preferences = root.resolve("preferences")

        for (layer in listOf("domain", "application", "infrastructure", "presentation")) {
            assertTrue("Missing preferences layer: $layer",
                Files.isDirectory(preferences.resolve(layer)))
        }
        assertTrue(
            "Legacy preferences usecases remain",
            !Files.exists(preferences.resolve("application/usecases")),
        )
        for (kind in listOf("commands", "queries")) {
            assertTrue(
                "Missing Preferences $kind",
                Files.isDirectory(preferences.resolve("application").resolve(kind)),
            )
        }

        for ((flow, stem) in listOf(
            "accessibility" to "Accessibility",
            "notifications" to "NotificationPreferences",
        )) {
            val dir = preferences.resolve("presentation").resolve(flow)
            assertTrue("Missing $flow Route", Files.isRegularFile(dir.resolve(stem + "Route.kt")))
            assertTrue(
                "Missing $flow Screen",
                Files.isRegularFile(dir.resolve("screens/" + stem + "Screen.kt")),
            )
            assertTrue("Missing $flow viewmodel", Files.isDirectory(dir.resolve("viewmodel")))
            assertTrue("Missing $flow ui", Files.isDirectory(dir.resolve("ui")))
            assertTrue("Missing $flow mappers", Files.isDirectory(dir.resolve("mappers")))
        }

        assertTrue(
            "Settings owns a reusable route registrar",
            Files.isRegularFile(preferences.resolve("presentation/navigation/PreferencesNavigationGraph.kt")),
        )
        assertTrue(
            "Owner-scoped accessibility storage missing",
            Files.isRegularFile(preferences.resolve("infrastructure/local/DataStoreAccessibilityLocalStore.kt")),
        )
        assertTrue(
            "Offline sync boundary missing",
            Files.isRegularFile(preferences.resolve("infrastructure/sync/OfflineFirstUserPreferencesRepository.kt")),
        )
        for (name in listOf(
            "SessionScopedUserPreferencesRepository.kt",
            "SessionScopedNotificationPreferencesRepository.kt",
        )) {
            assertTrue(
                "Session security adapter missing: $name",
                Files.isRegularFile(preferences.resolve("infrastructure/security").resolve(name)),
            )
        }
        for (flow in listOf("accessibility", "notifications")) {
            val vm = preferences.resolve("presentation").resolve(flow).resolve("viewmodel")
            assertTrue(
                "Preferences $flow ViewModel is bypassing handlers",
                Files.list(vm).use { files ->
                    files.filter { it.toString().endsWith("ViewModel.kt") }
                        .noneMatch {
                            val source = it.toFile().readText()
                            source.contains(".domain.repositories.")
                                || source.contains(".application.ports.")
                        }
                },
            )
        }
    }

    @Test
    fun analyticsUsesReadOnlyCqrsAndFlowLocalPresentation() {
        val analytics = root.resolve("analytics")

        for (layer in listOf("domain", "application", "infrastructure", "presentation")) {
            assertTrue(
                "Analytics layer missing: $layer",
                Files.isDirectory(analytics.resolve(layer)),
            )
        }
        assertTrue(
            "Analytics queries not separated",
            Files.isDirectory(analytics.resolve("application/queries")),
        )
        assertTrue(
            "Analytics keeps old usecases directory",
            !Files.exists(analytics.resolve("application/usecases")),
        )
        assertTrue(
            "Read-only Analytics should not create empty command folders",
            !Files.exists(analytics.resolve("application/commands")),
        )
        for ((flow, stem) in listOf(
            "history" to "AdherenceHistory",
            "recommendations" to "AdherenceRecommendations",
        )) {
            val path = analytics.resolve("presentation").resolve(flow)
            assertTrue("Missing $flow Route", Files.isRegularFile(path.resolve(stem + "Route.kt")))
            assertTrue(
                "Missing $flow Screen",
                Files.isRegularFile(path.resolve("screens/" + stem + "Screen.kt")),
            )
            assertTrue(
                "Missing $flow ViewModel",
                Files.isRegularFile(path.resolve("viewmodel/" + stem + "ViewModel.kt")),
            )
            assertTrue("Missing $flow UI", Files.isDirectory(path.resolve("ui")))
            assertTrue("Missing $flow mappers", Files.isDirectory(path.resolve("mappers")))
        }
        assertTrue(
            "Shared loading/empty card must have one owner",
            Files.isRegularFile(
                analytics.resolve("presentation/shared/ui/AnalyticsEmptyStateCard.kt")
            ),
        )
        val nav = analytics.resolve("presentation/navigation/AnalyticsNavigationGraph.kt")
        assertTrue("Missing internal analytics navigation", Files.isRegularFile(nav))
        val navSource = nav.toFile().readText()
        assertTrue(
            "Analytics must retain the history-to-recommendations navigation",
            navSource.contains("onOpenRecommendations"),
        )
        assertTrue(
            "Analytics navigation cannot access AppContainer",
            !navSource.contains("AppContainer") && !navSource.contains("TataApplication"),
        )
        assertTrue(
            "Analytics session ownership should be protected in Infrastructure",
            Files.isRegularFile(
                analytics.resolve("infrastructure/security/SessionScopedAnalyticsRepositories.kt")
            ),
        )
    }

    @Test
    fun omissionPushHasRealCqrsPortsAndAnExplicitFirebaseService() {
        val omission = root.resolve("omission")
        for (dir in listOf(
            "domain/model/valueobjects",
            "application/commands",
            "application/queries",
            "application/readmodels",
            "application/ports",
            "application/policies",
            "application/services",
            "infrastructure/push",
            "infrastructure/remote",
            "infrastructure/local",
            "infrastructure/security",
            "presentation/notifications/ui",
            "presentation/notifications/mappers",
        )) {
            assertTrue(
                "Omission boundary missing: $dir",
                Files.isDirectory(omission.resolve(dir)),
            )
        }
        assertTrue(
            "Legacy usecases package must be removed",
            !Files.exists(omission.resolve("application/usecases")),
        )
        assertTrue(
            "Push-only context has no artificial screen or ViewModel",
            !Files.exists(omission.resolve("presentation/screens"))
                && !Files.exists(omission.resolve("presentation/viewmodel")),
        )

        val appManifest = Path.of("src/main/AndroidManifest.xml").toFile().readText()
        assertTrue(
            "Custom FirebaseMessagingService not registered",
            appManifest.contains(
                ".omission.infrastructure.push.OmissionMessagingService"
            ) && appManifest.contains("com.google.firebase.MESSAGING_EVENT"),
        )
        val serviceTag =
            appManifest.substringAfter(
                ".omission.infrastructure.push.OmissionMessagingService"
            ).substringBefore('>')
        assertTrue(
            "Firebase service must not be exported",
            serviceTag.contains("android:exported") && serviceTag.contains("false"),
        )

        val receiver =
            omission.resolve("infrastructure/push/OmissionMessagingService.kt").toFile().readText()
        assertTrue(
            "Firebase adapter must hand off to application before rendering",
            receiver.contains("OmissionPushReceiver")
                && !receiver.contains("OmissionNotifications.show("),
        )

        val dependencies =
            omission.resolve("infrastructure/di/OmissionDependencies.kt").toFile().readText()
        assertTrue(
            "Omission must retain both notification access checks",
            dependencies.contains("accessPolicy.allowsDestination(destination)")
                || dependencies.contains("accessPolicy::allowsDestination"),
        )
        assertTrue(
            "Omission must gate rendering against account policy",
            dependencies.contains("accessPolicy.allows(push)"),
        )

        val app = root.resolve("app/di/AppContainer.kt").toFile().readText()
        assertTrue(
            "Application should delegate notification authorization to Omission",
            app.contains("omission.canDisplay(push)")
                && app.contains("omission.canOpen(destination)"),
        )
    }

    @Test
    fun sharedIsLayeredWithoutBecomingAnArtificialBusinessContext() {
        val shared = root.resolve("shared")
        for (directory in listOf(
            "application/session",
            "common/result",
            "infrastructure/network",
            "infrastructure/security",
            "presentation/designsystem/accessibility",
            "presentation/designsystem/components",
            "presentation/designsystem/theme",
        )) {
            assertTrue(
                "Expected reusable shared boundary $directory",
                Files.isDirectory(shared.resolve(directory)),
            )
        }

        assertTrue("Do not add empty shared/domain", !Files.exists(shared.resolve("domain")))
        assertTrue("Unused domain event bus remains", !Files.exists(shared.resolve("eventing")))
        assertTrue("Unused shared sync implementation remains", !Files.exists(shared.resolve("sync")))

        val bcNames = setOf(
            "analytics", "carelink", "identity", "intake", "inventory",
            "monitoring", "omission", "preferences", "treatment",
        )
        val violations = Files.walk(shared).use { all ->
            all.filter { it.toString().endsWith(".kt") }.toList().flatMap { source ->
                source.toFile().readLines()
                    .filter { line ->
                        line.startsWith("import com.vitahealth.tata.") &&
                            bcNames.any { name ->
                                line.startsWith("import com.vitahealth.tata.$name.")
                            }
                    }
                    .map { line -> "$source: $line" }
            }
        }
        assertTrue("Shared depends on a concrete bounded context: $violations", violations.isEmpty())
    }

    @Test
    fun shellOwnsGlobalNavigationWhileFeaturesKeepTheirInternalRegistrars() {
        val shell = root.resolve("app")
        val host = shell.resolve("navigation/TataNavHost.kt").toFile().readText()
        assertTrue("Host must be injected with composition", host.contains("container: AppContainer"))
        assertTrue("Host must not locate TataApplication", !host.contains("TataApplication"))
        assertTrue("Only one global NavHost", host.contains("NavHost("))
        assertTrue("Global navigation groups missing", Files.isDirectory(
            shell.resolve("navigation/graphs")
        ))
        assertTrue("Root route contract missing", Files.isRegularFile(
            shell.resolve("navigation/destinations/RootDestination.kt")
        ))
        val graphViolations = Files.list(shell.resolve("navigation/graphs")).use { paths ->
            paths.filter { it.toString().endsWith(".kt") }.toList().filter {
                val source = it.toFile().readText()
                source.contains("TataApplication") ||
                    source.contains("LocalContext.current.applicationContext")
            }
        }
        assertTrue("Host graphs read the global Application: $graphViolations", graphViolations.isEmpty())

        for ((owner, registrar) in listOf(
            "inventory" to "InventoryNavigationGraph.kt",
            "analytics" to "AnalyticsNavigationGraph.kt",
            "preferences" to "PreferencesNavigationGraph.kt",
        )) {
            val path = root.resolve(owner).resolve("presentation/navigation").resolve(registrar)
            assertTrue("$owner does not own its internal navigation registrar", Files.isRegularFile(path))
            assertTrue(
                "$owner registrar depends on app internals",
                !path.toFile().readText().contains("com.vitahealth.tata.app."),
            )
        }

        val container = shell.resolve("di/AppContainer.kt").toFile().readText()
        assertTrue("Omission assembly must remain in its own BC", Files.isRegularFile(
            root.resolve("omission/infrastructure/di/OmissionDependencies.kt")
        ))
        assertTrue("Treatment-Intake integration must be explicit", Files.isRegularFile(
            shell.resolve("integration/medicationoverview/MedicationOverviewIntegration.kt")
        ))
        assertTrue("Monitoring-Omission push bridge missing", Files.isRegularFile(
            shell.resolve("integration/pushnavigation/PushAlertResolver.kt")
        ))
        assertTrue(
            "AppContainer still constructs Firebase adapters",
            !container.contains("FirebaseTopicSubscriptions(")
                && !container.contains("FirebaseDevicePushTokenSource(")
                && !container.contains("RemoteDeviceTokenRegistry("),
        )
    }

    private fun kotlinFiles(): List<Path> {
        assertTrue("App source root is missing", Files.isDirectory(root))
        return Files.walk(root).use { paths ->
            paths.filter { it.toString().endsWith(".kt") }.toList()
        }
    }
}
