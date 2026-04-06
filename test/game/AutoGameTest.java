package game;

import model.CellState;
import model.EnglishBoard;
import model.HexagonBoard;
import model.Move;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AutoGame.
 *
 * Covers:
 *   - makeMove(null) picks and applies a valid move
 *   - makeMove returns false when game is already over
 *   - runToEnd() terminates and leaves board in game-over state
 *   - move history is recorded for every auto move
 *   - works correctly for both English and Hexagon boards
 */
class AutoGameTest {

    private AutoGame game;

    @BeforeEach
    void setUp() {
        game = new AutoGame(new EnglishBoard());
    }

    // -------------------------------------------------------------------------
    // makeMove — basic behaviour
    // -------------------------------------------------------------------------

    @Test
    void makeMove_returnsTrueWhenMovesAvailable() {
        assertTrue(game.makeMove(null));
    }

    @Test
    void makeMove_decrementsPegCount() {
        int before = game.getPegCount();
        game.makeMove(null);
        assertEquals(before - 1, game.getPegCount());
    }

    @Test
    void makeMove_appliesALegalMove() {
        // After one auto-move the board should still be internally consistent.
        // Verify by checking that the reported peg count matches actual pegs.
        game.makeMove(null);
        int counted = 0;
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG)
                    counted++;
        assertEquals(counted, game.getPegCount());
    }

    @Test
    void makeMove_returnsFalseWhenGameOver() {
        // Force game over: leave one peg, no valid moves
        clearBoardExcept(3, 3);
        assertFalse(game.makeMove(null));
    }

    @Test
    void makeMove_ignoresSuppliedMove() {
        // AutoGame should pick its own move regardless of what's passed in.
        // Passing a clearly invalid move should still result in a valid auto-move.
        int before = game.getPegCount();
        game.makeMove(new Move(0, 0, 0, 0)); // nonsense move
        assertEquals(before - 1, game.getPegCount()); // auto still moved
    }

    // -------------------------------------------------------------------------
    // Move history
    // -------------------------------------------------------------------------

    @Test
    void moveHistory_recordsAutoMoves() {
        game.makeMove(null);
        game.makeMove(null);
        assertEquals(2, game.getMoveHistory().size());
    }

    @Test
    void moveHistory_allMovesAreValid() {
        // Play 5 auto-moves, then verify each recorded move was valid at time of play.
        // We can't replay validity easily, so we just check they have sensible coordinates.
        for (int i = 0; i < 5; i++) game.makeMove(null);
        for (Move m : game.getMoveHistory()) {
            assertTrue(m.getFromRow() >= 0 && m.getFromRow() < game.getBoard().getSize());
            assertTrue(m.getFromCol() >= 0 && m.getFromCol() < game.getBoard().getSize());
            assertTrue(m.getToRow()   >= 0 && m.getToRow()   < game.getBoard().getSize());
            assertTrue(m.getToCol()   >= 0 && m.getToCol()   < game.getBoard().getSize());
        }
    }

    // -------------------------------------------------------------------------
    // runToEnd
    // -------------------------------------------------------------------------

    @Test
    void runToEnd_terminates() {
        // runToEnd() must not loop forever — it must stop when no moves remain.
        // This test will time out if there is an infinite loop.
        game.runToEnd();
        assertTrue(game.isOver());
    }

    @Test
    void runToEnd_leavesAtLeastOnePeg() {
        game.runToEnd();
        assertTrue(game.getPegCount() >= 1);
    }

    @Test
    void runToEnd_recordsAllMoves() {
        int startPegs = game.getPegCount();
        game.runToEnd();
        int endPegs = game.getPegCount();
        // Each move removes exactly one peg, so history size == pegs removed
        assertEquals(startPegs - endPegs, game.getMoveHistory().size());
    }

    @Test
    void runToEnd_doesNothingIfAlreadyOver() {
        clearBoardExcept(3, 3);
        int historyBefore = game.getMoveHistory().size();
        game.runToEnd();
        assertEquals(historyBefore, game.getMoveHistory().size());
    }

    // -------------------------------------------------------------------------
    // Works with other board types
    // -------------------------------------------------------------------------

    @Test
    void autoGame_worksWithHexagonBoard() {
        AutoGame hexGame = new AutoGame(new HexagonBoard());
        hexGame.runToEnd();
        assertTrue(hexGame.isOver());
        assertTrue(hexGame.getPegCount() >= 1);
    }

    // -------------------------------------------------------------------------
    // getModeName
    // -------------------------------------------------------------------------

    @Test
    void getModeName_isAutoplay() {
        assertEquals("Autoplay", game.getModeName());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void clearBoardExcept(int keepRow, int keepCol) {
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG
                        && !(r == keepRow && c == keepCol))
                    game.getBoard().setCell(r, c, CellState.EMPTY);
        game.getBoard().recountPegs();
    }
}