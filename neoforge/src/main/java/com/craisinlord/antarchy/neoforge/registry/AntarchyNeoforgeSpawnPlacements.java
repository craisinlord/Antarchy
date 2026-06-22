package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
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
        event.register(AntarchyNeoforgeEntites.FLYING_SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.CATERPILLAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.REVERIE.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.BRUTALFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.CLOUD_SHARK.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CloudSharkEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.WASP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.BOMBER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.KRAKEN.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.MISSILE_SQUID.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.MOLEWORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.MANTIS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.MOLEVORE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.BED_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.LUCID.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(AntarchyNeoforgeEntites.EMPEROR_SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
