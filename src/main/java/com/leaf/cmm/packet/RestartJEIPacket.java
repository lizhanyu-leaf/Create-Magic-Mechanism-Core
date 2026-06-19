package com.leaf.cmm.packet;

import com.leaf.cmm.jei.JEIRestarter;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class RestartJEIPacket extends SimplePacketBase {
    public RestartJEIPacket() {}
    public RestartJEIPacket(FriendlyByteBuf friendlyByteBuf) {}

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(JEIRestarter::restart);
        return true;
    }
}
