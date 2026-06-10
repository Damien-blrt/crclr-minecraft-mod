package name.crclr.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;

public class SharedHealthManager {
    private static final Logger LOGGER = name.crclr.Crclr.LOGGER;
    private static boolean processingHeal = false;

    public static void syncHeal(ServerPlayer healedPlayer, float amountHealed) {
        if (processingHeal)
            return;
        if (healedPlayer.getHealth() >= healedPlayer.getMaxHealth() || amountHealed <= 0)
            return;

        processingHeal = true;
        float newHealth = SharedHealthDatas.sharedHealth + amountHealed;
        SharedHealthDatas.sharedHealth = Math.min(healedPlayer.getMaxHealth(), newHealth);

        MinecraftServer server = healedPlayer.level().getServer();
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.setHealth(SharedHealthDatas.sharedHealth);
                LOGGER.info("Shared health updated via healing: {} Player: {}",
                        SharedHealthDatas.sharedHealth,
                        p.getName().getString());
            }
        }

        processingHeal = false;
    }
}