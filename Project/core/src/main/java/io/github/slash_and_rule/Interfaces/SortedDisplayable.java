package io.github.slash_and_rule.Interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface SortedDisplayable {
    public float getSortIndex();

    public void draw(SpriteBatch batch);
}
