package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class AntarchyNeoforgeSpawnPlacements {
    private AntarchyNeoforgeSpawnPlacements() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgeSpawnPlacements::registerSpawnPlacements);
    }

    static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(AntarchyNeoforgeEntities.FLYING_SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.CATERPILLAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.REVERIE.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.BRUTALFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.STRATOSHARK.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StratosharkEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.WASP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.BOMBER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.SPRINGBUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpringbugEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.SPIT_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpitBugEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.KRAKEN.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.MISSILE_SQUID.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.OCTOPUS_BOMB.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OctopusBombEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.MOLEWORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.WORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WormEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.MANTIS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.ALPHA_MANTIS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AlphaMantisEntity::checkAlphaMantisSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.MOLEVORE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.BED_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.STINK_BUG.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StinkBugEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.LUCID.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.VORTEX.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VortexEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.EMPEROR_SCORPION.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.CRAWLING_BLIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrawlingBlightEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.SKULKING_FRIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SkulkingFrightEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.MANTICORE.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ManticoreEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.JERRY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JerryEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.TERMITE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.ROLLY_POLLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RollyPollyEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.CHEEP.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractFish::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.DORRIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.OURANWOOD_DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.GLIMMER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GlimmerEntity::checkGlimmerSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntities.ELKA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
