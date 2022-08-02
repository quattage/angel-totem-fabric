package com.quattage.angeltotem.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import blue.endless.jankson.Comment;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Config(name = "angeltotem")
public class AngelTotemConfig implements ConfigData {
   
    @Comment("Angel Totem Options")
    @ConfigEntry.Gui.CollapsibleObject

    public AngelTotemOptions AngelTotemOptions = new AngelTotemOptions();
    public static class AngelTotemOptions {
        @Comment("Radius around the player's bed that they are allowed to use the ring in, measured in blocks. Set to 0 to disable.")
        public int bedFlightRadius = 100;

        @Comment("Toggle the requirement for an active respawn bed to use the totem.")
        public boolean doBedCheck = true;
        
        @Comment("Enable 'relief mode', which prevents the user from dying if they lose their totem mid-air.")
        public boolean reliefMode = false;

        @Comment("Toggle the totem's craftability. Useful for modpack creators who want to override this functionality.")
        public boolean totemCraftable = true;

        @Comment("The width of the actionbar's distance indicator while the player is holding the totem. Set to 0 to disable. Minimum value is 15.")
        public int indicatorWidth = 30;
    }
    //prepare a new configHolder instance and register it. also allows for config reloading with vanilla /reload command
    public static ConfigHolder<AngelTotemConfig> init() {
        ConfigHolder<AngelTotemConfig> configHolder = AutoConfig.register(AngelTotemConfig.class, JanksonConfigSerializer::new);
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) -> 
            AutoConfig.getConfigHolder(AngelTotemConfig.class).load()
        );
        return configHolder;
    }
    //now that that's done, we can bring the configs to ModMenu
    @Environment(EnvType.CLIENT)
    public static class ModMenuIntegration implements ModMenuApi {
        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return screen -> AutoConfig.getConfigScreen(AngelTotemConfig.class, screen).get();
        }
    }
}
