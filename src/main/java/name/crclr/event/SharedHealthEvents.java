package name.crclr.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import name.crclr.data.SharedHealthDatas;

public class SharedHealthEvents {
    static Logger logger = name.crclr.Crclr.LOGGER;
    private static boolean processingDeath = false;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayer player))
                return;

            float newHealth = SharedHealthDatas.sharedHealth - damageTaken;
            SharedHealthDatas.sharedHealth = Math.max(0, newHealth);

            MinecraftServer server = player.level().getServer();
            if (server == null) return;

            for (ServerPlayer players : server.getPlayerList().getPlayers()) {
                players.setHealth(SharedHealthDatas.sharedHealth);
                logger.info("Shared health updated: {} Player: {}",
                        SharedHealthDatas.sharedHealth,
                        players.getName().getString());
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }
            if (processingDeath == true)
                return;
            processingDeath = true;
            MinecraftServer server = player.level().getServer();
            if (server == null) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.kill(p.level());
            }
            SharedHealthDatas.sharedHealth = 20.0F;
            processingDeath = false;
        });
        
    }
}