# Conteo de fidelidad visual — 70 variantes

## Criterio vigente: fidelidad proporcional y coherencia visual

Se comparan composicion, opciones, jerarquia, recursos, legibilidad y estados.
Se admiten ajustes nativos y alineaciones consistentes entre pantallas, tal como
indica el usuario. La aceptacion visual se registra por variante y se separa
de pruebas funcionales e integracion remota. Una consulta local o una captura
con fixtures no demuestra despliegue, permisos ni persistencia remota.

El conteo confirmado es 70/70. Se conservan las evidencias anteriores;
las aceptaciones nuevas se sustentan en comparación directa con Figma y
capturas legibles del emulador. Esto no certifica el transporte remoto.


## Cobertura visual vigente por contexto

| Contexto | Aceptadas / variantes | Implementadas pendientes | Sin implementacion |
| --- | ---: | ---: | ---: |
| Identity & Subscription | 11/11 | 0 | 0 |
| Care Link | 4/4 | 0 | 0 |
| Intake Execution | 16/16 | 0 | 0 |
| Family Monitoring | 8/8 | 0 | 0 |
| Adherence Analytics | 6/6 | 0 | 0 |
| Accessibility & Preferences | 7/7 | 0 | 0 |
| Treatment Management | 10/10 | 0 | 0 |
| Inventory & Replenishment | 3/3 | 0 | 0 |
| Omission & Escalation | 1/1 | 0 | 0 |
| Transversal (landing, destino externo, EN) | 4/4 | 0 | 0 |
| Total | 70/70 | 0 | 0 |

Esta tabla mide aceptacion visual proporcional por variante, no cierre funcional
ni integracion remota completa de cada contexto.

---


Checkpoint: 2026-10-26. Registro independiente del conteo de implementaciones.

- 70 variantes tienen implementación, incluyendo composiciones parciales.
- 0 carecen actualmente de implementación específica.
- 70 están aceptadas visualmente con el criterio proporcional acordado.
- 0 variantes visuales pendientes. El contacto de #2 utiliza mailto:soporte@tataapp.com, la dirección española del landing configurada por indicación del usuario; la entrega de correo depende de la disponibilidad de esa cuenta.
- El conteo visual no implica integración completa ni ausencia de problemas funcionales.

Aceptación: composición, tipografía, proporciones, iconos, contraste y estado representado coherentes con Figma. Se permiten adaptaciones nativas, desplazamiento por contenido real y datos dinámicos; no se exige copiar nombres, fechas ni títulos a posiciones absolutas. La aceptación visual no certifica integraciones ni navegación completa. Cada variante requiere su propio resultado; reutilizar un composable no aprueba automáticamente todas sus variantes.

Evidencias PNG en el equipo de trabajo: C:/Users/david/Downloads/. Las capturas de pruebas contienen datos aislados. El consentimiento denegado conserva su propia validación pendiente.

Preferencias de notificación: los estados normal y guardado se comparan mediante capturas directas de Compose. Las tarjetas incluyen cadencia, alertas al cuidador, resumen diario, contactos y escalamiento cuando el servidor expone la política. Los editores usan formularios y selectores nativos. El horario ocupa una tarjeta compacta con switch y editor de horas en diálogo. Los cuatro canales usan iconos SVG de Figma y controles nativos; CALL queda deshabilitado cuando no llega del servidor. Los diálogos toman el idioma de la pantalla y la navegación usa la barra del cuidador. Las variantes 26 y 65 quedan aceptadas mediante capturas finales; tres pruebas de interacción comprueban el editor de cadencia, los canales y los estados de guardado. Guardar esta configuración no habilita por sí solo llamadas, resumen diario ni entrega a contactos de emergencia.

| # | Variante | Nodo Figma | Resultado | Captura aceptada |
| --- | --- | --- | --- | --- |
| 1 | Landing - Value & Features | 563:1849 | Aceptada proporcionalmente; hero, tres funciones, tarjetas finales y CTA; ilustración explícita sin pacientes ni métricas ficticias | tata-landing1-after-scroll-20261026.png |
| 2 | Landing - Plans & Contact | 563:1913 | Aceptada proporcionalmente con botón Contactar activo y callback de prueba (1 sola acción), catálogo y tabla equivalente; producción solo habilita el botón al configurar TATA_PUBLIC_CONTACT_URI verificada, sin apertura externa en la prueba | tata-landing2-active-contact-20261026.png |
| 3 | Onboarding | 563:5 | Aceptada visualmente | onboarding-final.png |
| 4 | Caregiver Registration | 563:1398 | Aceptada visualmente; secuencia real de cuenta y verificación | registration-verification-final.png |
| 5 | Link & Consent | 563:1440 | Aceptada visualmente; foto de referencia aislada, iniciales para perfiles sin foto | link-code-final.png |
| 6 | PIN Access | 563:1338 | Aceptada visualmente | pin-access-final.png |
| 7 | Login | 641:16 | Aceptada visualmente; correo y contraseña nativos, proveedores sociales solo informativos | login-final.png |
| 8 | Home | 563:58 | Aceptada visualmente | home-vector-icons.png |
| 9 | My Medications | 563:361 | Aceptada visualmente; catálogo de sesión PIN, conteo real y toma destacada sin duplicar | my-medications-verified.png |
| 10 | Medication Detail | 563:142 | Aceptada visualmente | dose-detail-polished.png |
| 11 | Weekly Schedule | 563:500 | Aceptada visualmente | agenda-final.png |
| 12 | Voice Confirmation | 563:242 | Aceptada visualmente; permiso, grabación privada, frase dinámica y cancelación | voice-listening-verified.png |
| 13 | Dose Confirmed | 563:291 | Aceptada visualmente; fecha persistida, próxima toma real y retorno al inicio | dose-confirmed-verified.png |
| 14 | Accessibility | 563:618 | Aceptada proporcionalmente; seis ajustes e iconos SVG originales, secciones y barra adulta; estado base por preferencias reales | tata-figma-acceptance-accessibility-20261025/base.png |
| 15 | Family Summary | 563:727 | Aceptada visualmente | family-summary-final.png |
| 16 | Linked Person | 563:1121 | Aceptada proporcionalmente; iniciales en ausencia de foto, adherencia y tratamientos autorizados, acciones SVG y tab; sin inventar nota ni fecha de vínculo | tata-linked16-stabilized-20261026.png |
| 17 | Alerts | 563:819 | Aceptada visualmente; resumen real de pendientes, agenda y confirmaciones diferenciadas | tata-alerts-list-final-20261024.png |
| 18 | Alert Detail | 563:918 | Aceptada proporcionalmente; Detalle con datos y eventos registrados; Inter real y acciones nativas | tata-verified-alert-detail-initial-20261025.png |
| 19 | History & Insights | 563:1017 | Aceptada visualmente; datos y gráfica dinámicos | history-content-final.png |
| 20 | Adherence Recommendations | 563:1767 | Aceptada visualmente; matriz y recomendaciones dinamicas, accion anclada con callback | tata-recommendations-anchored-content.png |
| 21 | Notes - Adult | 576:2785 | Aceptada visualmente; notas personales por sesión PIN, filtros y formulario nativo | personal-notes-verified.png |
| 22 | Add Medication - Caregiver | 563:453 | Aceptada visualmente; selector nativo y volver conectados | medication-registration-final.png |
| 23 | Create Treatment | 563:1490 | Aceptada visualmente; revisión nativa con pauta y agenda dinámicas, paso Revisar activo | tata-treatment-review-proportions-20261025.png |
| 24 | Treatment Management | 563:1554 | Aceptada proporcionalmente; pauta real, pill y gráfica por tratamiento, navegación y desactivación dirigida | tata-treatment24-adherence-top-20261026.png / tata-treatment24-adherence-bottom-20261026.png |
| 25 | Inventory & Restock | 563:1632 | Aceptada visualmente; cantidades y estimación proceden del contrato | inventory-content-final.png |
| 26 | Notification Preferences | 563:1219 | Aceptada visualmente; cinco grupos, iconos y navegación nativa | notifications-full-policy-verified.png |
| 27 | Notes - Caregiver | 576:2859 | Aceptada proporcionalmente; perfil autorizado, Inter real, acciones y contenido dinamico sin inventar titulos ni fotos | tata-notes-accepted-20261025.png |
| 28 | Plan & Subscription | 563:1701 | Aceptada proporcionalmente; precios y capacidades del servidor, selección y recuperación; no se inventa barra de ciclo sin dato contractual | tata-plan28-native-20261026.png |
| 29 | PIN Setup | 563:1970 | Aceptada visualmente | pin-setup-final.png |
| 30 | PIN Incorrect | 563:2034 | Aceptada visualmente | pin-incorrect-final.png |
| 31 | PIN Temporarily Blocked | 563:2098 | Aceptada visualmente | pin-blocked-final.png |
| 32 | Duplicate Email | 563:2162 | Aceptada visualmente; código todavía no enviado | registration-duplicate-final.png |
| 33 | Verification Expired | 563:2208 | Aceptada visualmente | registration-expired-final.png |
| 34 | Invalid Link Code | 563:2254 | Aceptada visualmente; aviso de error separado del estado del vínculo | link-invalid-final.png |
| 35 | Consent Required | 563:2308 | Aceptada visualmente; acceso restringido sin exponer seguimiento ni inferir la causa del vínculo inactivo | consent-required-final.png |
| 36 | Medication Required Fields Error | 563:2404 | Aceptada visualmente; aviso y campos vacíos, contenido desplazable | medication-error-final.png |
| 37 | Medication Updated | 563:2455 | Aceptada proporcionalmente; guardado de pauta confirmado y aviso verde anclado | tata-treatment37-updated-20261026.png |
| 38 | Medication Deactivated | 563:2537 | Aceptada proporcionalmente; estado real, historial, aviso y reactivacion explicita sin reanudar automaticamente | tata-treatment38-final-20261026.png |
| 39 | Medication Reminder Due | 563:2619 | Aceptada proporcionalmente; estado pendiente vencido real, aviso amarillo dinámico sobre barra adulta, sin afirmar entrega de push; transición Loading→Pending necesaria para captura fiable | tata-reminder39-state-transition-20261026.png |
| 40 | Voice Not Recognized | 563:2707 | Aceptada visualmente; confirmación no registrada, reintento y alternativa por toque | voice-not-recognized-verified.png |
| 41 | Dose Already Confirmed | 563:2760 | Aceptada visualmente; reintento idempotente, fecha original y aviso completo sin duplicados | dose-already-confirmed-verified.png |
| 42 | No Adherence Data | 563:2834 | Aceptada visualmente | history-insufficient-final.png |
| 43 | Treatment Incomplete | 563:2942 | Aceptada proporcionalmente; validacion real, aviso y retorno a borrador conservado | tata-treatment43-incomplete-20261026.png |
| 44 | Treatment Paused | 563:3010 | Aceptada proporcionalmente; estado servidor, Reanudar y aviso anclado | tata-treatment44-paused-20261026.png |
| 45 | Treatment Access Denied | 563:3092 | Aceptada proporcionalmente; icono original, panel restringido y retorno al selector sin datos clinicos | tata-treatment45-restricted-20261026.png |
| 46 | No Next Dose | 563:3178 | Aceptada proporcionalmente; tarjeta lavanda e ilustracion original, aviso anclado, agenda funcional y voz deshabilitada; sin dosis ficticia ni vacio inferido de desconexion | tata-no-next-dose-final-20261025.png |
| 47 | Dose Detail - Pending | 563:3266 | Aceptada visualmente; aviso pendiente y confirmación nativa accesible por desplazamiento | dose-detail-pending-final.png |
| 48 | Dose Detail - Confirmed | 563:3370 | Aceptada visualmente; aviso verde y confirmación retirada | dose-detail-confirmed-final.png |
| 49 | Dose Detail - Late | 563:3474 | Aceptada visualmente; siguiente toma real y estado tardío localizado | dose-detail-late-final.png |
| 50 | Dose Detail - Omitted | 563:3578 | Aceptada visualmente; aviso de omisión y barra nativa | dose-detail-omitted-final.png |
| 51 | Reinforced Reminder | 563:3682 | Aceptada visualmente; aviso sobre la barra, voz funcional y retirada cuando deja de estar pendiente | reinforced-reminder-review.png |
| 52 | Late Dose Confirmed | 563:3770 | Aceptada visualmente; fecha original, próxima toma real y aviso de clasificación tardía | late-dose-confirmed-verified.png |
| 53 | Omission Preserved | 563:3844 | Aceptada visualmente; horario programado, próxima toma real y confirmación deshabilitada | omission-preserved-verified.png |
| 54 | Agenda With Statuses | 563:3918 | Aceptada visualmente | agenda-final.png |
| 55 | Empty Intake History | 563:4037 | Aceptada visualmente | history-empty-final.png |
| 56 | Contact Unavailable | 563:4145 | Aceptada proporcionalmente; Contacto no disponible legible y seguimiento accesible | tata-verified-contact-unavailable-initial-20261025.png |
| 57 | Follow-up Note Saved | 563:4245 | Aceptada proporcionalmente; Confirmacion guardada visible por desplazamiento sin inventar eventos | tata-verified-followup-note-saved-20261025.png |
| 58 | Alert Attended | 563:4345 | Aceptada proporcionalmente; Estado atendido no accionable, distinto de cerrar o confirmar dosis | tata-verified-alert-attended-initial-20261025.png |
| 59 | Adherence Period Changed | 563:4445 | Aceptada visualmente; selección nativa y contenido desplazable | history-period-final.png |
| 60 | Insufficient Evidence | 563:4550 | Aceptada visualmente; estado vacio sin recomendaciones inventadas y retorno nativo | tata-recommendations-isolated-root.png |
| 61 | Large Text Enabled | 563:4638 | Aceptada proporcionalmente; tipografia ampliada real y aviso de guardado alcanzable mediante desplazamiento nativo | tata-figma-acceptance-accessibility-20261025/large-feedback.png |
| 62 | High Contrast Enabled | 563:4751 | Aceptada proporcionalmente; bordes de alto contraste, textos, controles y aviso especifico visibles | tata-figma-acceptance-accessibility-20261025/contrast.png |
| 63 | Reduced Motion Enabled | 563:4864 | Aceptada proporcionalmente; iconos y switches completos, aviso Movimiento reducido visible y barra persistente | tata-figma-acceptance-accessibility-20261025/motion.png |
| 64 | Reading Assistance Enabled | 563:4977 | Aceptada proporcionalmente; espaciado tipografico de lectura y confirmacion alcanzable mediante desplazamiento | tata-figma-acceptance-accessibility-20261025/reading-feedback.png |
| 65 | Notification Preferences Saved | 563:5090 | Aceptada visualmente; aviso de guardado visible con los cinco grupos | notifications-full-saved-verified.png |
| 66 | Invalid Inventory Quantity | 563:5213 | Aceptada visualmente; conserva el valor inválido para corregirlo y muestra ayuda nativa | inventory-invalid-final.png |
| 67 | Stock Replenished | 563:5284 | Aceptada visualmente; stock actualizado y fechas en español | inventory-replenished-final.png |
| 68 | Subscription Updated | 563:5354 | Aceptada proporcionalmente; confirmación verde real tras respuesta exitosa y renovación de fixture, sin inventar indicador de ciclo | tata-plan68-native-20261026.png |
| 69 | External Destination Unavailable | 563:5425 | Aceptada proporcionalmente; contacto sin destino real no accionable, catálogo y tabla de comparación dinámicos | tata-landing69-contact-unavailable-20261026.png |
| 70 | Internationalization | 665:183 | Aceptada visualmente en ES/EN; selector y recursos nativos, persistencia por cuenta; el recorrido completo entre pantallas conserva validación funcional independiente | tata-figma70-es-20261026.png / tata-figma70-en-20261026.png |

## My Medications: integración y aceptación

El catálogo usa `GET /api/v1/me/medications`, con la identidad derivada de la sesión PIN. La próxima dosis procede del contrato de Intake y abre su detalle real. El historial agrupa medicamentos sin tratamiento activo; los estados de carga, error y catálogo vacío son nativos.

La composición usa tarjetas, botones, texto y distribución Compose. La ilustración y los iconos de mañana/noche proceden de Figma. La barra adulta compartida abre Inicio, Medicamentos y Agenda. Agregar informa que el cuidador administra los tratamientos.

La captura se compara con `563:361`: encabezado, destacado, próxima dosis, filtros, cuadrícula y barra inferior. El contador usa los datos recibidos: el mockup muestra cinco medicamentos distintos y un contador de cuatro. La validación visual usa fixtures aislados; la comprobación de Render responde con 502 o timeout y no confirma integración contra el despliegue público.

Notas del adulto: GET/POST `/api/v1/me/notes`, categorías `MEDICATION` y `ROUTINE`, propietario derivado de sesión PIN. Se verifican aislamiento entre propietarios, orden persistido y rechazo de sesiones de cuidador/configuración. Navegación Inicio, Medicamentos, Agenda y Notas disponible sin acumular destinos repetidos. La comprobación visual usa datos aislados; Render conserva la limitación de verificación pública registrada.

Toma confirmada: próxima toma consultada después del registro, sin duplicar la toma confirmada. Un fallo de esa consulta conserva la confirmación exitosa. La tarjeta comunica progreso registrado y no afirma entrega de notificaciones familiares. Confeti y marca de éxito nativos; iconos vectoriales y medicamento en su slot de diseño.

La ilustración del vaso y la planta de los detalles 49 y 50 utiliza el original de Figma de 100×100 píxeles. Los iconos conservan SVG de 20/22 dp; no se amplía artificialmente el bitmap para atribuirle más detalle. Los datos de nombre, horario, dosis e indicaciones proceden del contrato.


## Checkpoint funcional y visual de suscripcion (2026-10-24)

Se conservan las 41 aceptaciones anteriores; las variantes 28 y 68 siguen en revision. Figma MCP devuelve los contextos completos de 563:1701 y 563:5354. Se compararon las capturas actuales de Tata_Core_API_36: `C:/Users/david/.codex/tmp/tata-plan-base-final-20261024.png` y `C:/Users/david/.codex/tmp/tata-plan-updated-final-20261024.png`.

Se corrigen el estado a la derecha, el precio recibido del servidor en renovacion, las fechas compactas segun locale y el gradiente de la tarjeta actual. El test espera el titulo de cada variante y captura Compose root para evitar registrar el frame anterior. Quedan diferencias en descripciones de planes y distribucion de capacidades; el fixture Esencial usado en esta captura contiene solo recordatorios y debe ampliarse a las capacidades reales antes de aprobar la equivalencia. La barra de ciclo requiere un dato contractual que no existe. No se fabrica porcentaje ni fecha.

Funcionalidad: las operaciones quedan ligadas a la cuenta cuidadora de pantalla con token capturado y descarte tras cambio de sesion. El estado elimina informacion previa al perder acceso y descarta respuestas de recargas canceladas. Cinco regresiones nuevas pasan. Gate final `C:/Users/david/.codex/tmp/tata-subscription-final-20261024.log`: BUILD SUCCESSFUL en 2m9s, 752 pruebas JVM en 141 suites, cero fallos/errores, produccion y androidTest compilados, 1 prueba instrumentada en dispositivo con 0 fallos. `git diff --check` correcto.

Backend real: catalogo 200; login demo 200; sesion actual 200 CAREGIVER; lectura de suscripcion propia 200 con propietario coincidente, ESSENTIAL y renovacion ausente; revocacion de la sesion de prueba 204. Resultado sanitizado: `C:/Users/david/.codex/tmp/tata-subscription-live-result-20261024.json`. No se modifica el plan compartido; PUT permanece sin prueba live en este bloque. La evidencia de UI usa fixtures aislados y no se presenta como recorrido autenticado completo.


### Continuacion de suscripcion: capacidades y recuperacion (2026-10-24)

Las tarjetas describen las capacidades recibidas y destacan las adicionales cuando el plan incluye las funciones base. No se anuncian multiples canales como capacidad de suscripcion porque el contrato no los declara. El fixture Esencial ahora representa las tres capacidades reales del catalogo. Evidencia visual actual: `C:/Users/david/.codex/tmp/tata-plan-capabilities-20261024.png`. Se mantienen 41 aceptaciones: 28 y 68 conservan el indicador de ciclo como diferencia dependiente de contrato; suscripciones sin renovacion muestran No disponible.

Al perder la sesion o dejar de estar activa la cuenta, se elimina el plan anterior, la seleccion y el dialogo. La pantalla ofrece Iniciar sesion y el host conecta cierre de sesion y acceso con limpieza del back stack. Los errores de red durante una recarga muestran aviso y reintento sin descartar el plan cargado.

Verificacion: `tata-subscription-completion-final-20261024.log`, BUILD SUCCESSFUL en 1m10s, 754 pruebas JVM en 142 suites sin fallos/errores y compilacion Kotlin/AndroidTest correctas. El filtro agrupado ejecuto solo captureOnboardingAndPlanVariants; la recuperacion se ejecuto por separado en `tata-subscription-recovery-device-20261024.log`, BUILD SUCCESSFUL 20s, subscriptionSessionFailureOffersSignInInsteadOfRetry pasa. Son dos metodos instrumentados diferentes comprobados, no un resultado inferido del filtro. La prueba del boton verifica el callback de acceso; no certifica por si sola todo el recorrido autenticado del host. Sin nuevas escrituras de suscripcion en la cuenta demo.


## Accesibilidad: barra persistente y capturas (2026-10-24)

Figma MCP devuelve contexto y screenshots de 563:618, 563:4638, 563:4751, 563:4864 y 563:4977. Se incorpora un slot Compose de navegacion al pie fuera del contenido desplazable, propagado por Route y registrar del BC. El host aporta barra adulta con rutas existentes cuando la pantalla procede del home adulto del mismo identificador; en el shell cuidador aporta su barra. Esta seleccion de shell no concede permisos. Back pasa a target 48dp. No se cambia notificaciones ni su aceptacion previa.

Gate tata-accessibility-navigation-final-20261024.log: BUILD SUCCESSFUL 1m45s, compilacion y JVM correctos y una prueba instrumentada captura las cinco variantes. Capturas inspeccionadas: tata-accessibility-base-20261024.png, tata-accessibility-large-20261024.png, tata-accessibility-contrast-20261024.png en .codex/tmp. Barra visible, texto grande sin clipping observado. No se aceptan las cinco variantes: falta tema oscuro funcional (sin propiedad/endpoint ni tema oscuro actual), hay diferencia de avisos y falta igualar estados equivalentes en fixtures. Conservadas 41 aceptaciones.

AccessibilityScreenTest: controles y bloqueo durante guardado pasan. La nueva prueba del pie fallo por buscar texto espanol bajo locale inglesa; se corrige a recursos de targetContext y se repite en tata-accessibility-interaction-final-20261024.log. Esperar resultado de sesion 44894; no inferir exito. Sin stage/commit/push; conservar indice previo.

Resultado posterior: la repeticion de AccessibilityScreenTest termina BUILD SUCCESSFUL en 1m26s; pasan los dos metodos instrumentados. El pie permanece visible tras scroll y comunica Agenda mediante callback. El recorrido real de cada ruta del host conserva validacion independiente pendiente.

Accesibilidad, avisos verificados (2026-10-24): gate tata-accessibility-banners-20261024.log BUILD SUCCESSFUL en 1m31s; compilacion de produccion/AndroidTest, suite JVM y captureAccessibilityVariants instrumentado correctos. Inspeccionada captura nueva tata-accessibility-banner-contrast-20261024.png: titulo especifico y aviso compacto visibles con barra persistente y sin recorte. Se mantienen las cinco variantes como parciales por tema oscuro y equivalencia de estados de referencia; conteo global 41 aceptadas. No quedan procesos de build pendientes de este bloque. Sin stage/commit/push; indice 198 rutas.

## Analytics: recomendaciones y evidencia insuficiente (2026-10-24)

Contextos Figma completos 563:1767 y 563:4550 consultados por MCP. Se corrigen titulo serif29sp, posicion relativa del estado insuficiente (separacion75dp y boton64dp debajo), rol semantico Button y altura minima50dp del retorno. Textos principales pasan a recursos ES/es-419/EN. Concentracion conserva datos reales, no se fuerzan dimensiones de matriz de produccion.

Se detecto boton Aplicar ajustes sin callback de host. Analytics registrar exige callback y lo propaga a Route; host abre TreatmentList para el adulto coincidente en FamilySummary, sin mutar tratamientos automaticamente. Sin contexto familiar valido vuelve al historial; este fallback y el recorrido host requieren prueba instrumentada independiente.

Gate tata-recommendations-visual-20261024.log BUILD SUCCESSFUL56s: compilacion, JVM y captureRecommendationsVariants instrumentado pasan. Capturas tata-recommendations-insufficient-20261024.png y tata-recommendations-content-20261024.png en .codex/tmp. Insuficiente se aproxima a referencia; no se aprueba aun. Fixture contenido tiene matriz7x4 y una recomendacion, frente a3x7 y3 tarjetas Figma, y captura muestra scroll heredado; debe corregirse fixture/estado de scroll antes de validar equivalencia. Mantener datos dinamicos en produccion, no fabricar tres recomendaciones. Conteo aceptado41 sin cambio.

Siguiente: fixture equivalente de recomendaciones, revisar restauracion de scroll por variante y anclaje de Aplicar ajustes; verificar callback hacia tratamiento. No stage/commit/push; indice198 conservado.

## Recomendaciones con datos equivalentes y accion anclada (2026-10-24)

Variante20 aceptada proporcionalmente contra563:1767 mediante `C:/Users/david/.codex/tmp/tata-recommendations-anchored-content.png`: titulo serif, panel degradado, matriz3x7 del fixture equivalente, tres tarjetas con badges, barra del cuidador y accion fija sobre la barra. Los datos de produccion siguen dinamicos. La prueba verifica callback de Aplicar ajustes sin scroll y retorno al historial. No certifica el recorrido completo del host hacia Treatment ni una mutacion de tratamientos.

Gate tata-recommendations-anchor-20261024.log BUILD SUCCESSFUL28s, suite JVM y compilacion AndroidTest correctas, captura/interaccion instrumentada pasa. La variante60 sigue pendiente: captura Compose tras transicion pierde elementos estaticos y captura de ventana aislada sale en blanco. Esta ultima se rechaza, aunque las aserciones semanticas de titulo/boton pasan. Test aislado tata-recommendations-insufficient-isolated.log pasa1m3s; no se usa su PNG como evidencia visual valida ni se infiere defecto de producto sin verificarlo en ejecucion normal.

Conteo vigente42 aceptadas,23 implementadas pendientes y5 sin implementacion especifica. Siguiente trabajo: resolver evidencia de60 en ejecucion normal o continuar otro estado verificable conservando esa limitacion. Sin stage/commit/push, indice198 conservado.

## Evidencia insuficiente: captura completa y aceptacion (2026-10-24)

Variante60 aceptada proporcionalmente frente a563:4550 con `C:/Users/david/.codex/tmp/tata-recommendations-isolated-root.png`: titulo serif, periodo30 dias, panel226dp, texto centrado, retorno con borde y barra cuidadora. Captura aislada de Compose completa; se descartan las capturas anteriores incompletas/de ventana. No fue necesario modificar el renderizado de produccion. La variante se representa sin metricas ni recomendaciones fabricadas.

`tata-recommendations-insufficient-root-final.log` BUILD SUCCESSFUL39s; un test instrumentado comprueba titulo y retorno visibles antes de capturar. El callback de retorno ya paso en captureRecommendationsVariants. Esta evidencia cierra fidelidad de60, no la integracion completa del host/backend. Conteo43 aceptadas,22 implementadas pendientes,5 sin implementacion especifica. Sin stage/commit/push; indice198 conservado.

### Monitoring: revisión del 24 de octubre

La referencia 563:819 se vuelve a consultar mediante MCP. La lista mantiene tres filtros de seguimiento con selección accesible, cuerpo redondeado y área táctil mínima de 48 dp. La prueba instrumentada `AlertFiltersTest` ejecuta las tres selecciones. El diseño requiere además próximas tomas y tomas confirmadas: estos eventos no equivalen al estado ATTENDED de una alerta. Los contratos existentes de agenda e historial proporcionan sus datos, pero aún falta componerlos en esta pantalla. La variante 17 conserva su estado pendiente y el total aceptado permanece en 43/70.

El detalle de alerta y las notas descartan contenido y editor al recibir acceso denegado, sesión requerida o sesión cambiada durante sus operaciones. Se comprueba cancelación antes de publicar una lectura de notas. La compilación y las pruebas JVM de este cambio se registran en `tata-notes-access-20261024.log`; la ejecución instrumentada de filtros se registra en `tata-alert-filter-device-20261024.log`, ambos en el directorio local de evidencia de Codex.

### Alert timeline integration, 2026-10-24

La pantalla incorpora cuatro filtros y tarjetas de dosis obtenidas de la agenda protegida. Las alertas mantienen su detalle y las tarjetas de dosis abren el resumen mediante la navegacion existente. La lectura combina estado, agenda y perfil con credenciales capturadas; rechaza identidades, duplicados y fechas inconsistentes. Los resultados de toma y seguimiento permanecen separados.

Evidencia local: `tata-alerts-timeline-utf8-20261024.png` inspeccionada; gate `tata-timeline-utf8-20261024.log` correcto, 772 pruebas JVM y ejecucion instrumentada de `captureAlertTimeline`. La variante 17 sigue pendiente por resumen superior, fechas, distintivos y distribucion. Falta verificar esta lectura conjunta contra backend desplegado y una regresion especifica de cambio de sesion. Total aceptado: 43/70.


### 2026-10-24 Dynamic alert summary

- Added native AlertTimelineSummary matching the rounded gradient summary slot. Actual pending count selects warning styling; zero pending selects green summary, original botanical58x72 and truthful no-pending copy. Does not infer clinical severity missing from backend contract.
- Inspected tata-alerts-summary-20261024.png and tata-alerts-summary-clear-20261024.png: summary, four filters, scheduled/confirmed cards, badge and bottom navigation render without clipping at default text size. Clear state preserves dose events.
- Gate tata-alert-summary-device-final-20261024.log successful19s: full JVM suite and instrumented timeline. Extended device test switches to no-pending state, asserts clear title and confirmed card; tata-alert-summary-clear-20261024.log successful17s. Initial missing imports fixed before successful gate.
- Variant17 remains pending: relative upcoming time, pending card compact time/name, header date and final proportional spacing still need comparison. Live deployment timed out on previous login; not certified. Count remains43/70.


### 2026-10-24 Alert timeline time labels

- Added locale resource-backed remaining minutes/hours for upcoming dose cards from scheduledAt minus loaded asOf. Device fixture asserts En2h45min; no hardcoded production time. Pending alert row now uses short localized time plus actual selected adult name.
- Inspected tata-alerts-relative-final-20261024.png: remaining time below medication, matching reference reading order; pending and upcoming time/name concise; badge and navigation visible without clipping. Initial screenshot exposed line order difference and was corrected before final capture.
- tata-alert-relative-final-20261024.log BUILD SUCCESSFUL20s: production compile, complete JVM suite and instrumented captureAlertTimeline. Existing index198 preserved; diff check clean.
- Variant17 remains pending final proportional/typographic comparison, dynamic header date/title choices and live deployment evidence. Other Monitoring variants still outstanding;43/70 accepted unchanged.


### 2026-10-24 Alert detail foreground recovery

- Prior turn was progress: dynamic relative time implementation and inspected capture.
- Found detail route lacked foreground refresh after external contact apps/background. Added lifecycle observer: initial ON_RESUME uses ViewModel initial reads, later resumes refresh alert/contact; observer unregisters on disposal. Refresh skips active status mutation or note save.
- Added regressions for revocation on returning and for preserving an in-flight mutation. Gate tata-alert-detail-resume-20261024.log BUILD SUCCESSFUL8s, 775 JVM tests/145 suites, zero failures/errors; production and AndroidTest compilation passed; diff check clean. Lifecycle observer itself still requires device lifecycle verification, distinct from these VM tests.
- Figma context563:918 currently returned reauthentication required. Asked reconnect asynchronously; no new visual acceptance. Detail visuals left intact pending reference. Count43/70; live integration remains unverified after previous login timeout.
- Index198 unchanged. No stage/commit/push.


### 2026-10-24 Real route lifecycle verification

- Added AlertDetailLifecycleTest using ComponentActivity and actual AlertDetailRoute/ViewModel factory on emulator. Verifies initial medication visible, one initial protected read, activity CREATED/background then RESUMED, second read denied, protected medication removed. No production repository or backend data mutations.
- tata-alert-detail-lifecycle-visible-20261024.log BUILD SUCCESSFUL12s, targeted instrumented test executed. Initial compile import error corrected; earlier run passed27s and test was strengthened to assert initial content presence before revocation.
- This resolves device lifecycle evidence gap from previous checkpoint, distinct from VM regressions. Previous full JVM baseline remains775/145 with zero failures/errors; no production edits this turn.
- Figma detail reauthentication still pending; visual acceptance43/70 unchanged. Index198 preserved and diff check clean.


### 2026-10-24 Upcoming timer freshness

- Found relative time/filter asOf stayed frozen while screen remained visible. AlertsRoute now runs minute updates only in RESUMED lifecycle via repeatOnLifecycle; cancellation stops work in background/disposal. AlertsViewModel updates asOf without network calls; day rollover requests fresh day evidence. Does not infer dose outcomes when scheduled time arrives.
- Added standard injected Clock defaulting to device zone; query day, asOf and ticker use same clock. Deterministic fixed-time regression verifies an upcoming dose leaves filter when due, preserves chosen filter and does not add a network call.
- tata-alert-clock-final-20261024.log BUILD SUCCESSFUL, compile production/AndroidTest and 776 JVM tests with zero failures/errors. Initial fixed-clock test omitted the injected clock and failed; corrected before successful gate. Route timer lifecycle scheduling itself not yet instrumented.
- Visual count43/70 unchanged. Figma reauthentication pending; no stage/commit/push, index198 conserved.


### 2026-10-24 Deployed timeline contracts and route reentry

- Live read-only contract recheck succeeded: public plans200, demo login200, current session200, caregiver links200 (2), selected adult status200 (7 alerts), agenda200 (0 doses for queried local day), profile200 with matching adult ID/name; test session logout204. Sanitized evidence tata-timeline-live-recheck-20261024.json, no tokens or personal payloads logged. No link/treatment writes.
- This proves deployed endpoint availability/schema basics; not an end-to-end Android Retrofit integration nor nonempty deployed upcoming/confirmed states. Those remain separately unproven. Prior timeout is superseded for endpoint availability.
- Fixed detail route reentry: initial-resume history belongs to ViewModel, not disposable Compose observer, so returning to the same ViewModel after route recreation rechecks access. Regression added.
- tata-detail-reentry-20261024.log BUILD SUCCESSFUL16s: production compilation, complete JVM suite (777 tests expected; XML count below checked separately) and actual AlertDetailLifecycleTest on emulator passed. No new visual acceptance;43/70.
- No Git index mutation or publication;198 paths preserved.


### 2026-10-24 Production HTTP timeline integration

- Added AlertTimelineHttpIntegrationTest with MockWebServer and actual ApiClientFactory, Retrofit service, JSON boundary converter, Gson DTOs, mapper and query handler. Nonempty PENDING/CONFIRMED evidence sorted correctly; time-zone query boundaries and Authorization checked for status/agenda/profile. Forbidden agenda discards aggregate and stops profile request.
- Gate tata-timeline-http-20261024.log BUILD SUCCESSFUL: production/AndroidTest compilation and complete JVM suite, 779 tests expected (XML verified separately). This is integration with deterministic HTTP, not deployed nonempty agenda evidence or full UI/device networking. Previous live endpoint verification remains valid.
- Retried Figma563:918, still reauthentication required; no new request to user (existing request pending). Other functional work available; goal not blocked or complete. Visual count43/70 unchanged.


### 2026-10-24 Alert dose navigation correction

- Found dose cards delegated to Home tab, which only popped an existing FamilySummary, potentially failing without parent stack or returning to another adult. Added explicit onOpenDoseSummary callback from Screen through Route, passing loaded profile name to host; host supplies route caregiver/adult IDs.
- New app navigation helper openFamilySummary pops only a matching caregiver/adult summary; otherwise navigates a summary with correctly encoded identities/name. No new route contracts or BC cross imports.
- Gate tata-alert-dose-navigation-20261024.log successful8s: production/AndroidTest compilation and full JVM suite (779 baseline). AlertSummaryNavigationTest executed with actual Compose NavHost on emulator, no parent, different adult and matching summary return; tata-alert-summary-navigation-device-20261024.log successful13s. Full rendered card tap through AppContainer remains a separate UI integration case, not claimed by helper test.
- Visual count43/70 unchanged; diff check clean and index198 preserved.


### 2026-10-24 Acceptance of Alerts variant17

Compared successful cached MCP context/screenshot563:819 (retrieved in this work session) with inspected final emulator capture `C:/Users/david/.codex/tmp/tata-alerts-list-final-20261024.png`. Native serif heading, four filters, orange/lavender/blue cards, original alert/reminder/check assets, confirmed badge, shadows and caregiver navigation are coherent under the agreed proportional criterion. Removed list alert icon background, aligned42dp icon slots, date13sp, pending title and short time/name. No clipping at tested default text scale.

Accepted dynamic/native adaptations: full localized date, real selected adult name, count-based warning/clear summary instead of unsupported clinical-severity inference, badge positioned beside medication without overlap. Remaining accessibility scales, timer lifecycle device verification and full production card-tap integration are separate functional checks. Detail/filtered variants are not approved by inheritance.

Gate `tata-alert-final-fidelity-20261024.log` successful18s: full JVM suite and instrumented all/upcoming/completed/clear capture test. Total44/70 accepted;21 implemented pending;5 without specific implementation. Previous chronological43/70 entries describe earlier checkpoints.


### Detail profile device evidence

- tata-detail-profile-device-final-20261024.log BUILD SUCCESSFUL1m21s after emulator boot. captureAlertProfileDetails executed: displayed actual-shaped fixture name and calculated68years, summary callback clicked exactly once. Inspected tata-alert-detail-profile-20261024.png: profile card correctly renders initials, full name, age and working summary action.
- This is fixture/device evidence for the new profile card, not full detail acceptance: hero dose quantity, actual reminder event timeline and compact follow-up composition still differ from Figma. New card queries backend profile in production, no fake age/avatar.
- Full JVM gate tata-detail-profile-access-final-20261024.log passed; profile date/identity contract tests and personal notes auth regression included. Count44/70 unchanged; index198 preserved.


### Detail hero device evidence

- tata-detail-dose-device-20261024.log BUILD SUCCESSFUL2m32s. Test asserted2tabletas (intentionally different from mockup1tableta), name/age and profile callback. Inspected tata-alert-detail-dose-20261024.png: real quantity and native gradient hero/tablet visible correctly.
- Detail18 still not accepted: reminder timeline absent, information panel differs, actions too tall for initial viewport (note action requires scroll). These remain concrete next work, not a finished module.
- Previous gate tata-detail-dose-20261024.log passed full JVM suite (784 tests expected; XML count verified separately), production and AndroidTest compilation. Index198 preserved; diff check clean.


### Compact follow-up device result

- Targeted retry tata-followup-compact-ready-20261024.log BUILD SUCCESSFUL2m2s. Asserts Contactar a Rosa and Agregar nota de seguimiento visible without scroll, actual-shaped2tabletas/profile and summary click. Inspected tata-followup-compact-ready-20261024.png: complete gradient action panel fits above navigation at default text scale, including contact, attend, close and add note.
- Earlier PixelCopy timeout is recorded, not counted as pass. Successful final capture replaces it for compact ready state. Existing full JVM baseline784/148 passed in preceding gate; current device-only changes are fixture assertions.
- Detail18 remains partial due reminder timeline and exact profile destination;44/70 accepted unchanged. No stage/commit/push;index198 preserved.


### Recorded timeline device evidence

- Final gate tata-detail-timeline-layout-20261025.log BUILD SUCCESSFUL1m42s: production compile, full JVM suite and captureAlertProfileDetails instrumented. Note action initial visibility assertion passed after layout correction.
- Inspected tata-detail-timeline-20261025.png: scheduled/opened actual times, current status, original icons/connector, profile/dose and all main actions visible above navigation. Administrative panel is below action section and expandable.
- Not full reminder-history acceptance: backend stores one reinforced reminder delivery timestamp internally but exposes no read endpoint, so first/second sent events in Figma remain unimplemented. This is a concrete backend contract gap, not a reason to fabricate sent events. Detail18 stays pending;44/70 accepted. No Git publication/index changes.


### 2026-10-25 Recorded reminder history contract and Android client

- Added protected GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/timeline?caregiverId=... in backend. Reads actual omission delivery timestamp through public ACL; includes scheduled/generated/closed facts sorted chronologically. Does not infer delivery from scheduling.
- Backend full gate tata-backend-timeline-full-20261025.log: 275 tests, zero failures/errors. Subsequent actual HTTP authorization gate tata-backend-timeline-http-access-20261025.log passed 7 cases including anonymous401, unlinked403 and forged selector403. Endpoint local only, not deployed.
- Android uses dedicated history read port, production Retrofit adapter, session boundary and independent ViewModel state. Malformed/unknown/missing events rejected. Access denial invalidates protected detail; account changes discard responses. Refreshes after status updates. Older deployed server without endpoint falls back to known scheduled/generated facts, never simulated sends.
- tata-history-client-final-20261025.log BUILD SUCCESSFUL55s: production and AndroidTest compilation plus full JVM gate. New HTTP tests verify real Retrofit parsing, auth header, chronological events and error mapping.
- Original Figma sent-reminder bell restored for actual recorded delivery. Device validation in progress; accepted visual count remains44/70. No stage/commit/push, index198 retained.

- Current JVM XML confirms786 tests/149 suites, zero failures/errors. First device gate tata-history-device-20261025.log reached device task but failed because emulator offline; not counted as interface pass. Fixture now includes actual-shaped recorded reminder event at schedule+5min and generated alert+10min, asserts sent label; fixture compilation tata-history-fixture-compile-20261025.log BUILD SUCCESSFUL1m52s. Retry running after ADB device returned online.

- User steering2026-10-25: backend publication authorized after more accumulated work, to develop/main; requested replacing prior minor changes history. Do not publish this small endpoint immediately. Fast-forward cannot itself overwrite a commit; inspect remote divergence/concurrent contributions before any eventual rewrite. Android Git restriction unchanged.
- Device retry tata-history-device-retry-20261025.log reached real UI: reminder label passed, but note action initial visibility failed after extra event. Corrected timeline to omit redundant OPEN current-status row when authoritative history exists (status still in header); ATTENDED/CLOSED facts retained. Full compile/JVM/device gate tata-history-layout-final-20261025.log running. Do not count prior device failure as acceptance.

- Final corrected gate tata-history-layout-final-20261025.log BUILD SUCCESSFUL37s: production compile, AndroidTest compile, full JVM suite and actual captureAlertProfileDetails instrumented test. Recorded sent label, dose/profile callback and contact/note initial visibility pass. Inspected tata-history-layout-final-20261025.png: original bell, three chronologically distinct facts and actions visible. Fixture uses deterministic HTTP-shaped data, not proof of deployed endpoint. Detail18 still pending full variant/profile parity; accepted count44/70 unchanged. Minor connector tail can be polished later. Index198 preserved, no publication.


### 2026-10-25 Omission transport audit: prevent false delivery evidence

- Linked Person563:1121 Figma get_design_context returned reauthentication, so no visual acceptance or design fabricated. Existing44/70 accepted count unchanged.
- Inspected actual backend transport: the only PushProviderClient bean was LoggingPushProviderClient, logging clinical payload and returning normally. PushNotificationAdapter treated it as success and persisted reinforcedReminderSentAt, so the new timeline could expose logging as sent evidence. Corrected fallback to fail explicitly with provider-not-configured and avoid logging the clinical payload. No runtime push provider is configured by this change.
- Initial targeted gate found2 flow tests depended implicitly on logging success. Successful integration scenarios now explicitly control provider acceptance using MockitoBean, and a new real omission-flow case checks provider failure leaves delivery timestamp null. Adapter regression verifies both reminder and caregiver alert fail with unconfigured provider. This preserves clinical omission/alert generation separately from delivery outcome.
- Full backend gate tata-push-truthful-full-20261025.log running; no publication. Android unchanged this turn, index198 preserved. Do not claim previously persisted timestamps prove actual transport; historical rows lack provider provenance and have not been rewritten.
- Topic audit: Android still subscribes legacy per-adult topics; no unsubscribe implementation exists. Backend recipient abstraction uses topic-style names and only logging fallback. Firebase official https://firebase.google.com/docs/cloud-messaging/topic-messaging recommends registration-token targeting for secure individual/small-group delivery. Remaining work requires actual authorized transport plus migration of persisted legacy subscriptions, not only local display filtering.

- Final backend gate tata-push-truthful-final-20261025.log BUILD SUCCESS2m13s:278 tests, zero failures/errors. Earlier full run exposed AutomaticUnconfirmedIntakeIntegrationTest assuming logging=delivery; now it asserts no delivery timestamp with fallback while still proving actual omission status propagates to Intake. Explicit provider-success integration remains separately covered. Backend index0, Android index198, diff checks clean. No Git mutations/publication.

- Actual device gate tata-treatment-review-visual-device-20261025.log BUILD SUCCESS1m52s, both visible-action/agenda assertions passed. Pulled and inspected tata-treatment-review-figma-20261025.png: serif, original medication24dp, step dots18dp, two-column data,15min actual fixture delay, two configured agenda times. Visual remains partial: native text padding makes stepper/pauta taller and agenda too close to lower inset; shadows stronger than reference. Do not accept variant23 yet;44/70 unchanged. Next pass tighten native line heights/includeFontPadding and shadow geometry, then recapture same fixture. No report changes or Git mutation.


### 2026-10-25 Treatment review23 accepted proportionally

- Reduced inherited Material line heights to reference-sized typography; softened native field/regimen shadows. No fixed clipping heights on text content. Final tata-treatment-review-proportions-20261025.log BUILD SUCCESS20s: full JVM suite, production/AndroidTest compilation and captureTreatmentReviewFigma actual instrumented case. Activation and agenda visible assertions pass.
- Inspected tata-treatment-review-proportions-20261025.png against successful Figma563:1490 screenshot/context: serif29 title, compact gradient stepper, name/medication fields, original24dp medication and18dp dots, two-column regimen, lavender follow-up and full agenda above system inset. Dynamic adaptations accepted:2configured times rather than3fictional examples;15min actual fixture delay;step4active for review. Agenda bars are schedule markers, not adherence percentages. Native system bars retained. Variant23 accepted; count45/70,20implemented pending,5absent. Does not approve remaining creation/form/paused variants or deployed integration.
- No stage/commit/push; report untouched. Canonical visual ledger row23 updated.


### 2026-10-25 Independent-chat continuation verified: Notes27 accepted

- Final tata-notes-resume-final-20261025.log BUILD SUCCESS4m47s:801 JVM tests/153 suites, zero failures/errors, plus two actual instrumented notes/profile/action cases. Last cosmetic plus-button correction verified separately by tata-notes-final-control-20261025.log BUILD SUCCESS2m38s, both cases pass. Captures no longer silently succeed on PixelCopy failure.
- Inspected final tata-notes-accepted-20261025.png against current Figma576:2859 full context/render: serif title, actual named subtitle, thin native48dp plus, authorized profile/age/initials, real Inter weights, original36dp newest-note marker, content/author/date, shared-history block, add action and Notes-selected tab bar visible. Dynamic count/body-only notes and initials accepted as truthful contract adaptations; no invented categories, titles, patient photo or author name. Approval is visual, not remote integration certification.
- Variant27 accepted proportionally. Confirmed46/70 accepted,19implemented pending,5without implementation-specific variant;65implemented. The other chat's nine provisional variants retain their evidence for focused review; not all reinstated or discarded. Existing source/API/session wrappers preserved. No report changes, no stage/commit/push, Android index198 and diff check clean.


### 2026-10-25 Alert variants18/56/57/58 accepted proportionally

- tata-alerts-proportional-final-20261025.log BUILD SUCCESS2m46s: production/AndroidTest compile,801 JVM tests with zero failures/errors and4actual MonitoringFigmaStatesTest device cases. Missing image now fails the visual gate.
- Inspected current screenshots tata-verified-alert-detail-initial-20261025.png, tata-verified-contact-unavailable-initial-20261025.png, tata-verified-followup-note-saved-initial-20261025.png plus scrolled tata-verified-followup-note-saved-20261025.png, and tata-verified-alert-attended-initial-20261025.png against each corresponding current Figma frame. Original scheduled/pending/bell resources, genuine Inter weights, serif title, medication/profile/history/action/tab hierarchy consistent. End connector fixed.
- Accepted truthful adaptations: actual date/quantity/name and initials; Verresumen targets implemented family summary (dedicated linked-person16 remains separate); only recorded reinforced send, no fictional second send; attended follows native green status palette and is not intake confirmation; explicit close action remains distinct; note feedback/actions can require native scrolling. Attended follow-up note reachability asserted after scrolling; no claim every action fits initial viewport.
- Variants18/56/57/58 accepted visually; functional callbacks/server permissions/remote deployment remain separately evidenced, not inferred from fixtures. Confirmed50/70accepted,15implemented pending,5absent,65implemented. Remaining five independent-chat provisional views46/61-64 retain evidence for focused next review. Android index198 retained; no Git mutation/report change.


### 2026-10-25 Dark preference contract and native accessibility row

- Backend now exposes PUT /api/v1/users/{userId}/preferences/dark-theme and returns darkTheme in the preference snapshot. Command/service/handler, aggregate and persistence assembler follow the existing accessibility pattern. Nullable database column reads old rows as false. Existing constructor callers remain compatible.
- Full backend baseline tata-dark-backend-contract-20261025.log:281 tests, zero failures/errors. New HTTP/H2 default/toggle/other-setting preservation and missing-value tests pass in tata-dark-backend-http-20261025.log. Actual Spring Security owner/anonymous/other-account gate tata-dark-backend-security-20261025.log:23 tests, zero failures/errors. Local contract, not deployed.
- Android now has UpdateDarkTheme command/handler, session-scoped repository operation, explicit DARK_THEME pending field, Retrofit endpoint/DTO mapping and ViewModel/DI/route wiring. Removed the temporary unconditional local-dark preservation and receipt comparison bypass. Offline intent retries only its field; mismatched receipt stays pending; rejected input restores previous state. Older remote read without darkTheme maps to false, not proof the remote endpoint supports writes.
- Current Figma563:4638 context/render fetched. Original24x24 moon SVG downloaded to figma_access_darkmode.svg (875bytes), called at24dp within existing34dp lavender badge. View group now includes the native interactive dark-theme row. Spanish default and English resources added. Theme-aware preference groups/help, shared default cards and adult tab keep the light palette; dark host setting and system-bar appearance are connected.
- tata-dark-android-contract-final-20261025.log BUILD SUCCESSFUL1m45s:production/AndroidTest compile and805 JVM tests, no failures/errors. After one JSON boundary case and final formatting/system-bar adjustment, tata-dark-accessibility-device-20261025.log is running production/JVM/device gates. Do not infer instrumented success from compilation; inspect XML and fresh captures before acceptance.
- IMPORTANT next scope: explicit fixed-white/pastel surfaces remain in other BCs (for example Treatment screens, CareLinkScreen and NotesScreen). Global dark-theme visual compatibility is not complete. Check these before publishing the theme as release-ready; existing light approvals are retained. Accessibility14/61-64 remain pending until fresh images are inspected. Count50/70,65implemented,15implemented pending,5absent.
- No stage/commit/push/reset or report edits. Android index198 paths remains unchanged.


### 2026-10-25 Final quota checkpoint

- tata-dark-accessibility-device-20261025.log BUILD SUCCESSFUL5m50s:806 JVM tests/153 suites zero failures/errors plus2 actual AccessibilityScreenTest cases. The multi-class argument selected only the first class; do NOT claim DataStore execution from this gate.
- Separate captureAccessibilityVariants gate tata-dark-accessibility-capture-20261025.log BUILD SUCCESSFUL39s. Pulled and inspected six fresh tata-accessibility-complete-{content,large-text,high-contrast,reduced-motion,reading-assistance,dark-theme}-20261025.png images. Complete six-row layout, original moon24dp, native switches, help and selected More tab rendered. Dark accessibility itself readable. Large-text/reading feedback requires scroll; added explicit scroll/assert/capture in a follow-up gate.
- Follow-up tata-accessibility-feedback-20261025.log running session92306 at checkpoint; inspect completion/XML and latest feedback captures. No visual counter increment yet:50/70 remains canonical. DataStore owner-isolation test compiled but still requires separate single-class device run. Explicit dark cross-BC surface migration remains required before release/publication.
- Quota changed from14%remaining to2% at next check; checkpoint target10% missed. No new Git actions; preserve198 indexed paths. Next start here, do not repeat the completed806JVM/backend23-case checks unless source changes justify them.


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


### 2026-10-25 — Siguiente grupo inspeccionado, sin aprobar
Se consultaron directamente Figma 563:2455 (Medicamento actualizado), 563:2537 (Medicamento desactivado) y 563:3178 (Sin proxima toma). Las variantes 37 y 38 muestran el resumen de tratamiento, adherencia, pauta y opciones de reactivacion; no se deben aceptar usando exclusivamente el formulario de gestion de medicamento, que representa un flujo distinto. La referencia 46 contiene una contradiccion visual: muestra el encabezado Sin proxima toma, pero conserva una hora y un contador En 25 min. El front debe preferir el dato efectivo de Intake y no fabricar una toma. Permanecen pendientes hasta capturar y contrastar los estados actuales. Conteo 55/70 inalterado.


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

### 2026-10-26 Treatment management #24

- Contexto y render actual de Figma 563:1554 cotejados con capturas nativas. Conserva título serif, perfil dinámico, pauta y estado, botones de edición/pausa, aviso de historial, desactivación dirigida, tarjeta lavanda con siete columnas y navegación anclada. Iniciales para perfiles sin foto; calendario de siete días reales y barras neutrales sin inventar umbrales clínicos.
- Porcentaje y tendencia proceden de la consulta exclusiva del tratamiento, no del resumen global del adulto. Los fixtures 92% y siete valores se limitan a AndroidTest. HTTP204 representa ausencia de evidencia; errores de red conservan su estado y reintento, y acceso revocado oculta la gestión e impide escrituras.
- tata-treatment-adherence-final-20261026.log: compilación de producción/AndroidTest, APK y 815 pruebas JVM sin fallos. tata-treatment-adherence-device-verified-20261026.log: AndroidJUnitRunner directo OK1test8.266s, apertura, gráfica, pausar/regresar/refrescar/reanudar y revocación. Dos intentos anteriores fallaron por un diálogo ANR de com.android.systemui, confirmado con foco y jerarquía UI; tras elegir Wait, el recorrido completo pasa. No se omitieron aserciones.
- Capturas inspeccionadas: C:/Users/david/.codex/tmp/tata-treatment24-adherence-top-20261026.png y tata-treatment24-adherence-bottom-20261026.png. La aceptación visual no acredita despliegue ni integración autenticada contra Render. Conteo57/70,8implementadas pendientes,5sin implementación. Índice198 y ramas conservados, sin commits ni push.

### 2026-10-26 Regimen saved #37 and paused #44

- Contextos completos y renders Figma563:2455 y563:3010 cotejados con capturas directas del emulador. Aviso verde Cambios guardados solo tras recibo de configuracion exitoso; aviso de pausa anclado sobre la barra, conservando pauta, grafica, historial y opcion Reanudar. Se mantienen calendario real, iniciales dinamicas y controles nativos.
- TreatmentStatusNotice adapta tono y tipografia compartidos, admite crecimiento de texto y anuncia el resultado con live region. Guardar no altera ACTIVE/PAUSED; nuevas operaciones, errores de validacion y refrescos eliminan el aviso anterior. El formulario conserva entradas ante fallo.
- Se encontro y corrigio una carrera real del dialogo: respuesta inmediata podia saltarse el frame isLoading=true y dejar el editor abierto. El efecto observa submitted ademas de loading/error. Prueba dedicada acredita cierre con exactamente una llamada incluso sin frame intermedio.
- tata-treatment-outcomes-race-20261026.log BUILD SUCCESS8s: produccion, AndroidTest, APKs,815JVM/154suites sin fallos. tata-treatment-outcomes-race-device-20261026.log AndroidJUnitRunner directo OK3tests9.323s: edicion real del formulario con una escritura, aviso, pausa/regreso/refresh/reanudar, revocacion y ambas respuestas de editor (fallo y exito inmediato). Intentos previos detectaron el dialogo abierto y NO se cuentan como aprobados.
- Capturas inspeccionadas C:/Users/david/.codex/tmp/tata-treatment37-updated-20261026.png y tata-treatment44-paused-20261026.png. Conteo59/70; Treatment7/10. Restan Treatment38/43/45, CareLink16, Subscription28/68 y cinco ausentes. No se acredita integracion Render ni se cambia el report.
- Figma38 incluye Reactivar medicamento, mientras el backend vigente solo desactiva, pausa tratamientos dependientes y rechaza reanudarlos con medicamento inactivo. No se simulo una reactivacion exitosa; esa diferencia funcional sigue pendiente.

### 2026-10-26 Restricted treatment #45

- Contexto completo Figma563:3092 cotejado con captura final. Titulo serif, icono de estado64 original descargado como treatment_restricted_state.svg (370bytes, root64x64), exclamacion nativa superpuesta, panel rosa226dp minimo con texto centrado, retorno explicito y barra del cuidador anclada. El icono conserva dimensiones y geometria originales; texto/panel/boton son Compose.
- TreatmentDetailRoute admite callback de retorno restringido y slot de navegacion. El host abre el selector del cuidador y retira el detalle denegado; el Back del sistema usa el mismo callback. Los tabs reutilizan contexto familiar del mismo cuidador o solicitan seleccionar persona cuando no existe contexto valido, sin inferir propietario del recurso denegado.
- tata-treatment-restricted-20261026.log BUILD SUCCESS11s,compilacion produccion/AndroidTest,APKs y815JVM sin fallos. tata-treatment-restricted-device-20261026.log AndroidJUnitRunner directo OK1test7.412s: recorrido funcional previo, revocacion durante pausa, ocultacion de pauta/acciones, barra seleccionada y retorno al selector. Captura inspeccionada C:/Users/david/.codex/tmp/tata-treatment45-restricted-20261026.png. No integracion desplegada acreditada.
- Conteo60/70,Treatment8/10. Quedan Treatment38/43,CareLink16,Subscription28/68 y cinco ausentes. Sin commits/push ni alteracion del indice198. El report no se modifica.

### 2026-10-26 Incomplete treatment #43

- Revision actual Figma563:2942 cotejada con captura nativa. Conserva campos reales, icono de medicamento y stepper SVG existentes, resumen, recordatorios, accion Completar configuracion y aviso amarillo. El estado procede de INVALID_TREATMENT_CONFIGURATION o REQUEST_VALIDATION_FAILED en configuracion; errores de red no se convierten en incompleto.
- La accion retorna al borrador del mismo cuidador/adulto/medicamento/tratamiento sin repetir configuracion ni activar. Si no existe entrada previa, crea la pauta con dosis, frecuencia, horarios, indicaciones y plazo existentes. No toma el SavedStateHandle de un tratamiento ajeno.
- tata-treatment-incomplete-final-20261026.log BUILD SUCCESS7s,produccion/AndroidTest,APKs y815JVM sin fallos. AndroidJUnitRunner directo tata-treatment-incomplete-device-20261026.log OK4tests7.651s: borrador existente, entrada directa, conservacion al volver y aislamiento del estado. El repositorio controlado recibe una configuracion rechazada y ninguna activacion. No acredita HTTP desplegado.
- Captura C:/Users/david/.codex/tmp/tata-treatment43-incomplete-20261026.png inspeccionada. Conteo61/70,Treatment9/10; quedan38,16,28,68 implementadas pendientes y cinco ausentes. Indice198 intacto, sin commits ni push.

### 2026-10-26 Medication deactivated #38

- Captura final equivalente a Figma563:2537 revisada: perfil e iniciales dinamicas, conteo real, Inactivo, pauta conservada, Editar deshabilitado, Reactivar disponible, historial protegido, grafica de tratamiento y aviso amarillo especifico sobre conservacion de historial, barra anclada. No se inventan datos productivos ni se usa imagen de pantalla.
- Backend implementa reactivacion autorizada/idempotente del medicamento bajo bloqueo. Conserva tratamientos PAUSED y no publica nuevas tomas; resume requiere otra decision. HTTP local confirma reintentos, disponibilidad, restricciones anonimo/ajeno/revocado y ausencia de escrituras rechazadas. Android confirma antes de escribir, valida recibo/id/propietario y refresca; errores no producen exito.
- tata-treatment38-final-build-20261026.log BUILD SUCCESS27s,produccion/AndroidTest,APKs,818JVM sin fallos. Directo tata-treatment38-final-device-20261026.log OK2tests4.184s: estado equivalente/accion y cancel0/confirm1 con PAUSED y conteo actualizado. Captura C:/Users/david/.codex/tmp/tata-treatment38-final-20261026.png inspeccionada. Los92 y barras solo existen en fixture.
- Conteo62/70,Treatment10/10 visual; no acredita cierre funcional total ni despliegue. Quedan CareLink16 y Subscription28/68 implementadas pendientes; cinco ausentes1/2/39/69/70. Indice198 intacto,sin commits/push,report sin cambios.

### 2026-10-26 Validacion posterior del aviso temporal

La prueba instrumentada de activacion automatica del aviso usa ahora un reloj coherente con el tiempo virtual de Compose. Verifica estado futuro sin aviso, cruce de la hora sin navegar ni interactuar y retirada tras confirmar. AndroidJUnitRunner directo: tata-handoff-clock-device-20261026.log OK1test23.606s. La regresion actual compila produccion/AndroidTest y ambos APK con833pruebasJVM159suites sin fallos (tata-handoff-validation-clock-20261026.log). No altera el conteo70/70 visual ni acredita entregaFCM o integracionRender. La consulta al contrato publicado no respondio en40segundos; contacto publico real sigue sin configurar.
