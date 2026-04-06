package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the redesigned HexagonBoard.
 *
 * Layout (symmetric, orthogonal moves only):
 *   Row 0: cols 2-5  (4 cells)
 *   Row 1: cols 1-5  (5 cells)
 *   Row 2: cols 1-6  (6 cells)
 *   Row 3: cols 0-6  (7 cells) <- widest, center at (3,3)
 *   Row 4: cols 1-6  (6 cells)
 *   Row 5: cols 1-5  (5 cells)
 *   Row 6: cols 2-5  (4 cells)
 */
class HexagonBoardTest {

    private HexagonBoard board;

    @BeforeEach
    void setUp() {
        board = new HexagonBoard();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void initialPegCount_is36() {
        // 3+5+7+7+7+5+3 = 37 holes, minus 1 center = 36
        assertEquals(36, board.getPegCount());
    }

    @Test
    void centerCell_isEmptyAtStart() {
        assertEquals(CellState.EMPTY, board.getCell(3, 3));
    }

    @Test
    void invalidCells_areMarkedCorrectly() {
        // Row 0 only has cols 2-4
        assertEquals(CellState.INVALID, board.getCell(0, 0));
        assertEquals(CellState.INVALID, board.getCell(0, 1));
        assertEquals(CellState.INVALID, board.getCell(0, 5));
        assertEquals(CellState.INVALID, board.getCell(0, 6));
        // Row 6 only has cols 2-4
        assertEquals(CellState.INVALID, board.getCell(6, 0));
        assertEquals(CellState.INVALID, board.getCell(6, 1));
        assertEquals(CellState.INVALID, board.getCell(6, 5));
        assertEquals(CellState.INVALID, board.getCell(6, 6));
        // Row 1 only has cols 1-5
        assertEquals(CellState.INVALID, board.getCell(1, 0));
        assertEquals(CellState.INVALID, board.getCell(1, 6));
    }

    @Test
    void validCells_arePegAtStart() {
        assertEquals(CellState.PEG, board.getCell(0, 2));
        assertEquals(CellState.PEG, board.getCell(0, 4));
        assertEquals(CellState.PEG, board.getCell(3, 0));
        assertEquals(CellState.PEG, board.getCell(3, 6));
        assertEquals(CellState.PEG, board.getCell(6, 2));
        assertEquals(CellState.PEG, board.getCell(6, 4));
    }

    // -------------------------------------------------------------------------
    // Move validation — orthogonal only
    // -------------------------------------------------------------------------

    @Test
    void validMove_jumpRightIntoCenter() {
        assertTrue(board.isValidMove(3, 1, 3, 3));
    }

    @Test
    void validMove_jumpLeftIntoCenter() {
        assertTrue(board.isValidMove(3, 5, 3, 3));
    }

    @Test
    void validMove_jumpDownIntoCenter() {
        assertTrue(board.isValidMove(1, 3, 3, 3));
    }

    @Test
    void validMove_jumpUpIntoCenter() {
        assertTrue(board.isValidMove(5, 3, 3, 3));
    }

    @Test
    void invalidMove_diagonalNotAllowed() {
        assertFalse(board.isValidMove(1, 1, 3, 3));
        assertFalse(board.isValidMove(5, 5, 3, 3));
    }

    @Test
    void invalidMove_sourceIsEmpty() {
        assertFalse(board.isValidMove(3, 3, 3, 5));
    }

    @Test
    void invalidMove_sourceIsInvalid() {
        assertFalse(board.isValidMove(0, 0, 0, 2));
    }

    // -------------------------------------------------------------------------
    // Move application
    // -------------------------------------------------------------------------

    @Test
    void applyMove_updatesCellStates() {
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, board.getCell(3, 1));
        assertEquals(CellState.EMPTY, board.getCell(3, 2));
        assertEquals(CellState.PEG,   board.getCell(3, 3));
    }

    @Test
    void applyMove_decrementsPegCount() {
        int before = board.getPegCount();
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, board.getPegCount());
    }

    // -------------------------------------------------------------------------
    // getValidMoves at start
    // -------------------------------------------------------------------------

    @Test
    void getValidMoves_atStart_returns4Moves() {
        List<Move> moves = board.getValidMoves();
        assertEquals(4, moves.size());
    }

    @Test
    void getValidMoves_containsExpectedMoves() {
        List<Move> moves = board.getValidMoves();
        assertTrue(moves.contains(new Move(3, 1, 3, 3)));
        assertTrue(moves.contains(new Move(3, 5, 3, 3)));
        assertTrue(moves.contains(new Move(1, 3, 3, 3)));
        assertTrue(moves.contains(new Move(5, 3, 3, 3)));
    }

    // -------------------------------------------------------------------------
    // Game over
    // -------------------------------------------------------------------------

    @Test
    void isGameOver_falseAtStart() {
        assertFalse(board.isGameOver());
    }

    @Test
    void isGameOver_trueWithOnePeg() {
        clearBoard();
        board.setCell(3, 3, CellState.PEG);
        board.recountPegs();
        assertTrue(board.isGameOver());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void clearBoard() {
        for (int r = 0; r < board.getSize(); r++)
            for (int c = 0; c < board.getSize(); c++)
                if (board.getCell(r, c) == CellState.PEG)
                    board.setCell(r, c, CellState.EMPTY);
        board.recountPegs();
    }
}