package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 房主版联机（最简框架）：\n
 * - 监听端口\n
 * - 接收 JOIN\n
 * - 分配 seat(1..3)，seat0 预留给房主本机 UI\n
 * - 广播 LOBBY（昵称与 ready 状态）\n
 *
 * <p>本类只做「大厅」层面的网络骨架，不触碰游戏逻辑与其他包。</p>
 */
public final class HostServer {

    private final int bindPort;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final String[] nicknames = new String[] {"", "", "", ""};
    private final boolean[] ready = new boolean[4];

    private final Socket[] sockets = new Socket[4];
    private final BufferedWriter[] writers = new BufferedWriter[4];

    private volatile Consumer<Protocol.Message> hostListener;

    public HostServer(int port, String hostNickname) {
        this.bindPort = port;
        nicknames[0] = (hostNickname == null || hostNickname.isBlank()) ? "Host" : hostNickname;
    }

    /** 用于房主本机 UI 获取大厅广播（如 LOBBY）。 */
    public void setHostListener(Consumer<Protocol.Message> listener) {
        this.hostListener = listener;
    }

    public void start() throws IOException {
        if (running.get()) return;
        serverSocket = new ServerSocket(bindPort);
        running.set(true);
        Thread acceptThread = new Thread(this::acceptLoop, "net-host-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        broadcastLobby();
    }

    public int getBoundPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : bindPort;
    }

    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        for (int s = 1; s <= 3; s++) {
            closeSeat(s);
        }
    }

    public void setHostReady(boolean r) {
        ready[0] = r;
        broadcastLobby();
    }

    public String[] getNicknames() {
        return Arrays.copyOf(nicknames, 4);
    }

    public boolean[] getReady() {
        return Arrays.copyOf(ready, 4);
    }

    private void acceptLoop() {
        while (running.get() && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                String first = reader.readLine();
                Protocol.Message join = Protocol.decode(first);
                if (!Protocol.JOIN.equals(join.type())) {
                    socket.close();
                    continue;
                }
                String nick = join.args().length > 0 ? join.args()[0] : "Player";
                int seat = allocateSeat();
                if (seat < 0) {
                    sendAndClose(socket, new Protocol.Message(Protocol.ERROR, new String[] {"ROOM_FULL"}));
                    continue;
                }

                sockets[seat] = socket;
                writers[seat] = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                nicknames[seat] = nick;
                ready[seat] = false;

                send(seat, new Protocol.Message(Protocol.ASSIGN_SEAT, new String[] {String.valueOf(seat)}));
                broadcastLobby();

                Thread readThread = new Thread(() -> readLoop(seat, reader), "net-host-read-" + seat);
                readThread.setDaemon(true);
                readThread.start();
            } catch (IOException e) {
                break;
            }
        }
    }

    private void readLoop(int seat, BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                Protocol.Message msg = Protocol.decode(line);
                if (Protocol.READY.equals(msg.type()) && msg.args().length > 0) {
                    ready[seat] = Boolean.parseBoolean(msg.args()[0]);
                    broadcastLobby();
                }
            }
        } catch (IOException ignored) {
        } finally {
            closeSeat(seat);
            broadcastLobby();
        }
    }

    private int allocateSeat() {
        for (int s = 1; s <= 3; s++) {
            if (sockets[s] == null) return s;
        }
        return -1;
    }

    private void closeSeat(int seat) {
        try {
            if (sockets[seat] != null) sockets[seat].close();
        } catch (IOException ignored) {}
        sockets[seat] = null;
        writers[seat] = null;
        nicknames[seat] = "";
        ready[seat] = false;
    }

    private void sendAndClose(Socket socket, Protocol.Message msg) {
        try {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            w.write(Protocol.encode(msg));
            w.newLine();
            w.flush();
            socket.close();
        } catch (IOException ignored) {}
    }

    private void send(int seat, Protocol.Message msg) {
        if (seat <= 0 || writers[seat] == null) return;
        try {
            writers[seat].write(Protocol.encode(msg));
            writers[seat].newLine();
            writers[seat].flush();
        } catch (IOException e) {
            closeSeat(seat);
        }
    }

    private void broadcastLobby() {
        String n = String.join(",", Arrays.stream(nicknames).map(s -> s == null ? "" : s).toList());
        StringBuilder rb = new StringBuilder();
        for (int i = 0; i < ready.length; i++) {
            if (i > 0) {
                rb.append(',');
            }
            rb.append(ready[i]);
        }
        String r = rb.toString();
        Protocol.Message msg = new Protocol.Message(Protocol.LOBBY, new String[] {n, r});
        for (int s = 1; s <= 3; s++) {
            send(s, msg);
        }
        Consumer<Protocol.Message> l = hostListener;
        if (l != null) l.accept(msg);
    }
}

