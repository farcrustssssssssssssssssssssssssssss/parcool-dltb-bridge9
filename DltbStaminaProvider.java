package dev.parcooldltb;

import com.alrex.parcool.common.capability.IStamina;
import com.alrex.parcool.common.capability.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Serves ParCool's IStamina capability for the local player with the DLTB-backed implementation. */
public class DltbStaminaProvider implements ICapabilityProvider {
    private final LazyOptional<IStamina> optional;

    public DltbStaminaProvider(Player player) {
        IStamina instance = new DltbStamina(player);
        this.optional = LazyOptional.of(() -> instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.STAMINA_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}
