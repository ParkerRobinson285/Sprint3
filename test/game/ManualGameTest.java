package game;

import model.CellState;
import model.EnglishBoard;
import model.Move;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ManualGame.
 *
 * Covers:
 *   - makeMove() with valid and invalid moves
 *   - move history recording
 *   - isOver() detection
 *   - randomize() produces a valid board state
 *   - getValidMoves() delegation
 *   - getRating() delegation
 */
class ManualGameTest {

    private ManualGame game;

    @BeforeEach
    void setUp() {
        game = new ManualGame(new EnglishBoard());
    }

    // -------------------------------------------------------------------------
    // makeMove — valid moves
    // -------------------------------------------------------------------------

    @Test
    void makeMove_validMove_returnsTrue() {
        assertTrue(game.makeMove(new Move(3, 1, 3, 3)));
    }

    @Test
    void makeMove_validMove_updatesBoardState() {
        game.makeMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, game.getBoard().getCell(3, 1));
        assertEquals(CellState.EMPTY, game.getBoard().getCell(3, 2));
        assertEquals(CellState.PEG,   game.getBoard().getCell(3, 3));
    }

    @Test
    void makeMove_validMove_decrementsPegCount() {
        int before = game.getPegCount();
        game.makeMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, game.getPegCount());
    }

    // -------------------------------------------------------------------------
    // makeMove — invalid moves
    // -------------------------------------------------------------------------

    @Test
    void makeMove_invalidMove_returnsFalse() {
        // (3,3) is empty — can't move from there
        assertFalse(game.makeMove(new Move(3, 3, 3, 5)));
    }

    @Test
    void makeMove_invalidMove_doesNotChangePegCount() {
        int before = game.getPegCount();
        game.makeMove(new Move(3, 3, 3, 5));
        assertEquals(before, game.getPegCount());
    }

    @Test
    void makeMove_nullMove_returnsFalse() {
        assertFalse(game.makeMove(null));
    }

    // -------------------------------------------------------------------------
    // Move history
    // -------------------------------------------------------------------------

    @Test
    void moveHistory_emptyAtStart() {
        assertTrue(game.getMoveHistory().isEmpty());
    }

    @Test
    void moveHistory_recordsSuccessfulMoves() {
        Move m1 = new Move(3, 1, 3, 3);
        Move m2 = new Move(1, 3, 3, 3); // after m1 the center has a peg, but (3,3) is filled now
        // Use two sequential valid moves instead
        game.makeMove(m1);                        // peg now at (3,3)
        game.makeMove(new Move(3, 4, 3, 2));      // another valid move after state change

        List<Move> history = game.getMoveHistory();
        assertEquals(2, history.size());
        assertEquals(m1, history.get(0));
    }

    @Test
    void moveHistory_doesNotRecordFailedMoves() {
        game.makeMove(new Move(3, 3, 3, 5)); // invalid
        assertTrue(game.getMoveHistory().isEmpty());
    }

    @Test
    void moveHistory_isUnmodifiable() {
        game.makeMove(new Move(3, 1, 3, 3));
        List<Move> history = game.getMoveHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.add(new Move(0, 0, 0, 2)));
    }

    // -------------------------------------------------------------------------
    // Game over
    // -------------------------------------------------------------------------

    @Test
    void isOver_falseAtStart() {
        assertFalse(game.isOver());
    }

    @Test
    void isOver_trueWhenNoMovesRemain() {
        // Manually gut the board down to one peg with no valid moves
        clearBoardExcept(3, 3);
        assertTrue(game.isOver());
    }

    // -------------------------------------------------------------------------
    // getValidMoves delegation
    // -------------------------------------------------------------------------

    @Test
    void getValidMoves_atStart_returns4() {
        assertEquals(4, game.getValidMoves().size());
    }

    // -------------------------------------------------------------------------
    // getRating delegation
    // -------------------------------------------------------------------------

    @Test
    void getRating_atStart_isAverage() {
        assertEquals("Average", game.getRating());
    }

    @Test
    void getRating_outstanding_whenOnePegLeft() {
        clearBoardExcept(3, 3);
        assertEquals("Outstanding", game.getRating());
    }

    // -------------------------------------------------------------------------
    // getModeName
    // -------------------------------------------------------------------------

    @Test
    void getModeName_isManual() {
        assertEquals("Manual", game.getModeName());
    }

    // -------------------------------------------------------------------------
    // randomize
    // -------------------------------------------------------------------------

    @Test
    void randomize_producesValidBoardState() {
        // After randomizing, the board should still be internally consistent:
        // every PEG cell has a valid CellState, and peg count matches actual pegs
        game.randomize();
        int counted = 0;
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG)
                    counted++;
        assertEquals(counted, game.getPegCount());
    }

    @Test
    void randomize_reducesPegCount() {
        int before = game.getPegCount();
        game.randomize(3, 3); // exactly 3 moves
        assertEquals(before - 3, game.getPegCount());
    }

    @Test
    void randomize_doesNotCorruptBoard() {
        // Board should still respond to getValidMoves() without throwing
        game.randomize();
        assertNotNull(game.getValidMoves());
    }

    @Test
    void randomize_doesNotAddToMoveHistory() {
        game.randomize(5, 5);
        // Randomize is not part of the player's move record
        assertTrue(game.getMoveHistory().isEmpty());
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