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

package com.nickuc.bot.io.packets.game.server;

import com.nickuc.bot.io.buf.InputBuffer;
import com.nickuc.bot.io.buf.OutputBuffer;
import com.nickuc.bot.io.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@AllArgsConstructor @RequiredArgsConstructor @Getter @ToString
public class UpdateHealth extends Packet {

    private float health;
    private int food;
    private float foodSaturation;

    @Override
    protected void read(InputBuffer in) throws IOException {
        health = in.readFloat();
        food = in.readVarInt();
        foodSaturation = in.readFloat();
    }

    @Override
    protected void write(OutputBuffer out) throws IOException {
        out.writeFloat(health);
        out.writeVarInt(food);
        out.writeFloat(foodSaturation);
    }
}
