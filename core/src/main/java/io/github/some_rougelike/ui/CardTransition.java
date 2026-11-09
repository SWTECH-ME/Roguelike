package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import io.github.some_rougelike.GameConfig;

public class CardTransition {

    public static void applyTransition(Stage stage, TextureRegion[][] cardRegions, TextureRegionDrawable whiteTintableBg, boolean isEnteringMenu) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        java.util.Random rng = new java.util.Random(888);

        float baseScale = GameConfig.TRANSITION_CARD_MIN_SCALE + (GameConfig.TRANSITION_CARD_SCALE_VAR / 2f);
        float finalScale = baseScale * GameConfig.TRANSITION_CARD_SCALE_MULTIPLIER;
        
        float cardW = GameConfig.COMBAT_CARD_WIDTH * scale * finalScale;
        float cardH = GameConfig.COMBAT_CARD_HEIGHT * scale * finalScale;

        float stepX = cardW * 0.55f;
        float stepY = cardH * 0.55f;

        float overscanX = cardW * 1.5f;
        float overscanY = cardH * 1.5f;

        int cols = (int) ((width + overscanX * 2) / stepX) + 1;
        int rows = (int) ((height + overscanY * 2) / stepY) + 1;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                
                float restX = -overscanX + c * stepX + rng.nextFloat() * (stepX * 0.4f);
                float restY = -overscanY + r * stepY + rng.nextFloat() * (stepY * 0.4f);
                
                float restRot = rng.nextFloat() * 360f;
                
                float offX = restX + (rng.nextFloat() * 600f - 300f);
                float topY = height + overscanY + rng.nextFloat() * 600f;
                float bottomY = -overscanY - rng.nextFloat() * 600f;
                float offRot = restRot + (rng.nextFloat() * 360f - 180f);
                
                int texRow = rng.nextInt(4);
                int texCol = rng.nextInt(13);
                
                Image cardImg;
                if (cardRegions != null) {
                    cardImg = new Image(cardRegions[texRow][texCol]);
                } else {
                    cardImg = new Image(whiteTintableBg);
                    cardImg.setColor(GameConfig.COMBAT_CARD_BG_NORMAL);
                }
                
                float randomScale = finalScale * (0.9f + rng.nextFloat() * 0.2f);
                cardImg.setSize(GameConfig.COMBAT_CARD_WIDTH * scale * randomScale, GameConfig.COMBAT_CARD_HEIGHT * scale * randomScale);
                cardImg.setOrigin(Align.center);
                
                float delayBase = isEnteringMenu ? (1f - (r / (float)rows)) : (r / (float)rows);
                float delay = delayBase * (GameConfig.SCREEN_TRANSITION_DURATION * 0.4f) + rng.nextFloat() * 0.1f;
                float duration = GameConfig.SCREEN_TRANSITION_DURATION * 0.6f;

                if (isEnteringMenu) {
                    cardImg.setPosition(restX, restY);
                    cardImg.setRotation(restRot);
                    
                    cardImg.addAction(Actions.sequence(
                        Actions.delay(delay),
                        Actions.parallel(
                            Actions.moveTo(offX, bottomY, duration, Interpolation.pow2In),
                            Actions.rotateTo(offRot, duration, Interpolation.pow2In)
                        ),
                        Actions.removeActor()
                    ));
                } else {
                    cardImg.setPosition(offX, topY);
                    cardImg.setRotation(offRot);
                    
                    cardImg.addAction(Actions.sequence(
                        Actions.delay(delay),
                        Actions.parallel(
                            Actions.moveTo(restX, restY, duration, Interpolation.exp10Out),
                            Actions.rotateTo(restRot, duration, Interpolation.exp10Out)
                        )
                    ));
                }
                
                stage.addActor(cardImg);
            }
        }
    }
}