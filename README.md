# GriefPreventionEasyGUI

A GUI addon for [GriefPrevention](https://github.com/GriefPrevention/GriefPrevention) that gives players a clean, clickable interface for managing their claims. No more memorizing commands.

## What it does

Players run `/claimhelp` and get a chest GUI where they can:

- **Auto-claim land** with preset sizes (small/medium/large) or a custom size
- **Get a golden shovel** for manual claiming (with configurable cooldown)
- **Browse all their claims** in a paginated list
- **View claim info** — dimensions, area, coordinates, trusted players
- **Resize claims** — pick a direction (N/S/E/W), use +1/+5/+MAX steppers, preview the cost, then confirm
- **Manage trust** — add/remove players, pick trust level (container, build, full)
- **Teleport to claims** with a configurable delay and movement cancellation
- **Abandon claims** with a confirmation screen
- **See claim boundaries** with particle walls (gold corners, green edges) — no extra plugins needed

New players get a one-time clickable onboarding message on join.

## Requirements

- Paper 1.21+ (or any Paper fork)
- [GriefPrevention](https://github.com/GriefPrevention/GriefPrevention)
- Java 21+

## Installation

1. Drop the jar into your `plugins/` folder
2. Restart the server
3. Done — default config is generated automatically

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/claimhelp` | Opens the main GUI | None |
| `/gpegui-reload` | Reloads config and language files | `gpegui.admin` |

## Configuration

Everything is toggleable in `config.yml`:

```yaml
language: "en"                    # "en" or "bg", or add your own lang_xx.yml

# Toggle any feature on/off
onboarding-enabled: true
autoclaim-enabled: true
shovel-enabled: true
resize-enabled: true
teleport-enabled: true
abandon-enabled: true

# Tuning
shovel-cooldown-seconds: 3600     # Set to 0 to disable
teleport-delay-seconds: 3
autoclaim-sizes:
  small: 10
  medium: 20
  large: 30

# Visualizer
visualizer-enabled: true
visualizer-radius: 60
visualizer-interval-ticks: 10
visualizer-max-particles: 1000
visualizer-wall-height: 2

# Chat input
chat-input-timeout-seconds: 60
chat-input-max-failures: 2
```

The GUI layout adjusts dynamically when features are disabled — buttons stay centered.

## Language

Ships with English (`lang.yml`) and Bulgarian (`lang_bg.yml`). To add a language:

1. Copy `lang.yml` to `lang_xx.yml` in the plugin folder
2. Translate the strings
3. Set `language: "xx"` in `config.yml`
4. Run `/gpegui-reload`

## Building

```bash
./gradlew shadowJar
```

Output jar is in `build/libs/`.

## License

MIT
