/**
 * Copyright NickUC
 * -
 * Esta interface pertence ao projeto de NickUC
 * Mais informações: https://nickuc.com
 * -
 * É expressamente proibido alterar o nome do proprietário do código, sem
 * expressar e deixar claramente o link para acesso da source original.
 * -
 * Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package com.nickuc.bot.model;

public interface Callback<T> {

    default void success() {
    }

    default void success(T value) {
        success();
    }

    default void throwable(Throwable throwable) {
    }

}
