package name.crclr.module.horse;

import name.crclr.Crclr;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class FastLoudHorseModule {
    private static final double HORSE_SPEED = 0.45D;
    private static final double HORSE_JUMP_STRENGTH = 1.2D;

    private FastLoudHorseModule() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof AbstractHorse horse)) {
                return;
            }

            boostAttribute(horse, Attributes.MOVEMENT_SPEED, HORSE_SPEED);
            boostAttribute(horse, Attributes.JUMP_STRENGTH, HORSE_JUMP_STRENGTH);

            Crclr.LOGGER.info("Boosted horse {} at {}", horse.getName().getString(), horse.blockPosition());
        });
    }

    private static void boostAttribute(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.setBaseValue(value);
    }
}
