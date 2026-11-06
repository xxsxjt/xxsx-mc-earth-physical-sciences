package com.xxsx.earthphysicalsciences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.earthlore.core.Gate;
import com.xxsx.earthlore.core.GraphValidator;
import com.xxsx.earthlore.core.NodeDef;
import com.xxsx.earthlore.core.Prereq;
import com.xxsx.earthlore.core.Reachability;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalSciencesLoreIntegrationTest {
    private static final Set<String> ALL_DISCIPLINES = Set.of(
            "earth_lore:geo", "earth_lore:che", "earth_lore:mat",
            "earth_lore:eng", "earth_lore:agr", "earth_lore:med",
            "earth_lore:bio", "earth_lore:phy", "earth_lore:cmp",
            "earth_lore:ast", "earth_lore:fut", "earth_lore:xua",
            "earth_lore:mag", "earth_lore:the"
    );

    private static final GraphValidator.DependencyRule FULL_DEPS = (owner, other) ->
            owner.equals(other) || "earth_lore".equals(other) || "earth_on_minecraft".equals(other)
                    || ("earth_life_sciences".equals(owner) && "earth_physical_sciences".equals(other));

    @Test
    void allSixtyTwoPhysicalNodesAndFullEarthSeriesGraphValidateWithZeroUnreachable() throws IOException {
        List<NodeDef> physNodes = loadNodes(
                Path.of("src/main/resources/data/earth_physical_sciences/earth_lore/knowledge_node"),
                "earth_physical_sciences"
        );
        assertEquals(62, physNodes.size(), "Expected 20 PHY + 16 CMP + 15 AST + 11 FUT = 62 nodes");

        List<NodeDef> eomNodes = loadNodes(
                Path.of("../../earth_on_minecraft/neoforge-26.2/src/main/resources/data/earth_on_minecraft/earth_lore/knowledge_node"),
                "earth_on_minecraft"
        );
        List<NodeDef> lifeNodes = loadNodes(
                Path.of("../../earth_life_sciences/neoforge-26.1.2/src/main/resources/data/earth_life_sciences/earth_lore/knowledge_node"),
                "earth_life_sciences"
        );
        List<NodeDef> xuanNodes = loadNodes(
                Path.of("../../xuanhuan_earth/neoforge-26.2/src/main/resources/data/xuanhuan_earth/earth_lore/knowledge_node"),
                "xuanhuan_earth"
        );
        List<NodeDef> fanNodes = loadNodes(
                Path.of("../../fantasy_earth/neoforge-26.2/src/main/resources/data/fantasy_earth/earth_lore/knowledge_node"),
                "fantasy_earth"
        );
        List<NodeDef> theoNodes = loadNodes(
                Path.of("../../earth_theology/neoforge-26.1.2/src/main/resources/data/earth_theology/earth_lore/knowledge_node"),
                "earth_theology"
        );

        // 1. Minimal boot: earth_on_minecraft + earth_physical_sciences
        List<NodeDef> minimal = new ArrayList<>();
        minimal.addAll(eomNodes);
        minimal.addAll(physNodes);
        GraphValidator.Result minVal = GraphValidator.validate(minimal, ALL_DISCIPLINES, FULL_DEPS);
        assertTrue(minVal.rejected().isEmpty(), () -> "Rejected in minimal: " + minVal.rejected());
        Reachability.Report minReach = Reachability.compute(minVal.accepted());
        assertTrue(minReach.unreachable().isEmpty(), () -> "Unreachable in minimal: " + minReach.unreachable());

        // 2. Full 14-discipline graph across all Earth Series mods
        List<NodeDef> full = new ArrayList<>();
        full.addAll(eomNodes);
        full.addAll(physNodes);
        full.addAll(lifeNodes);
        full.addAll(xuanNodes);
        full.addAll(fanNodes);
        full.addAll(theoNodes);
        GraphValidator.Result fullVal = GraphValidator.validate(full, ALL_DISCIPLINES, FULL_DEPS);
        assertTrue(fullVal.rejected().isEmpty(), () -> "Rejected in full graph: " + fullVal.rejected());
        Reachability.Report fullReach = Reachability.compute(fullVal.accepted());
        assertTrue(fullReach.unreachable().isEmpty(), () -> "Unreachable in full graph: " + fullReach.unreachable());
        assertEquals(full.size(), fullReach.learnable().size());
    }

    @Test
    void physicalFormulasAndAxiomIsolationMatchSpec() throws IOException {
        // earth_phys_spectroscope_readout
        assertEquals("contains_metal", PhysicalScienceMath.spectroscopeReadout("magnetite", true, true, false, "common"));
        assertTrue(PhysicalScienceMath.spectroscopeReadout("magnetite", true, true, true, "common").contains("Fe=62.5%"));
        assertTrue(PhysicalScienceMath.spectroscopeReadout("magnetite", true, true, true, "masterwork").contains("Fe=62.500%"));

        // earth_phys_device_quality_cap
        PhysicalScienceMath.QualityResult capped = PhysicalScienceMath.evaluateDeviceQuality("accuracy", 0.01, false, false);
        assertEquals("common", capped.grade());
        assertEquals(0.05, capped.metric(), 1e-6);

        // earth_phys_fusion_dt_output & containment
        assertEquals(0.98, PhysicalScienceMath.calculateContainment(24), 1e-6);
        assertEquals(20000, PhysicalScienceMath.calculateFusionOutput("dt", "heirloom"));
        assertEquals(4000, PhysicalScienceMath.calculateFusionOutput("dd", "heirloom"));

        // earth_phys_sc_cable_fallback
        assertEquals(50000, PhysicalScienceMath.superconductingCableCapacity(true));
        assertEquals(0.0, PhysicalScienceMath.superconductingCableLoss(true), 1e-6);
        assertEquals(2000, PhysicalScienceMath.superconductingCableCapacity(false));
        assertEquals(0.01, PhysicalScienceMath.superconductingCableLoss(false), 1e-6);

        // earth_phys_wafer_yield
        assertEquals(0.85, PhysicalScienceMath.calculateWaferYield(6, 6.0, true), 1e-6);
        assertEquals(0.55, PhysicalScienceMath.calculateWaferYield(0, 6.0, true), 1e-6);
        assertEquals(0.55, PhysicalScienceMath.calculateWaferYield(6, 8.0, false), 1e-6);

        // earth_phys_radio_range
        assertEquals(512, PhysicalScienceMath.calculateRadioRange(0, false));
        assertEquals(1024, PhysicalScienceMath.calculateRadioRange(2, false));

        // earth_phys_launch_delta_v: 2 stages (upper: 1 eng, 2 full tanks, 1 frame; lower: 4 eng, 8 full tanks, 2 frames; crew_capsule 6t)
        int dv420 = PhysicalScienceMath.calculateRocketDeltaV(List.of(
                new PhysicalScienceMath.StageSpec(1, 2, 1, 420),
                new PhysicalScienceMath.StageSpec(4, 8, 2, 420)
        ), 6.0);
        assertTrue(dv420 >= PhysicalScienceMath.LEO_DELTA_V, "Expected dv420 >= 9400, got " + dv420);
        int dv380 = PhysicalScienceMath.calculateRocketDeltaV(List.of(
                new PhysicalScienceMath.StageSpec(1, 2, 1, 380),
                new PhysicalScienceMath.StageSpec(4, 8, 2, 380)
        ), 6.0);
        assertTrue(dv380 < PhysicalScienceMath.LEO_DELTA_V, "Expected dv380 < 9400, got " + dv380);

        // earth_phys_reentry
        assertTrue(PhysicalScienceMath.canSurviveReentry(List.of(0.8, 0.7, 0.6, 0.5)));
        assertFalse(PhysicalScienceMath.canSurviveReentry(List.of(0.8, 0.7, 0.6, 0.25)));

        // earth_phys_satellite_health
        assertEquals(0.99, PhysicalScienceMath.decaySatelliteHealth(1.0, 24000L), 1e-6);
        assertEquals(0.0, PhysicalScienceMath.decaySatelliteHealth(0.01, 24000L), 1e-6);

        // earth_phys_warp_bubble
        assertFalse(PhysicalScienceMath.canFormWarpBubble(List.of(0.875, 0.875, 0.875, 0.875)));
        assertTrue(PhysicalScienceMath.canFormWarpBubble(List.of(-0.375, -0.375, -1.0, -1.0)));

        // earth_phys_axiom_isolation: PHY, CMP, AST nodes must not require axiom items
        Set<String> axiomItems = Set.of(
                "earth_physical_sciences:nano_assembler",
                "earth_physical_sciences:metamaterial",
                "earth_physical_sciences:penning_trap",
                "earth_physical_sciences:antimatter_cell",
                "earth_physical_sciences:annihilation_reactor",
                "earth_physical_sciences:warp_core",
                "earth_physical_sciences:warp_beacon",
                "earth_physical_sciences:stellar_alloy",
                "earth_physical_sciences:orbital_collector",
                "earth_physical_sciences:microwave_receiver",
                "earth_physical_sciences:android_assembly_bay"
        );
        for (String sub : List.of("phy", "cmp", "ast")) {
            try (Stream<Path> s = Files.walk(Path.of("src/main/resources/data/earth_physical_sciences/earth_lore/knowledge_node/" + sub))) {
                for (Path p : s.filter(f -> f.toString().endsWith(".json")).toList()) {
                    String txt = Files.readString(p, StandardCharsets.UTF_8);
                    for (String ax : axiomItems) {
                        assertFalse(txt.contains(ax), "Non-future node " + p + " must not reference axiom item " + ax);
                    }
                }
            }
        }

        // MC 26.1.2 dimension_type schema regression guard
        JsonObject dimType = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/data/earth_physical_sciences/dimension_type/low_earth_orbit.json"),
                StandardCharsets.UTF_8
        )).getAsJsonObject();
        assertTrue(dimType.has("has_ender_dragon_fight"), "MC 26.1.2 dimension_type requires has_ender_dragon_fight");
        assertFalse(dimType.has("ultrawarm"), "Legacy 1.21 ultrawarm key must not appear in MC 26.1.2 dimension_type");
    }

    private static List<NodeDef> loadNodes(Path root, String ownerNamespace) throws IOException {
        List<NodeDef> list = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                String rel = root.relativize(file).toString().replace('\\', '/').replaceAll("\\.json$", "");
                String id = ownerNamespace + ":" + rel;
                JsonObject obj = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                String discipline = obj.get("discipline").getAsString();
                int tier = obj.get("tier").getAsInt();
                Gate gate = Gate.parse(obj.get("gate").getAsString()).orElseThrow();
                List<Prereq> requires = new ArrayList<>();
                for (JsonElement reqEl : obj.getAsJsonArray("requires")) {
                    JsonObject reqObj = reqEl.getAsJsonObject();
                    if (reqObj.has("all")) {
                        requires.add(new Prereq.All(reqObj.get("all").getAsString()));
                    } else if (reqObj.has("any")) {
                        List<String> members = new ArrayList<>();
                        JsonArray arr = reqObj.getAsJsonArray("any");
                        arr.forEach(m -> members.add(m.getAsString()));
                        requires.add(new Prereq.Any(members));
                    }
                }
                List<String> unlockKeys = new ArrayList<>();
                for (JsonElement uEl : obj.getAsJsonArray("unlocks")) {
                    JsonObject uObj = uEl.getAsJsonObject();
                    Map.Entry<String, JsonElement> entry = uObj.entrySet().iterator().next();
                    String kind = entry.getKey();
                    String val = entry.getValue().getAsString();
                    unlockKeys.add("capstone".equals(kind) ? ("capstone:" + val + "@" + ownerNamespace) : (kind + ":" + val));
                }
                list.add(new NodeDef(id, discipline, tier, gate, requires, unlockKeys, false));
            }
        }
        return list;
    }
}
