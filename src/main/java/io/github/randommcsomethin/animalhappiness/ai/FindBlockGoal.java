package io.github.randommcsomethin.animalhappiness.ai;

import io.github.randommcsomethin.animalhappiness.AnimalHappiness;
import io.github.randommcsomethin.animalhappiness.mixin.AnimalEntityMixin;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class FindBlockGoal extends Goal {
    PathAwareEntity mob;
    int frequency = 500;
    private int timer = 200;
    BlockState state;

    public FindBlockGoal(PathAwareEntity mob, BlockState state) {
        super();
        this.mob = mob;
        this.state = state;
    }

    @Override
    public boolean canStart() {
        return this.mob.getRandom().nextInt(frequency) == 0;
    }

    @Override
    public void start() {
        //this.mob.getDataTracker().set(AnimalEntityMixin.SEARCH_TARGET, this.findTargetPos());
        //if ()
        //this.mob.setPositionTarget(this.findTargetPos().get(), 16);
        if (this.findTargetPos().isPresent()) {
            BlockPos pos = this.findTargetPos().get();
            this.mob.getNavigation().startMovingTo((double) ((float) pos.getX()) + 0.5D, (double) (pos.getY() + 1), (double) ((float) pos.getZ()) + 0.5D, 1.0D);
            AnimalHappiness.LOGGER.info("I'm moving to a block yippee!!");
        }
    }

    private Optional<BlockPos> findTargetPos() {
        return IntStream.range(0, 5).mapToObj((i) -> FuzzyTargeting.find(this.mob, 10 + 2*i, 3)).filter(Objects::nonNull).map(BlockPos::ofFloored).map(BlockPos::down).filter(this::matchesBlock).findFirst();
    }

    private boolean matchesBlock(BlockPos pos) {
        return this.mob.world.getBlockState(pos) == this.state;
    }

    @Override
    public void tick() {
        //this.timer = Math.max(0, this.timer - 1);
    }

    @Override
    public boolean shouldContinue() {
        return false; //this.timer > 0;
    }
}
