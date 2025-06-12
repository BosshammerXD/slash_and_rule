package io.github.slash_and_rule.Interfaces;

import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.LoadingScreen.LoadingSchedule;

public interface Initalizable {
    void init(LoadingSchedule loader); // Method to initialize the object

    void show(AssetManager assetManager); // Method to show the object on the screen
}
