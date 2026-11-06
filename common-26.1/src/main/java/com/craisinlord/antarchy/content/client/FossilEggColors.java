package com.craisinlord.antarchy.content.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.SpawnEggItem;

public final class FossilEggColors {
    private static final int[] UNKNOWN = new int[0];
    private static final int ITERATIONS = 12;
    private static final Map<Identifier, int[]> CACHE = new ConcurrentHashMap<>();

    private FossilEggColors() {
    }

    public static Optional<Integer> color(SpawnEggItem egg, int tintIndex) {
        Identifier item = BuiltInRegistries.ITEM.getKey(egg);
        int[] colors = CACHE.computeIfAbsent(item, FossilEggColors::sample);
        return colors == UNKNOWN ? Optional.empty() : Optional.of(colors[tintIndex]);
    }

    private static int[] sample(Identifier item) {
        Identifier texture = item.withPath(path -> "textures/item/" + path + ".png");
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return UNKNOWN;
        }
        try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
            return dominantColors(image);
        } catch (IOException | RuntimeException exception) {
            return UNKNOWN;
        }
    }

    private static int[] dominantColors(NativeImage image) {
        List<int[]> pixels = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getPixel(x, y);
                if (ARGB.alpha(pixel) > 127) {
                    pixels.add(new int[]{ARGB.red(pixel), ARGB.green(pixel), ARGB.blue(pixel)});
                }
            }
        }
        if (pixels.isEmpty()) {
            return UNKNOWN;
        }

        double[] brightest = toDouble(pixels.stream().max(Comparator.comparingInt(FossilEggColors::brightness)).orElseThrow());
        double[] first = brightest;
        double[] second = toDouble(pixels.stream().max(Comparator.comparingDouble(pixel -> distance(pixel, brightest))).orElseThrow());
        List<int[]> firstGroup = new ArrayList<>();
        List<int[]> secondGroup = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            firstGroup.clear();
            secondGroup.clear();
            for (int[] pixel : pixels) {
                (distance(pixel, first) <= distance(pixel, second) ? firstGroup : secondGroup).add(pixel);
            }
            if (!firstGroup.isEmpty()) {
                first = average(firstGroup);
            }
            if (!secondGroup.isEmpty()) {
                second = average(secondGroup);
            }
        }

        if (secondGroup.isEmpty()) {
            int color = litColor(firstGroup);
            return new int[]{color, color};
        }
        return firstGroup.size() >= secondGroup.size()
                ? new int[]{litColor(firstGroup), litColor(secondGroup)}
                : new int[]{litColor(secondGroup), litColor(firstGroup)};
    }

    private static int litColor(List<int[]> group) {
        List<int[]> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparingInt(FossilEggColors::brightness).reversed());
        double[] color = average(sorted.subList(0, Math.max(1, sorted.size() / 2)));
        return ARGB.color(255, (int) Math.round(color[0]), (int) Math.round(color[1]), (int) Math.round(color[2]));
    }

    private static int brightness(int[] pixel) {
        return pixel[0] + pixel[1] + pixel[2];
    }

    private static double[] toDouble(int[] pixel) {
        return Arrays.stream(pixel).asDoubleStream().toArray();
    }

    private static double distance(int[] pixel, double[] center) {
        double red = pixel[0] - center[0];
        double green = pixel[1] - center[1];
        double blue = pixel[2] - center[2];
        return red * red + green * green + blue * blue;
    }

    private static double[] average(List<int[]> pixels) {
        double[] sum = new double[3];
        for (int[] pixel : pixels) {
            for (int channel = 0; channel < 3; channel++) {
                sum[channel] += pixel[channel];
            }
        }
        for (int channel = 0; channel < 3; channel++) {
            sum[channel] /= pixels.size();
        }
        return sum;
    }
}
