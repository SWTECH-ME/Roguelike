package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final RunState runState;
    private final Skin skin;
    private final float s;
    private final Map<String, TextureRegion> regions = new HashMap<>();
    private final Array<Texture> textures = new Array<>();
    private final TextureRegionDrawable bgDrawable;
    private final TextureRegionDrawable emptyDrawable;
    private final Label tooltip;

    /** @param scale referencia-skála (képernyő / 1920x1080) */
    public HotCardBar(RunState runState, Skin skin, float scale) {
        this.runState = runState;
        this.skin = skin;
        this.s = scale;

        float w = (PAD * 2 + RunState.MAX_HOT_CARDS * SLOT_W + (RunState.MAX_HOT_CARDS - 1) * GAP) * s;
        float h = (PAD * 2 + TITLE_H + SLOT_H) * s;
        setSize(w, h);
        bgDrawable = roundedRect((int) w, (int) h, BG, BORDER, Math.max(2, (int) (3 * s)), (int) (14 * s));
        emptyDrawable = roundedRect((int) (SLOT_W * s), (int) (SLOT_H * s), EMPTY_BG, EMPTY_BORDER, Math.max(1, (int) (2 * s)), (int) (8 * s));

        tooltip = new Label("", skin);
        tooltip.setFontScale(1.7f * s);
        tooltip.setColor(Color.WHITE);
        tooltip.setAlignment(Align.right);
        tooltip.setTouchable(Touchable.disabled);

        setTouchable(Touchable.childrenOnly);
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

            Image slot = region != null ? new Image(region) : new Image(emptyDrawable);
            slot.setBounds(x, y, SLOT_W * s, SLOT_H * s);
            if (card != null) {
                final HotCard hovered = card;
                slot.setTouchable(Touchable.enabled);
                slot.addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (pointer == -1) showTooltip(hovered);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (pointer == -1) tooltip.setText("");
                    }
                });
            } else {
                slot.setTouchable(Touchable.disabled);
            }
            addActor(slot);
        }

        tooltip.setText("");
        addActor(tooltip);
    }

    private void showTooltip(HotCard card) {
        tooltip.setText(card.getDisplayName() + ": " + card.getDescription());
        tooltip.pack();
        tooltip.setPosition(getWidth() - tooltip.getWidth(), -tooltip.getHeight() - 6 * s);
    }

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
