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

package com.nickuc.bot.io.packets.game.client;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.*;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class ClientSettings extends Packet {

    private String locale;
    private byte viewDistance;
    private ChatMode chatMode;
    private boolean chatColors;
    private SkinParts skinParts;

    @Override
    protected void read(InputBuffer in) throws IOException {
        locale = in.readString();
        viewDistance = in.readByte();
        chatMode = ChatMode.search(in.readByte());
        chatColors = in.readBoolean();
        skinParts = SkinParts.search(in.readUByte());
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeString(locale);
        out.writeByte(viewDistance);
        out.writeByte((byte) chatMode.id);
        out.writeBoolean(chatColors);
        out.writeUByte(skinParts.id);
    }

    @RequiredArgsConstructor
    public enum SkinParts {

        CAPE(0x1),
        JACKED(0x2),
        LEFT_SLEEVE(0x4),
        RIGHT_SLEEVE(0x8),
        LEFT_PANTS(0x10),
        RIGHT_PANTS(0x20),
        HAT(0x40);

        public final int id;

        public static SkinParts search(int value) {
            for (SkinParts part : values()) {
                if (part.id == value) return part;
            }
            return null;
        }

    }

    @RequiredArgsConstructor
    public enum ChatMode {

        ENABLED(0),
        COMMANDS_ONLY(1),
        HIDDEN(2);

        public final int id;

        public static ChatMode search(byte value) {
            for (ChatMode mode : values()) {
                if (mode.id == value) return mode;
            }
            return null;
        }

    }
}
