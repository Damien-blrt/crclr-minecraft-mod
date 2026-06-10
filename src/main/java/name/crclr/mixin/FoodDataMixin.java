package name.crclr.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.crclr.data.SharedHungerManager;

/**
 * This mixin is intentionally minimal to avoid breaking natural hunger
 * mechanics.
 * Most synchronization is handled by ServerPlayerMixin which detects changes
 * and syncs them.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    /**
     * Allow natural Minecraft hunger mechanics to work
     * Synchronization happens in ServerPlayerMixin via tick detection
     */
}
