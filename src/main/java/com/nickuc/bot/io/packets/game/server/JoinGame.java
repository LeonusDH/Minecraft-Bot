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

package com.nickuc.bot.io.packets.game.server;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class JoinGame extends Packet {

    private int entityID;
    private int gameMode;
    private byte dimension;
    private int difficulty;
    private int maxPlayers;
    private String levelType;
    private boolean reducedInfo;

    @Override
    public void read(InputBuffer in) throws IOException {
        entityID = in.readInt();
        gameMode = in.readUByte();
        dimension = in.readByte();
        difficulty = in.readUByte();
        maxPlayers = in.readUByte();
        levelType = in.readString();
        reducedInfo = in.readBoolean();
    }

    @Override
    public void write(OutputBuffer out) throws IOException {
        out.writeVarInt(entityID);
        out.writeUByte(gameMode);
        out.writeUByte(dimension);
        out.writeUByte(difficulty);
        out.writeUByte(maxPlayers);
        out.writeString(levelType);
        out.writeBoolean(reducedInfo);
    }

}
