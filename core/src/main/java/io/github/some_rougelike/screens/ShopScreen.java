package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.CheatCard;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.logic.HotCard;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.ui.CardTransition;
import io.github.some_rougelike.ui.HotCardBar;
import io.github.some_rougelike.ui.TrippyBackground;

/**
 * Bolt.
 * Bal oldal: cheat lapok ágrajzban. Csak az alsó két lap vehető meg; vásárláskor a megvett lap
 * feletti pár lecsúszik az alsó sorba, a másik ág elveszik, felülre pedig 4 új random lap kerül.
 * Jobb oldal: random franciakártyák, minden látogatáskor újak, rerollal cserélhetők.
 */
public class ShopScreen extends PageScreen implements CheatCard.ShopContext {
    private static final float REF_W = 1920f;
    private static final float REF_H = 1080f;

    private static final Color GOLD = new Color(1f, 0.78f, 0.35f, 1f);
    private static final Color RED_BORDER = new Color(0.75f, 0.16f, 0.14f, 1f);
    private static final Color PANEL_BG = new Color(0.09f, 0.07f, 0.08f, 0.92f);
    private static final Color RIGHT_BG = new Color(0.06f, 0.15f, 0.15f, 0.92f);
    private static final Color TEAL = new Color(0.45f, 0.72f, 0.68f, 1f);
    private static final Color PRICE_BG = new Color(0.12f, 0.12f, 0.08f, 1f);
    private static final Color PRICE_BORDER = new Color(0.55f, 0.45f, 0.2f, 1f);
    private static final Color INFO_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color ERROR_COLOR = new Color(1f, 0.35f, 0.35f, 1f);
    private static final float LOCKED_ALPHA = 0.55f;

    /** A cheat lap képekről levágott átlátszó keret (a képek 1520x1640-esek, a lap ezen belül van). */
    private static final int CHEAT_CROP_X = 275, CHEAT_CROP_Y = 112, CHEAT_CROP_W = 985, CHEAT_CROP_H = 1390;

    // Bal panel geometria (referencia px, 1920x1080-hoz)
    private static final float LEFT_W = 860, LEFT_H = 680, RIGHT_W = 640;
    private static final float ITEM_W = 140, ITEM_IMG_H = 196, ITEM_H = 286;
    private static final float TOP_Y = LEFT_H - 24 - ITEM_H, BOTTOM_Y = 24;
    private static final float[] PAIR_CENTER = { 215, 645 };

    private final RunState runState;
    private final Skin uiSkin;
    private final Array<Texture> generatedTextures = new Array<>();
    private final TextureRegionDrawable whiteTintableBg;
    private final Map<CheatCard, TextureRegion> cheatRegions = new EnumMap<>(CheatCard.class);
    private TextureRegion[][] cardRegions;

    private final float s;
    private Table uiRoot;
    private Label moneyLabel;
    private Label messageLabel;
    private Label infoLabel;
    private Label rerollLabel;
    private TextureRegionDrawable priceBg;

    // Bal oldal állapota: top[0..1] a bottom[0] felett, top[2..3] a bottom[1] felett
    private Group treeGroup;
    private final ItemView[] topViews = new ItemView[4];
    private final ItemView[] bottomViews = new ItemView[2];
    private boolean treeAnimating = false;

    // Jobb oldal állapota (null = eladva)
    private Group cardsForSaleGroup;
    private final List<HotCard> stock = new ArrayList<>();
    private HotCardBar hotCardBar;
    private int freeRerolls;
    private boolean discountActive = false;
    private boolean rerolledThisVisit = false;

    private boolean isTransitioning = false;

    private static final class ItemView {
        final CheatCard card;
        final Group actor;
        final Table content;
        Label priceLabel;
        boolean buyable;

        ItemView(CheatCard card, Group actor, Table content) {
            this.card = card;
            this.actor = actor;
            this.content = content;
        }
    }

    public ShopScreen(Main main, RunState runState) {
        super(main, "");
        this.runState = runState;
        this.uiSkin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.s = Math.min(Gdx.graphics.getWidth() / REF_W, Gdx.graphics.getHeight() / REF_H);
        this.freeRerolls = runState.hasLuckySeven() ? 1 : 0;

        Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePix.setColor(Color.WHITE);
        whitePix.fill();
        Texture whiteTex = new Texture(whitePix);
        whitePix.dispose();
        generatedTextures.add(whiteTex);
        whiteTintableBg = new TextureRegionDrawable(whiteTex);

        loadDeckTexture();
        loadCheatTextures();
        rollStock();
        buildUI();

        CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, true);
    }

    // ---------------------------------------------------------------- assets

    private void loadDeckTexture() {
        try {
            FileHandle file = Gdx.files.internal(SaveManager.getInstance().getSelectedDeckSkinPath());
            if (!file.exists()) {
                file = Gdx.files.internal(GameConfig.DECK_SKINS_DIR + "/" + GameConfig.DEFAULT_DECK_SKIN);
            }
            if (file.exists()) {
                Texture deckTexture = new Texture(file);
                cardRegions = TextureRegion.split(deckTexture,
                    deckTexture.getWidth() / GameConfig.DECK_SHEET_COLS,
                    deckTexture.getHeight() / GameConfig.DECK_SHEET_ROWS);
                generatedTextures.add(deckTexture);
            }
        } catch (Exception e) {
            Gdx.app.error("ShopScreen", "Failed to load deck texture: " + e.getMessage());
        }
    }

    private void loadCheatTextures() {
        for (CheatCard card : CheatCard.values()) {
            try {
                FileHandle file = Gdx.files.internal(card.getImagePath());
                if (!file.exists()) continue;
                Texture tex = new Texture(file, true);
                tex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
                generatedTextures.add(tex);
                int w = Math.min(CHEAT_CROP_W, tex.getWidth() - CHEAT_CROP_X);
                int h = Math.min(CHEAT_CROP_H, tex.getHeight() - CHEAT_CROP_Y);
                cheatRegions.put(card, w > 0 && h > 0
                    ? new TextureRegion(tex, CHEAT_CROP_X, CHEAT_CROP_Y, w, h)
                    : new TextureRegion(tex));
            } catch (Exception e) {
                Gdx.app.error("ShopScreen", "Failed to load cheat card " + card + ": " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- layout

    private void buildUI() {
        TrippyBackground bg = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        bg.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        bg.setTouchable(Touchable.disabled);
        stage.addActor(bg);

        hotCardBar = new HotCardBar(runState, uiSkin, s);
        hotCardBar.setOnCardSoldListener((soldCard, sellPrice) -> {
            if (isTransitioning) return;
            updateMoneyLabel();
            showMessage(soldCard.getDisplayName() + " sold for $" + sellPrice, GOLD);
            refreshCardsForSale();
            SaveManager.getInstance().saveRunMapState(runState);
        });

        priceBg = outlinedRect((int) (150 * s), (int) (48 * s), PRICE_BG, PRICE_BORDER, Math.max(2, (int) (3 * s)), (int) (10 * s));

        uiRoot = new Table();
        uiRoot.setFillParent(true);
        // alulra igazítva, hogy a jobb felső sarokban legyen hely a hot card sávnak
        uiRoot.bottom().padBottom(16 * s);

        Table header = new Table();
        header.add(label("SHOP", 7f, GOLD)).row();
        header.add(label("Improve your run!", 2.2f, Color.WHITE)).padTop(2 * s);
        uiRoot.add(header).colspan(2).padBottom(14 * s).row();

        uiRoot.add(buildItemTree()).size(LEFT_W * s, LEFT_H * s).padRight(24 * s);
        uiRoot.add(buildCardsPanel()).size(RIGHT_W * s, LEFT_H * s).row();

        infoLabel = label("Buy one of the bottom cheat cards - the pair above it moves down", 1.8f, INFO_COLOR);
        uiRoot.add(infoLabel).colspan(2).padTop(10 * s).row();

        Table bottom = new Table();
        moneyLabel = label("", 2.8f, GOLD);
        updateMoneyLabel();
        Label chapterLabel = label("Chapter " + runState.getChapterIndex() + "/" + runState.getMaxChapters(), 2.2f, new Color(0.6f, 0.6f, 0.6f, 1f));
        messageLabel = label("", 2.2f, GOLD);
        bottom.add(moneyLabel).left().padRight(30 * s);
        bottom.add(chapterLabel).left();
        bottom.add(messageLabel).expandX().center();
        bottom.add(buildLeaveButton()).right().size(240 * s, 72 * s);
        uiRoot.add(bottom).colspan(2).width((LEFT_W + RIGHT_W + 24) * s).padTop(8 * s);

        stage.addActor(uiRoot);
        uiRoot.getColor().a = 0f;
        uiRoot.addAction(Actions.fadeIn(0.5f, Interpolation.fade));

        hotCardBar.setPosition(Gdx.graphics.getWidth() - hotCardBar.getWidth() - 24 * s,
            Gdx.graphics.getHeight() - hotCardBar.getHeight() - 20 * s);
        stage.addActor(hotCardBar);
    }

    // ---------------------------------------------------------------- left: cheat tree

    private Group buildItemTree() {
        float w = LEFT_W * s, h = LEFT_H * s;
        Group panel = new Group();
        panel.setSize(w, h);
        Image bg = new Image(outlinedRect((int) w, (int) h, PANEL_BG, RED_BORDER, Math.max(2, (int) (5 * s)), (int) (26 * s)));
        bg.setSize(w, h);
        panel.addActor(bg);

        for (int i = 0; i < 4; i++) {
            float fromX = (topX(i) + ITEM_W / 2f) * s;
            float toX = PAIR_CENTER[i / 2] * s;
            panel.addActor(connector(fromX, TOP_Y * s, toX, (BOTTOM_Y + ITEM_H) * s));
        }

        treeGroup = new Group();
        treeGroup.setSize(w, h);
        panel.addActor(treeGroup);

        // A fa a runhoz tartozik: blindok között is ugyanaz marad, csak vásárláskor változik.
        CheatCard[] tree = runState.getShopCheatTree();
        if (tree == null) {
            tree = new CheatCard[RunState.SHOP_CHEAT_TREE_SIZE];
        }
        for (int k = 0; k < tree.length; k++) {
            if (tree[k] == null || !tree[k].isAvailable(runState, this)) {
                tree[k] = CheatCard.randomAvailable(runState, this);
            }
        }
        for (int i = 0; i < 4; i++) {
            topViews[i] = createItem(tree[i], topX(i), TOP_Y, false);
        }
        for (int j = 0; j < 2; j++) {
            bottomViews[j] = createItem(tree[4 + j], bottomX(j), BOTTOM_Y, true);
        }
        storeTree();
        return panel;
    }

    private static float topX(int i) {
        float center = PAIR_CENTER[i / 2];
        return i % 2 == 0 ? center - ITEM_W - 10 : center + 10;
    }

    private static float bottomX(int j) {
        return PAIR_CENTER[j] - ITEM_W / 2f;
    }

    private ItemView createItem(CheatCard card, float refX, float refY, boolean buyable) {
        Group actor = new Group();
        actor.setBounds(refX * s, refY * s, ITEM_W * s, ITEM_H * s);

        Table content = new Table();
        content.setSize(ITEM_W * s, ITEM_H * s);
        content.top();
        content.add(label(card.getDisplayName(), 1.7f, Color.WHITE)).padBottom(6 * s).row();
        TextureRegion region = cheatRegions.get(card);
        if (region != null) {
            content.add(new Image(region)).size(ITEM_W * s, ITEM_IMG_H * s).row();
        } else {
            Label fallback = label(card.getDescription(), 1.4f, INFO_COLOR);
            fallback.setWrap(true);
            content.add(fallback).size(ITEM_W * s, ITEM_IMG_H * s).row();
        }
        Label priceLabel = label("$" + price(card.getPrice()), 2.2f, GOLD);
        content.add(pricePill(priceLabel, 130)).padTop(8 * s);
        actor.addActor(content);

        ItemView view = new ItemView(card, actor, content);
        view.priceLabel = priceLabel;
        view.buyable = buyable;
        if (!buyable) actor.getColor().a = LOCKED_ALPHA;

        actor.setTouchable(Touchable.enabled);
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1 || isTransitioning) return;
                infoLabel.setText(card.getDisplayName() + " ($" + price(card.getPrice()) + "): " + card.getDescription()
                    + (view.buyable ? "" : "  -  not available yet"));
                if (view.buyable && !treeAnimating) {
                    content.clearActions();
                    content.addAction(Actions.moveTo(0, 10 * s, 0.15f, Interpolation.smooth));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1 || isTransitioning) return;
                infoLabel.setText("");
                content.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.smooth));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isTransitioning || treeAnimating) return;
                if (!view.buyable) {
                    showMessage("Only the bottom cards can be bought", ERROR_COLOR);
                    return;
                }
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                buyCheat(view);
            }
        });

        treeGroup.addActor(actor);
        return view;
    }

    private void buyCheat(ItemView view) {
        int slot = view == bottomViews[0] ? 0 : view == bottomViews[1] ? 1 : -1;
        if (slot < 0 || !tryPay(price(view.card.getPrice()))) return;

        int slotsBefore = runState.getShopCardSlots();
        String result = view.card.apply(runState, this);
        runState.incrementCheatCardsBought();
        updateMoneyLabel();
        showMessage(result, GOLD);
        if (view.card == CheatCard.LUCKY_SEVEN && !rerolledThisVisit) {
            freeRerolls++;
        }
        updateRerollLabel();
        refreshPrices();
        if (runState.getShopCardSlots() > slotsBefore) {
            HotCard extra = randomHotCard();
            if (extra != null) stock.add(extra);
            refreshCardsForSale();
        }
        advanceTree(slot);
        storeTree();
        SaveManager.getInstance().saveRunMapState(runState);
    }

    private void storeTree() {
        CheatCard[] tree = new CheatCard[RunState.SHOP_CHEAT_TREE_SIZE];
        for (int i = 0; i < 4; i++) tree[i] = topViews[i].card;
        for (int j = 0; j < 2; j++) tree[4 + j] = bottomViews[j].card;
        runState.setShopCheatTree(tree);
    }

    /** A megvett lap feletti pár lecsúszik, a másik ág és a régi alsó sor eltűnik, felülre 4 új lap jön. */
    private void advanceTree(int boughtSlot) {
        treeAnimating = true;
        float dur = 0.4f;

        for (ItemView old : bottomViews) {
            old.actor.setTouchable(Touchable.disabled);
            old.actor.addAction(Actions.sequence(Actions.fadeOut(0.25f), Actions.removeActor()));
        }
        int other = 1 - boughtSlot;
        for (int k = 0; k < 2; k++) {
            ItemView lost = topViews[other * 2 + k];
            lost.actor.setTouchable(Touchable.disabled);
            lost.actor.addAction(Actions.sequence(Actions.fadeOut(0.25f), Actions.removeActor()));
        }
        for (int k = 0; k < 2; k++) {
            ItemView moving = topViews[boughtSlot * 2 + k];
            moving.buyable = true;
            moving.actor.addAction(Actions.parallel(
                Actions.moveTo(bottomX(k) * s, BOTTOM_Y * s, dur, Interpolation.smooth),
                Actions.alpha(1f, dur)));
            bottomViews[k] = moving;
        }
        for (int i = 0; i < 4; i++) {
            ItemView fresh = createItem(CheatCard.randomAvailable(runState, this), topX(i), TOP_Y + 40, false);
            fresh.actor.getColor().a = 0f;
            fresh.actor.addAction(Actions.sequence(Actions.delay(dur * 0.5f), Actions.parallel(
                Actions.moveTo(topX(i) * s, TOP_Y * s, dur, Interpolation.smooth),
                Actions.alpha(LOCKED_ALPHA, dur))));
            topViews[i] = fresh;
        }
        stage.addAction(Actions.sequence(Actions.delay(dur * 1.5f), Actions.run(() -> treeAnimating = false)));
    }

    private Actor connector(float x1, float y1, float x2, float y2) {
        float thickness = Math.max(2f, 5 * s);
        float length = (float) Math.hypot(x2 - x1, y2 - y1);
        Image line = new Image(whiteTintableBg);
        line.setColor(RED_BORDER);
        line.setSize(length, thickness);
        line.setOrigin(0, thickness / 2f);
        line.setPosition(x1, y1 - thickness / 2f);
        line.setRotation(MathUtils.atan2(y2 - y1, x2 - x1) * MathUtils.radiansToDegrees);
        line.setTouchable(Touchable.disabled);
        return line;
    }

    // ---------------------------------------------------------------- right: random cards

    private Group buildCardsPanel() {
        float w = RIGHT_W * s, h = LEFT_H * s;
        Group panel = new Group();
        panel.setSize(w, h);
        Image bg = new Image(outlinedRect((int) w, (int) h, RIGHT_BG, RED_BORDER, Math.max(2, (int) (5 * s)), (int) (26 * s)));
        bg.setSize(w, h);
        panel.addActor(bg);

        Label title = label("HOT CARDS", 2.6f, TEAL);
        title.pack();
        title.setPosition(36 * s, h - 80 * s);
        panel.addActor(title);

        Label hint = label("New cards every visit!", 1.5f, TEAL);
        hint.pack();
        hint.setPosition(w - 36 * s - hint.getWidth(), h - 74 * s);
        panel.addActor(hint);

        Image divider = new Image(whiteTintableBg);
        divider.setColor(TEAL.r, TEAL.g, TEAL.b, 0.5f);
        divider.setBounds(30 * s, h - 96 * s, w - 60 * s, Math.max(1f, 3 * s));
        panel.addActor(divider);

        cardsForSaleGroup = new Group();
        cardsForSaleGroup.setSize(w, h);
        panel.addActor(cardsForSaleGroup);
        refreshCardsForSale();

        panel.addActor(buildRerollButton(w));
        return panel;
    }

    private void rollStock() {
        stock.clear();
        for (int i = 0; i < runState.getShopCardSlots(); i++) {
            HotCard card = randomHotCard();
            if (card != null) stock.add(card);
        }
    }

    /** Véletlen hot card, ami nincs a játékosnál és nincs már a kínálatban. */
    private HotCard randomHotCard() {
        Set<String> excluded = new HashSet<>(runState.getHotCards());
        for (HotCard c : stock) {
            if (c != null) excluded.add(c.getId());
        }
        return HotCard.randomExcluding(excluded);
    }

    private void refreshCardsForSale() {
        cardsForSaleGroup.clearChildren();
        int n = stock.size();
        if (n == 0) return;
        float panelW = cardsForSaleGroup.getWidth();
        float gap = 16 * s;
        float cardW = Math.min(150 * s, (panelW - 40 * s - (n - 1) * gap) / n);
        float cardH = cardW * 1.41f;
        float pillH = 48 * s;
        float slotH = cardH + 18 * s + pillH;
        float startX = (panelW - (n * cardW + (n - 1) * gap)) / 2f;
        float slotY = 200 * s;

        for (int i = 0; i < n; i++) {
            final HotCard card = stock.get(i);
            final int index = i;
            Group slot = new Group();
            slot.setBounds(startX + i * (cardW + gap), slotY, cardW, slotH);
            cardsForSaleGroup.addActor(slot);

            if (card == null) {
                Label sold = label("SOLD", 2.6f, GOLD);
                sold.setSize(cardW, slotH);
                slot.addActor(sold);
                continue;
            }

            final int price = price(card.getPrice());
            Table content = new Table();
            content.setSize(cardW, slotH);
            content.top();
            TextureRegion region = hotCardBar.getRegion(card);
            if (region != null) {
                content.add(new Image(region)).size(cardW, cardH).row();
            } else {
                Label name = label(card.getDisplayName(), 1.6f, Color.WHITE);
                name.setWrap(true);
                content.add(name).size(cardW, cardH).row();
            }
            content.add(pricePill(label("$" + price, 2.2f, GOLD), Math.min(130, cardW / s))).padTop(18 * s);
            slot.addActor(content);

            slot.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) infoLabel.setText(card.getDisplayName() + " ($" + price + "): " + card.getDescription());
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) infoLabel.setText("");
                }
            });
            addHoverAndClick(slot, content, () -> {
                if (!runState.hasFreeHotCardSlot()) {
                    showMessage("Hot card slots are full (" + RunState.MAX_HOT_CARDS + "/" + RunState.MAX_HOT_CARDS + ") - sell a card to make room!", ERROR_COLOR);
                    return;
                }
                if (!tryPay(price)) return;
                runState.addHotCard(card.getId(), price);
                stock.set(index, null);
                SaveManager.getInstance().saveRunMapState(runState);
                showMessage(card.getDisplayName() + " added to your Hot cards", GOLD);
                hotCardBar.refresh();
                refreshCardsForSale();
            });
        }
    }

    private Actor buildRerollButton(float panelW) {
        Table btn = new Table();
        btn.setBackground(outlinedRect((int) (260 * s), (int) (64 * s), PRICE_BG, TEAL, Math.max(2, (int) (3 * s)), (int) (14 * s)));
        rerollLabel = label("", 2.2f, Color.WHITE);
        updateRerollLabel();
        btn.add(rerollLabel);
        btn.setSize(260 * s, 64 * s);
        btn.setPosition((panelW - 260 * s) / 2f, 50 * s);

        Group wrapper = new Group();
        wrapper.setBounds(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight());
        btn.setPosition(0, 0);
        wrapper.addActor(btn);
        addHoverAndClick(wrapper, btn, this::reroll);
        return wrapper;
    }

    private void reroll() {
        int price = currentRerollPrice();
        if (!tryPay(price)) return;
        if (freeRerolls > 0) freeRerolls--;
        rerolledThisVisit = true;
        rollStock();
        refreshCardsForSale();
        updateRerollLabel();
        SaveManager.getInstance().saveRunMapState(runState);
    }

    private void updateRerollLabel() {
        int price = currentRerollPrice();
        rerollLabel.setText(price == 0 ? "Reroll  FREE" : "Reroll  $" + price);
    }

    private int currentRerollPrice() {
        return freeRerolls > 0 ? 0 : price(runState.getRerollPrice());
    }

    /** Alapár, a Costume Jewelry kedvezménnyel (25%, kerekítve). */
    private int price(int basePrice) {
        return discountActive ? Math.round(basePrice * 0.75f) : basePrice;
    }

    private void refreshPrices() {
        for (ItemView v : topViews) {
            if (v != null) v.priceLabel.setText("$" + price(v.card.getPrice()));
        }
        for (ItemView v : bottomViews) {
            if (v != null) v.priceLabel.setText("$" + price(v.card.getPrice()));
        }
        refreshCardsForSale();
        updateRerollLabel();
    }

    // ---------------------------------------------------------------- CheatCard.ShopContext

    @Override
    public void addFreeReroll() {
        freeRerolls++;
    }

    @Override
    public void activateDiscount() {
        discountActive = true;
    }

    @Override
    public boolean isDiscountActive() {
        return discountActive;
    }

    @Override
    public void onHotCardsChanged() {
        hotCardBar.refresh();
    }

    // ---------------------------------------------------------------- helpers

    private boolean tryPay(int price) {
        if (!runState.spendMoney(price)) {
            showMessage("Not enough money!", ERROR_COLOR);
            return false;
        }
        updateMoneyLabel();
        return true;
    }

    private void updateMoneyLabel() {
        moneyLabel.setText("$ " + runState.getMoney());
    }

    private void showMessage(String text, Color color) {
        messageLabel.clearActions();
        messageLabel.setText(text);
        messageLabel.setColor(color);
        messageLabel.addAction(Actions.sequence(Actions.delay(2.2f), Actions.fadeOut(0.4f)));
    }

    private Table pricePill(Label priceLabel, float refWidth) {
        Table pill = new Table();
        pill.setBackground(priceBg);
        pill.add(priceLabel);
        Table holder = new Table();
        holder.add(pill).size(refWidth * s, 48 * s);
        return holder;
    }

    private void addHoverAndClick(Group wrapper, Actor lifted, Runnable onClick) {
        wrapper.setTouchable(Touchable.enabled);
        wrapper.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    lifted.clearActions();
                    lifted.addAction(Actions.moveTo(0, 8 * s, 0.15f, Interpolation.smooth));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    lifted.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.smooth));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isTransitioning) return;
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                onClick.run();
            }
        });
    }

    private Actor buildLeaveButton() {
        Table btn = new Table();
        btn.setBackground(outlinedRect((int) (240 * s), (int) (72 * s), new Color(0.55f, 0.12f, 0.12f, 1f),
            new Color(0.85f, 0.3f, 0.3f, 1f), Math.max(2, (int) (4 * s)), (int) (16 * s)));
        btn.add(label("Leave", 2.6f, Color.WHITE));
        btn.setTransform(true);
        btn.setTouchable(Touchable.enabled);
        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    btn.setOrigin(Align.center);
                    btn.clearActions();
                    btn.addAction(Actions.scaleTo(1.06f, 1.06f, 0.15f, Interpolation.fade));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    btn.clearActions();
                    btn.addAction(Actions.scaleTo(1f, 1f, 0.15f, Interpolation.fade));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                leaveShop();
            }
        });
        return btn;
    }

    private void leaveShop() {
        if (isTransitioning) return;
        isTransitioning = true;
        uiRoot.setTouchable(Touchable.disabled);
        if (hotCardBar != null) hotCardBar.setTouchable(Touchable.disabled);
        CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);
        uiRoot.addAction(Actions.parallel(
            Actions.moveBy(0, -80f, 0.6f, Interpolation.pow2In),
            Actions.fadeOut(0.6f, Interpolation.fade)
        ));
        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.run(() -> main.switchScreen(new RunMapScreen(main, runState, true)))
        ));
    }

    private Label label(String text, float fontScale, Color color) {
        Label l = new Label(text, uiSkin);
        l.setFontScale(fontScale * s);
        l.setColor(color);
        l.setAlignment(Align.center);
        return l;
    }

    private TextureRegionDrawable outlinedRect(int width, int height, Color bgColor, Color borderColor, int border, int radius) {
        width = Math.max(width, 2 * radius + 2);
        height = Math.max(height, 2 * radius + 2);
        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);
        fillRounded(p, 0, 0, width, height, radius, borderColor);
        fillRounded(p, border, border, width - 2 * border, height - 2 * border, Math.max(0, radius - border), bgColor);
        Texture tex = new Texture(p);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        generatedTextures.add(tex);
        return new TextureRegionDrawable(tex);
    }

    private static void fillRounded(Pixmap p, int x, int y, int w, int h, int r, Color c) {
        p.setColor(c);
        p.fillRectangle(x + r, y, w - 2 * r, h);
        p.fillRectangle(x, y + r, w, h - 2 * r);
        p.fillCircle(x + r, y + r, r);
        p.fillCircle(x + w - r - 1, y + r, r);
        p.fillCircle(x + r, y + h - r - 1, r);
        p.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    // ---------------------------------------------------------------- screen

    @Override
    protected void onEscapePressed() {
        leaveShop();
    }

    @Override
    public void render(float delta) {
        if (!isTransitioning && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onEscapePressed();
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (hotCardBar != null) hotCardBar.dispose();
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
    }
}
