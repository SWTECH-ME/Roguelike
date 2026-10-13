package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import io.github.some_rougelike.ui.TrippyBackground;

public class RunMapScreen extends PageScreen {
    private RunState runState;

    public RunMapScreen(Main main, RunState runState) {
        super(main, "");
        this.runState = runState;
        
        float overscan = 150f;
        TrippyBackground bgImg = new TrippyBackground(new Color(0.08f, 0.1f, 0.15f, 1f), new Color(0.12f, 0.08f, 0.12f, 1f));
        bgImg.setSize(Gdx.graphics.getWidth() + (overscan * 2), Gdx.graphics.getHeight() + (overscan * 2));
        bgImg.setPosition(-overscan, -overscan);
        bgImg.setTouchable(Touchable.disabled);
        stage.addActor(bgImg);
        
        buildUI();
    }

    private TextureRegionDrawable createRoundedRect(int width, int height, float r, float g, float b, float a, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(r, g, b, a);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(tex);
    }

    private void buildUI() {
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Table headerPanel = new Table();
        headerPanel.setBackground(createRoundedRect(400, 80, 0.1f, 0.1f, 0.12f, 0.7f, 20));
        
        Label moneyLabel = new Label("Balance: $" + runState.getMoney(), skin);
        moneyLabel.setFontScale(1.8f);
        moneyLabel.setColor(Color.valueOf("FFD700"));
        headerPanel.add(moneyLabel).pad(15);
        
        root.add(headerPanel).padBottom(50).colspan(3);
        root.row();

        Table cardsTable = new Table();
        
        for (Enemy enemy : runState.getAvailableNormals()) {
            Table cardBg = new Table();
            cardBg.setBackground(createRoundedRect(220, 320, 0.12f, 0.25f, 0.18f, 0.85f, 25));
            cardBg.pad(20);
            cardBg.setTransform(true);
            cardBg.setOrigin(Align.center);
            cardBg.setTouchable(Touchable.enabled);

            Label typeLbl = new Label("NORMAL BLIND", skin);
            typeLbl.setFontScale(0.9f);
            typeLbl.setColor(Color.valueOf("99E899"));
            typeLbl.setTouchable(Touchable.disabled);

            Label nameLbl = new Label(enemy.getName(), skin);
            nameLbl.setFontScale(1.4f);
            nameLbl.setColor(Color.WHITE);
            nameLbl.setWrap(true);
            nameLbl.setAlignment(Align.center);
            nameLbl.setTouchable(Touchable.disabled);

            Label targetLbl = new Label("Target\n" + enemy.getTargetScore(), skin);
            targetLbl.setFontScale(1.2f);
            targetLbl.setColor(Color.WHITE);
            targetLbl.setAlignment(Align.center);
            targetLbl.setTouchable(Touchable.disabled);

            cardBg.add(typeLbl).padBottom(25).row();
            cardBg.add(nameLbl).width(180).expandY().row();
            cardBg.add(targetLbl).padTop(25).row();

            cardBg.addListener(new ClickListener() {
                private boolean isSwitching = false;

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (isSwitching) return;
                    cardBg.clearActions();
                    cardBg.addAction(Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.smooth));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (isSwitching) return;
                    cardBg.clearActions();
                    cardBg.addAction(Actions.scaleTo(1f, 1f, 0.15f, Interpolation.smooth));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isSwitching) return;
                    isSwitching = true;
                    cardBg.clearActions();
                    cardBg.addAction(Actions.sequence(
                        Actions.scaleTo(0.9f, 0.9f, 0.1f),
                        Actions.run(() -> {
                            runState.setActiveEnemy(enemy);
                            main.switchScreen(new CombatScreen(main, runState));
                        })
                    ));
                }
            });

            cardsTable.add(cardBg).size(220, 320).pad(20);
        }

        Enemy boss = runState.getBossEnemy();
        Table bossCardBg = new Table();
        bossCardBg.setBackground(createRoundedRect(220, 320, 0.35f, 0.1f, 0.15f, 0.85f, 25));
        bossCardBg.pad(20);
        bossCardBg.setTransform(true);
        bossCardBg.setOrigin(Align.center);
        bossCardBg.setTouchable(Touchable.enabled);

        Label bossTypeLbl = new Label("BOSS BLIND", skin);
        bossTypeLbl.setFontScale(0.9f);
        bossTypeLbl.setColor(Color.valueOf("FF9999"));
        bossTypeLbl.setTouchable(Touchable.disabled);

        String bossNameText = runState.isBossUnlocked() ? boss.getName() : "LOCKED";
        Label bossNameLbl = new Label(bossNameText, skin);
        bossNameLbl.setFontScale(1.4f);
        bossNameLbl.setColor(runState.isBossUnlocked() ? Color.WHITE : Color.DARK_GRAY);
        bossNameLbl.setWrap(true);
        bossNameLbl.setAlignment(Align.center);
        bossNameLbl.setTouchable(Touchable.disabled);

        Label bossTargetLbl = new Label("Target\n" + boss.getTargetScore(), skin);
        bossTargetLbl.setFontScale(1.2f);
        bossTargetLbl.setColor(runState.isBossUnlocked() ? Color.WHITE : Color.DARK_GRAY);
        bossTargetLbl.setAlignment(Align.center);
        bossTargetLbl.setTouchable(Touchable.disabled);

        bossCardBg.add(bossTypeLbl).padBottom(25).row();
        bossCardBg.add(bossNameLbl).width(180).expandY().row();
        bossCardBg.add(bossTargetLbl).padTop(25).row();

        if (runState.isBossUnlocked()) {
            bossCardBg.addListener(new ClickListener() {
                private boolean isSwitching = false;

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (isSwitching) return;
                    bossCardBg.clearActions();
                    bossCardBg.addAction(Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.smooth));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (isSwitching) return;
                    bossCardBg.clearActions();
                    bossCardBg.addAction(Actions.scaleTo(1f, 1f, 0.15f, Interpolation.smooth));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isSwitching) return;
                    isSwitching = true;
                    bossCardBg.clearActions();
                    bossCardBg.addAction(Actions.sequence(
                        Actions.scaleTo(0.9f, 0.9f, 0.1f),
                        Actions.run(() -> {
                            runState.setActiveEnemy(boss);
                            main.switchScreen(new CombatScreen(main, runState));
                        })
                    ));
                }
            });
        }
        
        cardsTable.add(bossCardBg).size(220, 320).pad(20);

        root.add(cardsTable).padBottom(50).colspan(3);
        root.row();

        TextButton.TextButtonStyle originalStyle = skin.get(TextButton.TextButtonStyle.class);
        TextButton.TextButtonStyle shopStyle = new TextButton.TextButtonStyle(originalStyle);
        shopStyle.up = createRoundedRect(220, 60, 0.2f, 0.2f, 0.22f, 0.9f, 25);
        shopStyle.down = createRoundedRect(220, 60, 0.15f, 0.15f, 0.17f, 0.9f, 25);
        
        TextButton shopBtn = new TextButton("ENTER SHOP", shopStyle);
        shopBtn.getLabel().setFontScale(1.2f);
        root.add(shopBtn).size(220, 60).colspan(3);
        shopBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Shop opened!");
            }
        });

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
}