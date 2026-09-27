import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/**
 * AutoDonut Installer
 * A tiny double-clickable desktop app that drops the AutoDonut Fabric mod
 * (+ Fabric API) into a Minecraft client or server's mods folder, and writes
 * out config/autodonut.json with whatever AI settings you enter here.
 *
 * Run with: java -jar AutoDonut-Installer.jar
 */
public class AutoDonutInstaller extends JFrame {

    private final JRadioButton clientRadio = new JRadioButton("Minecraft client (singleplayer / join a server)");
    private final JRadioButton serverRadio = new JRadioButton("A server folder I run myself");
    private final JTextField pathField = new JTextField();
    private final JTextField apiKeyField = new JTextField();
    private final JTextField baseUrlField = new JTextField("https://api.openai.com/v1/chat/completions");
    private final JTextField modelField = new JTextField("gpt-4o-mini");
    private final JTextArea statusArea = new JTextArea(8, 50);

    public AutoDonutInstaller() {
        super("AutoDonut Installer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("AutoDonut \u2014 automation blocks + AI advisor for your own server");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        main.add(title);
        main.add(Box.createVerticalStrut(4));
        JLabel subtitle = new JLabel("For a self-hosted / your-own Minecraft server. Not affiliated with DonutSMP.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.ITALIC, 11f));
        main.add(subtitle);
        main.add(Box.createVerticalStrut(12));

        main.add(section("1. Where should this install?"));
        ButtonGroup group = new ButtonGroup();
        group.add(clientRadio);
        group.add(serverRadio);
        clientRadio.setSelected(true);
        clientRadio.addActionListener(e -> pathField.setText(defaultClientPath()));
        serverRadio.addActionListener(e -> pathField.setText(""));
        main.add(clientRadio);
        main.add(serverRadio);
        main.add(Box.createVerticalStrut(6));

        JPanel pathPanel = new JPanel(new BorderLayout(6, 0));
        pathField.setText(defaultClientPath());
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> browseForFolder());
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browse, BorderLayout.EAST);
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        main.add(pathPanel);
        main.add(Box.createVerticalStrut(14));

        main.add(section("2. AI advisor (optional) \u2014 used by /autodonut ai ask in-game"));
        main.add(labeled("API key (OpenAI-compatible):", apiKeyField));
        main.add(labeled("API endpoint:", baseUrlField));
        main.add(labeled("Model:", modelField));
        JLabel note = new JLabel("<html>Leave the key blank if you don't want AI features yet \u2014 you can set it later in-game with<br>" +
                "<b>/autodonut ai setkey &lt;key&gt;</b>, or by editing config/autodonut.json directly.</html>");
        note.setFont(note.getFont().deriveFont(11f));
        main.add(note);
        main.add(Box.createVerticalStrut(14));

        JButton installBtn = new JButton("Install AutoDonut");
        installBtn.setFont(installBtn.getFont().deriveFont(Font.BOLD, 13f));
        installBtn.addActionListener(e -> install());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(installBtn);

        JButton fabricBtn = new JButton("Get Fabric Loader...");
        fabricBtn.addActionListener(e -> openUrl("https://fabricmc.net/use/"));
        btnPanel.add(fabricBtn);
        main.add(btnPanel);

        main.add(Box.createVerticalStrut(10));
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log("Ready. Fill in the folder above (your .minecraft folder, or your server's root folder), then click Install.");
        JScrollPane scroll = new JScrollPane(statusArea);
        main.add(scroll);

        add(main, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setBorder(new EmptyBorder(6, 0, 4, 0));
        return l;
    }

    private JPanel labeled(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(190, 20));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return p;
    }

    private String defaultClientPath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", "");
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return (appdata != null ? appdata : home + "\\AppData\\Roaming") + "\\.minecraft";
        } else if (os.contains("mac")) {
            return home + "/Library/Application Support/minecraft";
        } else {
            return home + "/.minecraft";
        }
    }

    private void browseForFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File current = new File(pathField.getText());
        if (current.exists()) chooser.setCurrentDirectory(current);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            pathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            log("Couldn't open browser automatically. Visit: " + url);
        }
    }

    private void log(String msg) {
        statusArea.append(msg + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void install() {
        String targetPath = pathField.getText().trim();
        if (targetPath.isEmpty()) {
            log("Please choose a folder first.");
            return;
        }
        try {
            Path root = Paths.get(targetPath);
            Files.createDirectories(root);
            Path modsDir = root.resolve("mods");
            Path configDir = root.resolve("config");
            Files.createDirectories(modsDir);
            Files.createDirectories(configDir);

            copyBundled("/bundled/autodonut-1.0.0.jar", modsDir.resolve("autodonut-1.0.0.jar"));
            copyBundled("/bundled/fabric-api-0.161.0+26.2.jar", modsDir.resolve("fabric-api-0.161.0+26.2.jar"));

            String apiKey = apiKeyField.getText().trim().replace("\"", "\\\"");
            String baseUrl = baseUrlField.getText().trim().replace("\"", "\\\"");
            String model = modelField.getText().trim().replace("\"", "\\\"");

            String json = "{\n" +
                    "  \"automateEnabled\": true,\n" +
                    "  \"autoSmelterEnabled\": true,\n" +
                    "  \"autoFarmEnabled\": true,\n" +
                    "  \"autoMinerEnabled\": true,\n" +
                    "  \"aiApiKey\": \"" + apiKey + "\",\n" +
                    "  \"aiBaseUrl\": \"" + (baseUrl.isEmpty() ? "https://api.openai.com/v1/chat/completions" : baseUrl) + "\",\n" +
                    "  \"aiModel\": \"" + (model.isEmpty() ? "gpt-4o-mini" : model) + "\",\n" +
                    "  \"aiAutoControllerEnabled\": false,\n" +
                    "  \"aiAutoIntervalSeconds\": 300\n" +
                    "}\n";
            Files.write(configDir.resolve("autodonut.json"), json.getBytes(StandardCharsets.UTF_8));

            log("");
            log("Installed to: " + modsDir);
            log("Config written to: " + configDir.resolve("autodonut.json"));
            log("");
            log("Next steps:");
            log("1) Make sure this folder is running Fabric Loader for Minecraft 26.2 (click 'Get Fabric Loader' above if needed).");
            if (clientRadio.isSelected()) {
                log("2) Launch Minecraft using the Fabric profile.");
            } else {
                log("2) Start your server using its fabric-server-launcher.jar as usual.");
            }
            log("3) In-game: place the Auto Smelter / Auto Farm / Auto Miner blocks (use '/autodonut kit' to get one of each).");
            log("4) Check settings anytime with '/autodonut status', or set your AI key later with '/autodonut ai setkey <key>'.");
            JOptionPane.showMessageDialog(this, "AutoDonut installed successfully!", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            log("ERROR: " + e.getMessage());
        }
    }

    private void copyBundled(String resourcePath, Path destination) throws IOException {
        try (InputStream in = AutoDonutInstaller.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Missing bundled resource: " + resourcePath);
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new AutoDonutInstaller().setVisible(true));
    }
}
