/*
 * Copyright (c) 2015 Quadbits SLU
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quadbits.gdxhelper.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class SkyActor extends ControllableActor implements Disposable, Recyclable<SkyActor> {
    protected Pool<SkyActor> skyActorPool;

    protected Sprite vgradient;

    protected Array<DayPalette> palettes;
    protected float paletteAlpha;
    protected int primaryPaletteIndex;
    protected int secondaryPaletteIndex;

    protected Color colorTop;
    protected Color colorMiddle;
    protected Color colorBottom;
    protected Color dstColor;
    protected Color darkColor1, darkColor2;
    protected Color lightColor1, lightColor2;
    private Color tmpColor;

    protected float vGradientOffset;

    protected float fadeAnimDurationSeconds;
    protected float fadeAnimMaxDeltaSeconds;
    protected boolean crossFading;
    protected float targetPaletteAlpha;

    protected ShaderProgram shaderProgram;

    @Inject
    protected TimeManager timeManager;

    @Inject
    protected RandomXS128 random;

    public static final float DEFAULT_FADE_ANIM_DURATION_SECONDS = 1f;
    public static final float DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS = 1.f / 30.f;

    public static final String SHADER_ATTR_NAME_COLOR_TOP = "a_colorTop";
    public static final String SHADER_ATTR_NAME_COLOR_BOTTOM = "a_colorBottom";

    public static final String VERTEX_SHADER = "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "attribute vec4 " + SHADER_ATTR_NAME_COLOR_TOP + ";\n" +
            "attribute vec4 " + SHADER_ATTR_NAME_COLOR_BOTTOM + ";\n" +
            "\n" +
            "uniform mat4 u_projTrans;\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_colorTop;\n" +
            "varying vec4 v_colorBottom;\n" +
            "\n" +
            "void main() {\n" +
            "    v_colorTop = a_colorTop;\n" +
            "    v_colorBottom = a_colorBottom;\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";

    public static final String FRAGMENT_SHADER = "#ifdef GL_ES\n" +
            "    precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_colorTop;\n" +
            "varying vec4 v_colorBottom;\n" +
            "\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform mat4 u_projTrans;\n" +
            "\n" +
            "void main() {\n" +
            "        float alpha = texture2D(u_texture, v_texCoords).a;\n" +
            "        vec4 color = (1.0 - alpha) * v_colorTop + alpha * v_colorBottom;\n" +
            "\n" +
            "        gl_FragColor = vec4(color.rgb, 1.0);\n" +
            "}";

    @Inject
    public SkyActor() {
        super();

        colorTop = new Color();
        colorMiddle = new Color();
        colorBottom = new Color();
        dstColor = new Color();
        darkColor1 = new Color();
        darkColor2 = new Color();
        lightColor1 = new Color();
        lightColor2 = new Color();
        tmpColor = new Color();

        palettes = new Array<DayPalette>();
        palettes.add(new DayPalette());

        shaderProgram = new ShaderProgram(getVertexShader(), getFragmentShader());
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("SkyActor", "[Shader program error] " + shaderProgram.getLog());
            Gdx.app.exit();
        }

        init();
    }

    private void init() {
        primaryPaletteIndex = 0;
        secondaryPaletteIndex = -1;
        paletteAlpha = 0;

        vGradientOffset = 0.5f;

        fadeAnimDurationSeconds = DEFAULT_FADE_ANIM_DURATION_SECONDS;
        fadeAnimMaxDeltaSeconds = DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS;
        crossFading = false;
        targetPaletteAlpha = 0;
    }

    @Override
    public void reset() {
        super.reset();
        init();
    }

    @Override
    public void free() {
        skyActorPool.free(this);
    }

    @Override
    public void setPool(Pool<SkyActor> skyActorPool) {
        this.skyActorPool = skyActorPool;
    }

    protected String getVertexShader() {
        return VERTEX_SHADER;
    }

    protected String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);

        Pixmap vgradientPixmap;

        // Read any previously existing pixmap from file
        String vgradientPixmapFilenameSize =
                String.format("vgradient_%d_%d.cim", (int) width, (int) height);
        FileHandle dataFile = Gdx.files.local(vgradientPixmapFilenameSize);

        if (dataFile.exists()) {
            vgradientPixmap = PixmapIO.readCIM(dataFile);
        } else {
            vgradientPixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);

            // Create an array of viewportWidth integers indicating the shift in alpha for a certain
            // horizontal pixel value
            float[] alphaShifts = new float[(int) width];
            float maxAlphaShift = 3f / 255f;
            for (int i = 0; i < alphaShifts.length; i++) {
                alphaShifts[i] = maxAlphaShift * (2 * random.nextFloat() - 1);
            }

            // Pixmaps' (0,0) is at the top-left corner
            float deltaAlpha = 1f / vgradientPixmap.getHeight();
            float alpha = 0;
            int startAlphaShiftIndex = 0;
            tmpColor.set(0xffffffff); // initialize to white
            for (int y = 0; y < vgradientPixmap.getHeight(); y++, alpha += deltaAlpha) {
                // select a (wrapped) starting point in the alphaShift array
                startAlphaShiftIndex += random.nextInt(alphaShifts.length / 2);
                startAlphaShiftIndex = startAlphaShiftIndex % alphaShifts.length;
                for (int x = 0; x < vgradientPixmap.getWidth(); x++) {
                    int alphaShiftIndex = startAlphaShiftIndex + x;
                    alphaShiftIndex = alphaShiftIndex % alphaShifts.length;
                    tmpColor.a = alpha + alphaShifts[alphaShiftIndex];
                    tmpColor.clamp();
                    vgradientPixmap.drawPixel(x, y, Color.rgba8888(tmpColor));
                }
            }

            // Write pixmap to file
            PixmapIO.writeCIM(dataFile, vgradientPixmap);
        }

        // Free any previously existing texture
        if (vgradient != null && vgradient.getTexture() != null) {
            vgradient.getTexture().dispose();
        }

        vgradient = new Sprite(new Texture(vgradientPixmap));
        vgradient.setPosition(0, 0);
        vgradient.setSize(width, height);

        // Dispose pixmap
        vgradientPixmap.dispose();
    }

    @Override
    public void dispose() {
        if (vgradient != null && vgradient.getTexture() != null) {
            vgradient.getTexture().dispose();
        }

        if (shaderProgram != null) {
            shaderProgram.dispose();
        }
    }

    public void setPaletteSize(int paletteSize) {
        if (paletteSize < 1) {
            throw new IndexOutOfBoundsException(
                    "palette size must be >= 1, provided size = " + paletteSize);
        }

        while (palettes.size < paletteSize) {
            palettes.add(new DayPalette());
        }
    }

    public int getPaletteSize() {
        return palettes.size;
    }

    @Override
    public long getMaxSleepTime() {
        if (crossFading) {
            return 0;
        }

        int maxDiffColor;

        DayPalette primaryPalette = palettes.get(primaryPaletteIndex);
        DayPalette secondaryPalette =
                (secondaryPaletteIndex < 0) ? null : palettes.get(secondaryPaletteIndex);

        switch (timeManager.getPeriod()) {
            case TWILIGHT_PRE_SUNRISE:
                darkColor1.set(primaryPalette.getPreDawnDarkColor());
                darkColor2.set(primaryPalette.getSunRiseDarkColor());
                lightColor1.set(primaryPalette.getPreDawnLightColor());
                lightColor2.set(primaryPalette.getSunRiseLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getPreDawnDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getSunRiseDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getPreDawnLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getSunRiseLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNRISE:
                darkColor1.set(primaryPalette.getSunRiseDarkColor());
                darkColor2.set(primaryPalette.getPostDawnDarkColor());
                lightColor1.set(primaryPalette.getSunRiseLightColor());
                lightColor2.set(primaryPalette.getPostDawnLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getSunRiseDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getPostDawnDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getSunRiseLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getPostDawnLightColor(), paletteAlpha);
                }
                break;
            case PRE_MIDDAY:
                darkColor1.set(primaryPalette.getPostDawnDarkColor());
                darkColor2.set(primaryPalette.getMiddayDarkColor());
                lightColor1.set(primaryPalette.getPostDawnLightColor());
                lightColor2.set(primaryPalette.getMiddayLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getPostDawnDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getMiddayDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getPostDawnLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getMiddayLightColor(), paletteAlpha);
                }
                break;
            case POST_MIDDAY:
                darkColor1.set(primaryPalette.getMiddayDarkColor());
                darkColor2.set(primaryPalette.getPreDuskDarkColor());
                lightColor1.set(primaryPalette.getMiddayLightColor());
                lightColor2.set(primaryPalette.getPreDuskLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getMiddayDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getPreDuskDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getMiddayLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getPreDuskLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_PRE_SUNSET:
                darkColor1.set(primaryPalette.getPreDuskDarkColor());
                darkColor2.set(primaryPalette.getSunSetDarkColor());
                lightColor1.set(primaryPalette.getPreDuskLightColor());
                lightColor2.set(primaryPalette.getSunSetLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getPreDuskDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getSunSetDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getPreDuskLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getSunSetLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNSET:
                darkColor1.set(primaryPalette.getSunSetDarkColor());
                darkColor2.set(primaryPalette.getPostDuskDarkColor());
                lightColor1.set(primaryPalette.getSunSetLightColor());
                lightColor2.set(primaryPalette.getPostDuskLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getSunSetDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getPostDuskDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getSunSetLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getPostDuskLightColor(), paletteAlpha);
                }
                break;
            case PRE_MIDNIGHT:
                darkColor1.set(primaryPalette.getPostDuskDarkColor());
                darkColor2.set(primaryPalette.getMidnightDarkColor());
                lightColor1.set(primaryPalette.getPostDuskLightColor());
                lightColor2.set(primaryPalette.getMidnightLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getPostDuskDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getMidnightDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getPostDuskLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getMidnightLightColor(), paletteAlpha);
                }
                break;
            default:
                darkColor1.set(primaryPalette.getMidnightDarkColor());
                darkColor2.set(primaryPalette.getPreDawnDarkColor());
                lightColor1.set(primaryPalette.getMidnightLightColor());
                lightColor2.set(primaryPalette.getPreDawnLightColor());
                if (secondaryPalette != null) {
                    darkColor1.lerp(secondaryPalette.getMidnightDarkColor(), paletteAlpha);
                    darkColor2.lerp(secondaryPalette.getPreDawnDarkColor(), paletteAlpha);
                    lightColor1.lerp(secondaryPalette.getMidnightLightColor(), paletteAlpha);
                    lightColor2.lerp(secondaryPalette.getPreDawnLightColor(), paletteAlpha);
                }
                break;
        }

        maxDiffColor = Math.max(maxDiffColor(darkColor1, darkColor2),
                maxDiffColor(lightColor1, lightColor2));
        long maxSleepTimeMillis = timeManager.getUnnormalizedDayPeriodWidth() / maxDiffColor;

        return Math.min(super.getMaxSleepTime(), maxSleepTimeMillis);
    }

    protected int maxDiffColor(Color color1, Color color2) {
        float rDiff = Math.abs(color1.r - color2.r);
        float gDiff = Math.abs(color1.g - color2.g);
        float bDiff = Math.abs(color1.b - color2.b);

        return MathUtils.ceil(Math.max(rDiff, Math.max(gDiff, bDiff)) * 255);
    }

    @Override
    public void act(float deltaSeconds) {
        super.act(deltaSeconds);

        if (targetPaletteAlpha == paletteAlpha) {
            return;
        }

        float deltaAlpha = deltaSeconds / fadeAnimDurationSeconds;
        paletteAlpha += (targetPaletteAlpha == 1) ? deltaAlpha : -deltaAlpha;

        if (paletteAlpha <= 0) {
            paletteAlpha = 0;
            crossFading = false;
        }

        if (paletteAlpha >= 1) {
            paletteAlpha = 1;
            crossFading = false;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        DayPalette primaryPalette = palettes.get(primaryPaletteIndex);
        DayPalette secondaryPalette =
                (secondaryPaletteIndex < 0) ? null : palettes.get(secondaryPaletteIndex);

        // ------------------------------------------------------------------------
        // Calculate bottom color
        // ------------------------------------------------------------------------
        switch (timeManager.getPeriod()) {
            case TWILIGHT_PRE_SUNRISE:
                colorBottom.set(primaryPalette.getPreDawnLightColor());
                dstColor.set(primaryPalette.getSunRiseLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getPreDawnLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getSunRiseLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNRISE:
                colorBottom.set(primaryPalette.getSunRiseLightColor());
                dstColor.set(primaryPalette.getPostDawnLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getSunRiseLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPostDawnLightColor(), paletteAlpha);
                }
                break;
            case PRE_MIDDAY:
                colorBottom.set(primaryPalette.getPostDawnLightColor());
                dstColor.set(primaryPalette.getMiddayLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getPostDawnLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getMiddayLightColor(), paletteAlpha);
                }
                break;
            case POST_MIDDAY:
                colorBottom.set(primaryPalette.getMiddayLightColor());
                dstColor.set(primaryPalette.getPreDuskLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getMiddayLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPreDuskLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_PRE_SUNSET:
                colorBottom.set(primaryPalette.getPreDuskLightColor());
                dstColor.set(primaryPalette.getSunSetLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getPreDuskLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getSunSetLightColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNSET:
                colorBottom.set(primaryPalette.getSunSetLightColor());
                dstColor.set(primaryPalette.getPostDuskLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getSunSetLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPostDuskLightColor(), paletteAlpha);
                }
                break;
            case PRE_MIDNIGHT:
                colorBottom.set(primaryPalette.getPostDuskLightColor());
                dstColor.set(primaryPalette.getMidnightLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getPostDuskLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getMidnightLightColor(), paletteAlpha);
                }
                break;
            default:
                colorBottom.set(primaryPalette.getMidnightLightColor());
                dstColor.set(primaryPalette.getPreDawnLightColor());
                if (secondaryPalette != null) {
                    colorBottom.lerp(secondaryPalette.getMidnightLightColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPreDawnLightColor(), paletteAlpha);
                }
                break;
        }
        colorBottom.lerp(dstColor, timeManager.getTPeriod());

        // ------------------------------------------------------------------------
        // Calculate top color
        // ------------------------------------------------------------------------
        switch (timeManager.getPeriod()) {
            case TWILIGHT_PRE_SUNRISE:
                colorTop.set(primaryPalette.getPreDawnDarkColor());
                dstColor.set(primaryPalette.getSunRiseDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getPreDawnDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getSunRiseDarkColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNRISE:
                colorTop.set(primaryPalette.getSunRiseDarkColor());
                dstColor.set(primaryPalette.getPostDawnDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getSunRiseDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPostDawnDarkColor(), paletteAlpha);
                }
                break;
            case PRE_MIDDAY:
                colorTop.set(primaryPalette.getPostDawnDarkColor());
                dstColor.set(primaryPalette.getMiddayDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getPostDawnDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getMiddayDarkColor(), paletteAlpha);
                }
                break;
            case POST_MIDDAY:
                colorTop.set(primaryPalette.getMiddayDarkColor());
                dstColor.set(primaryPalette.getPreDuskDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getMiddayDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPreDuskDarkColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_PRE_SUNSET:
                colorTop.set(primaryPalette.getPreDuskDarkColor());
                dstColor.set(primaryPalette.getSunSetDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getPreDuskDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getSunSetDarkColor(), paletteAlpha);
                }
                break;
            case TWILIGHT_POST_SUNSET:
                colorTop.set(primaryPalette.getSunSetDarkColor());
                dstColor.set(primaryPalette.getPostDuskDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getSunSetDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPostDuskDarkColor(), paletteAlpha);
                }
                break;
            case PRE_MIDNIGHT:
                colorTop.set(primaryPalette.getPostDuskDarkColor());
                dstColor.set(primaryPalette.getMidnightDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getPostDuskDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getMidnightDarkColor(), paletteAlpha);
                }
                break;
            default:
                colorTop.set(primaryPalette.getMidnightDarkColor());
                dstColor.set(primaryPalette.getPreDawnDarkColor());
                if (secondaryPalette != null) {
                    colorTop.lerp(secondaryPalette.getMidnightDarkColor(), paletteAlpha);
                    dstColor.lerp(secondaryPalette.getPreDawnDarkColor(), paletteAlpha);
                }
                break;
        }
        colorTop.lerp(dstColor, timeManager.getTPeriod());

        // For the moment, ignore offset
        // TODO: add gradient offset to the pixmap generation
        vgradient.setPosition(getX(), getY());
        vgradient.setSize(getWidth(), getHeight());

        // ------------------------------------------------------------------------
        // Use a custom shader for creating the gradient based on the
        // vgradient sprite
        // ------------------------------------------------------------------------
        shaderProgram.setAttributef(getShaderAttrNameColorTop(), colorTop.r, colorTop.g, colorTop.b,
                colorTop.a);
        shaderProgram.setAttributef(getShaderAttrNameColorBottom(), colorBottom.r, colorBottom.g,
                colorBottom.b, colorBottom.a);
        batch.setShader(shaderProgram);
        vgradient.draw(batch);

        // Restore default shader
        batch.setShader(null);
    }

    protected String getShaderAttrNameColorTop() {
        return SHADER_ATTR_NAME_COLOR_TOP;
    }

    protected String getShaderAttrNameColorBottom() {
        return SHADER_ATTR_NAME_COLOR_BOTTOM;
    }

    public float getVGradientOffset() {
        return vGradientOffset;
    }

    /**
     * The offset of the vertical gradient. When drawing a gradient, color values are interpolated
     * between the initial and end colors in a linear way. This offset controls the deviation of
     * the middle color respect to the bottom color, using values between 0 and 1. Therefore,
     * a value of 0.25f means that the middle color is drawn at 1/4 of the height,
     * considered from the bottom.
     *
     * @param vGradientOffset
     */
    public void setVGradientOffset(float vGradientOffset) {
        if (vGradientOffset < 0) {
            vGradientOffset = 0;
        }
        if (vGradientOffset > 1) {
            vGradientOffset = 1;
        }
        this.vGradientOffset = vGradientOffset;
    }

    public int getPrimaryPaletteIndex() {
        return primaryPaletteIndex;
    }

    public void setPrimaryPaletteIndex(int primaryPaletteIndex) {
        if (primaryPaletteIndex < 0 || primaryPaletteIndex >= palettes.size) {
            throw new IndexOutOfBoundsException("index value = " + primaryPaletteIndex +
                    ", size = " + palettes.size);
        }
        this.primaryPaletteIndex = primaryPaletteIndex;
    }

    public int getSecondaryPaletteIndex() {
        return secondaryPaletteIndex;
    }

    public void setSecondaryPaletteIndex(int secondaryPaletteIndex) {
        if (secondaryPaletteIndex < 0 || secondaryPaletteIndex >= palettes.size) {
            throw new IndexOutOfBoundsException("index value = " + secondaryPaletteIndex +
                    ", size = " + palettes.size);
        }
        this.secondaryPaletteIndex = secondaryPaletteIndex;
    }

    public float getPaletteAlpha() {
        return paletteAlpha;
    }

    public void activatePrimaryPalette() {
        if (paletteAlpha == 0) {
            return;
        }

        targetPaletteAlpha = 0;
        crossFading = true;
    }

    public void activateSecondaryPalette() {
        if (paletteAlpha == 1) {
            return;
        }

        targetPaletteAlpha = 1;
        crossFading = true;
    }

    public float getFadeAnimDurationSeconds() {
        return fadeAnimDurationSeconds;
    }

    public void setFadeAnimDurationSeconds(float fadeAnimDurationSeconds) {
        this.fadeAnimDurationSeconds = fadeAnimDurationSeconds;
    }

    public float getFadeAnimMaxDeltaSeconds() {
        return fadeAnimMaxDeltaSeconds;
    }

    public void setFadeAnimMaxDeltaSeconds(float fadeAnimMaxDeltaSeconds) {
        this.fadeAnimMaxDeltaSeconds = fadeAnimMaxDeltaSeconds;
    }

    // Shortcuts to primary palette accessors
    public Color getMidnightDarkColor() {
        return palettes.get(primaryPaletteIndex).getMidnightDarkColor();
    }

    public void setMidnightDarkColor(Color midnightDarkColor) {
        palettes.get(primaryPaletteIndex).setMidnightDarkColor(midnightDarkColor);
    }

    public Color getPreDuskDarkColor() {
        return palettes.get(primaryPaletteIndex).getPreDuskDarkColor();
    }

    public void setPreDuskDarkColor(Color preDuskDarkColor) {
        palettes.get(primaryPaletteIndex).setPreDuskDarkColor(preDuskDarkColor);
    }

    public Color getSunRiseDarkColor() {
        return palettes.get(primaryPaletteIndex).getSunRiseDarkColor();
    }

    public void setSunRiseDarkColor(Color sunRiseDarkColor) {
        palettes.get(primaryPaletteIndex).setSunRiseDarkColor(sunRiseDarkColor);
    }

    public Color getPostDuskDarkColor() {
        return palettes.get(primaryPaletteIndex).getPostDuskDarkColor();
    }

    public void setPostDuskDarkColor(Color postDuskDarkColor) {
        palettes.get(primaryPaletteIndex).setPostDuskDarkColor(postDuskDarkColor);
    }

    public Color getMiddayDarkColor() {
        return palettes.get(primaryPaletteIndex).getMiddayDarkColor();
    }

    public void setMiddayDarkColor(Color middayDarkColor) {
        palettes.get(primaryPaletteIndex).setMiddayDarkColor(middayDarkColor);
    }

    public Color getPreDawnDarkColor() {
        return palettes.get(primaryPaletteIndex).getPreDawnDarkColor();
    }

    public void setPreDawnDarkColor(Color preDawnDarkColor) {
        palettes.get(primaryPaletteIndex).setPreDawnDarkColor(preDawnDarkColor);
    }

    public Color getSunSetDarkColor() {
        return palettes.get(primaryPaletteIndex).getSunSetDarkColor();
    }

    public void setSunSetDarkColor(Color sunSetDarkColor) {
        palettes.get(primaryPaletteIndex).setSunSetDarkColor(sunSetDarkColor);
    }

    public Color getPostDawnDarkColor() {
        return palettes.get(primaryPaletteIndex).getPostDawnDarkColor();
    }

    public void setPostDawnDarkColor(Color postDawnDarkColor) {
        palettes.get(primaryPaletteIndex).setPostDawnDarkColor(postDawnDarkColor);
    }

    public Color getMidnightLightColor() {
        return palettes.get(primaryPaletteIndex).getMidnightLightColor();
    }

    public void setMidnightLightColor(Color midnightLightColor) {
        palettes.get(primaryPaletteIndex).setMidnightLightColor(midnightLightColor);
    }

    public Color getPreDuskLightColor() {
        return palettes.get(primaryPaletteIndex).getPreDuskLightColor();
    }

    public void setPreDuskLightColor(Color preDuskLightColor) {
        palettes.get(primaryPaletteIndex).setPreDuskLightColor(preDuskLightColor);
    }

    public Color getSunRiseLightColor() {
        return palettes.get(primaryPaletteIndex).getSunRiseLightColor();
    }

    public void setSunRiseLightColor(Color sunRiseLightColor) {
        palettes.get(primaryPaletteIndex).setSunRiseLightColor(sunRiseLightColor);
    }

    public Color getPostDuskLightColor() {
        return palettes.get(primaryPaletteIndex).getPostDuskLightColor();
    }

    public void setPostDuskLightColor(Color postDuskLightColor) {
        palettes.get(primaryPaletteIndex).setPostDuskLightColor(postDuskLightColor);
    }

    public Color getMiddayLightColor() {
        return palettes.get(primaryPaletteIndex).getMiddayLightColor();
    }

    public void setMiddayLightColor(Color middayLightColor) {
        palettes.get(primaryPaletteIndex).setMiddayLightColor(middayLightColor);
    }

    public Color getPreDawnLightColor() {
        return palettes.get(primaryPaletteIndex).getPreDawnLightColor();
    }

    public void setPreDawnLightColor(Color preDawnLightColor) {
        palettes.get(primaryPaletteIndex).setPreDawnLightColor(preDawnLightColor);
    }

    public Color getSunSetLightColor() {
        return palettes.get(primaryPaletteIndex).getSunSetLightColor();
    }

    public void setSunSetLightColor(Color sunSetLightColor) {
        palettes.get(primaryPaletteIndex).setSunSetLightColor(sunSetLightColor);
    }

    public Color getPostDawnLightColor() {
        return palettes.get(primaryPaletteIndex).getPostDawnLightColor();
    }

    public void setPostDawnLightColor(Color postDawnLightColor) {
        palettes.get(primaryPaletteIndex).setPostDawnLightColor(postDawnLightColor);
    }

    // General accessors
    public Color getMidnightDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getMidnightDarkColor();
    }

    public void setMidnightDarkColor(int paletteIndex, Color midnightDarkColor) {
        palettes.get(paletteIndex).setMidnightDarkColor(midnightDarkColor);
    }

    public Color getPreDuskDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPreDuskDarkColor();
    }

    public void setPreDuskDarkColor(int paletteIndex, Color preDuskDarkColor) {
        palettes.get(paletteIndex).setPreDuskDarkColor(preDuskDarkColor);
    }

    public Color getSunRiseDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getSunRiseDarkColor();
    }

    public void setSunRiseDarkColor(int paletteIndex, Color sunRiseDarkColor) {
        palettes.get(paletteIndex).setSunRiseDarkColor(sunRiseDarkColor);
    }

    public Color getPostDuskDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPostDuskDarkColor();
    }

    public void setPostDuskDarkColor(int paletteIndex, Color postDuskDarkColor) {
        palettes.get(paletteIndex).setPostDuskDarkColor(postDuskDarkColor);
    }

    public Color getMiddayDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getMiddayDarkColor();
    }

    public void setMiddayDarkColor(int paletteIndex, Color middayDarkColor) {
        palettes.get(paletteIndex).setMiddayDarkColor(middayDarkColor);
    }

    public Color getPreDawnDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPreDawnDarkColor();
    }

    public void setPreDawnDarkColor(int paletteIndex, Color preDawnDarkColor) {
        palettes.get(paletteIndex).setPreDawnDarkColor(preDawnDarkColor);
    }

    public Color getSunSetDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getSunSetDarkColor();
    }

    public void setSunSetDarkColor(int paletteIndex, Color sunSetDarkColor) {
        palettes.get(paletteIndex).setSunSetDarkColor(sunSetDarkColor);
    }

    public Color getPostDawnDarkColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPostDawnDarkColor();
    }

    public void setPostDawnDarkColor(int paletteIndex, Color postDawnDarkColor) {
        palettes.get(paletteIndex).setPostDawnDarkColor(postDawnDarkColor);
    }

    public Color getMidnightLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getMidnightLightColor();
    }

    public void setMidnightLightColor(int paletteIndex, Color midnightLightColor) {
        palettes.get(paletteIndex).setMidnightLightColor(midnightLightColor);
    }

    public Color getPreDuskLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPreDuskLightColor();
    }

    public void setPreDuskLightColor(int paletteIndex, Color preDuskLightColor) {
        palettes.get(paletteIndex).setPreDuskLightColor(preDuskLightColor);
    }

    public Color getSunRiseLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getSunRiseLightColor();
    }

    public void setSunRiseLightColor(int paletteIndex, Color sunRiseLightColor) {
        palettes.get(paletteIndex).setSunRiseLightColor(sunRiseLightColor);
    }

    public Color getPostDuskLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPostDuskLightColor();
    }

    public void setPostDuskLightColor(int paletteIndex, Color postDuskLightColor) {
        palettes.get(paletteIndex).setPostDuskLightColor(postDuskLightColor);
    }

    public Color getMiddayLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getMiddayLightColor();
    }

    public void setMiddayLightColor(int paletteIndex, Color middayLightColor) {
        palettes.get(paletteIndex).setMiddayLightColor(middayLightColor);
    }

    public Color getPreDawnLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPreDawnLightColor();
    }

    public void setPreDawnLightColor(int paletteIndex, Color preDawnLightColor) {
        palettes.get(paletteIndex).setPreDawnLightColor(preDawnLightColor);
    }

    public Color getSunSetLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getSunSetLightColor();
    }

    public void setSunSetLightColor(int paletteIndex, Color sunSetLightColor) {
        palettes.get(paletteIndex).setSunSetLightColor(sunSetLightColor);
    }

    public Color getPostDawnLightColor(int paletteIndex) {
        return palettes.get(paletteIndex).getPostDawnLightColor();
    }

    public void setPostDawnLightColor(int paletteIndex, Color postDawnLightColor) {
        palettes.get(paletteIndex).setPostDawnLightColor(postDawnLightColor);
    }

    public static class DayPalette {
        protected Color midnightDarkColor;
        protected Color preDawnDarkColor;
        protected Color sunRiseDarkColor;
        protected Color postDawnDarkColor;
        protected Color middayDarkColor;
        protected Color preDuskDarkColor;
        protected Color sunSetDarkColor;
        protected Color postDuskDarkColor;

        protected Color midnightLightColor;
        protected Color preDawnLightColor;
        protected Color sunRiseLightColor;
        protected Color postDawnLightColor;
        protected Color middayLightColor;
        protected Color preDuskLightColor;
        protected Color sunSetLightColor;
        protected Color postDuskLightColor;

        public DayPalette() {
            midnightDarkColor = new Color();
            preDawnDarkColor = new Color();
            sunRiseDarkColor = new Color();
            postDawnDarkColor = new Color();
            middayDarkColor = new Color();
            preDuskDarkColor = new Color();
            sunSetDarkColor = new Color();
            postDuskDarkColor = new Color();

            midnightLightColor = new Color();
            preDawnLightColor = new Color();
            sunRiseLightColor = new Color();
            postDawnLightColor = new Color();
            middayLightColor = new Color();
            preDuskLightColor = new Color();
            sunSetLightColor = new Color();
            postDuskLightColor = new Color();
        }

        public Color getMidnightDarkColor() {
            return midnightDarkColor;
        }

        public void setMidnightDarkColor(Color midnightDarkColor) {
            this.midnightDarkColor = midnightDarkColor;
        }

        public Color getPreDuskDarkColor() {
            return preDuskDarkColor;
        }

        public void setPreDuskDarkColor(Color preDuskDarkColor) {
            this.preDuskDarkColor = preDuskDarkColor;
        }

        public Color getSunRiseDarkColor() {
            return sunRiseDarkColor;
        }

        public void setSunRiseDarkColor(Color sunRiseDarkColor) {
            this.sunRiseDarkColor = sunRiseDarkColor;
        }

        public Color getPostDuskDarkColor() {
            return postDuskDarkColor;
        }

        public void setPostDuskDarkColor(Color postDuskDarkColor) {
            this.postDuskDarkColor = postDuskDarkColor;
        }

        public Color getMiddayDarkColor() {
            return middayDarkColor;
        }

        public void setMiddayDarkColor(Color middayDarkColor) {
            this.middayDarkColor = middayDarkColor;
        }

        public Color getPreDawnDarkColor() {
            return preDawnDarkColor;
        }

        public void setPreDawnDarkColor(Color preDawnDarkColor) {
            this.preDawnDarkColor = preDawnDarkColor;
        }

        public Color getSunSetDarkColor() {
            return sunSetDarkColor;
        }

        public void setSunSetDarkColor(Color sunSetDarkColor) {
            this.sunSetDarkColor = sunSetDarkColor;
        }

        public Color getPostDawnDarkColor() {
            return postDawnDarkColor;
        }

        public void setPostDawnDarkColor(Color postDawnDarkColor) {
            this.postDawnDarkColor = postDawnDarkColor;
        }

        public Color getMidnightLightColor() {
            return midnightLightColor;
        }

        public void setMidnightLightColor(Color midnightLightColor) {
            this.midnightLightColor = midnightLightColor;
        }

        public Color getPreDuskLightColor() {
            return preDuskLightColor;
        }

        public void setPreDuskLightColor(Color preDuskLightColor) {
            this.preDuskLightColor = preDuskLightColor;
        }

        public Color getSunRiseLightColor() {
            return sunRiseLightColor;
        }

        public void setSunRiseLightColor(Color sunRiseLightColor) {
            this.sunRiseLightColor = sunRiseLightColor;
        }

        public Color getPostDuskLightColor() {
            return postDuskLightColor;
        }

        public void setPostDuskLightColor(Color postDuskLightColor) {
            this.postDuskLightColor = postDuskLightColor;
        }

        public Color getMiddayLightColor() {
            return middayLightColor;
        }

        public void setMiddayLightColor(Color middayLightColor) {
            this.middayLightColor = middayLightColor;
        }

        public Color getPreDawnLightColor() {
            return preDawnLightColor;
        }

        public void setPreDawnLightColor(Color preDawnLightColor) {
            this.preDawnLightColor = preDawnLightColor;
        }

        public Color getSunSetLightColor() {
            return sunSetLightColor;
        }

        public void setSunSetLightColor(Color sunSetLightColor) {
            this.sunSetLightColor = sunSetLightColor;
        }

        public Color getPostDawnLightColor() {
            return postDawnLightColor;
        }

        public void setPostDawnLightColor(Color postDawnLightColor) {
            this.postDawnLightColor = postDawnLightColor;
        }
    }

}

