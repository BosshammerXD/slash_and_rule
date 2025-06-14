package io.github.slash_and_rule.Animations;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Updatetable;

public class AnimationTest implements Displayable, Updatetable {
    private Animation<TextureAtlas.AtlasRegion> animation;
    private float x, y, width, height;
    private float elapsedTime = 0f;

    public AnimationTest(BaseScreen screen, String atlaspath, String ainmationName, float frameDuration, float x,
            float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        screen.drawableObjects.add(this);

        screen.getAssetManager().load(atlaspath, TextureAtlas.class);
        screen.getAssetManager().finishLoading();
        TextureAtlas atlas = screen.getAssetManager().get(atlaspath, TextureAtlas.class);
        animation = new Animation<>(frameDuration, atlas.findRegions(ainmationName), Animation.PlayMode.LOOP);
    }

    @Override
    public void update(float delta) {
        elapsedTime += delta;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // TODO Auto-generated method stub
        TextureAtlas.AtlasRegion frame = animation.getKeyFrame(elapsedTime, true);
        batch.draw(frame, x, y, width, height);
    }
}
