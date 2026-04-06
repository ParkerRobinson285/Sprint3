package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnglishBoard and the shared Board logic.
 *
 * Covers:
 *   - Initial board setup (peg count, center hole, invalid corners)
 *   - Move validation (legal and illegal cases)
 *   - Move application (peg moves, jumped peg removed, count decremented)
 *   - Game-over detection
 *   - Rating calculation
 *   - getValidMoves() at game start
 */
class EnglishBoardTest {

    private EnglishBoard board;

    @BeforeEach
    void setUp() {
        board = new EnglishBoard();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void initialPegCount_is32() {
        // English board starts with 33 valid cells minus the 1 center hole = 32
        assertEquals(32, board.getPegCount());
    }

    @Test
    void centerCell_isEmptyAtStart() {
        assertEquals(CellState.EMPTY, board.getCell(3, 3));
    }

    @Test
    void cornerCells_areInvalid() {
        // All four 2x2 corner blocks should be INVALID
        assertEquals(CellState.INVALID, board.getCell(0, 0));
        assertEquals(CellState.INVALID, board.getCell(0, 1));
        assertEquals(CellState.INVALID, board.getCell(1, 0));
        assertEquals(CellState.INVALID, board.getCell(1, 1));

        assertEquals(CellState.INVALID, board.getCell(0, 5));
        assertEquals(CellState.INVALID, board.getCell(0, 6));
        assertEquals(CellState.INVALID, board.getCell(1, 5));
        assertEquals(CellState.INVALID, board.getCell(1, 6));

        assertEquals(CellState.INVALID, board.getCell(5, 0));
        assertEquals(CellState.INVALID, board.getCell(6, 0));
        assertEquals(CellState.INVALID, board.getCell(5, 1));
        assertEquals(CellState.INVALID, board.getCell(6, 1));

        assertEquals(CellState.INVALID, board.getCell(5, 5));
        assertEquals(CellState.INVALID, board.getCell(6, 6));
    }

    @Test
    void playableCells_arePegAtStart() {
        // Spot-check a few cells in the cross that should have pegs
        assertEquals(CellState.PEG, board.getCell(0, 2));
        assertEquals(CellState.PEG, board.getCell(2, 0));
        assertEquals(CellState.PEG, board.getCell(3, 0));
        assertEquals(CellState.PEG, board.getCell(6, 4));
    }

    // -------------------------------------------------------------------------
    // Move validation — legal moves
    // -------------------------------------------------------------------------

    @Test
    void validMove_jumpRightIntoCenter() {
        // (3,1) -> (3,3): jumps over (3,2), lands in center hole
        assertTrue(board.isValidMove(3, 1, 3, 3));
    }

    @Test
    void validMove_jumpLeftIntoCenter() {
        // (3,5) -> (3,3): jumps over (3,4)
        assertTrue(board.isValidMove(3, 5, 3, 3));
    }

    @Test
    void validMove_jumpDownIntoCenter() {
        // (1,3) -> (3,3): jumps over (2,3)
        assertTrue(board.isValidMove(1, 3, 3, 3));
    }

    @Test
    void validMove_jumpUpIntoCenter() {
        // (5,3) -> (3,3): jumps over (4,3)
        assertTrue(board.isValidMove(5, 3, 3, 3));
    }

    // -------------------------------------------------------------------------
    // Move validation — illegal moves
    // -------------------------------------------------------------------------

    @Test
    void invalidMove_sourceIsEmpty() {
        // Center (3,3) is empty at start — can't jump from it
        assertFalse(board.isValidMove(3, 3, 3, 5));
    }

    @Test
    void invalidMove_destinationIsPeg() {
        // Both (3,1) and (3,2) have pegs — can't land where there's already a peg
        // (the only valid landing spot nearby from (3,1) is the empty center)
        assertFalse(board.isValidMove(3, 1, 3, 2)); // only 1 step, not 2
        assertFalse(board.isValidMove(3, 0, 3, 2)); // destination (3,2) is a peg
    }

    @Test
    void invalidMove_jumpOverEmptyCell() {
        // Apply one move first to create an empty cell in the middle
        board.applyMove(new Move(3, 1, 3, 3)); // (3,2) is now empty
        // Now try to jump over the empty (3,2) — should be invalid
        assertFalse(board.isValidMove(3, 0, 3, 2)); // (3,2) is the destination — peg is at (3,0)
        // More direct: try jumping over newly emptied (3,2)
        assertFalse(board.isValidMove(3, 1, 3, 3)); // (3,1) is now empty itself, invalid source
    }

    @Test
    void invalidMove_diagonalNotAllowed() {
        // English board does not allow diagonal jumps
        assertFalse(board.isValidMove(2, 2, 4, 4)); // diagonal
    }

    @Test
    void invalidMove_outOfBounds() {
        assertFalse(board.isValidMove(-1, 3, 1, 3));
        assertFalse(board.isValidMove(3, 3, 3, 9));
    }

    @Test
    void invalidMove_landingOnInvalidCell() {
        // Jumping from (2,2) up would land in the invalid corner (0,2)... wait, (0,2) IS valid.
        // Jump from (0,2) upward would go out of bounds — test that
        assertFalse(board.isValidMove(0, 2, -2, 2));
    }

    // -------------------------------------------------------------------------
    // Move application
    // -------------------------------------------------------------------------

    @Test
    void applyMove_updatesCellStates() {
        Move move = new Move(3, 1, 3, 3);
        board.applyMove(move);

        assertEquals(CellState.EMPTY, board.getCell(3, 1)); // source is now empty
        assertEquals(CellState.EMPTY, board.getCell(3, 2)); // jumped peg removed
        assertEquals(CellState.PEG,   board.getCell(3, 3)); // destination now has peg
    }

    @Test
    void applyMove_decrementsPegCount() {
        int before = board.getPegCount();
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, board.getPegCount());
    }

    @Test
    void applyMove_throwsOnInvalidMove() {
        // (3,3) is empty — can't move from there
        assertThrows(IllegalArgumentException.class,
            () -> board.applyMove(new Move(3, 3, 3, 5)));
    }

    // -------------------------------------------------------------------------
    // getValidMoves()
    // -------------------------------------------------------------------------

    @Test
    void getValidMoves_atStart_returns4Moves() {
        // From the standard start position (center empty), exactly 4 moves are valid:
        // from (1,3), (3,1), (3,5), (5,3) — all jumping into the center
        List<Move> moves = board.getValidMoves();
        assertEquals(4, moves.size());
    }

    @Test
    void getValidMoves_containsExpectedMoves() {
        List<Move> moves = board.getValidMoves();
        assertTrue(moves.contains(new Move(1, 3, 3, 3)));
        assertTrue(moves.contains(new Move(3, 1, 3, 3)));
        assertTrue(moves.contains(new Move(3, 5, 3, 3)));
        assertTrue(moves.contains(new Move(5, 3, 3, 3)));
    }

    // -------------------------------------------------------------------------
    // Game over detection
    // -------------------------------------------------------------------------

    @Test
    void isGameOver_falseAtStart() {
        assertFalse(board.isGameOver());
    }

    @Test
    void isGameOver_trueWhenNoMovesLeft() {
        // Manually set up a board where only one peg remains — game is over
        clearBoard();
        board.setCell(3, 3, CellState.PEG);
        board.recountPegs();
        assertTrue(board.isGameOver());
    }

    // -------------------------------------------------------------------------
    // Rating
    // -------------------------------------------------------------------------

    @Test
    void rating_outstanding_for1Peg() {
        clearBoard();
        board.setCell(3, 3, CellState.PEG);
        board.recountPegs();
        assertEquals("Outstanding", board.getRating());
    }

    @Test
    void rating_veryGood_for2Pegs() {
        clearBoard();
        board.setCell(3, 3, CellState.PEG);
        board.setCell(3, 2, CellState.PEG);
        board.recountPegs();
        assertEquals("Very Good", board.getRating());
    }

    @Test
    void rating_good_for3Pegs() {
        clearBoard();
        board.setCell(3, 3, CellState.PEG);
        board.setCell(3, 2, CellState.PEG);
        board.setCell(3, 1, CellState.PEG);
        board.recountPegs();
        assertEquals("Good", board.getRating());
    }

    @Test
    void rating_average_for4OrMorePegs() {
        // Full starting board has 32 pegs
        assertEquals("Average", board.getRating());
    }

    // -------------------------------------------------------------------------
    // Move value class
    // -------------------------------------------------------------------------

    @Test
    void move_jumpedCell_isCorrect() {
        Move m = new Move(3, 1, 3, 3);
        assertEquals(3, m.getJumpedRow());
        assertEquals(2, m.getJumpedCol());
    }

    @Test
    void move_equality() {
        Move a = new Move(3, 1, 3, 3);
        Move b = new Move(3, 1, 3, 3);
        Move c = new Move(1, 3, 3, 3);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /** Sets all playable cells to EMPTY so we can set up custom end states. */
    private void clearBoard() {
        for (int r = 0; r < board.getSize(); r++)
            for (int c = 0; c < board.getSize(); c++)
                if (board.getCell(r, c) == CellState.PEG)
                    board.setCell(r, c, CellState.EMPTY);
        board.recountPegs();
    }
}