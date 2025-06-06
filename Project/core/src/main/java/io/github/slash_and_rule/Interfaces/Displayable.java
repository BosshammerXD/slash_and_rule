package io.github.slash_and_rule.Interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Displayable {
    /**
     * Draws the object on the screen.
     * This method should be implemented to define how the object is rendered.
     */
    void draw(SpriteBatch batch);

    /**
     * This method is called when the object is no longer needed and should be
     * hidden.
     */
    void hide();
}
