package com.xxsx.earthphysicalsciences;

import com.xxsx.earthlore.api.LoreMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class PhysicalBlocks {
    private PhysicalBlocks() {}

    public static class BaseMachineBlock extends Block implements EntityBlock {
        private final String machineKind;
        private final boolean hasMenu;

        public BaseMachineBlock(BlockBehaviour.Properties properties, String machineKind, boolean hasMenu) {
            super(properties);
            this.machineKind = machineKind;
            this.hasMenu = hasMenu;
        }

        public String getMachineKind() {
            return machineKind;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new PhysicalBlockEntities.PhysicalMachineBlockEntity(pos, state);
        }

        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            if (level.isClientSide() || type != EarthPhysicalSciences.PHYSICAL_MACHINE_BE.get()) return null;
            return (lvl, pos, st, be) -> ((PhysicalBlockEntities.PhysicalMachineBlockEntity) be).serverTick();
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PhysicalBlockEntities.PhysicalMachineBlockEntity mbe) {
                if (placer instanceof ServerPlayer sp) {
                    LoreMachines.setOwner(mbe, sp);
                    mbe.setOwnerUuid(sp.getUUID().toString());
                }
                PhysicalComponents.DeviceQualityData q = stack.get(EarthPhysicalSciences.DEVICE_QUALITY.get());
                if (q != null) {
                    mbe.setDeviceQuality(q);
                }
            }
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PhysicalBlockEntities.PhysicalMachineBlockEntity mbe && player instanceof ServerPlayer sp) {
                if (mbe.onRightClick(sp)) {
                    return InteractionResult.SUCCESS;
                }
                if (hasMenu) {
                    sp.openMenu(mbe, pos);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }
    }

    public static class SuperconductingCableBlock extends Block {
        public static final BooleanProperty COOLED = BooleanProperty.create("cooled");

        public SuperconductingCableBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(COOLED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(COOLED);
        }

        public static int getCapacity(BlockState state) {
            return PhysicalScienceMath.superconductingCableCapacity(state.getValue(COOLED));
        }

        public static double getLossPerBlock(BlockState state) {
            return PhysicalScienceMath.superconductingCableLoss(state.getValue(COOLED));
        }
    }

    public static class SpectroscopeBlock extends BaseMachineBlock {
        public SpectroscopeBlock(BlockBehaviour.Properties p) { super(p, "spectroscope", true); }
    }
    public static class ElectromagnetBlock extends BaseMachineBlock {
        public ElectromagnetBlock(BlockBehaviour.Properties p) { super(p, "electromagnet", false); }
    }
    public static class VacuumPumpBlock extends BaseMachineBlock {
        public VacuumPumpBlock(BlockBehaviour.Properties p) { super(p, "vacuum_pump", true); }
    }
    public static class VacuumFurnaceBlock extends BaseMachineBlock {
        public VacuumFurnaceBlock(BlockBehaviour.Properties p) { super(p, "vacuum_furnace", true); }
    }
    public static class XrayDiffractometerBlock extends BaseMachineBlock {
        public XrayDiffractometerBlock(BlockBehaviour.Properties p) { super(p, "xray_diffractometer", true); }
    }
    public static class IsotopeSeparatorBlock extends BaseMachineBlock {
        public IsotopeSeparatorBlock(BlockBehaviour.Properties p) { super(p, "isotope_separator", true); }
    }
    public static class CryocoolerBlock extends BaseMachineBlock {
        public CryocoolerBlock(BlockBehaviour.Properties p) { super(p, "cryocooler", true); }
    }
    public static class LaserCutterBlock extends BaseMachineBlock {
        public LaserCutterBlock(BlockBehaviour.Properties p) { super(p, "laser_cutter", true); }
    }
    public static class CyclotronBlock extends BaseMachineBlock {
        public CyclotronBlock(BlockBehaviour.Properties p) { super(p, "cyclotron", true); }
    }
    public static class FusionIgniterBlock extends BaseMachineBlock {
        public FusionIgniterBlock(BlockBehaviour.Properties p) { super(p, "fusion_igniter", true); }
    }
    public static class PlasmaContainmentBlock extends BaseMachineBlock {
        public PlasmaContainmentBlock(BlockBehaviour.Properties p) { super(p, "plasma_containment", false); }
    }
    public static class AtomicClockBlock extends BaseMachineBlock {
        public AtomicClockBlock(BlockBehaviour.Properties p) { super(p, "atomic_clock", false); }
    }
    public static class RadioTransceiverBlock extends BaseMachineBlock {
        public RadioTransceiverBlock(BlockBehaviour.Properties p) { super(p, "radio_transceiver", true); }
    }
    public static class PhotovoltaicPanelBlock extends BaseMachineBlock {
        public PhotovoltaicPanelBlock(BlockBehaviour.Properties p) { super(p, "photovoltaic_panel", false); }
    }
    public static class ZoneRefinerBlock extends BaseMachineBlock {
        public ZoneRefinerBlock(BlockBehaviour.Properties p) { super(p, "zone_refiner", true); }
    }
    public static class WaferFabBlock extends BaseMachineBlock {
        public WaferFabBlock(BlockBehaviour.Properties p) { super(p, "wafer_fab", true); }
    }
    public static class MainframeBlock extends BaseMachineBlock {
        public MainframeBlock(BlockBehaviour.Properties p) { super(p, "mainframe", true); }
    }
    public static class RadarStationBlock extends BaseMachineBlock {
        public RadarStationBlock(BlockBehaviour.Properties p) { super(p, "radar_station", true); }
    }
    public static class ReflectingTelescopeBlock extends BaseMachineBlock {
        public ReflectingTelescopeBlock(BlockBehaviour.Properties p) { super(p, "reflecting_telescope", true); }
    }
    public static class LaunchPadBlock extends BaseMachineBlock {
        public LaunchPadBlock(BlockBehaviour.Properties p) { super(p, "launch_pad", true); }
    }
    public static class CrewCapsuleBlock extends BaseMachineBlock {
        public CrewCapsuleBlock(BlockBehaviour.Properties p) { super(p, "crew_capsule", false); }
    }
    public static class GroundStationBlock extends BaseMachineBlock {
        public GroundStationBlock(BlockBehaviour.Properties p) { super(p, "ground_station", true); }
    }
    public static class LifeSupportUnitBlock extends BaseMachineBlock {
        public LifeSupportUnitBlock(BlockBehaviour.Properties p) { super(p, "life_support_unit", true); }
    }
    public static class StationCoreBlock extends BaseMachineBlock {
        public StationCoreBlock(BlockBehaviour.Properties p) { super(p, "station_core", true); }
    }
    public static class NanoAssemblerBlock extends BaseMachineBlock {
        public NanoAssemblerBlock(BlockBehaviour.Properties p) { super(p, "nano_assembler", true); }
    }
    public static class PenningTrapBlock extends BaseMachineBlock {
        public PenningTrapBlock(BlockBehaviour.Properties p) { super(p, "penning_trap", true); }
    }
    public static class AnnihilationReactorBlock extends BaseMachineBlock {
        public AnnihilationReactorBlock(BlockBehaviour.Properties p) { super(p, "annihilation_reactor", true); }
    }
    public static class WarpCoreBlock extends BaseMachineBlock {
        public WarpCoreBlock(BlockBehaviour.Properties p) { super(p, "warp_core", true); }
    }
    public static class WarpBeaconBlock extends BaseMachineBlock {
        public WarpBeaconBlock(BlockBehaviour.Properties p) { super(p, "warp_beacon", false); }
    }
    public static class MicrowaveReceiverBlock extends BaseMachineBlock {
        public MicrowaveReceiverBlock(BlockBehaviour.Properties p) { super(p, "microwave_receiver", false); }
    }
    public static class AndroidAssemblyBayBlock extends BaseMachineBlock {
        public AndroidAssemblyBayBlock(BlockBehaviour.Properties p) { super(p, "android_assembly_bay", true); }
    }
}

