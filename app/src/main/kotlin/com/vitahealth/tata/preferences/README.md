# preferences

Contexto funcional dentro del módulo Android `:app`.

El código se organiza en `domain`, `application`, `infrastructure` y `presentation`. Las reglas de dominio permanecen independientes de Android; la aplicación coordina los casos de uso, la infraestructura implementa los adaptadores y la presentación gestiona las pantallas y su estado.

La política de notificaciones valida los tiempos de recordatorio, avisos y escalamiento. Los contactos usan nombres y teléfonos normalizados. El repositorio distingue la actualización de silencio y canales de la actualización de política y contactos; cada respuesta contiene el estado guardado del servidor. El ViewModel conserva los valores anteriores si el guardado falla. Compose presenta tarjetas, formularios, switches y selectores de hora nativos; los SVG se usan únicamente como iconos.

