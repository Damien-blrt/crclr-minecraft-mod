package name.crclr.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.crclr.data.SharedHealthManager;
import name.crclr.data.SharedHungerManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "heal", at = @At("HEAD"))
    private void onPlayerHeal(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof ServerPlayer player) {
            SharedHealthManager.syncHeal(player, amount);
        }
    }
}
