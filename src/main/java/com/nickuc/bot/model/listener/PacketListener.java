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

package com.nickuc.bot.model.listener;

import com.nickuc.bot.io.packets.Packet;
import com.nickuc.bot.model.Connection;

public interface PacketListener extends Listener {

    default void send(Connection connection, Packet packet) throws Exception {}

    default void receive(Connection connection, Packet packet) throws Exception {}

    default void err(Connection connection, Packet packet, Packet.DirectionData direction, Throwable throwable) throws Exception {}

}
