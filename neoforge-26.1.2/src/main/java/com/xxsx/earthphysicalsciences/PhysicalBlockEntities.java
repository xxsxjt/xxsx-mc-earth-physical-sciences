package com.xxsx.earthphysicalsciences;

import com.xxsx.earthlore.api.LoreMachines;
import com.xxsx.earthonminecraft.ElectricEnergyStorage;
import com.xxsx.earthonminecraft.EnergyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PhysicalBlockEntities {
    private PhysicalBlockEntities() {}

    public static class PhysicalMachineBlockEntity extends BlockEntity implements MenuProvider, ElectricEnergyStorage {
        private final SimpleContainer items = new SimpleContainer(6) {
            @Override
            public void setChanged() {
                super.setChanged();
                PhysicalMachineBlockEntity.this.setChanged();
            }
        };
        private int energyStored = 0;
        private int maxEnergy = 400000;
        private PhysicalComponents.DeviceQualityData deviceQuality = PhysicalComponents.DeviceQualityData.DEFAULT;
        private String ownerUuid = "";
        private String mode = "default";
        private String stateName = "idle";
        private String fault = "";
        private int progress = 0;
        private int maxProgress = 200;
        private int temperatureK = 293;
        private int antimatter = 0;
        private int oxygenMb = 0;
        private int deuterium = 0;
        private int tritium = 0;
        private int passes = 1;
        private int cleanroomTier = 0;
        private double containment = 0.0;

        private final ContainerData data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> energyStored;
                    case 3 -> maxEnergy;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) progress = value;
                else if (index == 1) maxProgress = value;
                else if (index == 2) energyStored = value;
                else if (index == 3) maxEnergy = value;
            }

            @Override
            public int getCount() {
                return 4;
            }
        };

        public PhysicalMachineBlockEntity(BlockPos pos, BlockState state) {
            super(EarthPhysicalSciences.PHYSICAL_MACHINE_BE.get(), pos, state);
        }

        public String getMachineKind() {
            if (getBlockState().getBlock() instanceof PhysicalBlocks.BaseMachineBlock b) {
                return b.getMachineKind();
            }
            return "machine";
        }

        public void setOwnerUuid(String ownerUuid) {
            this.ownerUuid = ownerUuid == null ? "" : ownerUuid;
            setChanged();
        }

        public void setDeviceQuality(PhysicalComponents.DeviceQualityData q) {
            this.deviceQuality = q == null ? PhysicalComponents.DeviceQualityData.DEFAULT : q;
            setChanged();
        }

        public PhysicalComponents.DeviceQualityData getDeviceQuality() {
            return deviceQuality;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getEnergyCapacity() {
            return maxEnergy;
        }

        public int getMaxEnergy() {
            return maxEnergy;
        }

        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            int accepted = Math.max(0, Math.min(amount, maxEnergy - energyStored));
            if (!simulate && accepted > 0) {
                energyStored += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            int extracted = Math.max(0, Math.min(amount, energyStored));
            if (!simulate && extracted > 0) {
                energyStored -= extracted;
                setChanged();
            }
            return extracted;
        }

        private boolean consumeEnergy(int eou) {
            if (level != null && energyStored < maxEnergy) {
                int pulled = EnergyNetwork.extractUpTo(level, worldPosition, Math.min(Math.max(eou * 4, 64), maxEnergy - energyStored));
                if (pulled > 0) energyStored += pulled;
            }
            if (energyStored >= eou) {
                energyStored -= eou;
                setChanged();
                return true;
            }
            return level != null && EnergyNetwork.consume(level, worldPosition, eou);
        }

        public String getFault() {
            return fault;
        }

        public SimpleContainer getItems() {
            return items;
        }

        public boolean hasAdjacentVacuumPort() {
            if (level == null) return false;
            for (Direction dir : Direction.values()) {
                BlockEntity adj = level.getBlockEntity(worldPosition.relative(dir));
                if (adj instanceof PhysicalMachineBlockEntity mbe && "vacuum_pump".equals(mbe.getMachineKind())) {
                    if (mbe.getEnergyStored() >= 5 || mbe.progress > 0) return true;
                }
            }
            return false;
        }

        public boolean onRightClick(ServerPlayer sp) {
            String kind = getMachineKind();
            if ("crew_capsule".equals(kind)) {
                List<Double> tiles = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    ItemStack st = items.getItem(i);
                    if (st.is(EarthPhysicalSciences.HEAT_SHIELD_TILE.get())) {
                        double metric = PhysicalItems.getQuality(st, "integrity").metric();
                        for (int c = 0; c < st.getCount(); c++) tiles.add(metric);
                    }
                }
                if (!PhysicalScienceMath.canSurviveReentry(tiles)) {
                    fault = "reentry_burnup";
                    sp.sendOverlayMessage(PhysicalItems.failMsg("reentry_burnup"));
                } else {
                    fault = "";
                    sp.sendOverlayMessage(PhysicalItems.infoMsg("reentry_safe"));
                }
                setChanged();
                return true;
            }
            if ("warp_core".equals(kind) && sp.isShiftKeyDown()) {
                List<Double> indices = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    ItemStack st = items.getItem(i);
                    if (st.is(EarthPhysicalSciences.METAMATERIAL.get())) {
                        indices.add(PhysicalItems.MetamaterialItem.getRefractiveIndex(st));
                    }
                }
                if (!PhysicalScienceMath.canFormWarpBubble(indices)) {
                    fault = "bubble_unstable";
                    sp.sendOverlayMessage(PhysicalItems.failMsg("bubble_unstable"));
                } else {
                    fault = "";
                    sp.sendOverlayMessage(PhysicalItems.infoMsg("warp_ready"));
                }
                setChanged();
                return true;
            }
            return false;
        }

        public void serverTick() {
            if (!(level instanceof ServerLevel sl)) return;
            String kind = getMachineKind();

            if ("photovoltaic_panel".equals(kind)) {
                boolean orbit = sl.dimension().equals(EarthPhysicalSciences.LOW_EARTH_ORBIT);
                boolean day = sl.isBrightOutside();
                if (orbit || (day && sl.canSeeSky(worldPosition.above()))) {
                    double ratio = PhysicalScienceMath.ratingRatio(deviceQuality.grade());
                    int out = (int) Math.round(30 * ratio * (orbit ? 1.4 : (sl.isRaining() ? 0.3 : 1.0)));
                    receiveEnergy(out, false);
                }
                return;
            }

            if ("atomic_clock".equals(kind)) {
                if (!consumeEnergy(2)) {
                    fault = "no_power";
                } else {
                    fault = "";
                }
                return;
            }

            if ("zone_refiner".equals(kind)) {
                tickZoneRefiner();
                return;
            }

            if ("nano_assembler".equals(kind)) {
                tickNanoAssembler();
                return;
            }

            if ("penning_trap".equals(kind)) {
                if (antimatter > 0 && (!consumeEnergy(500) || !hasAdjacentVacuumPort())) {
                    antimatter = 0;
                    fault = "vacuum_lost";
                    setChanged();
                }
                return;
            }

            if ("launch_pad".equals(kind)) {
                if (LoreMachines.check(this, getBlockState().getBlock()).locked()) {
                    fault = "lore_locked";
                }
                return;
            }

            // Generic progress tick for active machines with input in slot 0
            ItemStack in0 = items.getItem(0);
            if (!in0.isEmpty() && consumeEnergy(20)) {
                if ("vacuum_furnace".equals(kind) && "vacuum".equals(mode) && !hasAdjacentVacuumPort()) {
                    fault = "vacuum_lost";
                    return;
                }
                progress++;
                fault = "";
                if (progress >= maxProgress) {
                    progress = 0;
                }
                setChanged();
            }
        }

        private void tickZoneRefiner() {
            ItemStack in0 = items.getItem(0);
            if (in0.isEmpty()) return;
            Identifier inId = BuiltInRegistries.ITEM.getKey(in0.getItem());
            Identifier polyId = Identifier.fromNamespaceAndPath("earth_on_minecraft", "polysilicon");
            Identifier hpsId = Identifier.fromNamespaceAndPath("earth_on_minecraft", "high_purity_silicon");
            if (inId.equals(polyId) && in0.getCount() >= 2) {
                if (!consumeEnergy(400)) {
                    fault = "no_power";
                    return;
                }
                progress++;
                if (progress >= 600 * Math.max(1, passes)) {
                    progress = 0;
                    in0.shrink(2);
                    Item outItem = BuiltInRegistries.ITEM.getValue(hpsId);
                    if (outItem != Items.AIR) {
                        ItemStack out = new ItemStack(outItem, 1);
                        double maxNines = PhysicalScienceMath.zoneRefinerMaxNines(deviceQuality.grade());
                        double metric = Math.min(5.0 + passes, maxNines);
                        String grade = PhysicalScienceMath.deriveGrade("purity_nines", metric);
                        out.set(EarthPhysicalSciences.DEVICE_QUALITY.get(),
                                new PhysicalComponents.DeviceQualityData(1, grade, "purity_nines", metric, ""));
                        items.setItem(3, out);
                    }
                }
                setChanged();
            }
        }

        private void tickNanoAssembler() {
            ItemStack sample = items.getItem(0);
            if (sample.isEmpty()) return;
            if (canReplicateInNanoAssembler(sample) != null) {
                fault = canReplicateInNanoAssembler(sample);
                return;
            }
            if (LoreMachines.check(this, getBlockState().getBlock()).locked()) {
                fault = "lore_locked";
                return;
            }
            fault = "";
        }

        public static String canReplicateInNanoAssembler(ItemStack sample) {
            if (sample.isEmpty()) return "no_pattern";
            if (sample.is(EarthPhysicalSciences.NANO_BLACKLIST)) return "blacklisted";
            Identifier id = BuiltInRegistries.ITEM.getKey(sample.getItem());
            if ("earth_lore".equals(id.getNamespace())) return "blacklisted";
            Identifier pharmaId = Identifier.fromNamespaceAndPath("earth_on_minecraft", "pharmaceutical");
            DataComponentType<?> pharmaType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(pharmaId);
            if (pharmaType != null && sample.get(pharmaType) != null) return "blacklisted";
            Identifier loreTextId = Identifier.fromNamespaceAndPath("earth_lore", "lore_text");
            DataComponentType<?> loreTextType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(loreTextId);
            if (loreTextType != null && sample.get(loreTextType) != null) return "blacklisted";
            if (sample.is(Items.WRITTEN_BOOK) || sample.is(EarthPhysicalSciences.ANTIMATTER_CELL.get())
                    || sample.is(EarthPhysicalSciences.STELLAR_ALLOY.get()) || sample.is(EarthPhysicalSciences.METAMATERIAL.get())) {
                return "blacklisted";
            }
            return null;
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            ContainerHelper.saveAllItems(output, items.getItems());
            output.putInt("energy", energyStored);
            output.putString("owner", ownerUuid);
            output.putString("mode", mode);
            output.putString("state", stateName);
            output.putString("fault", fault);
            output.putInt("progress", progress);
            output.putInt("max_progress", maxProgress);
            output.putInt("temperature_k", temperatureK);
            output.putInt("antimatter", antimatter);
            output.putInt("oxygen_mb", oxygenMb);
            output.putInt("deuterium", deuterium);
            output.putInt("tritium", tritium);
            output.putInt("passes", passes);
            output.putString("dq_grade", deviceQuality.grade());
            output.putString("dq_kind", deviceQuality.metricKind());
            output.putDouble("dq_metric", deviceQuality.metric());
        }

        @Override
        protected void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            ContainerHelper.loadAllItems(input, items.getItems());
            energyStored = input.getIntOr("energy", 0);
            ownerUuid = input.getStringOr("owner", "");
            mode = input.getStringOr("mode", "default");
            stateName = input.getStringOr("state", "idle");
            fault = input.getStringOr("fault", "");
            progress = input.getIntOr("progress", 0);
            maxProgress = input.getIntOr("max_progress", 200);
            temperatureK = input.getIntOr("temperature_k", 293);
            antimatter = input.getIntOr("antimatter", 0);
            oxygenMb = input.getIntOr("oxygen_mb", 0);
            deuterium = input.getIntOr("deuterium", 0);
            tritium = input.getIntOr("tritium", 0);
            passes = input.getIntOr("passes", 1);
            String g = input.getStringOr("dq_grade", "common");
            String k = input.getStringOr("dq_kind", "accuracy");
            double m = input.getDoubleOr("dq_metric", 0.075);
            deviceQuality = new PhysicalComponents.DeviceQualityData(1, g, k, m, "");
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("block.earth_physical_sciences." + getMachineKind());
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new PhysicalMenus.PhysicalMachineMenu(containerId, playerInventory, items, data);
        }
    }
}

