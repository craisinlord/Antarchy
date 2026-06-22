package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.neoforge.content.fluid.AntiwaterFluidType;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class AntarchyNeoForgeFluidTypes {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Antarchy.MODID);

    public static final DeferredHolder<FluidType, FluidType> ANTIWATER_TYPE = FLUID_TYPES.register("antiwater",
            () -> new AntiwaterFluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.antiwater")
                    .fallDistanceModifier(0.0F)
                    .canSwim(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1000)
                    .viscosity(1000)));

    public static final DeferredHolder<FluidType, FluidType> ICHOR_TYPE = FLUID_TYPES.register("ichor",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.ichor")
                    .fallDistanceModifier(0.0F)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1200)
                    .viscosity(1400)));

    private AntarchyNeoForgeFluidTypes() {
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}
