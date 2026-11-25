package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class TrippyBackground extends Widget {
    private final ShaderProgram shader;

    private static float globalTime = 0f;
    private static long lastFrameId = 0;
    private static boolean frozen = false;

    /** 
     * @param frozen
     */
    public static void setFrozen(boolean frozen) {
        TrippyBackground.frozen = frozen;
    }
    
    private static final String VERTEX_SHADER = String.join("\n",
            "attribute vec4 a_position;",
            "attribute vec4 a_color;",
            "attribute vec2 a_texCoord0;",
            "uniform mat4 u_projTrans;",
            "varying vec4 v_color;",
            "varying vec2 v_texCoords;",
            "void main() {",
            "    v_color = a_color;",
            "    v_texCoords = a_texCoord0;",
            "    gl_Position = u_projTrans * a_position;",
            "}");

    private static final String FRAGMENT_SHADER = String.join("\n",
            "#ifdef GL_ES",
            "precision mediump float;",
            "#endif",
            "varying vec4 v_color;",
            "varying vec2 v_texCoords;",
            "uniform float u_time;",
            "uniform vec3 u_color1;",
            "uniform vec3 u_color2;",
            "void main() {",
            "    float pixels = 256.0;",
            "    vec2 uv = floor(v_texCoords * pixels) / pixels;",
            "    uv *= 8.0;",
            "    uv.x *= 1.77;",
            "    vec2 p = uv;",
            "    for(int i = 1; i < 7; i++) {",
            "        float fi = float(i);",
            "        vec2 newp = p;",
            "        newp.x += 0.6 / fi * sin(fi * p.y + u_time * 1.2 + 0.3);",
            "        newp.y += 0.6 / fi * cos(fi * p.x + u_time * 1.2 + 0.3);",
            "        p = newp;",
            "    }",
            "    float t = cos(p.x + p.y + 1.0) * 0.5 + 0.5;",
            "    vec3 finalColor = mix(u_color1, u_color2, t);",
            "    gl_FragColor = vec4(finalColor, 1.0) * v_color;",
            "}");

    private final Texture whiteTexture;
    private final Color color1;
    private final Color color2;

    public TrippyBackground(Color c1, Color c2) {
        this.color1 = new Color(c1);
        this.color2 = new Color(c2);

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) {
            Gdx.app.error("TrippyBackground", "Shader compilation failed:\n" + shader.getLog());
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        whiteTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
    }

    /** 
     * @param c1
     * @param c2
     */
    public void setColors(Color c1, Color c2) {
        this.color1.set(c1);
        this.color2.set(c2);
    }

    /** 
     * @param delta
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        
        long currentFrame = Gdx.graphics.getFrameId();
        if (currentFrame != lastFrameId) {
            if (!frozen) {
                globalTime += delta * 0.8f;
            }
            lastFrameId = currentFrame;
        }
    }

    /** 
     * @param batch
     * @param parentAlpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        float overscan = 150f; 

        if (shader != null && shader.isCompiled()) {
            batch.flush();
            ShaderProgram oldShader = batch.getShader();
            batch.setShader(shader);
            
            shader.setUniformf("u_time", globalTime);
            shader.setUniformf("u_color1", color1.r, color1.g, color1.b);
            shader.setUniformf("u_color2", color2.r, color2.g, color2.b);
            
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            batch.draw(whiteTexture, getX() - overscan, getY() - overscan, getWidth() + (overscan * 2), getHeight() + (overscan * 2));
            
            batch.flush();
            batch.setShader(oldShader);
        } else {
            batch.setColor(color1.r, color1.g, color1.b, parentAlpha);
            batch.draw(whiteTexture, getX() - overscan, getY() - overscan, getWidth() + (overscan * 2), getHeight() + (overscan * 2));
        }
    }
}