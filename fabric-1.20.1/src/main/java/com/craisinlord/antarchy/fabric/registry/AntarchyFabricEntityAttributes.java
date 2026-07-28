package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.Villager;

public final class AntarchyFabricEntityAttributes {


    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }



    public static void register() {
        FabricDefaultAttributeRegistry.register(
                net.minecraft.world.entity.EntityType.PLAYER,
                net.minecraft.world.entity.player.Player.createAttributes()
                        .add(AntarchyFabricMisc.SCALE.get())
                        .add(AntarchyFabricMisc.DOUBLE_DAMAGE_CHANCE.get())
                        .add(AntarchyFabricMisc.BLOODGLASS_MAX_HEARTS.get())
                        .build()
        );

        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.EASTER_BUNNY.get(), rabbitAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.REVERIE.get(), ReverieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CLOUD_SHARK.get(), CloudSharkEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.KRAKEN.get(), KrakenEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TENTACLE.get(), TentacleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BED_BUG.get(), BedBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.WASP.get(), WaspEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BOMBER.get(), BomberEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.JUMPY_BUG.get(), JumpyBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SPIT_BUG.get(), com.craisinlord.antarchy.content.entity.SpitBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MANTIS.get(), MantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TRIFFID.get(), TriffidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.LUCID.get(), LucidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BASILISK.get(), BasiliskEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CREEPING_HORROR.get(), com.craisinlord.antarchy.content.entity.CreepingHorrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.LURKING_TERROR.get(), com.craisinlord.antarchy.content.entity.LurkingTerrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.JERRY.get(), com.craisinlord.antarchy.content.entity.JerryEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CHEEP.get(), com.craisinlord.antarchy.content.entity.CheepEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.DORRIE.get(), com.craisinlord.antarchy.content.entity.DorrieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.HERCULES_BEETLE.get(), com.craisinlord.antarchy.content.entity.HerculesBeetleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.GLIMMER.get(), com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ELKA.get(), com.craisinlord.antarchy.content.entity.ElkaEntity.createAttributes().build());

        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.RED_ANT.get(), buildAntAttributes(
                AntarchySettings.redAntHealth(),
                AntarchySettings.redAntMovementSpeed(),
                AntarchySettings.redAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BROWN_ANT.get(), buildAntAttributes(
                AntarchySettings.brownAntHealth(),
                AntarchySettings.brownAntMovementSpeed(),
                AntarchySettings.brownAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.RAINBOW_ANT.get(), buildAntAttributes(
                AntarchySettings.rainbowAntHealth(),
                AntarchySettings.rainbowAntMovementSpeed(),
                AntarchySettings.rainbowAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TERMITE.get(), buildAntAttributes(
                AntarchySettings.termiteHealth(),
                AntarchySettings.termiteMovementSpeed(),
                AntarchySettings.termiteAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
    }

}
