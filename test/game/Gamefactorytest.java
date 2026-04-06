package game;

import model.DiamondBoard;
import model.EnglishBoard;
import model.HexagonBoard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameFactory.
 *
 * Verifies that all valid board type + game mode combinations
 * produce the correct concrete types, and that invalid inputs
 * throw an appropriate exception.
 */
class GameFactoryTest {

    // -------------------------------------------------------------------------
    // Valid combinations
    // -------------------------------------------------------------------------

    @Test
    void create_englishManual_returnsManualGame() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(EnglishBoard.class, g.getBoard());
    }

    @Test
    void create_englishAutoplay_returnsAutoGame() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.AUTOPLAY);
        assertInstanceOf(AutoGame.class, g);
        assertInstanceOf(EnglishBoard.class, g.getBoard());
    }

    @Test
    void create_hexagonManual_returnsManualGame() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(HexagonBoard.class, g.getBoard());
    }

    @Test
    void create_hexagonAutoplay_returnsAutoGame() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.AUTOPLAY);
        assertInstanceOf(AutoGame.class, g);
        assertInstanceOf(HexagonBoard.class, g.getBoard());
    }

    @Test
    void create_diamondManual_returnsManualGame() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(DiamondBoard.class, g.getBoard());
    }

    @Test
    void create_diamondAutoplay_returnsAutoGame() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.AUTOPLAY);
        assertInstanceOf(AutoGame.class, g);
        assertInstanceOf(DiamondBoard.class, g.getBoard());
    }

    // -------------------------------------------------------------------------
    // Games start in the correct initial state
    // -------------------------------------------------------------------------

    @Test
    void newGame_isNotOver() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertFalse(g.isOver());
    }

    @Test
    void newGame_hasCorrectInitialPegCount_english() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertEquals(32, g.getPegCount());
    }

    @Test
    void newGame_hasCorrectInitialPegCount_hexagon() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.MANUAL);
        assertEquals(36, g.getPegCount());
    }

    @Test
    void newGame_hasCorrectInitialPegCount_diamond() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.MANUAL);
        assertEquals(40, g.getPegCount());
    }

    // -------------------------------------------------------------------------
    // Invalid inputs
    // -------------------------------------------------------------------------

    @Test
    void create_unknownBoardType_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> GameFactory.create("Triangle", GameFactory.MANUAL));
    }

    @Test
    void create_unknownGameMode_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> GameFactory.create(GameFactory.ENGLISH, "TwoPlayer"));
    }
}