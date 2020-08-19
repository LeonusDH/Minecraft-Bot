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

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Proxies {

    private static long throttle;

    private static final List<Proxy> proxies = new ArrayList<>();
    private static int currentIndex;

    private static int checkerThreads;
    private static int checkerIndex;

    private static List<String> readLines(File file) {
        List<String> temp = new ArrayList<>();
        if (file.exists()) {
            BufferedReader bufferedReader;
            try (InputStream inputStream = new FileInputStream(file); InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    temp.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    private static Proxy available(SocketAddress address, Proxy.Type... types) throws Exception {
        for (Proxy.Type type : types) {
            Proxy proxy = new Proxy(type, address);
            if (available(proxy, "https://cloudflare.com", 2000)) return proxy;
        }
        return null;
    }

    private static boolean available(Proxy proxy, String website, int timeout) throws Exception {
        try {
            URL url = new URL(website);
            URLConnection http = url.openConnection(proxy);
            http.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            http.setRequestProperty("accept-language", "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7,fr;q=0.6");
            http.setRequestProperty("cache-control", "max-age=0");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");

            String host = website.replace("https://", "").replace("http://", "");
            http.setRequestProperty("host", host);
            http.setConnectTimeout(timeout);
            http.setReadTimeout(timeout * 2);
            http.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void parseProxies(File proxiesFile, int maxThreads, Runnable callback) throws IOException {
        if (proxiesFile == null) {
            throw new IllegalArgumentException("File cannot be null!");
        }
        if (!proxiesFile.exists()) {
            throw new FileNotFoundException("Proxies file does not exist! (" + proxiesFile.getCanonicalPath() + ").");
        }

        List<String> lines = readLines(proxiesFile);
        System.out.println(Color.ANSI_YELLOW + "[PROXIES] Detected " + lines.size() + " proxies, checking in 5 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!lines.isEmpty()) {
            while (true) {
                if (checkerThreads >= maxThreads) {
                    long current = System.currentTimeMillis();
                    if (current - throttle >= 2000) {
                        throttle = current;
                        System.out.println(Color.ANSI_YELLOW + "[PROXIES] Current checking progress: (" + checkerIndex + "/" + lines.size() + ")" + Color.ANSI_RESET);
                    }
                    continue;
                }

                int remaining = lines.size() - checkerIndex;
                if (remaining > 1) {
                    checkerThreads++;
                    String line = lines.get(checkerIndex);
                    checkerIndex++;
                    Thread thread = new Thread(() -> {
                        try {
                            String host = line.split(":")[0];
                            int port = Integer.parseInt(line.split(":")[1]);
                            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
                            Proxy proxy;
                            if (!socketAddress.isUnresolved() && (proxy = available(socketAddress, Proxy.Type.HTTP, Proxy.Type.SOCKS)) != null) {
                                proxies.add(proxy);
                                System.out.println(Color.ANSI_GREEN + "[PROXY CHECKER - " + Thread.currentThread().getName() + "] Valid proxy: " + line + ", valid: (" + proxies.size() + "/" + checkerIndex + ")" + Color.ANSI_RESET);
                            } else {
                                System.out.println(Color.ANSI_RED + "[PROXY CHECKER - " + Thread.currentThread().getName() + "] Invalid proxy: " + line + ", valid: (" + proxies.size() + "/" + checkerIndex + ")" + Color.ANSI_RESET);
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println("[PROXY CHECKER - " + Thread.currentThread().getName() + "] Failed to load line: invalid format? [original: " + (line.length() > 48 ? line.substring(0, 48) : line) + "]");
                        } catch (Exception e) {
                            System.err.println("[PROXY CHECKER - " + Thread.currentThread().getName() + "] Failed to load proxy: " + e.getLocalizedMessage() + " [original: " + (line.length() > 48 ? line.substring(0, 48) : line) + "]");
                        } finally {
                            checkerThreads--;
                        }
                    });
                    thread.setName("THREAD #" + checkerThreads);
                    thread.start();
                } else if (checkerThreads < 2) {
                    System.out.println(Color.ANSI_GREEN + "[PROXIES] Finished, available valid proxies: (" + proxies.size() + "/" + lines.size() + ")" + Color.ANSI_RESET);
                    lines.clear();
                    callback.run();
                    break;
                }
            }
            if (proxies.isEmpty()) {
                throw new IllegalArgumentException("Cannot find valid proxies!");
            }
        } else {
            throw new IllegalArgumentException("Proxies file is empty! (" + proxiesFile.getCanonicalPath() + ").");
        }
    }

    public static Proxy nextProxy() {
        if (proxies.isEmpty()) return null;

        if (proxies.size() - currentIndex < 1) {
            currentIndex = 0;
        }
        Proxy temp = proxies.get(currentIndex);
        currentIndex++;
        return temp;
    }

}
