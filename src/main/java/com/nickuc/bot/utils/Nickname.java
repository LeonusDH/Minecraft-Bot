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

package com.nickuc.bot.utils;

import java.util.HashSet;
import java.util.Set;

public class Nickname {

    public static String randomNick(String prefix) {
        StringBuilder builder = new StringBuilder();
        String customString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 16; i++) {
            builder.append(customString.charAt((int)(Math.random() * customString.length())));
        }
        String name = prefix + builder;
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    public static Set<String> randomNicks(String prefix, int size) {
        Set<String> tmp = new HashSet<>();
        for (int i = 0; i < size; i++) {
            tmp.add(randomNick(prefix));
        }
        return tmp;
    }

}
