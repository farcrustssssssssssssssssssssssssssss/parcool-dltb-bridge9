package dev.parcooldltb;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BridgeConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue COST_SCALE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("stamina");
        COST_SCALE = builder
                .comment(
                        "Multiplier applied to every ParCool stamina cost before it is taken from the DLTB bar.",
                        "ParCool's default max stamina is 2000, Stamina [DLTB]'s bar is 1000, so 0.5 keeps ParCool's",
                        "relative costs the same (a move that costs 1/4 of the ParCool bar costs 1/4 of the DLTB bar).",
                        "Raise it to make parkour more tiring, lower it to make it cheaper. 0 = ParCool moves are free."
                )
                .defineInRange("cost_scale", 0.5, 0.0, 100.0);
        builder.pop();
        SPEC = builder.build();
    }

    private BridgeConfig() {
    }
}
