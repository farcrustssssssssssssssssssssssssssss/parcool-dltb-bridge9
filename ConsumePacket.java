package dev.parcooldltb;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import ru.xmeowwwly.ktstanima.api.StaminaAPI;

import java.util.function.Supplier;

/**
 * Client -> server: "take this much stamina (already in DLTB units) from my DLTB bar".
 * Stamina [DLTB] keeps the real value on the server, so spending must happen there.
 */
public record ConsumePacket(float amount) {
    /** Upper bound per packet. ParCool sends one batched packet per tick, so real values are tiny. */
    private static final float MAX_PER_PACKET = 1000f;

    public static void encode(ConsumePacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.amount);
    }

    public static ConsumePacket decode(FriendlyByteBuf buf) {
        return new ConsumePacket(buf.readFloat());
    }

    public static void handle(ConsumePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            float amount = packet.amount;
            if (!Float.isFinite(amount) || amount <= 0f) return;
            if (player.isCreative() || player.isSpectator()) return;
            amount = Math.min(amount, MAX_PER_PACKET);

            // StaminaAPI.consumeStamina() refuses to spend anything when the bar holds LESS than the
            // requested amount, which would let a long parkour move run on the last few points forever.
            // Clamp to what is left so the bar can actually reach 0 and trigger DLTB's exhaustion.
            float current = StaminaAPI.INSTANCE.getStamina(player);
            if (current <= 0f) return;
            StaminaAPI.INSTANCE.consumeStamina(player, Math.min(amount, current));
        });
        context.setPacketHandled(true);
    }
}
