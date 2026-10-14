package com.craisinlord.antarchy.content.advancement;

import com.craisinlord.antarchy.mixins.CriteriaTriggersAccessor;
import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public final class AntarchyAdvancementTriggers {
    private static final CrossedGravityThroat CROSSING = new CrossedGravityThroat(new ResourceLocation("antarchy", "crossed_gravity_throat"));
    private static final HordePhaseChanged HORDE_PHASE = new HordePhaseChanged(new ResourceLocation("antarchy", "horde_phase_changed"));
    private static final TamedGlimmer TAMED_GLIMMER = new TamedGlimmer(new ResourceLocation("antarchy", "tamed_glimmer"));

    private AntarchyAdvancementTriggers() {
    }

    public static void register() {
        register((id, trigger) -> CriteriaTriggersAccessor.antarchy$register(trigger));
    }

    public static void register(BiConsumer<ResourceLocation, CriterionTrigger<?>> registrar) {
        registrar.accept(CROSSING.getId(), CROSSING);
        registrar.accept(HORDE_PHASE.getId(), HORDE_PHASE);
        registrar.accept(TAMED_GLIMMER.getId(), TAMED_GLIMMER);
    }

    public static void crossedGravityThroat(ServerPlayer player, String direction) {
        CROSSING.fire(player, direction);
    }

    public static void hordePhaseChanged(ServerPlayer player, String phase) {
        HORDE_PHASE.fire(player, phase);
    }

    public static void tamedGlimmer(ServerPlayer player, String variant) {
        TAMED_GLIMMER.fire(player, variant);
    }

    private static Optional<String> optionalString(JsonObject json, String key) {
        return json.has(key) ? Optional.of(GsonHelper.getAsString(json, key)) : Optional.empty();
    }

    private static final class EventInstance extends AbstractCriterionTriggerInstance {
        private final Optional<String> direction;
        private final Optional<String> phase;
        private final Optional<String> variant;

        private EventInstance(ResourceLocation id, ContextAwarePredicate player, Optional<String> direction, Optional<String> phase, Optional<String> variant) {
            super(id, player);
            this.direction = direction;
            this.phase = phase;
            this.variant = variant;
        }

        Optional<String> direction() {
            return this.direction;
        }

        Optional<String> phase() {
            return this.phase;
        }

        Optional<String> variant() {
            return this.variant;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            this.direction.ifPresent(value -> json.addProperty("direction", value));
            this.phase.ifPresent(value -> json.addProperty("phase", value));
            this.variant.ifPresent(value -> json.addProperty("variant", value));
            return json;
        }
    }

    private abstract static class EventTrigger extends SimpleCriterionTrigger<EventInstance> {
        private final ResourceLocation id;

        EventTrigger(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        protected EventInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
            return new EventInstance(this.id, player, optionalString(json, "direction"), optionalString(json, "phase"), optionalString(json, "variant"));
        }
    }

    private static final class CrossedGravityThroat extends EventTrigger {
        CrossedGravityThroat(ResourceLocation id) {
            super(id);
        }

        private void fire(ServerPlayer player, String direction) {
            trigger(player, instance -> instance.direction().map(direction::equals).orElse(true));
        }
    }

    private static final class HordePhaseChanged extends EventTrigger {
        HordePhaseChanged(ResourceLocation id) {
            super(id);
        }

        private void fire(ServerPlayer player, String phase) {
            trigger(player, instance -> instance.phase().map(phase::equals).orElse(true));
        }
    }

    private static final class TamedGlimmer extends EventTrigger {
        TamedGlimmer(ResourceLocation id) {
            super(id);
        }

        private void fire(ServerPlayer player, String variant) {
            trigger(player, instance -> instance.variant().map(variant::equals).orElse(true));
        }
    }
}
