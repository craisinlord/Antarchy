package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.ComputerBlock;
import com.craisinlord.antarchy.content.computer.ComputerFileSystem;
import com.craisinlord.antarchy.content.computer.ComputerDesktopState;
import com.craisinlord.antarchy.content.guide.ComputerGuideData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ComputerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final ResourceLocation INTRODUCTION_DISK = ResourceLocation.fromNamespaceAndPath("antarchy", "introduction");
    private static final String DISKS_TAG = "Disks";
    private static final String ACTIVE_UNTIL_TAG = "ActiveUntil";
    private static final String PASSWORD_TAG = "Password";
    private static final String AUTHENTICATED_TAG = "Authenticated";
    private static final String AUTHENTICATED_UNTIL_TAG = "AuthenticatedUntil";
    private static final String DESKTOP_TAG = "Desktop";
    private static final String BASILISK_SCORE_TAG = "BasiliskScore";
    private static final String BASILISK_HIGH_SCORE_TAG = "BasiliskHighScore";
    private static final String ANTMAN_SCORE_TAG = "AntmanScore";
    private static final String ANTMAN_HIGH_SCORE_TAG = "AntmanHighScore";
    private static final String BLOCKLE_DAY_TAG = "BlockleDay";
    private static final String BLOCKLE_GUESSES_TAG = "BlockleGuesses";
    private static final long AUTHENTICATION_TIMEOUT = 6000L;
    private final Set<ResourceLocation> physicalDiskIds = new LinkedHashSet<>();
    private final ComputerFileSystem fileSystem = new ComputerFileSystem();
    private final ComputerDesktopState desktopState = new ComputerDesktopState();
    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ON = RawAnimation.begin().thenLoop("on_state");
    private static final RawAnimation OFF = RawAnimation.begin().thenLoop("off_state");
    private static final RawAnimation TURN_ON = RawAnimation.begin().thenPlay("turn_on");
    private static final RawAnimation TURN_OFF = RawAnimation.begin().thenPlay("turn_off");
    private long activeUntil;
    private String password = "";
    private boolean authenticated;
    private long authenticatedUntil;
    private java.util.UUID activeUser;
    private boolean lastActive;
    private int basiliskScore;
    private int basiliskHighScore;
    private int antmanScore;
    private int antmanHighScore;
    private long blockleDay = -1L;
    private final List<String> blockleGuesses = new ArrayList<>();

    public ComputerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<ComputerBlockEntity>> type) {
        super(type.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity computer) {
        boolean active = computer.isActive();
        if (!level.isClientSide && state.getValue(ComputerBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(ComputerBlock.ACTIVE, active), 3);
        }
        if (active != computer.lastActive) {
            computer.lastActive = active;
            computer.triggerAnim("transitions", active ? "turn_on" : "turn_off");
        }
        if (!level.isClientSide && computer.activeUntil > 0L && level.getGameTime() >= computer.activeUntil && !level.hasNeighborSignal(pos)) {
            computer.activeUntil = 0L;
            computer.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (!level.isClientSide && computer.authenticated && (level.getGameTime() >= computer.authenticatedUntil || computer.activeUser == null ||
                (level instanceof net.minecraft.server.level.ServerLevel serverLevel && serverLevel.getServer().getPlayerList().getPlayer(computer.activeUser) == null))) {
            computer.clearAuthentication();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean insert(ItemStack stack, Player player) {
        if (!stack.is(AntarchyObjects.FLOPPY_DISK.get())) {
            return false;
        }
        ResourceLocation diskId = stack.get(AntarchyObjects.FLOPPY_DISK_COMPONENT.get());
        if (diskId == null || this.physicalDiskIds.contains(diskId)) {
            stack.shrink(1);
            activate();
            return true;
        }
        this.physicalDiskIds.add(diskId);
        ComputerGuideData.Disk disk = ComputerGuideData.disk(diskId);
        if (disk != null && !disk.wallpaper().isBlank()) {
            try {
                desktopState.unlockWallpaper(ResourceLocation.parse(disk.wallpaper()));
            } catch (RuntimeException ignored) {
            }
        }
        stack.shrink(1);
        activate();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public void activate() {
        if (level != null && !level.isClientSide) {
            activeUntil = Math.max(activeUntil, level.getGameTime() + 40L);
            if (!getBlockState().getValue(ComputerBlock.ACTIVE)) {
                level.setBlock(worldPosition, getBlockState().setValue(ComputerBlock.ACTIVE, true), 3);
            }
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isActive() {
        return level != null && (level.hasNeighborSignal(worldPosition) || isAuthenticated());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "state", state -> state.setAndContinue(isActive() ? ON : OFF)));
        controllers.add(new AnimationController<>(this, "transitions", state -> PlayState.STOP)
                .triggerableAnim("turn_on", TURN_ON)
                .triggerableAnim("turn_off", TURN_OFF));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }

    public List<ResourceLocation> diskIds() {
        List<ResourceLocation> ids = new ArrayList<>(physicalDiskIds.size() + 1);
        ids.add(INTRODUCTION_DISK);
        ids.addAll(physicalDiskIds);
        return List.copyOf(ids);
    }

    public boolean ejectOne(Player player) {
        if (physicalDiskIds.isEmpty() || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return ejectOne(serverPlayer, physicalDiskIds.iterator().next());
    }

    public boolean ejectOne(ServerPlayer player, ResourceLocation diskId) {
        if (level == null || level.isClientSide || diskId == null || !isAuthenticated() || !playerCanReach(player) || !physicalDiskIds.remove(diskId)) {
            return false;
        }
        ItemStack disk = diskStack(diskId);
        if (!player.addItem(disk)) {
            player.drop(disk, false);
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public void dropDisks(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (ResourceLocation diskId : physicalDiskIds) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, diskStack(diskId));
        }
        physicalDiskIds.clear();
    }

    public boolean hasPassword() {
        return !password.isEmpty();
    }

    public boolean setPassword(ServerPlayer player, String newPassword) {
        if (newPassword == null || newPassword.isBlank() || !isAuthenticated()) {
            return false;
        }
        password = newPassword;
        setChanged();
        return true;
    }

    public boolean initializePassword(ServerPlayer player, String newPassword) {
        if (hasPassword() || newPassword == null || newPassword.isBlank() || !claimUser(player)) {
            return false;
        }
        password = newPassword;
        authenticated = true;
        authenticatedUntil = level.getGameTime() + AUTHENTICATION_TIMEOUT;
        setChanged();
        return true;
    }

    public boolean initializePassword(String newPassword) {
        return false;
    }

    public boolean authenticate(ServerPlayer player, String attemptedPassword) {
        if (!hasPassword() || !password.equals(attemptedPassword) || !claimUser(player)) {
            return false;
        }
        authenticated = true;
        authenticatedUntil = level == null ? 0L : level.getGameTime() + AUTHENTICATION_TIMEOUT;
        setChanged();
        return true;
    }

    public boolean authenticate(String attemptedPassword) {
        return false;
    }

    public boolean isAuthenticated() {
        if (!authenticated) {
            return false;
        }
        if (level != null && level.getGameTime() >= authenticatedUntil) {
            authenticated = false;
            authenticatedUntil = 0L;
            setChanged();
            return false;
        }
        return true;
    }

    public boolean isAuthenticatedBy(ServerPlayer player) {
        return isAuthenticated() && activeUser != null && activeUser.equals(player.getUUID());
    }

    public boolean claimUser(ServerPlayer player) {
        if (level == null || level.isClientSide || !playerCanReach(player)) {
            return false;
        }
        if (activeUser != null && !activeUser.equals(player.getUUID()) && isAuthenticated()) {
            return false;
        }
        activeUser = player.getUUID();
        return true;
    }

    public boolean playerCanReach(ServerPlayer player) {
        return player.level() == level && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public ComputerFileSystem fileSystem() {
        return fileSystem;
    }

    public ComputerDesktopState desktopState() {
        return desktopState;
    }

    public int basiliskScore() {
        return basiliskScore;
    }

    public int basiliskHighScore() {
        return basiliskHighScore;
    }

    public void setBasiliskScore(int score) {
        basiliskScore = Math.max(0, score);
        basiliskHighScore = Math.max(basiliskHighScore, basiliskScore);
        setChanged();
    }

    public int antmanScore() { return antmanScore; }
    public int antmanHighScore() { return antmanHighScore; }
    public void setAntmanScore(int score) {
        antmanScore = Math.max(0, score);
        antmanHighScore = Math.max(antmanHighScore, antmanScore);
        setChanged();
    }

    public long blockleDay() {
        return blockleDay;
    }

    public List<String> blockleGuesses() {
        return List.copyOf(blockleGuesses);
    }

    public void resetBlockle(long day) {
        if (blockleDay == day) return;
        blockleDay = day;
        blockleGuesses.clear();
        setChanged();
    }

    public boolean addBlockleGuess(String guess) {
        if (blockleGuesses.size() >= 6) return false;
        blockleGuesses.add(guess);
        setChanged();
        return true;
    }

    public boolean unlockWallpaper(ResourceLocation id) {
        return serverDesktopMutation(() -> desktopState.unlockWallpaper(id));
    }

    public boolean installProgram(ResourceLocation id) {
        return serverDesktopMutation(() -> desktopState.installProgram(id));
    }

    public boolean uninstallProgram(ResourceLocation id) {
        return serverDesktopMutation(() -> desktopState.uninstallProgram(id));
    }

    public boolean selectWallpaper(ResourceLocation id) {
        return serverDesktopMutation(() -> desktopState.selectWallpaper(id));
    }

    public boolean setIconPosition(ResourceLocation id, int x, int y) {
        return serverDesktopMutation(() -> desktopState.setIconPosition(id, x, y));
    }

    public boolean removeIconPosition(ResourceLocation id) {
        return serverDesktopMutation(() -> desktopState.removeIconPosition(id));
    }

    private boolean serverDesktopMutation(java.util.function.BooleanSupplier mutation) {
        if (level == null || level.isClientSide) {
            return false;
        }
        boolean changed = mutation.getAsBoolean();
        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return changed;
    }

    public boolean canUseFileSystem(ServerPlayer player) {
        return level != null && !level.isClientSide && isAuthenticated() && playerCanReach(player) && (activeUser == null || activeUser.equals(player.getUUID()));
    }

    public void logout() {
        clearAuthentication();
    }

    public void releaseUser(ServerPlayer player) {
        if (activeUser != null && activeUser.equals(player.getUUID())) {
            activeUser = null;
            setChanged();
        }
    }

    public void clearAuthentication() {
        authenticated = false;
        authenticatedUntil = 0L;
        activeUser = null;
        setChanged();
    }

    public long authenticationTimeout() {
        return AUTHENTICATION_TIMEOUT;
    }

    public boolean hasActiveUser() {
        return activeUser != null && isAuthenticated();
    }

    private static ItemStack diskStack(ResourceLocation diskId) {
        ItemStack stack = new ItemStack(AntarchyObjects.FLOPPY_DISK.get());
        stack.set(AntarchyObjects.FLOPPY_DISK_COMPONENT.get(), diskId);
        return stack;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        physicalDiskIds.clear();
        ListTag disks = tag.getList(DISKS_TAG, 8);
        for (int index = 0; index < disks.size(); index++) {
            try {
                ResourceLocation diskId = ResourceLocation.parse(disks.getString(index));
                if (!INTRODUCTION_DISK.equals(diskId)) {
                    physicalDiskIds.add(diskId);
                }
            } catch (Exception ignored) {
            }
        }
        activeUntil = tag.getLong(ACTIVE_UNTIL_TAG);
        password = tag.getString(PASSWORD_TAG);
        fileSystem.load(tag, registries);
        desktopState.load(tag.getCompound(DESKTOP_TAG));
        basiliskScore = tag.getInt(BASILISK_SCORE_TAG);
        basiliskHighScore = tag.getInt(BASILISK_HIGH_SCORE_TAG);
        antmanScore = tag.getInt(ANTMAN_SCORE_TAG);
        antmanHighScore = tag.getInt(ANTMAN_HIGH_SCORE_TAG);
        blockleDay = tag.contains(BLOCKLE_DAY_TAG) ? tag.getLong(BLOCKLE_DAY_TAG) : -1L;
        blockleGuesses.clear();
        ListTag blockle = tag.getList(BLOCKLE_GUESSES_TAG, 8);
        for (int index = 0; index < blockle.size(); index++) {
            String guess = blockle.getString(index);
            if (guess.length() == 5 && !blockleGuesses.contains(guess) && blockleGuesses.size() < 6) blockleGuesses.add(guess);
        }
        authenticated = false;
        authenticatedUntil = 0L;
        activeUser = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag disks = new ListTag();
        for (ResourceLocation diskId : physicalDiskIds) {
            disks.add(StringTag.valueOf(diskId.toString()));
        }
        tag.put(DISKS_TAG, disks);
        tag.putLong(ACTIVE_UNTIL_TAG, activeUntil);
        tag.putString(PASSWORD_TAG, password);
        fileSystem.save(tag, registries);
        tag.put(DESKTOP_TAG, desktopState.save());
        tag.putInt(BASILISK_SCORE_TAG, basiliskScore);
        tag.putInt(BASILISK_HIGH_SCORE_TAG, basiliskHighScore);
        tag.putInt(ANTMAN_SCORE_TAG, antmanScore);
        tag.putInt(ANTMAN_HIGH_SCORE_TAG, antmanHighScore);
        tag.putLong(BLOCKLE_DAY_TAG, blockleDay);
        ListTag blockle = new ListTag();
        for (String guess : blockleGuesses) blockle.add(StringTag.valueOf(guess));
        tag.put(BLOCKLE_GUESSES_TAG, blockle);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasPassword", hasPassword());
        tag.putBoolean(AUTHENTICATED_TAG, isAuthenticated());
        ListTag disks = new ListTag();
        for (ResourceLocation diskId : physicalDiskIds) disks.add(StringTag.valueOf(diskId.toString()));
        tag.put(DISKS_TAG, disks);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
