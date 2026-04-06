package game;

import model.Board;
import model.CellState;
import model.Move;

import java.util.List;
import java.util.Random;

/**
 * Controls a human-driven Solitaire game.
 *
 * The UI calls makeMove(move) with the player's chosen move.
 * This class validates it, applies it, and records it in history.
 *
 * Also supports randomize(), which shuffles the board into a new
 * valid state by replaying a series of random moves forward from
 * the initial position — required by Sprint 3.
 */
public class ManualGame extends Game {

    private static final Random RNG = new Random();

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public ManualGame(Board board) {
        super(board);
    }

    // -------------------------------------------------------------------------
    // Game.makeMove implementation
    // -------------------------------------------------------------------------

    /**
     * Attempts to apply the player's chosen move.
     *
     * @param move The move selected by the player.
     * @return true if the move was valid and applied; false if it was illegal.
     */
    @Override
    public boolean makeMove(Move move) {
        if (move == null || !board.isValidMove(move)) return false;
        board.applyMove(move);
        moveHistory.add(move);
        return true;
    }

    // -------------------------------------------------------------------------
    // Randomize (Sprint 3)
    // -------------------------------------------------------------------------

    /**
     * Randomizes the board by applying a random number of valid moves
     * (between minMoves and maxMoves) from the current state.
     *
     * This gives a fresh mid-game position without hardcoding any state.
     * If no valid moves exist at any point the process stops early.
     *
     * The move history is NOT updated — randomization is not part of
     * the player's recorded move sequence.
     *
     * @param minMoves minimum number of random moves to apply
     * @param maxMoves maximum number of random moves to apply
     */
    public void randomize(int minMoves, int maxMoves) {
        if (minMoves < 0) minMoves = 0;
        if (maxMoves < minMoves) maxMoves = minMoves;

        int steps = minMoves + RNG.nextInt(maxMoves - minMoves + 1);

        for (int i = 0; i < steps; i++) {
            List<Move> valid = board.getValidMoves();
            if (valid.isEmpty()) break;
            Move chosen = valid.get(RNG.nextInt(valid.size()));
            board.applyMove(chosen);
        }
    }

    /**
     * Convenience overload: applies a default range of 5–15 random moves.
     */
    public void randomize() {
        randomize(5, 15);
    }

    // -------------------------------------------------------------------------
    // Game identity
    // -------------------------------------------------------------------------

    @Override
    public String getModeName() { return "Manual"; }
}