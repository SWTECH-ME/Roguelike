package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.ui.SettingsUIBuilder;

public class SettingsScreen extends PageScreen {
    
    public SettingsScreen(Main main) {
        super(main, "SETTINGS");
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        SettingsUIBuilder uiBuilder = new SettingsUIBuilder();
        uiBuilder.build(this.stage, skin, this.main);
    }
}