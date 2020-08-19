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
public class TabComplete extends Packet {

    private String text;
    private Object lookedBlock;

    public TabComplete(String text) {
        this.text = text;
    }

    @Override
    protected void read(InputBuffer in) throws IOException {
        text = in.readString();
        if (in.readBoolean()) {
            throw new UnsupportedOperationException("Looked block is currently unsupported.");
        }
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeString(text);
        if (lookedBlock != null) {
            throw new UnsupportedOperationException("Looked block is currently unsupported.");
        }
        out.writeBoolean(false);
    }
}
