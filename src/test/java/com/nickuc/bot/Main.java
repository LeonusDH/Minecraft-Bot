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

package com.nickuc.bot;

import com.nickuc.bot.io.packets.Packet;
import com.nickuc.bot.io.packets.game.client.PlayerPosition;
import com.nickuc.bot.io.packets.login.server.LoginSuccess;
import com.nickuc.bot.model.Callback;
import com.nickuc.bot.model.Connection;
import com.nickuc.bot.model.listener.PacketListener;
import com.nickuc.bot.utils.Color;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Timer;

public class Main {

    public static final Timer timer = new Timer();
    public static final Random random = new Random();

    public static void main(String[] args) {

        //File file = new File("/home/nickuc/Área de Trabalho/proxies.txt");
        InetSocketAddress server = new InetSocketAddress("142.44.215.25", 10000);

        Connection connection = new Connection();
        connection.addListener(new PacketListener() {
            @Override
            public void receive(Connection connection, Packet packet) throws Exception {

                if (packet instanceof Packet.Unknown) {
                    connection.print(Color.ANSI_RED + "Pacote desconhecido, id: 0x" + Integer.toHexString(packet.<Packet.Unknown>convert().id) + ", state: " + connection.getState());
                }

                if (packet instanceof LoginSuccess) {
                    PlayerPosition playerPosition = new PlayerPosition(true);
                    playerPosition.prepare(connection.getThreshold());
                    connection.runTaskTimer(() -> connection.send(playerPosition), 1000, 1000);
                }
            }
        });

        connection.connect(server, 8000, new Callback<Connection>() {
            @Override
            public void success() {
                try {
                    System.out.println("Connected.");
                    connection.login("NickBot");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void throwable(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }

}