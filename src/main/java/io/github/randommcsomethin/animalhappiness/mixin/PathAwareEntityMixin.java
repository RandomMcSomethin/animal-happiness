package io.github.randommcsomethin.animalhappiness.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin extends MobEntityMixin {

    protected PathAwareEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


}
