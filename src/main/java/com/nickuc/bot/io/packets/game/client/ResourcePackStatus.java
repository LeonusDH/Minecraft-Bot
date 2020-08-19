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
public class ResourcePackStatus extends Packet {

    private String hash;
    private Result result;

    @Override
    protected void read(InputBuffer in) throws IOException {
        hash = in.readString();
        result = Result.search(in.readVarInt());
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeString(hash);
        out.writeVarInt(result.id);
    }

    @RequiredArgsConstructor
    public enum Result {

        SUCCESSFULLY_LOADED(0),
        DECLINED(1),
        FAILED_DOWNLOAD(2),
        ACCEPTED(3);

        public final int id;

        public static Result search(int value) {
            for (Result type : values()) {
                if (type.id == value) return type;
            }
            return null;
        }

    }
}
