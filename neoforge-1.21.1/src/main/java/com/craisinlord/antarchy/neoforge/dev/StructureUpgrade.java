package com.craisinlord.antarchy.neoforge.dev;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only structure template upgrader. Does nothing unless the JVM has
 * {@code -Dantarchy.upgradestructures=<namespace>}.
 *
 * <p>On server start it lists every {@code structure/*.nbt} in that namespace, loads each one through the
 * structure manager (which runs DataFixerUpper on templates saved by an older version), saves it back with
 * the current DataVersion and writes it to {@code -Dantarchy.upgradestructures.out=<dir>} (default
 * {@code <gamedir>/upgraded}) at {@code <namespace>/structure/<path>.nbt}. Only templates below the current
 * DataVersion are written; every template's load time is logged either way, so a second boot with the
 * upgraded copies in a datapack shows whether the datafixer still runs. Then it halts the server.
 *
 * <p>Used by the Integrated Boogaloo pack to ship pre-upgraded copies of old mod templates in its datapack.
 */
public final class StructureUpgrade {
    public static final String PROPERTY = "antarchy.upgradestructures";
    private static final Logger LOG = LoggerFactory.getLogger("AntarchyStructureUpgrade");

    private StructureUpgrade() {}

    public static void initIfEnabled() {
        String ns = System.getProperty(PROPERTY);
        if (ns == null || ns.isBlank()) {
            return;
        }
        LOG.warn("Structure upgrader enabled for namespace {}", ns);
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> run(e.getServer(), ns.trim()));
    }

    private static void run(MinecraftServer server, String ns) {
        int current = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        Path out = Path.of(System.getProperty(PROPERTY + ".out", server.getServerDirectory().resolve("upgraded").toString()));
        Map<ResourceLocation, Resource> found = new TreeMap<>(server.getResourceManager().listResources("structure",
                rl -> rl.getNamespace().equals(ns) && rl.getPath().endsWith(".nbt")));
        LOG.warn("Found {} templates in {} (current DataVersion {}), output {}", found.size(), ns, current, out);
        int written = 0, failed = 0, upToDate = 0;
        long totalMs = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            ResourceLocation file = entry.getKey();
            String path = file.getPath().substring("structure/".length(), file.getPath().length() - ".nbt".length());
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ns, path);
            try {
                int oldVersion;
                try (InputStream in = entry.getValue().open()) {
                    CompoundTag raw = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                    oldVersion = raw.contains("DataVersion", 99) ? raw.getInt("DataVersion") : 500;
                }
                long t0 = System.nanoTime();
                Optional<StructureTemplate> template = server.getStructureManager().get(id);
                long ms = (System.nanoTime() - t0) / 1_000_000;
                totalMs += ms;
                if (template.isEmpty()) {
                    failed++;
                    LOG.error("FAILED {} (from {}): structure manager returned nothing", id, entry.getValue().sourcePackId());
                    continue;
                }
                if (oldVersion >= current) {
                    upToDate++;
                    LOG.warn("UPTODATE {} DataVersion {} from {} loaded in {} ms", id, oldVersion,
                            entry.getValue().sourcePackId(), ms);
                    continue;
                }
                CompoundTag saved = template.get().save(new CompoundTag());
                Path dest = out.resolve(ns).resolve("structure").resolve(path + ".nbt");
                Files.createDirectories(dest.getParent());
                NbtIo.writeCompressed(saved, dest);
                written++;
                LOG.warn("UPGRADED {} DataVersion {} -> {} from {} loaded in {} ms, wrote {} bytes", id, oldVersion,
                        saved.getInt("DataVersion"), entry.getValue().sourcePackId(), ms, Files.size(dest));
            } catch (Throwable t) {
                failed++;
                LOG.error("FAILED {}", id, t);
            }
        }
        LOG.warn("Structure upgrade done: {} written, {} already current, {} failed, {} ms total load time",
                written, upToDate, failed, totalMs);
        server.halt(false);
    }
}
