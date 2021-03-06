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

package com.nickuc.bot.io.packets;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidPacketException extends RuntimeException {

    public InvalidPacketException(String message) {
        super(message);
    }

    public InvalidPacketException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
