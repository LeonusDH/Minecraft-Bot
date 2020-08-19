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

import com.nickuc.bot.io.packets.Packet;
import com.nickuc.bot.io.packets.game.client.KeepAliveResponse;
import com.nickuc.bot.io.packets.game.server.GameDisconnect;
import com.nickuc.bot.io.packets.game.server.KeepAliveRequest;
import com.nickuc.bot.io.packets.handshaking.Handshake;
import com.nickuc.bot.io.packets.login.client.LoginStart;
import com.nickuc.bot.io.packets.login.server.EncryptionRequest;
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
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class Connection {

    private static final Timer timer = new Timer();

    private final Proxy proxy;
    private final List<Listener> listeners = new ArrayList<>();

    @Getter private Thread thread;
    private Socket socket;
    private InetSocketAddress server;

    private boolean looping;
    private long throttle;
    private long keepAlive;
    private long timeout;

    private String name;
    @Getter private Packet.State state;
    @Getter private int threshold;

    public Connection() {
        this(Proxy.NO_PROXY);
    }

    public Connection(Proxy proxy) {
        this.proxy = proxy;
    }

    public void connect(InetSocketAddress server, Callback<Connection> callback) {
        connect(server, 7500, 20000, callback);
    }

    public void connect(InetSocketAddress server, int connectTimeout, int keepAliveTimeout, Callback<Connection> callback) {
        thread = new Thread(() -> {
            try {
                socket = new Socket(proxy);
                socket.connect(this.server = server, connectTimeout);
                keepAlive = System.currentTimeMillis();
                timeout = keepAliveTimeout;
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
            print(Color.ANSI_RED + "Disconnected: " + (message == null ? "Unknown" : message));
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
        return socket != null && !socket.isClosed() && socket.isConnected() && thread.isAlive();
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
        timer.schedule(task, delay);
        return task;
    }

    public TimerTask runTaskTimer(Consumer<TimerTask> consumer, long initDelay, long delay) {
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
        timer.scheduleAtFixedRate(task, initDelay, delay);
        return task;
    }

    public void login(String name) throws IllegalAccessException {
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
        } else {
            socketLoop();
        }
    }

    public void status() throws IllegalAccessException {
        if (!isConnected()) {
            throw new IllegalAccessException("Socket is not connected!");
        }
        prepareListeners(Packet.State.STATUS);
        Handshake handshake = new Handshake(47, server.getAddress().getHostAddress(), server.getPort(), state);
        if (!send(handshake, new StatusRequest())) {
            disconnect("Failed to send handshake & status request, aborting connection...");
        } else {
            socketLoop();
        }
    }

    private void prepareListeners(Packet.State state) {
        this.state = state;
        switch (state) {
            case LOGIN:
                listeners.add(new PacketListener() {
                    @Override
                    public void receive(Connection connection, Packet packet) throws Exception {
                        if (connection.state == Packet.State.GAME) {
                            if (packet instanceof KeepAliveRequest) {
                                connection.send(new KeepAliveResponse(packet.<KeepAliveRequest>convert().getPayload()));
                                return;
                            }
                            if (packet instanceof GameDisconnect) {
                                disconnect(packet.<GameDisconnect>convert().getJson());
                            }
                        } else {
                            if (packet instanceof SetCompression) {
                                threshold = packet.<SetCompression>convert().getThreshold();
                                return;
                            }
                            if (packet instanceof EncryptionRequest) {
                                disconnect("The server has online mode enabled.");
                                return;
                            }
                            if (packet instanceof LoginSuccess) {
                                connection.state = Packet.State.GAME;
                                return;
                            }
                            if (packet instanceof LoginDisconnect) {
                                disconnect(packet.<LoginDisconnect>convert().getJson());
                            }
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

    public void socketLoop() {
        if (looping) {
            throw new IllegalStateException("Already in loop!");
        }
        looping = true;
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            while (isConnected()) {
                long current = System.currentTimeMillis();
                if (current - throttle >= 5) {
                    if (System.currentTimeMillis() - keepAlive > timeout) {
                        disconnect("Connection Timeout");
                        break;
                    }
                    receive(input);
                    throttle = current;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive(DataInputStream in) {
        Packet packet = null;
        try {
            if (in.available() > 0) {
                keepAlive = System.currentTimeMillis();
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
        } catch (SocketException e) {
            disconnect(e.getClass().getSimpleName() + ": " + e.getMessage());
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
        if (!isConnected()) return false;

        try {
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
        } catch (SocketException e) {
            disconnect(e.getClass().getSimpleName() + ": " + e.getMessage());
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
