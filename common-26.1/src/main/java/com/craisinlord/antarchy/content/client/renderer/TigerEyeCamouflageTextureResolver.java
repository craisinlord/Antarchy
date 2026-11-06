package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TigerEyeCamouflageTextureResolver {
    public record ResolvedTexture(Identifier texture, int argbTint) {}

    private static final int SKIN_SIZE = 64;
    private static final Map<Integer, ResolvedTexture> CACHE = new ConcurrentHashMap<>();
    private static final Identifier FALLBACK_TEXTURE = Identifier.withDefaultNamespace("textures/block/stone.png");
    private static final long QUAD_SEED = 0x54EADL;

    private TigerEyeCamouflageTextureResolver() {
    }

    public static ResolvedTexture resolve(BlockState state, BlockPos pos) {
        int stateId = Block.getId(state);
        ResolvedTexture cached = CACHE.get(stateId);
        if (cached != null) {
            return cached;
        }

        ResolvedTexture resolved = resolveUncached(state, pos);
        CACHE.put(stateId, resolved);
        return resolved;
    }

    public static void clearCache() {
        Minecraft minecraft = Minecraft.getInstance();
        for (ResolvedTexture resolved : CACHE.values()) {
            if (!resolved.texture().equals(FALLBACK_TEXTURE)) {
                minecraft.getTextureManager().release(resolved.texture());
            }
        }
        CACHE.clear();
    }

    private static ResolvedTexture resolveUncached(BlockState state, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(QUAD_SEED), parts);
        List<BakedQuad> upwardQuads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            upwardQuads.addAll(part.getQuads(Direction.UP));
        }
        TextureAtlasSprite sprite = firstSprite(upwardQuads);
        int tintIndex = -1;
        if (sprite == null) {
            sprite = model.particleMaterial() == null ? null : model.particleMaterial().sprite();
        } else if (!upwardQuads.isEmpty()) {
            tintIndex = upwardQuads.getFirst().materialInfo().tintIndex();
        }

        if (sprite == null) {
            return new ResolvedTexture(FALLBACK_TEXTURE, 0xFFFFFFFF);
        }

        int tint = 0xFFFFFFFF;
        if (tintIndex >= 0) {
            BlockColors colors = minecraft.getBlockColors();
            BlockTintSource tintSource = colors.getTintSource(state, tintIndex);
            if (tintSource != null) {
                int color = minecraft.level != null ? tintSource.colorInWorld(state, minecraft.level, pos) : tintSource.color(state);
                if (color != -1) {
                    tint = 0xFF000000 | color;
                }
            }
        }

        Identifier tiled = buildTiledSkin(minecraft, sprite.contents().name());
        return new ResolvedTexture(tiled, tint);
    }

    private static Identifier buildTiledSkin(Minecraft minecraft, Identifier spriteId) {
        Identifier source = Identifier.fromNamespaceAndPath(
                spriteId.getNamespace(),
                "textures/" + spriteId.getPath() + ".png"
        );
        Identifier target = Identifier.fromNamespaceAndPath(
                spriteId.getNamespace(),
                "dynamic/tiger_eye_camouflage/" + spriteId.getPath()
        );

        try (InputStream stream = openStream(minecraft, source)) {
            if (stream == null) {
                return FALLBACK_TEXTURE;
            }

            try (NativeImage sourceImage = NativeImage.read(stream)) {
                int tileWidth = sourceImage.getWidth();
                int tileHeight = tileWidth <= sourceImage.getHeight() ? tileWidth : sourceImage.getHeight();

                NativeImage canvas = new NativeImage(SKIN_SIZE, SKIN_SIZE, false);
                for (int y = 0; y < SKIN_SIZE; y++) {
                    for (int x = 0; x < SKIN_SIZE; x++) {
                        canvas.setPixel(x, y, sourceImage.getPixel(x % tileWidth, y % tileHeight));
                    }
                }

                minecraft.getTextureManager().register(target, new DynamicTexture(target::toString, canvas));
                return target;
            }
        } catch (IOException e) {
            return FALLBACK_TEXTURE;
        }
    }

    private static InputStream openStream(Minecraft minecraft, Identifier location) {
        return minecraft.getResourceManager().getResource(location)
                .map(resource -> {
                    try {
                        return resource.open();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private static TextureAtlasSprite firstSprite(List<BakedQuad> quads) {
        if (quads == null || quads.isEmpty()) {
            return null;
        }
        for (BakedQuad quad : quads) {
            if (quad != null && quad.materialInfo() != null && quad.materialInfo().sprite() != null) {
                return quad.materialInfo().sprite();
            }
        }
        return null;
    }
}
