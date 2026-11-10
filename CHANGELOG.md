## v1.6.0 changelog

### *Whats's new?*
* **New Handicap**: 
  * Added `derealization`, the world turns into an unrecognizable pixelated mess.
* **Wheelchair**
  * Added a wheelchair item in the game. You can use it to move.
  * With `paraplegia` and `amputated_leg` together you can't move anymore, so the action bar tells you that you need a wheelchair.
* **Settings menu**
  * The gear in the `inventory panel` now opens a full settings menu.
  * Display: position of the HUD, background and list style.
  * Game rules: synchronization, stacking, timer, max, duration and descriptions, directly in game.
  * Hover any setting to see what it does.
  * Only players who can use `/handicap` can change the game rules, the others just see them.
* **Synchronization**
  * New option: when it's on, everyone gets the same handicaps. If a player gets or loses one, all players do.
* **Disabled handicaps**
  * You can now disable any handicap for your world in the settings menu. A disabled handicap is never given, even with `give all`.

### *Changes & Balancing*
* **HUD**
  * New look and customised for the Handicap DA.
  * You can now choose the position in the `inventory panel` settings.
* **Visuals**
  * `phantom_light` is now stronger, it blinds you with a white flash.
  * Visual handicaps now stack: if you have `color_blind` and `tunnel_vision`, you see both at the same time (26.3 only).
  * In 26.1 and 26.2 you can't randomly get a second visual handicap while you already have one (while waiting to find a solution for the accumulation).
* **OCD**
  * `ocd` now deals more damage: 2 hearts per second until you obey.
  * The timer and what you still have to do are now shown in the action bar, not in the tchat anymore.
* **Action bar**
  * When several messages want the action bar, a new handicap comes first, then `ocd`, then the rest.
* **Config**
  * The descriptions option moved from the mod config screen to the in-game settings menu.

### *Fixes*
* **Visual handicaps**
  * Fixed visual handicaps hiding each other when you had more than one (26.3 only).
