## v1.6.0 Changelog

### *What's new?*
- **New Handicap** 
  - Added `derealization`: turns the world into an unrecognizable pixelated mess.
- **Wheelchair**
  - Added a Wheelchair item to the game to help you move around.
  - Having both `paraplegia` and `amputated_leg` together now completely prevents movement, and the action bar will inform you that a wheelchair is required.
- **Settings Menu**
  - The gear icon in the inventory panel now opens a full settings menu.
  - **Display:** Customize HUD position, background, and list style.
  - **Game Rules:** Adjust synchronization, stacking, timer, max handicaps, duration, and descriptions directly in-game.
  - Hover over any setting to see its description.
  - Only players with permission to use `/handicap` can modify game rules; other players can only view them.
- **Synchronization**
  - New option: when enabled, all players share the exact same handicaps. When one player gains or loses a handicap, everyone does.
- **Disabled Handicaps**
  - You can now disable specific handicaps for your world in the settings menu. Disabled handicaps will never be assigned, even when using `/handicap give all`.

### *Changes & Balancing*
- **HUD**
  - Redesigned look tailored to the mod's visual identity.
  - You can now choose its position in the settings menu.
- **Visuals**
  - `phantom_light` is now stronger and blinds you with a white flash.
  - Visual handicaps now stack: having both `color_blind` and `tunnel_vision` displays both effects simultaneously (26.3 only).
  - In 26.1 and 26.2, players will not randomly receive a second visual handicap while one is active (temporary fix while working on stacking for older versions).
- **OCD**
  - `ocd` now deals increased damage: 2 hearts per second until task completion.
  - The remaining time and task requirements are now displayed in the action bar instead of the chat.
- **Action Bar**
  - Priority system implemented: when multiple messages request the action bar, new handicaps take top priority, followed by `ocd`, then other notifications.
- **Config**
  - Moved the descriptions toggle from the mod config screen to the in-game settings menu.

### *Fixes*
- **Visual Handicaps**
  - Fixed an issue where visual handicaps would overlap and hide each other when holding multiple at once (26.3 only).
