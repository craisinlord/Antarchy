package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.content.entity.*;
import net.minecraft.world.entity.animal.AbstractFish;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
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
        event.register(AntarchyForgeEntites.FLYING_SQUIRREL.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.CATERPILLAR.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.BUTTERFLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.REVERIE.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.BRUTALFLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.GOLDEN_APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.CLOUD_SHARK.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CloudSharkEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.WASP.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.BOMBER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.JUMPY_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JumpyBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.SPIT_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpitBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.KRAKEN.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.MISSILE_SQUID.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.OCTOPUS_BOMB.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OctopusBombEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.NIGHTMARE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.MOLEWORM.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.MANTIS.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.ALPHA_MANTIS.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AlphaMantisEntity::checkAlphaMantisSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.MOLEVORE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.BED_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.STINK_BUG.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StinkBugEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.LUCID.get(), net.minecraft.world.entity.SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.SCORPION.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.BASILISK.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.EMPEROR_SCORPION.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.CREEPING_HORROR.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CreepingHorrorEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.LURKING_TERROR.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LurkingTerrorEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.JERRY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JerryEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.TERMITE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.ROLLY_POLLY.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.RollyPollyEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.CHEEP.get(), net.minecraft.world.entity.SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractFish::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.DORRIE.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.OURANWOOD_DEER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.GLIMMER.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity::checkGlimmerSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(AntarchyForgeEntites.ELKA.get(), net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
