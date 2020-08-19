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

package com.nickuc.bot.io.buf;

import com.nickuc.bot.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class InputBuffer extends IOUtils implements Closeable {

    public final ByteArrayInputStream bais;
    public final DataInputStream dis;

    public InputBuffer(byte[] data) {
        dis = new DataInputStream(bais = new ByteArrayInputStream(data));
    }

    public short readShort() throws IOException {
        return dis.readShort();
    }

    public long readLong() throws IOException {
        return dis.readLong();
    }

    public boolean readBoolean() throws IOException {
        return dis.readBoolean();
    }

    public int readInt() throws IOException {
        return dis.readInt();
    }

    public int readUByte() throws IOException {
        return dis.readByte() & 0xFF;
    }

    public byte readByte() throws IOException {
        return dis.readByte();
    }

    public int readVarInt() throws IOException {
        return readVarInt(dis);
    }

    public int readVarInt(int maxBytes) throws IOException {
        return readVarInt(dis, maxBytes);
    }

    public byte[] readArray() throws IOException {
        return readArray(dis);
    }

    public byte[] readArray(int limit) throws IOException {
        return readArray(dis, limit);
    }

    public String readString() throws IOException {
        return readString(dis);
    }

    public UUID readUUID() throws IOException {
        return readUUID(dis);
    }

    @Override
    public void close() throws IOException {
        bais.close();
        dis.close();
    }
}
