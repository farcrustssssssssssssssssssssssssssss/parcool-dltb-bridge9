package dev.parcooldltb;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Bridge for ParCool 3.4.x: ParCool uses the stamina bar of Stamina [DLTB] instead of its own.
 *
 * ParCool 3.x has no API for foreign stamina types, but it reads the player's stamina through a Forge
 * capability. On the client we attach our own provider of that capability BEFORE ParCool does
 * (see ClientHooks), so ParCool talks to {@link DltbStamina} without knowing it.
 */
@Mod(ParCoolDltbBridge.MOD_ID)
public class ParCoolDltbBridge {
    public static final String MOD_ID = "parcooldltb";

    public ParCoolDltbBridge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BridgeConfig.SPEC);
        BridgeNetwork.register();
        // Everything that touches the local player / HUD is client-only.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientHooks()));
    }
}
