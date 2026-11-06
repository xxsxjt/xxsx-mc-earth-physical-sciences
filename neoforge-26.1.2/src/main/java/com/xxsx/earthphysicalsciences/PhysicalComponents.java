package com.xxsx.earthphysicalsciences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public final class PhysicalComponents {
    private PhysicalComponents() {}

    public record DeviceQualityData(
            int schemaVersion,
            String grade,
            String metricKind,
            double metric,
            String maker
    ) {
        public static final DeviceQualityData DEFAULT = new DeviceQualityData(1, "common", "accuracy", 0.075, "");

        public static final Codec<DeviceQualityData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("schema_version", 1).forGetter(DeviceQualityData::schemaVersion),
                Codec.STRING.optionalFieldOf("grade", "common").forGetter(DeviceQualityData::grade),
                Codec.STRING.optionalFieldOf("metric_kind", "accuracy").forGetter(DeviceQualityData::metricKind),
                Codec.DOUBLE.optionalFieldOf("metric", 0.075).forGetter(DeviceQualityData::metric),
                Codec.STRING.optionalFieldOf("maker", "").forGetter(DeviceQualityData::maker)
        ).apply(inst, DeviceQualityData::new));

        public static final StreamCodec<ByteBuf, DeviceQualityData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, DeviceQualityData::schemaVersion,
                ByteBufCodecs.STRING_UTF8, DeviceQualityData::grade,
                ByteBufCodecs.STRING_UTF8, DeviceQualityData::metricKind,
                ByteBufCodecs.DOUBLE, DeviceQualityData::metric,
                ByteBufCodecs.STRING_UTF8, DeviceQualityData::maker,
                DeviceQualityData::new
        );
    }

    public record DewarContentsData(String contents, int count) {
        public static final DewarContentsData EMPTY = new DewarContentsData("empty", 0);
        public static final Codec<DewarContentsData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("contents", "empty").forGetter(DewarContentsData::contents),
                Codec.INT.optionalFieldOf("count", 0).forGetter(DewarContentsData::count)
        ).apply(inst, DewarContentsData::new));
        public static final StreamCodec<ByteBuf, DewarContentsData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, DewarContentsData::contents,
                ByteBufCodecs.VAR_INT, DewarContentsData::count,
                DewarContentsData::new
        );
    }

    public record CoilStateData(int criticalTempK, double fieldStrengthT, boolean quenched, int quenchCount) {
        public static final CoilStateData DEFAULT = new CoilStateData(92, 12.0, false, 0);
        public static final Codec<CoilStateData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("critical_temp_k", 92).forGetter(CoilStateData::criticalTempK),
                Codec.DOUBLE.optionalFieldOf("field_strength_t", 12.0).forGetter(CoilStateData::fieldStrengthT),
                Codec.BOOL.optionalFieldOf("quenched", false).forGetter(CoilStateData::quenched),
                Codec.INT.optionalFieldOf("quench_count", 0).forGetter(CoilStateData::quenchCount)
        ).apply(inst, CoilStateData::new));
        public static final StreamCodec<ByteBuf, CoilStateData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, CoilStateData::criticalTempK,
                ByteBufCodecs.DOUBLE, CoilStateData::fieldStrengthT,
                ByteBufCodecs.BOOL, CoilStateData::quenched,
                ByteBufCodecs.VAR_INT, CoilStateData::quenchCount,
                CoilStateData::new
        );
    }

    public record PropellantTankData(int loxMb, int lh2Mb) {
        public static final PropellantTankData EMPTY = new PropellantTankData(0, 0);
        public static final Codec<PropellantTankData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("lox_mb", 0).forGetter(PropellantTankData::loxMb),
                Codec.INT.optionalFieldOf("lh2_mb", 0).forGetter(PropellantTankData::lh2Mb)
        ).apply(inst, PropellantTankData::new));
        public static final StreamCodec<ByteBuf, PropellantTankData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, PropellantTankData::loxMb,
                ByteBufCodecs.VAR_INT, PropellantTankData::lh2Mb,
                PropellantTankData::new
        );
    }

    public record StarChartData(List<String> constellations, int moonPhases, List<String> bodies) {
        public static final StarChartData EMPTY = new StarChartData(List.of(), 0, List.of());
        public static final Codec<StarChartData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.listOf().optionalFieldOf("constellations", List.of()).forGetter(StarChartData::constellations),
                Codec.INT.optionalFieldOf("moon_phases", 0).forGetter(StarChartData::moonPhases),
                Codec.STRING.listOf().optionalFieldOf("bodies", List.of()).forGetter(StarChartData::bodies)
        ).apply(inst, StarChartData::new));
        public static final StreamCodec<ByteBuf, StarChartData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), StarChartData::constellations,
                ByteBufCodecs.VAR_INT, StarChartData::moonPhases,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), StarChartData::bodies,
                StarChartData::new
        );
    }
}
