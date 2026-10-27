package com.craisinlord.antarchy.content.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class AntarchyAdvancementTriggers {
    private static final EventInstance EMPTY = new EventInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    private static final Codec<EventInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootItemCondition.CODEC.optionalFieldOf("player").forGetter(EventInstance::player),
            Codec.STRING.optionalFieldOf("direction").forGetter(EventInstance::direction),
            Codec.STRING.optionalFieldOf("phase").forGetter(EventInstance::phase),
            Codec.STRING.optionalFieldOf("variant").forGetter(EventInstance::variant)
    ).apply(instance, EventInstance::new));
    private static final CrossedGravityThroat CROSSING = new CrossedGravityThroat();
    private static final HordePhaseChanged HORDE_PHASE = new HordePhaseChanged();
    private static final TamedGlimmer TAMED_GLIMMER = new TamedGlimmer();

    private AntarchyAdvancementTriggers() {
    }

    public static void register() {
        register((id, trigger) -> Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger));
    }

    public static void register(BiConsumer<Identifier, CriterionTrigger<?>> registrar) {
        registrar.accept(Identifier.fromNamespaceAndPath("antarchy", "crossed_gravity_throat"), CROSSING);
        registrar.accept(Identifier.fromNamespaceAndPath("antarchy", "horde_phase_changed"), HORDE_PHASE);
        registrar.accept(Identifier.fromNamespaceAndPath("antarchy", "tamed_glimmer"), TAMED_GLIMMER);
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

    private record EventInstance(Optional<Holder<LootItemCondition>> player, Optional<String> direction, Optional<String> phase, Optional<String> variant)
            implements SimpleCriterionTrigger.SimpleInstance {
    }

    private static final class CrossedGravityThroat extends SimpleCriterionTrigger<EventInstance> {
        private void fire(ServerPlayer player, String direction) {
            trigger(player, instance -> instance.direction().map(direction::equals).orElse(true));
        }

        @Override
        public Codec<EventInstance> codec() {
            return CODEC;
        }
    }

    private static final class HordePhaseChanged extends SimpleCriterionTrigger<EventInstance> {
        private void fire(ServerPlayer player, String phase) {
            trigger(player, instance -> instance.phase().map(phase::equals).orElse(true));
        }

        @Override
        public Codec<EventInstance> codec() {
            return CODEC;
        }
    }

    private static final class TamedGlimmer extends SimpleCriterionTrigger<EventInstance> {
        private void fire(ServerPlayer player, String variant) {
            trigger(player, instance -> instance.variant().map(variant::equals).orElse(true));
        }

        @Override
        public Codec<EventInstance> codec() {
            return CODEC;
        }
    }
}
