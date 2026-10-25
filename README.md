# Dynamic Crosshair (Fabric, client-only)

| Looking at | Crosshair |
|---|---|
| nothing / normal block | small white `+` |
| an entity or player | red `+` with small red corner brackets |
| a block you can right-click (chest, door, lever, crafting table, bed…) | bigger white `+` with bigger white corner brackets |

The attack-cooldown bar (when Attack Indicator = Crosshair) is kept.
Sneaking with an item in hand counts as "not interacting", same as vanilla.

No Fabric API needed. Drop the jar that matches your Minecraft version into `mods/`.

| Jar | Minecraft |
|---|---|
| `dynamic-crosshair-1.0.1+mc1.20.1.jar` | 1.20 – 1.20.4 |
| `dynamic-crosshair-1.0.1+mc1.20.6.jar` | 1.20.5 – 1.20.6 |
| `dynamic-crosshair-1.0.1+mc1.21.1.jar` | 1.21 – 1.21.5 |
| `dynamic-crosshair-1.0.1+mc1.21.11.jar` | 1.21.6 – 1.21.11 |
| `dynamic-crosshair-1.0.1+mc26.1.2.jar` | 26.1.x |
| `dynamic-crosshair-1.0.1+mc26.2.jar` | 26.2.x |
| `dynamic-crosshair-1.0.1+mc26.3.jar` | 26.3.x |

## Building

```powershell
.\setup-versions.ps1      # (re)generate versions/<mc>/ from the table in the script
.\build-all.ps1           # build everything into dist/
.\build-all.ps1 26.2      # or just some versions
```

Uses the JDK 25 in `%USERPROFILE%\.jdks` unless `JAVA_HOME` is set.

## Layout

- `src/main` – shared code: `CrosshairRenderer` (drawing + animation), `Interactables` (right-clickable check)
- `src/gui_1_20`, `src/gui_1_20_5`, `src/gui_1_21` – mixins for `Gui.renderCrosshair`, whose parameters changed in 1.20.5 and 1.21
- `src/gui_extract` – mixin for 26.1 (`Gui.extractCrosshair`)
- `src/hud_extract` – mixin for 26.2+ (`Hud.extractCrosshair`)

A block counts as right-clickable if its class overrides vanilla's empty-hand "use" method, or it opens a menu.
