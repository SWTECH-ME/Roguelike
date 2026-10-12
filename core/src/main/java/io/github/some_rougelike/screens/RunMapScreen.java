package io.github.some_rougelike.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;

public class RunMapScreen extends PageScreen {
    private RunState runState;

    public RunMapScreen(Main main, RunState runState) {
        super(main, "CHAPTER " + runState.getChapterIndex());
        this.runState = runState;
        buildUI();
    }

    private TextureRegionDrawable createRoundedCardDrawable(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(120, 180, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(r, g, b, a);
        pixmap.fillRectangle(10, 0, 100, 180);
        pixmap.fillRectangle(0, 10, 120, 160);
        pixmap.fillCircle(10, 10, 10);
        pixmap.fillCircle(110, 10, 10);
        pixmap.fillCircle(10, 170, 10);
        pixmap.fillCircle(110, 170, 10);
        return new TextureRegionDrawable(new Texture(pixmap));
    }

    private void buildUI() {
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label moneyLabel = new Label("Mulah: $" + runState.getMoney(), skin);
        moneyLabel.setFontScale(1.5f);
        moneyLabel.setColor(Color.GOLD);
        root.add(moneyLabel).padBottom(40).colspan(3);
        root.row();

        Table cardsTable = new Table();
        
        for (Enemy enemy : runState.getAvailableNormals()) {
            Table cardBg = new Table();
            cardBg.setBackground(createRoundedCardDrawable(0.1f, 0.4f, 0.2f, 0.9f));
            cardBg.pad(15);

            Label typeLbl = new Label("NORMAL BLIND", skin);
            typeLbl.setFontScale(0.8f);
            typeLbl.setColor(Color.LIGHT_GRAY);

            Label nameLbl = new Label(enemy.getName(), skin);
            nameLbl.setFontScale(1.2f);
            nameLbl.setColor(Color.WHITE);
            nameLbl.setWrap(true);
            nameLbl.setAlignment(Align.center);

            Label targetLbl = new Label("Target:\n" + enemy.getTargetScore(), skin);
            targetLbl.setFontScale(1f);
            targetLbl.setColor(Color.CYAN);
            targetLbl.setAlignment(Align.center);

            cardBg.add(typeLbl).padBottom(15).row();
            cardBg.add(nameLbl).width(160).padBottom(20).row();
            cardBg.add(targetLbl).row();

            cardBg.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    runState.setActiveEnemy(enemy);
                    main.switchScreen(new CombatScreen(main, runState));
                }
            });

            cardsTable.add(cardBg).size(200, 300).pad(15);
        }

        Enemy boss = runState.getBossEnemy();
        Table bossCardBg = new Table();
        bossCardBg.setBackground(createRoundedCardDrawable(0.5f, 0.1f, 0.1f, 0.9f));
        bossCardBg.pad(15);

        Label bossTypeLbl = new Label("BOSS BLIND", skin);
        bossTypeLbl.setFontScale(0.8f);
        bossTypeLbl.setColor(Color.LIGHT_GRAY);

        String bossNameText = runState.isBossUnlocked() ? boss.getName() : "LOCKED";
        Label bossNameLbl = new Label(bossNameText, skin);
        bossNameLbl.setFontScale(1.2f);
        bossNameLbl.setColor(runState.isBossUnlocked() ? Color.WHITE : Color.DARK_GRAY);
        bossNameLbl.setWrap(true);
        bossNameLbl.setAlignment(Align.center);

        Label bossTargetLbl = new Label("Target:\n" + boss.getTargetScore(), skin);
        bossTargetLbl.setFontScale(1f);
        bossTargetLbl.setColor(Color.CYAN);
        bossTargetLbl.setAlignment(Align.center);

        bossCardBg.add(bossTypeLbl).padBottom(15).row();
        bossCardBg.add(bossNameLbl).width(160).padBottom(20).row();
        bossCardBg.add(bossTargetLbl).row();

        if (runState.isBossUnlocked()) {
            bossCardBg.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    runState.setActiveEnemy(boss);
                    main.switchScreen(new CombatScreen(main, runState));
                }
            });
        }
        
        cardsTable.add(bossCardBg).size(200, 300).pad(15);

        root.add(cardsTable).padBottom(50).colspan(3);
        root.row();

        TextButton shopBtn = new TextButton("SHOP", skin);
        root.add(shopBtn).size(200, 50).colspan(3);
        shopBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Shop opened!");
            }
        });

        stage.addActor(root);
    }
}