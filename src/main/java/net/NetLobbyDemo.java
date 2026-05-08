package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * 控制台演示：在不改 GUI 的前提下，单独启动房主大厅服务器或客户端加入。
 * <p>
 * IDE / 命令行传参示例：
 * <pre>
 *   房主（本机开服）：{@code NetLobbyDemo host 58888 房主昵称}
 *   加入（连另一台）：{@code NetLobbyDemo join 127.0.0.1 58888 玩家2}
 * </pre>
 * 房主启动后控制台输入：<br>
 * {@code ready} — 切换自己是否「准备」（会广播 {@link Protocol#LOBBY}）；<br>
 * {@code stop} — 结束服务器并退出。<br>
 * 加入方输入：<br>
 * {@code ready} — 切换准备；{@code quit} — 断开退出。
 */
public final class NetLobbyDemo {

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                printUsage();
                return;
            }
            switch (args[0].toLowerCase()) {
                case "host":
                    runHost(args);
                    break;
                case "join":
                    runJoin(args);
                    break;
                default:
                    printUsage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("""
                用法:
                  房主（TCP 监听，不是网页）:  NetLobbyDemo host <端口> [房主昵称]
                  加入同一局域网/本机测试:       NetLobbyDemo join <房主IP或127.0.0.1> <端口> [昵称]

                说明: 仍是 Socket 字节流文本协议（见 Protocol），无法在浏览器打开。
                控制台命令: ready = 切换准备状态; stop(房主)/quit(加入) = 退出
                """);
    }

    private static void runHost(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
            return;
        }
        int port = Integer.parseInt(args[1]);
        String nick = args.length > 2 ? args[2] : "Host";

        HostServer server = new HostServer(port, nick);
        server.setHostListener(NetLobbyDemo::printMessage);
        server.start();

        System.out.println("房主大厅已监听，端口: " + server.getBoundPort());
        System.out.println("其他玩家在「加入」里填同一 IP + 端口。输入 ready 切换准备，输入 stop 结束。");

        boolean[] hostReady = {false};
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            String cmd = line.trim();
            if (cmd.isEmpty()) {
                continue;
            }
            if ("stop".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                server.stop();
                System.out.println("已停止监听。");
                break;
            }
            if ("ready".equalsIgnoreCase(cmd)) {
                hostReady[0] = !hostReady[0];
                server.setHostReady(hostReady[0]);
                System.out.println("房主准备状态: " + hostReady[0]);
            } else if ("help".equalsIgnoreCase(cmd)) {
                System.out.println("命令: ready | stop");
            } else {
                System.out.println("未知命令，输入 help");
            }
        }
    }

    private static void runJoin(String[] args) throws IOException {
        if (args.length < 3) {
            printUsage();
            return;
        }
        String ip = args[1];
        int port = Integer.parseInt(args[2]);
        String nick = args.length > 3 ? args[3] : "Guest";

        RemoteClient client = new RemoteClient();
        client.setListener(NetLobbyDemo::printMessage);
        client.connect(ip, port, nick);

        System.out.println("已向 " + ip + ":" + port + " 发送 JOIN。输入 ready 切换准备，输入 quit 退出。");

        boolean[] selfReady = {false};
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            String cmd = line.trim();
            if (cmd.isEmpty()) {
                continue;
            }
            if ("quit".equalsIgnoreCase(cmd) || "stop".equalsIgnoreCase(cmd)) {
                client.close();
                System.out.println("已断开。");
                break;
            }
            if ("ready".equalsIgnoreCase(cmd)) {
                selfReady[0] = !selfReady[0];
                client.sendReady(selfReady[0]);
                System.out.println("你已切换准备状态: " + selfReady[0]);
            } else if ("help".equalsIgnoreCase(cmd)) {
                System.out.println("命令: ready | quit");
            } else {
                System.out.println("未知命令，输入 help");
            }
        }
    }

    private static void printMessage(Protocol.Message msg) {
        System.out.println("[收到] type=" + msg.type() + " args=" + Arrays.toString(msg.args()));
    }

    private NetLobbyDemo() {}
}
