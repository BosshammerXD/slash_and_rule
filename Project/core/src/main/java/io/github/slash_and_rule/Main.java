package io.github.slash_and_rule;

import com.badlogic.gdx.Game;

import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;

/**
 * aaaaa
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new DungeonCrawlerScene());
    }
}