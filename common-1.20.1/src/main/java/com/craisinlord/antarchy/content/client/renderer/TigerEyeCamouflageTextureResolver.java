package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TigerEyeCamouflageTextureResolver {
    public record ResolvedTexture(ResourceLocation texture, int argbTint) {}

    // PlayerModel's skin layout is always laid out on a 64x64 canvas, regardless of the
    // block texture's actual size, so the generated overlay texture must match that grid
    // for every body part to sample the block texture at the same pixel density.
    private static final int SKIN_SIZE = 64;
    private static final Map<Integer, ResolvedTexture> CACHE = new ConcurrentHashMap<>();
    private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("textures/block/stone.png");
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
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        var model = dispatcher.getBlockModel(state);
        List<BakedQuad> upwardQuads = model.getQuads(state, Direction.UP, RandomSource.create(QUAD_SEED));
        TextureAtlasSprite sprite = firstSprite(upwardQuads);
        int tintIndex = -1;
        if (sprite == null) {
            sprite = model.getParticleIcon();
        } else if (!upwardQuads.isEmpty()) {
            tintIndex = upwardQuads.get(0).getTintIndex();
        }

        if (sprite == null) {
            return new ResolvedTexture(FALLBACK_TEXTURE, 0xFFFFFFFF);
        }

        int tint = 0xFFFFFFFF;
        if (tintIndex >= 0) {
            BlockColors colors = minecraft.getBlockColors();
            int color = colors.getColor(state, minecraft.level, pos, tintIndex);
            if (color != -1) {
                tint = 0xFF000000 | color;
            }
        }

        ResourceLocation tiled = buildTiledSkin(minecraft, sprite.contents().name());
        return new ResolvedTexture(tiled, tint);
    }

    private static ResourceLocation buildTiledSkin(Minecraft minecraft, ResourceLocation spriteId) {
        ResourceLocation source = new ResourceLocation(
                spriteId.getNamespace(),
                "textures/" + spriteId.getPath() + ".png"
        );
        ResourceLocation target = new ResourceLocation(
                spriteId.getNamespace(),
                "dynamic/tiger_eye_camouflage/" + spriteId.getPath()
        );

        try (InputStream stream = openStream(minecraft, source)) {
            if (stream == null) {
                return FALLBACK_TEXTURE;
            }

            try (NativeImage sourceImage = NativeImage.read(stream)) {
                int tileWidth = sourceImage.getWidth();
                // Animated block textures stack frames vertically below a square first frame.
                int tileHeight = tileWidth <= sourceImage.getHeight() ? tileWidth : sourceImage.getHeight();

                NativeImage canvas = new NativeImage(SKIN_SIZE, SKIN_SIZE, false);
                for (int y = 0; y < SKIN_SIZE; y++) {
                    for (int x = 0; x < SKIN_SIZE; x++) {
                        canvas.setPixelRGBA(x, y, sourceImage.getPixelRGBA(x % tileWidth, y % tileHeight));
                    }
                }

                minecraft.getTextureManager().register(target, new DynamicTexture(canvas));
                return target;
            }
        } catch (IOException e) {
            return FALLBACK_TEXTURE;
        }
    }

    private static InputStream openStream(Minecraft minecraft, ResourceLocation location) {
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
            if (quad != null && quad.getSprite() != null) {
                return quad.getSprite();
            }
        }
        return null;
    }
}
