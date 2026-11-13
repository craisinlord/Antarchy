# Antarchy 2.3.2

This update is about Elythia's world generation: less memory held onto, and less repeated work per chunk. It's built to work with C2ME, including its density function compiler.

Terrain in new chunks can differ very slightly from 2.3.1, mostly along river edges, so you may spot small seams where new chunks meet ones you've already explored.

---

## Memory

- **King's Tree worlds:** leaving a world while a tree's King was still waiting to spawn kept that whole world in memory until the game closed. It's now released properly.
- **C2ME:** fixed a leak that kept finished chunk generation data alive while generating Elythia with C2ME installed.
- **Worker threads:** generation threads no longer hold on to data from the last chunk they worked on, or from the previous world after you leave it.
- **King's Tree tiles:** the tree's structure pieces are unloaded after they're placed, instead of staying in memory until the server stops. Exploring a whole tree could otherwise hold on to around 185 MB.

## Chunk generation

- **Shared terrain caches:** river, basin and column calculations are now cached for the whole world instead of being thrown away and redone for each chunk. Height lookups benefit too.
- **Fewer repeated calculations:** terrain values that were being recalculated several times for the same chunk are now reused.
- **Reefs:** reef sites are checked against biomes first, so bad spots get ruled out before the expensive height checks. Reefs still pick the same spots.
- **Underground decoration:** floor patches and dripstone clusters search for caves more efficiently. Caves can look slightly sparser because of this.

## Server ticks

- **Waiting Kings:** a King waiting to spawn used to run its full spawn check every tick until the area loaded. It now waits until a spawn spot is loaded, and retries every 5 seconds.

## Chunk loading on the client

- **1.21.1 only:** without Sodium, chunk meshes now upload to the graphics card a limited amount per frame instead of all at once, which reduces frame spikes while moving through new terrain. Terrain can take slightly longer to appear during heavy loading.

## Primordial Grass

- **1.21.1 only:** grass now animates on the GPU, so gusts roll across whole fields in every dimension, and it bends with the legs of anything walking through it, then springs back. Gusts show wind lines above the grass and sound louder the stronger they are. It can now be placed on dirt and grass blocks and potted in a flower pot. Covered grass only sways gently. Video Settings now has Dynamic Grass, which turns it back into a normal static block for lower-end PCs, and Grass Quality. Shader packs use static grass automatically. Draw distance is configurable and defaults to 96 blocks.

## Logs

- **Ouranwood trees:** big trees reaching past the edge of the area they can safely build in no longer spam "Detected setBlock in a far chunk" errors. Those branches get cut off without the error, as before.

## Old worlds

- **Worlds from before 2.3.0:** these could fail to open with a datapack error after updating, because the Elythian Meadows biome had been removed. The biome is back for old chunks only, and new terrain never generates it.

## Version notes

- **26.3:** this version generates terrain differently, so the shared terrain caches, the repeated calculation fix, the C2ME leak fix and the terrain change above don't apply there. Everything else does.
