package com.craisinlord.antarchy.neoforge.entity.multipart;

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
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class MultipartPartEntity extends PartEntity<Entity> implements OwnableEntity, MultipartPartAccess {
    private final MultipartEntityOwner owner;
    private final int partIndex;
    private final MultipartPartDefinition definition;

    public MultipartPartEntity(Entity parent, MultipartEntityOwner owner, int partIndex, MultipartPartDefinition definition) {
        super(parent);
        this.owner = owner;
        this.partIndex = partIndex;
        this.definition = definition;
        this.setNoGravity(true);
        this.refreshDimensions();
        this.antarchy$syncFromParent();
    }

    public int getPartIndex() {
        return this.partIndex;
    }

    public String getPartName() {
        return this.definition.name();
    }

    public float getDamageMultiplier() {
        return this.definition.damageMultiplier();
    }

    public MultipartPartDefinition getPartDefinition() {
        return this.definition;
    }

    @Override
    public @Nullable java.util.UUID getOwnerUUID() {
        Entity parent = this.getParent();
        return parent instanceof OwnableEntity ownable ? ownable.getOwnerUUID() : null;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.definition.width(), this.definition.height());
    }

    @Override
    public void antarchy$syncFromParent() {
        Entity parent = this.getParent();
        if (parent == null || parent.isRemoved()) {
            return;
        }

        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        double yawRadians = Math.toRadians(parent.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 position = new Vec3(
                parent.getX() + forward.x * this.definition.forwardOffset() + right.x * this.definition.lateralOffset(),
                parent.getY() + this.definition.yOffset(),
                parent.getZ() + forward.z * this.definition.forwardOffset() + right.z * this.definition.lateralOffset()
        );
        this.setPos(position.x, position.y, position.z);
        this.setRot(parent.getYRot(), parent.getXRot());
        this.xo = oldX;
        this.yo = oldY;
        this.zo = oldZ;
        this.xOld = oldX;
        this.yOld = oldY;
        this.zOld = oldZ;
    }

    @Override
    public boolean is(Entity entity) {
        return entity == this || entity == this.getParent();
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
        // Always true so player melee raycasts (ProjectileUtil.getEntityHitResult) can find this part.
        // Physical push is prevented separately via isPushable()=false and canCollideWith()=false.
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if (!this.definition.collisionEnabled()) {
            return false;
        }

        Entity parent = this.getParent();
        if (entity == parent) {
            return false;
        }

        if (entity instanceof PartEntity<?> otherPart && otherPart.getParent() == parent) {
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
        Entity parent = this.getParent();
        return parent == null ? ItemStack.EMPTY : parent.getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity parent = this.getParent();
        if (parent == null || parent.isRemoved()) {
            return false;
        }

        if (this.level().isClientSide()) {
            if (source.getEntity() instanceof Player) {
                MultipartFramework.sendMultipartAttack(parent.getUUID(), this.partIndex, amount * this.definition.damageMultiplier());
            }
            return true;
        }

        return this.owner.antarchy$hurtMultipartPart(this, source, amount * this.definition.damageMultiplier());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return false;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        Entity parent = this.getParent();
        if (parent == null || parent.isRemoved()) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            MultipartFramework.sendMultipartInteract(parent.getUUID(), this.partIndex, hand.ordinal());
            return InteractionResult.SUCCESS;
        }

        return this.owner.antarchy$interactMultipartPart(this, player, vec, hand);
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
