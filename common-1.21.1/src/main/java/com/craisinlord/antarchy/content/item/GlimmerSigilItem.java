package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerSigilTarget;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerSite;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GlimmerSigilItem extends Item {
    private static final int CHANNEL_TICKS = 60;
    private static final double OCCUPIED_MARGIN = 16.0D;
    private static final int SPAWN_VERTICAL_SEARCH = 16;
    private static final int[] SPAWN_RADII = {0, 4, 8};
    private static final int[] AIR_SPAWN_RADII = {6, 10, 14};
    private static final int AIR_SPAWN_LIFT = 6;

    private final GlimmerSigilTarget target;

    public GlimmerSigilItem(GlimmerSigilTarget target, Properties properties) {
        super(properties);
        this.target = target;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (!level.isClientSide) {
                this.message(player, "peaceful");
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            String failure = this.validate(serverLevel, serverPlayer).failure();
            if (failure != null) {
                this.message(player, failure);
                return InteractionResultHolder.fail(stack);
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.7F);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel) || remainingUseDuration % 4 != 0) {
            return;
        }
        Optional<GlimmerSite.Match> site = this.findSite(serverLevel, user.blockPosition());
        if (site.isEmpty()) {
            return;
        }
        BlockPos center = site.get().spawnCenter();
        Vec3 point = new Vec3(center.getX() + 0.5D, user.getY() + 0.5D, center.getZ() + 0.5D);
        double progress = 1.0D - remainingUseDuration / (double) CHANNEL_TICKS;
        GlimmerEchoes.ring(serverLevel, point, 10.0D * (1.0D - progress) + 1.5D, 24);
        GlimmerEchoes.burst(serverLevel, user.position().add(0.0D, 1.0D, 0.0D), 0.4D, 3);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) {
            return stack;
        }
        Validation validation = this.validate(serverLevel, player);
        if (validation.failure() != null) {
            this.message(player, validation.failure());
            return stack;
        }
        Mob boss = this.target.entityType().get().create(serverLevel);
        if (boss == null) {
            return stack;
        }
        if (boss instanceof GlimmerEcho echo) {
            echo.setGlimmerEcho(true);
        }
        Vec3 spawn = this.findSpawn(serverLevel, boss, validation.site(), player.blockPosition());
        if (spawn == null) {
            this.message(player, "no_space");
            return stack;
        }
        boss.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(spawn)), MobSpawnType.TRIGGERED, null);
        if (boss instanceof GlimmerEcho echo) {
            echo.onGlimmerSummoned(serverLevel, validation.site(), player);
        }
        boss.setPersistenceRequired();
        boss.setTarget(player);
        serverLevel.addFreshEntity(boss);
        GlimmerEchoes.burst(serverLevel, spawn.add(0.0D, boss.getBbHeight() * 0.5D, 0.0D), boss.getBbWidth(), 80);
        serverLevel.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.8F);
        this.message(player, "summoned");
        stack.consume(1, player);
        player.getCooldowns().addCooldown(this, 40);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHANNEL_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String key = "tooltip.antarchy.glimmer_sigil." + this.target.bossKey();
        tooltipComponents.add(Component.translatable(key).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.glimmer_sigil.use").withStyle(ChatFormatting.GRAY));
    }

    private Validation validate(ServerLevel level, ServerPlayer player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return Validation.fail("peaceful");
        }
        if (!player.getAbilities().instabuild && !this.hasDefeatedBoss(player)) {
            return Validation.fail("not_defeated");
        }
        Optional<GlimmerSite.Match> site = this.findSite(level, player.blockPosition());
        if (site.isEmpty()) {
            return Validation.fail("wrong_site");
        }
        AABB area = AABB.of(site.get().area()).inflate(OCCUPIED_MARGIN);
        if (!level.getEntities(this.target.entityType().get(), area, GlimmerSigilItem::occupiesSite).isEmpty()) {
            return Validation.fail("occupied");
        }
        return new Validation(site.get(), null);
    }

    private static boolean occupiesSite(Entity entity) {
        return entity.isAlive() && !(entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null);
    }

    private boolean hasDefeatedBoss(ServerPlayer player) {
        AdvancementHolder holder = player.server.getAdvancements().get(this.target.defeatAdvancement());
        return holder == null || player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private Optional<GlimmerSite.Match> findSite(ServerLevel level, BlockPos pos) {
        return this.target.site().find(level, pos);
    }

    @Nullable
    private Vec3 findSpawn(ServerLevel level, Mob boss, GlimmerSite.Match match, BlockPos origin) {
        BlockPos center = match.spawnCenter();
        BoundingBox site = match.area();
        boolean air = this.target.spawnMode() == GlimmerSigilTarget.SpawnMode.AIR;
        int baseY = air ? origin.getY() + AIR_SPAWN_LIFT : origin.getY();
        for (int radius : air ? AIR_SPAWN_RADII : SPAWN_RADII) {
            int samples = radius == 0 ? 1 : 8;
            for (int sample = 0; sample < samples; sample++) {
                double angle = sample * Math.PI * 2.0D / samples;
                int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
                for (int offset = 0; offset <= SPAWN_VERTICAL_SEARCH; offset++) {
                    Vec3 above = this.trySpawnAt(level, boss, site, x, baseY + offset, z, origin, air);
                    if (above != null) {
                        return above;
                    }
                    if (offset > 0) {
                        Vec3 below = this.trySpawnAt(level, boss, site, x, baseY - offset, z, origin, air);
                        if (below != null) {
                            return below;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private Vec3 trySpawnAt(ServerLevel level, Mob boss, BoundingBox site, int x, int y, int z, BlockPos origin, boolean air) {
        if (y < site.minY() || y > site.maxY()) {
            return null;
        }
        BlockPos feet = new BlockPos(x, y, z);
        BlockPos support = feet.below();
        if (!air && !level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
            return null;
        }
        float yaw = (float) (Math.atan2(origin.getZ() - z, origin.getX() - x) * (180.0D / Math.PI)) - 90.0F;
        boss.moveTo(x + 0.5D, y, z + 0.5D, yaw, 0.0F);
        return level.noCollision(boss) ? boss.position() : null;
    }

    private void message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message.antarchy.glimmer_sigil." + key)
                .withStyle(ChatFormatting.AQUA), true);
    }

    private record Validation(@Nullable GlimmerSite.Match site, @Nullable String failure) {
        static Validation fail(String failure) {
            return new Validation(null, failure);
        }
    }
}
