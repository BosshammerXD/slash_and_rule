package io.github.slash_and_rule.Interfaces;

public interface Pausable {
    /**
     * This method is called when the game is paused.
     * Implementations should handle any necessary logic for pausing the game.
     */
    void pause();

    /**
     * This method is called when the game is resumed after being paused.
     * Implementations should handle any necessary logic for resuming the game.
     */
    void resume();
}
