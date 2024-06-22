import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Robot;
import java.awt.AWTException;
import java.util.HashSet;
import java.util.Set;

public class ClickGen extends JFrame {
    private Robot robot;
    private Timer timer;
    private boolean isAutoClicking = false;
    private int clickInterval = 1000; // Default interval
    private Set<Integer> hotkeySet = new HashSet<>(); // Store hotkey combination
    private Set<Integer> pressedKeys = new HashSet<>(); // Store currently pressed keys
    private Window selectedWindow = null; // Selected window

    private JComboBox<Window> windowComboBox;

    public ClickGen() {
        setTitle("ClickGen v1.0");
        setSize(480, 270);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel intervalLabel = new JLabel("Click Interval (ms):");
        intervalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(intervalLabel, gbc);

        JTextField intervalField = new JTextField(String.valueOf(clickInterval), 10);
        intervalField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(intervalField, gbc);

        JButton setIntervalButton = new JButton("Set Interval");
        setIntervalButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(setIntervalButton, gbc);

        JLabel hotkeyLabel = new JLabel("Hotkey:");
        hotkeyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(hotkeyLabel, gbc);

        JTextField hotkeyField = new JTextField("ctrl shift A", 10); // Default hotkey
        hotkeyField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(hotkeyField, gbc);

        JButton setHotkeyButton = new JButton("Set Hotkey");
        setHotkeyButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 2;
        gbc.gridy = 1;
        add(setHotkeyButton, gbc);

        JLabel selectWindowLabel = new JLabel("Select Window:");
        selectWindowLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(selectWindowLabel, gbc);

        windowComboBox = new JComboBox<>();
        updateWindowComboBox();
        windowComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(windowComboBox, gbc);

        JButton refreshWindowsButton = new JButton("Refresh Windows");
        refreshWindowsButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(refreshWindowsButton, gbc);

        JButton toggleButton = new JButton("Start/Stop");
        toggleButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(toggleButton, gbc);

        setIntervalButton.addActionListener(e -> {
            try {
                clickInterval = Integer.parseInt(intervalField.getText());
                JOptionPane.showMessageDialog(this, "Click interval set to " + clickInterval + " ms");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        });

        setHotkeyButton.addActionListener(e -> {
            String keyText = hotkeyField.getText().trim();
            hotkeySet.clear();
            for (String key : keyText.split(" ")) {
                try {
                    int keyCode = getKeyCode(key);
                    if (keyCode != -1) {
                        hotkeySet.add(keyCode);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid hotkey: " + key);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid hotkey: " + key);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Hotkey set to " + keyText);
        });

        refreshWindowsButton.addActionListener(e -> updateWindowComboBox());

        toggleButton.addActionListener(e -> toggleAutoClicking());

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize Robot.");
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    pressedKeys.add(keyCode);
                    if (hotkeySet.equals(pressedKeys)) {
                        toggleAutoClicking();
                        return true;
                    }
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    pressedKeys.remove(keyCode);
                }
                return false;
            }
        });

        setLocationRelativeTo(null); // Center the window on the screen
    }

    private int getKeyCode(String key) {
        switch (key.toLowerCase()) {
            case "ctrl":
                return KeyEvent.VK_CONTROL;
            case "shift":
                return KeyEvent.VK_SHIFT;
            case "alt":
                return KeyEvent.VK_ALT;
            case "meta":
                return KeyEvent.VK_META;
            default:
                return KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));
        }
    }

    private void updateWindowComboBox() {
        Window[] windows = Window.getWindows();
        windowComboBox.removeAllItems();
        for (Window window : windows) {
            windowComboBox.addItem(window);
        }
    }

    private void toggleAutoClicking() {
        if (isAutoClicking) {
            stopAutoClicking();
        } else {
            startAutoClicking();
        }
    }

    private void startAutoClicking() {
        selectedWindow = (Window) windowComboBox.getSelectedItem();
        if (selectedWindow == null) {
            JOptionPane.showMessageDialog(this, "No window selected. Please select a window first.");
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (selectedWindow.isActive()) {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
            }
        }, 0, clickInterval);
        isAutoClicking = true;
        JOptionPane.showMessageDialog(this, "Auto clicking started.");
    }

    private void stopAutoClicking() {
        if (timer != null) {
            timer.cancel();
        }
        isAutoClicking = false;
        JOptionPane.showMessageDialog(this, "Auto clicking stopped.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClickGen clickGen = new ClickGen();
            clickGen.setVisible(true);
        });
    }
}
