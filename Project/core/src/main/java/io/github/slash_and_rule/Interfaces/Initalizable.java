package io.github.slash_and_rule.Interfaces;

import java.util.Stack;

import com.badlogic.gdx.assets.AssetManager;

public interface Initalizable {
    void init(AssetManager assetManager, Stack<Runnable> todo); // Method to initialize the object

    void show(AssetManager assetManager); // Method to show the object on the screen

    void dispose(); // Method to dispose of the object when no longer needed
}
