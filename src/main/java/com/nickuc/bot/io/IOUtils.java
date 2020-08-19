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

package com.nickuc.bot.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class IOUtils {

    public static int getVarIntSize(int val) {
        return (val & 0xFFFFFF80) == 0 ? 1 : (val & 0xFFFFC000) == 0 ? 2 : (val & 0xFFE00000) == 0 ? 3 : (val & 0xF0000000) == 0 ? 4 : 5;
    }

    public static void putVarInt(byte[] buf, int pos, int val) {
        while ((val & ~0x7F) != 0) {
            buf[pos++] = (byte)((val & 0x7F) | 0x80);
            val >>>= 7;
        }
        buf[pos++] = (byte)val;
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        return readVarInt(in, 5);
    }

    public static int readVarInt(DataInputStream in, int maxBytes) throws IOException {
        int out = 0;
        int bytes = 0;
        byte temp;
        do {
            temp = in.readByte();

            out |= (temp & 0x7F) << (bytes++ * 7);

            if (bytes > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }

        } while ((temp & 0x80) == 0x80);

        return out;
    }

    public static void writeVarInt(int value, DataOutputStream out) throws IOException {
        int part;
        do {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            out.writeByte(part);
        } while (value != 0);
    }

    public static byte[] readArray(DataInputStream in) throws IOException {
        return readArray(in, in.available());
    }

    public static byte[] readArray(DataInputStream in, int limit) throws IOException {
        int len = readVarInt(in);
        if (len <= limit) {
            byte[] ret = new byte[len];
            in.readFully(ret);
            return ret;
        }
        return null;
    }

    public static void writeArray(byte[] b, DataOutputStream out) throws IOException {
        if (b.length <= Short.MAX_VALUE) {
            writeVarInt(b.length, out);
            out.write(b);
        }
    }

    public static String readString(DataInputStream in) throws IOException {
        int len = readVarInt(in);
        if (len > Short.MAX_VALUE) {
            return null;
        }
        byte[] b = new byte[len];
        in.read(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    public static boolean writeString(String s, DataOutputStream out) throws IOException {
        if (s.length() > Short.MAX_VALUE) {
            return false;
        }
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(b.length, out);
        out.write(b);
        return true;
    }

    public static void writeUUID(UUID value, DataOutputStream out) throws IOException {
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
    }

    public static UUID readUUID(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    public static byte[] deflate(byte[] input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[4096];

        Deflater def = new Deflater(9);
        def.setInput(input);
        def.finish();

        while (!def.finished()) {
            int bytes = def.deflate(buf);
            baos.write(buf, 0, bytes);
        }
        def.end();

        return baos.toByteArray();
    }

    public static byte[] inflate(byte[] input) throws DataFormatException {
        Inflater inf = new Inflater();
        inf.setInput(input, 0, input.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[4096];

        while (!inf.finished()) {
            int bytes = inf.inflate(buf);
            baos.write(buf, 0, bytes);
        }
        inf.end();

        return baos.toByteArray();
    }

}
