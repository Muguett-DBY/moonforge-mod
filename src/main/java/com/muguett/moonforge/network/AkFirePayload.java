package com.muguett.moonforge.network;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record AkFirePayload(Hand hand, boolean scoped) implements CustomPayload {
    public static final CustomPayload.Id<AkFirePayload> ID = new CustomPayload.Id<>(Identifier.of(MoonforgeMod.MOD_ID, "ak_fire"));
    public static final PacketCodec<RegistryByteBuf, AkFirePayload> CODEC = CustomPayload.codecOf(AkFirePayload::write, AkFirePayload::new);

    public AkFirePayload(RegistryByteBuf buf) {
        this(buf.readEnumConstant(Hand.class), buf.readBoolean());
    }

    private void write(RegistryByteBuf buf) {
        buf.writeEnumConstant(hand);
        buf.writeBoolean(scoped);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
