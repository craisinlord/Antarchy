package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class AntarchyNeoforgeEntityAttributes {
    private AntarchyNeoforgeEntityAttributes() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgeEntityAttributes::registerEntityAttributes);
    }

    static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        event.put(AntarchyNeoforgeEntites.EASTER_BUNNY.get(), rabbitAttributes);
        event.put(AntarchyNeoforgeEntites.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.REVERIE.get(), ReverieEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        event.put(AntarchyNeoforgeEntites.APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntites.GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntites.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.CLOUD_SHARK.get(), CloudSharkEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.KRAKEN.get(), KrakenEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.BED_BUG.get(), BedBugEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.WASP.get(), WaspEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.BOMBER.get(), BomberEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.MANTIS.get(), MantisEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.TRIFFID.get(), TriffidEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.LUCID.get(), LucidEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.BASILISK.get(), BasiliskEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());

        AttributeSupplier redAntAttributes = buildAntAttributes(AntarchySettings.redAntHealth(), AntarchySettings.redAntMovementSpeed(), AntarchySettings.redAntAttackDamage()).build();
        AttributeSupplier brownAntAttributes = buildAntAttributes(AntarchySettings.brownAntHealth(), AntarchySettings.brownAntMovementSpeed(), AntarchySettings.brownAntAttackDamage()).build();
        AttributeSupplier rainbowAntAttributes = buildAntAttributes(AntarchySettings.rainbowAntHealth(), AntarchySettings.rainbowAntMovementSpeed(), AntarchySettings.rainbowAntAttackDamage()).build();
        AttributeSupplier termiteAttributes = buildAntAttributes(AntarchySettings.termiteHealth(), AntarchySettings.termiteMovementSpeed(), AntarchySettings.termiteAttackDamage()).build();
        event.put(AntarchyNeoforgeEntites.RED_ANT.get(), redAntAttributes);
        event.put(AntarchyNeoforgeEntites.BROWN_ANT.get(), brownAntAttributes);
        event.put(AntarchyNeoforgeEntites.RAINBOW_ANT.get(), rainbowAntAttributes);
        event.put(AntarchyNeoforgeEntites.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntites.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
    }

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

}
