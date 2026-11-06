package com.xxsx.earthphysicalsciences;

import com.xxsx.earthlore.api.LoreApi;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PhysicalLoreIntegration {
    public static final Identifier K_PHY_00 = node("phy/physics_basics");
    public static final Identifier K_PHY_01 = node("phy/classical_mechanics");
    public static final Identifier K_PHY_02 = node("phy/acoustics");
    public static final Identifier K_PHY_03 = node("phy/optics");
    public static final Identifier K_PHY_04 = node("phy/electromagnetism");
    public static final Identifier K_PHY_05 = node("phy/quantum_mechanics");
    public static final Identifier K_PHY_06 = node("phy/vacuum_technology");
    public static final Identifier K_PHY_07 = node("phy/xray_physics");
    public static final Identifier K_PHY_08 = node("phy/nuclear_physics");
    public static final Identifier K_PHY_09 = node("phy/condensed_matter");
    public static final Identifier K_PHY_10 = node("phy/cryogenics");
    public static final Identifier K_PHY_11 = node("phy/superconductivity");
    public static final Identifier K_PHY_12 = node("phy/plasma_physics");
    public static final Identifier K_PHY_13 = node("phy/lasers");
    public static final Identifier K_PHY_14 = node("phy/particle_acceleration");
    public static final Identifier K_PHY_15 = node("phy/high_energy_batteries");
    public static final Identifier K_PHY_16 = node("phy/controlled_fusion");
    public static final Identifier K_PHY_17 = node("phy/relativity");
    public static final Identifier K_PHY_18 = node("phy/precision_metrology");
    public static final Identifier K_PHY_19 = node("phy/physics_mastery");

    public static final Identifier K_CMP_00 = node("cmp/electronics_basics");
    public static final Identifier K_CMP_01 = node("cmp/circuit_theory");
    public static final Identifier K_CMP_02 = node("cmp/vacuum_electronics");
    public static final Identifier K_CMP_03 = node("cmp/semiconductor_physics");
    public static final Identifier K_CMP_04 = node("cmp/transistors");
    public static final Identifier K_CMP_05 = node("cmp/integrated_circuits");
    public static final Identifier K_CMP_06 = node("cmp/silicon_refining");
    public static final Identifier K_CMP_07 = node("cmp/digital_computing");
    public static final Identifier K_CMP_08 = node("cmp/software");
    public static final Identifier K_CMP_09 = node("cmp/radio_communication");
    public static final Identifier K_CMP_10 = node("cmp/radar_and_sensing");
    public static final Identifier K_CMP_11 = node("cmp/photonic_computing");
    public static final Identifier K_CMP_12 = node("cmp/quantum_computing");
    public static final Identifier K_CMP_13 = node("cmp/artificial_intelligence");
    public static final Identifier K_CMP_14 = node("cmp/automation_networks");
    public static final Identifier K_CMP_15 = node("cmp/computing_mastery");

    public static final Identifier K_AST_00 = node("ast/astronomy_basics");
    public static final Identifier K_AST_01 = node("ast/positional_astronomy");
    public static final Identifier K_AST_02 = node("ast/telescopic_astronomy");
    public static final Identifier K_AST_03 = node("ast/celestial_mechanics");
    public static final Identifier K_AST_04 = node("ast/astrophysics");
    public static final Identifier K_AST_05 = node("ast/rocket_propulsion");
    public static final Identifier K_AST_06 = node("ast/thermal_protection");
    public static final Identifier K_AST_07 = node("ast/spacecraft_structures");
    public static final Identifier K_AST_08 = node("ast/launch_operations");
    public static final Identifier K_AST_09 = node("ast/satellites");
    public static final Identifier K_AST_10 = node("ast/life_support");
    public static final Identifier K_AST_11 = node("ast/space_station");
    public static final Identifier K_AST_12 = node("ast/orbital_mechanics");
    public static final Identifier K_AST_13 = node("ast/deep_space_exploration");
    public static final Identifier K_AST_14 = node("ast/astronautics_mastery");

    public static final Identifier K_FUT_01 = node("fut/axiom_primer");
    public static final Identifier K_FUT_02 = node("fut/nanofabrication");
    public static final Identifier K_FUT_03 = node("fut/metamaterials");
    public static final Identifier K_FUT_04 = node("fut/antimatter_containment");
    public static final Identifier K_FUT_05 = node("fut/antimatter_energy");
    public static final Identifier K_FUT_06 = node("fut/warp_theory");
    public static final Identifier K_FUT_07 = node("fut/warp_drive");
    public static final Identifier K_FUT_08 = node("fut/interstellar_materials");
    public static final Identifier K_FUT_09 = node("fut/megastructures");
    public static final Identifier K_FUT_10 = node("fut/machine_society");
    public static final Identifier K_FUT_11 = node("fut/future_mastery");

    private PhysicalLoreIntegration() {}

    public static Identifier node(String path) {
        return Identifier.fromNamespaceAndPath(EarthPhysicalSciences.MOD_ID, path);
    }

    public static boolean hasNode(Player player, Identifier nodeId) {
        if (player instanceof ServerPlayer sp) {
            return LoreApi.knows(sp, nodeId);
        }
        return false;
    }

    public static boolean canRead(Player player, Identifier contentId) {
        if (player instanceof ServerPlayer sp) {
            return !LoreApi.check(sp, contentId).locked();
        }
        return false;
    }
}

