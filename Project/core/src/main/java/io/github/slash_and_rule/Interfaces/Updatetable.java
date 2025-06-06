package io.github.slash_and_rule.Interfaces;

public interface Updatetable {
    /**
     * Updates the object with the given delta time.
     * This method should be implemented to define how the object behaves over time.
     * 
     * @param delta The time in seconds since the last update.
     */
    public void update(float delta);
}
