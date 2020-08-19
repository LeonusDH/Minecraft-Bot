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
import lombok.*;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class ServerChatMessage extends Packet {

    private String message;
    private MessageType messageType;

    @Override
    public void read(InputBuffer in) throws IOException {
        message = in.readString();
        messageType = MessageType.search(in.readByte());
    }

    @Override
    public void write(OutputBuffer out) throws IOException {
        out.writeString(message.substring(0, 100));
        out.writeByte((byte) messageType.id);
    }

    @RequiredArgsConstructor
    public enum MessageType {

        CHAT(0),
        SYSTEM(1),
        ACTION_BAR(2);

        public final int id;

        public static MessageType search(byte value) {
            for (MessageType type : values()) {
                if (type.id == value) return type;
            }
            return null;
        }

    }
}
