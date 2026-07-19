# Walkthrough de Mejoras Pomodoro

Se han implementado todas las funcionalidades solicitadas, enfocándose en la persistencia, la experiencia de usuario y la robustez ante cambios de ciclo de vida.

## Cambios Realizados

### 1. Persistencia Completa
- **DataStore Integration**: Se integró `DataStoreManager` en el `MainViewModel` para guardar y cargar automáticamente las tareas, el historial de sesiones y el estado del temporizador.
- **AppData**: Se utiliza una clase de datos centralizada para serializar el estado completo a JSON y guardarlo en las preferencias de Android.

### 2. Lógica de Temporizador Mejorada
- **Background Support**: El temporizador ahora usa un `endTime` absoluto. Al regresar a la aplicación, se calcula el tiempo restante real, incluso si el proceso fue destruido o si el usuario estuvo fuera.
- **Configuración**: Se añadió un campo de texto y un botón para que el usuario pueda cambiar la duración de la sesión (por defecto 25 minutos).

### 3. Historial de Sesiones
- **UI de Historial**: Se implementó el redibujado de la lista de sesiones completadas en la parte inferior de la pantalla.
- **Detalles**: Cada sesión muestra el nombre de la tarea asociada, la duración y la fecha/hora de finalización.
- **Estados Vacíos**: Se manejan correctamente los mensajes de "No hay tareas" y "No hay sesiones" cuando las listas correspondientes están vacías.

### 4. Notificaciones y Validaciones
- **Notificaciones**: Se añadió un `Toast` informativo que aparece cuando una sesión de Pomodoro finaliza con éxito.
- **Validación de Tareas**: Se reforzó la validación para evitar tareas vacías o con espacios en blanco, informando al usuario mediante un error en el campo de texto.

### 5. Robustez en Rotación
- Gracias al uso de `AndroidViewModel` y `LiveData`, el estado del temporizador, la lista de tareas y la tarea seleccionada persisten sin problemas al rotar la pantalla.

## Verificación Realizada

- **Tareas**: Se verificó que el tachado y la atenuación de tareas completadas funcione correctamente.
- **Selección**: Solo se pueden seleccionar tareas que no estén completadas.
- **Timer**: Se probó el inicio, pausa, reanudación y reinicio.
- **Persistencia**: Se cerró la aplicación forzosamente y al reabrirla, los datos seguían presentes.
- **Cálculo de Tiempo**: Se inició un timer, se salió de la app por un minuto y al volver, el tiempo restante reflejaba el paso de ese minuto.

render_diffs(file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/viewmodel/MainViewModel.kt)
render_diffs(file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/MainActivity.kt)
render_diffs(file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/res/layout/activity_main.xml)
