package com.roulette.view;

import com.roulette.factory.BetFactory;
import com.roulette.model.bet.Bet;
import com.roulette.model.entity.GameSession;
import com.roulette.model.perk.PerkType;
import com.roulette.model.roulette.PocketColor;
import com.roulette.model.roulette.RoulettePocket;
import com.roulette.service.GameSessionService;
import com.roulette.dto.SpinResultDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * Graphical User Interface (Swing) for the Roulette Roguelike Game (RQ07, RQ10).
 * Uses pixel-art table and wheel images with interactive clickable betting areas.
 */
public class GameGuiView extends JFrame {
    private final GameSessionService gameSessionService;
    private final BetFactory betFactory;
    private final String username;
    private GameSession currentSession;

    // GUI Components
    private JLabel balanceLabel;
    private JLabel quotaLabel;
    private JLabel spinsLabel;
    private JLabel roundLabel;
    private JLabel minBetLabel;
    private JLabel resultLabel;
    private JProgressBar quotaProgressBar;

    private JTextArea betListArea;
    private final List<Bet> currentBets = new ArrayList<>();
    private long selectedChip = 10;
    private JLabel selectedChipLabel;

    // Perk buttons
    private JButton btnClairvoyance;
    private JButton btnLossMitigation;

    // Table image
    private BufferedImage tableImage;
    private BufferedImage wheelImage;

    // Red numbers on American roulette
    private static final Set<Integer> RED_NUMBERS = new HashSet<>(Arrays.asList(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    ));

    public GameGuiView(GameSessionService gameSessionService, String username) {
        this.gameSessionService = gameSessionService;
        this.username = (username != null && !username.isBlank()) ? username : "Player1";
        this.betFactory = new BetFactory();
        this.currentSession = gameSessionService.getActiveSession(this.username)
                .orElseGet(() -> gameSessionService.createSession(this.username));

        // Load images
        loadImages();

        setTitle("Ruleta Roguelike – Cuotas Progresivas (Java Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 820);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(24, 20, 16));

        initComponents();
        updateDisplay();
    }

    private void loadImages() {
        try {
            InputStream tableStream = getClass().getClassLoader().getResourceAsStream("roulette_table.png");
            if (tableStream != null) {
                tableImage = ImageIO.read(tableStream);
            }
        } catch (Exception e) {
            System.err.println("Could not load table image: " + e.getMessage());
        }
        try {
            // Try loading from classpath or working directory
            java.io.File wheelFile = new java.io.File("wheel_rotor.png");
            if (wheelFile.exists()) {
                wheelImage = ImageIO.read(wheelFile);
            }
            java.io.File tableFile = new java.io.File("roulette_table.png");
            if (tableImage == null && tableFile.exists()) {
                tableImage = ImageIO.read(tableFile);
            }
        } catch (Exception e) {
            System.err.println("Could not load images from file: " + e.getMessage());
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // Top Header & HUD
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Center: Table image + Betting Board
        add(createCenterPanel(), BorderLayout.CENTER);

        // Bottom: Controls, Chips & Perks
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(36, 20, 11));
        header.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel title = new JLabel("\uD83C\uDFB0 RULETA CASINO: RETO DE CUOTAS PROGRESIVAS");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(255, 215, 0));
        header.add(title, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);

        JButton btnRules = createStyledButton("\uD83D\uDCD6 Reglas & Pagos", new Color(55, 71, 79));
        btnRules.addActionListener(e -> showRulesDialog());
        nav.add(btnRules);

        JButton btnSave = createStyledButton("\uD83D\uDCBE Guardar", new Color(46, 125, 50));
        btnSave.addActionListener(e -> {
            gameSessionService.saveSession(currentSession);
            JOptionPane.showMessageDialog(this, "¡Partida guardada exitosamente!", "Guardado", JOptionPane.INFORMATION_MESSAGE);
        });
        nav.add(btnSave);

        header.add(nav, BorderLayout.EAST);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 6));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(5, 10, 5, 10));

        // HUD panel
        JPanel hudPanel = new JPanel(new GridLayout(1, 5, 6, 0));
        hudPanel.setOpaque(false);

        balanceLabel = createHudBox(hudPanel, "SALDO", "$100", new Color(105, 240, 174));
        quotaLabel = createHudBox(hudPanel, "CUOTA OBJETIVO", "$1,000", new Color(255, 215, 0));
        spinsLabel = createHudBox(hudPanel, "TIRADAS", "6 / 6", Color.WHITE);
        roundLabel = createHudBox(hudPanel, "RONDA", "Nivel 1", Color.WHITE);
        minBetLabel = createHudBox(hudPanel, "APUESTA MÍNIMA", "$10", new Color(255, 138, 101));

        center.add(hudPanel, BorderLayout.NORTH);

        // Middle content: table image + betting grid
        JPanel middleStack = new JPanel(new BorderLayout(0, 6));
        middleStack.setOpaque(false);

        // Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout(5, 2));
        progressPanel.setOpaque(false);
        JLabel progLbl = new JLabel("Progreso hacia la Cuota:");
        progLbl.setForeground(new Color(200, 230, 201));
        progLbl.setFont(new Font("Monospaced", Font.BOLD, 10));
        progressPanel.add(progLbl, BorderLayout.NORTH);

        quotaProgressBar = new JProgressBar(0, 100);
        quotaProgressBar.setStringPainted(true);
        quotaProgressBar.setForeground(new Color(255, 193, 7));
        quotaProgressBar.setBackground(new Color(18, 30, 18));
        quotaProgressBar.setPreferredSize(new Dimension(0, 18));
        progressPanel.add(quotaProgressBar, BorderLayout.CENTER);
        middleStack.add(progressPanel, BorderLayout.NORTH);

        // Table container with image and interactive board
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBackground(new Color(25, 65, 30));
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 50, 20), 4),
                new EmptyBorder(8, 8, 8, 8)));

        resultLabel = new JLabel("¡Haz tus apuestas y gira la ruleta!", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        resultLabel.setForeground(Color.WHITE);
        resultLabel.setBorder(new EmptyBorder(6, 0, 6, 0));
        tableContainer.add(resultLabel, BorderLayout.NORTH);

        // Main content: wheel image on left, betting grid on right
        JPanel gameArea = new JPanel(new BorderLayout(10, 0));
        gameArea.setOpaque(false);

        // Wheel image panel
        if (tableImage != null) {
            JLabel tableLabel = new JLabel(new ImageIcon(tableImage.getScaledInstance(280, 280, Image.SCALE_SMOOTH)));
            tableLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel wheelPanel = new JPanel(new BorderLayout());
            wheelPanel.setOpaque(false);
            wheelPanel.add(tableLabel, BorderLayout.CENTER);
            wheelPanel.setPreferredSize(new Dimension(290, 280));
            gameArea.add(wheelPanel, BorderLayout.WEST);
        }

        // Interactive betting grid
        JPanel bettingGrid = createBettingBoard();
        gameArea.add(bettingGrid, BorderLayout.CENTER);

        tableContainer.add(gameArea, BorderLayout.CENTER);

        // Bets summary list
        betListArea = new JTextArea(3, 30);
        betListArea.setEditable(false);
        betListArea.setBackground(new Color(15, 25, 15));
        betListArea.setForeground(new Color(230, 230, 230));
        betListArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollBets = new JScrollPane(betListArea);
        scrollBets.setPreferredSize(new Dimension(0, 65));
        scrollBets.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Apuestas del Turno",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Monospaced", Font.BOLD, 10), Color.LIGHT_GRAY));
        tableContainer.add(scrollBets, BorderLayout.SOUTH);

        middleStack.add(tableContainer, BorderLayout.CENTER);
        center.add(middleStack, BorderLayout.CENTER);
        return center;
    }

    private JPanel createBettingBoard() {
        JPanel board = new JPanel();
        board.setLayout(new BoxLayout(board, BoxLayout.Y_AXIS));
        board.setBackground(new Color(20, 55, 25));
        board.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Row: 0 and 00 (green)
        JPanel zeroRow = new JPanel(new GridLayout(1, 2, 3, 0));
        zeroRow.setOpaque(false);
        zeroRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addBetButton(zeroRow, "0", () -> placeBet(betFactory.createNumberBet("0", selectedChip)), new Color(0, 130, 50));
        addBetButton(zeroRow, "00", () -> placeBet(betFactory.createNumberBet("00", selectedChip)), new Color(0, 130, 50));
        board.add(zeroRow);
        board.add(Box.createVerticalStrut(3));

        // Numbers 1-36 in 3 rows of 12
        int[][] rows = {
                {3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36},
                {2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35},
                {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34}
        };

        for (int[] row : rows) {
            JPanel numRow = new JPanel(new GridLayout(1, 12, 2, 0));
            numRow.setOpaque(false);
            numRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            for (int num : row) {
                Color bg = RED_NUMBERS.contains(num) ? new Color(180, 30, 30) : new Color(30, 30, 30);
                String label = String.valueOf(num);
                addBetButton(numRow, label, () -> placeBet(betFactory.createNumberBet(label, selectedChip)), bg);
            }
            board.add(numRow);
            board.add(Box.createVerticalStrut(2));
        }

        // Dozens row
        JPanel dozenRow = new JPanel(new GridLayout(1, 3, 3, 0));
        dozenRow.setOpaque(false);
        dozenRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addBetButton(dozenRow, "1ST 12", () -> placeBet(betFactory.createDozenBet(com.roulette.model.bet.DozenBet.Dozen.FIRST, selectedChip)), new Color(38, 80, 45));
        addBetButton(dozenRow, "2ND 12", () -> placeBet(betFactory.createDozenBet(com.roulette.model.bet.DozenBet.Dozen.SECOND, selectedChip)), new Color(38, 80, 45));
        addBetButton(dozenRow, "3RD 12", () -> placeBet(betFactory.createDozenBet(com.roulette.model.bet.DozenBet.Dozen.THIRD, selectedChip)), new Color(38, 80, 45));
        board.add(dozenRow);
        board.add(Box.createVerticalStrut(2));

        // Bottom row: color and parity
        JPanel bottomRow = new JPanel(new GridLayout(1, 4, 3, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addBetButton(bottomRow, "\u2B24 ROJO", () -> placeBet(betFactory.createColorBet(PocketColor.RED, selectedChip)), new Color(180, 30, 30));
        addBetButton(bottomRow, "\u2B24 NEGRO", () -> placeBet(betFactory.createColorBet(PocketColor.BLACK, selectedChip)), new Color(30, 30, 30));
        addBetButton(bottomRow, "PAR", () -> placeBet(betFactory.createParityBet(com.roulette.model.bet.ParityBet.Parity.EVEN, selectedChip)), new Color(38, 80, 45));
        addBetButton(bottomRow, "IMPAR", () -> placeBet(betFactory.createParityBet(com.roulette.model.bet.ParityBet.Parity.ODD, selectedChip)), new Color(38, 80, 45));
        board.add(bottomRow);

        return board;
    }

    /**
     * Creates a JButton that correctly renders custom background colors on all platforms,
     * including Windows with the System Look and Feel.
     */
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    private void addBetButton(JPanel panel, String text, Runnable action, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 10));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 60, 30), 1));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color original = bg;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(original.brighter());
                btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
                btn.setBorder(BorderFactory.createLineBorder(new Color(80, 60, 30), 1));
            }
        });

        btn.addActionListener(e -> action.run());
        panel.add(btn);
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(6, 4));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(4, 10, 10, 10));

        JPanel stack = new JPanel(new GridLayout(3, 1, 0, 3));
        stack.setOpaque(false);

        // Chip Selector
        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3));
        chipPanel.setBackground(new Color(30, 20, 12));
        chipPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 100, 50)), "Seleccionar Ficha",
                TitledBorder.CENTER, TitledBorder.TOP, new Font("Monospaced", Font.BOLD, 10), Color.WHITE));

        long[] chips = {10, 25, 50, 100, 500, 1000};
        Color[] chipColors = {
                new Color(25, 118, 210), new Color(25, 118, 210), new Color(25, 118, 210),
                new Color(80, 80, 80), new Color(123, 31, 162), new Color(123, 31, 162)
        };
        for (int i = 0; i < chips.length; i++) {
            long val = chips[i];
            JButton chipBtn = createStyledButton("$" + val, chipColors[i]);
            chipBtn.setPreferredSize(new Dimension(70, 28));
            chipBtn.addActionListener(e -> {
                selectedChip = val;
                selectedChipLabel.setText("Ficha: $" + val);
            });
            chipPanel.add(chipBtn);
        }

        selectedChipLabel = new JLabel("Ficha: $10");
        selectedChipLabel.setForeground(new Color(255, 215, 0));
        selectedChipLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
        chipPanel.add(selectedChipLabel);

        stack.add(chipPanel);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        actionsPanel.setOpaque(false);

        JButton btnSpin = createStyledButton("\uD83C\uDFB2 GIRAR RULETA", new Color(46, 125, 50));
        btnSpin.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnSpin.setPreferredSize(new Dimension(220, 35));
        btnSpin.addActionListener(e -> spinRoulette());
        actionsPanel.add(btnSpin);

        JButton btnClear = createStyledButton("\uD83E\uDDF9 Limpiar Apuestas", new Color(183, 28, 28));
        btnClear.setPreferredSize(new Dimension(180, 35));
        btnClear.addActionListener(e -> {
            currentBets.clear();
            updateBetsArea();
        });
        actionsPanel.add(btnClear);

        stack.add(actionsPanel);

        // Perks Tray
        JPanel perksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        perksPanel.setBackground(new Color(25, 18, 32));
        perksPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 50, 180)), "Ventajas Estratégicas (RQ05)",
                TitledBorder.CENTER, TitledBorder.TOP, new Font("Monospaced", Font.BOLD, 10), new Color(220, 180, 255)));

        btnClairvoyance = createStyledButton("\uD83D\uDD2E Clairvoyance (0)", new Color(74, 20, 140));
        btnClairvoyance.setPreferredSize(new Dimension(200, 30));
        btnClairvoyance.addActionListener(e -> {
            String revealed = gameSessionService.revealNextNumber(currentSession);
            if (revealed != null) {
                JOptionPane.showMessageDialog(this, "\uD83D\uDD2E VISIÓN: ¡El próximo número será " + revealed + "!", "Clairvoyance", JOptionPane.INFORMATION_MESSAGE);
                resultLabel.setText("\uD83D\uDD2E Clairvoyance reveló: " + revealed);
            } else {
                JOptionPane.showMessageDialog(this, "No tienes cargas de Clairvoyance.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            updateDisplay();
        });
        perksPanel.add(btnClairvoyance);

        btnLossMitigation = createStyledButton("\uD83D\uDEE1\uFE0F Escudo -50% (0)", new Color(0, 131, 143));
        btnLossMitigation.setPreferredSize(new Dimension(200, 30));
        btnLossMitigation.addActionListener(e -> {
            var perk = currentSession.getPerk(PerkType.LOSS_MITIGATION);
            if (perk != null && perk.getQuantity() > 0) {
                perk.setActive(!perk.isActive());
                JOptionPane.showMessageDialog(this, "Escudo " + (perk.isActive() ? "ACTIVADO" : "DESACTIVADO"), "Escudo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No tienes cargas de Loss Mitigation.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            updateDisplay();
        });
        perksPanel.add(btnLossMitigation);

        stack.add(perksPanel);
        bottom.add(stack, BorderLayout.CENTER);
        return bottom;
    }

    private JLabel createHudBox(JPanel parent, String title, String val, Color valColor) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(25, 36, 26));
        box.setBorder(BorderFactory.createLineBorder(new Color(60, 95, 65), 2));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Monospaced", Font.PLAIN, 9));
        titleLbl.setForeground(new Color(160, 200, 165));
        box.add(titleLbl, BorderLayout.NORTH);

        JLabel valLbl = new JLabel(val, SwingConstants.CENTER);
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        valLbl.setForeground(valColor);
        box.add(valLbl, BorderLayout.CENTER);

        parent.add(box);
        return valLbl;
    }

    private void placeBet(Bet bet) {
        long currentTotal = currentBets.stream().mapToLong(Bet::getAmount).sum();
        if (currentTotal + bet.getAmount() > currentSession.getCurrentBalance()) {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para esta apuesta.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentBets.add(bet);
        updateBetsArea();
    }

    private void updateBetsArea() {
        StringBuilder sb = new StringBuilder();
        long total = 0;
        for (Bet b : currentBets) {
            sb.append("• ").append(b.getTargetDescription())
              .append(" - $").append(b.getAmount())
              .append(" (Paga ").append(b.getPayoutMultiplier()).append(":1)\n");
            total += b.getAmount();
        }
        sb.append("Total en mesa: $").append(total);
        betListArea.setText(sb.toString());
    }

    private void spinRoulette() {
        if (currentBets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor coloca al menos una apuesta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SpinResultDTO result = gameSessionService.processSpin(currentSession, currentBets);

        if (result.isGameOver()) {
            resultLabel.setText(result.getMessage());
            updateDisplay();
            JOptionPane.showMessageDialog(this, result.getMessage(), "GAME OVER", JOptionPane.ERROR_MESSAGE);
            int opt = JOptionPane.showConfirmDialog(this, "¿Deseas reiniciar una nueva partida?", "Reiniciar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                currentSession = gameSessionService.createSession(username);
            }
            currentBets.clear();
            updateBetsArea();
            updateDisplay();
            return;
        }

        if (result.getWinningPocket() == null) {
            String errorMsg = result.getMessage() != null ? result.getMessage() : "No se pudo procesar la tirada.";
            JOptionPane.showMessageDialog(this, errorMsg, "Aviso", JOptionPane.WARNING_MESSAGE);
            resultLabel.setText("⚠️ " + errorMsg);
            return;
        }

        String colorEmoji = "RED".equals(result.getPocketColor()) ? "\uD83D\uDD34" : "BLACK".equals(result.getPocketColor()) ? "\u26AB" : "\uD83D\uDFE2";
        resultLabel.setText(colorEmoji + " Resultado: " + result.getWinningPocket() + " (" + result.getPocketColor() + ") | Ganancia: +$" + result.getTotalPayout());
        currentBets.clear();
        updateBetsArea();
        updateDisplay();

        if (result.isQuotaReached()) {
            showPerkChoiceDialog();
        }
    }

    private void showPerkChoiceDialog() {
        String[] options = {
                "\uD83C\uDF1F Money Boost (+50% balance base)",
                "\uD83D\uDD2E Clairvoyance (Revela número)",
                "\uD83D\uDEE1\uFE0F Loss Mitigation (-50% pérdidas)"
        };

        int choice = JOptionPane.showOptionDialog(
                this,
                "¡CUOTA ALCANZADA!\nHas superado la meta exigida.\nSelecciona 1 ventaja estratégica:",
                "Selección de Ventaja Estratégica (RQ05)",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.CLOSED_OPTION) {
            choice = 0;
        }

        PerkType selected;
        switch (choice) {
            case 0: selected = PerkType.MONEY_BOOST; break;
            case 1: selected = PerkType.CLAIRVOYANCE; break;
            case 2: selected = PerkType.LOSS_MITIGATION; break;
            default: selected = PerkType.MONEY_BOOST; break;
        }

        gameSessionService.grantPerkReward(currentSession, selected);
        JOptionPane.showMessageDialog(this, "¡Ventaja elegida: " + selected.getDisplayName() + "!\nAvanzas al Nivel " + currentSession.getCurrentRound() + " con nueva cuota: $" + currentSession.getCurrentQuota(), "Nivel Superado", JOptionPane.INFORMATION_MESSAGE);
        updateDisplay();
    }

    private void showRulesDialog() {
        String rules = "=========================================================\n" +
                       "         TABLA DE MULTIPLICADORES Y PAGOS (RQ07)         \n" +
                       "=========================================================\n\n" +
                       "1. APUESTA A PLENO (Número específico 0, 00, 1 a 36):\n" +
                       "   • Multiplicador: 35 a 1 (Devuelve 36x lo apostado)\n\n" +
                       "2. DOCENAS (1st 12, 2nd 12, 3rd 12):\n" +
                       "   • Multiplicador: 2 a 1 (Devuelve 3x lo apostado)\n\n" +
                       "3. ROJO / NEGRO:\n" +
                       "   • Multiplicador: 1 a 1 (Devuelve 2x lo apostado)\n\n" +
                       "4. PAR / IMPAR (Even / Odd):\n" +
                       "   • Multiplicador: 1 a 1 (Devuelve 2x lo apostado)\n" +
                       "   • (Nota: 0 y 00 no cuentan como par ni impar)\n\n" +
                       "=========================================================\n" +
                       "                REGLAS DE CUOTA Y ROGUELIKE              \n" +
                       "=========================================================\n" +
                       "• Inicias con $100 y una cuota objetivo de $1,000 en 6 tiradas.\n" +
                       "• Debes cubrir la apuesta mínima obligatoria por ronda.\n" +
                       "• Si superas la cuota por un margen amplio, la dificultad\n" +
                       "  de la siguiente ronda escala proporcionalmente (RQ04).\n" +
                       "• Al superar la cuota, eliges 1 de 3 ventajas estratégicas (RQ05).\n";

        JTextArea area = new JTextArea(rules);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(new Color(20, 20, 20));
        area.setForeground(new Color(230, 230, 230));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(550, 420));
        JOptionPane.showMessageDialog(this, scroll, "Panel Informativo de Pagos (RQ07)", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateDisplay() {
        balanceLabel.setText("$" + String.format("%,d", currentSession.getCurrentBalance()));
        quotaLabel.setText("$" + String.format("%,d", currentSession.getCurrentQuota()));
        spinsLabel.setText(currentSession.getSpinsLeft() + " / 6");
        roundLabel.setText("Nivel " + currentSession.getCurrentRound());

        long minBet = Math.max(10, currentSession.getCurrentRound() * 10);
        minBetLabel.setText("$" + minBet);

        int progress = (int) Math.min(100, (currentSession.getCurrentBalance() * 100) / currentSession.getCurrentQuota());
        quotaProgressBar.setValue(progress);
        quotaProgressBar.setString(progress + "% hacia $" + String.format("%,d", currentSession.getCurrentQuota()));

        var clairPerk = currentSession.getPerk(PerkType.CLAIRVOYANCE);
        btnClairvoyance.setText("\uD83D\uDD2E Clairvoyance (" + (clairPerk != null ? clairPerk.getQuantity() : 0) + ")");

        var lossPerk = currentSession.getPerk(PerkType.LOSS_MITIGATION);
        btnLossMitigation.setText("\uD83D\uDEE1\uFE0F Escudo -50% (" + (lossPerk != null ? lossPerk.getQuantity() : 0) + ")" + (lossPerk != null && lossPerk.isActive() ? " [ACTIVO]" : ""));
    }
}
