package com.xxsx.earthphysicalsciences;

import java.util.List;
import java.util.Locale;

public final class PhysicalScienceMath {
    public static final double HIGH_VACUUM_KPA = 0.001;
    public static final int PUMPDOWN_TICKS_PER_BLOCK = 20;
    public static final int CRYO_BOILOFF_TICKS = 1200;
    public static final int LN2_K = 77;
    public static final int LH2_K = 20;
    public static final int LHE_K = 4;
    public static final int SC_CABLE_CAPACITY = 50000;
    public static final int SC_CABLE_FALLBACK_CAPACITY = 2000;
    public static final double SC_CABLE_FALLBACK_LOSS = 0.01;
    public static final int FUSION_DT_IGNITION_K = 100000000;
    public static final int FUSION_DD_IGNITION_K = 150000000;
    public static final int FUSION_DT_OUTPUT = 20000;
    public static final int FUSION_DD_OUTPUT = 4000;
    public static final double CONTAINMENT_IGNITE = 0.90;
    public static final double CONTAINMENT_TRIP = 0.50;
    public static final double WAFER_BASE_YIELD = 0.55;
    public static final int CLEANROOM_TIER_MAX = 6;
    public static final int RADIO_RANGE = 512;
    public static final int ANTENNA_BONUS = 256;
    public static final int LEO_DELTA_V = 9400;
    public static final int REENTRY_TILES_MIN = 4;
    public static final int SAT_MAX = 64;
    public static final int ORBIT_AIR_TICKS = 300;
    public static final long ANTIMATTER_UNIT_EOU = 40000000L;
    public static final long ANTIMATTER_COST_EOU = 240000000L;
    public static final int WARP_BLOCKS_PER_UNIT = 2000;
    public static final int ANDROID_MAX_PER_OWNER = 8;

    private PhysicalScienceMath() {}

    public static int gradeRank(String grade) {
        if (grade == null) return 1;
        return switch (grade.toLowerCase(Locale.ROOT)) {
            case "crude" -> 0;
            case "common" -> 1;
            case "fine" -> 2;
            case "masterwork" -> 3;
            case "heirloom" -> 4;
            default -> 1;
        };
    }

    public static String rankToGrade(int rank) {
        return switch (Math.max(0, Math.min(4, rank))) {
            case 0 -> "crude";
            case 1 -> "common";
            case 2 -> "fine";
            case 3 -> "masterwork";
            default -> "heirloom";
        };
    }

    public static double capacityMultiplier(String grade) {
        return switch (gradeRank(grade)) {
            case 0 -> 0.75;
            case 1 -> 1.00;
            case 2 -> 1.20;
            case 3 -> 1.35;
            default -> 1.50;
        };
    }

    public static double ratingRatio(String grade) {
        return switch (gradeRank(grade)) {
            case 0 -> 0.50;
            case 1 -> 0.67;
            case 2 -> 0.80;
            case 3 -> 0.90;
            default -> 1.00;
        };
    }

    public static String deriveGrade(String metricKind, double metric) {
        String kind = metricKind == null ? "accuracy" : metricKind.toLowerCase(Locale.ROOT);
        return switch (kind) {
            case "accuracy" -> {
                if (metric > 0.10) yield "crude";
                if (metric >= 0.05) yield "common";
                if (metric >= 0.02) yield "fine";
                if (metric >= 0.005) yield "masterwork";
                yield "heirloom";
            }
            case "purity_nines" -> {
                if (metric < 5.0) yield "crude";
                if (metric < 6.0) yield "common";
                if (metric < 7.0) yield "fine";
                if (metric < 9.0) yield "masterwork";
                yield "heirloom";
            }
            case "isotopic_purity" -> {
                if (metric < 0.90) yield "crude";
                if (metric < 0.98) yield "common";
                if (metric < 0.995) yield "fine";
                if (metric < 0.999) yield "masterwork";
                yield "heirloom";
            }
            case "yield" -> {
                if (metric < 0.30) yield "crude";
                if (metric <= 0.55) yield "common";
                if (metric <= 0.75) yield "fine";
                if (metric < 0.90) yield "masterwork";
                yield "heirloom";
            }
            case "efficiency" -> {
                if (metric < 0.50) yield "crude";
                if (metric < 0.70) yield "common";
                if (metric < 0.85) yield "fine";
                if (metric < 0.95) yield "masterwork";
                yield "heirloom";
            }
            case "integrity" -> {
                if (metric < 0.60) yield "crude";
                if (metric < 0.80) yield "common";
                if (metric < 0.90) yield "fine";
                if (metric < 0.97) yield "masterwork";
                yield "heirloom";
            }
            default -> "common";
        };
    }

    public static double metricBoundaryForCap(String metricKind, String capGrade) {
        int r = gradeRank(capGrade);
        String kind = metricKind == null ? "accuracy" : metricKind.toLowerCase(Locale.ROOT);
        return switch (kind) {
            case "accuracy" -> switch (r) {
                case 0 -> 0.12;
                case 1 -> 0.05;
                case 2 -> 0.02;
                case 3 -> 0.005;
                default -> 0.002;
            };
            case "purity_nines" -> switch (r) {
                case 0 -> 4.9;
                case 1 -> 5.9;
                case 2 -> 6.9;
                case 3 -> 8.9;
                default -> 9.5;
            };
            case "isotopic_purity" -> switch (r) {
                case 0 -> 0.89;
                case 1 -> 0.98;
                case 2 -> 0.995;
                case 3 -> 0.999;
                default -> 0.9995;
            };
            case "yield" -> switch (r) {
                case 0 -> 0.29;
                case 1 -> 0.55;
                case 2 -> 0.75;
                case 3 -> 0.89;
                default -> 0.92;
            };
            case "efficiency" -> switch (r) {
                case 0 -> 0.49;
                case 1 -> 0.70;
                case 2 -> 0.85;
                case 3 -> 0.95;
                default -> 0.98;
            };
            case "integrity" -> switch (r) {
                case 0 -> 0.59;
                case 1 -> 0.80;
                case 2 -> 0.90;
                case 3 -> 0.97;
                default -> 0.99;
            };
            default -> 0.5;
        };
    }

    public record QualityResult(String grade, double metric) {}

    public static QualityResult evaluateDeviceQuality(String metricKind, double rawMetric, boolean unlockedNode, boolean hasDisciplineCap) {
        String derived = deriveGrade(metricKind, rawMetric);
        String cap = !unlockedNode ? "common" : (hasDisciplineCap ? "heirloom" : "masterwork");
        if (gradeRank(derived) > gradeRank(cap)) {
            return new QualityResult(cap, metricBoundaryForCap(metricKind, cap));
        }
        return new QualityResult(derived, rawMetric);
    }

    public static String spectroscopeReadout(String speciesId, boolean hasMetal, boolean excitable, boolean learnedQuantum, String grade) {
        if (!excitable) return "unexcitable";
        if (!learnedQuantum) {
            return hasMetal ? "contains_metal" : "non_metal_only";
        }
        int decimals = switch (gradeRank(grade)) {
            case 0, 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
        String fmt = "%." + decimals + "f%%";
        return speciesId + ": Fe=" + String.format(Locale.ROOT, fmt, hasMetal ? 62.5 : 0.0)
                + ", Si=" + String.format(Locale.ROOT, fmt, hasMetal ? 18.2 : 46.7)
                + ", O=" + String.format(Locale.ROOT, fmt, hasMetal ? 19.3 : 53.3);
    }

    public static double calculateContainment(int activeCoils) {
        int clamped = Math.max(0, Math.min(24, activeCoils));
        return Math.min(0.98, 0.50 + 0.02 * clamped);
    }

    public static int calculateFusionOutput(String mode, String avgCoilGrade) {
        int base = "dd".equalsIgnoreCase(mode) ? FUSION_DD_OUTPUT : FUSION_DT_OUTPUT;
        return (int) Math.round(base * ratingRatio(avgCoilGrade));
    }

    public static int superconductingCableCapacity(boolean cooled) {
        return cooled ? SC_CABLE_CAPACITY : SC_CABLE_FALLBACK_CAPACITY;
    }

    public static double superconductingCableLoss(boolean cooled) {
        return cooled ? 0.0 : SC_CABLE_FALLBACK_LOSS;
    }

    public static double calculateWaferYield(int cleanroomTier, double waferPurityNines, boolean learnedIc) {
        int tier = Math.max(0, Math.min(CLEANROOM_TIER_MAX, cleanroomTier));
        double base = Math.min(0.85, WAFER_BASE_YIELD + 0.05 * tier);
        double mod = waferPurityNines >= 7.0 ? 0.05 : 0.0;
        double total = Math.min(0.95, base + mod);
        if (!learnedIc) {
            total = Math.min(0.55, total);
        }
        return Math.round(total * 1000.0) / 1000.0;
    }

    public static int calculateRadioRange(int antennaMasts, boolean hasCommSatelliteAndGroundStation) {
        if (hasCommSatelliteAndGroundStation) return Integer.MAX_VALUE;
        int masts = Math.max(0, Math.min(15, antennaMasts));
        return Math.min(4096, RADIO_RANGE + ANTENNA_BONUS * masts);
    }

    public record StageSpec(int engines, int fullTanks, int frames, int isp) {}

    public static int calculateRocketDeltaV(List<StageSpec> stagesTopDown, double payloadMassTons) {
        double upperMass = payloadMassTons;
        double totalDv = 0.0;
        for (StageSpec st : stagesTopDown) {
            double dryMass = st.engines() * 3.0 + st.fullTanks() * 2.0 + st.frames() * 1.0;
            double propMass = st.fullTanks() * 20.0;
            double m0 = upperMass + dryMass + propMass;
            double mf = upperMass + dryMass;
            if (mf > 0.0 && m0 > mf) {
                totalDv += st.isp() * 9.81 * Math.log(m0 / mf);
            }
            upperMass = m0;
        }
        return (int) Math.round(totalDv);
    }

    public static int requiredDeltaV(String destination) {
        if (destination == null) return LEO_DELTA_V;
        return switch (destination.toLowerCase(Locale.ROOT)) {
            case "high_orbit" -> 13000;
            case "moon" -> 12500;
            case "mars" -> 13200;
            case "asteroid_belt" -> 14000;
            default -> LEO_DELTA_V;
        };
    }

    public static boolean canSurviveReentry(List<Double> tileIntegrities) {
        int valid = 0;
        for (double integrity : tileIntegrities) {
            if (integrity >= 0.30) valid++;
        }
        return valid >= REENTRY_TILES_MIN;
    }

    public static double decaySatelliteHealth(double currentHealth, long elapsedTicks) {
        long steps = elapsedTicks / 24000L;
        return Math.max(0.0, Math.round((currentHealth - 0.01 * steps) * 1000.0) / 1000.0);
    }

    public static double metamaterialRefractiveIndex(String grade) {
        return switch (gradeRank(grade)) {
            case 0 -> 1.5;
            case 1 -> 0.875;
            case 2 -> 0.25;
            case 3 -> -0.375;
            default -> -1.0;
        };
    }

    public static boolean canFormWarpBubble(List<Double> shellRefractiveIndices) {
        if (shellRefractiveIndices == null || shellRefractiveIndices.size() < 4) return false;
        for (int i = 0; i < 4; i++) {
            if (shellRefractiveIndices.get(i) >= 0.0) return false;
        }
        return true;
    }

    public static int calculateWarpAntimatterCost(double horizontalDistance, boolean crossDimension) {
        double effDist = crossDimension ? horizontalDistance * 2.0 : horizontalDistance;
        return Math.max(1, (int) Math.ceil(effDist / WARP_BLOCKS_PER_UNIT));
    }

    public static double zoneRefinerMaxNines(String machineGrade) {
        return switch (gradeRank(machineGrade)) {
            case 0 -> 5.5;
            case 1 -> 6.5;
            case 2 -> 7.5;
            case 3 -> 9.0;
            default -> 10.0;
        };
    }
}

