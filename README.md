# SkyList

Proyecto Android (Kotlin) enfocado en reproducción local de música + letras sincronizadas.

## Alcance incluido
- Reproductor local con ExoPlayer (play/pause, siguiente, anterior, seek).
- Lista de canciones en RecyclerView (título, artista, portada).
- Mini player inferior.
- Línea activa de letra sincronizada si el archivo de audio trae letra en formato LRC embebida en metadata.

## Configuración
- package: `com.skylist.app`
- Min SDK 23
- Target / Compile SDK 34
- Arquitectura MVVM + Jetpack (ViewModel/LiveData)

## Nota sobre InnerTune
El entorno de ejecución no permitió clonar GitHub (error de red 403), por lo que se implementó una base equivalente simplificada siguiendo la estructura y la lógica de reproducción estable con Media3/ExoPlayer.
