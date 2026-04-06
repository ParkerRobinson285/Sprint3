package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DiamondBoard.
 *
 * The diamond board has 41 holes on a 9x9 grid.
 * Row widths: 1, 3, 5, 7, 9, 7, 5, 3, 1 (centered).
 * Center at (4,4). Starting pegs: 40.
 */
class DiamondBoardTest {

    private DiamondBoard board;

    @BeforeEach
    void setUp() {
        board = new DiamondBoard();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void initialPegCount_is40() {
        // 41 valid holes minus 1 center hole = 40 pegs
        assertEquals(40, board.getPegCount());
    }

    @Test
    void centerCell_isEmptyAtStart() {
        assertEquals(CellState.EMPTY, board.getCell(4, 4));
    }

    @Test
    void cornerCells_areInvalid() {
        assertEquals(CellState.INVALID, board.getCell(0, 0));
        assertEquals(CellState.INVALID, board.getCell(0, 8));
        assertEquals(CellState.INVALID, board.getCell(8, 0));
        assertEquals(CellState.INVALID, board.getCell(8, 8));
        // Row 0 only has col 4 valid
        assertEquals(CellState.INVALID, board.getCell(0, 3));
        assertEquals(CellState.INVALID, board.getCell(0, 5));
    }

    @Test
    void tipCells_arePeg() {
        // The four tips of the diamond
        assertEquals(CellState.PEG, board.getCell(0, 4)); // top tip
        assertEquals(CellState.PEG, board.getCell(8, 4)); // bottom tip
        assertEquals(CellState.PEG, board.getCell(4, 0)); // left tip
        assertEquals(CellState.PEG, board.getCell(4, 8)); // right tip
    }

    @Test
    void rowBounds_areCorrect() {
        assertEquals(4, board.getRowColStart(0)); assertEquals(4, board.getRowColEnd(0)); // 1 cell
        assertEquals(3, board.getRowColStart(1)); assertEquals(5, board.getRowColEnd(1)); // 3 cells
        assertEquals(0, board.getRowColStart(4)); assertEquals(8, board.getRowColEnd(4)); // 9 cells
        assertEquals(3, board.getRowColStart(7)); assertEquals(5, board.getRowColEnd(7)); // 3 cells
        assertEquals(4, board.getRowColStart(8)); assertEquals(4, board.getRowColEnd(8)); // 1 cell
    }

    // -------------------------------------------------------------------------
    // Move validation — legal
    // -------------------------------------------------------------------------

    @Test
    void validMove_jumpRightIntoCenter() {
        // (4,2) -> (4,4): jumps over (4,3)
        assertTrue(board.isValidMove(4, 2, 4, 4));
    }

    @Test
    void validMove_jumpLeftIntoCenter() {
        // (4,6) -> (4,4): jumps over (4,5)
        assertTrue(board.isValidMove(4, 6, 4, 4));
    }

    @Test
    void validMove_jumpDownIntoCenter() {
        // (2,4) -> (4,4): jumps over (3,4)
        assertTrue(board.isValidMove(2, 4, 4, 4));
    }

    @Test
    void validMove_jumpUpIntoCenter() {
        // (6,4) -> (4,4): jumps over (5,4)
        assertTrue(board.isValidMove(6, 4, 4, 4));
    }

    // -------------------------------------------------------------------------
    // Move validation — illegal
    // -------------------------------------------------------------------------

    @Test
    void invalidMove_diagonalNotAllowed() {
        // Diamond board is orthogonal only — no diagonal moves
        assertFalse(board.isValidMove(2, 2, 4, 4));
        assertFalse(board.isValidMove(6, 6, 4, 4));
    }

    @Test
    void invalidMove_sourceIsEmpty() {
        assertFalse(board.isValidMove(4, 4, 4, 6)); // center is empty
    }

    @Test
    void invalidMove_jumpingToInvalidCell() {
        // (2,4) jumping up would land at (0,4) which IS valid (tip cell)
        // So let's test something truly invalid: (1,3) jumping left -> (-1,3)
        assertFalse(board.isValidMove(1, 3, 1, 1)); // (1,1) is INVALID on diamond
    }

    @Test
    void invalidMove_outOfBounds() {
        assertFalse(board.isValidMove(0, 4, -2, 4));
    }

    // -------------------------------------------------------------------------
    // Move application
    // -------------------------------------------------------------------------

    @Test
    void applyMove_updatesCellStates() {
        Move move = new Move(4, 2, 4, 4);
        board.applyMove(move);

        assertEquals(CellState.EMPTY, board.getCell(4, 2));
        assertEquals(CellState.EMPTY, board.getCell(4, 3));
        assertEquals(CellState.PEG,   board.getCell(4, 4));
    }

    @Test
    void applyMove_decrementsPegCount() {
        int before = board.getPegCount();
        board.applyMove(new Move(4, 2, 4, 4));
        assertEquals(before - 1, board.getPegCount());
    }

    @Test
    void applyMove_throwsOnInvalidMove() {
        assertThrows(IllegalArgumentException.class,
            () -> board.applyMove(new Move(4, 4, 4, 6))); // source is empty
    }

    // -------------------------------------------------------------------------
    // getValidMoves at start
    // -------------------------------------------------------------------------

    @Test
    void getValidMoves_atStart_returns4Moves() {
        // Center-empty start: 4 orthogonal jumps into center
        List<Move> moves = board.getValidMoves();
        assertEquals(4, moves.size());
    }

    @Test
    void getValidMoves_containsExpectedMoves() {
        List<Move> moves = board.getValidMoves();
        assertTrue(moves.contains(new Move(4, 2, 4, 4)));
        assertTrue(moves.contains(new Move(4, 6, 4, 4)));
        assertTrue(moves.contains(new Move(2, 4, 4, 4)));
        assertTrue(moves.contains(new Move(6, 4, 4, 4)));
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
        board.setCell(4, 4, CellState.PEG);
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