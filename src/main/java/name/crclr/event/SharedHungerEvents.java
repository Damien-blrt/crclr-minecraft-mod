package name.crclr.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.slf4j.Logger;
import name.crclr.data.SharedHungerDatas;
import name.crclr.data.SharedHungerManager;
import name.crclr.data.SharedHealthDatas;

public class SharedHungerEvents {
    static Logger logger = name.crclr.Crclr.LOGGER;
    private static int lastFoodLevel = 20;
    private static float lastSaturation = 5.0f;
    private static long lastRegenTick = 0;
    private static final long REGEN_INTERVAL = 80; // ticks between regeneration checks

    public static void register() {
        // Handle hunger exhaustion (from actions like sprinting, jumping, etc.)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player))
                return true;

            // If player would take damage from starvation, sync it
            if (amount <= 0.5f) {
                // This is likely starvation damage
                int newFoodLevel = Math.max(0, SharedHungerDatas.sharedFoodLevel - 1);
                SharedHungerManager.syncHunger(player, newFoodLevel, SharedHungerDatas.sharedSaturation);
            }

            return true;
        });

        logger.info("Shared Hunger Events registered!");
    }

    /**
     * Call this from a server tick event to handle natural regeneration
     * This should be called once per server tick
     */
    public static void handleRegeneration(ServerPlayer anyPlayer) {
        long currentTick = anyPlayer.tickCount;

        // Check regeneration every REGEN_INTERVAL ticks (once per 4 seconds)
        if (currentTick - lastRegenTick < REGEN_INTERVAL)
            return;

        lastRegenTick = currentTick;

        MinecraftServer server = anyPlayer.level().getServer();
        if (server == null)
            return;

        // Only the first player processes regeneration to avoid duplicates
        ServerPlayer[] players = server.getPlayerList().getPlayers().toArray(new ServerPlayer[0]);
        if (players.length == 0)
            return;

        // Natural regeneration: if food >= 17 and health < max, regenerate health
        if (SharedHungerDatas.sharedFoodLevel >= 17 && SharedHealthDatas.sharedHealth < 20.0f) {
            if (SharedHungerDatas.sharedSaturation > 0) {
                // Regenerate health
                float newHealth = Math.min(20.0f, SharedHealthDatas.sharedHealth + 0.5f);
                SharedHealthDatas.sharedHealth = newHealth;

                // Exhaust hunger
                SharedHungerManager.exhaust(anyPlayer, 3.0f);

                for (ServerPlayer player : players) {
                    player.setHealth(SharedHealthDatas.sharedHealth);
                    player.getFoodData().setFoodLevel(SharedHungerDatas.sharedFoodLevel);
                    player.getFoodData().setSaturation(SharedHungerDatas.sharedSaturation);
                }

                logger.info("Shared regeneration triggered: Health={}, Food={}, Saturation={}",
                        SharedHealthDatas.sharedHealth,
                        SharedHungerDatas.sharedFoodLevel,
                        SharedHungerDatas.sharedSaturation);
            }
        }
    }
}
