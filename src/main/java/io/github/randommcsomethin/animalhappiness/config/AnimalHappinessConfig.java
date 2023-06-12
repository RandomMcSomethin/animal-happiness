package io.github.randommcsomethin.animalhappiness.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@Config(name = "animalhappiness")
public class AnimalHappinessConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Category("animalhappiness.general")
    public transient static AnimalHappinessConfig instance;

    /*
    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Category("animalhappiness.general")
    public String[] defaultAffectedAnimals = {
            "minecraft:cow",
            "minecraft:chicken",
            "minecraft:pig",
            "minecraft:sheep"
    };

    @ConfigEntry.Category("animalhappiness.general")
    @ConfigEntry.Gui.RequiresRestart
    @ConfigEntry.Gui.Tooltip
    public List<String> affectedAnimals = Arrays.asList(defaultAffectedAnimals);
    */

    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Category("animalhappiness.general")
    public AnimalEntry[] defaultAnimalEntries = {
            new AnimalEntry("minecraft:cow", new String[] {
                    "animalhappiness:space:8"
            }),
            new AnimalEntry("minecraft:chicken", new String[] {
                    "animalhappiness:space:8"
            }),
            new AnimalEntry("minecraft:pig", new String[] {
                    "animalhappiness:space:8"
            }),
            new AnimalEntry("minecraft:sheep", new String[] {
                    "animalhappiness:space:8"
            })
    };

    @ConfigEntry.Category("animalhappiness.general")
    @ConfigEntry.Gui.RequiresRestart
    @ConfigEntry.Gui.Tooltip
    public List<AnimalEntry> animalEntries = Arrays.asList(defaultAnimalEntries);
}
