package io.github.slash_and_rule.Interfaces;

import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.LoadingScreen;

public interface Initalizable {
    void init(LoadingScreen loader); // Method to initialize the object

    void show(AssetManager assetManager); // Method to show the object on the screen
}
