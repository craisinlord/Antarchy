package com.craisinlord.antarchy.content.antmail;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AntmailEventData extends SimplePreparableReloadListener<Map<ResourceLocation, AntmailEventData.Definition>> {
    private static final AntmailEventData INSTANCE = new AntmailEventData();
    private static volatile Map<ResourceLocation, Definition> definitions = Map.of();
    private AntmailEventData() {
    }

    public static AntmailEventData instance() {
        return INSTANCE;
    }

    public static void onTick(MinecraftServer server) {
        long dayTime = server.overworld().getDayTime();
        long day = dayTime / 24000L;
        if (dayTime % 24000L >= 20L) return;
        AntmailServerData data = AntmailServerData.access(server);
        if (!data.beginRandomMailDay(day)) return;
        for (AntmailAddress address : data.registeredAddresses()) {
            for (Map.Entry<ResourceLocation, Definition> entry : definitions.entrySet()) {
                Definition definition = entry.getValue();
                if (definition.trigger().type().equals("random") && server.overworld().random.nextDouble() < definition.trigger().chance()) deliver(server, address, entry.getKey(), definition, false);
            }
        }
    }

    public static void onAdvancement(ServerPlayer player, AdvancementHolder advancement) {
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress address = data.lastUsedAddress(player);
        if (address == null) return;
        for (Map.Entry<ResourceLocation, Definition> entry : definitions.entrySet()) {
            Trigger trigger = entry.getValue().trigger();
            if (trigger.type().equals("advancement") && trigger.value().equals(advancement.id().toString())) deliver(player.server, address, entry.getKey(), entry.getValue(), true);
        }
    }

    private static void deliver(MinecraftServer server, AntmailAddress recipient, ResourceLocation id, Definition definition, boolean permanentClaim) {
        AntmailServerData data = AntmailServerData.access(server);
        try {
            AntmailAddress sender = AntmailAddress.parse(definition.sender());
            AntmailMessage message = AntmailMessage.create(sender, recipient, definition.subject(), definition.body(), server.overworld().getGameTime(), List.of());
            AntmailDeliveryResult result = data.deliver(server, message);
            if (permanentClaim && result.status() != AntmailDeliveryResult.Status.MAILBOX_FULL
                    && result.status() != AntmailDeliveryResult.Status.FAILED
                    && result.status() != AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND) {
                data.claimTrigger(recipient, id);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    protected Map<ResourceLocation, Definition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Definition> loaded = new HashMap<>();
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> resource : manager.listResources("antmail/message", path -> path.getPath().endsWith(".json")).entrySet()) {
            try {
                JsonElement element = JsonParser.parseReader(resource.getValue().openAsReader());
                if (!element.isJsonObject()) continue;
                var object = element.getAsJsonObject();
                var trigger = object.getAsJsonObject("trigger");
                String type = trigger.get("type").getAsString();
                String value = trigger.has("value") ? trigger.get("value").getAsString() : String.valueOf(trigger.get("day").getAsInt());
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(resource.getKey().getNamespace(), resource.getKey().getPath().substring("antmail/message/".length(), resource.getKey().getPath().length() - 5));
                loaded.put(id, new Definition(object.get("sender").getAsString(), object.get("subject").getAsString(), object.get("body").getAsString(), new Trigger(type, value)));
            } catch (Exception ignored) {
            }
        }
        return Map.copyOf(loaded);
    }

    @Override
    protected void apply(Map<ResourceLocation, Definition> loaded, ResourceManager manager, ProfilerFiller profiler) {
        definitions = loaded;
    }

    public record Definition(String sender, String subject, String body, Trigger trigger) {
    }

    public record Trigger(String type, String value) {
        double chance() {
            try {
                return Math.max(0.0D, Math.min(1.0D, Double.parseDouble(value)));
            } catch (NumberFormatException ignored) {
                return 0.0D;
            }
        }
    }
}
