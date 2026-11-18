package io.github.some_rougelike.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
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
        this.uiBuilder.build(this.stage, this.skin, this.main, getScaleFactor(), true);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        for (int i = stage.getActors().size - 1; i >= 0; i--) {
            Actor actor = stage.getActors().get(i);
            if (actor instanceof Table) {
                actor.remove();
            }
        }
        uiBuilder.build(this.stage, this.skin, this.main, getScaleFactor(), false);
    }
}
