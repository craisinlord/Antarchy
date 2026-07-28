package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

public class AntarchyForgeEntityAttributes {
    private AntarchyForgeEntityAttributes() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyForgeEntityAttributes::registerEntityAttributes);
    }

    static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        event.put(AntarchyForgeEntites.EASTER_BUNNY.get(), rabbitAttributes);
        event.put(AntarchyForgeEntites.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.REVERIE.get(), ReverieEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        event.put(AntarchyForgeEntites.APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntites.GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntites.TERMITE.get(), buildAntAttributes(
                AntarchySettings.termiteHealth(),
                AntarchySettings.termiteMovementSpeed(),
                AntarchySettings.termiteAttackDamage()
        ).build());
        event.put(AntarchyForgeEntites.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        event.put(AntarchyForgeEntites.CLOUD_SHARK.get(), CloudSharkEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.KRAKEN.get(), KrakenEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.TENTACLE.get(), TentacleEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.BED_BUG.get(), BedBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.WASP.get(), WaspEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.BOMBER.get(), BomberEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.JUMPY_BUG.get(), JumpyBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.SPIT_BUG.get(), SpitBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.MANTIS.get(), MantisEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.TRIFFID.get(), TriffidEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.LUCID.get(), LucidEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.BASILISK.get(), BasiliskEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.CREEPING_HORROR.get(), CreepingHorrorEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.LURKING_TERROR.get(), LurkingTerrorEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.JERRY.get(), JerryEntity.createAttributes().build());

        AttributeSupplier redAntAttributes = buildAntAttributes(AntarchySettings.redAntHealth(), AntarchySettings.redAntMovementSpeed(), AntarchySettings.redAntAttackDamage()).build();
        AttributeSupplier brownAntAttributes = buildAntAttributes(AntarchySettings.brownAntHealth(), AntarchySettings.brownAntMovementSpeed(), AntarchySettings.brownAntAttackDamage()).build();
        AttributeSupplier rainbowAntAttributes = buildAntAttributes(AntarchySettings.rainbowAntHealth(), AntarchySettings.rainbowAntMovementSpeed(), AntarchySettings.rainbowAntAttackDamage()).build();
        event.put(AntarchyForgeEntites.RED_ANT.get(), redAntAttributes);
        event.put(AntarchyForgeEntites.BROWN_ANT.get(), brownAntAttributes);
        event.put(AntarchyForgeEntites.RAINBOW_ANT.get(), rainbowAntAttributes);
        event.put(AntarchyForgeEntites.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.CHEEP.get(), com.craisinlord.antarchy.content.entity.CheepEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.DORRIE.get(), com.craisinlord.antarchy.content.entity.DorrieEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.HERCULES_BEETLE.get(), com.craisinlord.antarchy.content.entity.HerculesBeetleEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.OURANWOOD_DEER.get(), com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.GLIMMER.get(), com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity.createAttributes().build());
        event.put(AntarchyForgeEntites.ELKA.get(), ElkaEntity.createAttributes().build());
    }

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

}
