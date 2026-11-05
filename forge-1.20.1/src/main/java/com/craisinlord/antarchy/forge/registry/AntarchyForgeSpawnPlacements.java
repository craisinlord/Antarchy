package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.content.entity.*;
import net.minecraft.world.entity.animal.AbstractFish;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

public class AntarchyForgeSpawnPlacements {
    private AntarchyForgeSpawnPlacements() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyForgeSpawnPlacements::registerSpawnPlacements);
    }

    static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(AntarchyForgeEntities.FLYING_SQUIRREL.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.CATERPILLAR.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.BUTTERFLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.REVERIE.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.BRUTALFLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.GOLDEN_APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.STRATOSHARK.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StratosharkEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.WASP.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.BOMBER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.SPRINGBUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpringbugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.SPIT_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpitBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.KRAKEN.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.MISSILE_SQUID.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.OCTOPUS_BOMB.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OctopusBombEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.NIGHTMARE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.MOLEWORM.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.WORM.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WormEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.MANTIS.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.ALPHA_MANTIS.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AlphaMantisEntity::checkAlphaMantisSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.MOLEVORE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.BED_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.STINK_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StinkBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.LUCID.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.VORTEX.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.vortex.VortexEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.SCORPION.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.BASILISK.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.EMPEROR_SCORPION.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.CRAWLING_BLIGHT.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrawlingBlightEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.SKULKING_FRIGHT.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SkulkingFrightEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.MANTICORE.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.ManticoreEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.JERRY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JerryEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.TERMITE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.ROLLY_POLLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.RollyPollyEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.CHEEP.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractFish::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.DORRIE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.OURANWOOD_DEER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.GLIMMER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity::checkGlimmerSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntities.ELKA.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
