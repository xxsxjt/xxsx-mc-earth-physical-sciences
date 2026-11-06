package com.xxsx.earthphysicalsciences;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PhysicalItems {
    private PhysicalItems() {}

    public static Component failMsg(String code) {
        return Component.translatable("phys.earth_physical_sciences.fail." + code);
    }

    public static Component infoMsg(String key, Object... args) {
        return Component.translatable("phys.earth_physical_sciences.info." + key, args);
    }

    public static PhysicalComponents.DeviceQualityData getQuality(ItemStack stack, String defaultKind) {
        PhysicalComponents.DeviceQualityData d = stack.get(EarthPhysicalSciences.DEVICE_QUALITY.get());
        if (d != null) return d;
        double defMetric = PhysicalScienceMath.metricBoundaryForCap(defaultKind, "common");
        return new PhysicalComponents.DeviceQualityData(1, "common", defaultKind, defMetric, "");
    }

    public static class AxiomBlockItem extends BlockItem {
        public AxiomBlockItem(Block block, Properties properties) {
            super(block, properties);
        }
    }

    public static class QualityDeviceItem extends Item {
        private final String metricKind;
        private final boolean axiom;

        public QualityDeviceItem(Properties properties, String metricKind, boolean axiom) {
            super(properties);
            this.metricKind = metricKind;
            this.axiom = axiom;
        }

        public String getMetricKind() {
            return metricKind;
        }

        public boolean isAxiom() {
            return axiom;
        }
    }

    public static class EchoSounderItem extends QualityDeviceItem {
        public EchoSounderItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.CHARGE.get(), 1000), "accuracy", false);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            int charge = stack.getOrDefault(EarthPhysicalSciences.CHARGE.get(), 1000);
            int cost = player.isShiftKeyDown() ? 20 : 10;
            if (charge < cost) {
                sp.sendOverlayMessage(failMsg("no_charge"));
                return InteractionResult.FAIL;
            }
            stack.set(EarthPhysicalSciences.CHARGE.get(), charge - cost);
            BlockPos pos = player.blockPosition();
            int depth = 0;
            for (int dy = 1; dy <= 24; dy++) {
                if (level.getBlockState(pos.below(dy)).isAir() == player.isShiftKeyDown()) {
                    depth = dy;
                    break;
                }
            }
            if (depth == 0) {
                sp.sendOverlayMessage(failMsg("nothing_detected"));
            } else {
                sp.sendOverlayMessage(infoMsg("echo_depth", depth));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static class MultimeterItem extends QualityDeviceItem {
        public MultimeterItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), "flow"), "accuracy", false);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            Player player = context.getPlayer();
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            ItemStack stack = context.getItemInHand();
            if (player.isShiftKeyDown()) {
                String cur = stack.getOrDefault(EarthPhysicalSciences.VARIANT_KIND.get(), "flow");
                String next = switch (cur) {
                    case "flow" -> "capacity";
                    case "capacity" -> "loss";
                    default -> "flow";
                };
                stack.set(EarthPhysicalSciences.VARIANT_KIND.get(), next);
                sp.sendOverlayMessage(infoMsg("multimeter_mode", next));
                return InteractionResult.SUCCESS;
            }
            BlockEntity be = level.getBlockEntity(context.getClickedPos());
            if (be instanceof PhysicalBlockEntities.PhysicalMachineBlockEntity mbe) {
                boolean learned = PhysicalLoreIntegration.hasNode(sp, PhysicalLoreIntegration.K_CMP_01);
                if (!learned) {
                    sp.sendOverlayMessage(infoMsg("multimeter_coarse", mbe.getEnergyStored() > 0 ? "powered" : "unpowered"));
                } else {
                    sp.sendOverlayMessage(infoMsg("multimeter_precise", mbe.getEnergyStored(), mbe.getMaxEnergy()));
                }
                return InteractionResult.SUCCESS;
            }
            sp.sendOverlayMessage(failMsg("no_energy_port"));
            return InteractionResult.FAIL;
        }
    }

    public static class GroundLensItem extends QualityDeviceItem {
        public GroundLensItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), "convex"), "accuracy", false);
        }
    }

    public static class GeigerCounterItem extends QualityDeviceItem {
        public GeigerCounterItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.CHARGE.get(), 1000), "accuracy", false);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            ItemStack stack = player.getItemInHand(hand);
            int charge = stack.getOrDefault(EarthPhysicalSciences.CHARGE.get(), 1000);
            if (charge <= 0) {
                sp.sendOverlayMessage(failMsg("no_charge"));
                return InteractionResult.FAIL;
            }
            stack.set(EarthPhysicalSciences.CHARGE.get(), charge - 1);
            boolean learned = PhysicalLoreIntegration.hasNode(sp, PhysicalLoreIntegration.K_PHY_08);
            if (!learned) {
                sp.sendOverlayMessage(infoMsg("geiger_coarse", "quiet"));
            } else {
                sp.sendOverlayMessage(infoMsg("geiger_precise", "12 CPM (0.08 uSv/h)"));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static class DewarFlaskItem extends QualityDeviceItem {
        public DewarFlaskItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.DEWAR_CONTENTS.get(), PhysicalComponents.DewarContentsData.EMPTY), "integrity", false);
        }

        public static int maxCapacity(ItemStack flask) {
            PhysicalComponents.DeviceQualityData q = getQuality(flask, "integrity");
            return (int) Math.floor(8.0 * PhysicalScienceMath.capacityMultiplier(q.grade()));
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            ItemStack flask = player.getItemInHand(hand);
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack other = player.getItemInHand(otherHand);
            PhysicalComponents.DewarContentsData data = flask.getOrDefault(EarthPhysicalSciences.DEWAR_CONTENTS.get(), PhysicalComponents.DewarContentsData.EMPTY);
            int cap = maxCapacity(flask);
            if (other.getItem() instanceof CryogenItem cryo) {
                String kind = cryo.getCryoKind();
                if (!"empty".equals(data.contents()) && !data.contents().equals(kind)) {
                    sp.sendOverlayMessage(failMsg("wrong_contents"));
                    return InteractionResult.FAIL;
                }
                if (data.count() >= cap) {
                    sp.sendOverlayMessage(failMsg("flask_full"));
                    return InteractionResult.FAIL;
                }
                other.shrink(1);
                flask.set(EarthPhysicalSciences.DEWAR_CONTENTS.get(), new PhysicalComponents.DewarContentsData(kind, data.count() + 1));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    public static class CryogenItem extends Item {
        private final String cryoKind;

        public CryogenItem(Properties properties, String cryoKind) {
            super(properties);
            this.cryoKind = cryoKind;
        }

        public String getCryoKind() {
            return cryoKind;
        }

        @Override
        public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
            if (level.getGameTime() % PhysicalScienceMath.CRYO_BOILOFF_TICKS == 0 && stack.getCount() > 0) {
                stack.shrink(1);
            }
        }
    }

    public static class SuperconductorCoilItem extends QualityDeviceItem {
        public SuperconductorCoilItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.COIL_STATE.get(), PhysicalComponents.CoilStateData.DEFAULT), "efficiency", false);
        }
    }

    public static class HighDensityCellItem extends QualityDeviceItem {
        public HighDensityCellItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.CHARGE.get(), 0), "efficiency", false);
        }
    }

    public static class VacuumTubeItem extends QualityDeviceItem {
        public VacuumTubeItem(Properties properties) {
            super(properties
                    .component(EarthPhysicalSciences.VARIANT_KIND.get(), "triode")
                    .component(EarthPhysicalSciences.CHARGE.get(), 240000), "efficiency", false);
        }
    }

    public static class ProgramCardItem extends Item {
        public ProgramCardItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), "census"));
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                String cur = stack.getOrDefault(EarthPhysicalSciences.VARIANT_KIND.get(), "census");
                String next = switch (cur) {
                    case "census" -> "scheduler";
                    case "scheduler" -> "launch_guidance";
                    case "launch_guidance" -> "orbit_tracking";
                    default -> "census";
                };
                stack.set(EarthPhysicalSciences.VARIANT_KIND.get(), next);
                sp.sendOverlayMessage(infoMsg("program_job", next));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static class StarChartItem extends Item {
        private static final String[] CONSTELLATIONS = {
                "aries", "taurus", "gemini", "cancer", "leo", "virgo",
                "libra", "scorpius", "sagittarius", "capricornus", "aquarius", "pisces"
        };

        public StarChartItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.STAR_CHART_DATA.get(), PhysicalComponents.StarChartData.EMPTY));
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            if (!level.isDarkOutside()) {
                sp.sendOverlayMessage(failMsg("not_night"));
                return InteractionResult.FAIL;
            }
            if (!level.canSeeSky(player.blockPosition().above())) {
                sp.sendOverlayMessage(failMsg("no_sky"));
                return InteractionResult.FAIL;
            }
            ItemStack stack = player.getItemInHand(hand);
            PhysicalComponents.StarChartData cur = stack.getOrDefault(EarthPhysicalSciences.STAR_CHART_DATA.get(), PhysicalComponents.StarChartData.EMPTY);
            long dayCount = level.getGameTime() / 24000L;
            String cName = CONSTELLATIONS[(int) (Math.abs(dayCount) % CONSTELLATIONS.length)];
            List<String> nextConst = new ArrayList<>(cur.constellations());
            if (!nextConst.contains(cName)) nextConst.add(cName);
            int phase = (int) (dayCount % 8L);
            int nextPhases = cur.moonPhases() | (1 << phase);
            stack.set(EarthPhysicalSciences.STAR_CHART_DATA.get(), new PhysicalComponents.StarChartData(nextConst, nextPhases, cur.bodies()));
            boolean learned = PhysicalLoreIntegration.hasNode(sp, PhysicalLoreIntegration.K_AST_01);
            if (!learned) {
                sp.sendOverlayMessage(infoMsg("star_chart_coarse", cName, phase));
            } else {
                sp.sendOverlayMessage(infoMsg("star_chart_precise", cName, dayCount, (8 - phase) % 8));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static class RocketEngineItem extends QualityDeviceItem {
        public RocketEngineItem(Properties properties) {
            super(properties, "efficiency", false);
        }

        public static int getIsp(ItemStack stack) {
            PhysicalComponents.DeviceQualityData q = getQuality(stack, "efficiency");
            return (int) Math.round(300 + 160 * PhysicalScienceMath.ratingRatio(q.grade()));
        }
    }

    public static class PropellantTankItem extends Item {
        public PropellantTankItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.PROPELLANT_TANK_DATA.get(), PhysicalComponents.PropellantTankData.EMPTY));
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack tank = player.getItemInHand(hand);
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack other = player.getItemInHand(otherHand);
            PhysicalComponents.PropellantTankData data = tank.getOrDefault(EarthPhysicalSciences.PROPELLANT_TANK_DATA.get(), PhysicalComponents.PropellantTankData.EMPTY);
            if (other.is(EarthPhysicalSciences.LIQUID_OXYGEN.get()) && data.loxMb() <= 16000) {
                other.shrink(1);
                tank.set(EarthPhysicalSciences.PROPELLANT_TANK_DATA.get(), new PhysicalComponents.PropellantTankData(Math.min(17000, data.loxMb() + 1000), data.lh2Mb()));
                return InteractionResult.SUCCESS;
            }
            if (other.is(EarthPhysicalSciences.LIQUID_HYDROGEN.get()) && data.lh2Mb() <= 2000) {
                other.shrink(1);
                tank.set(EarthPhysicalSciences.PROPELLANT_TANK_DATA.get(), new PhysicalComponents.PropellantTankData(data.loxMb(), Math.min(3000, data.lh2Mb() + 1000)));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    public static class SpaceHelmetItem extends Item {
        public SpaceHelmetItem(Properties properties) {
            super(properties);
        }
    }

    public static class SpaceSuitItem extends QualityDeviceItem {
        public SpaceSuitItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.CHARGE.get(), 6000), "integrity", false);
        }
    }

    public static class SatelliteItem extends Item {
        public SatelliteItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), "communication"));
        }
    }

    public static class SpaceProbeItem extends Item {
        public SpaceProbeItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), "moon"));
        }
    }

    public static class MetamaterialItem extends QualityDeviceItem {
        public MetamaterialItem(Properties properties) {
            super(properties, "integrity", true);
        }

        public static double getRefractiveIndex(ItemStack stack) {
            PhysicalComponents.DeviceQualityData q = getQuality(stack, "integrity");
            return PhysicalScienceMath.metamaterialRefractiveIndex(q.grade());
        }
    }

    public static class AntimatterCellItem extends QualityDeviceItem {
        public AntimatterCellItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.CHARGE.get(), 0), "integrity", true);
        }
    }

    public static class OrbitalCollectorItem extends QualityDeviceItem {
        public OrbitalCollectorItem(Properties properties) {
            super(properties.component(EarthPhysicalSciences.VARIANT_KIND.get(), ""), "efficiency", true);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (level.getBlockState(pos).is(EarthPhysicalSciences.MICROWAVE_RECEIVER.get())) {
                String link = pos.getX() + "," + pos.getY() + "," + pos.getZ();
                context.getItemInHand().set(EarthPhysicalSciences.VARIANT_KIND.get(), link);
                if (context.getPlayer() instanceof ServerPlayer sp) {
                    sp.sendOverlayMessage(infoMsg("collector_linked", link));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }
}
