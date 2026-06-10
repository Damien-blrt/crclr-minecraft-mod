package name.crclr.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.crclr.data.SharedHungerManager;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    private int lastTrackedFood = 20;
    private float lastTrackedSaturation = 5.0f;

    /**
     * Detect hunger changes and sync to all players
     * Runs at the END of the tick to capture all natural depletion
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        // Skip if we're already processing to avoid infinite loops
        if (SharedHungerManager.isProcessing()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;

        int currentFood = player.getFoodData().getFoodLevel();
        float currentSaturation = player.getFoodData().getSaturationLevel();

        // Check if food or saturation has changed
        if (currentFood != lastTrackedFood || currentSaturation != lastTrackedSaturation) {
            // Update shared values and sync to all players
            SharedHungerManager.syncHunger(player, currentFood, currentSaturation);

            // Update our tracking
            lastTrackedFood = currentFood;
            lastTrackedSaturation = currentSaturation;
        }
    }
}
