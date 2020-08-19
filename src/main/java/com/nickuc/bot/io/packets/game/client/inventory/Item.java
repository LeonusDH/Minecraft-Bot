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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.NBTIO;

import java.io.IOException;

@AllArgsConstructor @NoArgsConstructor @Getter @ToString
public class Item {

    private boolean present;
    private int itemId;
    private byte itemCount;
    private CompoundTag nbt;

    public Item read(InputBuffer in) throws IOException {
        present = in.readBoolean();
        if (present) {
            itemId = in.readVarInt();
            itemCount = in.readByte();
            nbt = (CompoundTag) NBTIO.readTag(in.dis);
        }
        return this;
    }

    public Item write(OutputBuffer out) throws IOException {
        out.writeBoolean(present);
        out.writeVarInt(itemId);
        out.writeByte(itemCount);
        NBTIO.writeTag(out.dos, nbt);
        return this;
    }

    public static Item newEmpty() {
        return new Item();
    }

}
