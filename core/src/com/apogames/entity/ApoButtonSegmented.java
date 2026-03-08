package com.apogames.entity;

import com.apogames.aistories.Constants;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import java.util.ArrayList;
import java.util.List;

public class ApoButtonSegmented extends ApoButton {

    private static final float[] COLOR_HOVER = new float[]{220f / 255f, 220f / 255f, 220f / 255f, 1f};

    private final List<String> labels = new ArrayList<>();
    private final List<Boolean> visible = new ArrayList<>();
    private int selectedIndex = 0;
    private int hoveredIndex = -1;

    private int[] labelX;
    private int[] labelW;
    private int bgX, bgW;

    private static final int LABEL_PAD = 14;
    private static final int SEP_PAD = 8;
    private static final int BG_PAD = 12;
    private static final String SEPARATOR = "|";

    private int sepW;
    private boolean layoutDirty = true;

    public ApoButtonSegmented(int x, int y, String function) {
        super(x, y, 0, 0, function, "");
    }

    public void addOption(String label) {
        labels.add(label);
        visible.add(true);
        layoutDirty = true;
    }

    public void setOptionVisible(int index, boolean vis) {
        if (index >= 0 && index < visible.size()) {
            visible.set(index, vis);
            layoutDirty = true;
            if (!vis && selectedIndex == index) {
                selectedIndex = 0;
            }
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < labels.size() && visible.get(index)) {
            this.selectedIndex = index;
        }
    }

    public void updateHover(int mouseX, int mouseY) {
        if (!isVisible() || labelX == null) {
            hoveredIndex = -1;
            return;
        }
        if (layoutDirty) recalcLayout();

        hoveredIndex = -1;
        if (mouseY < getY() || mouseY >= getY() + getHeight()) return;

        for (int i = 0; i < labels.size(); i++) {
            if (!visible.get(i)) continue;
            int lx = labelX[i];
            int lw = labelW[i] + LABEL_PAD * 2;
            if (mouseX >= lx && mouseX < lx + lw) {
                hoveredIndex = i;
                return;
            }
        }
    }

    private void recalcLayout() {
        layoutDirty = false;
        BitmapFont font = AssetLoader.font20;

        labelX = new int[labels.size()];
        labelW = new int[labels.size()];

        Constants.glyphLayout.setText(font, SEPARATOR);
        sepW = (int) Constants.glyphLayout.width;

        int totalW = 0;
        int visCount = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (!visible.get(i)) { labelW[i] = 0; continue; }
            Constants.glyphLayout.setText(font, labels.get(i));
            labelW[i] = (int) Constants.glyphLayout.width;
            totalW += labelW[i] + LABEL_PAD * 2;
            visCount++;
        }
        if (visCount > 1) {
            totalW += (visCount - 1) * (SEP_PAD * 2 + sepW);
        }

        bgW = totalW + BG_PAD * 2;
        bgX = (int) getX();

        setWidth(bgW);
        setHeight(44);

        int cx = bgX + BG_PAD;
        boolean first = true;
        for (int i = 0; i < labels.size(); i++) {
            if (!visible.get(i)) continue;
            if (!first) {
                cx += SEP_PAD * 2 + sepW;
            }
            labelX[i] = cx;
            cx += labelW[i] + LABEL_PAD * 2;
            first = false;
        }
    }

    public void ensureLayout() {
        if (layoutDirty) recalcLayout();
    }

    public void invalidateLayout() {
        layoutDirty = true;
    }

    public boolean handleClick(int mouseX, int mouseY) {
        if (!isVisible()) return false;
        if (layoutDirty) recalcLayout();

        if (mouseY < getY() || mouseY >= getY() + getHeight()) return false;

        for (int i = 0; i < labels.size(); i++) {
            if (!visible.get(i)) continue;
            int lx = labelX[i];
            int lw = labelW[i] + LABEL_PAD * 2;
            if (mouseX >= lx && mouseX < lx + lw) {
                selectedIndex = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GameScreen screen, int changeX, int changeY) {
        if (!isVisible()) return;
        if (layoutDirty) recalcLayout();

        float bx = bgX + changeX;
        float by = getY() + changeY;
        BitmapFont font = AssetLoader.font20;

        // Dark transparent background
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeType.Filled);
        screen.getRenderer().setColor(0f, 0f, 0f, 0.45f);
        screen.getRenderer().roundedRect(bx, by, bgW, getHeight(), 10);
        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Labels and separators
        screen.spriteBatch.begin();
        float textY = by + 8;
        boolean first = true;
        for (int i = 0; i < labels.size(); i++) {
            if (!visible.get(i)) continue;

            if (!first) {
                float sepX = labelX[i] + changeX - SEP_PAD - sepW / 2f;
                screen.drawString(SEPARATOR, sepX, textY + 6,
                        Constants.COLOR_GREY, font, DrawString.MIDDLE, false, false);
            }

            boolean selected = (i == selectedIndex);
            boolean hovered = (i == hoveredIndex && !selected);
            float[] color = selected ? Constants.COLOR_WHITE : (hovered ? COLOR_HOVER : Constants.COLOR_GREY);
            float cx = labelX[i] + changeX + (labelW[i] + LABEL_PAD * 2) / 2f;
            screen.drawString(labels.get(i), cx, textY + 6, color, font, DrawString.MIDDLE, false, false);

            first = false;
        }
        screen.spriteBatch.end();
    }
}
