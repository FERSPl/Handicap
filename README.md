# Handicap

A Minecraft Fabric mod that randomly gives players a new handicap at regular intervals. Survive as long as you can with 33 different handicaps, or pick the ones you want with commands.

> Inspired by the mod [Disabilities](https://modrinth.com/mod/disabilities) by OddSense. Handicap is an independent implementation with its own code.

## Features

- A random handicap is given to every player every **10 minutes** by default (the interval can be changed or turned off).
- Give, remove or list handicaps for selected players with commands.
- Pick a specific handicap, a random one, or all of them.
- Handicaps are saved per world.
- Operator permissions are required for commands.

## Commands

| Command | Description |
|---|---|
| `/handicap give <players> <handicap\|all\|random>` | Give one handicap, a random one, or all of them. |
| `/handicap remove` | Remove **all** your handicaps. |
| `/handicap remove <players>` | Remove **all** handicaps from the selected players. |
| `/handicap remove <players> <handicap\|all\|random>` | Remove one specific handicap, a random one, or all of them. |
| `/handicap list <players>` | Show the handicaps of the selected players. |
| `/handicap next <players>` | Skip the timer and give the next random handicap. |
| `/handicap timer [off\|<minutes>]` | Show the timer, turn it off, or set the interval in **minutes** (`1` = every minute, `0.5` = every 30 seconds). Minimum 0.1 minute. |

Replace `<players>` with a player name or a selector such as `@s` or `@a`, and `<handicap>` with an ID from the table below.

## Handicaps

| # | Handicap | ID | Effect | Status |
|---|---|---|---|---|
| 1 | Blind | `blind` | Permanent blindness | ✅ |
| 2 | Color Blind | `color_blind` | You see in black and white | ✅ |
| 3 | Deaf | `deaf` | You can't hear anything (subtitles are turned on automatically) | ✅ |
| 4 | Diabetes | `diabetes` | Sugary food hurts you when eaten | ✅ |
| 5 | Schizophrenia | `schizophrenia` | You see and hear things that aren't real | ✅ |
| 6 | Mute | `mute` | You can't speak in chat | ✅ |
| 7 | Seizures | `seizures` | Damage, low hunger or bad effects can trigger seizures | ✅ |
| 8 | Cancer | `cancer` | Your maximum health slowly wastes away | ✅ |
| 9 | OCD | `ocd` | Compulsions you must obey, or you get sick | ✅ |
| 10 | Amputated Offhand | `amputated_offhand` | Your offhand is gone | ✅ |
| 11 | Amputated Leg | `amputated_leg` | You must jump to move around | ✅ |
| 12 | Dyslexia | `dyslexia` | Most text becomes hard to read | ✅ |
| 13 | Short | `short` | You are smaller and your reach is shorter | ✅ |
| 14 | Dementia | `dementia` | Items move around in your inventory | ✅ |
| 15 | Asthma | `asthma` | Sprinting for too long leaves you out of breath | ✅ |
| 16 | Cerebral Palsy | `cerebral_palsy` | Random involuntary movements | ✅ |
| 17 | Narcolepsy | `narcolepsy` | You randomly fall asleep | ✅ |
| 18 | Parkinson's | `parkinsons` | Your crosshair shakes constantly | ✅ |
| 19 | Pollen Allergy | `pollen_allergy` | Flowers nearby poison you | ✅ |
| 20 | Insomnia | `insomnia` | You can't sleep | ✅ |
| 21 | Arthritis | `arthritis` | Mining and attacking are slower | ✅ |
| 22 | Brittle Bones | `brittle_bones` | You take twice as much fall damage | ✅ |
| 23 | ADHD | `adhd` | Your selected hotbar slot changes at random | ✅ |
| 24 | Tourette's | `tourettes` | Your camera jerks and you say random things in chat | ✅ |
| 25 | Vertigo | `vertigo` | The world randomly starts to spin | ✅ |
| 26 | Scoliosis | `scoliosis` | Your view is permanently tilted | ✅ |
| 27 | Kleptomania | `kleptomania` | You can't drop items | ✅ |
| 28 | Lactose Intolerance | `lactose_intolerance` | Drinking milk kills you instantly | ✅ |
| 29 | Anosognosia | `anosognosia` | You can't see your own handicaps | ✅ |
| 30 | Paraplegia | `paraplegia` | You can't jump or sprint | ✅ |
| 31 | Reduced Mobility | `reduced_mobility` | You move more slowly | ✅ |
| 32 | Celiac Disease | `celiac` | Bread and cookies make you sick | ✅ |
| 33 | Tinnitus | `tinnitus` | A ringing noise plays from time to time | ✅ |

## Requirements

- Minecraft **26.1**
- [Fabric Loader](https://fabricmc.net/) 0.19.5 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25 (the official Minecraft launcher already includes it)

Install the mod on the **client**, and also on the **server** if you play multiplayer. Some handicaps (camera shaking, sound, fake noises) only work when the mod is installed on the client.

## Installation

1. Install Fabric Loader for Minecraft 26.1.
2. Download Fabric API and the mod `.jar` file.
3. Place both `.jar` files in your Minecraft `mods` folder.
4. Launch Minecraft using the Fabric profile.

## Building from source

You need JDK 25. Run `./gradlew build`; the mod is created in `build/libs/`.

## Contributing

Bug reports and suggestions are welcome. Please open an [issue](https://github.com/FERSPI/Disabilitiesmod/issues) in this repository.

## License

Released under the [MIT License](LICENSE).
