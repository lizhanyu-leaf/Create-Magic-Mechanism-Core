//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.leaf.cmm;

import com.leaf.cmm.content.redprint.RedprintPacket;
import com.leaf.cmm.packet.RestartJEIPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

public enum CmmAllPackets {
    RESTART_JEI(RestartJEIPacket.class, RestartJEIPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    REDPRINT(RedprintPacket.class, RedprintPacket::new, NetworkDirection.PLAY_TO_SERVER);

    public static final ResourceLocation CHANNEL_NAME;
    public static final int NETWORK_VERSION = 3;
    public static final String NETWORK_VERSION_STR;
    private static SimpleChannel channel;
    private final PacketType<?> packetType;

    <T extends SimplePacketBase> CmmAllPackets(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
        this.packetType = new PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
        NetworkRegistry.ChannelBuilder var10000 = ChannelBuilder.named(CHANNEL_NAME);
        String var10001 = NETWORK_VERSION_STR;
        Objects.requireNonNull(var10001);
        var10000 = var10000.serverAcceptedVersions(var10001::equals);
        var10001 = NETWORK_VERSION_STR;
        Objects.requireNonNull(var10001);
        channel = var10000.clientAcceptedVersions(var10001::equals).networkProtocolVersion(() -> NETWORK_VERSION_STR).simpleChannel();

        for(CmmAllPackets packet : values()) {
            packet.packetType.register();
        }

    }

    public static SimpleChannel getChannel() {
        return channel;
    }

    public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
        getChannel().send(PacketDistributor.NEAR.with(TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.dimension())), message);
    }

    static {
        CHANNEL_NAME = CreateMagicMechanismCore.asResource("main");
        NETWORK_VERSION_STR = String.valueOf(3);
    }

    private static class PacketType<T extends SimplePacketBase> {
        private static int index = 0;
        private final BiConsumer<T, FriendlyByteBuf> encoder = SimplePacketBase::write;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private final Class<T> type;
        private final NetworkDirection direction;

        private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
            this.decoder = factory;
            this.handler = (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (packet.handle(context)) {
                    context.setPacketHandled(true);
                }

            };
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            CmmAllPackets.getChannel().messageBuilder(this.type, index++, this.direction)
                    .encoder(this.encoder).decoder(this.decoder)
                    .consumerNetworkThread(this.handler).add();
        }
    }
}
