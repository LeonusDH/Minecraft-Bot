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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class OutputBuffer extends IOUtils implements Closeable {

    public final ByteArrayOutputStream baos;
    public final DataOutputStream dos;

    public OutputBuffer() throws IOException {
        dos = new DataOutputStream(baos = new ByteArrayOutputStream());
    }

    public void writeShort(short value) throws IOException {
        dos.writeShort(value);
    }

    public void writeLong(long value) throws IOException {
        dos.writeLong(value);
    }

    public void writeByte(byte val) throws IOException {
        dos.writeByte(val & 0xFF);
    }

    public void writeBoolean(boolean val) throws IOException {
        dos.writeBoolean(val);
    }

    public void writeUnsignedByte(int val) throws IOException
    {
        dos.writeByte(val);
    }

    public void writeVarInt(int value) throws IOException {
        writeVarInt(value, dos);
    }

    public void writeFloat(float value) throws IOException {
        dos.writeFloat(value);
    }

    public void writeArray(byte[] b) throws IOException {
        writeArray(b, dos);
    }

    public boolean writeString(String s) throws IOException {
        return writeString(s, dos);
    }

    public void writeUUID(UUID value) throws IOException {
        writeUUID(value, dos);
    }

    @Override
    public void close() throws IOException {
        if (baos != null) {
            baos.close();
        }
        if (dos != null) {
            dos.close();
        }
    }
}
