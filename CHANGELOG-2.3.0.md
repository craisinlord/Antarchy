# Antarchy 2.3.0

Elythia, Thoraxis and the Cavaryn all generate new terrain in this update. Chunks you've already explored keep their old terrain, so there will be a hard seam where old chunks meet new ones. Explore further out or start a new world to see the new generation.

Old worlds get a chat prompt (ops only) to reset any of these dimensions. Picked ones get deleted next time the world loads, so restart or relog. `/antarchyreset` brings the prompt back and works from the console too.

---

## Elythia overhaul

### Terrain

- **Raised world:** the whole dimension sits 40 blocks higher.
  - Sea level is now 100.
  - The world is taller, with its top now at y 575.
  - The cloud layers moved up with it.
- **Real oceans:** oceans are 25 to 45 blocks deep and cover much more of the map.
  - Coasts are either cliffs or narrow sandy beaches.
  - The old flat sea-level shallows are gone.
- **Rivers:** carved rivers wind across the land.
  - They step down in small cascades, reach the sea, and pour over the Fungal Reserve cliffs as waterfalls.
  - Depending on the region they carry lumen or water. Where a lumen river meets water, a calcite wall keeps them apart.
  - Riverbeds are mud, and rivers have fish.
  - Glimmers gather along lumen rivers at night.
- **Ores:** ores and underground features are moved up to match the new heights.
  - Ore heights are now the vanilla heights plus 40.
  - The deepslate band is now at y 32 to 40.
- **Dry caves:** caves near sea level are no longer flooded.

### New biomes

- **Primordial Massif:** ridged mountains rising out of the inland plateau to almost y 300.
  - The rock changes with height:
    - Loam on the lower slopes.
    - Grass with tuff outcrops above that.
    - Tuff, andesite and stone cliffs higher up.
    - Calcite peaks at the top.
  - Rivers climb partway up the slopes in narrow gorges.
- **Fungal Reserve:** huge sunken basins and winding canyons about 100 blocks below the surface.
  - Cliff walls and mossy floors.
  - Lumen pools, Basilisks, Basilisk Lairs and Royal Shrines.
  - The Fungal Reserve is very rare.
- **Truffalo Spires:** a new biome in the heart of Truffalo Forest climates.
  - Towering shellstone and tuff spires 24 to 40 blocks tall, mossy on their faces.
  - Half of the spires are arches you can walk through, some with an extra window hole.
  - Spires have crown spikes on top and smaller satellite pillars around them.
  - Truffalo trees grow on the summits and ledges.
  - New giant Truffalo trees 20 to 30 blocks tall grow here. They also appear rarely in the regular Truffalo Forest.
- **Primordial Plains:** replaces the Elythian Meadows.
  - The plains are covered in the new Primordial Loam block.
  - No milkweed, and only rare torchflowers and pitcher plants.
  - Primordial Loam is mined with a shovel and drops Loam unless you use Silk Touch.
- **Elythia Beach and Elythia River** now generate as their own biomes.

### Biome changes

- The cloud sea biome is gone. Stratosharks now hunt high in the open sky above the land instead.
  - They appear 40 to 70 blocks above the canopy.
  - At most 2 are near a player at a time.
- Fungal Ouranwood Forest no longer generates in new terrain; the Fungal Reserve replaces it.
- Butterfly Fields appear again, and Ouranwood Forest covers more of the land.

### Reefs

- Elythia's oceans now grow large coral reefs. Each one is laid out from a mix of these pieces:
  - Atolls up to about 60 blocks across, with sand lagoons, small islets and channels.
  - Coral arches in rings, domes, avenues and aqueducts. Some arches break the surface, and coral above the water is dead.
  - Organ-pipe coral fields up to about 35 blocks tall.
  - Starbursts with radiating arms around a central spire.
  - Spike forests and coral gardens.
- Reefs use all five vanilla coral types plus Star Coral, with fans, seagrass and sea pickles.
- Smaller coral trees, claws, mushrooms and Star Coral patches are scattered between the reefs.

### Smaller worldgen changes

- Tuff boulders are half as common and no longer hang off slopes or cliff edges.
- Fungal Reserve lumen pools rarely spill down the cliff walls.
- Lumen streams no longer leave walls inside themselves where they cross.
- The King's Tree sits at a more natural ground height, and it no longer erases terrain around it.
- Basilisk Lairs and Royal Shrines can generate in the Fungal Reserve.
- Structures that used to generate in the Meadows now generate in Primordial Plains.
- Basilisks spawn at y 100 to 140, or anywhere in the Fungal Reserve.
- Flying Squirrels' spawning, climbing and gliding heights moved up to match the raised terrain.

### Arriving in Elythia

- Arrivals by brown ant or portal always land on high ground at y 101 or above. You will never land in the ocean, a river, or the floor of the Fungal Reserve.
- Using a brown ant now shows "The ant is finding you a way through..." while it searches for a safe landing spot.

### Throwback Zones

Elythia is where Minecraft's memories go. In some places the memory is strong enough that the game remembers how it used to play.

- **Where:** Throwback Zones form around nostalgic structures and around free-standing "nerd poles" out in the wild.
- **What changes inside a zone:**
  - Colors become saturated.
  - A soft, dreamy haze settles over the screen and fog rolls in.
  - Classic C418 music plays.
  - Getting hurt makes the classic "oof" sound.
  - There's no attack cooldown, so it's spam-click combat.
- **Strength:** the effects get stronger toward the center of the zone and fade out gently as you leave.
- **Remembrance:** while you're in a zone you have the Remembrance effect.
- **Structure overlap:** zones don't apply inside other structures.
- **Whole dimension:** classic music and a faint warm color grade play everywhere in Elythia.

### Remembrance (new status effect)

- **Effects:**
  - Hunger is paused and the hunger bar fades away.
  - Eating heals health equal to the food's nutrition, and you can eat whenever you're below full health.
  - There's no attack cooldown.
  - You get the classic "oof" hurt sound.
- **AppleSkin:** AppleSkin's overlays hide while the hunger bar is hidden.
- **Config:** each part of Remembrance can be turned off in the config.

### Sleeping in Elythia

You're already dreaming. In Elythia you can lie down in a bed at any time of day, unless monsters are nearby, but sleeping never skips the night.

### Brown ant journeys

Riding a brown ant to or from Elythia is now a journey instead of an instant teleport:

1. A circle of void opens beneath you and you sink into it.
2. The world fades to black.
3. Color slowly returns as you glide down through Elythia's sky.
4. About 4 blocks above the ground you get a soft "poof" landing.

Going home is the reverse: you wake up in the Overworld once your destination has loaded.

### Sky, clouds and weather

- **Wind:** Elythia now has wind.
  - It slowly changes direction over a few days and grows stronger and weaker over time.
  - Clouds and morning ground mist drift with it.
- **Cloud layers:** the layers are much sparser and move at different speeds and angles, so they slide past each other.
- **Soft clouds:** low and mid clouds can render as soft, translucent puffs.
  - Turn them off with the `softCloudsEnabled` client config.
  - They switch off automatically with shader packs.
- **Purple rain:** Elythia's purple rain is a lighter lavender and glows.
- **Rain growth:** rain in Elythia speeds up the growth of exposed crops, saplings and other plants near players.
- **Ground mist and fireflies:** both appear in the new biomes, and mist rolls into the Fungal Reserve.

---

## World events

Elythia and Thoraxis now have random world events. The server picks them, so everyone in the dimension sees the same event.

### Elythia events

#### Night events

About half of all nights have one of these events. Night events last until dawn.

| Event | Chance | What happens |
|---|---|---|
| Aurora | 26% | Green-to-violet curtains of light, brightest around midnight. |
| Grand Aurora | 8% | Taller, wider, faster cyan, blue and pink curtains. |
| Prism Aurora | 4% | Four rainbow curtains that cycle through every color. |
| Meteor Shower | 12% | Meteors streak out from a single point in the sky. |
| Starfall | 4% | A heavy, multicolored meteor storm with bright fireball bolides. |

#### Day events

These events last up to 5 minutes and end at dusk.

- **Deer Migration (10%):**
  - Herds of Ouranwood deer, fawns included, appear behind you.
  - Every adult deer then travels across the land in one shared direction.
- **Towering Clouds (6%):**
  - Giant cumulonimbus towers, hundreds of blocks tall, rise on the horizon and drift with the wind.
  - The normal cloud layers clear while the towers are visible.

#### Rain event

- **Lumen Rain (25% of rainstorms, lasts until the rain stops):**
  - The rain turns glowing cyan and splashes sparkle.
  - Swarms of wild Glimmers appear in any biome, day or night.
  - Crop growth from rain is doubled.

### Thoraxis events

While players are in Thoraxis, an event may start about every 15 minutes.

- **Warning:** each event is announced by a warning of 30 to 60 seconds with its own sounds and omens.
- **Duration:** the event itself then lasts 3 to 6 minutes.

| Event | What happens |
|---|---|
| **Weightless** | Gravity drops to 35% for every player, mob and item in the dimension. Glowing motes drift upward. |
| **Temporal Bubbles** | Huge faceted bubbles bounce across the terrain. Dark red bubbles slow time inside them to 12%. Bright orange bubbles speed it up to 3×. |
| **Upward Rain** | Red antiwater rain falls *up*. Under open sky your gravity drops to 55%. |
| **Blind Sky** (Underside only) | The eyes in the Underside sky droop, blink and close. The world goes nearly black-red, fog closes in to 40 blocks, and monsters swarm out of the dark. |

### Commands and config

- **Commands (operators):**
  - `/elythia event`, `/elythia event start <type>` and `/elythia event stop` control Elythia events.
  - `/thoraxis event`, `/thoraxis event start <type> [now]` and `/thoraxis event stop` do the same for Thoraxis.
- **Config:** three new options, all on by default:
  - `events` (Elythia events)
  - `rainGrowth` (Elythia rain growth)
  - `thoraxisEvents` (Thoraxis events)

---

## Thoraxis worldgen overhaul

### Main side

The main side of Thoraxis has completely new terrain. Instead of overworld-style noise, the dimension is split into regions with four distinct terrain styles that blend into each other:

- **Strata:** stacked horizontal rock shelves, some with holes worn through them, linked by thick stone strands.
- **Ribcage:** giant stone ribs arching over open caverns, with a spine ridge along the top.
- **Mirror:** hills rise from each floor and hang mirrored from the ceiling above.
- **Spires:** twisting, leaning, three-lobed pillars. They come as floor-to-ceiling hourglass columns, stalagmites and stalactites.

**Tiers:** most areas stack 4 cavern tiers on top of each other. They are separated by rock plates that open up into windows in places.

**Grand regions:** in rarer regions the tiers merge into one enormous open space:
- Grand Strata shelves.
- Cages more than 190 blocks tall.
- Mirrored peaks up to 160 tall.
- Spires up to 18 blocks in radius.

**Biomes:**
- The biome layout is now different in every world seed.
- Vortex Expanse appears exactly where the Spires terrain is.
- Nadir Forest is more common.

### The Underside

- **Mountains:** the Underside now has huge mountains hanging up to 130 blocks below the divider. They come in three styles:
  - Stepped mesas with terraces and canyons.
  - Crossing star dunes.
  - Jagged peaks.
- **Dream Peaks (new biome):** where the mountains rise.
  - Pale nyxite caps the peaks and bands the mesas.
  - Dream sandstone makes up the rest.
  - Scorpions, Manticores and Bed Bugs live here, along with nyxite spikes, dimensional tears, fire and cornea stalks.
- **Dream Dunes:** away from the mountains, the Dream Dunes are unchanged.

### Feature changes

- Typhonite swirls now grow on every tier floor, not just the top.
- Transition throats, lucid antiwater pools, rib columns and dimensional tears have been reworked to fit the new layered terrain.
- Magma red ant nests now generate throughout the tiers and no longer generate in the Underside.

---

## Cavaryn worldgen overhaul

The original cave network is still there. Each biome now carves its own signature cave shapes on top of it, and the shapes can run into neighboring biomes.

### Cave shapes

- **Wormy Caves:** long ribbed worm bores that wind through the caves, sometimes swelling into coiled chambers. Their walls are lined with loam, castings and mucus.
- **Brooding Tunnels:**
  - Galleries:
    - A two-level mesh of wide, arched galleries with junction chambers, ramps and side niches.
    - Broodstone lines the walls, and spit bug eggs are laid in the chambers.
  - Brood nests:
    - Big brood nest halls with branching galleries ending in brood cells.
    - Spit bug eggs fill the cells.
- **Glowcap Caves:**
  - Huge glowcap grottos, domes about 50 blocks across with side lobes.
  - Tunnels run off from them, and one always dives down.
  - Floors are amber moss and mycelium, with giant glowcaps growing on them.
- **Moleworm Caves:**
  - Giant mole warrens up to three levels deep, with dig chambers joined by ramps.
  - D-shaped tunnels have claw-scraped walls, and some end in side chambers. Vertical shafts drop between levels.
  - Spoil heaps of dirt are piled in the chambers.
- **Cavaryn:** carapace rifts, which are tall winding slot canyons banded with chitin and sometimes forking, sloping down into the network.
- **Biowart Caves:** biowart guts, which are chains of bulging biomite-lined sacs joined by narrow necks.

### Overall

- The Cavaryn is now roughly 70% more open, and almost all of that space connects into one network.
- Biome regions are about half the size, so you'll find every Cavaryn biome much sooner.
- The plain Cavaryn biome is more common.
- The lower biomes reach higher.

---

## New items

### Anvil Cannon

- **Crafting:** 8 Iron Blocks around an Anvil.
- **Ammo:** it fires anvils (normal, chipped or damaged) taken from your offhand or inventory.
- **Damage:**
  - A direct hit deals up to 30 damage, scaled by speed.
  - The anvil then drops and crushes whatever is beneath it.
- **Landing:** the anvil wears down one stage on impact, then lands as a block.
- **Firing:** 2.5 second cooldown, with recoil and screen shake.
- **Enchantments:** takes Multishot (3 anvils at once), Unbreaking and Mending.
- **Dispensers:** a dispenser holding an Anvil Cannon fires anvils from the dispenser's inventory.
- **Config:** cooldown, launch speed, damage, knockback, recoil and anvil wear are all configurable.

### Big Betsy

- **Crafting:** Big Bertha + Amethyst Shard + Netherite Ingot.
- **Stats:** a new look for Big Bertha with the same stats. It keeps your Bertha's enchantments, durability and name.
- **Recipes:** works anywhere Big Bertha is used in a recipe.
- **Better Combat:** fully supported.

### New advancements

- **Swatted:** defeat the Brutalfly.
- **Flattened:** kill a mob with an anvil fired from an Anvil Cannon.
- **Sibling Rivalry:** have Big Bertha and Big Betsy in your inventory at the same time.

---

## AntOS guidance

Antarchy now uses AntOS to tell players where things are and how to do them. Antarchy now needs AntOS 1.6.2.

### Dr. Trayaurus mail

- **Event mail:** Dr. Trayaurus emails you a few minutes after key moments.
  - Your first AntOS sign-in, your first floppy disk, and finding his lab.
  - Reaching Elythia, Cavaryn and Thoraxis, and falling into the Underside.
  - Surviving a Cavaryn horde.
  - Beating the Kraken, the King, the Queen, the Toreterror and Basilisk, and hatching a royal.
  - Killing a Squid or getting Bad Omen sends a warning about the Kraken.
- **Hints:** about one day in five, he sends a hint for something you haven't done yet in a dimension you've reached. Each hint arrives at most once.
- **Archive cards:** many of his mails carry an Archive card, and reading the mail unlocks that page even without its disk.

### Archive and disks

- **Free pages:** the Brown Ant, Termite and Red Ant pages, which explain how to reach each dimension, are unlocked on every computer.
- **Look up:** with a phone in your inventory, hover an item and hold the look-up key to open its Archive page. Most pages now cover their drops, eggs, armor and blocks.
- **Disk descriptions:** every disk now has a short description in its tooltip and in the Disks Library.
- **Field exams:** the Elythia, Cavaryn and Thoraxis Access Record disks now install a quiz. Get a perfect run once for 5 levels. Each answer explains the right one.
- **Dr. T's Lab Notes:** a new disk in the Trayaurus lab chest, with the Easter Bunny and Duplicator Tree pages. These pages weren't on any disk before.
- **Fixed:** the Computer Prototype chest gave an unknown disk instead of Temporal Manipulation.

### Computers and phones

- **Recipe:** the computer shows up in the recipe book once you pick up copper, a repeater or a redstone torch.
- **Computer Scientist house:** the house and its basement computer are one piece now, so the computer always generates. The house also spawns in desert, savanna, snowy and taiga villages.
- **Antroid Phones:** phones can drop from village chests, shipwreck supply chests, buried treasure, dungeons, mineshafts, ancient cities and most Antarchy structures. The Trayaurus lab chest always has one.

## Changes and fixes

- **Squidzooka and RPO Launcher:**
  - Both can take Unbreaking and Mending.
  - Multishot now works in every language and with datapacks.
- **Jade (NeoForge):** Jade tooltips no longer disappear in the Cavaryn during hordes.
  - The same fix applies while Dread, Gooped, Glimmering, Paralyzed, Flytrap goo or the King's judgment flash tint the screen.
- **Lumen:** lumen flowing into water now hardens into calcite, the way lava turns into stone.
- **Vortex:** the Vortex no longer drops its experience twice.
- **Dimensional tears:**
  - Tears opened by a Glimmer Queen spawn Glimmer Manticores.
  - Tears respect the Queen's Manticore cap.
- **Leaves:** Ouranwood and Royal leaves buried inside a canopy no longer render their hidden faces, for better FPS. This works with Sodium.
- **World generation performance:** Elythia generates faster.
  - Climate is cached per column.
  - Water is handled more simply.
  - Leaves are placed in batches.
  - Flying Squirrels read the world more cheaply.
- **Ant teleports:**
  - Using an ant again mid-teleport no longer starts a second teleport.
  - Dying or changing dimension cancels a teleport cleanly.
  - The landing search gives up after 60 seconds if no safe spot is found.
- **Debug commands for operators:**
  - `/elythia massif [next]` teleports to a mountain peak.
  - `/elythia map [radius] [step] [exact]` renders a terrain map.
  - `/elythia perf start|report|stop` profiles chunk generation.
