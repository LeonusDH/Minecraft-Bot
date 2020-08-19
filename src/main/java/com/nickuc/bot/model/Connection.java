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

package com.nickuc.bot.model;

import com.nickuc.bot.Main;
import com.nickuc.bot.io.packets.Packet;
import com.nickuc.bot.io.packets.game.client.KeepAliveResponse;
import com.nickuc.bot.io.packets.game.server.GameDisconnect;
import com.nickuc.bot.io.packets.game.server.KeepAliveRequest;
import com.nickuc.bot.io.packets.handshaking.Handshake;
import com.nickuc.bot.io.packets.login.client.LoginStart;
import com.nickuc.bot.io.packets.login.server.LoginDisconnect;
import com.nickuc.bot.io.packets.login.server.LoginSuccess;
import com.nickuc.bot.io.packets.login.server.SetCompression;
import com.nickuc.bot.io.packets.status.client.PingRequest;
import com.nickuc.bot.io.packets.status.client.StatusRequest;
import com.nickuc.bot.io.packets.status.server.PingResponse;
import com.nickuc.bot.io.packets.status.server.StatusResponse;
import com.nickuc.bot.model.listener.Listener;
import com.nickuc.bot.model.listener.PacketListener;
import com.nickuc.bot.utils.Color;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.function.Consumer;

public class Connection {

    private final Proxy proxy;
    private final List<Listener> listeners = new ArrayList<>();

    @Getter private Thread thread;
    private Socket socket;
    private InetSocketAddress server;

    private String name;
    @Getter private Packet.State state;
    @Getter private int threshold;
    private boolean looping;

    public Connection() {
        this(Proxy.NO_PROXY);
    }

    public Connection(Proxy proxy) {
        this.proxy = proxy;
    }

    public void connect(InetSocketAddress server, Callback<Connection> callback) {
        connect(server, 7500, callback);
    }

    public void connect(InetSocketAddress server, int timeout, Callback<Connection> callback) {
        thread = new Thread(() -> {
            try {
                socket = new Socket(proxy);
                socket.connect(this.server = server, timeout);
                callback.success(this);
            } catch (Throwable e) {
                callback.throwable(e);
            }
        });
        thread.start();
    }

    public void print(String message) {
        SocketAddress socketAddress;
        System.out.println(Color.ANSI_YELLOW + "[" + (socket == null || (socketAddress = socket.getRemoteSocketAddress()) == null ? "???" : socketAddress) + (name == null ? "" : " ~ " + name) + "] " + message + Color.ANSI_RESET);
    }

    public void err(String message) {
        SocketAddress socketAddress;
        System.err.println("[" + (socket == null || (socketAddress = socket.getRemoteSocketAddress()) == null ? "???" : socketAddress) + (name == null ? "" : " ~ " + name)  + "] " + message + Color.ANSI_RESET);
    }

    public boolean disconnect() {
        return disconnect(null);
    }

    public boolean disconnect(String message) {
        try {
            close();
            print("Disconnected: " + (message == null ? "Unknown" : message));
            return true;
        } catch (IOException e) {
            err("Failed to close socket.");
        }
        return false;
    }

    private void close() throws IOException {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        listeners.clear();
    }

    public boolean isConnected() {
        return thread != null && !thread.isInterrupted() && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public Connection addListener(Listener listener) {
        if (isConnected()) {
            throw new IllegalStateException("Cannot add a listener while connection is alive!");
        }
        listeners.add(listener);
        return this;
    }

    public TimerTask runTaskLater(Runnable runnable, long delay) {
        return runTaskLater(a -> runnable.run(), delay);
    }

    public TimerTask runTaskTimer(Runnable runnable, long initDelay, long delay) {
        return runTaskTimer(a -> runnable.run(), initDelay, delay);
    }

    public TimerTask runTaskLater(Consumer<TimerTask> consumer, long delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!isConnected()) {
                    cancel();
                    return;
                }
                consumer.accept(this);
            }
        };
        Main.timer.schedule(task, delay);
        return task;
    }

    public TimerTask runTaskTimer(Consumer<TimerTask> consumer, long initDelay, long delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                consumer.accept(this);
            }
        };
        Main.timer.scheduleAtFixedRate(task, initDelay, delay);
        return task;
    }

    public void login(String name) throws IOException, InterruptedException, IllegalAccessException {
        if (!isConnected()) {
            throw new IllegalAccessException("Socket is not connected!");
        }
        if (state != null) {
            throw new IllegalStateException("Already connected!");
        }
        if (name == null) {
            throw new IllegalArgumentException("Invalid nickname!");
        }
        if (name.length() > 16) {
            throw new IllegalArgumentException("Invalid nickname length!");
        }
        this.name = name;
        prepareListeners(Packet.State.LOGIN);
        Handshake handshake = new Handshake(47, server.getAddress().getHostAddress(), server.getPort(), state);
        if (!send(handshake, new LoginStart(name))) {
            disconnect("Failed to send handshake & login start, aborting connection...");
        }
        packetLoop();
    }

    public void status() throws IOException, InterruptedException, IllegalAccessException {
        if (!isConnected()) {
            throw new IllegalAccessException("Socket is not connected!");
        }
        if (state != null) {
            throw new IllegalStateException("Already requested!");
        }
        prepareListeners(Packet.State.STATUS);
        Handshake handshake = new Handshake(47, server.getAddress().getHostAddress(), server.getPort(), state);
        if (!send(handshake, new StatusRequest())) {
            disconnect("Failed to send handshake & status request, aborting connection...");
        }
        packetLoop();
    }

    private void prepareListeners(Packet.State state) {
        this.state = state;
        switch (state) {
            case LOGIN:
                listeners.add(new PacketListener() {
                    @Override
                    public void receive(Connection connection, Packet packet) throws Exception {
                        if (packet instanceof KeepAliveRequest) {
                            connection.send(new KeepAliveResponse(packet.<KeepAliveRequest>convert().getPayload()));
                            return;
                        }
                        if (packet instanceof SetCompression) {
                            threshold = packet.convert(SetCompression.class).getThreshold();
                            return;
                        }
                        if (packet instanceof LoginSuccess) {
                            if (connection.state != Packet.State.LOGIN) {
                                throw new IllegalStateException("Received LoginSuccess in a invalid state! [" + connection.state + "]");
                            }
                            connection.state = Packet.State.GAME;
                            return;
                        }
                        if (packet instanceof LoginDisconnect) {
                            disconnect(packet.<LoginDisconnect>convert().getJson());
                            return;
                        }
                        if (packet instanceof GameDisconnect) {
                            disconnect(packet.<GameDisconnect>convert().getJson());
                        }
                    }
                });
                break;

            case STATUS:
                listeners.add(new PacketListener() {
                    @Override
                    public void receive(Connection connection, Packet packet) throws Exception {
                        if (packet instanceof StatusResponse) {
                            connection.print("Response: " + packet);
                            connection.send(new PingRequest(System.currentTimeMillis()));
                            return;
                        }
                        if (packet instanceof PingResponse) {
                            connection.print("Pong! Took " + (System.currentTimeMillis() - packet.<PingResponse>convert().getPayload()) + "ms");
                        }
                    }
                });
                break;

            default:
                throw new IllegalStateException("Invalid handshake state! [" + state + "]");

        }
    }

    public void packetLoop() throws IOException, InterruptedException {
        if (looping) {
            throw new IllegalStateException("Already in loop!");
        }
        looping = true;
        DataInputStream input = new DataInputStream(socket.getInputStream());
        while (isConnected()) {
            if (!receive(input)) {
                break;
            }
            Thread.sleep(25);
        }
    }

    private boolean receive(DataInputStream in) {
        Packet packet = null;
        try {
            if (thread != null) {
                if (!thread.isAlive() || thread.isInterrupted()) return false;
            }

            if (in.available() > 0 && isConnected()) {
                packet = Packet.read(in, state, threshold);
                for (Listener listener : listeners) {
                    if (!(listener instanceof PacketListener)) continue;

                    try {
                        ((PacketListener) listener).receive(this, packet);
                    } catch (Exception e) {
                        err("[READ] Failed to notify packet listener: " + e.getMessage() + " - [Packet: " + packet + ", Listener: " + listener.getClass().getSimpleName() + "].");
                    }
                }
            }
        } catch (Exception e) {
            for (Listener listener : listeners) {
                if (!(listener instanceof PacketListener)) continue;

                try {
                    ((PacketListener) listener).err(this, packet, Packet.DirectionData.TO_CLIENT, e);
                } catch (Exception ex) {
                    err("[READ] Failed to notify packet listener: " + e.getMessage() + " - [Listener: " + listener.getClass().getSimpleName() + "].");
                }
            }
        }
        return true;
    }

    public boolean send(Packet packet) {
        return send(packet, null);
    }

    public boolean send(Packet... packets) {
        return send(null, packets);
    }

    public boolean send(Callback<Connection> callback, Packet... packets) {
        for (Packet packet : packets) {
            if (!send(packet)) {
                return false;
            }
        }
        if (callback != null) {
            callback.success(this);
        }
        return true;
    }

    public boolean send(Packet packet, Callback<Connection> callback) {
        try {
            if (thread != null) {
                if (!thread.isAlive() || thread.isInterrupted()) return false;
            }

            if (isConnected()) {
                for (Listener listener : listeners) {
                    if (!(listener instanceof PacketListener)) continue;

                    try {
                        ((PacketListener) listener).send(this, packet);
                    } catch (Exception e) {
                        err("[WRITE] Failed to notify packet listener: " + e.getMessage() + " - [Packet: " + packet + ", Listener: " + listener.getClass().getSimpleName() + "].");
                    }
                }
                packet.prepare(threshold);
                socket.getOutputStream().write(packet.buf);
                if (callback != null) {
                    callback.success(this);
                }
                if (packet instanceof Handshake && (state == null || state == Packet.State.HANDSHAKING)) {
                    state = packet.<Handshake>convert().getNextState();
                }
                return true;
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.throwable(e);
            }
            for (Listener listener : listeners) {
                if (!(listener instanceof PacketListener)) continue;

                try {
                    ((PacketListener) listener).err(this, packet, Packet.DirectionData.TO_SERVER, e);
                } catch (Exception ex) {
                    err("[WRITE] Failed to notify packet listener: " + e.getMessage() + " - [Packet: " + packet + ", Listener: " + listener.getClass().getSimpleName() + "].");
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return thread.getName();
    }

}
