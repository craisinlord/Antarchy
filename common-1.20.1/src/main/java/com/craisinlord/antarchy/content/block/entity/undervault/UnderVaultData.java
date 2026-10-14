package com.craisinlord.antarchy.content.block.entity.undervault;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public final class UnderVaultData {
    private final UnderVaultServerData serverData = new UnderVaultServerData();
    private final UnderVaultSharedData sharedData = new UnderVaultSharedData();
    private final UnderVaultClientData clientData = new UnderVaultClientData();
    private UnderVaultConfig config = UnderVaultConfig.DEFAULT;
    private Level level;

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(UnderVaultConfig.TAG_NAME, this.config.save());
        tag.put(UnderVaultSharedData.TAG_NAME, this.sharedData.save());
        tag.put(UnderVaultServerData.TAG_NAME, this.serverData.save());
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains(UnderVaultServerData.TAG_NAME, Tag.TAG_COMPOUND)) {
            this.serverData.set(UnderVaultServerData.load(tag.getCompound(UnderVaultServerData.TAG_NAME)));
        }
        if (tag.contains(UnderVaultConfig.TAG_NAME, Tag.TAG_COMPOUND)) {
            UnderVaultConfig.load(tag.getCompound(UnderVaultConfig.TAG_NAME)).ifPresent(loaded -> this.config = loaded);
        }
        if (tag.contains(UnderVaultSharedData.TAG_NAME, Tag.TAG_COMPOUND)) {
            this.sharedData.set(UnderVaultSharedData.load(tag.getCompound(UnderVaultSharedData.TAG_NAME)));
        }
    }

    public UnderVaultServerData getServerData() {
        return this.level != null && !this.level.isClientSide ? this.serverData : null;
    }

    public UnderVaultSharedData getSharedData() {
        return this.sharedData;
    }

    public UnderVaultClientData getClientData() {
        return this.clientData;
    }

    public UnderVaultConfig getConfig() {
        return this.config;
    }

    public void setConfig(UnderVaultConfig config) {
        this.config = config;
    }
}
