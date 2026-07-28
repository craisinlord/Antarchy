package com.craisinlord.antarchy.fabric.entity.multipart;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.MultipartFramework;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartAccess;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MultipartPartEntity extends Entity implements MultipartPartAccess {
    @Nullable
    private MultipartEntityOwner owner;
    private int partIndex;
    @Nullable
    private MultipartPartDefinition definition;

    public MultipartPartEntity(EntityType<? extends MultipartPartEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public MultipartPartEntity antarchy$configure(MultipartEntityOwner owner, int partIndex, MultipartPartDefinition definition) {
        this.owner = owner;
        this.partIndex = partIndex;
        this.definition = definition;
        this.refreshDimensions();
        this.antarchy$syncFromParent();
        return this;
    }

    @Nullable
    public Entity antarchy$getMultipartParent() {
        return this.owner == null ? null : (Entity) this.owner;
    }

    public int getPartIndex() {
        return this.partIndex;
    }

    public String getPartName() {
        return this.definition == null ? "" : this.definition.name();
    }

    public float getDamageMultiplier() {
        return this.definition == null ? 1.0F : this.definition.damageMultiplier();
    }

    @Override
    public void antarchy$syncFromParent() {
        Entity parent = this.antarchy$getMultipartParent();
        MultipartPartDefinition definition = this.definition;
        if (parent == null || definition == null || parent.isRemoved()) {
            return;
        }

        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        double yawRadians = Math.toRadians(parent.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 position = new Vec3(
                parent.getX() + forward.x * definition.forwardOffset() + right.x * definition.lateralOffset(),
                parent.getY() + definition.yOffset(),
                parent.getZ() + forward.z * definition.forwardOffset() + right.z * definition.lateralOffset()
        );
        this.setPos(position.x, position.y, position.z);
        this.setYRot(parent.getYRot());
        this.setXRot(parent.getXRot());
        this.xo = oldX;
        this.yo = oldY;
        this.zo = oldZ;
        this.xOld = oldX;
        this.yOld = oldY;
        this.zOld = oldZ;
    }

    @Override
    public boolean is(Entity entity) {
        return entity == this || entity == this.antarchy$getMultipartParent();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        MultipartPartDefinition definition = this.definition;
        if (definition == null || !definition.collisionEnabled()) {
            return false;
        }

        Entity parent = this.antarchy$getMultipartParent();
        if (entity == parent) {
            return false;
        }

        if (entity instanceof MultipartPartEntity otherPart && otherPart.antarchy$getMultipartParent() == parent) {
            return false;
        }

        return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        Entity parent = this.antarchy$getMultipartParent();
        return parent == null ? ItemStack.EMPTY : parent.getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity parent = this.antarchy$getMultipartParent();
        MultipartPartDefinition definition = this.definition;
        if (parent == null || definition == null || parent.isRemoved()) {
            return false;
        }

        if (this.level().isClientSide()) {
            if (source.getEntity() instanceof Player) {
                MultipartFramework.sendMultipartAttack(parent.getUUID(), this.partIndex, amount * definition.damageMultiplier());
            }
            return true;
        }

        return this.owner != null && this.owner.antarchy$hurtMultipartPart(this, source, amount * definition.damageMultiplier());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return false;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        Entity parent = this.antarchy$getMultipartParent();
        if (parent == null || parent.isRemoved()) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            MultipartFramework.sendMultipartInteract(parent.getUUID(), this.partIndex, hand.ordinal());
            return InteractionResult.SUCCESS;
        }

        return this.owner == null ? InteractionResult.PASS : this.owner.antarchy$interactMultipartPart(this, player, vec, hand);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        MultipartPartDefinition definition = this.definition;
        return definition == null ? super.getDimensions(pose) : EntityDimensions.scalable(definition.width(), definition.height());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
