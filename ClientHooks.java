package dev.parcooldltb;

import com.alrex.parcool.api.client.gui.ParCoolHUDEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Client-side hooks. Registered on the Forge event bus only when running on a client. */
public class ClientHooks {
    private static final ResourceLocation STAMINA_KEY = new ResourceLocation(ParCoolDltbBridge.MOD_ID, "stamina");

    /**
     * ParCool attaches its own StaminaProvider in the same event at NORMAL priority. Forge answers a capability
     * query with the FIRST attached provider that has it, so attaching ours at HIGHEST makes ParCool (and everything
     * else calling IStamina.get(player)) receive DltbStamina for the local player.
     * Other players keep ParCool's own OtherStamina (it only mirrors values synced over the network).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player player)) return;
        if (!player.isLocalPlayer()) return;
        event.addCapability(STAMINA_KEY, new DltbStaminaProvider(player));
    }

    /** Stamina [DLTB] draws its own bar, so hide ParCool's. */
    @SubscribeEvent
    public void onParCoolHud(ParCoolHUDEvent.RenderEvent event) {
        event.setCanceled(true);
    }
}
