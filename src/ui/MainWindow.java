package ui;

import game.AutoGame;
import game.Game;
import game.GameFactory;
import game.ManualGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private static final int AUTOPLAY_DELAY_MS = 400;

    // Child components
    private final BoardPanel      boardPanel;
    private final GameStatusPanel statusPanel;

    // Sidebar controls
    private final JRadioButton rbEnglish;
    private final JRadioButton rbHexagon;
    private final JRadioButton rbDiamond;
    private final JSpinner     sizeSpinner;
    private final JButton      btnNewGame;
    private final JButton      btnAutoplay;
    private final JButton      btnRandomize;

    // State
    private Game  currentGame;
    private Timer autoplayTimer;

    private static final Color SIDEBAR_BG = new Color(235, 210, 160);

    public MainWindow() {
        super("Peg Solitaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        boardPanel  = new BoardPanel();
        statusPanel = new GameStatusPanel();

        boardPanel.setOnMoveApplied(() -> {
            statusPanel.refresh(currentGame);
            if (currentGame != null && currentGame.isOver())
                showGameOverDialog();
        });

        // --- Radio buttons ---
        rbEnglish = new JRadioButton("English", true);
        rbHexagon = new JRadioButton("Hexagon");
        rbDiamond = new JRadioButton("Diamond");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEnglish); bg.add(rbHexagon); bg.add(rbDiamond);

        // --- Odd-only spinner ---
        // SpinnerNumberModel(value, min, max, step)
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 5, 25, 2));
        sizeSpinner.setMaximumSize(new Dimension(60, 28));
        sizeSpinner.setPreferredSize(new Dimension(60, 28));

        // When board type changes, reset spinner to that type's default & start game
        rbEnglish.addActionListener(e -> onBoardTypeChanged());
        rbHexagon.addActionListener(e -> onBoardTypeChanged());
        rbDiamond.addActionListener(e -> onBoardTypeChanged());

        // Spinner change does NOT auto-start — player must press New Game
        // (prevents starting mid-edit while they're still typing a number)

        // --- Buttons ---
        btnNewGame   = new JButton("New Game");
        btnAutoplay  = new JButton("Autoplay");
        btnRandomize = new JButton("Randomize");

        styleButton(btnAutoplay,  new Color(100, 180, 80),  new Color(20, 60, 20));
        styleButton(btnRandomize, new Color(80,  140, 200), new Color(10, 30, 80));

        btnNewGame.addActionListener(e   -> startNewManualGame());
        btnAutoplay.addActionListener(e  -> startAutoplay());
        btnRandomize.addActionListener(e -> randomizeBoard());

        setLayout(new BorderLayout(8, 8));
        add(buildSidebar(), BorderLayout.WEST);
        add(boardPanel,     BorderLayout.CENTER);
        add(statusPanel,    BorderLayout.SOUTH);

        startNewManualGame();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 480));
    }

    // -------------------------------------------------------------------------
    // Game control
    // -------------------------------------------------------------------------

    private void startNewManualGame() {
        stopAutoplay();
        currentGame = GameFactory.create(getSelectedBoardType(), GameFactory.MANUAL, getSelectedSize());
        boardPanel.setGame(currentGame);
        statusPanel.refresh(currentGame);
        btnRandomize.setEnabled(true);
        btnAutoplay.setEnabled(true);
        pack();
    }

    private void startAutoplay() {
        stopAutoplay();
        currentGame = GameFactory.create(getSelectedBoardType(), GameFactory.AUTOPLAY, getSelectedSize());
        boardPanel.setGame(currentGame);
        statusPanel.refresh(currentGame);
        btnRandomize.setEnabled(false);
        btnAutoplay.setEnabled(false);

        autoplayTimer = new Timer(AUTOPLAY_DELAY_MS, e -> {
            if (currentGame.isOver()) {
                stopAutoplay();
                statusPanel.refresh(currentGame);
                showGameOverDialog();
                return;
            }
            ((AutoGame) currentGame).makeMove(null);
            boardPanel.refresh();
            statusPanel.refresh(currentGame);
        });
        autoplayTimer.start();
        pack();
    }

    private void randomizeBoard() {
        if (currentGame instanceof ManualGame) {
            ((ManualGame) currentGame).randomize();
            boardPanel.refresh();
            statusPanel.refresh(currentGame);
        }
    }

    private void stopAutoplay() {
        if (autoplayTimer != null && autoplayTimer.isRunning())
            autoplayTimer.stop();
        btnAutoplay.setEnabled(true);
        btnRandomize.setEnabled(true);
    }

    private void onBoardTypeChanged() {
        // Update spinner min and default for the newly selected board type
        String boardType = getSelectedBoardType();
        int minSize      = GameFactory.getMinSize(boardType);
        int defaultSize  = GameFactory.getDefaultSize(boardType);

        SpinnerNumberModel model = (SpinnerNumberModel) sizeSpinner.getModel();
        // Clamp current value to new min before changing the model bounds
        int current = (int) sizeSpinner.getValue();
        int newVal  = Math.max(current, minSize);
        // Ensure it's odd
        if (newVal % 2 == 0) newVal++;

        model.setMinimum(minSize);
        model.setValue(defaultSize);

        startNewManualGame();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getSelectedBoardType() {
        if (rbHexagon.isSelected()) return GameFactory.HEXAGON;
        if (rbDiamond.isSelected()) return GameFactory.DIAMOND;
        return GameFactory.ENGLISH;
    }

    /**
     * Returns the current spinner value, guaranteed to be odd.
     * If the user somehow enters an even number, rounds up to the next odd.
     */
    private int getSelectedSize() {
        int val = (int) sizeSpinner.getValue();
        if (val % 2 == 0) val++;
        return val;
    }

    private void showGameOverDialog() {
        int pegs = currentGame.getPegCount();
        String msg = String.format("Game Over!\n\nPegs remaining: %d\nRating: %s",
            pegs, currentGame.getRating());
        JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
    }

    // -------------------------------------------------------------------------
    // Sidebar layout
    // -------------------------------------------------------------------------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));
        sidebar.setBackground(SIDEBAR_BG);

        // Board Type group
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setBorder(new TitledBorder("Board Type"));
        typePanel.setBackground(SIDEBAR_BG);
        for (JRadioButton rb : new JRadioButton[]{rbEnglish, rbHexagon, rbDiamond}) {
            rb.setBackground(SIDEBAR_BG);
            typePanel.add(rb);
        }

        // Board size row with spinner
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sizePanel.setBackground(SIDEBAR_BG);
        JLabel sizeLabel = new JLabel("Board size:");
        sizePanel.add(sizeLabel);
        sizePanel.add(sizeSpinner);
        JLabel oddNote = new JLabel("(odd only)");
        oddNote.setFont(oddNote.getFont().deriveFont(10f));
        oddNote.setForeground(Color.DARK_GRAY);
        sizePanel.add(oddNote);

        Dimension spacer = new Dimension(0, 10);

        sidebar.add(typePanel);
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(sizePanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(makeFullWidthButton(btnNewGame));
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(makeFullWidthButton(btnAutoplay));
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(makeFullWidthButton(btnRandomize));
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel makeFullWidthButton(JButton btn) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}