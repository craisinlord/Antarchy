package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

public final class AntarchyFabricSpawnPlacements {


    public static void register() {
        SpawnPlacements.register(AntarchyFabricEntities.FLYING_SQUIRREL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.CATERPILLAR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.BUTTERFLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.REVERIE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.BRUTALFLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.APPLE_COW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.GOLDEN_APPLE_COW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.CLOUD_SHARK.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CloudSharkEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.WASP.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.BOMBER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.JUMPY_BUG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JumpyBugEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.SPIT_BUG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.SpitBugEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.KRAKEN.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.MISSILE_SQUID.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OctopusBombEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.CHEEP.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.AbstractFish::checkSurfaceWaterAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.DORRIE.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.OCEAN_FLOOR, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.GLIMMER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity::checkGlimmerSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.ELKA.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.NIGHTMARE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.MOLEWORM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.MANTIS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AlphaMantisEntity::checkAlphaMantisSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.MOLEVORE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.BED_BUG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.STINK_BUG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StinkBugEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.LUCID.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.SCORPION.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.BASILISK.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.TORETERROR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ToreterrorEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.CREEPING_HORROR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.CreepingHorrorEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.LURKING_TERROR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.LurkingTerrorEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.JERRY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.JerryEntity::canSpawn);
        SpawnPlacements.register(AntarchyFabricEntities.TERMITE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(AntarchyFabricEntities.ROLLY_POLLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.craisinlord.antarchy.content.entity.RollyPollyEntity::canSpawn);
    }

}
