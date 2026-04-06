package ui;

import game.Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Displays live game status below the board:
 *   - Pegs remaining
 *   - Current rating (Outstanding / Very Good / Good / Average)
 *   - A "Game Over" message when no moves remain
 *
 * Call refresh(game) after every move to update the display.
 */
public class GameStatusPanel extends JPanel {

    private final JLabel pegCountLabel;
    private final JLabel ratingLabel;
    private final JLabel gameOverLabel;

    public GameStatusPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 24, 6));
        setBackground(new Color(235, 210, 160));
        setBorder(new EmptyBorder(4, 8, 4, 8));

        pegCountLabel = makeLabel("Pegs: --", new Color(40, 40, 100), 14f);
        ratingLabel   = makeLabel("",         new Color(60, 100, 60), 13f);
        gameOverLabel = makeLabel("",         new Color(180, 30, 30), 15f);
        gameOverLabel.setFont(gameOverLabel.getFont().deriveFont(Font.BOLD, 15f));

        add(pegCountLabel);
        add(ratingLabel);
        add(gameOverLabel);
    }

    /**
     * Updates all labels to reflect the current game state.
     * Safe to call from the Swing EDT at any time.
     */
    public void refresh(Game game) {
        if (game == null) {
            pegCountLabel.setText("Pegs: --");
            ratingLabel.setText("");
            gameOverLabel.setText("");
            return;
        }

        pegCountLabel.setText("Pegs remaining: " + game.getPegCount());
        ratingLabel.setText("Rating: " + game.getRating());

        if (game.isOver()) {
            gameOverLabel.setText("Game Over!  Final rating: " + game.getRating());
            ratingLabel.setText("");
        } else {
            gameOverLabel.setText("");
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private JLabel makeLabel(String text, Color color, float size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(size));
        return label;
    }
}