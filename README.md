# SkyList

SkyList is a Kotlin Android music player application inspired by MetroList-style modular architecture and built for Android Studio.

**Developer / Owner:** SSteveXD

"SkyList is developed by SSteveXD and inspired by open-source music player projects."

## App description
SkyList focuses on local playback with modern Media3 service architecture and integrates concept modules inspired by:
- InnerTune (player architecture baseline)
- OuterTune (playback control refinements)
- Kizzy (Discord Rich Presence integration approach)
- Better Lyrics (synchronized lyrics rendering)
- SimpMusic Lyrics API (lyrics provider strategy)
- metroserver (listen together sessions)
- MusicRecognizer (music recognition workflow)

## Included features
- Local music playback (MediaStore + Media3 ExoPlayer)
- Playlists (repository and favorites action)
- Album artwork loading
- Synchronized lyrics (embedded LRC parser + manager)
- Discord Rich Presence manager abstraction
- Background playback via `MusicService`
- Notification & lockscreen controls through MediaSessionService
- Music recognition manager (snippet matching baseline)
- Listen together session hosting + state sync abstraction

## Package structure
```
app/
├─ data/
│  ├─ repository/
│  └─ models/
├─ player/
├─ lyrics/
├─ recognition/
├─ discord/
├─ ui/
│  ├─ main/
│  ├─ player/
│  ├─ playlists/
│  └─ settings/
└─ utils/
```

## Metadata
- Application name: SkyList
- Application ID: `com.skylist.app`
- Developer credit tag: `SSteveXD`

## Credits
- MetroList reference integration approach
- InnerTune — Zion Huang, Malopieds
- OuterTune — Davide Garberi, Michael Zh
- Kizzy — Discord Rich Presence implementation
- Better Lyrics — synchronized lyrics
- SimpMusic Lyrics API — lyrics data
- metroserver — listen together implementation
- MusicRecognizer — music recognition
