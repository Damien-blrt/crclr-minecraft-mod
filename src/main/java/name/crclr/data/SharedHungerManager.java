package name.crclr.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class SharedHungerManager {
    private static final Logger LOGGER = name.crclr.Crclr.LOGGER;
    private static boolean processingHunger = false;

    /**
     * Sync hunger changes to all players
     */
    public static void syncHunger(ServerPlayer player, int foodLevel, float saturation) {
        if (processingHunger)
            return;

        processingHunger = true;

        // Update shared values
        SharedHungerDatas.sharedFoodLevel = Math.max(0, Math.min(20, foodLevel));
        SharedHungerDatas.sharedSaturation = Math.max(0, saturation);

        MinecraftServer server = player.level().getServer();
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (p.getFoodData().getFoodLevel() != SharedHungerDatas.sharedFoodLevel ||
                        Math.abs(p.getFoodData().getSaturationLevel() - SharedHungerDatas.sharedSaturation) > 0.01f) {
                    p.getFoodData().setFoodLevel(SharedHungerDatas.sharedFoodLevel);
                    p.getFoodData().setSaturation(SharedHungerDatas.sharedSaturation);
                }
                LOGGER.debug("Shared hunger updated: Food={}, Saturation={} Player: {}",
                        SharedHungerDatas.sharedFoodLevel,
                        SharedHungerDatas.sharedSaturation,
                        p.getName().getString());
            }
        }

        processingHunger = false;
    }

    /**
     * Add food to the shared hunger pool
     */
    public static void addFood(ServerPlayer player, int foodAmount, float saturationAmount) {
        if (processingHunger)
            return;

        processingHunger = true;
        int newFoodLevel = Math.min(20, SharedHungerDatas.sharedFoodLevel + foodAmount);
        float newSaturation = Math.min(newFoodLevel, SharedHungerDatas.sharedSaturation + saturationAmount);

        SharedHungerDatas.sharedFoodLevel = newFoodLevel;
        SharedHungerDatas.sharedSaturation = newSaturation;

        MinecraftServer server = player.level().getServer();
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.getFoodData().setFoodLevel(SharedHungerDatas.sharedFoodLevel);
                p.getFoodData().setSaturation(SharedHungerDatas.sharedSaturation);
                LOGGER.debug("Shared hunger increased via food: Food={}, Saturation={} Player: {}",
                        SharedHungerDatas.sharedFoodLevel,
                        SharedHungerDatas.sharedSaturation,
                        p.getName().getString());
            }
        }

        processingHunger = false;
    }

    /**
     * Exhaust the shared hunger
     */
    public static void exhaust(ServerPlayer player, float exhaustionAmount) {
        if (processingHunger)
            return;

        processingHunger = true;
        SharedHungerDatas.sharedExhaustion += exhaustionAmount;

        // Saturation is depleted first
        if (SharedHungerDatas.sharedExhaustion > SharedHungerDatas.sharedSaturation) {
            SharedHungerDatas.sharedExhaustion -= SharedHungerDatas.sharedSaturation;
            SharedHungerDatas.sharedSaturation = 0;

            // Then food level
            int foodDecrease = (int) SharedHungerDatas.sharedExhaustion;
            SharedHungerDatas.sharedFoodLevel = Math.max(0, SharedHungerDatas.sharedFoodLevel - foodDecrease);
            SharedHungerDatas.sharedExhaustion = 0;
        } else {
            SharedHungerDatas.sharedSaturation -= SharedHungerDatas.sharedExhaustion;
            SharedHungerDatas.sharedExhaustion = 0;
        }

        MinecraftServer server = player.level().getServer();
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.getFoodData().setFoodLevel(SharedHungerDatas.sharedFoodLevel);
                p.getFoodData().setSaturation(SharedHungerDatas.sharedSaturation);
            }
        }

        processingHunger = false;
    }

    /**
     * Check if we're currently processing hunger (to avoid recursive syncs)
     */
    public static boolean isProcessing() {
        return processingHunger;
    }
}
