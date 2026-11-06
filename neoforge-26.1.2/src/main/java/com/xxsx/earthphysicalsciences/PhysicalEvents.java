package com.xxsx.earthphysicalsciences;

import com.xxsx.earthlore.data.LoreText;
import com.xxsx.earthlore.registry.LoreContent;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PhysicalEvents {
    private PhysicalEvents() {}

    public static void register() {
        var bus = NeoForge.EVENT_BUS;
        bus.addListener(ItemTooltipEvent.class, PhysicalEvents::onItemTooltip);
        bus.addListener(PlayerTickEvent.Post.class, PhysicalEvents::onPlayerTick);
        bus.addListener(LevelTickEvent.Post.class, PhysicalEvents::onLevelTick);
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        boolean isAxiom = (stack.getItem() instanceof PhysicalItems.QualityDeviceItem q && q.isAxiom())
                || (stack.getItem() instanceof PhysicalItems.AxiomBlockItem);
        if (isAxiom) {
            event.getToolTip().add(Component.translatable("tooltip.earth_physical_sciences.axiom").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        PhysicalComponents.DeviceQualityData dq = stack.get(EarthPhysicalSciences.DEVICE_QUALITY.get());
        if (dq != null) {
            event.getToolTip().add(Component.literal("Grade: " + dq.grade() + " (" + dq.metricKind() + "=" + dq.metric() + ")").withStyle(ChatFormatting.AQUA));
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!sp.level().dimension().equals(EarthPhysicalSciences.LOW_EARTH_ORBIT)) return;
        ItemStack head = sp.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = sp.getItemBySlot(EquipmentSlot.CHEST);
        boolean suited = head.is(EarthPhysicalSciences.SPACE_HELMET.get()) && chest.is(EarthPhysicalSciences.SPACE_SUIT.get());
        if (suited) {
            int ox = chest.getOrDefault(EarthPhysicalSciences.CHARGE.get(), 6000);
            if (ox > 0) {
                chest.set(EarthPhysicalSciences.CHARGE.get(), ox - 1);
                return;
            }
        }
        BlockPos p = sp.blockPosition();
        for (BlockPos cur : BlockPos.betweenClosed(p.offset(-3, -3, -3), p.offset(3, 3, 3))) {
            if (sp.level().getBlockState(cur).is(EarthPhysicalSciences.STATION_CORE.get())
                    || sp.level().getBlockState(cur).is(EarthPhysicalSciences.LIFE_SUPPORT_UNIT.get())
                    || sp.level().getBlockState(cur).is(EarthPhysicalSciences.CREW_CAPSULE.get())) {
                return;
            }
        }
        sp.setAirSupply(Math.max(-20, sp.getAirSupply() - 1));
        if (sp.getAirSupply() <= 0 && sp.level().getGameTime() % 20 == 0) {
            sp.hurtServer((ServerLevel) sp.level(), sp.damageSources().drown(), 2.0F);
            sp.sendOverlayMessage(PhysicalItems.failMsg("no_atmosphere"));
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel sl && sl.dimension().equals(ServerLevel.OVERWORLD)) {
            if (sl.getGameTime() > 0 && sl.getGameTime() % 24000L == 0) {
                OrbitalRegistrySavedData.get(sl).tickDecay24000();
            }
        }
    }

    private static ItemStack makeNote(Identifier nodeId, String author) {
        ItemStack note = new ItemStack(LoreContent.RESEARCH_NOTES.get(), 1);
        Identifier disc = Identifier.fromNamespaceAndPath("earth_lore", nodeId.getPath().startsWith("ast/") ? "astronomy" : "physics");
        note.set(LoreContent.LORE_TEXT.get(), new LoreText(List.of(nodeId), disc, LoreText.FORM_NOTES, Optional.empty(), author, 1));
        return note;
    }

    public static void addProfessionTrades(Villager villager, int level) {
        var prof = villager.getVillagerData().profession();
        MerchantOffers offers = villager.getOffers();
        if (prof.is(EarthPhysicalSciences.PHYSICIST.getId())) {
            switch (level) {
                case 1 -> {
                    offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 12), makeNote(PhysicalLoreIntegration.K_PHY_01, "Physicist"), 4, 5, 0.05F));
                    offers.add(new MerchantOffer(new ItemCost(Items.GLASS, 8), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
                }
                case 3 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(EarthPhysicalSciences.GROUND_LENS.get(), 1), 12, 10, 0.05F));
                case 4 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 4), new ItemStack(EarthPhysicalSciences.ARGON.get(), 2), 12, 15, 0.05F));
                case 5 -> {
                    ItemStack flask = new ItemStack(EarthPhysicalSciences.DEWAR_FLASK.get(), 1);
                    flask.set(EarthPhysicalSciences.DEWAR_CONTENTS.get(), new PhysicalComponents.DewarContentsData("liquid_nitrogen", 4));
                    offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 10), flask, 4, 30, 0.05F));
                }
            }
        } else if (prof.is(EarthPhysicalSciences.ELECTRONICS_ENGINEER.getId())) {
            switch (level) {
                case 2 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(EarthPhysicalSciences.VACUUM_TUBE.get(), 2), 12, 5, 0.05F));
                case 3 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 4), new ItemStack(EarthPhysicalSciences.TRANSISTOR.get(), 8), 12, 10, 0.05F));
                case 4 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(EarthPhysicalSciences.PROGRAM_CARD.get(), 1), 8, 15, 0.05F));
                case 5 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 8), new ItemStack(EarthPhysicalSciences.CIRCUIT_BOARD.get(), 2), 8, 30, 0.05F));
            }
        } else if (prof.is(EarthPhysicalSciences.ASTRONOMER.getId())) {
            switch (level) {
                case 1 -> {
                    offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 8), makeNote(PhysicalLoreIntegration.K_AST_01, "Astronomer"), 4, 5, 0.05F));
                    offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(EarthPhysicalSciences.STAR_CHART.get(), 1), 12, 2, 0.05F));
                }
                case 2 -> offers.add(new MerchantOffer(new ItemCost(EarthPhysicalSciences.OBSERVATION_PLATE.get(), 2), new ItemStack(Items.EMERALD, 2), 12, 5, 0.05F));
                case 3 -> {
                    ItemStack mirror = new ItemStack(EarthPhysicalSciences.GROUND_LENS.get(), 1);
                    mirror.set(EarthPhysicalSciences.VARIANT_KIND.get(), "mirror");
                    offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 4), mirror, 12, 10, 0.05F));
                }
                case 4 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(EarthPhysicalSciences.OBSERVATION_PLATE.get(), 4), 8, 15, 0.05F));
                case 5 -> offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 16), new ItemStack(EarthPhysicalSciences.DEEP_SPACE_DATA.get(), 1), 4, 30, 0.05F));
            }
        }
    }
}
