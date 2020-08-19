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

package com.nickuc.bot.io.packets.game.client.inventory;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class ClickInventory extends Packet {

    private int windowId;
    private short slot;
    private byte button;
    private short action;
    private byte mode;
    private Item item;

    @Override
    protected void read(InputBuffer in) throws IOException {
        windowId = in.readUByte();
        slot = in.readShort();
        button = in.readByte();
        action = in.readShort();
        mode = in.readByte();
        item = Item.newEmpty().read(in);
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeUByte(windowId);
        out.writeShort(slot);
        out.writeByte(button);
        out.writeShort(action);
        out.writeByte(mode);
        item.write(out);
    }
}
