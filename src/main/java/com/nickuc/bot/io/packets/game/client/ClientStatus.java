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
public class ClientStatus extends Packet {

    private Action action;

    @Override
    protected void read(InputBuffer in) throws IOException {
        action = Action.search(in.readVarInt());
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeVarInt(action.id);
    }

    @RequiredArgsConstructor
    public enum Action {

        PERFORM_RESPAWN(0),
        REQUEST_STATS(1),
        INVENTORY_ACHIEVEMENT(2);

        public final int id;

        public static Action search(int value) {
            for (Action action : values()) {
                if (action.id == value) return action;
            }
            return null;
        }

    }
}
