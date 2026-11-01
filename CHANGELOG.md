## v1.5.0 changelog

### *Critical bug fix*
* **Drunk crash**
  * Fixed a critical bug where the drunk handicap could make your speed go infinite, crashing the game.

### *Whats's new?*
* **Permissions**
  * A solo world no longer requires `allow cheats` to be enabled.

* **Inventory panel**
  * Added a panel next to your inventory (survival and creative) with all your handicaps.
  * Hover a handicap to see what it does.
  * Shows the time left before the next handicap and if stacking is on or off (hover it to see what it means).
  * Added a *Next handicap* button, it follows the same permission as `/handicap next`.
  * The list scrolls with the mouse wheel when you have a lot of handicaps.
* **New Handicap**: 
  * Added `prosopagnosia`, you can't recognize faces anymore: players and villagers look like Steve, names are hidden (above heads, in the tchat and in the Tab list) and monsters are disguised as other monsters.
* **Max and duration**
  * Added `max`: limit how many handicaps a player can have, the oldest one is removed when you go over it.
  * Added `duration`: each handicap wears off on its own after some time.
  * Both are off by default.

### *Changes & Balancing*
* **Commands**
  * All the settings are now in `/handicap settings`: `timer`, `stacking`, `max` and `duration`.
  * `/handicap timer` and `/handicap stacking` don't exist anymore, use `/handicap settings timer` and `/handicap settings stacking` instead.
  * `/handicap settings` alone shows every setting at once.
  * In singleplayer, the player hosting the world can now always use `/handicap`, even with "allow cheats" off.
* **HUD**
  * New look, smaller, and it now shows the timer.
  * It hides when your inventory is open and when you have anosognosia.
* **Visuals**
  * Broken arm now puts your arm in a white cast, visible from everyone's POV.
  * Anosognosia now scrambles the handicap names in the inventory panel, you can see how many you have but not which ones.
* **Announcement when getting an handicap**
  * The action bar only shows the handicap name now, the description is in the inventory panel.
  * The "hide descriptions" option now hides the descriptions in the inventory panel.

### *Fixes*
* **Spatial disorientation**
  * Fixed a bug where your left and right keys could stay swapped in your settings after the handicap was gone. If it happened to you, go to Options > Controls and set them back once.
* **Dyslexia**
  * Fixed a bug where the menus stayed scrambled after losing dyslexia.
* **Max and stacking**
  * Fixed `next` doing nothing when you already had every handicap with stacking off.
* **Visual effects**
  * Fixed a bug that caused any visual effect to vanish for a short time by changing the camera view.
* **OCD**
  * Fixed impossible compulsion with OCD and Paraplegia.
