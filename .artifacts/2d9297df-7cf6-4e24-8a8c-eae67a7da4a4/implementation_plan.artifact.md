# Plan de Implementación de Mejoras Pomodoro

Este plan detalla las modificaciones necesarias para cumplir con todos los requisitos funcionales y de persistencia solicitados para la aplicación Pomodoro.

## Cambios Propuestos

### Componente de Persistencia y Datos

#### [MODIFY] [MainViewModel.kt](file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/viewmodel/MainViewModel.kt)
- Integrar `DataStoreManager` para persistir tareas, historial de sesiones y estado del temporizador.
- Implementar la lógica de restauración del temporizador al iniciar la aplicación para que el tiempo transcurrido en segundo plano se descuente correctamente.
- Asegurar que cada cambio en la lista de tareas o sesiones se guarde automáticamente.

#### [MODIFY] [AppData.kt](file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/storage/AppData.kt)
- Verificar que todos los campos necesarios para la persistencia estén presentes (tareas, sesiones, tiempo de finalización, si el temporizador está corriendo).

### Interfaz de Usuario (UI)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/MainActivity.kt)
- Implementar el redibujado de la lista de sesiones en `layoutSessions`.
- Manejar la visibilidad de `tvNoSessions` cuando no hay historial.
- Agregar una notificación (Toast) al finalizar el temporizador.
- Asegurar que el estado del campo de texto de nueva tarea no se pierda al rotar (usando el estado del ViewModel si es necesario, o confiando en el comportamiento por defecto de Android si tiene ID).
- (Opcional pero recomendado) Agregar un control para configurar la duración del Pomodoro.

#### [MODIFY] [activity_main.xml](file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/res/layout/activity_main.xml)
- Asegurar que los IDs coincidan con el código y agregar cualquier elemento faltante para la configuración de duración si se decide implementar.

### Lógica del Temporizador

#### [MODIFY] [MainViewModel.kt](file:///C:/Users/Tito/AndroidStudioProjects/pomodoro/app/src/main/java/com/example/pomodoro/viewmodel/MainViewModel.kt)
- Mejorar la función `registerSession` para incluir la duración real y asociarla correctamente a la tarea activa.
- Manejar el caso donde el temporizador termina mientras la app está cerrada.

## Plan de Verificación

### Pruebas Manuales
- **Persistencia**: Agregar una tarea, cerrar la app (matar el proceso) y volver a abrirla. La tarea debe seguir ahí.
- **Rotación**: Iniciar el temporizador y rotar el dispositivo. El temporizador debe continuar sin saltos ni reinicios.
- **Segundo plano**: Iniciar el temporizador, salir de la app, esperar 10 segundos, y volver a entrar. El tiempo restante debe haber disminuido 10 segundos.
- **Validación**: Intentar agregar una tarea vacía o con solo espacios. Debe mostrar un error.
- **Historial**: Completar una sesión y verificar que aparece en el historial con el nombre de la tarea activa.
- **Estado vacío**: Borrar todas las tareas y sesiones para verificar los mensajes de "No hay tareas/sesiones".

### Pruebas Automatizadas
- Se pueden añadir unit tests para `MainViewModel` para verificar la lógica de cálculo de tiempo restante y filtrado de tareas.
