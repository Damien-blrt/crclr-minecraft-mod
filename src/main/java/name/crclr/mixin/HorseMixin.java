package name.crclr.mixin;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class HorseMixin {
    @Inject(method = "getSoundVolume", at = @At("HEAD"), cancellable = true)
    private void makeHorsesScreamLouder(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(4.5F);
    }

    @Inject(method = "getAmbientSoundInterval", at = @At("HEAD"), cancellable = true)
    private void makeHorsesScreamMoreOften(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(40);
    }
}
