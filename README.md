# Gestión de Solicitudes – DSY1105 (Evaluación Parcial 2)

Aplicación Android que representa cómo una solicitud de un cliente (que hoy llega por WhatsApp a una empresa de servicios informáticos) queda **registrada, con código único y disponible para seguimiento**. Esta entrega implementa el **módulo de Solicitudes** definido en los lineamientos del caso *Gestión móvil y seguimiento de solicitudes de clientes*.

## Integrantes
- Manuel Enrrique Pérez Rodrigues
- José Tomás Reyes Pérez

## Planificación
Tablero Trello: https://trello.com/b/kB2Tu1mX/gesti%C3%B3n-de-solicitudes-dsy1105

## Tecnologías
Kotlin · Jetpack Compose · Material Design 3 · MVVM · StateFlow · Room · Navigation Compose · CameraX · ML Kit · ZXing · Notificaciones locales

## Funcionalidades implementadas
- **Login** con cuentas ficticias y validación por campo (correo y contraseña).
- **Menú** principal y **cierre de sesión**.
- **ListaSolicitudesScreen**: solicitudes guardadas en Room con código, título, categoría, estado y fecha. La lista se actualiza sola al crear o editar.
- **SolicitudFormScreen**: crear y editar solicitudes (título, descripción, categoría, servicio relacionado).
    - Validaciones en el ViewModel, con errores visibles en cada campo (borde rojo, ícono y mensaje).
    - Código único `SOL-XXXXXXXX`, fecha y hora de creación, estado inicial **"Recibida"**.
    - **Foto de evidencia ficticia** con la cámara, asociada a la solicitud.
    - **Código QR** de la solicitud generado en la ficha de edición.
- **Lector QR** (CameraX + ML Kit): escanea el QR de una solicitud y la abre.
- **Notificaciones locales** al registrar o actualizar una solicitud.
- **Animaciones**: transiciones entre pantallas, errores que aparecen y desaparecen, ítems de la lista animados, indicador de carga en botones y marco del escáner animado.
- **Persistencia**: los datos siguen disponibles después de cerrar la app.

## Recursos nativos
1. **Cámara**: foto de evidencia (`TakePicture` + `FileProvider`) y lectura de QR (CameraX + ML Kit).
2. **Notificaciones locales**: canal propio y permiso `POST_NOTIFICATIONS` (Android 13+).

## Arquitectura (MVVM)
```
app/src/main/java/cl/duoc/gestionsolicitudes/
├── model/            LoginUiState, SolicitudUiState, Catalogo, Usuario, QrResult
├── navigation/       AppNavigation
├── ui/
│   ├── screen/       LoginScreen, MenuScreen, ListaSolicitudesScreen, SolicitudFormScreen,
│   │                 QrScannerScreen, CampoFormulario, EstadoChip
│   └── theme/        Theme
├── viewmodel/        LoginViewModel, SolicitudViewModel, QrViewModel
├── data/
│   ├── local/        SolicitudEntity, SolicitudDao, AppDatabase   (Room)
│   └── repository/   SolicitudRepository, UsuarioRepository, QrRepository
├── utils/            QrScanner, CameraPermissionHelper, NotificationHelper, EvidenciaHelper, QrGenerator
└── MainActivity.kt
```
Flujo de datos: **View → ViewModel → Repository → DAO → Room**, y de vuelta **Room (Flow) → ViewModel (StateFlow) → View**.

## Cómo ejecutar
1. Clonar el repositorio y abrir la carpeta en **Android Studio**:
   ```
   git clone https://github.com/JudeManuel05/Gestion-de-Solicitudes-DSY1105.git
   ```
2. Esperar la sincronización de Gradle (*Sync Now*).
3. Crear o usar un emulador (API 26 o superior). En *Device Manager → Edit → Additional settings → Camera* dejar la cámara trasera en **Webcam0** o **Emulated** para probar la cámara.
4. Ejecutar con **Run ▶**.
5. Ingresar con la cuenta ficticia: `cliente@demo.cl` / `demo123`.

## Datos
Todos los usuarios, solicitudes, fotos y códigos son **ficticios**. La app no lee conversaciones ni usa credenciales reales de WhatsApp.