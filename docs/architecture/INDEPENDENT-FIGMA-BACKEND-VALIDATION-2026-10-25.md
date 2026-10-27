## ESTADO VIGENTE — 25-10-2026 (correccion de aceptaciones)

**45/70 aprobaciones visuales exhaustivas (64,3 %)**, como dejo Codex.
Las aprobaciones posteriores de 18, 46, 56-58 y 61-64 fueron
RETIRADAS por falta de auditoria completa de SVG, fuentes, imagenes,
estados de interfaz e integracion productiva con backend.
Las capturas y los tests anteriores siguen siendo evidencia
PARCIAL, no certificacion. El unico contador valido es
docs/ux/figma-visual-validation.md. Se toleran leves diferencias
de coordenadas, NO iconos o datos clinicos simulados.


# Auditoría independiente Figma ↔ Android ↔ backend — 2026-10-25

**Alcance:** Intake Home, detalle y agenda; contraste del inventario TS12. Es una comprobación acotada, no una nueva aprobación de las 70 variantes Figma. No se tocaron las modificaciones de Treatment en curso ni el backend.

## Contratos comprobados

| Frontera | Evidencia | Resultado |
| --- | --- | --- |
| Figma Home | Archivo `jCppvxtSpLpHOrC3ZWUIVC`, nodo `563:60`, 393×852; consultados `get_design_context` y screenshot | Los bloques estructurales Inicio, próxima toma, progreso, voz, agenda, consejo y pestañas existen en Compose |
| Próxima toma | Spring `GET /api/v1/older-adults/{id}/intakes/next` y Retrofit `IntakeApiService` | Ruta y semántica `204 = sin próxima toma` coherentes |
| Progreso del día | Spring `GET /api/v1/older-adults/{id}/intakes/agenda` y `GetDailyDoseProgressQueryHandler` | Android contabiliza `CONFIRMED` y `LATE`; el backend confirma que `LATE` significa confirmación tardía, no omisión |
| Inventario TS12 | `InventoryController` de backend frente a `InventoryApiService` Android | Coinciden alta, consulta y reposición; `daysRemaining` es dato opcional calculado por backend |
| Avatar Home | Figma muestra foto de Rosa; Compose usa iniciales del nombre actual | No introducir una foto fija de prototipo para todos los usuarios sin un contrato de foto autorizada |

## Incongruencia real corregida: inventario inicial

Java `Inventory.registerInitial` crea **el primer lote**. `registerBatch` agrega los lotes de reposición. Android calculaba `lastReplenishment` incluso si solo existía el lote inicial, mostrando falsamente «Última reposición».

En `InventoryStockReadModel` se devuelve `null` para cero o un lote. Para múltiples lotes se selecciona el más reciente. El mock HTTP del test de reposición ahora tiene lote inicial y reposición; se agregaron dos regresiones en `InventoryStockReadModelTest.kt`.

El recurso `BatchResource` no identifica explícitamente el origen de cada lote; el caso de registros con timestamps idénticos necesita contrato más preciso y no debe inventarse información en el cliente.

## Instrumentación con contexto regional de producción

El primer intento de `NextDoseVisualAuditTest` falló al buscar «Confirmar toma» porque la actividad Compose de pruebas no aplicaba el `es-419` de `MainActivity`. La captura mostraba textos ingleses. Se añadió `ProductionSpanishTheme` exclusivamente en el test, usando `withSpanishAppLocale()`. En la segunda ejecución, los **tres casos instrumentados pasaron sin fallos**: Home, detalle y agenda.

**Limitación de captura:** el AVD sigue mostrando ventanas `Pixel Launcher isn't responding` y `System UI isn't responding` en los PNG. Las pruebas funcionales Compose pasan, pero esas capturas no son válidas para certificar fidelidad visual pixel a pixel. Repetir tras estabilizar el emulador.

## Gates

- `:app:compileDebugKotlin`, `:app:compileDebugAndroidTestKotlin` y `:app:testDebugUnitTest`: **799 tests JVM, 153 suites, 0 fallos**; log `C:\Users\david\.codex\tmp\tata-independent-inventory-contract-20261025.log`.
- `:app:connectedDebugAndroidTest` (clase `NextDoseVisualAuditTest`): **3 pruebas instrumentadas, 0 fallos**; log `C:\Users\david\.codex\tmp\tata-independent-figma-home-retest-20261025.log`.
- `git diff --check`: sin incidencias. Sin stage, commit, push ni reset. Índice previo con 198 rutas intacto.
- La coherencia del backend se verificó leyendo controladores/dominio; **no se ejecutó Maven** en esta intervención.

## Para Codex

Conservar esta corrección y sus regresiones. No convertir `LATE` en omisión. Aceptar visualmente la Home únicamente con captura limpia sin diálogos de sistema y estado comparable a Figma. Verificar contrato de foto de perfil antes de cambiar iniciales por fotografía. No rehacer la arquitectura ni sobreescribir código concurrente de Treatment. Continuar validación Figma de variantes restantes una por una.


## Segunda ronda: comparación visual y pruebas de dispositivo

**Figma, revisado directamente:** Home `563:60`, detalle `563:144`,
detalle pendiente `563:3268`, Agenda `563:502` y Agenda con estados `563:3920`.

**Evidencia local:** C:\Users\david\.codex\tmp\tata-figma-visual-20261025
contiene `figma-home/detail/agenda.png`, `android-home/detail/agenda*.png` y
`compare-home/detail/agenda.png`. El AVD `emulator-5554` funciona con
1080×2400 px (420 dpi). Figma usa frames de 393×852; las capturas Compose
no contienen las barras del sistema y los ejemplos varían en fecha/hora.
Las comparaciones son **visuales por estructura**, no pixel-perfect.

Para evitar diálogos del sistema se sustituyó `screencap` por capturas
`onRoot().captureToImage()`, exportadas como PNG con MediaStore
a `Pictures/TataAudit`. El montaje de tests reproduce ahora
`LocalContext` y `LocalConfiguration` con `es-419`, y no duplica
`safeDrawingPadding()` en una actividad que ya respeta las barras
de estado. **No se cambió MainActivity ni el diseño de producción**
para arreglar el entorno genérico de pruebas.

| Vista | Evidencia y estado |
| --- | --- |
| Home | Jerarquía, iconos, tablet, colores, tarjeta de progreso, accesos y pestañas corresponden a Figma; avatar dinámico en iniciales no sustituye el perfil real por la fotografía de prototipo. |
| Detalle pendiente | Estructura, sección informativa, preparación y estado se visualizan en español. La implementación tiene un CTA de confirmación manual que no aparece en el frame de referencia; no eliminarlo sin validar el flujo funcional. La respuesta Intake no contiene motivo clínico, por ello la UI indica «No especificado» en vez de inventar la indicación mostrada en Figma. |
| Agenda con estados | Estructura semanal, línea temporal, badges y tarjetas presentes. Los estados/medicamentos/horas del fixture no son idénticos al ejemplo de diseño; no confundir datos dinámicos con defectos visuales. |

**Corrección de contenido aprobada por evidencia Figma:** Home en estado
de progreso 2/3 decía «Cada toma cuenta», pero el frame `563:60` muestra
«¡Vas muy bien!». Se agregó `home_progress_in_progress` en los recursos
españoles y su uso solo cuando hay al menos una toma completada y faltan
otras. Para progreso 0/3 se conserva «Cada toma cuenta». Se añadió
aserción de regresión instrumentada; no cambió el layout.

**Capturas bloqueadas:** una llamada Compose de Agenda lanzó
`ComposeTimeoutException` por renderizado lento del AVD. Ahora se registra
`TataVisualAudit` y se omite únicamente esa imagen, manteniendo las
aserciones funcionales. Una captura ausente no equivale a PASS visual.
El gate de control posterior a los cambios consta en
`C:\Users\david\.codex\tmp\tata-figma-home-copy-final-gate-20261025.log`.


### Gate final verificado tras ajustar Inicio y las pruebas

Se ejecutó:
`gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.vitahealth.tata.intake.presentation.home.NextDoseVisualAuditTest --console=plain`.

**Resultado: BUILD SUCCESSFUL.** JVM: **799 tests, 153 suites,
0 fallos ni errores**. Instrumentación de dispositivo: **3 tests,
0 fallos** (Inicio, detalle con estados y agenda).
El fixture 2/3 renderizado en el último APK confirma el nuevo texto
**«¡Vas muy bien!»** y quedó guardado en
`C:\Users\david\.codex\tmp\tata-figma-visual-20261025\android-home-final-copy.png`.
La comparación `compare-home.png` fue regenerada con esa versión final.
`git diff --check` = 0; índice Git de 198 rutas intacto;
sin stage, commit, push ni reset. Log completo:
`C:\Users\david\.codex\tmp\tata-figma-home-copy-final-gate-20261025.log`.


## ARCHIVADO: checkpoint parcial 50/70 sin auditoria de assets

**Implementadas:** 65/70 (92,9 %). **Comparadas:** 64/70 (91,4 %).
**Aceptadas visualmente:** 50/70 (71,4 %).
**Pendientes implementadas:** 15/70 (21,4 %).
**Sin implementación específica:** 5/70 (7,1 %).

Se contrastaron **diez referencias distintas** directamente con Figma y
capturas Android: 8, 10, 11, 14, 16, 46, 61, 62, 63 y 64.
Las primeras tres ya estaban aceptadas y no se volvieron a contar;
14 y 16 permanecen pendientes. Las **cinco aprobaciones adicionales**
son 46 y 61–64. Se toleran diferencias pequeñas de medidas,
posiciones, horas y datos dinámicos, manteniendo funcionalidad
y consistencia visual en toda la aplicación.

| Área / BC propietario de la referencia | Aceptadas | Pendientes | Sin implementación | Progreso visual |
| --- | ---: | ---: | ---: | ---: |
| Identidad y suscripción | 9/11 | 2 | 0 | 81.8 % |
| Care Link | 3/4 | 1 | 0 | 75.0 % |
| Intake | 16/16 | 0 | 0 | 100.0 % |
| Inventory | 3/3 | 0 | 0 | 100.0 % |
| Monitoring | 3/8 | 5 | 0 | 37.5 % |
| Analytics | 6/6 | 0 | 0 | 100.0 % |
| Preferences | 6/7 | 1 | 0 | 85.7 % |
| Treatment | 4/10 | 6 | 0 | 40.0 % |
| Omission | 0/1 | 0 | 1 | 0.0 % |
| App/transversal | 0/4 | 0 | 4 | 0.0 % |
| **TOTAL** | **50/70** | **15** | **5** | **71,4 %** |

El 46 usa deliberadamente un estado vacío real. En el Figma de
«Sin próxima toma» aún aparecen una hora y una pastilla del ejemplo,
una contradicción que el producto **no debe copiar**: HTTP 204
representa ninguna próxima toma. Los estados 61–64 mantienen avisos,
interruptores y navegación acordes con el modelo y los recursos.

El 14 sigue pendiente: falta capacidad real de tema oscuro solicitada
en el prototipo. El 16 sigue pendiente: la lista de vinculados
no sustituye el perfil detallado con adherencia y notas.
La clasificación App/transversal incluye las landings, destino
externo e internacionalización; Omission tiene un recordatorio
visual (39), pero no requiere pantallas Compose propias.

Esta tabla representa exclusivamente **aceptación visual
proporcional**, no que todos los flujos clínicos, permisos,
transportes FCM o endpoints estén aprobados extremo a extremo.

Evidencia visual: carpeta
C:/Users/david/.codex/tmp/tata-figma-next-audit-20261025/
junto con Figma Mobile Prototyping (archivo jCppvxtSpLpHOrC3ZWUIVC).
Tests instrumentados: tata-accessibility-variant-20261025.log,
tata-accessibility-datastore-20261025.log,
tata-accessibility-figma-variants-20261025.log, almacenados en
C:/Users/david/.codex/tmp/.

---


## ARCHIVADO: cierre parcial 50/70 (no aprobado)

El cuadro por BC y los estados de las 70 referencias se actualizaron
en `docs/ux/figma-visual-validation.md`: **50 aceptadas visualmente
(71,4 %), 15 implementadas pendientes, cinco sin implementación
específica**. Se inspeccionaron 10 frames distintos en esta conversación:
8, 10, 11, 14, 16, 46, 61, 62, 63 y 64. Solo cinco suman nuevas
aceptaciones: 46 y 61–64. Conservar 14 y 16 pendientes.

**Pruebas específicas:**
- `tata-accessibility-variant-20261025.log`:
  BUILD SUCCESSFUL, dos pruebas instrumentadas de controles
  y navegación inferior (el runner ejecutó únicamente esa clase).
- `tata-accessibility-datastore-20261025.log`:
  BUILD SUCCESSFUL, comprobación de persistencia y aislamiento
  por propietario con la clase DataStoreAccessibilityLocalStoreTest.
- `tata-accessibility-figma-variants-20261025.log`:
  BUILD FAILED por `AssertionError: Failed waiting for PixelCopy!`
  en el helper de screenshot `ExistingScreensVisualAuditTest.capture`.
  El problema es del capturador del AVD, **no prueba que la pantalla
  falle**. No afirmar que este nuevo capturador pasó ni usarlo como
  evidencia visual nueva; las capturas previas del AVD (2026-10-24)
  sí fueron recuperadas, vistas y comparadas con los frames Figma.

La variante 46 («Sin próxima toma») está aceptada con una
**desviación deliberada y correcta**: el mock Figma dice que no hay
próxima toma pero deja visibles hora y medicamento. La UI real no
los inventa. Las variantes 61–64 muestran valores reales
e independientes para cada preferencia, no la combinación arbitraria
de switches del prototipo. La pantalla global 14 espera una
decisión funcional sobre tema oscuro; el 16 necesita el perfil
completo en lugar de una lista.

El porcentaje visual es distinto del progreso end-to-end del backend;
es una estimación basada en casos de Figma, no una certificación
de los nueve BC. Git sigue en `fix/android-architecture`,
sin stage/commit/push ni reset.


## ARCHIVADO: Monitoring 54/70 (no aprobado)

**65/70 variantes implementadas (92,9 %), 64/70 contrastadas (91,4 %),
54/70 aceptadas visualmente (77,1 %), 11 pendientes implementadas
(15,7 %) y cinco sin implementacion especifica (7,1 %).**
Se agregan cuatro aceptaciones visuales proporcionales: 18, 56, 57 y 58.

| BC / Area | Aceptadas | Pendientes | Sin implementacion | Progreso visual |
| --- | ---: | ---: | ---: | ---: |
| Identity & Subscription | 9/11 | 2 | 0 | 81.8 % |
| Care Link | 3/4 | 1 | 0 | 75.0 % |
| Intake | 16/16 | 0 | 0 | 100.0 % |
| Inventory | 3/3 | 0 | 0 | 100.0 % |
| Monitoring | 7/8 | 1 | 0 | 87.5 % |
| Analytics | 6/6 | 0 | 0 | 100.0 % |
| Preferences | 6/7 | 1 | 0 | 85.7 % |
| Treatment | 4/10 | 6 | 0 | 40.0 % |
| Omission | 0/1 | 0 | 1 | 0.0 % |
| App/transversal | 0/4 | 0 | 4 | 0.0 % |
| **TOTAL** | **54/70** | **11** | **5** | **77,1 %** |

Las capturas espanolas se guardaron en
C:/Users/david/.codex/tmp/tata-treatment-monitoring-audit-20261025:
alert-detail-complete-es.png, contact-unavailable-es.png,
followup-note-saved-es.png y alert-attended-es.png.
La suite MonitoringFigmaStatesTest paso cuatro pruebas instrumentadas,
sin fallos, con LocalContext y LocalConfiguration en es-419.
Log: C:/Users/david/.codex/tmp/tata-monitoring-figma-spanish-gate-20261025.log.

El backend solo certifica recordatorios efectivamente registrados.
No agregar el segundo aviso ficticio del mock. Sin canal de contacto,
mostrar el control deshabilitado. El indicador verde de Atendida
es de estado, no un boton. Cerrar alerta permanece como accion
adicional real de negocio. La foto de ejemplo Figma no es una
foto real del adulto; se conservan iniciales en ausencia de URI.

Monitoring conserva pendiente la variante 27 (Notas del cuidador).
Treatment conserva seis referencias pendientes. Este porcentaje
es exclusivamente visual; no certifica uso de cuentas reales
ni entrega FCM.

---


### Gates de interacciones de Monitoring (2026-10-25)

Después de aprobar las variantes visuales 18, 56, 57 y 58,
se ejecutaron por separado dos suites instrumentadas de rutas
y mutaciones (para no confundir un screenshot con persistencia):

- `AlertDetailLifecycleTest`: log
  `C:\Users\david\.codex\tmp\tata-monitoring-route-note-20261025.log`,
  **BUILD SUCCESSFUL**. Comprueba revalidación del vínculo en resume
  y registro de nota desde la Route con rechazo de blanco/duplicado.
- `AlertFollowUpActionsTest`: log
  `C:\Users\david\.codex\tmp\tata-monitoring-update-status-20261025.log`,
  **BUILD SUCCESSFUL**. Comprueba confirmación antes de cerrar,
  cancelación segura y bloqueo de acciones durante mutación.
- `MonitoringFigmaStatesTest`: log
  `C:\Users\david\.codex\tmp\tata-monitoring-figma-spanish-gate-20261025.log`,
  **BUILD SUCCESSFUL**; 4/4 casos instrumentados y capturas PNG
  españolas válidas. JVM: **799 tests, 153 suites, 0 fallos/errores**.

Estas pruebas usan repositorios de fixture/fakes donde corresponde;
no certifican una solicitud autenticada al despliegue remoto ni
la entrega real de FCM. El backend mantiene autoridad sobre el
estado de alerta, los eventos de recordatorio y las notas.


## Auditoría estricta Monitoring: Figma + recursos + backend (2026-10-25)

**Referencias todavía pendientes de aprobación:** 18 (`563:918`),
56 (`563:4145`), 57 (`563:4245`) y 58 (`563:4345`).
Las cuatro se contrastaron con metadata, código y exports reales
de Figma. **No se suman a las 45 aprobaciones vigentes.**

### Recursos verificados

- Fuentes: Figma utiliza Inter Regular/Medium/Semi Bold/Bold y DM
  Serif Display Regular. Los TTF locales se identificaron por
  OpenType como Inter 4.001 Regular y DM Serif Display Regular.
  **Pendiente:** la app no incluye Inter 500/600/700 como archivos;
  algunos pesos se sintetizan y ciertos estilos Material3 pueden
  usar una tipografía predeterminada. No se certifica todavía
  la coincidencia completa de fuentes.
- Icono de alerta hero: dibujo de advertencia rojo ya igual en
  geometría al export Figma. Primer recordatorio: SVG byte a
  byte idéntico al recurso original en el nodo 18.
- Corrección realizada: el reloj de toma programada y el icono
  pequeño de alerta pendiente eran vectores diferentes.
  Se incorporaron los exports exactos en
  `app/src/main/res/raw/alerts_timeline_scheduled.svg` y
  `app/src/main/res/raw/alerts_timeline_pending.svg`.
  Únicamente `AlertEvidenceTimeline.kt` utiliza esos recursos;
  no se cambian las listas, otros BC ni el hero.
  Se compararon además Scheduled, Pending y Reminder 1 en
  las variantes 56, 57 y 58: los nueve SVG tienen la misma
  geometría de Figma que la referencia 18.
- Tabs: `CaregiverTabBar` tiñe iconos de forma condicional con
  `ColorFilter.tint`. Los cambios entre activo/inactivo son
  intencionales y sus contornos corresponden al prototipo.
- Imagen de tableta: PNG Figma y Android miden 248 x 248,
  representan la misma tableta y, al componer sobre blanco,
  difieren en media menos de 0,3 niveles por canal. Se conserva
  el archivo ya suavizado por Codex.
- Foto de Rosa: no se convierte un recurso de demostración
  en fotografía fija de los usuarios. Android conserva las
  iniciales cuando no dispone de una foto autorizada.

### Integración real y verificaciones

En producción `AlertDetailRoute` construye un ViewModel mediante
la fábrica de `MonitoringDependencies`; el lector de historial
usa Retrofit y un límite de sesión. `FamilyMonitoringApiService`
consulta el endpoint
`GET /api/v1/older-adults/{id}/alerts/{alertId}/timeline`
con parámetro `caregiverId`, idéntico al controlador Spring.
El backend exige vínculo del cuidador antes de leer eventos.
El cliente ordena eventos, valida tipos e impide interpretar una
respuesta incompleta como evidencia válida. Los tests del cliente
verifican el Bearer, orden y HTTP 403.

Una toma programada NO demuestra que el recordatorio fue enviado.
El servidor agrega `REINFORCED_REMINDER_SENT` solamente cuando
dispone de evidencia de entrega; no se crea artificialmente el
segundo recordatorio del mock Figma. Las transiciones ATTENDED y
CLOSED son operaciones de backend distintas. Los fixtures
utilizados para screenshots viven exclusivamente en AndroidTest.

**Backend con JDK 26 / Maven:**
- `GetAlertTimelineQueryHandlerTest` y
  `AlertTimelineControllerTest`: **3/3 PASS**.
  Log `C:/Users/david/.codex/tmp/tata-monitoring-server-timeline-jdk26-20261025.log`.
- `FamilyMonitoringApiIntegrationTest`: **5/5 PASS**,
  Spring Boot + MockMvc + H2 / perfil test; comprueba
  consentimiento, autorización de cuidador, resumen, historial
  y notas.
  Log `C:/Users/david/.codex/tmp/tata-monitoring-spring-integration-jdk26-20261025.log`.
  No sustituye un despliegue real.

**Android tras cambiar los SVG:** BUILD SUCCESSFUL,
**799 pruebas JVM en 153 suites, 0 fallos** y
**4/4 instrumentadas en emulador, 0 fallos**.
Log `C:/Users/david/.codex/tmp/tata-strict-monitoring-vectors-and-functional-20261025.log`.
Captura visual actual:
`C:/Users/david/.codex/tmp/figma-monitoring-assets-20261025/after-exact-icons.png`.

**Integración remota BLOQUEADA:** por defecto el APK apunta a
`https://web-services-yzxl.onrender.com/`. Dos consultas sin
credenciales, a health y a un recurso de timeline inexistente,
agotaron el tiempo de espera. No pudo confirmarse si el
despliegue está disponible ni actualizado. El servicio local
en puerto 8080 era Apache httpd, NO Spring. No se usaron
credenciales de usuarios ni se editaron datos de producción.

**Estado de 18/56/57/58:** auditoría real de recursos y
funcionamiento en pruebas completada con correcciones concretas,
pero quedan pendientes de aceptación exhaustiva hasta verificar
pesos tipográficos y una integración autenticada contra un
backend desplegado. No se exige calco de coordenadas.


## Continuación: fuentes Inter 4.001 y Notas del cuidador (#27)

**Contador vigente:** 45/70 (64,3 %) aprobaciones exhaustivas, sin nuevas
aprobaciones prematuras. La referencia #27 sigue pendiente de auditoría
total de assets e integración autenticada real.

### Tipografía

Se localizó la fuente variable Inter 4.001 (mismo identificador
`Version 4.001;git-66647c0bb` que `tata_inter.ttf`) a partir de
Google Fonts, cuyo origen documentado es el proyecto Inter. El
repositorio ya incluye `docs/licenses/Inter-OFL.txt`.

Se obtuvieron masters estáticos genuinos de peso 500, 600 y 700,
a tamaño óptico 14, conservando los 197 caracteres de la fuente
Regular instalada. Sus archivos son:
- `app/src/main/res/font/tata_inter_medium.ttf`
- `app/src/main/res/font/tata_inter_semibold.ttf`
- `app/src/main/res/font/tata_inter_bold.ttf`

La familia común se declara en
`shared/presentation/designsystem/theme/TataInterFontFamily.kt`.
**Solo** AlertDetailScreen y NotesScreen de Monitoring usan esta
familia en su raíz. La fuente Regular original y el tema global
quedan intactos para proteger las vistas ya aprobadas por Codex.
La licencia OFL existente se conserva.

### Notas del cuidador, referencia Figma 576:2859

El diseño incluye cabecera con persona vinculada, edad/estado,
historial de notas y acción de nueva nota. El backend realmente
proporciona contenido, identificador de autor, fecha y perfil
de la persona, pero **no** títulos ni categorías de las notas.
No deben introducirse títulos, fotos de pacientes ni supuestos
sobre el autor que solo aparezcan como datos ficticios de Figma.

Se añadió `NotesPersonCard.kt` y se conectó
`NotesViewModel` al `MonitoredPersonReader` existente mediante
`MonitoringDependencies`. Primero se leen notas protegidas
por `CareRelationshipQueryService` y después el perfil.
Se muestra cabecera únicamente cuando hubo lectura autorizada;
el cliente limpia la identidad y las notas tras una denegación
en cualquiera de las lecturas. Cuando la fecha de nacimiento
es nula, no se inventa edad. La imagen fija de «Rosa» del mock
se sustituye por iniciales del nombre obtenido del backend.

La integración productiva utiliza Retrofit, token de sesión,
repositorio con límite de sesión y query handler existentes.
Las pruebas de pantalla solo usan datos ficticios dentro de
AndroidTest; no hay mock en código productivo.

Se agregaron dos pruebas unitarias de perfil (lectura autorizada,
revocación de acceso y rechazo desde perfil) y
`NotesAuthorizedProfileFigmaTest` con dos casos instrumentados:
perfil autorizado visible y perfil completamente oculto después
de un error de acceso. Captura de Compose:
`C:/Users/david/.codex/tmp/tata-treatment-monitoring-audit-20261025/android-notes-linked-20261025.png`.
La prueba instrumentada **pasó 2/2** antes de aplicar Inter
al contenedor de NotesScreen; el gate final posterior a ese
último ajuste se registra aparte en
`C:/Users/david/.codex/tmp/tata-notes-profile-final-font-20261025.log`.

**Limitaciones para aceptación:** falta una comparación rigurosa
de cada icono y elemento estático de la referencia #27, y
comprobar el backend desplegado con sesión autorizada.
El backend del perfil `GET /api/v1/older-adults/{id}` no expone
por sí solo un parámetro de relación de cuidador: el flujo de
Notas se apoya primero en el GET de notas, que SÍ verifica
`caregiverId`; conviene reforzar/validar la autorización directa
de perfiles en una revisión integral del contrato de Care Link.
Sin esa revisión no declaramos una certificación de seguridad
del endpoint genérico.


## Revision y continuacion de Codex

Se conserva el trabajo del otro chat en fuentes Inter, recursos de alertas y perfil
autorizado de Notas. El gate final anterior confirma compilacion y pruebas; la
familia nueva se aplica solo a las raices de Notas y Detalle de alerta. La fuente
Regular y el tema global permanecen conservados.

La aceptacion visual vuelve al criterio proporcional indicado por el usuario:
composicion, opciones, recursos y legibilidad consistentes, con adaptaciones
nativas. Las comprobaciones remotas se documentan por separado. El contador
confirmado parte de45; las cifras provisionales50/54 no se restauran sin revisar
su evidencia, ni se descartan capturas utiles por diferencias menores.

Contrastado el frame actual576:2859, se completan en Notas el acceso superior+,
el subtitulo con nombre autorizado, el marcador original36dp y el bloque
Historial compartido. Los titulos/categorias y fotos del ejemplo no se fabrican.
Las acciones solo aparecen tras lectura protegida satisfactoria. El test de
captura ahora falla si no puede obtener la imagen, en vez de terminar sin ella.

`tata-notes-resume-final-20261025.log`: BUILD SUCCESSFUL,801 pruebas JVM,
0fallos/errores y dos casos instrumentados. Captura inspeccionada:
`C:/Users/david/.codex/tmp/tata-notes-resume-current-20261025.png`.
El ultimo ajuste del borde del boton+ se verifica por separado en
`tata-notes-final-control-20261025.log` antes de cerrar la aceptacion.
No se modifican el indice Git de198 rutas, los commits ni el remoto.


### 2026-10-25 Independent-chat continuation verified: Notes27 accepted

- Final tata-notes-resume-final-20261025.log BUILD SUCCESS4m47s:801 JVM tests/153 suites, zero failures/errors, plus two actual instrumented notes/profile/action cases. Last cosmetic plus-button correction verified separately by tata-notes-final-control-20261025.log BUILD SUCCESS2m38s, both cases pass. Captures no longer silently succeed on PixelCopy failure.
- Inspected final tata-notes-accepted-20261025.png against current Figma576:2859 full context/render: serif title, actual named subtitle, thin native48dp plus, authorized profile/age/initials, real Inter weights, original36dp newest-note marker, content/author/date, shared-history block, add action and Notes-selected tab bar visible. Dynamic count/body-only notes and initials accepted as truthful contract adaptations; no invented categories, titles, patient photo or author name. Approval is visual, not remote integration certification.
- Variant27 accepted proportionally. Confirmed46/70 accepted,19implemented pending,5without implementation-specific variant;65implemented. The other chat's nine provisional variants retain their evidence for focused review; not all reinstated or discarded. Existing source/API/session wrappers preserved. No report changes, no stage/commit/push, Android index198 and diff check clean.


### 2026-10-25 Dark preference contract and native accessibility row

- Backend now exposes PUT /api/v1/users/{userId}/preferences/dark-theme and returns darkTheme in the preference snapshot. Command/service/handler, aggregate and persistence assembler follow the existing accessibility pattern. Nullable database column reads old rows as false. Existing constructor callers remain compatible.
- Full backend baseline tata-dark-backend-contract-20261025.log:281 tests, zero failures/errors. New HTTP/H2 default/toggle/other-setting preservation and missing-value tests pass in tata-dark-backend-http-20261025.log. Actual Spring Security owner/anonymous/other-account gate tata-dark-backend-security-20261025.log:23 tests, zero failures/errors. Local contract, not deployed.
- Android now has UpdateDarkTheme command/handler, session-scoped repository operation, explicit DARK_THEME pending field, Retrofit endpoint/DTO mapping and ViewModel/DI/route wiring. Removed the temporary unconditional local-dark preservation and receipt comparison bypass. Offline intent retries only its field; mismatched receipt stays pending; rejected input restores previous state. Older remote read without darkTheme maps to false, not proof the remote endpoint supports writes.
- Current Figma563:4638 context/render fetched. Original24x24 moon SVG downloaded to figma_access_darkmode.svg (875bytes), called at24dp within existing34dp lavender badge. View group now includes the native interactive dark-theme row. Spanish default and English resources added. Theme-aware preference groups/help, shared default cards and adult tab keep the light palette; dark host setting and system-bar appearance are connected.
- tata-dark-android-contract-final-20261025.log BUILD SUCCESSFUL1m45s:production/AndroidTest compile and805 JVM tests, no failures/errors. After one JSON boundary case and final formatting/system-bar adjustment, tata-dark-accessibility-device-20261025.log is running production/JVM/device gates. Do not infer instrumented success from compilation; inspect XML and fresh captures before acceptance.
- IMPORTANT next scope: explicit fixed-white/pastel surfaces remain in other BCs (for example Treatment screens, CareLinkScreen and NotesScreen). Global dark-theme visual compatibility is not complete. Check these before publishing the theme as release-ready; existing light approvals are retained. Accessibility14/61-64 remain pending until fresh images are inspected. Count50/70,65implemented,15implemented pending,5absent.
- No stage/commit/push/reset or report edits. Android index198 paths remains unchanged.


## Checkpoint del 25/10/2026 tras el trabajo de Codex

**Último contador válido: 50/70 (71,4 %)**. La migración oscura no implica aprobación adicional. La prueba de feedback en es-419 quedó corregida y pasó (`tata-accessibility-feedback-es-recheck-20261025.log`). Cuatro pruebas instrumentadas Care Link/Treatment/Notas con pantallas reales pasaron con capturas válidas (`tata-dark-continuation-device-retry-20261025.log`, `C:\Users\david\.codex\tmp\tata-dark-final-screens-20261025`), y las seis pruebas de DataStore para aislamiento por propietario pasaron (`tata-dark-owner-isolation-final-20261025.log`). JVM 806/806. Backend Maven previo 281/281, **sin certificación de despliegue remoto**.

Tres nuevos casos instrumentados de formularios y notas con datos quedaron **pendientes** por `PixelCopy`, ajustes de selectores y finalmente `INSTRUMENTATION_FAILED` / `DELETE_FAILED_INTERNAL_ERROR` del AVD; un `BUILD SUCCESSFUL` de Gradle no los certifica. El test nuevo está en `DarkThemePopulatedFlowsTest.kt`. Se preservaron modificaciones concurrentes de Monitoring. Git mantiene 198 rutas staged, sin commits ni push; `git diff --check` sin problemas. Próximo control: XML y screenshots de los tres casos con AVD estable, auditoría de iconos/fuentes/contraste por variante y endpoints autenticados reales antes de aprobar más pantallas.
