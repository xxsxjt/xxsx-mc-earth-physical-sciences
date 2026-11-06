package com.xxsx.earthphysicalsciences;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mod(EarthPhysicalSciences.MOD_ID)
public final class EarthPhysicalSciences {
    public static final String MOD_ID = "earth_physical_sciences";

    public static final ResourceKey<Level> LOW_EARTH_ORBIT = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath(MOD_ID, "low_earth_orbit"));

    public static final TagKey<Item> NANO_BLACKLIST = ItemTags.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "nano_blacklist"));

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // Data Components
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhysicalComponents.DeviceQualityData>> DEVICE_QUALITY =
            DATA_COMPONENTS.registerComponentType("device_quality", b -> b
                    .persistent(PhysicalComponents.DeviceQualityData.CODEC)
                    .networkSynchronized(PhysicalComponents.DeviceQualityData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhysicalComponents.DewarContentsData>> DEWAR_CONTENTS =
            DATA_COMPONENTS.registerComponentType("dewar_contents", b -> b
                    .persistent(PhysicalComponents.DewarContentsData.CODEC)
                    .networkSynchronized(PhysicalComponents.DewarContentsData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhysicalComponents.CoilStateData>> COIL_STATE =
            DATA_COMPONENTS.registerComponentType("coil_state", b -> b
                    .persistent(PhysicalComponents.CoilStateData.CODEC)
                    .networkSynchronized(PhysicalComponents.CoilStateData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhysicalComponents.PropellantTankData>> PROPELLANT_TANK_DATA =
            DATA_COMPONENTS.registerComponentType("propellant_tank_data", b -> b
                    .persistent(PhysicalComponents.PropellantTankData.CODEC)
                    .networkSynchronized(PhysicalComponents.PropellantTankData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhysicalComponents.StarChartData>> STAR_CHART_DATA =
            DATA_COMPONENTS.registerComponentType("star_chart_data", b -> b
                    .persistent(PhysicalComponents.StarChartData.CODEC)
                    .networkSynchronized(PhysicalComponents.StarChartData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            DATA_COMPONENTS.registerComponentType("charge", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> VARIANT_KIND =
            DATA_COMPONENTS.registerComponentType("variant_kind", b -> b
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final List<DeferredItem<? extends Item>> CREATIVE_TAB_ITEMS = new ArrayList<>();
    public static final Map<String, DeferredItem<? extends Item>> ALL_ITEMS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<? extends Block>> ALL_BLOCKS = new LinkedHashMap<>();

    // 34 Blocks
    public static final DeferredBlock<Block> SPECTROSCOPE = registerMachineBlock("spectroscope", PhysicalBlocks.SpectroscopeBlock::new);
    public static final DeferredBlock<Block> ELECTROMAGNET = registerMachineBlock("electromagnet", PhysicalBlocks.ElectromagnetBlock::new);
    public static final DeferredBlock<Block> VACUUM_PUMP = registerMachineBlock("vacuum_pump", PhysicalBlocks.VacuumPumpBlock::new);
    public static final DeferredBlock<Block> VACUUM_FURNACE = registerMachineBlock("vacuum_furnace", PhysicalBlocks.VacuumFurnaceBlock::new);
    public static final DeferredBlock<Block> XRAY_DIFFRACTOMETER = registerMachineBlock("xray_diffractometer", PhysicalBlocks.XrayDiffractometerBlock::new);
    public static final DeferredBlock<Block> ISOTOPE_SEPARATOR = registerMachineBlock("isotope_separator", PhysicalBlocks.IsotopeSeparatorBlock::new);
    public static final DeferredBlock<Block> CRYOCOOLER = registerMachineBlock("cryocooler", PhysicalBlocks.CryocoolerBlock::new);
    public static final DeferredBlock<Block> SUPERCONDUCTING_CABLE = registerMachineBlock("superconducting_cable", PhysicalBlocks.SuperconductingCableBlock::new);
    public static final DeferredBlock<Block> LASER_CUTTER = registerMachineBlock("laser_cutter", PhysicalBlocks.LaserCutterBlock::new);
    public static final DeferredBlock<Block> CYCLOTRON = registerMachineBlock("cyclotron", PhysicalBlocks.CyclotronBlock::new);
    public static final DeferredBlock<Block> FUSION_IGNITER = registerMachineBlock("fusion_igniter", PhysicalBlocks.FusionIgniterBlock::new);
    public static final DeferredBlock<Block> PLASMA_CONTAINMENT = registerMachineBlock("plasma_containment", PhysicalBlocks.PlasmaContainmentBlock::new);
    public static final DeferredBlock<Block> ATOMIC_CLOCK = registerMachineBlock("atomic_clock", PhysicalBlocks.AtomicClockBlock::new);
    public static final DeferredBlock<Block> RADIO_TRANSCEIVER = registerMachineBlock("radio_transceiver", PhysicalBlocks.RadioTransceiverBlock::new);
    public static final DeferredBlock<Block> ANTENNA_MAST = registerMachineBlock("antenna_mast", Block::new);
    public static final DeferredBlock<Block> PHOTOVOLTAIC_PANEL = registerMachineBlock("photovoltaic_panel", PhysicalBlocks.PhotovoltaicPanelBlock::new);
    public static final DeferredBlock<Block> ZONE_REFINER = registerMachineBlock("zone_refiner", PhysicalBlocks.ZoneRefinerBlock::new);
    public static final DeferredBlock<Block> WAFER_FAB = registerMachineBlock("wafer_fab", PhysicalBlocks.WaferFabBlock::new);
    public static final DeferredBlock<Block> CLEANROOM_FILTER = registerMachineBlock("cleanroom_filter", Block::new);
    public static final DeferredBlock<Block> MAINFRAME = registerMachineBlock("mainframe", PhysicalBlocks.MainframeBlock::new);
    public static final DeferredBlock<Block> RADAR_STATION = registerMachineBlock("radar_station", PhysicalBlocks.RadarStationBlock::new);
    public static final DeferredBlock<Block> BUS_BRIDGE = registerMachineBlock("bus_bridge", Block::new);
    public static final DeferredBlock<Block> REFLECTING_TELESCOPE = registerMachineBlock("reflecting_telescope", PhysicalBlocks.ReflectingTelescopeBlock::new);
    public static final DeferredBlock<Block> LAUNCH_PAD = registerMachineBlock("launch_pad", PhysicalBlocks.LaunchPadBlock::new);
    public static final DeferredBlock<Block> CREW_CAPSULE = registerMachineBlock("crew_capsule", PhysicalBlocks.CrewCapsuleBlock::new);
    public static final DeferredBlock<Block> GROUND_STATION = registerMachineBlock("ground_station", PhysicalBlocks.GroundStationBlock::new);
    public static final DeferredBlock<Block> LIFE_SUPPORT_UNIT = registerMachineBlock("life_support_unit", PhysicalBlocks.LifeSupportUnitBlock::new);
    public static final DeferredBlock<Block> STATION_CORE = registerMachineBlock("station_core", PhysicalBlocks.StationCoreBlock::new);
    public static final DeferredBlock<Block> NANO_ASSEMBLER = registerAxiomBlock("nano_assembler", PhysicalBlocks.NanoAssemblerBlock::new);
    public static final DeferredBlock<Block> PENNING_TRAP = registerAxiomBlock("penning_trap", PhysicalBlocks.PenningTrapBlock::new);
    public static final DeferredBlock<Block> ANNIHILATION_REACTOR = registerAxiomBlock("annihilation_reactor", PhysicalBlocks.AnnihilationReactorBlock::new);
    public static final DeferredBlock<Block> WARP_CORE = registerAxiomBlock("warp_core", PhysicalBlocks.WarpCoreBlock::new);
    public static final DeferredBlock<Block> WARP_BEACON = registerAxiomBlock("warp_beacon", PhysicalBlocks.WarpBeaconBlock::new);
    public static final DeferredBlock<Block> MICROWAVE_RECEIVER = registerAxiomBlock("microwave_receiver", PhysicalBlocks.MicrowaveReceiverBlock::new);
    public static final DeferredBlock<Block> ANDROID_ASSEMBLY_BAY = registerAxiomBlock("android_assembly_bay", PhysicalBlocks.AndroidAssemblyBayBlock::new);

    // Primary Items
    public static final DeferredItem<Item> ECHO_SOUNDER = registerMainItem("echo_sounder", p -> new PhysicalItems.EchoSounderItem(p.stacksTo(1)));
    public static final DeferredItem<Item> MULTIMETER = registerMainItem("multimeter", p -> new PhysicalItems.MultimeterItem(p.stacksTo(1)));
    public static final DeferredItem<Item> GROUND_LENS = registerMainItem("ground_lens", p -> new PhysicalItems.GroundLensItem(p.stacksTo(16)));
    public static final DeferredItem<Item> XRAY_TUBE = registerMainItem("xray_tube", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(1), "efficiency", false));
    public static final DeferredItem<Item> GEIGER_COUNTER = registerMainItem("geiger_counter", p -> new PhysicalItems.GeigerCounterItem(p.stacksTo(1)));
    public static final DeferredItem<Item> DEWAR_FLASK = registerMainItem("dewar_flask", p -> new PhysicalItems.DewarFlaskItem(p.stacksTo(1)));
    public static final DeferredItem<Item> SUPERCONDUCTOR_COIL = registerMainItem("superconductor_coil", p -> new PhysicalItems.SuperconductorCoilItem(p.stacksTo(16)));
    public static final DeferredItem<Item> HIGH_DENSITY_CELL = registerMainItem("high_density_cell", p -> new PhysicalItems.HighDensityCellItem(p.stacksTo(1)));
    public static final DeferredItem<Item> VACUUM_TUBE = registerMainItem("vacuum_tube", p -> new PhysicalItems.VacuumTubeItem(p.stacksTo(16)));
    public static final DeferredItem<Item> TRANSISTOR = registerMainItem("transistor", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(64), "yield", false));
    public static final DeferredItem<Item> CIRCUIT_BOARD = registerMainItem("circuit_board", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(16), "yield", false));
    public static final DeferredItem<Item> PHOTORESIST = registerMainItem("photoresist", p -> new Item(p.stacksTo(16)));
    public static final DeferredItem<Item> INTEGRATED_CIRCUIT = registerMainItem("integrated_circuit", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(64), "yield", false));
    public static final DeferredItem<Item> PROGRAM_CARD = registerMainItem("program_card", p -> new PhysicalItems.ProgramCardItem(p.stacksTo(1)));
    public static final DeferredItem<Item> PHOTONIC_CORE = registerMainItem("photonic_core", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(1), "yield", false));
    public static final DeferredItem<Item> QUANTUM_CORE = registerMainItem("quantum_core", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(1), "yield", false));
    public static final DeferredItem<Item> AI_CONTROLLER = registerMainItem("ai_controller", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(1), "efficiency", false));
    public static final DeferredItem<Item> STAR_CHART = registerMainItem("star_chart", p -> new PhysicalItems.StarChartItem(p.stacksTo(1)));
    public static final DeferredItem<Item> ROCKET_ENGINE = registerMainItem("rocket_engine", p -> new PhysicalItems.RocketEngineItem(p.stacksTo(4)));
    public static final DeferredItem<Item> PROPELLANT_TANK = registerMainItem("propellant_tank", p -> new PhysicalItems.PropellantTankItem(p.stacksTo(1)));
    public static final DeferredItem<Item> HEAT_SHIELD_TILE = registerMainItem("heat_shield_tile", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(16), "integrity", false));
    public static final DeferredItem<Item> VACUUM_FRAME = registerMainItem("vacuum_frame", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(16), "integrity", false));
    public static final DeferredItem<Item> SPACE_HELMET = registerMainItem("space_helmet", p -> new PhysicalItems.SpaceHelmetItem(p.stacksTo(1)));
    public static final DeferredItem<Item> SPACE_SUIT = registerMainItem("space_suit", p -> new PhysicalItems.SpaceSuitItem(p.stacksTo(1)));
    public static final DeferredItem<Item> SATELLITE = registerMainItem("satellite", p -> new PhysicalItems.SatelliteItem(p.stacksTo(1)));
    public static final DeferredItem<Item> SPACE_PROBE = registerMainItem("space_probe", p -> new PhysicalItems.SpaceProbeItem(p.stacksTo(1)));
    public static final DeferredItem<Item> METAMATERIAL = registerMainItem("metamaterial", p -> new PhysicalItems.MetamaterialItem(p.stacksTo(16)));
    public static final DeferredItem<Item> ANTIMATTER_CELL = registerMainItem("antimatter_cell", p -> new PhysicalItems.AntimatterCellItem(p.stacksTo(1)));
    public static final DeferredItem<Item> STELLAR_ALLOY = registerMainItem("stellar_alloy", p -> new PhysicalItems.QualityDeviceItem(p.stacksTo(64), "integrity", true));
    public static final DeferredItem<Item> ORBITAL_COLLECTOR = registerMainItem("orbital_collector", p -> new PhysicalItems.OrbitalCollectorItem(p.stacksTo(1)));

    // 28 Intermediate Items (§9 - not added to main creative tab)
    public static final DeferredItem<Item> PATTERNED_WAFER = registerIntermediateItem("patterned_wafer", 16);
    public static final DeferredItem<Item> DIE = registerIntermediateItem("die", 64);
    public static final DeferredItem<Item> SOLAR_CELL = registerIntermediateItem("solar_cell", 64);
    public static final DeferredItem<Item> ETCHANT = registerIntermediateItem("etchant", 64);
    public static final DeferredItem<Item> DOPANT = registerIntermediateItem("dopant", 64);
    public static final DeferredItem<Item> GERMANIUM = registerIntermediateItem("germanium", 64);
    public static final DeferredItem<Item> BARIUM_CARBONATE = registerIntermediateItem("barium_carbonate", 64);
    public static final DeferredItem<Item> YBCO_PRECURSOR = registerIntermediateItem("ybco_precursor", 64);
    public static final DeferredItem<Item> SILVER_SHEATH = registerIntermediateItem("silver_sheath", 64);
    public static final DeferredItem<Item> CARBON_FIBER = registerIntermediateItem("carbon_fiber", 64);
    public static final DeferredItem<Item> CARBON_COMPOSITE = registerIntermediateItem("carbon_composite", 64);
    public static final DeferredItem<Item> PHOTONIC_WAVEGUIDE = registerIntermediateItem("photonic_waveguide", 64);
    public static final DeferredItem<Item> LASER_DIODE = registerIntermediateItem("laser_diode", 64);
    public static final DeferredItem<Item> POWER_BUS = registerIntermediateItem("power_bus", 16);
    public static final DeferredItem<Item> DEUTERIUM = registerIntermediateItem("deuterium", 64);
    public static final DeferredItem<Item> TRITIUM = registerIntermediateItem("tritium", 64);
    public static final DeferredItem<Item> HELIUM_GAS = registerIntermediateItem("helium_gas", 64);
    public static final DeferredItem<Item> RUBIDIUM_SALT = registerIntermediateItem("rubidium_salt", 64);
    public static final DeferredItem<Item> ARGON = registerIntermediateItem("argon", 64);
    public static final DeferredItem<Item> LIQUID_NITROGEN = registerCryogenItem("liquid_nitrogen", 16);
    public static final DeferredItem<Item> LIQUID_OXYGEN = registerCryogenItem("liquid_oxygen", 16);
    public static final DeferredItem<Item> LIQUID_HYDROGEN = registerCryogenItem("liquid_hydrogen", 16);
    public static final DeferredItem<Item> LIQUID_HELIUM = registerCryogenItem("liquid_helium", 16);
    public static final DeferredItem<Item> OBSERVATION_PLATE = registerIntermediateItem("observation_plate", 16);
    public static final DeferredItem<Item> DEEP_SPACE_DATA = registerIntermediateItem("deep_space_data", 16);
    public static final DeferredItem<Item> LUNAR_REGOLITH = registerIntermediateItem("lunar_regolith", 16);
    public static final DeferredItem<Item> ASTEROID_SAMPLE = registerIntermediateItem("asteroid_sample", 16);
    public static final DeferredItem<Item> NANO_FEEDSTOCK = registerIntermediateItem("nano_feedstock", 64);

    // BlockEntity & Menu
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PhysicalBlockEntities.PhysicalMachineBlockEntity>> PHYSICAL_MACHINE_BE =
            BLOCK_ENTITIES.register("physical_machine", () -> new BlockEntityType<>(
                    PhysicalBlockEntities.PhysicalMachineBlockEntity::new,
                    Set.of(
                            SPECTROSCOPE.get(), ELECTROMAGNET.get(), VACUUM_PUMP.get(), VACUUM_FURNACE.get(),
                            XRAY_DIFFRACTOMETER.get(), ISOTOPE_SEPARATOR.get(), CRYOCOOLER.get(), LASER_CUTTER.get(),
                            CYCLOTRON.get(), FUSION_IGNITER.get(), PLASMA_CONTAINMENT.get(), ATOMIC_CLOCK.get(),
                            RADIO_TRANSCEIVER.get(), PHOTOVOLTAIC_PANEL.get(), ZONE_REFINER.get(), WAFER_FAB.get(),
                            MAINFRAME.get(), RADAR_STATION.get(), REFLECTING_TELESCOPE.get(), LAUNCH_PAD.get(),
                            CREW_CAPSULE.get(), GROUND_STATION.get(), LIFE_SUPPORT_UNIT.get(), STATION_CORE.get(),
                            NANO_ASSEMBLER.get(), PENNING_TRAP.get(), ANNIHILATION_REACTOR.get(), WARP_CORE.get(),
                            WARP_BEACON.get(), MICROWAVE_RECEIVER.get(), ANDROID_ASSEMBLY_BAY.get()
                    )));

    public static final DeferredHolder<MenuType<?>, MenuType<PhysicalMenus.PhysicalMachineMenu>> PHYSICAL_MACHINE_MENU =
            MENUS.register("physical_machine", () -> new MenuType<>(PhysicalMenus.PhysicalMachineMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // Entities
    public static final DeferredHolder<EntityType<?>, EntityType<PhysicalEntities.LaunchVehicleEntity>> LAUNCH_VEHICLE =
            ENTITY_TYPES.register("launch_vehicle", () -> EntityType.Builder
                    .<PhysicalEntities.LaunchVehicleEntity>of(PhysicalEntities.LaunchVehicleEntity::new, MobCategory.MISC)
                    .sized(1.8F, 6.0F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "launch_vehicle"))));

    public static final DeferredHolder<EntityType<?>, EntityType<PhysicalEntities.AndroidWorkerEntity>> ANDROID_WORKER =
            ENTITY_TYPES.register("android_worker", () -> EntityType.Builder
                    .<PhysicalEntities.AndroidWorkerEntity>of(PhysicalEntities.AndroidWorkerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "android_worker"))));

    // POI & Villager Professions
    public static final DeferredHolder<PoiType, PoiType> PHYSICIST_POI = POI_TYPES.register("physicist",
            () -> new PoiType(ImmutableSet.copyOf(SPECTROSCOPE.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> ELECTRONICS_ENGINEER_POI = POI_TYPES.register("electronics_engineer",
            () -> new PoiType(ImmutableSet.copyOf(RADIO_TRANSCEIVER.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> ASTRONOMER_POI = POI_TYPES.register("astronomer",
            () -> new PoiType(ImmutableSet.copyOf(REFLECTING_TELESCOPE.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PHYSICIST = VILLAGER_PROFESSIONS.register("physicist",
            () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.earth_physical_sciences.physicist"),
                    h -> h.is(PHYSICIST_POI.getKey()),
                    h -> h.is(PHYSICIST_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN, Int2ObjectMap.ofEntries()));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> ELECTRONICS_ENGINEER = VILLAGER_PROFESSIONS.register("electronics_engineer",
            () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.earth_physical_sciences.electronics_engineer"),
                    h -> h.is(ELECTRONICS_ENGINEER_POI.getKey()),
                    h -> h.is(ELECTRONICS_ENGINEER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_TOOLSMITH, Int2ObjectMap.ofEntries()));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> ASTRONOMER = VILLAGER_PROFESSIONS.register("astronomer",
            () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.earth_physical_sciences.astronomer"),
                    h -> h.is(ASTRONOMER_POI.getKey()),
                    h -> h.is(ASTRONOMER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, Int2ObjectMap.ofEntries()));

    // Creative Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.earth_physical_sciences"))
            .icon(() -> new ItemStack(SPECTROSCOPE.get()))
            .displayItems((params, output) -> {
                for (DeferredItem<? extends Item> item : CREATIVE_TAB_ITEMS) {
                    output.accept(item.get());
                }
            })
            .build());

    public EarthPhysicalSciences(IEventBus modBus) {
        registerSounds();
        DATA_COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        ENTITY_TYPES.register(modBus);
        POI_TYPES.register(modBus);
        VILLAGER_PROFESSIONS.register(modBus);
        SOUND_EVENTS.register(modBus);
        CREATIVE_TABS.register(modBus);

        PhysicalEvents.register();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            com.xxsx.earthphysicalsciences.client.EarthPhysicalSciencesClient.init(modBus);
        }
    }

    private static DeferredBlock<Block> registerMachineBlock(String name, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        DeferredBlock<Block> blk = BLOCKS.registerBlock(name, factory,
                p -> p.mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion());
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(name, blk);
        CREATIVE_TAB_ITEMS.add(item);
        ALL_ITEMS.put(name, item);
        ALL_BLOCKS.put(name, blk);
        return blk;
    }

    private static DeferredBlock<Block> registerAxiomBlock(String name, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        DeferredBlock<Block> blk = BLOCKS.registerBlock(name, factory,
                p -> p.mapColor(MapColor.COLOR_PURPLE).strength(4.0F, 8.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion());
        DeferredItem<Item> item = ITEMS.registerItem(name, p -> new PhysicalItems.AxiomBlockItem(blk.get(), p));
        CREATIVE_TAB_ITEMS.add(item);
        ALL_ITEMS.put(name, item);
        ALL_BLOCKS.put(name, blk);
        return blk;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static DeferredItem<Item> registerMainItem(String name, Function<Item.Properties, ? extends Item> factory) {
        DeferredItem<Item> item = ITEMS.registerItem(name, factory);
        CREATIVE_TAB_ITEMS.add(item);
        ALL_ITEMS.put(name, item);
        return item;
    }

    private static DeferredItem<Item> registerIntermediateItem(String name, int stackSize) {
        DeferredItem<Item> item = ITEMS.registerItem(name, p -> new Item(p.stacksTo(stackSize)));
        ALL_ITEMS.put(name, item);
        return item;
    }

    private static DeferredItem<Item> registerCryogenItem(String name, int stackSize) {
        DeferredItem<Item> item = ITEMS.registerItem(name, p -> new PhysicalItems.CryogenItem(p.stacksTo(stackSize), name));
        ALL_ITEMS.put(name, item);
        return item;
    }

    private static void registerSounds() {
        String[] events = {
                "spectroscope_hum", "echo_sounder_ping", "multimeter_beep", "vacuum_pump_run",
                "vacuum_furnace_roar", "xray_diffractometer_scan", "geiger_counter_click",
                "isotope_separator_whir", "cryocooler_hiss", "coil_quench", "laser_cutter_zap",
                "cyclotron_hum", "fusion_ignite", "fusion_burn", "atomic_clock_tick",
                "radio_static", "wafer_fab_step", "mainframe_hum", "radar_sweep",
                "telescope_turn", "rocket_ignition", "rocket_roar", "capsule_reentry",
                "nano_assembler_hum", "penning_trap_hum", "annihilation_hum", "warp_charge", "warp_jump"
        };
        for (String ev : events) {
            SOUND_EVENTS.register(ev, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, ev)));
        }
    }
}
