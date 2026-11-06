package com.xxsx.earthphysicalsciences;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class PhysicalEntities {
    private PhysicalEntities() {}

    public static class LaunchVehicleEntity extends Entity {
        private int flightTick = 0;
        private boolean aborted = false;

        public LaunchVehicleEntity(EntityType<? extends LaunchVehicleEntity> type, Level level) {
            super(type, level);
        }

        @Override
        public void tick() {
            super.tick();
            flightTick++;
            if (!aborted) {
                this.setDeltaMovement(new Vec3(0.0, 0.4, 0.0));
                this.setPos(this.getX(), this.getY() + 0.4, this.getZ());
            }
            if (!this.level().isClientSide() && flightTick >= 600) {
                this.discard();
            }
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            return false;
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput input) {
            this.flightTick = input.getIntOr("flight_tick", 0);
            this.aborted = input.getBooleanOr("aborted", false);
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {
            output.putInt("flight_tick", this.flightTick);
            output.putBoolean("aborted", this.aborted);
        }
    }

    public static class AndroidWorkerEntity extends Entity {
        private String ownerUuid = "";
        private String task = "idle";
        private long charge = 5000000L;
        private double integrity = 1.0;

        public AndroidWorkerEntity(EntityType<? extends AndroidWorkerEntity> type, Level level) {
            super(type, level);
        }

        public void setOwnerUuid(String ownerUuid) {
            this.ownerUuid = ownerUuid == null ? "" : ownerUuid;
        }

        public String getOwnerUuid() {
            return ownerUuid;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task == null ? "idle" : task;
        }

        public long getCharge() {
            return charge;
        }

        public double getIntegrity() {
            return integrity;
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.level().isClientSide() && !"idle".equals(task) && this.tickCount % 20 == 0 && charge >= 50) {
                charge -= 50;
            }
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            this.integrity = Math.max(0.0, this.integrity - amount * 0.05);
            if (this.integrity <= 0.0) {
                this.discard();
            }
            return true;
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput input) {
            this.ownerUuid = input.getStringOr("owner", "");
            this.task = input.getStringOr("task", "idle");
            this.charge = input.getLongOr("charge", 5000000L);
            this.integrity = input.getDoubleOr("integrity", 1.0);
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {
            output.putString("owner", this.ownerUuid);
            output.putString("task", this.task);
            output.putLong("charge", this.charge);
            output.putDouble("integrity", this.integrity);
        }
    }
}
