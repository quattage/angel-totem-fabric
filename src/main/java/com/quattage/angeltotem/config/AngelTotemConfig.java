package com.quattage.angeltotem.config;


import blue.endless.jankson.Comment;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Config(name = "angeltotem")
public class AngelTotemConfig implements ConfigData {
   
    @Comment("Basic Options")
    @ConfigEntry.Gui.CollapsibleObject
    public BasicTotemOptions BasicTotemOptions = new BasicTotemOptions();
    public static class BasicTotemOptions {
        @Comment("Radius around the player's bed that they are allowed to use the ring in, measured in blocks. Set to 0 to disable.")
        public int bedFlightRadius = 50;

        @Comment("Toggle the requirement for an active respawn bed to use the totem.")
        public boolean doBedCheck = true;
        
        @Comment("Enable Hard Mode, which replaces beds as a binding target with a beacon")
        public boolean hardMode = false;

        @Comment("Enable falling relief, which prevents the user from dying if they lose their totem mid-air.")
        public boolean useFallingRelief = false;

        @Comment("Toggle trinket registration for the totem. Useful if you have trinkets installed, but don't want to use the totem as a trinket.")
        public boolean useTrinkets = true;
    }

    @Comment("Advaned Options")
    @ConfigEntry.Gui.CollapsibleObject
    public AdvancedTotemOptions AdvancedTotemOptions = new AdvancedTotemOptions();
    public static class AdvancedTotemOptions {
        @Comment("Toggle the totem's craftability. Useful for modpack creators who want to override this functionality.")
        public boolean totemCraftable = true;

        @Comment("The width of the actionbar's distance indicator while the player is holding the totem. Set to 0 to disable. Minimum value is 15.")
        public int indicatorWidth = 30;
    }

    //prepare a new configHolder instance and register it. also allows for config reloading with vanilla /reload command
    public static ConfigHolder<AngelTotemConfig> initializeConfigs() {
        ConfigHolder<AngelTotemConfig> configHolder = AutoConfig.register(AngelTotemConfig.class, JanksonConfigSerializer::new);
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) -> 
            AutoConfig.getConfigHolder(AngelTotemConfig.class).load()
        );
        return configHolder;
    }
}
