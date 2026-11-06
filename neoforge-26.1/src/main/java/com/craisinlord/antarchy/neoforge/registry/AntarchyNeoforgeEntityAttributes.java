package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class AntarchyNeoforgeEntityAttributes {
    private AntarchyNeoforgeEntityAttributes() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgeEntityAttributes::registerEntityAttributes);
    }

    static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        event.put(AntarchyNeoforgeEntities.EASTER_BUNNY.get(), rabbitAttributes);
        event.put(AntarchyNeoforgeEntities.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.REVERIE.get(), ReverieEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        event.put(AntarchyNeoforgeEntities.APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntities.GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyNeoforgeEntities.TERMITE.get(), buildAntAttributes(
                AntarchySettings.termiteHealth(),
                AntarchySettings.termiteMovementSpeed(),
                AntarchySettings.termiteAttackDamage()
        ).build());
        event.put(AntarchyNeoforgeEntities.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.STRATOSHARK.get(), StratosharkEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.KRAKEN.get(), KrakenEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.TENTACLE.get(), TentacleEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.BED_BUG.get(), BedBugEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.WASP.get(), WaspEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.BOMBER.get(), BomberEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.SPRINGBUG.get(), SpringbugEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.SPIT_BUG.get(), SpitBugEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.MANTIS.get(), MantisEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.FLYTRAP.get(), FlytrapEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.LUCID.get(), LucidEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.VORTEX.get(), com.craisinlord.antarchy.content.entity.vortex.VortexEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.BASILISK.get(), BasiliskEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.CRAWLING_BLIGHT.get(), CrawlingBlightEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.SKULKING_FRIGHT.get(), SkulkingFrightEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.MANTICORE.get(), ManticoreEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.JERRY.get(), JerryEntity.createAttributes().build());

        AttributeSupplier redAntAttributes = buildAntAttributes(AntarchySettings.redAntHealth(), AntarchySettings.redAntMovementSpeed(), AntarchySettings.redAntAttackDamage()).build();
        AttributeSupplier brownAntAttributes = buildAntAttributes(AntarchySettings.brownAntHealth(), AntarchySettings.brownAntMovementSpeed(), AntarchySettings.brownAntAttackDamage()).build();
        AttributeSupplier rainbowAntAttributes = buildAntAttributes(AntarchySettings.rainbowAntHealth(), AntarchySettings.rainbowAntMovementSpeed(), AntarchySettings.rainbowAntAttackDamage()).build();
        event.put(AntarchyNeoforgeEntities.RED_ANT.get(), redAntAttributes);
        event.put(AntarchyNeoforgeEntities.BROWN_ANT.get(), brownAntAttributes);
        event.put(AntarchyNeoforgeEntities.RAINBOW_ANT.get(), rainbowAntAttributes);
        event.put(AntarchyNeoforgeEntities.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.WORM.get(), WormEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.CHEEP.get(), com.craisinlord.antarchy.content.entity.CheepEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.DORRIE.get(), com.craisinlord.antarchy.content.entity.DorrieEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.HERCULES_BEETLE.get(), com.craisinlord.antarchy.content.entity.HerculesBeetleEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.PRINCE.get(), com.craisinlord.antarchy.content.entity.royal.PrinceEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.PRINCESS.get(), com.craisinlord.antarchy.content.entity.royal.PrincessEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.KING.get(), com.craisinlord.antarchy.content.entity.royal.KingEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.QUEEN.get(), com.craisinlord.antarchy.content.entity.royal.QueenEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.OURANWOOD_DEER.get(), com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.GLIMMER.get(), com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.ELKA.get(), ElkaEntity.createAttributes().build());
        event.put(AntarchyNeoforgeEntities.HOVERBOARD.get(), com.craisinlord.antarchy.content.entity.HoverboardEntity.createAttributes().build());
    }

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.animal.Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

}
