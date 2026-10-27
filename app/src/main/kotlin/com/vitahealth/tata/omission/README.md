# Omission & Escalation — Android

Este bounded context implementa **recepción, distribución local y navegación
de avisos push** del backend de Omission & Escalation (TS-05/TS-06, US-22).
Permanece en el único módulo Gradle `:app` y sigue las cuatro capas DDD/CQRS.

El **backend** crea omisiones y escalaciones y autoriza sus transiciones.
Las listas, detalles, atención, cierre y notas de alertas del cuidador
pertenecen a **Monitoring** (TS-09). El cliente Omission no tiene pantallas
Compose ni ViewModels propios; su UI es una notificación del sistema.

## Organización

```text
omission/
  domain/
    model/valueobjects/             OmissionPush, OmissionPushKind
  application/
    commands/                       FollowCaregiverAlerts, FollowDoseReminders,
                                    sus Commands y Handlers
    queries/                        ResolvePushDestinationQuery + Handler
    readmodels/                     PushDestination
    ports/                          tokens, topics, destinos y callbacks
    policies/                       OmissionPushAccessPolicy, OmissionPushTopics
    services/                       PushDeviceRegistrationCoordinator
    OmissionFailureCodes.kt
  infrastructure/
    local/                          installation UUID y destinos SharedPreferences
    push/                           FCM token, legacy topic retirement, data mapper,
                                    OmissionMessagingService
    remote/                         Retrofit API, DTOs, RemoteDeviceTokenRegistry
    security/                       OmissionNotificationSessionCleanup
  presentation/
    notifications/
      ui/                           OmissionNotifications
      mappers/                      OmissionPushIntent
```

## Responsabilidades

- **Domain**: evidencia tipada e inmutable del push, sin falsos
  agregados clínicos ni eventos de dominio. Las referencias de adulto,
  entrega y toma deben ser no vacías cuando se proporcionan, incluso
  al aplicar Kotlin `copy()`.
- **Application**: dos comandos suscriben a recordatorios/alertas;
  una consulta resuelve el destino local de navegación. Los puertos
  no dependen de Firebase ni Android. `OmissionPushTopics` conserva el
  protocolo legado del backend: se ubica en políticas de integración,
  no en un agregado clínico. La autorización revalida cuenta, rol,
  adulto y expiración tanto antes de mostrar como antes de navegar.
- **Infrastructure**: el token del dispositivo se registra en backend
  con credenciales de sesión capturadas. La coordinación maneja cambios
  de token, reintentos acotados y baja antes del cierre de sesión.
  La limpieza de notificaciones al cambiar/caducar sesión pertenece
  a `infrastructure/security`; los almacenes a `local`.
- **Presentation**: únicamente canales de notificación nativos,
  mensajes localizados, PendingIntent y parseo de extras. No se crean
  `screens/`, `viewmodel/`, `navigation/` ni `di/` ficticios.
  `app/di/AppContainer` compone los flujos de Omission con la
  navegación global de Intake y Monitoring.

## Contrato Firebase

`app/src/main/AndroidManifest.xml` registra explícitamente
`.omission.infrastructure.push.OmissionMessagingService` para
`com.google.firebase.MESSAGING_EVENT` con `android:exported="false"`.
Los mensajes data versionados requieren IDs de entrega/toma y
fecha de programación válidos. Los mensajes de tópico legado
solo se aceptan con prefijo `/topics/` y destinatario bien formado.

## Alcance y riesgos pendientes

Las pruebas JVM cubren contratos HTTP, ciclo de vida del token,
cancelación, cambio de cuenta, parseo de datos y tópicos, políticas
de destinatario, invariantes del dominio y el manifiesto.
Compilar AndroidTest no equivale a ejecutar pruebas en emulador.

Debe verificarse con el backend y un dispositivo físico la entrega
foreground/background, el permiso POST_NOTIFICATIONS, registro de
FCM real, enlaces revocados, 401/403/409, restauración de sesión
y navegación hacia tomas ya resueltas.

La app registra el dispositivo mediante el endpoint autenticado y conserva
los destinos locales para navegar. No solicita nuevas suscripciones
clínicas a tópicos. Al iniciar el proceso, LegacyPushTopicRetirement
recupera todos los tópicos inferibles de los destinos persistidos de
versiones anteriores y espera el ACK de unsubscribeFromTopic. Los fallos
transitorios se reintentan; los destinos permanecen disponibles para
repetir la retirada tras reiniciar el proceso. La retirada no depende
de la cuenta actualmente conectada.

La privacidad del transporte requiere validar esa migración con Firebase
real y completar el proveedor de entrega dirigido a tokens autorizados.
El backend sin proveedor configurado devuelve fallo de envío y no registra
recordatorios como enviados. Los filtros locales siguen siendo una
protección adicional, no una prueba de privacidad del transporte.

No se modificaron diseños Figma, no se crearon nuevas pantallas y
no se hizo commit, stage, push ni reset.

Plan único: `C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
