package net.luckystudios.entity.ai.crew;

import net.luckystudios.BlockySiegeConfig;
import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.fluids.FluidStack;
import net.luckystudios.fluids.FluidTank;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModGameRules;
import net.luckystudios.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lets mobs in the {@code blockysiege:siege_crew} entity tag (Pillagers and Piglins by default) crew the Cannon,
 * Burst Cannon and Spewer.
 * <p>
 * Called at the start of every {@code Mob#serverAiStep}. While a mob walks to a weapon or operates one, this
 * returns true and the mob's own goals or brain are paused, so nothing else pulls it away. Only movement, look
 * and jump controls keep running. The rest of the time it returns false and the mob behaves normally.
 * <p>
 * The crew:
 * <ul>
 *     <li>looks for a free, loaded weapon nearby when it has a target it could hit with one, and sometimes when
 *     idle, to stand guard;</li>
 *     <li>walks to it, sits in its seat, and turns the turret at a limited speed, as a person would;</li>
 *     <li>solves a ballistic arc for the weapon's ammo, leads moving targets, and fires only with a clear line
 *     and no allies in the way;</li>
 *     <li>reloads with its own ammo when the weapon runs dry (configurable), or leaves when it can't;</li>
 *     <li>jumps off to fight normally when an enemy gets too close.</li>
 * </ul>
 */
public final class SiegeCrewAI {

    // Timings, in ticks
    private static final int SEARCH_INTERVAL = 20;
    private static final int GUARD_CHANCE = 20;
    private static final int GUARD_DURATION = 1200;
    private static final int GIVE_UP_WITHOUT_TARGET = 100;
    private static final int APPROACH_TIMEOUT = 400;
    private static final int APPROACH_STALL = 80;
    private static final int RELOAD_TIME = 30;
    private static final int NO_AMMO_PATIENCE = 60;
    private static final int IGNORE_FAILED_WEAPON = 600;
    private static final int IGNORE_EMPTY_WEAPON = 1200;

    // Distances, in blocks
    private static final int GUARD_RADIUS = 8;
    private static final double MOUNT_DISTANCE = 2.2;
    private static final double BAIL_DISTANCE = 3.5;
    private static final double CANNON_MIN_RANGE = 6.0;
    /** How far out crews engage with cannons and burst cannons. Well past a mob's own 16 to 32 block senses. */
    private static final double CANNON_RANGE = 48.0;
    private static final int MAX_VERTICAL_SEARCH = 6;

    /** Which mob has claimed each weapon, so two crew don't run for the same one. */
    private static final Map<ServerLevel, Map<BlockPos, Mob>> CLAIMS = new WeakHashMap<>();

    private SiegeCrewAI() {
    }

    /** @return true if the crew AI ran this tick and the mob's own AI should be skipped */
    public static boolean tick(Mob mob) {
        if (!mob.getType().is(ModTags.SIEGE_CREW)) return false;
        if (!(mob.level() instanceof ServerLevel level)) return false;
        SiegeCrewState state = ((SiegeCrewHolder) mob).blockysiege$getCrewState();

        if (!level.getGameRules().getBoolean(ModGameRules.RULE_SIEGE_WEAPON_CREWS) || !canCrew(mob)) {
            if (state.phase != SiegeCrewState.Phase.NONE) release(level, mob, state, true);
            return false;
        }

        adoptExistingSeat(level, mob, state);

        return switch (state.phase) {
            case NONE -> tickIdle(level, mob, state);
            case APPROACH -> tickApproach(level, mob, state);
            case MANNING -> tickManning(level, mob, state);
        };
    }

    private static boolean canCrew(Mob mob) {
        if (!mob.isAlive() || mob.isBaby() || mob.isLeashed()) return false;
        // Piglins about to turn into zombified piglins are in no state to aim a cannon
        return !(mob instanceof AbstractPiglin piglin && piglin.isConverting());
    }

    /** A mob loaded from disk while riding a weapon's seat picks up where it left off. */
    private static void adoptExistingSeat(ServerLevel level, Mob mob, SiegeCrewState state) {
        if (state.phase == SiegeCrewState.Phase.MANNING) return;
        if (!(mob.getVehicle() instanceof Seat seat)) return;
        BlockPos pos = seat.blockPosition();
        if (weaponAt(level, pos) == null) return;
        unclaim(level, state.weapon, mob);
        state.reset();
        state.phase = SiegeCrewState.Phase.MANNING;
        state.weapon = pos.immutable();
        state.guarding = state.target == null;
        claim(level, state.weapon, mob);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Idle: look for a weapon
    // ------------------------------------------------------------------------------------------------------------

    private static boolean tickIdle(ServerLevel level, Mob mob, SiegeCrewState state) {
        if (mob.isPassenger()) return false;
        if (state.searchCooldown > 0) {
            state.searchCooldown--;
            return false;
        }
        state.searchCooldown = SEARCH_INTERVAL + mob.getRandom().nextInt(10);
        forgetOldIgnoredWeapons(level, state);

        LivingEntity target = vanillaTarget(mob);
        if (target != null && !isValidTarget(mob, target)) target = null;

        int searchRadius = Math.max(4, BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS.get());
        if (target == null) {
            // A mob's own senses only reach 16 to 32 blocks. Near a weapon, the crew looks out from the weapon
            // instead, so a cannon can open up on enemies far beyond that.
            target = spotFromNearbyWeapons(level, mob, state, searchRadius);
        }

        boolean guard = false;
        if (target == null) {
            if (mob.getRandom().nextInt(GUARD_CHANCE) != 0) return false;
            guard = true;
        } else if (mob.distanceTo(target) < BAIL_DISTANCE * 2) {
            // Too late to run for a weapon: fight it out
            return false;
        }

        int radius = guard ? GUARD_RADIUS : searchRadius;
        BlockPos weapon = findWeapon(level, mob, state, target, radius);
        if (weapon == null) return false;

        state.reset();
        state.phase = SiegeCrewState.Phase.APPROACH;
        state.weapon = weapon;
        state.target = target;
        state.guarding = guard;
        claim(level, weapon, mob);
        return tickApproach(level, mob, state);
    }

    /** Free, usable weapons within the radius, nearest first. Cheap: no pathfinding. */
    private static List<AbstractShootingAimableBlockEntity> nearbyWeapons(ServerLevel level, Mob mob, SiegeCrewState state, int radius) {
        BlockPos origin = mob.blockPosition();
        int chunkRadius = (radius >> 4) + 1;
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        double radiusSqr = (double) radius * radius;

        List<AbstractShootingAimableBlockEntity> weapons = new ArrayList<>();
        for (int cx = originChunkX - chunkRadius; cx <= originChunkX + chunkRadius; cx++) {
            for (int cz = originChunkZ - chunkRadius; cz <= originChunkZ + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof AbstractShootingAimableBlockEntity weapon) || !isCrewable(weapon)) continue;
                    BlockPos pos = weapon.getBlockPos();
                    if (Math.abs(pos.getY() - origin.getY()) > MAX_VERTICAL_SEARCH) continue;
                    if (pos.distSqr(origin) > radiusSqr) continue;
                    if (state.ignoredWeapons.containsKey(pos)) continue;
                    if (isOccupied(level, pos) || isClaimedByOther(level, pos, mob)) continue;
                    if (!hasAmmo(weapon) && !canResupply(weapon)) continue;
                    weapons.add(weapon);
                }
            }
        }
        weapons.sort(Comparator.comparingDouble(weapon -> weapon.getBlockPos().distSqr(origin)));
        return weapons;
    }

    /** Looks out from the nearest free weapons for an enemy one of them could hit. */
    @Nullable
    private static LivingEntity spotFromNearbyWeapons(ServerLevel level, Mob mob, SiegeCrewState state, int radius) {
        List<AbstractShootingAimableBlockEntity> weapons = nearbyWeapons(level, mob, state, radius);
        for (int i = 0; i < Math.min(2, weapons.size()); i++) {
            LivingEntity spotted = findTarget(level, mob, weapons.get(i));
            if (spotted != null) return spotted;
        }
        return null;
    }

    @Nullable
    private static BlockPos findWeapon(ServerLevel level, Mob mob, SiegeCrewState state, @Nullable LivingEntity target, int radius) {
        BlockPos origin = mob.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (AbstractShootingAimableBlockEntity weapon : nearbyWeapons(level, mob, state, radius)) {
            if (target != null && !canEngage(weapon, target, true)) continue;
            candidates.add(weapon.getBlockPos().immutable());
        }
        if (candidates.isEmpty()) return null;

        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(origin)));
        // Pathfinding is the expensive part, so only try the closest few
        for (int i = 0; i < Math.min(3, candidates.size()); i++) {
            BlockPos pos = candidates.get(i);
            Path path = mob.getNavigation().createPath(pos, 1);
            if (path != null && path.canReach()) return pos;
            ignore(level, state, pos, IGNORE_FAILED_WEAPON);
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Approach: walk to the weapon and sit down
    // ------------------------------------------------------------------------------------------------------------

    private static boolean tickApproach(ServerLevel level, Mob mob, SiegeCrewState state) {
        state.phaseTicks++;
        BlockPos pos = state.weapon;
        AbstractShootingAimableBlockEntity weapon = pos == null ? null : weaponAt(level, pos);
        if (weapon == null || isOccupied(level, pos) || isClaimedByOther(level, pos, mob)) {
            if (pos != null) ignore(level, state, pos, IGNORE_FAILED_WEAPON);
            release(level, mob, state, true);
            return false;
        }

        if (state.target != null && !isValidTarget(mob, state.target)) state.target = null;
        if (state.target == null && !state.guarding) {
            release(level, mob, state, false);
            return false;
        }
        // A guard on its way to a post reacts to whatever its own senses picked up
        if (state.guarding && state.target == null) {
            LivingEntity spotted = vanillaTarget(mob);
            if (spotted != null && isValidTarget(mob, spotted)) state.target = spotted;
        }

        LivingEntity attacker = recentCloseAttacker(mob);
        if (attacker != null) {
            state.target = attacker;
            release(level, mob, state, true);
            return false;
        }
        if (state.target != null && mob.distanceTo(state.target) < BAIL_DISTANCE) {
            release(level, mob, state, true);
            return false;
        }

        Vec3 centre = Vec3.atCenterOf(pos);
        double horizontal = Math.sqrt(Mth.square(mob.getX() - centre.x) + Mth.square(mob.getZ() - centre.z));
        if (horizontal < MOUNT_DISTANCE && Math.abs(mob.getY() - pos.getY()) < 1.5) {
            mount(level, mob, state, weapon);
            return true;
        }

        if (horizontal < state.closestApproach - 0.25) {
            state.closestApproach = horizontal;
            state.ticksSinceProgress = 0;
        } else {
            state.ticksSinceProgress++;
        }
        if (state.ticksSinceProgress > APPROACH_STALL || state.phaseTicks > APPROACH_TIMEOUT) {
            ignore(level, state, pos, IGNORE_FAILED_WEAPON);
            release(level, mob, state, true);
            return false;
        }

        if (--state.repathCooldown <= 0 || mob.getNavigation().isDone()) {
            state.repathCooldown = 20;
            Path path = mob.getNavigation().createPath(pos, 1);
            if (path == null) {
                ignore(level, state, pos, IGNORE_FAILED_WEAPON);
                release(level, mob, state, true);
                return false;
            }
            // Hurry when there's a fight on
            mob.getNavigation().moveTo(path, state.guarding && state.target == null ? 0.8 : 1.15);
        }
        mob.getLookControl().setLookAt(centre.x, centre.y, centre.z, 30.0F, 30.0F);
        runMovementControls(mob);
        return true;
    }

    private static void mount(ServerLevel level, Mob mob, SiegeCrewState state, AbstractShootingAimableBlockEntity weapon) {
        BlockPos pos = weapon.getBlockPos();
        mob.getNavigation().stop();
        Seat seat = new Seat(level, pos);
        level.addFreshEntity(seat);
        if (!mob.startRiding(seat)) {
            seat.discard();
            ignore(level, state, pos, IGNORE_FAILED_WEAPON);
            release(level, mob, state, true);
            return;
        }
        state.phase = SiegeCrewState.Phase.MANNING;
        state.phaseTicks = 0;
        // A moment to settle in before the first shot
        state.fireCooldown = 15 + mob.getRandom().nextInt(15);
        state.sweepCentre = weapon.yaw;
        mob.playAmbientSound();
        holdPose(mob, weapon);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Manning: aim, reload and fire
    // ------------------------------------------------------------------------------------------------------------

    private static boolean tickManning(ServerLevel level, Mob mob, SiegeCrewState state) {
        state.phaseTicks++;
        BlockPos pos = state.weapon;
        if (pos == null || !(mob.getVehicle() instanceof Seat seat) || !seat.blockPosition().equals(pos)) {
            release(level, mob, state, true);
            return false;
        }
        AbstractShootingAimableBlockEntity weapon = weaponAt(level, pos);
        if (weapon == null) {
            release(level, mob, state, true);
            return false;
        }
        claim(level, pos, mob);

        // Someone is hitting us from up close: get off and deal with them
        LivingEntity attacker = recentCloseAttacker(mob);
        if (attacker != null) {
            state.target = attacker;
            release(level, mob, state, true);
            return false;
        }

        updateTarget(level, mob, state, weapon);
        LivingEntity target = state.target;

        if (target == null) {
            state.noTargetTicks++;
            if (state.noTargetTicks > (state.guarding ? GUARD_DURATION : GIVE_UP_WITHOUT_TARGET)) {
                // Wander off for a while before coming back to this one
                ignore(level, state, pos, IGNORE_FAILED_WEAPON);
                release(level, mob, state, false);
                return false;
            }
            sweep(state, weapon);
            holdPose(mob, weapon);
            syncWeapon(level, state, weapon);
            return true;
        }
        state.noTargetTicks = 0;

        if (mob.distanceTo(target) < BAIL_DISTANCE) {
            release(level, mob, state, true);
            return false;
        }

        // Ammo
        if (!hasAmmo(weapon)) {
            if (canResupply(weapon)) {
                state.reloadTicks++;
                if (state.reloadTicks == RELOAD_TIME / 2) mob.swing(InteractionHand.MAIN_HAND);
                if (state.reloadTicks >= RELOAD_TIME) {
                    resupply(level, mob, weapon);
                    state.reloadTicks = 0;
                    state.fireCooldown = Math.max(state.fireCooldown, 10);
                }
            } else if (++state.noAmmoTicks > NO_AMMO_PATIENCE) {
                ignore(level, state, pos, IGNORE_EMPTY_WEAPON);
                release(level, mob, state, true);
                return false;
            }
        } else {
            state.reloadTicks = 0;
            state.noAmmoTicks = 0;
        }

        // Aim. Crews keep tracking (and re-picking the charge) while a cannon's fuse burns, so the shot goes
        // where the target will be when it actually fires.
        boolean fuseBurning = weapon instanceof CannonBlockEntity cannon && cannon.cooldown > 0;
        AimSolution aim = solveAim(weapon, target, state, false);
        boolean aligned = turnTowards(weapon, aim.yaw, aim.pitch);
        if (fuseBurning) ((CannonBlockEntity) weapon).firePower = Mth.clamp(aim.cannonPower, 1, 4);
        holdPose(mob, weapon);
        // Hold back syncs in the last moments of a fuse so a late packet can't reset the client's own countdown
        // past zero, which is when the client plays the shot's sound and recoil
        boolean aboutToFire = weapon instanceof CannonBlockEntity cannon && cannon.cooldown > 0 && cannon.cooldown <= 6;
        if (!aboutToFire) syncWeapon(level, state, weapon);

        if (state.fireCooldown > 0) {
            state.fireCooldown--;
            return true;
        }
        if (fuseBurning || !aligned || !aim.reachable || !hasAmmo(weapon)) return true;
        if (!hasClearShot(level, mob, weapon, target)) return true;

        fire(level, mob, state, weapon, aim);
        return true;
    }

    private static void updateTarget(ServerLevel level, Mob mob, SiegeCrewState state, AbstractShootingAimableBlockEntity weapon) {
        LivingEntity target = state.target;
        if (target != null) {
            boolean outOfRange = target.distanceToSqr(Vec3.atCenterOf(weapon.getBlockPos())) > Mth.square(maxRange(weapon) + 8.0);
            if (!isValidTarget(mob, target) || outOfRange) {
                if (target.isDeadOrDying()) celebrate(mob);
                state.target = null;
                state.lastTargetPos = null;
                state.targetVelocity = Vec3.ZERO;
            }
        }

        // Whoever shot at us from a distance becomes the new priority
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker != null && attacker != state.target && mob.tickCount - mob.getLastHurtByMobTimestamp() < 40
                && isValidTarget(mob, attacker) && canEngage(weapon, attacker, false)) {
            setTarget(mob, state, attacker);
        }

        if (state.target == null && --state.retargetCooldown <= 0) {
            state.retargetCooldown = 10;
            LivingEntity found = findTarget(level, mob, weapon);
            if (found != null) setTarget(mob, state, found);
        }

        // Track the target's velocity for leading shots. Server-side player motion isn't reliable, so use positions.
        if (state.target != null) {
            Vec3 now = state.target.position();
            if (state.lastTargetPos != null) {
                Vec3 moved = now.subtract(state.lastTargetPos);
                if (moved.lengthSqr() > 4.0) moved = Vec3.ZERO; // teleported
                state.targetVelocity = state.targetVelocity.scale(0.6).add(moved.scale(0.4));
            }
            state.lastTargetPos = now;
        }
    }

    private static void setTarget(Mob mob, SiegeCrewState state, LivingEntity target) {
        state.target = target;
        state.lastTargetPos = null;
        state.targetVelocity = Vec3.ZERO;
        mob.setTarget(target);
    }

    @Nullable
    private static LivingEntity findTarget(ServerLevel level, Mob mob, AbstractShootingAimableBlockEntity weapon) {
        Vec3 pivot = Vec3.atCenterOf(weapon.getBlockPos());
        double range = maxRange(weapon);
        AABB area = new AABB(pivot, pivot).inflate(range, 24.0, range);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> isValidTarget(mob, entity) && isNaturalEnemy(mob, entity));
        candidates.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(pivot)));
        for (int i = 0; i < Math.min(6, candidates.size()); i++) {
            LivingEntity candidate = candidates.get(i);
            if (hasLineOfSight(level, mob, weapon, candidate) && canEngage(weapon, candidate, false)) return candidate;
        }
        return null;
    }

    /** Slowly swings the barrel left and right while keeping watch. */
    private static void sweep(SiegeCrewState state, AbstractShootingAimableBlockEntity weapon) {
        if (Float.isNaN(state.sweepCentre)) state.sweepCentre = weapon.yaw;
        float offset = Mth.wrapDegrees(weapon.yaw - state.sweepCentre);
        if (offset > 35.0F) state.sweepDirection = -1.0F;
        if (offset < -35.0F) state.sweepDirection = 1.0F;
        weapon.setYaw(normalizeYaw(weapon.yaw + state.sweepDirection * 0.75F));
        float restPitch = Mth.clamp(5.0F, weapon.minPitch(), weapon.maxPitch());
        float pitchStep = Mth.clamp(restPitch - weapon.pitch, -1.0F, 1.0F);
        if (pitchStep != 0.0F) weapon.setPitch(weapon.pitch + pitchStep);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Aiming
    // ------------------------------------------------------------------------------------------------------------

    private record AimSolution(float yaw, float pitch, boolean reachable, int cannonPower) {
    }

    /**
     * Works out where to point the barrel. For cannons it also picks the gunpowder charge: the smallest one that
     * gets there quickly, which saves powder and puts less strain on the barrel.
     */
    private static AimSolution solveAim(AbstractShootingAimableBlockEntity weapon, LivingEntity target, SiegeCrewState state, boolean ignoreLead) {
        Vec3 pivot = Vec3.atCenterOf(weapon.getBlockPos());
        // Cannonballs burst on impact, so aim low; everything else aims at the body
        double heightFactor = weapon instanceof CannonBlockEntity ? 0.25 : 0.5;
        Vec3 point = target.position().add(0.0, target.getBbHeight() * heightFactor, 0.0);

        AimSolution solution = solveFor(weapon, pivot, point);
        if (!ignoreLead && solution.reachable && state.targetVelocity.lengthSqr() > 1.0E-4) {
            int flight = flightTicks(weapon, pivot, point, solution);
            Vec3 velocity = new Vec3(state.targetVelocity.x, Math.max(0.0, state.targetVelocity.y), state.targetVelocity.z);
            Vec3 led = point.add(velocity.scale(Math.min(flight, 60)));
            AimSolution ledSolution = solveFor(weapon, pivot, led);
            if (ledSolution.reachable) return ledSolution;
        }
        return solution;
    }

    private static AimSolution solveFor(AbstractShootingAimableBlockEntity weapon, Vec3 pivot, Vec3 point) {
        double dx = point.x - pivot.x;
        double dz = point.z - pivot.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double vertical = point.y - pivot.y;
        // Same convention as AbstractShootingAimableBlockEntity#getAimVector: x = -sin(yaw), z = cos(yaw)
        float yaw = normalizeYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
        double tolerance = weapon instanceof SpewerBlockEntity ? 2.0 : 0.75;

        if (weapon instanceof CannonBlockEntity cannon) {
            int maxPower = availableCannonPower(cannon);
            AimSolution fallback = null;
            for (int power = 1; power <= maxPower; power++) {
                WeaponBallistics.Solution s = WeaponBallistics.solve(cannonProfile(cannon, power), horizontal, vertical, cannon.minPitch(), cannon.maxPitch(), tolerance);
                AimSolution candidate = new AimSolution(yaw, s.pitch(), s.reachable(), power);
                if (s.reachable() && s.flightTicks() <= 30) return candidate;
                if (s.reachable() || fallback == null || !fallback.reachable) fallback = candidate;
            }
            return fallback != null ? fallback : new AimSolution(yaw, 0.0F, false, 1);
        }

        WeaponBallistics.Solution s = WeaponBallistics.solve(profile(weapon), horizontal, vertical, weapon.minPitch(), weapon.maxPitch(), tolerance);
        return new AimSolution(yaw, s.pitch(), s.reachable(), 0);
    }

    private static int flightTicks(AbstractShootingAimableBlockEntity weapon, Vec3 pivot, Vec3 point, AimSolution solution) {
        double dx = point.x - pivot.x;
        double dz = point.z - pivot.z;
        WeaponBallistics.Profile profile = weapon instanceof CannonBlockEntity cannon
                ? cannonProfile(cannon, Math.max(1, solution.cannonPower))
                : profile(weapon);
        return WeaponBallistics.flightTime(profile, solution.pitch, Math.sqrt(dx * dx + dz * dz));
    }

    /** Whether the weapon could put a shot on this target from where it stands. */
    private static boolean canEngage(AbstractShootingAimableBlockEntity weapon, LivingEntity target, boolean assumeFullyLoaded) {
        Vec3 pivot = Vec3.atCenterOf(weapon.getBlockPos());
        if (target.distanceToSqr(pivot) > Mth.square(maxRange(weapon))) return false;
        if (weapon instanceof CannonBlockEntity && target.distanceToSqr(pivot) < Mth.square(CANNON_MIN_RANGE)) return false;
        Vec3 point = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        if (weapon instanceof CannonBlockEntity cannon && assumeFullyLoaded) {
            double dx = point.x - pivot.x;
            double dz = point.z - pivot.z;
            return WeaponBallistics.solve(cannonProfile(cannon, 4), Math.sqrt(dx * dx + dz * dz), point.y - pivot.y,
                    cannon.minPitch(), cannon.maxPitch(), 0.75).reachable();
        }
        return solveFor(weapon, pivot, point).reachable;
    }

    /** Turns the turret toward the wanted angles at the weapon's traverse speed. @return true once on target */
    private static boolean turnTowards(AbstractShootingAimableBlockEntity weapon, float wantedYaw, float wantedPitch) {
        float yawSpeed, pitchSpeed, yawTolerance, pitchTolerance;
        if (weapon instanceof CannonBlockEntity) {
            yawSpeed = 4.0F; pitchSpeed = 3.0F; yawTolerance = 2.0F; pitchTolerance = 1.0F;
        } else if (weapon instanceof MultiCannonBlockEntity) {
            yawSpeed = 7.0F; pitchSpeed = 5.0F; yawTolerance = 4.0F; pitchTolerance = 3.0F;
        } else {
            yawSpeed = 9.0F; pitchSpeed = 6.0F; yawTolerance = 10.0F; pitchTolerance = 6.0F;
        }

        float yawError = Mth.wrapDegrees(wantedYaw - weapon.yaw);
        if (yawError != 0.0F) weapon.setYaw(normalizeYaw(weapon.yaw + Mth.clamp(yawError, -yawSpeed, yawSpeed)));

        float pitch = Mth.clamp(wantedPitch, weapon.minPitch(), weapon.maxPitch());
        float pitchError = pitch - weapon.pitch;
        if (pitchError != 0.0F) weapon.setPitch(weapon.pitch + Mth.clamp(pitchError, -pitchSpeed, pitchSpeed));

        return Math.abs(Mth.wrapDegrees(wantedYaw - weapon.yaw)) <= yawTolerance
                && Math.abs(pitch - weapon.pitch) <= pitchTolerance;
    }

    /** Sends the new barrel angles to clients, at most every other tick. Players aim on their own client. */
    private static void syncWeapon(ServerLevel level, SiegeCrewState state, AbstractShootingAimableBlockEntity weapon) {
        if (state.syncCooldown > 0) {
            state.syncCooldown--;
            return;
        }
        if (weapon.yaw == state.syncedYaw && weapon.pitch == state.syncedPitch) return;
        state.syncedYaw = weapon.yaw;
        state.syncedPitch = weapon.pitch;
        state.syncCooldown = 1;
        BlockState blockState = weapon.getBlockState();
        level.sendBlockUpdated(weapon.getBlockPos(), blockState, blockState, Block.UPDATE_CLIENTS);
    }

    /** Keeps the crew facing down the barrel; the seat places them behind it from their rotation. */
    private static void holdPose(Mob mob, AbstractShootingAimableBlockEntity weapon) {
        float yaw = weapon.yaw;
        mob.setYRot(yaw);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        mob.setXRot(Mth.clamp(-weapon.pitch, -60.0F, 60.0F));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Firing
    // ------------------------------------------------------------------------------------------------------------

    private static void fire(ServerLevel level, Mob mob, SiegeCrewState state, AbstractShootingAimableBlockEntity weapon, AimSolution aim) {
        BlockPos pos = weapon.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractShootingBlock block)) return;

        if (weapon instanceof CannonBlockEntity cannon) {
            if (cannon.cooldown > -1 || !cannon.canShoot()) return;
            cannon.firePower = Mth.clamp(aim.cannonPower, 1, 4);
            // Make sure clients hold the final angles before the fuse locks them
            BlockState current = weapon.getBlockState();
            level.sendBlockUpdated(pos, current, current, Block.UPDATE_CLIENTS);
            block.triggerBlock(blockState, level, pos);
            mob.swing(InteractionHand.MAIN_HAND);
            // Fuse, then time to swab and reload
            state.fireCooldown = cannon.maxCooldown + 40 + mob.getRandom().nextInt(30);
        } else if (weapon instanceof MultiCannonBlockEntity multiCannon) {
            if (!multiCannon.canShoot()) return;
            int rounds = Math.min(4, Math.min(multiCannon.getFuse().getCount(), multiCannon.getProjectile().getCount()));
            multiCannon.setBulletCount(Math.max(1, rounds));
            block.triggerBlock(blockState, level, pos);
            mob.swing(InteractionHand.MAIN_HAND);
            state.fireCooldown = multiCannon.getBulletCount() * 10 + 25 + mob.getRandom().nextInt(20);
        } else if (weapon instanceof SpewerBlockEntity spewer) {
            // The spewer fires while held, at its own rate
            spewer.holdFire();
        }
    }

    /** Line of sight from the muzzle to the target, ignoring the weapon block itself. */
    private static boolean hasLineOfSight(Level level, Mob mob, AbstractShootingAimableBlockEntity weapon, LivingEntity target) {
        Vec3 pivot = Vec3.atCenterOf(weapon.getBlockPos());
        for (Vec3 point : new Vec3[]{target.getEyePosition(), target.position().add(0.0, target.getBbHeight() * 0.5, 0.0)}) {
            Vec3 direction = point.subtract(pivot);
            if (direction.lengthSqr() < 1.0E-4) return true;
            Vec3 from = pivot.add(direction.normalize().scale(weapon.barrelOffsetDistance()));
            HitResult hit = level.clip(new ClipContext(from, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
            if (hit.getType() == HitResult.Type.MISS) return true;
        }
        return false;
    }

    /** A clear line to the target, and no friends standing in front of the barrel. */
    private static boolean hasClearShot(Level level, Mob mob, AbstractShootingAimableBlockEntity weapon, LivingEntity target) {
        if (!hasLineOfSight(level, mob, weapon, target)) return false;
        Vec3 pivot = Vec3.atCenterOf(weapon.getBlockPos());
        if (weapon instanceof CannonBlockEntity && target.distanceToSqr(pivot) < Mth.square(CANNON_MIN_RANGE)) return false;

        Vec3 direction = AbstractShootingAimableBlockEntity.getAimVector(weapon);
        Vec3 from = pivot.add(direction.scale(weapon.barrelOffsetDistance()));
        double length = Math.min(Math.sqrt(target.distanceToSqr(pivot)), 12.0);
        Vec3 to = from.add(direction.scale(length));
        List<LivingEntity> inTheWay = level.getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(1.5),
                entity -> entity != mob && entity != target && entity.isAlive() && isAlly(mob, entity));
        for (LivingEntity entity : inTheWay) {
            if (entity.getBoundingBox().inflate(0.75).clip(from, to).isPresent()) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Ammo
    // ------------------------------------------------------------------------------------------------------------

    private static boolean isCrewable(AbstractShootingAimableBlockEntity weapon) {
        return weapon instanceof CannonBlockEntity || weapon instanceof MultiCannonBlockEntity || weapon instanceof SpewerBlockEntity;
    }

    private static boolean hasAmmo(AbstractShootingAimableBlockEntity weapon) {
        if (weapon instanceof CannonBlockEntity cannon) {
            return cannon.getFuse().is(ModTags.GUNPOWDERS) && CannonBlockEntity.isValidAmmo(cannon.getProjectile());
        }
        if (weapon instanceof MultiCannonBlockEntity multiCannon) {
            return multiCannon.getFuse().is(ModTags.GUNPOWDERS) && !multiCannon.getProjectile().isEmpty();
        }
        if (weapon instanceof SpewerBlockEntity spewer) {
            return !spewer.getFluidTank().isEmpty();
        }
        return false;
    }

    /** Whether the crew can top the weapon up from their own supplies. They only fill empty slots. */
    private static boolean canResupply(AbstractShootingAimableBlockEntity weapon) {
        if (!BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO.get()) return false;
        if (weapon instanceof CannonBlockEntity cannon) {
            ItemStack fuse = cannon.getFuse();
            ItemStack ammo = cannon.getProjectile();
            return (fuse.isEmpty() || fuse.is(ModTags.GUNPOWDERS)) && (ammo.isEmpty() || CannonBlockEntity.isValidAmmo(ammo));
        }
        if (weapon instanceof MultiCannonBlockEntity multiCannon) {
            ItemStack fuse = multiCannon.getFuse();
            return fuse.isEmpty() || fuse.is(ModTags.GUNPOWDERS);
        }
        if (weapon instanceof SpewerBlockEntity spewer) {
            return spewer.getFluidTank().isEmpty() && spewer.getLiquidStack().isEmpty();
        }
        return false;
    }

    /**
     * Loads the weapon from the crew's own supplies. Piglins bring Nether ammo (fire bombs, fire charges, lava);
     * everyone else brings cannonballs, arrows and poison.
     */
    private static void resupply(ServerLevel level, Mob mob, AbstractShootingAimableBlockEntity weapon) {
        boolean nether = mob instanceof AbstractPiglin;
        BlockPos pos = weapon.getBlockPos();
        SoundEvent sound = SoundEvents.CHAIN_PLACE;

        if (weapon instanceof CannonBlockEntity cannon) {
            if (cannon.getFuse().isEmpty()) cannon.setItem(0, new ItemStack(Items.GUNPOWDER, 4));
            if (cannon.getProjectile().isEmpty()) {
                cannon.setItem(1, new ItemStack(nether ? ModBlocks.FIRE_BOMB.get() : ModBlocks.CANNONBALL.get()));
            }
        } else if (weapon instanceof MultiCannonBlockEntity multiCannon) {
            if (multiCannon.getFuse().isEmpty()) multiCannon.setItem(0, new ItemStack(Items.GUNPOWDER, 8));
            if (multiCannon.getProjectile().isEmpty()) {
                multiCannon.setItem(1, new ItemStack(nether ? Items.FIRE_CHARGE : Items.ARROW, 8));
            }
        } else if (weapon instanceof SpewerBlockEntity spewer) {
            FluidStack fluid;
            if (nether) {
                fluid = new FluidStack(Fluids.LAVA, 1000);
                sound = SoundEvents.BUCKET_EMPTY_LAVA;
            } else {
                fluid = new FluidStack(Fluids.WATER, 1000);
                fluid.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
                sound = SoundEvents.BREWING_STAND_BREW;
            }
            spewer.getFluidTank().fill(fluid, FluidTank.FluidAction.EXECUTE);
        }
        weapon.setChanged();
        mob.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, sound, SoundSource.HOSTILE, 0.8F, 0.9F + mob.getRandom().nextFloat() * 0.2F);
    }

    private static int availableCannonPower(CannonBlockEntity cannon) {
        ItemStack fuse = cannon.getFuse();
        if (fuse.is(ModTags.GUNPOWDERS)) return Mth.clamp(fuse.getCount(), 1, 4);
        return 4; // Empty, and about to be filled by the crew
    }

    private static WeaponBallistics.Profile cannonProfile(CannonBlockEntity cannon, int power) {
        ItemStack ammo = cannon.getProjectile();
        // Cannon ammo flies like the mod's cannonballs; any other block is a slightly floatier block projectile
        double gravity = ammo.isEmpty() || ammo.is(ModTags.CANNON_AMMO) ? 0.05 : 0.04;
        return new WeaponBallistics.Profile(power, gravity, 0.99, cannon.barrelOffsetDistance());
    }

    private static WeaponBallistics.Profile profile(AbstractShootingAimableBlockEntity weapon) {
        if (weapon instanceof MultiCannonBlockEntity multiCannon) {
            ItemStack ammo = multiCannon.getProjectile();
            if (ammo.getItem() instanceof FireChargeItem) return new WeaponBallistics.Profile(4.0F, 0.0, 1.0, 1.0F);
            if (ammo.getItem() instanceof ArrowItem) return new WeaponBallistics.Profile(4.0F, 0.05, 0.99, 1.0F);
            return new WeaponBallistics.Profile(4.0F, 0.03, 0.99, 1.0F);
        }
        // Spewer blobs
        return new WeaponBallistics.Profile(0.5F, 0.03, 0.99, weapon.barrelOffsetDistance());
    }

    private static double maxRange(AbstractShootingAimableBlockEntity weapon) {
        if (weapon instanceof CannonBlockEntity) return CANNON_RANGE;
        if (weapon instanceof MultiCannonBlockEntity) return CANNON_RANGE;
        return 12.0;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Targets and allies
    // ------------------------------------------------------------------------------------------------------------

    @Nullable
    private static LivingEntity vanillaTarget(Mob mob) {
        LivingEntity target = mob.getTarget();
        if (target == null && mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
            target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        return target;
    }

    private static boolean isValidTarget(Mob mob, LivingEntity target) {
        if (target == mob || !target.isAlive() || target.isRemoved() || target.level() != mob.level()) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return mob.canAttack(target) && !isAlly(mob, target);
    }

    /** Who this crew would pick a fight with on its own. */
    private static boolean isNaturalEnemy(Mob mob, LivingEntity entity) {
        if (entity == mob.getLastHurtByMob()) return true;
        if (entity instanceof Player player) {
            if (mob instanceof AbstractPiglin && PiglinAi.isWearingGold(player)) return false;
            return mob instanceof Enemy;
        }
        if (mob instanceof Raider) return entity instanceof AbstractVillager || entity instanceof IronGolem;
        return false;
    }

    private static boolean isAlly(Mob mob, Entity entity) {
        if (entity == mob || mob.isAlliedTo(entity) || entity.getType() == mob.getType()) return true;
        if (mob instanceof Raider) return entity instanceof Raider || entity.getType().is(EntityTypeTags.ILLAGER_FRIENDS);
        if (mob instanceof AbstractPiglin) return entity instanceof AbstractPiglin;
        return false;
    }

    @Nullable
    private static LivingEntity recentCloseAttacker(Mob mob) {
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null || !attacker.isAlive()) return null;
        if (mob.tickCount - mob.getLastHurtByMobTimestamp() > 20) return null;
        return mob.distanceTo(attacker) < BAIL_DISTANCE + 1.5 && isValidTarget(mob, attacker) ? attacker : null;
    }

    private static void celebrate(Mob mob) {
        SoundEvent sound = null;
        if (mob instanceof Raider raider) sound = raider.getCelebrateSound();
        else if (mob instanceof AbstractPiglin) sound = SoundEvents.PIGLIN_CELEBRATE;
        if (sound != null) mob.playSound(sound, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------------------------------------------

    /** Hands the mob back to its own AI, getting off the weapon first if needed. */
    private static void release(ServerLevel level, Mob mob, SiegeCrewState state, boolean keepTarget) {
        BlockPos weapon = state.weapon;
        LivingEntity target = state.target;
        AbstractShootingAimableBlockEntity weaponEntity = weapon == null ? null : weaponAt(level, weapon);
        if (state.phase == SiegeCrewState.Phase.MANNING && mob.getVehicle() instanceof Seat) {
            dismount(level, mob, weapon, weaponEntity != null ? weaponEntity.yaw : mob.getYRot());
        }
        unclaim(level, weapon, mob);
        mob.getNavigation().stop();
        state.reset();
        state.target = null;
        state.searchCooldown = 40 + mob.getRandom().nextInt(40);

        if (keepTarget && target != null && isValidTarget(mob, target)) {
            mob.setTarget(target);
            if (mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            }
        }
    }

    /** Gets off the seat and steps clear of the weapon instead of standing on top of it. */
    private static void dismount(ServerLevel level, Mob mob, @Nullable BlockPos weapon, float yaw) {
        mob.stopRiding();
        if (weapon == null) return;
        float radians = yaw * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 base = Vec3.atBottomCenterOf(weapon);
        Vec3[] spots = {
                base.subtract(forward.scale(1.2)),
                base.add(right.scale(1.2)),
                base.subtract(right.scale(1.2)),
                base.subtract(forward.scale(1.2)).add(0.0, 1.0, 0.0),
                base.add(forward.scale(1.2))
        };
        for (Vec3 spot : spots) {
            if (level.noCollision(mob, mob.getDimensions(mob.getPose()).makeBoundingBox(spot))) {
                mob.moveTo(spot.x, spot.y, spot.z, yaw, 0.0F);
                return;
            }
        }
    }

    private static void runMovementControls(Mob mob) {
        mob.getSensing().tick();
        mob.getNavigation().tick();
        mob.getMoveControl().tick();
        mob.getLookControl().tick();
        mob.getJumpControl().tick();
    }

    @Nullable
    private static AbstractShootingAimableBlockEntity weaponAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) return null;
        return level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity weapon && isCrewable(weapon) ? weapon : null;
    }

    private static boolean isOccupied(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(Seat.class, new AABB(pos)).isEmpty();
    }

    private static void claim(ServerLevel level, @Nullable BlockPos pos, Mob mob) {
        if (pos == null) return;
        CLAIMS.computeIfAbsent(level, key -> new HashMap<>()).put(pos, mob);
    }

    private static void unclaim(ServerLevel level, @Nullable BlockPos pos, Mob mob) {
        if (pos == null) return;
        Map<BlockPos, Mob> claims = CLAIMS.get(level);
        if (claims != null) claims.remove(pos, mob);
    }

    private static boolean isClaimedByOther(ServerLevel level, BlockPos pos, Mob mob) {
        Map<BlockPos, Mob> claims = CLAIMS.get(level);
        if (claims == null) return false;
        Mob owner = claims.get(pos);
        if (owner == null || owner == mob) return false;
        SiegeCrewState ownerState = ((SiegeCrewHolder) owner).blockysiege$getCrewState();
        if (!owner.isAlive() || owner.isRemoved() || !pos.equals(ownerState.weapon)) {
            claims.remove(pos);
            return false;
        }
        return true;
    }

    private static void ignore(ServerLevel level, SiegeCrewState state, BlockPos pos, int ticks) {
        state.ignoredWeapons.put(pos.immutable(), level.getGameTime() + ticks);
    }

    private static void forgetOldIgnoredWeapons(ServerLevel level, SiegeCrewState state) {
        long now = level.getGameTime();
        state.ignoredWeapons.values().removeIf(until -> until <= now);
    }

    private static float normalizeYaw(float yaw) {
        return (yaw % 360.0F + 360.0F) % 360.0F;
    }
}
