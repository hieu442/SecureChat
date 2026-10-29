package securechat.client;

import securechat.model.ChatPacket;
import securechat.model.FilePacket;
import securechat.model.KeyExchangePacket;
import securechat.model.PublicKeyPacket;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.PublicKey;

public class ModernClientUI extends JFrame {

    private JPanel chatContainer;
    private JScrollPane chatScrollPane;
    private JTextField inputField;
    private JButton sendBtn;
    private JButton fileBtn;
    private JLabel statusLabel;
    private JLabel usernameLabel;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey myDesKey;

    private String myUsername;
    private String serverIP;

    private final Color PRIMARY = new Color(37, 99, 235);
    private final Color BACKGROUND = new Color(241, 245, 249);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(30, 41, 59);
    private final Color MUTED = new Color(100, 116, 139);
    private final Color BORDER = new Color(226, 232, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color RED = new Color(239, 68, 68);

    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 19);

    public ModernClientUI() {
        serverIP = JOptionPane.showInputDialog(null, "Nhập địa chỉ IP của Server:", "Kết nối Server", JOptionPane.QUESTION_MESSAGE);
        if (serverIP == null || serverIP.trim().isEmpty()) System.exit(0);
        serverIP = serverIP.trim();

        myUsername = JOptionPane.showInputDialog(null, "Nhập tên hiển thị của bạn:", "Thông tin người dùng", JOptionPane.QUESTION_MESSAGE);
        if (myUsername == null || myUsername.trim().isEmpty()) System.exit(0);
        myUsername = myUsername.trim();

        setTitle("SecureChat - " + myUsername);
        setSize(850, 700);
        setMinimumSize(new Dimension(700, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        createHeader();
        createChatArea();
        createInputArea();

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        fileBtn.addActionListener(e -> sendFile());

        connectToServer();
    }

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 78));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        left.setOpaque(false);
        JLabel logo = new JLabel("[S]");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(PRIMARY);
        left.add(logo);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("SecureChat");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Private & encrypted communication");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);
        left.add(titlePanel);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 15));
        right.setOpaque(false);
        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        usernameLabel = new JLabel(myUsername);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usernameLabel.setForeground(TEXT);
        statusLabel = new JLabel("● Offline");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        userPanel.add(usernameLabel);
        userPanel.add(statusLabel);
        right.add(userPanel);
        header.add(right, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void createChatArea() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(18, 22, 10, 22));

        JPanel chatTitlePanel = new JPanel(new BorderLayout());
        chatTitlePanel.setOpaque(false);
        JLabel chatTitle = new JLabel("Secure Chat Room");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatTitle.setForeground(TEXT);
        JLabel encryption = new JLabel("SECURE CONNECTION");
        encryption.setFont(new Font("Segoe UI", Font.BOLD, 11));
        encryption.setForeground(GREEN);
        chatTitlePanel.add(chatTitle, BorderLayout.WEST);
        chatTitlePanel.add(encryption, BorderLayout.EAST);
        center.add(chatTitlePanel, BorderLayout.NORTH);

        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(BACKGROUND);
        chatContainer.setBorder(new EmptyBorder(10, 5, 10, 5));

        chatScrollPane = new JScrollPane(chatContainer);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.setBackground(BACKGROUND);
        chatScrollPane.getViewport().setBackground(BACKGROUND);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        center.add(chatScrollPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void createInputArea() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(WHITE);
        inputPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        fileBtn = new JButton("FILE");
        fileBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        styleButton(fileBtn, WHITE, TEXT);
        fileBtn.setToolTipText("Gửi file");
        fileBtn.setPreferredSize(new Dimension(90, 46));

        inputField = new JTextField();
        inputField.setFont(MAIN_FONT);
        inputField.setForeground(TEXT);
        inputField.setBackground(new Color(248, 250, 252));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        sendBtn = new JButton("GỬI");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendBtn.setForeground(WHITE);
        sendBtn.setBackground(PRIMARY);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.setPreferredSize(new Dimension(90, 46));

        inputPanel.add(fileBtn, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        bottom.add(inputPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel("• " + message);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(MUTED);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            chatContainer.add(label);
            chatContainer.add(Box.createVerticalStrut(2));
            refreshChat();
        });
    }

    private void appendChatMessage(String sender, String message, boolean isSelf) {
        JPanel row = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 3));
        row.setOpaque(false);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);

        JLabel nameLabel = new JLabel(sender);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(nameLabel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        messageArea.setColumns(20);
        messageArea.setRows(1);

        wrapper.add(messageArea, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(wrapper);

        row.add(messagePanel);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, messagePanel.getPreferredSize().height + 10));

        chatContainer.add(row);
        chatContainer.revalidate();
        chatContainer.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void refreshChat() {
        chatContainer.revalidate();
        chatContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void setConnectionStatus(boolean online) {
        SwingUtilities.invokeLater(() -> {
            if (online) {
                statusLabel.setText("● Online");
                statusLabel.setForeground(GREEN);
            } else {
                statusLabel.setText("● Offline");
                statusLabel.setForeground(RED);
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                appendSystemMessage("Đang kết nối tới Server...");
                socket = new Socket(serverIP, 8888);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                appendSystemMessage("Đã kết nối đến Server thành công!");
                setConnectionStatus(true);

                Object obj = in.readObject();
                if (obj instanceof PublicKeyPacket) {
                    PublicKey serverPubKey = ((PublicKeyPacket) obj).getPublicKey();
                    KeyGenerator keyGen = KeyGenerator.getInstance("DES");
                    myDesKey = keyGen.generateKey();

                    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    rsaCipher.init(Cipher.ENCRYPT_MODE, serverPubKey);
                    byte[] encryptedKey = rsaCipher.doFinal(myDesKey.getEncoded());

                    synchronized (out) {
                        out.writeObject(new KeyExchangePacket(encryptedKey));
                        out.flush();
                        out.reset();
                    }
                    appendSystemMessage("Bắt tay bảo mật RSA-DES hoàn tất.");
                }

                while (true) {
                    Object incoming = in.readObject();
                    if (incoming instanceof ChatPacket) {
                        ChatPacket chat = (ChatPacket) incoming;
                        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
                        desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
                        byte[] decryptedBytes = desCipher.doFinal(chat.getEncryptedData());
                        String message = new String(decryptedBytes, StandardCharsets.UTF_8);
                        appendChatMessage(chat.getSenderName(), message, chat.getSenderName().equals(myUsername));
                    } else if (incoming instanceof FilePacket) {
                        receiveFile((FilePacket) incoming);
                    }
                }

            } catch (Exception e) {
                setConnectionStatus(false);
                appendSystemMessage("Đã mất kết nối tới Server.");
                System.out.println("[CLIENT ERROR] " + e.getMessage());
            }
        }).start();
    }

    private void receiveFile(FilePacket file) {
        try {
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
            byte[] decryptedFile = desCipher.doFinal(file.getEncryptedFileData());

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] calculatedHash = md5.digest(decryptedFile);

            if (!MessageDigest.isEqual(calculatedHash, file.getMd5Hash())) {
                appendSystemMessage("File nhận được không hợp lệ!");
                return;
            }

            appendChatMessage(file.getSenderName(), "Đã nhận tệp: " + file.getFileName(), false);

            SwingUtilities.invokeLater(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Lưu file nhận được");
                chooser.setSelectedFile(new File(file.getFileName()));
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File saveFile = chooser.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        fos.write(decryptedFile);
                        appendSystemMessage("Đã lưu file: " + saveFile.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "File đã được lưu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Không thể lưu file:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

        } catch (Exception e) {
            appendSystemMessage("Không thể giải mã file.");
            System.out.println("[FILE ERROR] " + e.getMessage());
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (myDesKey == null) {
            appendSystemMessage("Chưa hoàn tất kết nối bảo mật.");
            return;
        }
        if (out == null) {
            appendSystemMessage("Chưa kết nối tới Server.");
            return;
        }

        try {
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(textBytes);

            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
            byte[] encryptedData = desCipher.doFinal(textBytes);

            ChatPacket packet = new ChatPacket(myUsername, encryptedData, hash);

            synchronized (out) {
                out.writeObject(packet);
                out.flush();
                out.reset();
            }

            appendChatMessage("Bạn", text, true);
            inputField.setText("");
            inputField.requestFocus();

        } catch (Exception e) {
            appendSystemMessage("Lỗi gửi tin nhắn: " + e.getMessage());
        }
    }

    private void sendFile() {
        if (myDesKey == null) {
            appendSystemMessage("Chưa hoàn tất kết nối bảo mật.");
            return;
        }
        if (out == null) {
            appendSystemMessage("Chưa kết nối tới Server.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file để gửi");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(fileBytes);

            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
            byte[] encryptedData = desCipher.doFinal(fileBytes);

            FilePacket packet = new FilePacket(myUsername, selectedFile.getName(), encryptedData, hash);

            synchronized (out) {
                out.writeObject(packet);
                out.flush();
                out.reset();
            }

            appendChatMessage("Bạn", "Đã gửi tệp: " + selectedFile.getName(), true);

        } catch (Exception e) {
            appendSystemMessage("Lỗi gửi file: " + e.getMessage());
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color backgroundColor;
        private final int radius;

        public RoundedPanel(Color backgroundColor, int radius) {
            this.backgroundColor = backgroundColor;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            ModernClientUI ui = new ModernClientUI();
            ui.setVisible(true);
        });
    }
}