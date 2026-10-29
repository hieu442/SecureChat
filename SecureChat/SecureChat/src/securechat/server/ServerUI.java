package securechat.server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerUI extends JFrame {
    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color PANEL_COLOR = new Color(30, 41, 59);
    private static final Color CARD_COLOR = new Color(51, 65, 85);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color WHITE = new Color(248, 250, 252);
    private static final Color GRAY = new Color(148, 163, 184);

    private static JTextArea logArea;
    private static JLabel statusLabel;
    private static JLabel clientCountLabel;
    private static JLabel rsaLabel;
    private static JLabel portLabel;

    private JButton clearButton;
    private JButton exitButton;

    public ServerUI() {
        setTitle("Secure Chat Server");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setBackground(BG_COLOR);
        cards.setBorder(new EmptyBorder(20, 0, 20, 0));

        cards.add(createCard("SERVER STATUS", "OFFLINE", "●", RED));
        cards.add(createCard("PORT", "8888", "⚡", BLUE));
        cards.add(createCard("CLIENTS", "0", "👥", GREEN));
        cards.add(createCard("ENCRYPTION", "RSA 2048", "🔐", new Color(168, 85, 247)));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_COLOR);
        center.add(cards, BorderLayout.NORTH);

        JPanel logPanel = createLogPanel();
        center.add(logPanel, BorderLayout.CENTER);
        mainPanel.add(center, BorderLayout.CENTER);

        JPanel footer = createFooter();
        mainPanel.add(footer, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("🔐 Secure Chat Server");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Real-time encrypted communication server");
        subtitle.setForeground(GRAY);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(BG_COLOR);
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(subtitle);
        panel.add(text, BorderLayout.WEST);

        return panel;
    }

    private JPanel createCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        iconLabel.setForeground(color);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(GRAY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        if (title.equals("CLIENTS")) clientCountLabel = valueLabel;
        if (title.equals("PORT")) portLabel = valueLabel;
        if (title.equals("ENCRYPTION")) rsaLabel = valueLabel;
        if (title.equals("SERVER STATUS")) statusLabel = valueLabel;

        return card;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("  Server Activity Log");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(203, 213, 225));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(null);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(BG_COLOR);

        clearButton = createButton("Clear Log", CARD_COLOR);
        exitButton = createButton("Exit", RED);

        clearButton.addActionListener(e -> logArea.setText(""));
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(clearButton);
        panel.add(exitButton);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 38));
        return button;
    }

    public static void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void setServerStatus(boolean online) {
        SwingUtilities.invokeLater(() -> {
            if (online) {
                statusLabel.setText("ONLINE");
                statusLabel.setForeground(GREEN);
            } else {
                statusLabel.setText("OFFLINE");
                statusLabel.setForeground(RED);
            }
        });
    }

    public static void updateClientCount(int count) {
        SwingUtilities.invokeLater(() -> {
            if (clientCountLabel != null) {
                clientCountLabel.setText(String.valueOf(count));
            }
        });
    }
}