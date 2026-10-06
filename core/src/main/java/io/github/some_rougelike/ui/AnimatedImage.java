package io.github.some_rougelike.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AnimatedImage extends Image {
    private Animation<TextureRegion> animation;
    private float stateTime = 0;

    public AnimatedImage(Animation<TextureRegion> animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        ((TextureRegionDrawable)getDrawable()).setRegion(animation.getKeyFrame(stateTime, true));
    }
}