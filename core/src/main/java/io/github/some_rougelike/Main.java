package io.github.some_rougelike;

import com.badlogic.gdx.Game;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        Assets.getInstance().load();
        Assets.getInstance().finishLoading();
        SoundManager.getInstance().load();
        
        this.setScreen(new MainMenuScreen());
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        SoundManager.getInstance().dispose();
        Assets.getInstance().dispose();
    }
}