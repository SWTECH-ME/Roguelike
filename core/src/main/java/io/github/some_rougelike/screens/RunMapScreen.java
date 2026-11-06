package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.ui.TrippyBackground;

public class RunMapScreen extends PageScreen {
    private RunState runState;
    private Skin uiSkin;
    private Array<Texture> generatedTextures;
    private TextureRegionDrawable whiteTintableBg;
    private Table uiRoot;
    private boolean isTransitioning = false;
    private Texture deckTexture;
    private TextureRegion[][] cardRegions;

    public RunMapScreen(Main main, RunState runState) {
        super(main, "");
        this.runState = runState;
        this.uiSkin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.generatedTextures = new Array<>();
        
        Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePix.setColor(Color.WHITE);
        whitePix.fill();
        Texture whiteTex = new Texture(whitePix);
        whiteTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        whitePix.dispose();
        generatedTextures.add(whiteTex);
        whiteTintableBg = new TextureRegionDrawable(whiteTex);

        try {
            FileHandle file = Gdx.files.internal("deck_skins/white_black.png");
            if (file.exists()) {
                deckTexture = new Texture(file);
                int cols = 13;
                int rows = 5;
                cardRegions = TextureRegion.split(deckTexture, deckTexture.getWidth() / cols, deckTexture.getHeight() / rows);
                generatedTextures.add(deckTexture);
            }
        } catch (Exception e) {}
        
        buildUI();
    }

    private TextureRegionDrawable createOutlinedRoundedRect(int width, int height, Color bgColor, Color borderColor, int borderThickness, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        pixmap.setColor(borderColor);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        pixmap.setColor(bgColor);
        int innerWidth = width - 2 * borderThickness;
        int innerHeight = height - 2 * borderThickness;
        int innerRadius = Math.max(0, radius - borderThickness);
        int offset = borderThickness;

        pixmap.fillRectangle(offset + innerRadius, offset, innerWidth - 2 * innerRadius, innerHeight);
        pixmap.fillRectangle(offset, offset + innerRadius, innerWidth, innerHeight - 2 * innerRadius);
        pixmap.fillCircle(offset + innerRadius, offset + innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerWidth - innerRadius, offset + innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerRadius, offset + innerHeight - innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerWidth - innerRadius, offset + innerHeight - innerRadius, innerRadius);

        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        generatedTextures.add(tex);
        
        return new TextureRegionDrawable(tex);
    }

    private TextureRegionDrawable createSolidRoundedRect(int width, int height, Color color, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        
        pixmap.setColor(color);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);
        
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        generatedTextures.add(tex);
        
        return new TextureRegionDrawable(tex);
    }

    private void buildUI() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        TrippyBackground bgImg = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        bgImg.setSize(width, height);
        bgImg.setPosition(0, 0);
        bgImg.setTouchable(Touchable.disabled);
        stage.addActor(bgImg);

        uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.center();

        Table cardsTable = new Table();
        
        int cardWidth = (int)(GameConfig.RUN_MAP_CARD_WIDTH * scale);
        int cardHeight = (int)(GameConfig.RUN_MAP_CARD_HEIGHT * scale);
        int cardRadius = (int)(GameConfig.RUN_MAP_CARD_RADIUS * scale);
        int borderThickness = (int)(GameConfig.RUN_MAP_BORDER_THICKNESS * scale);
        
        TextureRegionDrawable normalBg = createOutlinedRoundedRect(cardWidth, cardHeight, GameConfig.RUN_MAP_NORMAL_BG, Color.WHITE, borderThickness, cardRadius);
        TextureRegionDrawable bossBg = createOutlinedRoundedRect(cardWidth, cardHeight, GameConfig.RUN_MAP_BOSS_BG, new Color(0.9f, 0.2f, 0.2f, 1f), borderThickness, cardRadius);
        TextureRegionDrawable lockedBg = createOutlinedRoundedRect(cardWidth, cardHeight, GameConfig.RUN_MAP_LOCKED_BG, new Color(0.3f, 0.3f, 0.3f, 1f), borderThickness, cardRadius);
        
        int pillWidth = (int)(cardWidth - 80 * scale);
        int pillHeight = (int)(35 * scale);
        int pillRadius = (int)(17 * scale);
        
        TextureRegionDrawable normalPillBg = createSolidRoundedRect(pillWidth, pillHeight, GameConfig.RUN_MAP_NORMAL_PILL, pillRadius);
        TextureRegionDrawable bossPillBg = createSolidRoundedRect(pillWidth, pillHeight, GameConfig.RUN_MAP_BOSS_PILL, pillRadius);
        TextureRegionDrawable lockedPillBg = createSolidRoundedRect(pillWidth, pillHeight, GameConfig.RUN_MAP_LOCKED_PILL, pillRadius);

        for (Enemy enemy : runState.getAvailableNormals()) {
            boolean isDefeated = enemy.isDefeated();
            Group blindItem = createBlindCard("NORMAL BLIND", normalPillBg, enemy.getName(), enemy.getTargetScore(), GameConfig.REWARD_MONEY, false, isDefeated, scale, normalBg, cardWidth, cardHeight, () -> transitionToCombat(enemy));
            cardsTable.add(blindItem).size(cardWidth, cardHeight).pad(20 * scale);
        }

        Enemy boss = runState.getBossEnemy();
        boolean isBossUnlocked = runState.isBossUnlocked();
        boolean isBossDefeated = boss.isDefeated();
        
        Group bossItem = createBlindCard("BOSS BLIND", isBossUnlocked ? bossPillBg : lockedPillBg, boss.getName(), boss.getTargetScore(), GameConfig.REWARD_MONEY * 2, !isBossUnlocked, isBossDefeated, scale, isBossUnlocked ? bossBg : lockedBg, cardWidth, cardHeight, () -> transitionToCombat(boss));
        cardsTable.add(bossItem).size(cardWidth, cardHeight).pad(20 * scale);

        uiRoot.add(cardsTable).padBottom(50 * scale).colspan(3).row();

        Label shopBtn = new Label("ENTER SHOP", uiSkin);
        shopBtn.setFontScale(scale * 1.3f);
        shopBtn.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        shopBtn.setOrigin(Align.center);
        shopBtn.setAlignment(Align.center);
        shopBtn.setTouchable(Touchable.enabled);
        
        shopBtn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    shopBtn.clearActions();
                    shopBtn.addAction(Actions.parallel(
                        Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale * 1.1f, scale * 1.1f, 0.15f, Interpolation.fade)
                    ));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    shopBtn.clearActions();
                    shopBtn.addAction(Actions.parallel(
                        Actions.color(new Color(0.7f, 0.7f, 0.7f, 1f), 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.15f, Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isTransitioning) {
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                }
            }
        });

        uiRoot.add(shopBtn).colspan(3).center();
        stage.addActor(uiRoot);

        uiRoot.getColor().a = 0f;
        uiRoot.addAction(Actions.fadeIn(0.5f, Interpolation.fade));
    }

    private Group createBlindCard(String typeText, TextureRegionDrawable pillBg, String nameText, int target, int reward, boolean locked, boolean defeated, float scale, TextureRegionDrawable bg, int width, int height, Runnable onClick) {
        Group wrapper = new Group();
        wrapper.setSize(width, height);
        
        Table table = new Table();
        table.setSize(width, height);
        table.setBackground(bg);
        table.top().padTop(30 * scale).padBottom(25 * scale);

        Label typeLbl = new Label(typeText, uiSkin);
        typeLbl.setFontScale(scale * 0.9f);
        typeLbl.setColor(Color.WHITE);
        typeLbl.setAlignment(Align.center);
        
        Table typeBg = new Table();
        typeBg.setBackground(pillBg);
        typeBg.add(typeLbl).center();

        table.add(typeBg).size(width - 80 * scale, 35 * scale).padBottom(30 * scale).row();

        Label nameLbl = new Label(locked ? "LOCKED" : nameText, uiSkin);
        nameLbl.setFontScale(scale * 1.4f);
        nameLbl.setColor(locked ? new Color(0.5f, 0.5f, 0.5f, 1f) : Color.WHITE);
        nameLbl.setAlignment(Align.center);
        nameLbl.setWrap(true);

        table.add(nameLbl).width(width - 30 * scale).center().expandY().row();

        if (!locked) {
            Label rewardLbl = new Label("Reward: $" + reward, uiSkin);
            rewardLbl.setFontScale(scale * 1.0f);
            rewardLbl.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
            rewardLbl.setAlignment(Align.center);
            table.add(rewardLbl).padBottom(15 * scale).row();
        }

        Label targetLbl = new Label("Target: " + target, uiSkin);
        targetLbl.setFontScale(scale * 1.2f);
        targetLbl.setColor(locked ? new Color(0.4f, 0.4f, 0.4f, 1f) : Color.WHITE);
        targetLbl.setAlignment(Align.center);

        table.add(targetLbl).center();
        
        wrapper.addActor(table);

        if (defeated) {
            table.getColor().a = 0.4f;
            
            float diagLength = (float) Math.hypot(width, height) - (40 * scale);
            float angle = (float) Math.toDegrees(Math.atan2(height, width));
            
            Image strikeThrough = new Image(whiteTintableBg);
            strikeThrough.setColor(new Color(1f, 1f, 1f, 0.85f));
            strikeThrough.setSize(diagLength, 4 * scale);
            strikeThrough.setOrigin(diagLength / 2f, 2 * scale);
            strikeThrough.setPosition((width - diagLength) / 2f, (height - 4 * scale) / 2f);
            strikeThrough.setRotation(angle);
            
            wrapper.addActor(strikeThrough);
            wrapper.setTouchable(Touchable.disabled);
        } else if (locked) {
            wrapper.setTouchable(Touchable.disabled);
        } else {
            wrapper.setTouchable(Touchable.enabled);
            wrapper.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1 && !isTransitioning) {
                        table.clearActions();
                        table.addAction(Actions.moveTo(0, 15 * scale, 0.15f, Interpolation.smooth));
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1 && !isTransitioning) {
                        table.clearActions();
                        table.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.smooth));
                    }
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isTransitioning) {
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                        if (onClick != null) onClick.run();
                    }
                }
            });
        }

        return wrapper;
    }

    private void transitionToCombat(Enemy selectedEnemy) {
        if (isTransitioning) return;
        isTransitioning = true;
        
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;
        
        TextureRegionDrawable fallbackBg = createSolidRoundedRect((int)(100*scale), (int)(140*scale), GameConfig.RUN_MAP_NORMAL_PILL, (int)(10*scale));
        
        java.util.Random rng = new java.util.Random(888);
        int cardCount = GameConfig.TRANSITION_CARD_COUNT;
        
        for (int i = 0; i < cardCount; i++) {
            int r = rng.nextInt(4);
            int c = rng.nextInt(13);
            float finalScale = GameConfig.TRANSITION_CARD_MIN_SCALE + rng.nextFloat() * GameConfig.TRANSITION_CARD_SCALE_VAR;
            float targetX = -250f + rng.nextFloat() * (width + 500f);
            float targetY = -250f + rng.nextFloat() * (height + 500f);
            float targetRot = rng.nextFloat() * 360f;
            
            Image cardImg;
            if (cardRegions != null) {
                cardImg = new Image(cardRegions[r][c]);
            } else {
                cardImg = new Image(fallbackBg);
            }
            
            cardImg.setSize(GameConfig.COMBAT_CARD_WIDTH * scale * finalScale, GameConfig.COMBAT_CARD_HEIGHT * scale * finalScale);
            cardImg.setOrigin(Align.center);
            
            float startX = targetX + (rng.nextFloat() * 800f - 400f);
            float startY = height + 400f + rng.nextFloat() * 1000f;
            float startRot = targetRot + (rng.nextFloat() * 360f - 180f);
            
            cardImg.setPosition(startX, startY);
            cardImg.setRotation(startRot);
            
            float progress = (float) i / cardCount;
            float delay = (float) Math.pow(progress, 3.5) * 0.9f;
            float duration = 0.45f;
            
            cardImg.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(targetX, targetY, duration, Interpolation.exp10Out),
                    Actions.rotateTo(targetRot, duration, Interpolation.exp10Out)
                )
            ));
            
            stage.addActor(cardImg);
        }
        
        uiRoot.addAction(Actions.parallel(
            Actions.moveBy(0, -80f, 0.6f, Interpolation.pow2In),
            Actions.fadeOut(0.6f, Interpolation.fade)
        ));
        
        stage.addAction(Actions.sequence(
            Actions.delay(1.4f),
            Actions.run(() -> {
                runState.setActiveEnemy(selectedEnemy);
                main.switchScreen(new CombatScreen(main, runState));
            })
        ));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
    }
}