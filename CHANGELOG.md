# Changelog

## Unreleased

### Elythia terrain overhaul

Elythia generates new terrain. Already-explored areas keep their old terrain, and the border between old and new chunks shows a hard seam; explore further out or start a new world to see the new generation.

- The whole dimension sits 40 blocks higher. Sea level is now 100, and the cloud layers moved up with it.
- Oceans are real oceans: 25 to 45 blocks deep, with coasts that are either cliffs or narrow sandy beaches. The old flat sea-level shallows are gone.
- New biome: the Fungal Reserve. Huge sunken basins and winding canyons about 100 blocks below the surface, with cliff walls, mossy floors, lumen pools, Basilisks, Basilisk Lairs and Royal Shrines.
- Carved rivers wind across the land. They run lumen through forests and water through meadows, step down in small cascades, reach the sea, and pour over the Reserve cliffs as waterfalls.
- The cloud sea biome is gone. Stratosharks now hunt high in the open sky above the land.
- Fungal Ouranwood Forest no longer generates in new terrain; the Fungal Reserve replaces it.
- Butterfly Fields appear again, and Ouranwood Forest covers more of the land.
- Meadows are now Primordial Plains: covered in Primordial Loam, with no milkweed and only rare torchflowers and pitcher plants. Primordial Loam drops Loam unless mined with Silk Touch.
- The Fungal Reserve is rarer.
- New biome: the Primordial Massif. Ridged mountains rising up to about y 335 out of the inland plateau, with tuff, andesite and stone cliffs, grassy shoulders and calcite peaks. Rivers spring from their lower slopes.
- Operators can jump to massif peaks with `/elythia massif` and `/elythia massif next`.
- Riverbeds are mud. Glimmers gather along lumen rivers at night, and rivers have fish.
- Ores, underground features, Basilisk spawning, Flying Squirrel behavior and arrival points are adjusted to the new heights. Arrivals by brown ant or portal always land on the surface, never on a Reserve floor.
- Operators can render a terrain map with `/elythia map [radius] [step]`.
- Operators can profile chunk generation with `/elythia perf start`, `/elythia perf report` and `/elythia perf stop`.

### Changes

- Hoverboards no longer burn in fire or lava when dropped, like Netherite gear.

### Fixes

- Fixed a memory leak that could fill a server's RAM within minutes while generating Elythia with C2ME installed.
- The Squidzooka and RPO Launcher can be enchanted at the enchanting table. They take Unbreaking, Mending and Multishot, plus any other mod's enchantments meant for items with durability.
