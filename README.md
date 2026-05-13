# SkyList

Proyecto Android (Kotlin) enfocado en reproducción local de música, letras sincronizadas y una experiencia de biblioteca inspirada en los cambios recientes de Metrolist, conservando el nombre e identidad de SkyList.

## Alcance incluido
- Reproductor local con ExoPlayer (play/pause, siguiente, anterior y seek).
- Lista de canciones en RecyclerView con título, artista, álbum y portada.
- Búsqueda de biblioteca por canción, artista o álbum.
- Modo aleatorio con cola mezclada desde la canción seleccionada.
- Mini player inferior y panel de reproducción con línea activa de letra LRC.
- Copia de letras al portapapeles cuando el archivo trae letra embebida.
- Temporizador de sueño para pausar la reproducción después de 15, 30, 45 o 60 minutos.
- Exportación/backup de la biblioteca local en CSV dentro del almacenamiento privado de la app.

## Configuración
- App name: `SkyList`
- Package: `com.skylist.app`
- Min SDK 23
- Target / Compile SDK 34
- Arquitectura MVVM + Jetpack (ViewModel/LiveData)

## Nota sobre Metrolist
Se mantuvo la marca SkyList mientras se incorporaron funciones equivalentes de biblioteca, letras, exportación y temporizador. El entorno de terminal no permitió clonar GitHub directamente (CONNECT 403), así que las funciones se portaron manualmente sobre la base actual del proyecto.
