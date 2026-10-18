package dev.parcooldltb;

import com.alrex.parcool.api.Effects;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.common.capability.stamina.ParCoolStamina;
import net.minecraft.world.entity.player.Player;
import ru.xmeowwwly.ktstanima.client.ClientStaminaData;

/**
 * ParCool stamina backed by Stamina [DLTB], for the LOCAL player only.
 *
 * It extends ParCoolStamina on purpose: ParCool identifies the stamina "type" by class
 * (IStamina.Type.getFromInstance), and a subclass is recognised as the normal Default type, so ParCool's
 * network sync and HUD logic keep working unchanged.
 *
 * Units: ParCool thinks in its own units (default bar 2000). DLTB's bar is 1000. cost_scale converts
 * ParCool -> DLTB units (DLTB = ParCool * scale). Values reported back to ParCool are converted the other way,
 * so ParCool's own cost checks stay consistent.
 *
 *  - reading  = DLTB's synchronized client copy (ClientStaminaData)
 *  - spending = batched and sent to the server once per tick (ConsumePacket), where DLTB owns the real value
 *  - recovery = DLTB's own regeneration, so recover()/set() intentionally do nothing
 */
public class DltbStamina extends ParCoolStamina {
    private static final float DLTB_MAX = 1000f;

    private final Player player;
    /** Cost (DLTB units) collected this tick and not yet sent to the server. */
    private float pending = 0f;
    private int oldValue = 0;

    public DltbStamina(Player player) {
        super(); // no-arg constructor: it must not call our overridden methods before fields exist
        this.player = player;
    }

    private static float scale() {
        return (float) Math.max(0.01, BridgeConfig.COST_SCALE.get());
    }

    private int toParCoolUnits(float dltbValue) {
        return Math.round(dltbValue / scale());
    }

    @Override
    public int getActualMaxStamina() {
        return toParCoolUnits(DLTB_MAX);
    }

    @Override
    public int get() {
        return toParCoolUnits(Math.max(0f, ClientStaminaData.INSTANCE.getTargetStamina() - pending));
    }

    @Override
    public int getOldValue() {
        return oldValue;
    }

    @Override
    public void updateOldValue() {
        oldValue = get();
    }

    @Override
    public boolean isExhausted() {
        return ClientStaminaData.INSTANCE.isExhausted();
    }

    @Override
    public void consume(int value) {
        if (value <= 0 || isExhausted()) return;
        Parkourability parkourability = Parkourability.get(player);
        if (parkourability == null) return;
        if (parkourability.getActionInfo().isStaminaInfinite(player.isSpectator() || player.isCreative())
                || player.hasEffect(Effects.INEXHAUSTIBLE.get())) {
            return;
        }
        pending += (float) (value * BridgeConfig.COST_SCALE.get());
    }

    @Override
    public void recover(int value) {
        // DLTB regenerates stamina by itself (after a short delay following any spending).
    }

    @Override
    public void set(int value) {
        // The value is owned by DLTB on the server.
    }

    @Override
    public void setExhaustion(boolean value) {
        // Exhaustion is decided by DLTB.
    }

    @Override
    public boolean isImposingExhaustionPenalty() {
        // DLTB already blocks sprinting, mining and attacking while exhausted; avoid a second penalty on top.
        return false;
    }

    @Override
    public void tick() {
        if (pending > 0f) {
            BridgeNetwork.CHANNEL.sendToServer(new ConsumePacket(pending));
            pending = 0f;
        }
    }
}
