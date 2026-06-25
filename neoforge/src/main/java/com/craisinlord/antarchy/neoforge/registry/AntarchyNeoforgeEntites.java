package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.AppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.GoldenAppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.HoneyedAppleCow;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.entity.DrTrayaurusEntity;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodBoatEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodChestBoatEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AntarchyNeoforgeEntites {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Antarchy.MODID);

    public static final boolean CREATE_LOADED = isModLoaded("create");

    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static final DeferredHolder<EntityType<?>, EntityType<RedAntEntity>> RED_ANT = ENTITY_TYPES.register("red_ant",
            () -> buildAntType(RedAntEntity::new, MobCategory.CREATURE, "red_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrownAntEntity>> BROWN_ANT = ENTITY_TYPES.register("brown_ant",
            () -> buildAntType(BrownAntEntity::new, MobCategory.CREATURE, "brown_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<RainbowAntEntity>> RAINBOW_ANT = ENTITY_TYPES.register("rainbow_ant",
            () -> buildAntType(RainbowAntEntity::new, MobCategory.CREATURE, "rainbow_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<MolewormEntity>> MOLEWORM = ENTITY_TYPES.register("moleworm",
            () -> EntityType.Builder.of(MolewormEntity::new, MobCategory.MONSTER)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(8)
                    .build("moleworm"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantisEntity>> MANTIS = ENTITY_TYPES.register("mantis",
            () -> EntityType.Builder.of(MantisEntity::new, MobCategory.MONSTER)
                    .sized(3.125F, 2.5F)
                    .clientTrackingRange(8)
                    .build("mantis"));
    public static final DeferredHolder<EntityType<?>, EntityType<OuranwoodBoatEntity>> OURANWOOD_BOAT_ENTITY = ENTITY_TYPES.register("ouranwood_boat",
            () -> EntityType.Builder.<OuranwoodBoatEntity>of(OuranwoodBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("ouranwood_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<OuranwoodChestBoatEntity>> OURANWOOD_CHEST_BOAT_ENTITY = ENTITY_TYPES.register("ouranwood_chest_boat",
            () -> EntityType.Builder.<OuranwoodChestBoatEntity>of(OuranwoodChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("ouranwood_chest_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<MolevoreEntity>> MOLEVORE = ENTITY_TYPES.register("molevore",
            () -> EntityType.Builder.of(MolevoreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 1.1F)
                    .clientTrackingRange(10)
                    .build("molevore"));
    public static final DeferredHolder<EntityType<?>, EntityType<TriffidEntity>> TRIFFID = ENTITY_TYPES.register("triffid",
            () -> EntityType.Builder.of(TriffidEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 5.0F)
                    .clientTrackingRange(10)
                    .build("triffid"));
    public static final DeferredHolder<EntityType<?>, EntityType<BedBugEntity>> BED_BUG = ENTITY_TYPES.register("bed_bug",
            () -> EntityType.Builder.of(BedBugEntity::new, MobCategory.CREATURE)
                    .sized(1.15F, 0.8F)
                    .clientTrackingRange(8)
                    .build("bed_bug"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaspEntity>> WASP = ENTITY_TYPES.register("wasp",
            () -> EntityType.Builder.of(WaspEntity::new, MobCategory.MONSTER)
                    .sized(0.8625F, 1.365F)
                    .clientTrackingRange(8)
                    .build("wasp"));
    public static final DeferredHolder<EntityType<?>, EntityType<BomberEntity>> BOMBER = ENTITY_TYPES.register("bomber",
            () -> EntityType.Builder.of(BomberEntity::new, MobCategory.MONSTER)
                    .sized(0.55F, 0.75F)
                    .clientTrackingRange(8)
                    .build("bomber"));
    public static final DeferredHolder<EntityType<?>, EntityType<JumpyBugEntity>> JUMPY_BUG = ENTITY_TYPES.register("jumpy_bug",
            () -> EntityType.Builder.of(JumpyBugEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 3.0F)
                    .clientTrackingRange(8)
                    .build("jumpy_bug"));
    public static final DeferredHolder<EntityType<?>, EntityType<SizeRayProjectileEntity>> SHRINK_RAY_PROJECTILE = ENTITY_TYPES.register("shrink_ray_projectile",
            () -> EntityType.Builder.<SizeRayProjectileEntity>of(SizeRayProjectileEntity::createShrink, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("shrink_ray_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SizeRayProjectileEntity>> GROWTH_RAY_PROJECTILE = ENTITY_TYPES.register("growth_ray_projectile",
            () -> EntityType.Builder.<SizeRayProjectileEntity>of(SizeRayProjectileEntity::createGrowth, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("growth_ray_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<DiamondMinecartEntity>> DIAMOND_MINECART = ENTITY_TYPES.register("diamond_minecart",
            () -> EntityType.Builder.<DiamondMinecartEntity>of((entityType, level) -> new DiamondMinecartEntity(entityType, level, AntarchyNeoforgeItems.diamondMinecartItem()), MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(8)
                    .build("diamond_minecart"));
    public static final DeferredHolder<EntityType<?>, EntityType<EasterBunnyEntity>> EASTER_BUNNY = ENTITY_TYPES.register("easter_bunny",
            () -> EntityType.Builder.of(EasterBunnyEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.0F)
                    .clientTrackingRange(8)
                    .build("easter_bunny"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingSquirrelEntity>> FLYING_SQUIRREL = ENTITY_TYPES.register("flying_squirrel",
            () -> EntityType.Builder.of(FlyingSquirrelEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(8)
                    .build("flying_squirrel"));
    public static final DeferredHolder<EntityType<?>, EntityType<CaterpillarEntity>> CATERPILLAR = ENTITY_TYPES.register("caterpillar",
            () -> EntityType.Builder.of(CaterpillarEntity::new, MobCategory.CREATURE)
                    .sized(0.8775F, 0.585F)
                    .clientTrackingRange(8)
                    .build("caterpillar"));
    public static final DeferredHolder<EntityType<?>, EntityType<ButterflyEntity>> BUTTERFLY = ENTITY_TYPES.register("butterfly",
            () -> EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                    .sized(1.125F, 1.375F)
                    .clientTrackingRange(8)
                    .build("butterfly"));
    public static final DeferredHolder<EntityType<?>, EntityType<ReverieEntity>> REVERIE = ENTITY_TYPES.register("reverie",
            () -> EntityType.Builder.of(ReverieEntity::new, MobCategory.AMBIENT)
                    .sized(0.7F, 0.7F)
                    .clientTrackingRange(8)
                    .build("reverie"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrutalflyEntity>> BRUTALFLY = ENTITY_TYPES.register("brutalfly",
            () -> EntityType.Builder.of(BrutalflyEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 2.1F)
                    .clientTrackingRange(12)
                    .build("brutalfly"));
    public static final DeferredHolder<EntityType<?>, EntityType<AppleCow>> APPLE_COW = ENTITY_TYPES.register("apple_cow",
            () -> buildCowType(AppleCow::new, "apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<GoldenAppleCow>> GOLDEN_APPLE_COW = ENTITY_TYPES.register("golden_apple_cow",
            () -> buildCowType(GoldenAppleCow::new, "golden_apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedGoldenAppleCow>> ENCHANTED_GOLDEN_APPLE_COW = ENTITY_TYPES.register("enchanted_golden_apple_cow",
            () -> buildCowType(EnchantedGoldenAppleCow::new, "enchanted_golden_apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<HoneyedAppleCow>> HONEYED_APPLE_COW = CREATE_LOADED
            ? ENTITY_TYPES.register("honeyed_apple_cow", () -> buildCowType(HoneyedAppleCow::new, "honeyed_apple_cow"))
            : null;
    public static final DeferredHolder<EntityType<?>, EntityType<DrTrayaurusEntity>> DR_TRAYAURUS = ENTITY_TYPES.register("dr_trayaurus",
            () -> EntityType.Builder.of(DrTrayaurusEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("dr_trayaurus"));
    public static final DeferredHolder<EntityType<?>, EntityType<CloudSharkEntity>> CLOUD_SHARK = ENTITY_TYPES.register("cloud_shark",
            () -> EntityType.Builder.of(CloudSharkEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 0.9F)
                    .clientTrackingRange(10)
                    .build("cloud_shark"));
    public static final DeferredHolder<EntityType<?>, EntityType<KrakenEntity>> KRAKEN = ENTITY_TYPES.register("kraken",
            () -> EntityType.Builder.of(KrakenEntity::new, MobCategory.MONSTER)
                    .sized(5.2F, 9.8F)
                    .clientTrackingRange(12)
                    .build("kraken"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileSquidEntity>> MISSILE_SQUID = ENTITY_TYPES.register("missile_squid",
            () -> EntityType.Builder.of(MissileSquidEntity::new, MobCategory.MONSTER)
                    .sized(1.62F, 3.18F)
                    .clientTrackingRange(10)
                    .build("missile_squid"));
    public static final DeferredHolder<EntityType<?>, EntityType<NightmareEntity>> NIGHTMARE = ENTITY_TYPES.register("nightmare",
            () -> EntityType.Builder.of(NightmareEntity::new, MobCategory.MONSTER)
                    .sized(3.2F, 3.8F)
                    .clientTrackingRange(12)
                    .build("nightmare"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidEntity>> LUCID = ENTITY_TYPES.register("lucid",
            () -> EntityType.Builder.of(LucidEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(10)
                    .build("lucid"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidBoltEntity>> LUCID_BOLT = ENTITY_TYPES.register("lucid_bolt",
            () -> EntityType.Builder.<LucidBoltEntity>of(LucidBoltEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("lucid_bolt"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScorpionEntity>> SCORPION = ENTITY_TYPES.register("scorpion",
            () -> EntityType.Builder.of(ScorpionEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, 1.0F)
                    .clientTrackingRange(8)
                    .build("scorpion"));
    public static final DeferredHolder<EntityType<?>, EntityType<BasiliskEntity>> BASILISK = ENTITY_TYPES.register("basilisk",
            () -> EntityType.Builder.of(BasiliskEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 3.5F)
                    .clientTrackingRange(14)
                    .build("basilisk"));
    public static final DeferredHolder<EntityType<?>, EntityType<EmperorScorpionEntity>> EMPEROR_SCORPION = ENTITY_TYPES.register("emperor_scorpion",
            () -> EntityType.Builder.of(EmperorScorpionEntity::new, MobCategory.MONSTER)
                    .sized(4.05F, 1.35F)
                    .clientTrackingRange(10)
                    .build("emperor_scorpion"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidEyeProjectileEntity>> LUCID_PEARL_PROJECTILE = ENTITY_TYPES.register("lucid_pearl_projectile",
            () -> EntityType.Builder.<LucidEyeProjectileEntity>of(LucidEyeProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("lucid_pearl_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<HushProjectileEntity>> HUSH_PROJECTILE = ENTITY_TYPES.register("hush_projectile",
            () -> EntityType.Builder.<HushProjectileEntity>of(HushProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("hush_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrutalflyOrbEntity>> BRUTALFLY_ORB = ENTITY_TYPES.register("brutalfly_orb",
            () -> EntityType.Builder.<BrutalflyOrbEntity>of(BrutalflyOrbEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("brutalfly_orb"));
    public static final DeferredHolder<EntityType<?>, EntityType<ToreterrorEntity>> TORETERROR = ENTITY_TYPES.register("toreterror",
            () -> EntityType.Builder.of(ToreterrorEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.0F)
                    .clientTrackingRange(14)
                    .build("toreterror"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaterBombEntity>> WATER_BOMB = ENTITY_TYPES.register("water_bomb",
            () -> EntityType.Builder.<WaterBombEntity>of(WaterBombEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("water_bomb"));
    public static final DeferredHolder<EntityType<?>, EntityType<CreepingHorrorEntity>> CREEPING_HORROR = ENTITY_TYPES.register("creeping_horror",
            () -> EntityType.Builder.of(CreepingHorrorEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.5F)
                    .clientTrackingRange(10)
                    .build("creeping_horror"));
    public static final DeferredHolder<EntityType<?>, EntityType<LurkingTerrorEntity>> LURKING_TERROR = ENTITY_TYPES.register("lurking_terror",
            () -> EntityType.Builder.of(LurkingTerrorEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.6F)
                    .clientTrackingRange(10)
                    .build("lurking_terror"));
    public static final DeferredHolder<EntityType<?>, EntityType<UpwardFallingBlockEntity>> UPWARD_FALLING_BLOCK = ENTITY_TYPES.register("upward_falling_block",
            () -> EntityType.Builder.<UpwardFallingBlockEntity>of(
                            UpwardFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("upward_falling_block"));

    private AntarchyNeoforgeEntites() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    static <T extends BaseAntEntity> EntityType<T> buildAntType(EntityType.EntityFactory<T> factory, MobCategory category, String name) {
        return EntityType.Builder.of(factory, category)
                .sized(0.6375F, 0.2125F)
                .clientTrackingRange(8)
                .build(name);
    }

    static <T extends Cow> EntityType<T> buildCowType(EntityType.EntityFactory<T> factory, String name) {
        return EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(0.9F, 1.4F)
                .clientTrackingRange(10)
                .build(name);
    }
}
