package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.PrinceEntity;
import com.craisinlord.antarchy.content.entity.royal.PrincessEntity;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AntarchyForgeEntityAttributes {
    private AntarchyForgeEntityAttributes() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyForgeEntityAttributes::registerEntityAttributes);
    }

    static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        event.put(AntarchyForgeEntities.EASTER_BUNNY.get(), rabbitAttributes);
        event.put(AntarchyForgeEntities.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.REVERIE.get(), ReverieEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        event.put(AntarchyForgeEntities.APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntities.GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        event.put(AntarchyForgeEntities.TERMITE.get(), buildAntAttributes(
                AntarchySettings.termiteHealth(),
                AntarchySettings.termiteMovementSpeed(),
                AntarchySettings.termiteAttackDamage()
        ).build());
        event.put(AntarchyForgeEntities.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        event.put(AntarchyForgeEntities.STRATOSHARK.get(), StratosharkEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.KRAKEN.get(), KrakenEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.TENTACLE.get(), TentacleEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.BED_BUG.get(), BedBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.WASP.get(), WaspEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.BOMBER.get(), BomberEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.SPRINGBUG.get(), SpringbugEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.SPIT_BUG.get(), SpitBugEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.MANTIS.get(), MantisEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.FLYTRAP.get(), FlytrapEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.LUCID.get(), LucidEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.VORTEX.get(), VortexEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.BASILISK.get(), BasiliskEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.CRAWLING_BLIGHT.get(), CrawlingBlightEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.SKULKING_FRIGHT.get(), SkulkingFrightEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.MANTICORE.get(), ManticoreEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.JERRY.get(), JerryEntity.createAttributes().build());

        AttributeSupplier redAntAttributes = buildAntAttributes(AntarchySettings.redAntHealth(), AntarchySettings.redAntMovementSpeed(), AntarchySettings.redAntAttackDamage()).build();
        AttributeSupplier brownAntAttributes = buildAntAttributes(AntarchySettings.brownAntHealth(), AntarchySettings.brownAntMovementSpeed(), AntarchySettings.brownAntAttackDamage()).build();
        AttributeSupplier rainbowAntAttributes = buildAntAttributes(AntarchySettings.rainbowAntHealth(), AntarchySettings.rainbowAntMovementSpeed(), AntarchySettings.rainbowAntAttackDamage()).build();
        event.put(AntarchyForgeEntities.RED_ANT.get(), redAntAttributes);
        event.put(AntarchyForgeEntities.BROWN_ANT.get(), brownAntAttributes);
        event.put(AntarchyForgeEntities.RAINBOW_ANT.get(), rainbowAntAttributes);
        event.put(AntarchyForgeEntities.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.WORM.get(), WormEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.CHEEP.get(), CheepEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.DORRIE.get(), DorrieEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.HERCULES_BEETLE.get(), HerculesBeetleEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.PRINCE.get(), PrinceEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.PRINCESS.get(), PrincessEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.KING.get(), KingEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.QUEEN.get(), QueenEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.OURANWOOD_DEER.get(), OuranwoodDeerEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.GLIMMER.get(), GlimmerEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.ELKA.get(), ElkaEntity.createAttributes().build());
        event.put(AntarchyForgeEntities.HOVERBOARD.get(), HoverboardEntity.createAttributes().build());
    }

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

}
