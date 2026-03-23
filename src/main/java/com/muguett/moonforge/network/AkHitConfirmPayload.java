package com.muguett.moonforge.network;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AkHitConfirmPayload(boolean lethal) implements CustomPayload {
    public static final CustomPayload.Id<AkHitConfirmPayload> ID = new CustomPayload.Id<>(Identifier.of(MoonforgeMod.MOD_ID, "ak_hit_confirm"));
    public static final PacketCodec<RegistryByteBuf, AkHitConfirmPayload> CODEC = CustomPayload.codecOf(AkHitConfirmPayload::write, AkHitConfirmPayload::new);

    public AkHitConfirmPayload(RegistryByteBuf buf) {
        this(buf.readBoolean());
    }

    private void write(RegistryByteBuf buf) {
        buf.writeBoolean(lethal);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
