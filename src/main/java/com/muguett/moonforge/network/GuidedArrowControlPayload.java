package com.muguett.moonforge.network;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GuidedArrowControlPayload(int projectileId, float turnInput, float liftInput) implements CustomPayload {
    public static final CustomPayload.Id<GuidedArrowControlPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MoonforgeMod.MOD_ID, "guided_arrow_control"));
    public static final PacketCodec<RegistryByteBuf, GuidedArrowControlPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            GuidedArrowControlPayload::projectileId,
            PacketCodecs.FLOAT,
            GuidedArrowControlPayload::turnInput,
            PacketCodecs.FLOAT,
            GuidedArrowControlPayload::liftInput,
            GuidedArrowControlPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
