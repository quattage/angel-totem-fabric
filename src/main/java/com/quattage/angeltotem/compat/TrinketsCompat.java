package com.quattage.angeltotem.compat;
import com.quattage.angeltotem.AngelTotem;

import dev.emi.trinkets.api.TrinketsApi;


public class TrinketsCompat {
    public static void initializeTrinketTotem() {
        TrinketsApi.registerTrinket(AngelTotem.ANGEL_TOTEM, new TrinketTotem());
    }
}
