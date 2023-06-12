package io.github.randommcsomethin.animalhappiness;

import io.github.randommcsomethin.animalhappiness.config.AnimalHappinessConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class AnimalHappiness implements ModInitializer {
    public static final String MODID = "animalhappiness";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Predicate<MobEntity> IS_ADULT;

    public static AnimalHappinessConfig config;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");
        config = AutoConfig.register(AnimalHappinessConfig.class, GsonConfigSerializer::new).getConfig();
    }

    static {
        IS_ADULT = new Predicate<MobEntity>() {
            public boolean test(@Nullable MobEntity mobEntity) {
                return mobEntity != null && !mobEntity.isBaby();
            }
        };
    }
}
