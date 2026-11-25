package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.HotCard;
import io.github.some_rougelike.logic.RunState;

/**
 * A játékosnál lévő hot cardok 5 helyes sávja. Blind, bolt és run map közben is látszik;
 * egérrel egy lap fölé állva kiírja a nevét és a hatását.
 */
public class HotCardBar extends Group implements Disposable {
    /** A hot card képekről levágott átlátszó keret (1520x1640-es képek). */
    private static final int CROP_X = 275, CROP_Y = 112, CROP_W = 985, CROP_H = 1390;

    private static final float SLOT_W = 100, SLOT_H = 141, GAP = 12, PAD = 14, TITLE_H = 34;
    private static final Color BG = new Color(0.08f, 0.06f, 0.06f, 0.85f);
    private static final Color BORDER = new Color(0.85f, 0.45f, 0.2f, 1f);
    private static final Color EMPTY_BG = new Color(0.15f, 0.12f, 0.12f, 0.6f);
    private static final Color EMPTY_BORDER = new Color(0.45f, 0.3f, 0.25f, 1f);
    private static final Color TITLE = new Color(1f, 0.6f, 0.3f, 1f);
    private static final Color GOLD = new Color(1f, 0.78f, 0.35f, 1f);
    private static final Color SELL_NORMAL_BG = new Color(0.55f, 0.12f, 0.12f, 0.92f);
    private static final Color SELL_NORMAL_BORDER = new Color(0.85f, 0.3f, 0.3f, 1f);
    private static final Color SELL_HOVER_BG = new Color(0.72f, 0.16f, 0.16f, 1f);
    private static final Color SELL_HOVER_BORDER = new Color(1f, 0.55f, 0.45f, 1f);

    private final RunState runState;
    private final Skin skin;
    private final float s;
    private final Map<String, TextureRegion> regions = new HashMap<>();
    private final Array<Texture> textures = new Array<>();
    private final TextureRegionDrawable bgDrawable;
    private final TextureRegionDrawable emptyDrawable;
    private final TextureRegionDrawable sellBtnNormal;
    private final TextureRegionDrawable sellBtnHover;
    private final Label tooltip;

    private boolean sellable = false;
    private OnCardSoldListener sellListener;

    public interface OnCardSoldListener {
        void onCardSold(HotCard card, int sellPrice);
    }

    /** @param scale referencia-skála (képernyő / 1920x1080) */
    public HotCardBar(RunState runState, Skin skin, float scale) {
        this(runState, skin, scale, false);
    }

    public HotCardBar(RunState runState, Skin skin, float scale, boolean sellable) {
        this.runState = runState;
        this.skin = skin;
        this.s = scale;
        this.sellable = sellable;

        float w = (PAD * 2 + RunState.MAX_HOT_CARDS * SLOT_W + (RunState.MAX_HOT_CARDS - 1) * GAP) * s;
        float h = (PAD * 2 + TITLE_H + SLOT_H) * s;
        setSize(w, h);
        bgDrawable = roundedRect((int) w, (int) h, BG, BORDER, Math.max(2, (int) (3 * s)), (int) (14 * s));
        emptyDrawable = roundedRect((int) (SLOT_W * s), (int) (SLOT_H * s), EMPTY_BG, EMPTY_BORDER, Math.max(1, (int) (2 * s)), (int) (8 * s));

        int btnW = Math.max(10, (int) (88 * s));
        int btnH = Math.max(10, (int) (28 * s));
        int border = Math.max(1, (int) (2 * s));
        int radius = Math.max(2, (int) (6 * s));
        sellBtnNormal = roundedRect(btnW, btnH, SELL_NORMAL_BG, SELL_NORMAL_BORDER, border, radius);
        sellBtnHover = roundedRect(btnW, btnH, SELL_HOVER_BG, SELL_HOVER_BORDER, border, radius);

        tooltip = new Label("", skin);
        tooltip.setFontScale(1.7f * s);
        tooltip.setColor(Color.WHITE);
        tooltip.setAlignment(Align.right);
        tooltip.setTouchable(Touchable.disabled);

        setTouchable(Touchable.childrenOnly);
        refresh();
    }

    /** 
     * @param sellable
     */
    public void setSellable(boolean sellable) {
        this.sellable = sellable;
        refresh();
    }

    /** 
     * @return boolean
     */
    public boolean isSellable() {
        return sellable;
    }

    /** 
     * @param listener
     */
    public void setOnCardSoldListener(OnCardSoldListener listener) {
        this.sellListener = listener;
        this.sellable = true;
        refresh();
    }

    /** Újrarajzolja a sávot (pl. vásárlás után). */
    public void refresh() {
        clearChildren();
        Image bg = new Image(bgDrawable);
        bg.setSize(getWidth(), getHeight());
        bg.setTouchable(Touchable.disabled);
        addActor(bg);

        List<String> owned = runState.getHotCards();
        Label title = new Label("HOT CARDS  " + owned.size() + "/" + RunState.MAX_HOT_CARDS, skin);
        title.setFontScale(1.9f * s);
        title.setColor(TITLE);
        title.pack();
        title.setPosition(PAD * s, getHeight() - PAD * s - title.getHeight());
        title.setTouchable(Touchable.disabled);
        addActor(title);

        for (int i = 0; i < RunState.MAX_HOT_CARDS; i++) {
            float x = (PAD + i * (SLOT_W + GAP)) * s;
            float y = PAD * s;
            HotCard card = i < owned.size() ? HotCard.byId(owned.get(i)) : null;
            TextureRegion region = card != null ? regionFor(card) : null;

            if (card == null || region == null) {
                Image slot = new Image(emptyDrawable);
                slot.setBounds(x, y, SLOT_W * s, SLOT_H * s);
                slot.setTouchable(Touchable.disabled);
                addActor(slot);
                continue;
            }

            final HotCard currentCard = card;
            final int sellPrice = runState.getHotCardSellPrice(currentCard.getId());

            final Group slotGroup = new Group();
            slotGroup.setBounds(x, y, SLOT_W * s, SLOT_H * s);

            final Group cardWrapper = new Group();
            cardWrapper.setBounds(0, 0, SLOT_W * s, SLOT_H * s);
            Image cardImg = new Image(region);
            cardImg.setSize(SLOT_W * s, SLOT_H * s);
            cardWrapper.addActor(cardImg);

            if (sellable) {
                final Table sellBtn = new Table();
                sellBtn.setBackground(sellBtnNormal);
                final Label sellLabel = new Label("SELL $" + sellPrice, skin);
                sellLabel.setFontScale(1.35f * s);
                sellLabel.setColor(GOLD);
                sellLabel.setAlignment(Align.center);
                sellBtn.add(sellLabel).center();

                float btnW = Math.min(88 * s, (SLOT_W - 12) * s);
                float btnH = 28 * s;
                sellBtn.setSize(btnW, btnH);
                sellBtn.setPosition((SLOT_W * s - btnW) / 2f, 6 * s);
                sellBtn.setTouchable(Touchable.enabled);

                sellBtn.addListener(new ClickListener() {
                    @Override
                    public void enter(InputEvent event, float ex, float ey, int pointer, Actor fromActor) {
                        if (pointer == -1) {
                            sellBtn.setBackground(sellBtnHover);
                            sellLabel.setColor(Color.WHITE);
                            tooltip.setText("Sell " + currentCard.getDisplayName() + " for $" + sellPrice + " (50% refund)");
                            tooltip.pack();
                            tooltip.setPosition(getWidth() - tooltip.getWidth(), -tooltip.getHeight() - 6 * s);
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float ex, float ey, int pointer, Actor toActor) {
                        if (pointer == -1) {
                            sellBtn.setBackground(sellBtnNormal);
                            sellLabel.setColor(GOLD);
                            showTooltip(currentCard);
                        }
                    }

                    @Override
                    public void clicked(InputEvent event, float ex, float ey) {
                        event.stop();
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                        sellCard(currentCard);
                    }
                });

                cardWrapper.addActor(sellBtn);
            }

            slotGroup.addActor(cardWrapper);
            slotGroup.setTouchable(Touchable.enabled);

            slotGroup.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float ex, float ey, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        cardWrapper.clearActions();
                        cardWrapper.addAction(Actions.moveTo(0, 6 * s, 0.12f, Interpolation.smooth));
                        showTooltip(currentCard);
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                    }
                }

                @Override
                public void exit(InputEvent event, float ex, float ey, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        if (toActor != null && slotGroup.isAscendantOf(toActor)) return;
                        cardWrapper.clearActions();
                        cardWrapper.addAction(Actions.moveTo(0, 0, 0.12f, Interpolation.smooth));
                        tooltip.setText("");
                    }
                }
            });

            addActor(slotGroup);
        }

        tooltip.setText("");
        addActor(tooltip);
    }

    /** 
     * @param card
     */
    private void showTooltip(HotCard card) {
        String text = card.getDisplayName();
        if (sellable) {
            int sellPrice = runState.getHotCardSellPrice(card.getId());
            text += " (Sell: $" + sellPrice + ")";
        }
        text += ": " + card.getDescription();
        tooltip.setText(text);
        tooltip.pack();
        tooltip.setPosition(getWidth() - tooltip.getWidth(), -tooltip.getHeight() - 6 * s);
    }

    /** 
     * @param card
     */
    private void sellCard(HotCard card) {
        if (card == null || !runState.hasHotCard(card.getId())) return;
        int gained = runState.sellHotCard(card.getId());
        tooltip.setText("");
        refresh();
        if (sellListener != null) {
            sellListener.onCardSold(card, gained);
        }
    }

    /** 
     * @param card
     * @return TextureRegion
     */
    private TextureRegion regionFor(HotCard card) {
        TextureRegion cached = regions.get(card.getId());
        if (cached != null) return cached;
        try {
            FileHandle file = Gdx.files.internal(card.getImagePath());
            if (!file.exists()) return null;
            Texture tex = new Texture(file, true);
            tex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            textures.add(tex);
            int w = Math.min(CROP_W, tex.getWidth() - CROP_X);
            int h = Math.min(CROP_H, tex.getHeight() - CROP_Y);
            TextureRegion region = w > 0 && h > 0 ? new TextureRegion(tex, CROP_X, CROP_Y, w, h) : new TextureRegion(tex);
            regions.put(card.getId(), region);
            return region;
        } catch (Exception e) {
            Gdx.app.error("HotCardBar", "Failed to load " + card.getImagePath() + ": " + e.getMessage());
            return null;
        }
    }

    /** Ugyanazt a vágott képet adja a bolt számára is, hogy ne kelljen kétszer betölteni. */
    public TextureRegion getRegion(HotCard card) {
        return regionFor(card);
    }

    /** 
     * @param width
     * @param height
     * @param fill
     * @param border
     * @param thickness
     * @param radius
     * @return TextureRegionDrawable
     */
    private TextureRegionDrawable roundedRect(int width, int height, Color fill, Color border, int thickness, int radius) {
        width = Math.max(width, 2 * radius + 2);
        height = Math.max(height, 2 * radius + 2);
        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);
        fill(p, 0, 0, width, height, radius, border);
        fill(p, thickness, thickness, width - 2 * thickness, height - 2 * thickness, Math.max(0, radius - thickness), fill);
        Texture tex = new Texture(p);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        textures.add(tex);
        return new TextureRegionDrawable(tex);
    }

    /** 
     * @param p
     * @param x
     * @param y
     * @param w
     * @param h
     * @param r
     * @param c
     */
    private static void fill(Pixmap p, int x, int y, int w, int h, int r, Color c) {
        p.setColor(c);
        p.fillRectangle(x + r, y, w - 2 * r, h);
        p.fillRectangle(x, y + r, w, h - 2 * r);
        p.fillCircle(x + r, y + r, r);
        p.fillCircle(x + w - r - 1, y + r, r);
        p.fillCircle(x + r, y + h - r - 1, r);
        p.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
        regions.clear();
    }
}
