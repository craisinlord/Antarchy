package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.CheepEntity;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.entity.ElkaEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import com.craisinlord.antarchy.content.entity.SpitBugEntity;
import com.craisinlord.antarchy.content.entity.SpringbugEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.WormEntity;
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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public final class AntarchyFabricEntityAttributes {

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                EntityType.PLAYER,
                Player.createAttributes()
                        .add(AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.DOUBLE_DAMAGE_CHANCE))
                        .add(AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.BLOODGLASS_MAX_HEARTS))
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
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.STRATOSHARK.get(), StratosharkEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.KRAKEN.get(), KrakenEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TENTACLE.get(), TentacleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BED_BUG.get(), BedBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.WASP.get(), WaspEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BOMBER.get(), BomberEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SPRINGBUG.get(), SpringbugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SPIT_BUG.get(), SpitBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MANTIS.get(), MantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.FLYTRAP.get(), FlytrapEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.LUCID.get(), LucidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.VORTEX.get(), VortexEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BASILISK.get(), BasiliskEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CRAWLING_BLIGHT.get(), CrawlingBlightEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SKULKING_FRIGHT.get(), SkulkingFrightEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MANTICORE.get(), ManticoreEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.JERRY.get(), JerryEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CHEEP.get(), CheepEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.DORRIE.get(), DorrieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.HERCULES_BEETLE.get(), HerculesBeetleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.PRINCE.get(), PrinceEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.PRINCESS.get(), PrincessEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.KING.get(), KingEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.QUEEN.get(), QueenEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), OuranwoodDeerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.GLIMMER.get(), GlimmerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ELKA.get(), ElkaEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.HOVERBOARD.get(), HoverboardEntity.createAttributes().build());

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
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.WORM.get(), WormEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
    }

}
