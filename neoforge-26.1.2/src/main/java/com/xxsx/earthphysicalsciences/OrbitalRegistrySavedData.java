package com.xxsx.earthphysicalsciences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class OrbitalRegistrySavedData extends SavedData {
    public record OrbitRecord(
            String id,
            String owner,
            String payload,
            long launched,
            int groundX,
            int groundZ,
            double health,
            String link
    ) {
        public static final Codec<OrbitRecord> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(OrbitRecord::id),
                Codec.STRING.fieldOf("owner").forGetter(OrbitRecord::owner),
                Codec.STRING.fieldOf("payload").forGetter(OrbitRecord::payload),
                Codec.LONG.fieldOf("launched").forGetter(OrbitRecord::launched),
                Codec.INT.fieldOf("ground_x").forGetter(OrbitRecord::groundX),
                Codec.INT.fieldOf("ground_z").forGetter(OrbitRecord::groundZ),
                Codec.DOUBLE.fieldOf("health").forGetter(OrbitRecord::health),
                Codec.STRING.optionalFieldOf("link", "").forGetter(OrbitRecord::link)
        ).apply(inst, OrbitRecord::new));
    }

    public record ProbeRecord(
            String id,
            String owner,
            String target,
            long arrive,
            boolean returnCapsule,
            String state
    ) {
        public static final Codec<ProbeRecord> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(ProbeRecord::id),
                Codec.STRING.fieldOf("owner").forGetter(ProbeRecord::owner),
                Codec.STRING.fieldOf("target").forGetter(ProbeRecord::target),
                Codec.LONG.fieldOf("arrive").forGetter(ProbeRecord::arrive),
                Codec.BOOL.fieldOf("return_capsule").forGetter(ProbeRecord::returnCapsule),
                Codec.STRING.fieldOf("state").forGetter(ProbeRecord::state)
        ).apply(inst, ProbeRecord::new));
    }

    public static final Codec<OrbitalRegistrySavedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            OrbitRecord.CODEC.listOf().optionalFieldOf("satellites", List.of()).forGetter(OrbitalRegistrySavedData::getSatellites),
            ProbeRecord.CODEC.listOf().optionalFieldOf("probes", List.of()).forGetter(OrbitalRegistrySavedData::getProbes),
            OrbitRecord.CODEC.listOf().optionalFieldOf("collectors", List.of()).forGetter(OrbitalRegistrySavedData::getCollectors)
    ).apply(inst, OrbitalRegistrySavedData::new));

    public static final SavedDataType<OrbitalRegistrySavedData> TYPE = new SavedDataType<>(
            EarthPhysicalSciences.id("orbital_registry"),
            OrbitalRegistrySavedData::new,
            CODEC
    );

    private final List<OrbitRecord> satellites = new ArrayList<>();
    private final List<ProbeRecord> probes = new ArrayList<>();
    private final List<OrbitRecord> collectors = new ArrayList<>();

    public OrbitalRegistrySavedData() {}

    public OrbitalRegistrySavedData(List<OrbitRecord> satellites, List<ProbeRecord> probes, List<OrbitRecord> collectors) {
        this.satellites.addAll(satellites);
        this.probes.addAll(probes);
        this.collectors.addAll(collectors);
    }

    public static OrbitalRegistrySavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<OrbitRecord> getSatellites() {
        return satellites;
    }

    public List<ProbeRecord> getProbes() {
        return probes;
    }

    public List<OrbitRecord> getCollectors() {
        return collectors;
    }

    public boolean launchSatellite(UUID owner, String payload, long gameTime, int x, int z) {
        if (satellites.size() >= PhysicalScienceMath.SAT_MAX) {
            return false;
        }
        satellites.add(new OrbitRecord(UUID.randomUUID().toString(), owner == null ? "" : owner.toString(), payload, gameTime, x, z, 1.0, ""));
        setDirty();
        return true;
    }

    public boolean launchCollector(UUID owner, long gameTime, int x, int z, String link) {
        if (collectors.size() >= 256) return false;
        collectors.add(new OrbitRecord(UUID.randomUUID().toString(), owner == null ? "" : owner.toString(), "orbital_collector", gameTime, x, z, 1.0, link == null ? "" : link));
        setDirty();
        return true;
    }

    public boolean launchProbe(UUID owner, String target, long gameTime, boolean returnCapsule) {
        if (probes.size() >= 16) return false;
        long travel = switch (target) {
            case "mars" -> 120000L;
            case "asteroid_belt" -> 168000L;
            default -> 24000L;
        };
        probes.add(new ProbeRecord(UUID.randomUUID().toString(), owner == null ? "" : owner.toString(), target, gameTime + travel, returnCapsule, "cruise"));
        setDirty();
        return true;
    }

    public void tickDecay24000() {
        boolean changed = false;
        Iterator<OrbitRecord> it = satellites.iterator();
        while (it.hasNext()) {
            OrbitRecord rec = it.next();
            double nextH = PhysicalScienceMath.decaySatelliteHealth(rec.health(), 24000L);
            if (nextH <= 0.0) {
                it.remove();
            } else {
                int idx = satellites.indexOf(rec);
                if (idx >= 0) {
                    satellites.set(idx, new OrbitRecord(rec.id(), rec.owner(), rec.payload(), rec.launched(), rec.groundX(), rec.groundZ(), nextH, rec.link()));
                }
            }
            changed = true;
        }
        if (changed) setDirty();
    }
}
