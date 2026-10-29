package securechat.server;

import javax.swing.SwingUtilities;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class ServerMain {
    private static final int PORT = 8888;
    public static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static PublicKey serverPublicKey;
    public static PrivateKey serverPrivateKey;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI();
            ui.setVisible(true);
        });

        new Thread(() -> {
            startServer();
        }).start();
    }

    private static void startServer() {
        try {
            generateRSAKeys();
            ServerUI.appendLog("[OK] Đã tạo thành công cặp khóa RSA 2048-bit.");

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                ServerUI.appendLog("======================================");
                ServerUI.appendLog("       SECURE CHAT SERVER");
                ServerUI.appendLog("======================================");
                ServerUI.appendLog("[OK] Server đang chạy...");
                ServerUI.appendLog("[OK] Port: " + PORT);
                ServerUI.appendLog("[OK] RSA: 2048-bit");
                ServerUI.setServerStatus(true);

                while (true) {
                    Socket socket = serverSocket.accept();
                    String clientIP = socket.getInetAddress().getHostAddress();
                    ServerUI.appendLog("[+] Client kết nối: " + clientIP);

                    ClientHandler handler = new ClientHandler(socket);
                    clients.add(handler);
                    ServerUI.updateClientCount(clients.size());
                    ServerUI.appendLog("[INFO] Tổng số Client: " + clients.size());

                    Thread clientThread = new Thread(handler);
                    clientThread.start();
                    ServerUI.appendLog("[OK] Đã tạo Thread cho Client.");
                }
            }
        } catch (Exception e) {
            ServerUI.setServerStatus(false);
            ServerUI.appendLog("[LỖI SERVER] " + e.getMessage());
        }
    }

    private static void generateRSAKeys() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        serverPublicKey = keyPair.getPublic();
        serverPrivateKey = keyPair.getPrivate();
    }
}