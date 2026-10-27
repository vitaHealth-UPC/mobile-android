## Estado vigente: 26-10-2026

La matriz `docs/ux/figma-visual-validation.md` registra 70/70 variantes
implementadas y aceptadas con fidelidad proporcional. La aceptación visual
no certifica funcionalidad completa ni integración con el backend desplegado.
Las retiradas de aceptación del 25-10 corresponden a una revisión anterior;
las comparaciones posteriores y sus capturas están registradas en la matriz.

Android conserva 847 pruebas JVM verificadas, sin fallos ni errores.
El backend normalizado se trabaja en
`C:/Users/david/IdeaProjects/web-services-architecture`, rama
`fix/backend-architecture`; la carpeta original `web-services` no es la fuente
actual para contrastar estos cambios.

Se añadió la consulta de cronología de alertas que Android ya consumía:
`GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/timeline`.
Devuelve programación, generación y cierre cuando existen en el registro;
no infiere entregas de recordatorios. La suite HTTP de Monitoring pasa
14 pruebas, incluidas sesión obligatoria, alerta inexistente y consentimiento
revocado. Evidencia local: `tata-alert-timeline-security-20261026.log` en
`C:/Users/david/.codex/tmp`. Falta verificar esta ruta contra el despliegue.

### Recuperación: formulario conectado al acceso

ForgotPassword abre PasswordRecoveryDialog con estado propio y adaptador anónimo
compuesto en IdentityDependencies. Solicita correo, luego código y nueva contraseña;
204 muestra confirmación y vuelve al acceso sin login automático. Cancelar reinicia
el estado salvo durante una operación en curso. Recursos ES/EN, entrada desplazable,
contraseña oculta y errores específicos. Gate `tata-recovery-ui-final-20261026.log`:
producción, AndroidTest y suite JVM pasan. Falta prueba instrumentada y recorrido
Compose-Spring de recuperación, además de entrega de correo externo.
Google/Facebook todavía no están conectados al botón.

### Recuperación: HTTP y estado Android

Backend expone POST password-recovery-requests (202 uniforme) y password-recoveries
(204, sin sesión automática). Gate `tata-recovery-api-localized-20261026.log`:
7 pruebas seleccionadas pasan; corrige la clave i18n al formato lower-hyphen del
resolver. La prueba previa detectó ese defecto y no se considera gate verde.

Android implementa PasswordRecovery/RemotePasswordRecovery y
PasswordRecoveryViewModel. Gate `tata-recovery-viewmodel-20261026.log` pasa las
pruebas seleccionadas de contrato y estado: avance solo tras recibo, rechazo de
código sin cierre del formulario, limpieza de contraseña tras error y limpieza
completa de credenciales tras éxito. Falta formulario Compose, composición DI y
callback desde el acceso. No considerar el recorrido completo todavía.

### Correo de recuperación: cola conectada

La cola cifrada existente incorpora propósito ACCOUNT_VERIFICATION/PASSWORD_RECOVERY
(migración V20). El dispatcher valida la evidencia correspondiente y cancela retos
de recuperación usados, reemplazados, bloqueados o vencidos. Resend recibe una
plantilla de recuperación y prefijo de idempotencia propios. Se conserva la
verificación de registro. Gate `tata-recovery-delivery-20261026.log`: 30 pruebas
seleccionadas pasan, incluyendo regresión de cola/dispatcher/protocolo Resend.
Los tests no enviaron correo a destinatarios reales. Faltan solicitud HTTP y UI.

### Recuperación de contraseña: dominio y persistencia

Backend fix/backend-architecture incorpora PasswordRecoveryChallenge, separado
respecto de VerificationChallenge. Define expiración exclusiva, cinco intentos
fallidos por código y consumo único; conserva únicamente evidencia codificada y
redacta diagnósticos. Cuatro pruebas pasan en
`tata-password-recovery-domain-20261026.log` (BUILD SUCCESS).

PasswordRecoveryRepository y su adaptador JPA ya almacenan el reto separado del
alta. Las mutaciones requieren transacción externa y bloquean cuenta antes del
reto, incluso para la primera emisión. La migración V19 define PK/FK y restricciones
de intentos, vencimiento y consumo. Gate `tata-password-recovery-persistence-final-20261026.log`:
10 pruebas seleccionadas pasan, incluidas emisión/consumo concurrentes, rollback,
lectura posterior y ejecución del SQL en H2 con modo PostgreSQL. No certifica
la migración ejecutada en un PostgreSQL desplegado.

CompletePasswordRecoveryCommandHandler ya cambia el hash, consume el reto y revoca
las sesiones cuidadoras de esa cuenta en una transacción. Conserva la política de
contraseñas del registro y no emite una sesión nueva. Los fallos de código persisten
sus intentos sin cambiar contraseña; un fallo de revocación revierte las tres
operaciones. Las sesiones ajenas y las posteriores a un replay no se revocan.
Gate `tata-password-recovery-completion-20261026.log`: 20 pruebas seleccionadas,
cero fallos, errores o saltos, con dominio, persistencia, migración, cuenta y
completado de recuperación. No es una nueva suite completa de todo el backend.

El flujo NO está conectado: faltan solicitud y confirmación HTTP con respuesta
uniforme para correos existentes/inexistentes, entrega cifrada y formulario Android.
Antes de exponer los endpoints, igualar también el trabajo de verificación de
códigos en cuentas/retos inexistentes para reducir diferencias observables.
Consumo, cambio de contraseña y revocación deben confirmarse en una transacción;
el modelo de dominio por sí solo no acredita estas garantías ni un envío real.

El usuario agregó la configuración Google válida. Facebook sigue pendiente de configuración. No sustituir los botones por accesos simulados ni almacenar secretos en código.

### Identity: rechazo de autenticación visible

Se corrigió SessionAccessViewModel: AUTHENTICATION_REQUIRED solo se silencia al
restaurar una sesión inexistente. Cuando el usuario pulsa acceso por correo o PIN,
el rechazo se muestra mediante un recurso localizado ES/EN y no publica sujeto.
Dos pruebas reprodujeron el silencio anterior; ambas pasan tras la corrección,
junto con el caso de restauración silenciosa existente. El mapeo del mensaje tiene
su propia prueba. No se modificaron proveedores externos ni la autenticación real.
Gate `tata-access-required-final-20261026.log`: 847 JVM / 163 suites sin fallos,
errores o saltos; producción y AndroidTest compilan. El cambio no añade otra
aceptación Figma ni acredita el despliegue remoto.

### Identity: alcance funcional pendiente comprobado

El acceso por correo/contraseña y el registro/verificación llaman al backend.
Los formularios parten vacíos. La opción demo rellena credenciales públicas al
pulsarla y utiliza el mismo SignInCommandHandler; no concede acceso localmente.
Su disponibilidad efectiva depende de que el servidor tenga esa cuenta habilitada.

Google y Facebook solo modifican el estado `information` en SessionLoginScreen
y muestran un diálogo. No hay intercambio OAuth ni validación de tokens de esos
proveedores en los contratos actuales de Identity. Recuperar contraseña muestra
una indicación de ayuda; no existe un flujo de restablecimiento conectado.
Son faltas de implementación, además de la configuración externa necesaria;
no se resuelven solo desplegando los cambios locales ni reconectando Firebase.
El usuario agregó app/google-services.json y se verificó el paquete com.vitahealth.tata, los clientes OAuth Android/Web y processDebugGoogleServices. El archivo está excluido de Git; falta implementar la autenticación social.

La aceptación local de los recorridos probados y las 70 variantes visuales no
acredita estas tres opciones. Conservar esta distinción en el informe final.

### Integración efectiva con el backend local

La comprobación `:app:verifyLocalBackendContracts` utiliza los adaptadores Android
contra Spring/JPA/H2 por HTTP real en localhost. Verifica escritura y lectura
persistida de contacto sin cambiar los datos básicos, lectura de cronología,
retiro, invalidación del token adulto (401) y bloqueo del seguimiento cuidador (403).
Evidencia: `tata-live-backend-android-contracts-final-20261026.log`.
Procedimiento: `docs/testing/local-backend-contracts.md`.
Servidor y credenciales temporales se cierran al terminar. No acredita Render,
PostgreSQL, FCM ni la ruta completa Compose-servidor; la UI y composición de sesión
tienen sus propias pruebas. Este resultado supera la falta de servidor vivo local
que indican las entradas anteriores, sin modificar la limitación de despliegue.

### Retiro de consentimiento: flujo conectado

La acción aparece al final del inicio del adulto, junto al cambio de cuenta.
CareLink presenta una confirmación nativa localizada ES/EN y su ViewModel evita
escrituras al cancelar o duplicar confirmaciones. El host compone retiro,
limpieza de notificaciones y cierre de sesión; comprueba la sesión capturada
antes y después de operaciones suspendidas. El adaptador resuelve careLinkId
mediante identidad autenticada y solo acepta 204.

Gate `tata-consent-withdrawal-final-gate-20261026.log`: producción, AndroidTest
y 844 JVM / 163 suites, cero fallos y errores. La suite nueva prueba cancelación,
confirmación duplicada, error y cambio de cuenta durante retiro o limpieza.
Ambos APK fueron generados en `tata-consent-withdrawal-ui-gate-20261026.log`.
Dispositivo: `tata-consent-withdrawal-device-final-20261026.log`, OK (1 test),
8.255 segundos. Cancelar cero escrituras y cero salidas; confirmar una escritura
y una salida, mediante puerto de fixture. Captura del diálogo inspeccionada:
`C:/Users/david/.codex/tmp/tata-consent-withdrawal-20261026.png`.
Se corrigió la resolución de textos antes de crear la ventana nativa para
conservar el idioma de la pantalla. Los intentos anteriores de arranque y
localización fallaron y quedan superados por este caso verificado.
Falta verificación de retiro y cierre contra servidor vivo.

### Contacto de emergencia del perfil

Implementado en Persona vinculada > Editar > Editar contacto de emergencia.
Comando y puerto CareLink propios, adaptador HTTP con credencial capturada,
rechazo de recibos de otro adulto o con campos distintos, recarga tras éxito y
cancelación de escritura tardía al perder acceso. No modifica la política de
notificaciones ni los datos básicos del adulto.

Gate `tata-emergency-contact-final-20261026.log`: producción, AndroidTest,
ambos APK y 838 pruebas JVM / 160 suites, cero fallos y errores.
Instrumentación directa `tata-contact-device-20261026.log`: OK (1 test),
23.914 segundos; cancelar cero escrituras y guardar una escritura mediante
puerto de prueba. Captura inspeccionada:
`C:/Users/david/.codex/tmp/tata-linked-emergency-contact-20261026.png`.
La prueba de interfaz usa fixture; no confirma persistencia en Render.
La suite HTTP con MockWebServer comprueba ruta, token, recibo válido,
recibo ajeno, 403 y ausencia de sesión. Falta integración con servidor vivo.

# Handoff para Codex — Tata Android: funcionalidad y fidelidad Figma

**Estado:** refactorización arquitectónica por BC y transversal aplicada; no es
certificación funcional, visual, de accesibilidad ni de seguridad extremo a extremo.
**Proyecto:** `C:/Users/david/StudioProjects/mobile-android`.
**Backend de comparación:** `C:/Users/david/IdeaProjects/web-services`.
**Rama de trabajo:** `fix/android-architecture`, sin commits adicionales.
**Plan histórico único:** `C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
**ADRs:** `docs/adr/ADR-001-bounded-context-modularization.md` y
`docs/adr/ADR-002-application-shell-and-shared.md`.

## 1. Decisiones que NO deben revertirse

1. **Un solo Gradle `:app`.** Los nueve bounded contexts son paquetes
   `analytics`, `carelink`, `identity`, `intake`, `inventory`, `monitoring`,
   `omission`, `preferences` y `treatment`. Ningún BC depende del código
   privado de otro BC. `shared` no importa BCs; `app` es composición y
   coordinación entre BCs. No crear nuevos módulos Gradle.
2. **Cuatro capas lógicas por BC:** `domain` (reglas, entidades/value objects),
   `application` (CQRS, puertos, readmodels), `infrastructure`
   (HTTP/DTO/Room/DataStore/FCM/DI/adaptadores), `presentation`
   (Route, Screen, ViewModel/UiState, UI components, mappers, navegación
   específica cuando se justifica). La lámina Android de tres columnas
   expresa otra abstracción: Presentation ↔ Presentation, Domain logic ↔
   Domain + Application, Data ↔ Infrastructure. No generar paquetes vacíos.
3. **CQRS semántico:** Commands/Handlers para escrituras, Queries/Handlers
   para lecturas. Un repository contractual que entrega una entidad de
   dominio pertenece a `domain/repositories`; los lectores de proyecciones y
   capacidades externas a `application/ports`. El cliente no es autoridad
   para reglas transaccionales/permiso/concurrencia del backend.
4. **Presentación por flujo:** cada flujo con código real agrupa `screens/`,
   `viewmodel/`, `ui/` y `mappers/`; `*Route.kt` se ubica en raíz.
   Un Flow sin mappers, gráficos o navegación no necesita esa carpeta.
   No crear ni modificar diseños Figma validados por razones de arquitectura.
5. **Navegación:** `app/navigation/TataNavHost.kt` es el único host global;
   `app/navigation/destinations/RootDestination.kt` conserva rutas codificadas;
   `app/navigation/graphs` contiene adaptadores del host que conectan BCs y
   callbacks/rutas globales (pueden ser voluminosos, **no son lógica privada
   de los BC**). Grafos de destino autocontenidos, como Analytics, Inventory
   y Preferences, permanecen en `<bc>/presentation/navigation`, sin importar
   `app`. No mover un adaptador de host a un BC si aún depende de
   `AppContainer` o `RootDestination`: primero diseñar una interfaz de
   fábrica/callbacks y mantener la dirección de dependencias.
6. **Composición:** `app/di/AppContainer.kt` solo compone contratos/fábricas
   y delega. `omission/infrastructure/di/OmissionDependencies.kt` crea
   tokens, topics, registro de dispositivos y protege notificaciones.
   `app/integration/medicationoverview` une resultados Treatment + Intake;
   `app/integration/pushnavigation` resuelve consultas Monitoring
   para taps de Omission. Las demás fábricas están en `<bc>/infrastructure/di`.
   Los grafos reciben `AppContainer` explícito del host; no buscar
   `TataApplication` desde Compose.
7. **Shared ≠ bounded context de negocio:** la organización actual es
   `shared/application/session`, `shared/common/result`,
   `shared/infrastructure/{network,security}` y
   `shared/presentation/designsystem/{accessibility,components,theme}`.
   Contratos de sesión no guardan datos; `EncryptedSessionStore` y
   `NoSessionStore` son adaptadores de Infrastructure.
   Sin carpeta `shared/domain` vacía ni eventos locales simulados.
   `SyncScheduler` pertenece a `intake/application/ports`.
   Nunca cambiar paleta, tipografías, ratios ni dimensiones del
   design system sin cotejo exacto con Figma.
8. **Seguridad/estado de sesión:** cambios de cuenta, expiración,
   permisos y pertenencia del adulto son límites de acceso.
   No omitir partición Room/DataStore/SharedPreferences por usuario,
   descarte de respuestas antiguas ni desregistro de tokens.
   El cliente solo ofrece defensas adicionales; el servidor manda.
9. **Git:** conservar estado e índice actuales; el usuario prevé
   **un único commit** al completar revisión. No stage, commit, push,
   reset, revert ni limpiar trabajo ajeno sin una instrucción expresa.

## 2. Decisiones por los nueve bounded contexts

| BC | Responsabilidad y organización actual | Al verificar funcionalidad |
| --- | --- | --- |
| `identity` — Identity & Subscription | Identidad, registro, sesión/PIN, suscripción, onboarding; Commands/Queries y flujos Compose separados; permisos y almacenamiento de sesión detrás de interfaces. | Login, signup, PIN, validaciones, restauración/expiración, onboarding y decisiones de roles. |
| `carelink` — Care Link | Vinculación, código, consentimiento, perfiles relacionados; contratos de dominio, handlers, adaptadores con sesión y rutas de cuidador. | Invitaciones, aceptación/rechazo, revocación del vínculo, datos de adulto autorizados y back stacks. |
| `treatment` — Treatment Management | Medicamentos, alta/edición, tratamiento, régimen, frecuencia, instrucciones, recordatorios y lifecycle. Transiciones definitivas en backend; cada paso Compose tiene sus carpetas. | Wizard completo sin perder estado, payloads y validaciones por paso, detalle/lista, edición y activación; versiones Figma restantes. |
| `intake` — Intake Execution | Agenda, confirmación de tomas, voz, recuperación offline, Room y replay; `SyncScheduler` en su puerto de aplicación. | Idempotencia, concurrencia, offline/reconexión, DST y zonas horarias, expiración/revocación, voz, confirmación y Home. |
| `inventory` — Inventory & Replenishment | Agregados/valores de inventario cuando son reales, Commands/Queries, respuesta remota/mapper y presentación por flujo. Stock definitivo en backend. | Stock disponible, lotes, reposición, errores 409 y sincronía con la toma; todas las variantes Figma pendientes. |
| `monitoring` — Family Monitoring | Resumen familiar, alertas (lista y detalle), seguimiento, notas personales y contacto; Commands/Queries, proyecciones, adaptadores `security`, flujos Compose. | Autorización del cuidador, 401/403/409, estado de alerta, vínculo revocado, notas, accesibilidad y acciones de contacto. |
| `omission` — Omission & Escalation | Push nativo y navegación por intent; datos FCM, registro/baja, topics, callback y políticas; **no tiene pantallas ni ViewModel Compose**; backend produce y escala omisiones. | Entrega data FCM en foreground/background/proceso muerto, permiso Android 13+, registro/baja con usuarios distintos, push duplicado/expirado, links a Monitoring o Intake. **Pendiente crítico:** desuscripción/transporte para topics FCM legacy tras cambio de cuenta. |
| `preferences` — Accessibility & Preferences | `accessibility` y `notifications` por flujo; comandos/consultas, DataStore local, `sync` offline-first, `security` por propietario; grafo feature propio. | Persistencia, tamaño de texto, alto contraste, lectura asistida, reducción de movimiento, horarios silenciosos DST, notificaciones/escala y cambios de usuario. |
| `analytics` — Adherence Analytics | **Solo Queries**, proyecciones/readmodels sin comandos vacíos; historial, patrones, gráficas Canvas, recomendaciones y pantalla de evidencia insuficiente; sesión protegida, grafo interno. | Cálculos y respuestas reales sin proyecciones inventadas, rangos 1–31 días, vacíos, accesibilidad y localización de gráficas, casos de error. |

## 3. La tarea concreta a realizar ahora

No repetir la normalización arquitectónica ni aprobar pantallas únicamente
porque existe un Composable. Realizar una **auditoría funcional y visual de
extremo a extremo**, en este orden:

1. **Inventario de pantallas y Figma.** Enumerar todas las rutas,
   `screens/`, `Route`, estados de UI y frames de diseño disponibles.
   Para cada pantalla, indicar `aprobada`, `por contrastar`,
   `falta implementar` o `requiere corrección`, sin inferir aprobaciones.
   Separar las variantes: carga, vacío, error, datos, validaciones,
   confirmación, permisos, roles, accesibilidad, dispositivo pequeño.
2. **Fidelidad visual concreta.** Contrastar capturas reales/emulador
   con frames Figma por resolución y estado equivalente. Inspeccionar
   jerarquía visual, tipografía, tamaños, márgenes, grids, tarjetas,
   iconos y activos, consistencia de colores, truncamiento, overflow,
   touch targets y navegación entre vistas. Respetar componentes del
   design system; no transformar por estilo ni sustituir pantallas
   existentes por dashboards genéricos.
3. **Cobertura funcional.** Trazar cada pantalla a Query/Command,
   ViewModel, puerto, DTO, endpoint de `web-services` y respuesta del
   backend. Verificar UI state, lifecycle, loading/empty/error,
   reintentos, permisos, flujos felices, fallas HTTP y back stack.
   Nunca fabricar datos de producción para que una pantalla se vea llena.
4. **Integración y seguridad.** Probar roles cuidador/adulto, cambios
   de usuario, expiración de token, solicitudes obsoletas, consentimiento,
   notificaciones push, suscripciones FCM heredadas, almacenamiento
   local aislado y sincronización. Priorizar vulnerabilidades del
   transporte y evitar exponer alertas a una cuenta previa.
5. **Pruebas ejecutables.** Primero JVM + compilación Kotlin de
   androidTest, luego tests instrumentados en emulador/dispositivo
   cuando exista conexión, y luego capturas de Figma. Documentar
   PASS/FAIL/BLOCKED con logs y evidencia; compilar androidTest
   **no** ejecuta la instrumentación.
6. **Correcciones enfocadas.** Implementar pantallas faltantes y
   corregir comportamientos/estados visuales no conformes;
   no deshacer decisiones de dominio, CQRS, puertos, carpetas,
   seguridad o sesión sin aportar evidencia y registrar ADR.
   Mantener código Kotlin idiomático, imports ordenados, separación
   por responsabilidades, espacios/line breaks, KDoc solo cuando
   añade contrato o invariante relevante.
7. **Entregable final.** Tabla `BC → flujo → pantalla/Figma →
   estado funcional → fidelidad visual → evidencia → pendiente`;
   lista priorizada de bugs con severidad; suites verificadas;
   hallazgos backend bloqueantes; cambios hechos y pendientes que
   no pueden verificarse sin un dispositivo/cuenta real.

**Último gate arquitectónico registrado, 2026-10-24:** 747 tests
JVM en 140 suites, 0 fallos y errores. Compilación de producción y
AndroidTest correcta; `git diff --check` = 0. Este conteo es la línea
base anterior a las tareas funcionales de Codex; no es prueba visual
ni ejecución de instrumentadas. Log:
`C:/Users/david/.codex/tmp/tata-transversal-final-verified-20261024.log`.

## 4. Gate de aceptación que debe volver a correrse

`gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --console=plain`

Además: `git diff --check`, sin imports antiguos ni dependencias
privadas entre BC, y comprobación del `AndroidManifest.xml` fusionado
con `OmissionMessagingService`.

Registrar conteos de suites/fallos nuevamente: **no reutilizar 745
como prueba actual** sin ejecutar el gate después de futuras ediciones.

## 5. Riesgos, bloqueos y disciplina

- Contrastar las guías del curso APMO y la implementación de
  `web-services` cuando el ownership no esté claro.
- Un diseño en Figma no prueba comportamiento y una suite JVM no
  prueba apariencia o entrega FCM real.
- No asumir que el otro equipo debe tener idéntica estructura:
  su backend usa `shared/domain/application/infrastructure/interfaces`;
  el Android necesita `presentation/designsystem` y un host de Compose.
- No prometer validaciones en dispositivo sin haberlas ejecutado.
- El proyecto ya tiene cambios acumulados (incluidos cambios en el
  índice) que deben mantenerse para el único commit final.


## Auditoría independiente de Figma y contratos (2026-10-25)

Antes de modificar Intake Home, Dose Detail o Inventory, revisar
`docs/architecture/INDEPENDENT-FIGMA-BACKEND-VALIDATION-2026-10-25.md`.
Contiene el contraste Figma 563:60 ↔ Compose ↔ API de Intake, la corrección
comprobada de «Última reposición» en Inventory, 799 tests JVM y tres pruebas
instrumentadas exitosas con contexto es-419. La fidelidad visual permanece
bloqueada por diálogos ANR de System UI/Pixel Launcher en el AVD.
No revertir las correcciones ni aceptar capturas contaminadas.


## ARCHIVADO: progreso visual parcial 50/70 (no aprobado)

Consultar `docs/ux/figma-visual-validation.md`: 50/70 referencias aceptadas visualmente (71,4 %), 15 pendientes implementadas y cinco sin implementación específica. Las últimas aprobaciones son 46 y 61–64, con evidencia y matices en `docs/architecture/INDEPENDENT-FIGMA-BACKEND-VALIDATION-2026-10-25.md`. No confundir aceptación visual con pruebas backend E2E. El rerun de captura de Accesibilidad falló por PixelCopy del AVD; sus interacciones/persistencia sí pasaron y existen PNG válidos anteriores.


## ARCHIVADO: Monitoring 54/70 (no aprobado)

Visual: 54/70 (77,1 %). Monitoring 7/8 (87,5 %), pendiente
Notas del cuidador (27). Las referencias 18, 56, 57 y 58
tienen capturas espanolas reales y 4 tests instrumentados.
Consultar docs/ux/figma-visual-validation.md y el informe
INDEPENDENT-FIGMA-BACKEND-VALIDATION-2026-10-25.md antes
de rehacer la presentacion o inventar recordatorios.


## Continuación: Monitoring y Notes (#27), 2026-10-25

El avance **no incrementa las 45/70 aprobaciones exhaustivas**
conservadas por Codex. Se corrigió tipografía únicamente en
AlertDetailScreen y NotesScreen mediante
`TataInterFontFamily` (Inter 4.001 reales 400/500/600/700),
sin reemplazar el Regular anterior ni el tema global.
Los tests JVM pasaron: **801/801**, 153 suites.

En #27 se añadió la tarjeta de la persona vinculada con
`MonitoredPersonReader` remoto, a través de la sesión existente.
El flujo lee notas autorizadas antes de pedir el perfil;
la denegación elimina del estado nombre, notas y editor.
`NotesPersonCard.kt` no emplea foto estática de Figma
ni inventa edad si falta nacimiento. No se inventan títulos
de notas ni autores por nombre: el contrato solo da texto,
`familiarId` e instante de registro.

La captura Compose ya fue generada en
`C:\Users\david\.codex\tmp\tata-treatment-monitoring-audit-20261025\android-notes-linked-20261025.png`.
La primera suite instrumentada del perfil pasó **2/2**.
La segunda pasada con la última tipografía sufrió
`ShellCommandUnresponsiveException` del AVD; revisar resultado
definitivo antes de afirmar que el screenshot final pasó.
Consultar informe `INDEPENDENT-FIGMA-BACKEND-VALIDATION-2026-10-25.md`
para detalles y limitaciones.

**No aprobado todavía:** la referencia #27 carece de
comparación final de cada asset y los datos dinámicos del
servidor desplegado no fueron comprobados con una sesión
autorizada. Considerar reforzar el contrato directo
`GET /api/v1/older-adults/{id}` para acceso por relación:
el endpoint genérico no recibe `caregiverId`; la ruta actual
comprueba el vínculo en el GET de notas previo, pero no
convierte eso en autorización de cualquier llamada directa
al perfil.


## 2026-10-25 — Monitoring: tema oscuro y validación

Conteo Figma vigente: 50/70 (71,4 %); no se incrementa por
migraciones oscuras que no han pasado auditoría Figma + backend real.

**Trabajo local realizado:** Resumen familiar, Notas personales,
Alertas y componentes de Monitoring ahora adaptan sus fondos,
gradientes, bordes, estados, iconos y textos a tema oscuro.
El modo claro conserva sus tonalidades originales (incluyendo
el fondo #FBFBFE de notas) y los SVG originales de Figma.
No se modificaron navegación, dominio, CQRS, endpoints ni repositorios.
Solo se usan datos ficticios dentro de AndroidTest.

**Evidencia previa a última corrección:** 806 pruebas JVM / 153
suites sin fallos y 7/7 casos instrumentados reales mediante
AndroidJUnitRunner directo en el AVD. El wrapper Gradle con
class filter informó BUILD SUCCESSFUL pero ejecutó cero tests;
por tanto no sirve como gate. El runner manual sí ejecutó siete
casos y produjo ocho PNG reales:
C:\Users\david\.codex\tmp\tata-monitoring-dark-audit-20261025\
Log: C:\Users\david\.codex\tmp\tata-monitoring-dark-adb-runner-20261025.log

**Defecto detectado en capturas:** AlertTimelineSummary todavía
tenía fondo melocotón claro y texto blanco en oscuro; ya fue
corregido con gradiente de superficie oscura. También se
adaptaron AlertDoseCard y el SVG de campana por tint solo en oscuro.
Es necesario confirmar la compilación final y tomar una nueva
captura del listado tras la corrección. Gate:
C:\Users\david\.codex\tmp\tata-monitoring-alert-contrast-final-20261025.log

Backend: los 281 tests existentes de Codex son la evidencia previa,
no una conexión Android autenticada contra el servidor desplegado.
No se usaron cuentas reales. Mantener rama fix/android-architecture,
198 rutas previamente staged y sin commit ni push.


### 2026-10-25 — Monitoring dark surfaces: 7/7 instrumentadas

Contador Figma CONFIRMADO: 50/70 (71,4 %), sin incrementar por
compatibilidad con tema oscuro. Permanecen 15 variantes
implementadas por aceptar y cinco sin implementación específica.

Se completaron superficies adaptativas en el Resumen familiar,
Notas personales, componentes de Alertas y utilidades UI de
Monitoring. La ilustración original y el glow se mantienen
en modo claro; en modo oscuro se emplea un indicador circular
calculado con summary.weekly.percentage del backend y el botón
de alertas con fondo adaptativo. No se inventaron estadísticas.
El pixel de la captura clara de Resumen familiar antes/después
es idéntico (1080 x 2400). Revisadas capturas reales en AVD.

Evidencias: C:/Users/david/.codex/tmp/tata-monitoring-dark-review-20261025/
incluye family-summary-dark-final.png, family-summary-light-post.png,
personal-notes-light.png, personal-notes-dark.png y family-consent-dark.png.

Código: FamilySummaryScreen.kt, SummaryComponents.kt,
PersonalNotesScreen.kt, PersonalNoteComponents.kt y AlertsComponents.kt.
Se añadió MonitoringDarkSurfaceRegressionTest.kt.

Pruebas JVM: 806/806, 153 suites sin fallos.
Log: C:/Users/david/.codex/tmp/tata-monitoring-dark-final-gate-20261025.log.
Un paso Gradle connected generó XML vacío (0 testcases), por lo
que NO se contó como instrumentación aprobada. Después se instalaron
manualmente los APKs debug y androidTest en emulator-5554 y se ejecutó
el runner AndroidJUnitRunner filtrado por MonitoringDarkSurfaceRegressionTest:
OK (7 tests), exit 0. Log:
C:/Users/david/.codex/tmp/tata-monitoring-dark-actions-device-20261025.log.

Cobertura: resumen con datos en claro/oscuro, notas pobladas
en claro/oscuro, denegación de relación que oculta estadísticas,
callback de navegación a Agenda y callback de agregar nota.
Los datos de las pruebas viven en androidTest; no certifican
el backend desplegado ni sesiones reales. Backend 281 pruebas
completadas previamente por Codex, sin cambios en esta revisión.

Siguiente prioridad: pruebas de superficies pobladas oscuras de
los BC restantes, contraste de accesibilidad y verificación
de integraciones reales por cuenta y permisos. Conservar 198
rutas en el índice; sin commits ni push.


### 2026-10-25 — Treatment and Monitoring final dark contrast checkpoint

Following inspection of the populated AVD images, verified and
corrected dark-theme-only contrast in Treatment and Monitoring.
Affected production files: MedicationRegistrationScreen.kt,
TreatmentCreationScreen.kt, TreatmentDoseFrequencyScreen.kt,
TreatmentReminderScreen.kt, TreatmentScheduleInstructionsScreen.kt
and NotesPersonCard.kt. Fixes: true text token for a selected
medication frequency instead of light-only navy/black; genuine
theme foreground/background for all four treatment steppers;
accent-tinted medication icon; legible mint success text for the
linked-person Activa badge. Light Figma colors are preserved by
the token functions in these branches; no backend, CQRS, repository,
navigation contract or design asset was altered.

Android final compile/assemble/JVM gate:
C:/Users/david/.codex/tmp/tata-treatment-dark-contrast-final-icon-20261025.log
BUILD SUCCESSFUL; JVM 806 tests passed.
Explicit emulator instrumentation (installed build, real Compose
screens, test-only fixture data):
C:/Users/david/.codex/tmp/tata-treatment-dark-contrast-final-device-20261025.log
OK (3 tests). Tested populated medication form, treatment creation,
caregiver notes with linked profile. Captures for close visual audit:
C:/Users/david/.codex/tmp/tata-monitoring-dark-review-20261025/
tata-final-medication-form-dark.png,
tata-final-treatment-create-dark.png,
tata-final-notes-with-profile-dark.png
(pre-contrast comparison files are in the same folder).
These are dark-mode compatibility gates, NOT new Figma approvals;
the canonical count remains 50/70. Backend was not modified or
authenticated against a live deployment in this checkpoint.
Pending: dark coverage for the other Treatment steps and release
readiness across remaining BCs, plus independent Figma checks.


### 2026-10-25 — Treatment wizard and Inventory dark-theme regression

**Figma acceptance counter remains 50/70 (71.4 %).** Dark-mode
and functional regression gates are not new Figma approvals.

#### Treatment: all four steps audited

TreatmentWizardDarkFlowTest.kt covers dosage/frequency,
schedule/instructions, reminders, draft review/activation and actual
continue/activate callbacks. Initial direct AVD run: 8/8 PASS.
Visual inspection found two light-only surfaces in the final review:
the stepper and agenda preview. Adjusted only the review UI in
treatment/presentation/treatment/ui/ReviewTreatment.kt, making the
stepper, markers, medication icon and agenda preview theme adaptive.
Original Figma SVGs and pastel gradients are preserved in light mode.

Final build and 806 JVM tests: PASS, zero failures.
Final direct AndroidJUnitRunner: 8/8 PASS.
Light review before and after: pixel-identical at 1080x2400.
Dark review after the change: legible and without white/light panels.
Captures: C:/Users/david/.codex/tmp/tata-treatment-wizard-review-20261025/
Logs: C:/Users/david/.codex/tmp/tata-review-dark-final-build-20261025.log
and C:/Users/david/.codex/tmp/tata-review-dark-final-device-20261025.log

#### Inventory: four presentation files migrated together

Updated InventoryScreen.kt, InventoryWidgets.kt,
InventoryStockCards.kt and InventoryFormBodies.kt under
inventory/presentation/inventory. The inventory screen,
forms, stock/last-batch cards, badge, action buttons and errors
now use appropriate shared dark tokens, preserving Figma light values.
The ready-stock LIGHT screen before/after is pixel-identical at
1080x2400. Dark screenshots show readable states.

InventoryDarkFlowTest.kt uses production Compose screens with
test-only fixtures. Coverage: stock ready dark/light, missing stock,
access denied hides data/forms, save callback, duplicate-save
prevention and actual latest recorded replenishment.
Final direct AndroidJUnitRunner: 7/7 PASS.
Tests confirmed existing inventory rule: the first batch is initial
stock, NOT a replenishment; last replenishment appears only once
a later batch exists. No domain alteration was needed.

Captures:
C:/Users/david/.codex/tmp/tata-inventory-dark-review-20261025/
including final-ready-batch-dark.png and baseline light comparison.
Logs:
C:/Users/david/.codex/tmp/tata-inventory-dark-final-build-20261025.log
C:/Users/david/.codex/tmp/tata-inventory-dark-batch-final-20261025.log

These tests do not authenticate against a deployed backend,
nor constitute complete Figma-asset validation. No business logic,
DDD/CQRS handlers, repositories or remote backend code were changed.
Continue with remaining dark-theme BCs and independent Figma acceptance.
Preserve 198 staged routes; no commits or push.


### 2026-10-25 — Aceptacion independiente de Accesibilidad 14/61–64

Cinco frames consultados directamente en Figma (archivo
jCppvxtSpLpHOrC3ZWUIVC): 563:618, 563:4638, 563:4751,
563:4864 y 563:4977. Se compararon composicion, secciones, seis
interruptores, seis recursos SVG originales (todos presentes y
no vacios en res/raw), fuente serif del titulo, Inter,
barra adulta y avisos de estados.

Capturador instrumentado:
C:/Users/david/.codex/tmp/tata-figma-access-recapture-20261025.log
AndroidJUnitRunner OK (1 test) despues de reintentar un error
temporal de attach que no se conto como exito.
Capturas actuales (base, large, large-feedback, contrast, motion,
reading, reading-feedback):
C:/Users/david/.codex/tmp/tata-figma-acceptance-accessibility-20261025/

Validaciones funcionales separadas:
- AccessibilityScreenTest: OK (2 tests), seis switches, bloqueo
  en guardado, accesibilidad de barra inferior y callback Agenda.
- DataStoreAccessibilityLocalStoreTest: OK (6 tests),
  persistencia local y aislamiento por propietario/sesion.
- Build Gradle y 806 JVM tests: BUILD SUCCESSFUL, cero fallos.
- El backend para tema oscuro se habia validado previamente,
  pero no se hizo un recorrido autenticado extremo a extremo
  con servidor remoto en esta aceptacion.

Criterio proporcional: frames Figma de los 5 estados muestran
combinaciones de switches encendidos que no representan exactamente
las preferencias predeterminadas ni las combinaciones funcionales
aisladas del dispositivo. Los fixtures Android registran el estado
real seleccionado para cada variante, sin fingir otras preferencias
o un backend. Los banners largos pueden precisar scroll nativo,
sin ocultar la barra inferior. Las capturas de realimentacion
confirmaron su alcance y lectura. No se cambiaron pantallas de
produccion ni assets, aprobaciones previas o arquitectura DDD/CQRS.

Resultado: 55/70 (78,6%) aceptadas visualmente;
10 implementadas siguen pendientes y 5 ausentes.
La aceptacion visual no es una certificacion de transporte remoto.


### 2026-10-25 Independent-chat audit and NoNextDose46 acceptance

- Verified newer independent-chat handoff and actual logs, not only pasted50/70 summary: accessibility14/61-64 already accepted55/70. Direct capture OK1, DataStore OK6, Treatment wizard OK8 and Inventory OK7 logs retained. Inspected accessible large/reading feedback captures. Preserved other-chat adaptive surfaces and198 indexed paths.
- Current full Figma563:3178 context/render fetched. It contains contradictory pending-dose data under Sin proxima toma. Added native lavender165dp minimum hero with original home_tablet illustration and truthful empty description, plus anchored58dp Agenda al dia status above native tab. No stale time, medication, countdown or fictional progress. Initials/date/progress remain actual state-derived. Voice is disabled without a dose identity; agenda remains usable.
- Found and corrected OfflineFirstNextDoseRepository turning NETWORK_UNAVAILABLE plus cache miss into Success(null). It now preserves the failure; only server success(null)/HTTP204 establishes empty. New tests cover missing-cache failure, scoped cleanup after authoritative empty and real Retrofit204/401/403/404/500/null-body semantics with auth header/path.
- tata-no-next-dose-contract-20261025.log BUILD SUCCESSFUL1m:production/AndroidTest compile and809 JVM tests/154 suites zero failures/errors. Earlier connected Gradle run was green with0tests, NOT accepted. Direct AndroidJUnitRunner tata-no-next-dose-final-device-20261025.log OK1test; confirms populated->empty removes stale medication, disables voice, opens agenda and retry works in error. Capture now fails on image error and settles native click ripple.
- Pulled/inspected tata-no-next-dose-final-20261025.png against current Figma: whole hierarchy, original tablet/mic/list/tab resources, truthful empty hero, dynamic progress placeholder and anchored green status. Proportional adaptation approved for46. Count56/70accepted,9implemented pending,5absent,65implemented. Local HTTP/device fixtures do not prove deployed integration. No backend mutation, Git stage/commit/push or report edits.
- Next Treatment24 frame563:1554 fetched. Current Lifecycle management still lacks deactivation action, per-treatment adherence/trend and host tab integration; no acceptance yet. Analytics backend currently has no treatmentId scope; do not substitute adult-global92% as treatment data.


### 2026-10-26 Treatment management entry, freshness and revocation

- Added Gestionar pauta action only for a loaded, configured ACTIVE/PAUSED treatment. Feature emits actual loaded readmodel and medication label; host maps it to the existing lifecycle destination. Existing draft URLs remain valid, with optional initialStatus query defaultDRAFT for compatibility. No duplicate navigation destination or BC coupling introduced.
- Existing management begins from loaded status rather than DRAFT and refreshes protected treatment data on entry/restoration and subsequent foreground resumes. During refresh it hides the management content; a denied, mismatched or unusable read exposes only error/back/retry. Mutations are blocked until successful retry. Pause/edit authorization failures invalidate management, hide clinical data/actions and close the editor. Detail reloads on return so acknowledged pause/resume is visible.
- Nullable follow-up remains the actual system default: management labels it accordingly; native editor keeps blank as null, not invented0. Explicit0 remains distinct. Existing pre-dose lead is preserved. Medication label/count now come from the matching authorized medication catalog, when available.
- tata-treatment-management-revocation-final-20261025.log BUILD SUCCESSFUL43s: production/AndroidTest compile, both APKs and812 JVM tests/154 suites, zero failures/errors. New JVM cases prove existing ACTIVE/PAUSED never auto-configures/reactivates; denied management performs no mutations until retry, adopts latest PAUSED regimen and preserves null follow-up.
- Final direct AndroidJUnitRunner tata-treatment-management-revocation-device-20261025.log OK1test41.983s. Uses real feature Routes/ViewModels/handlers and host URL mapper with controlled backend ports. It verifies encoded treatment name, loaded active->pause->back/reload->paused->resume->back/reload, then revocation DURING a pause attempt hides medication/edit/pause and returns to Restricted detail. Denied attempt adds no successful mutation. This is not deployed end-to-end integration.
- Earlier final-device assertion failure was a fixture defect: denied flag was never connected to its reader. Corrected it; final fixture also rejects pause when revoked. Do not count the failed run or the previous Gradle green0tests as success.
- Current visual count56/70 remains unchanged by functional management work. Treatment24 still lacks per-treatment adherence chart, direct medication deactivation and full caregiver-tab composition;44/37/38/43/45 also retain pending visual status. Current Figma563:1554 context is available; never substitute adult-wide adherence for per-treatment data.
- No backend edits/publication, report edits, new Git staging, commits or push. Android index198 preserved. Canonical visual ledger includes exact per-context visual counts; high visual coverage is not functional module completion.


### 2026-10-26 Missing treatment invalidates management

- Renamed the invalidation predicate to invalidatesManagement and included RESOURCE_NOT_FOUND. A treatment deleted between the verified read and pause/configure receipt now hides the previous management data/actions and prevents repeated mutations, just like a revoked session. No status or success is fabricated.
- Added one JVM regression with two scenarios: deletion during pause and deletion during regimen edit. After each failure, subsequent pause/edit attempts produce zero additional writes. Existing authorized refresh remains the retry path.
- tata-treatment-management-missing-resource-20261026.log BUILD SUCCESSFUL1m: production and AndroidTest compilation plus813 JVM tests, zero failures/errors. No new instrumented run claimed for this predicate-only change; previous pause/resume/revocation device evidence remains separately recorded.
- Visual56/70 unchanged. No Git stage/commit/push, index198 preserved. Next substantive scope remains Treatment per-treatment adherence, direct deactivation and caregiver navigation/remaining variants; do not repeat the completed generic architecture migration or re-count theme-only tests as Figma acceptance.


### 2026-10-26 Management caregiver footer and backend GitFlow correction

- TreatmentLifecycleScreen now separates scrollable content from optional anchored navigation. Draft review retains no footer; ACTIVE/PAUSED management uses host-composed CaregiverTabBar with Person selected. Host dispatches existing caregiver routes with actual caregiver/adult IDs. Route ignores tab navigation during pending mutation/refresh or open editor. Feature does not depend on root destinations.
- tata-treatment-management-tabs-20261026.log BUILD SUCCESSFUL2m6s:production/AndroidTest compile, full813JVM suite and APKs. Actual direct TreatmentManagementNavigationTest in tata-treatment-management-tabs-device-20261026.log OK1test8.685s:footer visible/Person selected after scrolling, Alerts callback fires, existing pause/resume/detail-refresh/revocation assertions remain green. Does not approve full Figma24; trend/deactivation still missing.
- User requested backend changes live in fix branch before future develop/main integration. Backend was on develop (not main), changes uncommitted. Existing fix/backend-architecture diverges and checkout was safely refused; ancestor check also refused moving it. No refs overwritten. Created fix/backend-flow-integration from current develop with all dirty changes preserved, no stage/commit/push. Old fix evidence retained. Future completed minor changes must originate here then integrate develop/main. Android remains fix/android-architecture with198indexed paths untouched.
- User requested removal of backend licences/. Read-only root inventory, rg and git ls-files confirm no licences/license directory or tracked license files in this checkout. No deletion occurred. Initial generic deletion command was policy-rejected; read-only verification followed. Recheck actual remote checkout before eventual publication if user sees the directory there.
- Visual56/70 unchanged; goal remains active.


### 2026-10-26 User correction: only fix/backend-architecture

- IMPORTANT supersedes previous GitFlow note: user rejected new fix/backend-flow-integration. Deleted that branch safely after switching original checkout to develop; no commit/stage/push. Discovered original C:/Users/david/IdeaProjects/web-services checkout is OLD com.tata tree,53commits behind origin/develop. Previous local backend281/23test evidence applies to that old local tree, NOT the normalized architecture branch. Do not claim those changes are already integrated into the current architecture or deployed.
- Existing C:/Users/david/IdeaProjects/web-services-architecture was clean main; switched it safely to existing fix/backend-architecture (0e24649), preserving all branch history. This is the authoritative backend workspace for future work. Old dirty com.tata edits remain preserved in original checkout for comparison/porting, NOT blindly copied. Must audit corresponding current contracts and port only missing changes with current architecture tests before eventual minor changes/develop/main integration.
- Actual licenses directory exists in architecture checkout (spelling licenses, not licences). User-authorized removal completed via exact file patch and removal of empty directory: licenses/learning-center-platform-MIT.txt deleted locally, no stage/commit. Earlier claim absent applied only to wrong/old checkout and is superseded for current architecture.
- Front remains fix/android-architecture,813JVM and last tab-navigation device gate passed,56/70visual. Continue front while preserving198indexed paths. No duplicate backend flow branch remains.


### 2026-10-26 Authoritative backend contract re-audit (source evidence)

Backend workspace: C:/Users/david/IdeaProjects/web-services-architecture,
fix/backend-architecture, HEAD0e246499f0f32fb7791a4d7e2c9805a2e90fa155.
This supersedes integration assumptions based on the old com.tata checkout.

- AccessibilityController and UserPreferencesResource currently contain no dark-theme endpoint/field. Android dark preference is locally implemented and tested, but normalized-backend cross-device synchronization is NOT complete. Port through current result/response assemblers and owner security, not the old aggregate-to-resource shortcut.
- No followUpDelayMinutes field exists in normalized Treatment code. Android explicit-delay requests therefore still require current-backend implementation before successful receipt validation. Preserve nullable default-system vs explicit0 semantics and existing pre-dose lead; old code/tests are reference only.
- No timeline endpoint exists under normalized FamilyMonitoring interfaces. Android's existing fallback shows only known scheduled/generated facts on404; it must not fabricate delivery. Port actual delivery evidence through current public ACL and consent boundary if still required.
- Current FirebaseCloudMessagingClient already sends real FCM HTTPv1, validates project-specific receipt and sanitizes failures. The old LoggingPushProviderClient fix is OBSOLETE for this tree and must NOT replace current transport. Source existence is not proof of deployed credentials/device delivery.
- Front813JVM and56/70visual remain valid local evidence; old backend281test/23security gates do not establish compatibility with this normalized backend. No new backend production edits or test success claimed here; only licenses deletion is dirty in architecture checkout.
- Next prioritize these three contract gaps in existing fix/backend-architecture, preserving its packages, concurrency, result assemblers and tests; then resume Treatment visual completion. No extra backend branch, no stage/commit/push.


### 2026-10-26 Dark theme port completed on normalized backend

- Implemented explicit darkTheme contract in C:/Users/david/IdeaProjects/web-services-architecture, existing fix/backend-architecture. This supersedes the dark-contract gap recorded immediately above; timeline and per-treatment followUpDelay remain gaps.
- New command/public service/transactional handler use current UserPreferencesMutationService logical reservation, UserPreferencesCommandServiceImpl Result wrapper, immutable UserPreferencesResult and existing response assembler. New DarkThemeController PUT /api/v1/users/{userId}/preferences/dark-theme participates in current exception advice. JSON shape matches Android enabled request/darkTheme response. No old aggregate-to-resource controller copied.
- Domain defaultfalse, nullable dark_theme JPA column maps legacy nullfalse. Persistence assembler AND managed entity apply-state copy dark value; unrelated writes preserve it. Existing constructor paths/default factory retain defaults. Updated result snapshot constructor test and API documentation.
- tata-architecture-dark-verified-20261026.log BUILD SUCCESS32.653s,40tests0failures/errors:actual SpringSecurity HTTP/H2 owner/default/toggle/flush-clear/preserve-text-size/missing-input400VALIDATION_ERROR/other-owner403/anonymous401, legacy SQL-null read, immutable result, managed persistence, outcome and concurrency suites. Added real concurrent dark+contrast first-creation case preserving both decisions and one row. Production978/test264sources compile. This is targeted coverage, not a claim full normalized backend suite or deployed server passed.
- google-java-format1.24 initially incompatible with JDK26; reran successfully on bundledJDK21 before final gate. diffcheckclean. No stage/commit/push; license removal remains pending alongside changes. Android unchanged56/70visual and813JVM last verified. Old checkout dirty reference edits remain untouched.
- Next work: port followUpDelay into current recurrence-aware regimen without overwriting recurrence, and timeline into current consent/publicACL. Then run corresponding current-backend tests and authenticated Android integration. Do not reuse old281tests as normalized-backend proof.
