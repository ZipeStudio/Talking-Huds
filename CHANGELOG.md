v1.0.0:
### Changes
- Reworked HUD in a flat, vanilla-like style: Faces (row or column) and List (face, name and a pixel speaker icon, like the voice mods' overlays), simple fades, "+N" when there are too many talkers
- Subtitles mode now uses Minecraft's own subtitles (with direction arrows) instead of a separate box
- Speaking animations that work in every display: Pulse (Talking Heads style), Rings, Bounce; plus a single Highlight (glow + frame) toggle. By default every player gets their own colour, the same as their Locator Bar dot. They replace the outline style, speaking effect and head scaling options, and follow a smoothed voice level so nothing flickers
- Picking another HUD position no longer throws the HUD off-screen (the old drag offset is reset)
- Fewer, clearer settings; the HUD editor greys out options that don't apply to the current mode and shows talkers starting and stopping
- Renamed to Talking Huds (mod id `talkinghuds`); the old `talkinghud.json5` config is migrated automatically
- Supports Fabric 1.20.1, 1.21.1, 1.21.11, 26.1–26.3, NeoForge 1.21.1, 1.21.11, 26.1–26.3 and Forge 1.20.1
- NeoForge and Forge now actually draw the HUD (the HUD layer and fade-out were only wired up on Fabric)
- New settings screen on vanilla widgets: options sorted into sections, a sidebar that stays put (the button in its title moves it to the other side) and steps aside while you drag the HUD, tooltips for every option and right-click to reset an option to its default; YACL is no longer used
- The voice mods' own speaking icons (Simple Voice Chat and Plasmo Voice) are always hidden while the mod is on, instead of a setting
- New options: Show Yourself (see your own face while you talk, also a mic check) and Max Shown
- The Locator Bar mode is hidden on versions without the Locator Bar (before 1.21.6)
### Fixes
- Returning from the HUD editor no longer goes back to a stale settings screen that could undo edits
- One invalid value in the config no longer resets the whole config
- Translation keys broke on systems with a Turkish locale
### Internal
- Updated to Mossy 4.10.0 and Gradle 9.6
- Moved to the ZipeStudio mod template, package `me.zipestudio.talkinghuds`
- Unit tests and singleplayer dev tools (`/thdev`, dummy talkers), enabled only in the dev client
