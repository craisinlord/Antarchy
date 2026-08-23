package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import java.util.function.Predicate;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HoverboardItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    private final EntityType<? extends HoverboardEntity> entityType;
    @Nullable
    private final DyeColor color;

    public HoverboardItem(EntityType<? extends HoverboardEntity> entityType, @Nullable DyeColor color, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.color = color;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        for (Entity entity : level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE)) {
            if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(player.getEyePosition())) {
                return InteractionResultHolder.pass(stack);
            }
        }

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        HoverboardEntity hoverboard = new HoverboardEntity(this.entityType, level);
        Vec3 spawnPos = hitResult.getLocation();
        hoverboard.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
        hoverboard.setColor(this.color);
        if (!level.noCollision(hoverboard, hoverboard.getBoundingBox())) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            level.addFreshEntity(hoverboard);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
            if (!player.getAbilities().instabuild) {
                stack.consume(1, player);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
