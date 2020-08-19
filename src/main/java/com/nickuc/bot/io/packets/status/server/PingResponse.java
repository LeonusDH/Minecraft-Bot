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

package com.nickuc.bot.io.packets.status.server;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class PingResponse extends Packet {

    private long payload;

    @Override
    public void read(InputBuffer in) throws IOException {
        payload = in.readLong();
    }

    @Override
    public void write(OutputBuffer out) throws IOException {
        out.writeLong(payload);
    }
}
