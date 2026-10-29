package securechat.server;

import securechat.model.ChatPacket;
import securechat.model.FilePacket;
import securechat.model.KeyExchangePacket;
import securechat.model.PublicKeyPacket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey desKey;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new PublicKeyPacket(ServerMain.serverPublicKey));
            out.flush();
            ServerUI.appendLog("[SECURITY] Đã gửi Public Key cho Client.");

            Object obj = in.readObject();
            if (obj instanceof KeyExchangePacket) {
                KeyExchangePacket packet = (KeyExchangePacket) obj;
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                rsaCipher.init(Cipher.DECRYPT_MODE, ServerMain.serverPrivateKey);
                byte[] decryptedKey = rsaCipher.doFinal(packet.getEncryptedDesKey());
                desKey = new SecretKeySpec(decryptedKey, "DES");
                ServerUI.appendLog("[SECURITY] RSA-DES handshake hoàn tất.");
            } else {
                ServerUI.appendLog("[WARNING] Client không gửi KeyExchangePacket.");
                return;
            }

            while (true) {
                Object incoming = in.readObject();

                if (incoming instanceof ChatPacket) {
                    ChatPacket chat = (ChatPacket) incoming;
                    try {
                        byte[] decryptedData = decrypt(chat.getEncryptedData());
                        if (!verifyMD5(decryptedData, chat.getMd5Hash())) {
                            ServerUI.appendLog("[WARNING] MD5 tin nhắn không hợp lệ.");
                            continue;
                        }
                        String message = new String(decryptedData, StandardCharsets.UTF_8);
                        ServerUI.appendLog("[RECEIVE] " + chat.getSenderName() + ": " + message);
                        broadcastChat(chat.getSenderName(), decryptedData);
                    } catch (Exception e) {
                        ServerUI.appendLog("[ERROR] Không thể xử lý tin nhắn: " + e.getMessage());
                    }
                } else if (incoming instanceof FilePacket) {
                    FilePacket file = (FilePacket) incoming;
                    ServerUI.appendLog("[RECEIVE] File từ: " + file.getSenderName() + " - " + file.getFileName());
                    try {
                        byte[] decryptedFile = decrypt(file.getEncryptedFileData());
                        if (!verifyMD5(decryptedFile, file.getMd5Hash())) {
                            ServerUI.appendLog("[WARNING] MD5 file không hợp lệ.");
                            continue;
                        }
                        broadcastFile(file.getSenderName(), file.getFileName(), decryptedFile);
                    } catch (Exception e) {
                        ServerUI.appendLog("[ERROR] Không thể xử lý file: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            ServerUI.appendLog("[LỖI CLIENT] " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private byte[] decrypt(byte[] encryptedData) throws Exception {
        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher.init(Cipher.DECRYPT_MODE, desKey);
        return desCipher.doFinal(encryptedData);
    }

    private byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher.init(Cipher.ENCRYPT_MODE, key);
        return desCipher.doFinal(data);
    }

    private boolean verifyMD5(byte[] data, byte[] receivedHash) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] calculatedHash = md5.digest(data);
        return MessageDigest.isEqual(calculatedHash, receivedHash);
    }

    private void broadcastChat(String senderName, byte[] message) {
        for (ClientHandler client : ServerMain.clients) {
            if (client == this) continue;
            try {
                byte[] encryptedData = client.encrypt(message, client.desKey);
                byte[] hash = MessageDigest.getInstance("MD5").digest(message);
                ChatPacket packet = new ChatPacket(senderName, encryptedData, hash);
                client.sendPacket(packet);
            } catch (Exception e) {
                ServerUI.appendLog("[ERROR] Không thể gửi tin nhắn tới Client.");
            }
        }
    }

    private void broadcastFile(String senderName, String fileName, byte[] fileData) {
        for (ClientHandler client : ServerMain.clients) {
            if (client == this) continue;
            try {
                byte[] encryptedData = client.encrypt(fileData, client.desKey);
                byte[] hash = MessageDigest.getInstance("MD5").digest(fileData);
                FilePacket packet = new FilePacket(senderName, fileName, encryptedData, hash);
                client.sendPacket(packet);
            } catch (Exception e) {
                ServerUI.appendLog("[ERROR] Không thể gửi file tới Client.");
            }
        }
    }

    private synchronized void sendPacket(Object packet) throws Exception {
        if (out != null) {
            out.writeObject(packet);
            out.flush();
            out.reset();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            ServerUI.appendLog("[WARNING] Không thể đóng Client.");
        }
        ServerMain.clients.remove(this);
        try {
            ServerUI.updateClientCount(ServerMain.clients.size());
        } catch (Exception ignored) {
        }
    }
}