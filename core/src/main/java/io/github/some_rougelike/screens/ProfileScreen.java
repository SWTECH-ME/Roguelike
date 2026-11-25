package io.github.some_rougelike.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.ui.ProfileUIBuilder;

public class ProfileScreen extends PageScreen {
    private final Skin skin;
    private final ProfileUIBuilder uiBuilder;

    public ProfileScreen(Main main) {
        super(main, "PROFILE");
        this.skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.uiBuilder = new ProfileUIBuilder();
        this.uiBuilder.build(this.stage, this.skin, this.main, getScaleFactor());

        for (Actor actor : stage.getActors()) {
            if (actor instanceof Table) {
                actor.getColor().a = 0f;
                actor.addAction(Actions.fadeIn(GameConfig.SUBMENU_FADE_DURATION, Interpolation.fade));
            }
        }
    }

    /** 
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        boolean wasFading = false;
        float currentAlpha = 1f;
        for (int i = stage.getActors().size - 1; i >= 0; i--) {
            Actor actor = stage.getActors().get(i);
            if (actor instanceof Table) {
                if (actor.hasActions()) {
                    wasFading = true;
                    currentAlpha = actor.getColor().a;
                }
                actor.remove();
            }
        }
        uiBuilder.build(this.stage, this.skin, this.main, getScaleFactor());
        if (wasFading) {
            for (Actor actor : stage.getActors()) {
                if (actor instanceof Table) {
                    actor.getColor().a = currentAlpha;
                    actor.addAction(Actions.fadeIn(GameConfig.SUBMENU_FADE_DURATION, Interpolation.fade));
                }
            }
        }
    }
}
