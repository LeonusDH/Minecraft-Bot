/**
 * Copyright NickUC
 * -
 * Esta class pertence ao projeto de NickUC
 * Mais informações: https://nickuc.com
 * -
 * É expressamente proibido alterar o nome do proprietário do código, sem
 * expressar e deixar claramente o link para acesso da source original.
 * -
 * Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package com.nickuc.bot.io.packets;

import com.nickuc.bot.io.IOUtils;
import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.game.client.*;
import com.nickuc.bot.io.packets.game.client.inventory.ClickInventory;
import com.nickuc.bot.io.packets.game.client.inventory.CloseInventory;
import com.nickuc.bot.io.packets.game.server.*;
import com.nickuc.bot.io.packets.handshaking.Handshake;
import com.nickuc.bot.io.packets.login.client.LoginStart;
import com.nickuc.bot.io.packets.login.server.EncryptionRequest;
import com.nickuc.bot.io.packets.login.server.LoginDisconnect;
import com.nickuc.bot.io.packets.login.server.LoginSuccess;
import com.nickuc.bot.io.packets.login.server.SetCompression;
import com.nickuc.bot.io.packets.status.client.PingRequest;
import com.nickuc.bot.io.packets.status.client.StatusRequest;
import com.nickuc.bot.io.packets.status.server.PingResponse;
import com.nickuc.bot.io.packets.status.server.StatusResponse;
import lombok.AllArgsConstructor;
import lombok.Cleanup;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;

public abstract class Packet {

    public byte[] buf;

    protected abstract void read(InputBuffer in) throws IOException;

    protected abstract void write(OutputBuffer out) throws IOException;

    public <T extends Packet> T convert() {
        return (T) this;
    }

    public <T extends Packet> T convert(Class<T> clasz) {
        return (T) this;
    }

    public static Packet read(DataInputStream is, Packet.State state, int threshold) throws DataFormatException, IllegalArgumentException, IOException {
        try {
            boolean compressed = threshold > 0;
            int length = IOUtils.readVarInt(is);
            int dataLength = 0;
            if (compressed) {
                dataLength = IOUtils.readVarInt(is);
                length -= IOUtils.getVarIntSize(dataLength);
            }

            if (length > Short.MAX_VALUE) {
                throw new InvalidPacketException("Packet too big! " + length);
            }

            byte[] data = new byte[length];
            is.readFully(data);
            if (compressed && dataLength != 0) {
                data = IOUtils.inflate(data);
            }

            @Cleanup InputBuffer in = new InputBuffer(data);
            int packetId = in.readVarInt();
            Optional<Packet> packetOpt = Packet.Mapping.search(state, Packet.DirectionData.TO_CLIENT, packetId);

            Packet packet;
            if (packetOpt.isPresent()) {
                packet = packetOpt.get();
                try {
                    packet.read(in);
                    if (in.dis.available() > 0) {
                        throw new InvalidPacketException("Packet " + packet + " cannot be readed fully!");
                   }
                } catch (IOException e) {
                    throw new InvalidPacketException("Packet " + packet + " cannot be readed!");
                }
            } else {
                packet = new Unknown(packetId);
            }
            return packet;
        } finally {
            is.skipBytes(is.available());
        }
    }

    public void prepare(int threshold) throws IllegalArgumentException, IOException {
        if (buf == null) {
            Optional<Mapping.Data> packetOpt = Mapping.search(getClass());
            if (packetOpt.isPresent()) {
                boolean compress = threshold > 0;
                Mapping.Data mappingData = packetOpt.get();

                @Cleanup OutputBuffer out = new OutputBuffer();
                out.writeVarInt(mappingData.id);
                try {
                    write(out);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Packet " + this + " cannot be writed!");
                }
                out.dos.flush();

                byte[] data = out.baos.toByteArray();
                if (compress) {
                    int realLen = data.length;
                    if (realLen < threshold) {
                        realLen = 0;
                    } else {
                        data = IOUtils.deflate(data);
                    }

                    int realLenSize = IOUtils.getVarIntSize(realLen);
                    int len = realLenSize + data.length;
                    int lenSize = IOUtils.getVarIntSize(len);

                    byte[] b = new byte[len + lenSize];
                    IOUtils.putVarInt(b, 0, len);
                    IOUtils.putVarInt(b, lenSize, realLen);
                    System.arraycopy(data, 0, b, realLenSize + lenSize, data.length);

                    buf = b;
                } else {
                    buf = appendVarInt(data, data.length);
                }
            } else {
                throw new IllegalArgumentException("Packet " + this + " is not registered!");
            }
        }
    }

    private static byte[] appendVarInt(byte[] data, int value) {
        int valueSize = IOUtils.getVarIntSize(value);
        byte[] b = new byte[data.length + valueSize];
        IOUtils.putVarInt(b, 0, value);
        System.arraycopy(data, 0, b, valueSize, data.length);
        return b;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @AllArgsConstructor
    public enum State {

        HANDSHAKING(0),
        STATUS(1),
        LOGIN(2),
        GAME(3);

        public final int id;

        public static State search(int id) {
            for (State state : State.values()) {
                if (state.id == id) return state;
            }
            return null;
        }
    }

    public enum DirectionData {

        TO_CLIENT,
        TO_SERVER;

        public Map<String, Mapping.Data> packetMap() {
            return this == TO_CLIENT ? Mapping.TO_CLIENT : Mapping.TO_SERVER;
        }

    }

    public static class Unknown extends Packet {

        public final int id;

        Unknown(int id) {
            this.id = id;
        }

        @Override
        protected void read(InputBuffer in) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void write(OutputBuffer out) throws IOException {
            throw new UnsupportedOperationException();
        }

    }

    public static class Mapping {

        private static final HashMap<String, Data> TO_CLIENT = new HashMap<>(), TO_SERVER = new HashMap<>();

        static {

            // handshaking
            registerPacket(0x0, State.HANDSHAKING, DirectionData.TO_SERVER, Handshake.class);


            // login
            registerPacket(0x0, State.LOGIN, DirectionData.TO_SERVER, LoginStart.class);

            registerPacket(0x0, State.LOGIN, DirectionData.TO_CLIENT, LoginDisconnect.class);
            registerPacket(0x1, State.LOGIN, DirectionData.TO_CLIENT, EncryptionRequest.class);
            registerPacket(0x2, State.LOGIN, DirectionData.TO_CLIENT, LoginSuccess.class);
            registerPacket(0x3, State.LOGIN, DirectionData.TO_CLIENT, SetCompression.class);


            // status
            registerPacket(0x0, State.STATUS, DirectionData.TO_SERVER, StatusRequest.class);
            registerPacket(0x1, State.STATUS, DirectionData.TO_SERVER, PingRequest.class);

            registerPacket(0x0, State.STATUS, DirectionData.TO_CLIENT, StatusResponse.class);
            registerPacket(0x1, State.STATUS, DirectionData.TO_CLIENT, PingResponse.class);


            // game
            registerPacket(0x0, State.GAME, DirectionData.TO_CLIENT, KeepAliveRequest.class);
            registerPacket(0x1, State.GAME, DirectionData.TO_CLIENT, JoinGame.class);
            registerPacket(0x2, State.GAME, DirectionData.TO_CLIENT, ServerChatMessage.class);
            registerPacket(0x6, State.GAME, DirectionData.TO_CLIENT, UpdateHealth.class);
            registerPacket(0x40, State.GAME, DirectionData.TO_CLIENT, GameDisconnect.class);

            registerPacket(0x0, State.GAME, DirectionData.TO_SERVER, KeepAliveResponse.class);
            registerPacket(0x1, State.GAME, DirectionData.TO_SERVER, ClientChatMessage.class);
            registerPacket(0x3, State.GAME, DirectionData.TO_SERVER, PlayerPosition.class);
            registerPacket(0x9, State.GAME, DirectionData.TO_SERVER, HeldItemChange.class);
            registerPacket(0x0A, State.GAME, DirectionData.TO_SERVER, ArmAnimation.class);
            registerPacket(0x0D, State.GAME, DirectionData.TO_SERVER, CloseInventory.class);
            registerPacket(0x0E, State.GAME, DirectionData.TO_SERVER, ClickInventory.class);
            registerPacket(0x14, State.GAME, DirectionData.TO_SERVER, TabComplete.class);
            registerPacket(0x15, State.GAME, DirectionData.TO_SERVER, ClientSettings.class);
            registerPacket(0x16, State.GAME, DirectionData.TO_SERVER, ClientStatus.class);
            registerPacket(0x17, State.GAME, DirectionData.TO_SERVER, PluginMessage.class);
            registerPacket(0x19, State.GAME, DirectionData.TO_SERVER, ResourcePackStatus.class);

        }

        static void registerPacket(int packetId, State state, DirectionData direction, Class<? extends Packet> clasz) {
            direction.packetMap().put(state.id + "." + packetId, new Data(packetId, clasz));
        }

        static Optional<Data> search(Class<? extends Packet> clasz) {
            DirectionData directionData = clasz.getPackage().getName().contains("client") || clasz.isAssignableFrom(Handshake.class) ? DirectionData.TO_SERVER : DirectionData.TO_CLIENT;
            return directionData.packetMap().values().stream().filter(tmpData -> tmpData.clasz.isAssignableFrom(clasz)).findFirst();
        }

        static Optional<Packet> search(State state, DirectionData direction, int packetId) {
            Data data = direction.packetMap().get(state.id + "." + packetId);
            return data != null ? newPacket(data.clasz) : Optional.empty();
        }

        private static Optional<Packet> newPacket(Class<? extends Packet> clasz) {
            try {
                return Optional.of(clasz.newInstance());
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        @AllArgsConstructor
        public static class Data {

            public final int id;
            public final Class<? extends Packet> clasz;

        }
    }
}
