package com.lw.OpenFile.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

/**
 * 负责客户端→服务端的网络通信，玩家点击10次后发送传送请求到服务端。
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("openfile_tp");
    private static int packetId = 0;

    public static void init() {
        INSTANCE.registerMessage(TeleportHandler.class, TeleportRequest.class, packetId++, Side.SERVER);
    }

    /**
     * 客户端发送到服务端的消息：请求随机传送
     */
    public static class TeleportRequest implements IMessage {

        public TeleportRequest() {}

        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}
    }

    /**
     * 服务端处理：将玩家随机传送到附近 ±200 格内的安全位置
     */
    public static class TeleportHandler implements IMessageHandler<TeleportRequest, IMessage> {

        @Override
        public IMessage onMessage(TeleportRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                World world = player.world;
                Random rand = world.rand;

                // 在玩家当前位置 ±200 格范围内随机
                int dx = rand.nextInt(401) - 200; // -200 ~ +200
                int dz = rand.nextInt(401) - 200;
                int x = (int) player.posX + dx;
                int z = (int) player.posZ + dz;

                // 获取该位置地表高度
                int y = world.getHeight(x, z) + 1;

                // 如果 getHeight 返回 0（虚空/末地外岛等），则使用玩家当前Y
                if (y <= 1) {
                    y = (int) player.posY;
                }

                // 执行传送
                player.connection.setPlayerLocation(x + 0.5, y, z + 0.5, player.rotationYaw, player.rotationPitch);

                // 发送提示消息
                player.sendMessage(new TextComponentString(
                        TextFormatting.AQUA + "[OpenFile] " + TextFormatting.WHITE +
                                "你已被随机传送到 X:" + x + " Y:" + y + " Z:" + z
                ));
            });

            return null;
        }
    }
}
