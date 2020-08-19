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

package com.nickuc.bot.io.packets.handshaking;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class Handshake extends Packet {

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    @Getter private State nextState;

    @Override
    public void read(InputBuffer in) throws IOException {
        protocolVersion = in.readVarInt();
        serverAddress = in.readString();
        serverPort = in.readShort();
        nextState = State.search(in.readVarInt());
    }

    @Override
    public void write(OutputBuffer out) throws IOException {
        out.writeVarInt(protocolVersion);
        out.writeString(serverAddress);
        out.writeShort((short) serverPort);
        out.writeVarInt(nextState.id);
    }

}