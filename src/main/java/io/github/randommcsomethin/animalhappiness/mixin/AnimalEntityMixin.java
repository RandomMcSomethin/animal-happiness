package io.github.randommcsomethin.animalhappiness.mixin;

import io.github.randommcsomethin.animalhappiness.AnimalHappiness;
import io.github.randommcsomethin.animalhappiness.ai.FindBlockGoal;
import io.github.randommcsomethin.animalhappiness.ai.Trackable;
import io.github.randommcsomethin.animalhappiness.config.AnimalEntry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.VariantPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntityMixin {

    private static final TrackedData<Integer> HAPPINESS = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> PET_COOLDOWN = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<BlockPos> SEARCH_TARGET = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);


    // happiness
    public int happiness;
    public int maxHappiness = 100;
    // in ticks
    public int happinessCooldown;
    public int maxHappinessCooldown = 12000;
    // decay cooldown
    //public int decayCooldown;
    //public int maxDecayCooldown = 72000;

    private static AnimalEntry entryForThisAnimal;
    boolean hasSearchTarget;

    // checks from the config
    public boolean isAffectedByHappiness() {
        for (AnimalEntry e : AnimalHappiness.config.animalEntries) {
            if (e.getID().equals(Registries.ENTITY_TYPE.getId(this.getType()).getNamespace() + ":" +
                    Registries.ENTITY_TYPE.getId(this.getType()).getPath())) {
                entryForThisAnimal = e;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onInitDataTracker(CallbackInfo ci) {
        if (isAffectedByHappiness()) {
            this.getDataTracker().startTracking(HAPPINESS, 0);
            this.getDataTracker().startTracking(PET_COOLDOWN, 0);
            /*
            String[] pb = entryForThisAnimal.getParameter("animalhappiness:preferred_block");
            if (pb != null) {
                this.hasSearchTarget = true;
                this.getDataTracker().startTracking(SEARCH_TARGET, BlockPos.ORIGIN);
            } else {
                this.hasSearchTarget = false;
            }
            */
        }
    }

    protected void syncHappiness(boolean setInstead) {
        if (!setInstead) {
            this.happiness = this.getDataTracker().get(HAPPINESS);
            this.happinessCooldown = this.getDataTracker().get(PET_COOLDOWN);
        } else {
            this.getDataTracker().set(HAPPINESS, this.happiness);
            this.getDataTracker().set(PET_COOLDOWN, this.happinessCooldown);
        }
    }

    protected void syncHappiness() {
        this.syncHappiness(false);
    }

    // Initialize NBT data
    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable cir) {
        this.syncHappiness();
        if (this.hasSearchTarget) {
            String[] pb = entryForThisAnimal.getParameter("animalhappiness:preferred_block");
            if (pb != null) {
                BlockState state = Registries.BLOCK.get(Identifier.tryParse(pb[2] + ":" + pb[3])).getDefaultState();
                this.goalSelector.add(1, new FindBlockGoal(this, state));
            } else {
                AnimalHappiness.LOGGER.error("Preferred block was not able to be loaded for entity");
            }
        }
    }

    // dummy constructor
    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (this.isAffectedByHappiness()) {
            this.syncHappiness();
            if (this.happinessCooldown > 0) this.happinessCooldown--;

            // check for passive happiness changes
            if (random.nextInt(300) == 1 && entryForThisAnimal.getParameter("animalhappiness:space") != null) {
                if (this.isCramped()) {
                    this.updateHappiness(-this.maxHappiness/10, true);
                } else if (this.happiness < 0) {
                    this.updateHappiness(this.maxHappiness/10);
                }
            }

            this.syncHappiness(true);
        }
    }

    @Inject(method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable cir) {
        if (this.isAffectedByHappiness()) {
            if (this.happinessCooldown == 0 && player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
                this.playAmbientSound();
                this.updateHappiness(this.maxHappiness / 10);
                if (this.happiness < 0) {
                    world.addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getHeight(), this.getZ(), 0.0, 0.0, 0.0);
                } else if (this.happiness >= 0) {
                    world.addParticle(ParticleTypes.HEART, this.getX(), this.getY() + this.getHeight(), this.getZ(), 0.0, 0.0, 0.0);
                }
                this.happinessCooldown = maxHappinessCooldown;
                this.syncHappiness(true);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    public boolean isCramped() {
        int limit = Integer.parseInt(entryForThisAnimal.getParameter("animalhappiness:space")[2]);
        List<AnimalEntity> list = world.getEntitiesByClass(AnimalEntity.class, this.getBoundingBox().expand(5.0D), AnimalHappiness.IS_ADULT);
        return list.size() > limit;
    }


    @Override
    protected boolean shouldDropLoot() {
        // appends happiness status to mob lootability
        return super.shouldDropLoot() &&
                (!this.isAffectedByHappiness() || this.happiness >= -this.maxHappiness/4);
    }

    protected void updateHappiness(int amt) {
        this.updateHappiness(amt, false);
    }

    protected void updateHappiness(int amt, boolean shouldAddParticles) {
        this.happiness += amt;
        this.clampHappiness();
        if (shouldAddParticles) {
            if (amt < 0) {
                world.addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getHeight(), this.getZ(), 0.0, 0.0, 0.0);
            } else if (amt > 0) {
                world.addParticle(ParticleTypes.HEART, this.getX(), this.getY() + this.getHeight(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void clampHappiness() {
        if (this.happiness > this.maxHappiness) {
            this.happiness = this.maxHappiness;
        } else if (this.happiness < -this.maxHappiness) {
            this.happiness = -this.maxHappiness;
        }
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    public void writeCustomDataToNbt(NbtCompound tags, CallbackInfo ci) {
        if (this.isAffectedByHappiness()) {
            tags.putInt("happiness", this.getDataTracker().get(HAPPINESS));
            tags.putInt("petCooldown", this.getDataTracker().get(PET_COOLDOWN));
        }
        //tags.putBoolean("has_milk", this.hasMilk);
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    public void readCustomDataFromNbt(NbtCompound tags, CallbackInfo ci) {
        if (this.isAffectedByHappiness()) {
            this.getDataTracker().set(HAPPINESS, tags.getInt("happiness"));
            this.getDataTracker().set(PET_COOLDOWN, tags.getInt("petCooldown"));
            //this.happiness = tags.getDouble("happiness");
            //this.happinessCooldown = tags.getInt("happinessCooldown");
        }
        //this.hasMilk = tags.getBoolean("has_milk");
    }
}


