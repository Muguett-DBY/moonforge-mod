package com.muguett.moonforge.network;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record AkReloadPayload(Hand hand) implements CustomPayload {
    public static final CustomPayload.Id<AkReloadPayload> ID = new CustomPayload.Id<>(Identifier.of(MoonforgeMod.MOD_ID, "ak_reload"));
    public static final PacketCodec<RegistryByteBuf, AkReloadPayload> CODEC = CustomPayload.codecOf(AkReloadPayload::write, AkReloadPayload::new);

    public AkReloadPayload(RegistryByteBuf buf) {
        this(buf.readEnumConstant(Hand.class));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeEnumConstant(hand);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
