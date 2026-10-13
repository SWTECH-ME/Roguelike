package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class TrippyBackground extends Widget {
    private ShaderProgram shader;
    private float time = 0;
    private Texture whiteTexture;
    private Color color1;
    private Color color2;

    public TrippyBackground(Color c1, Color c2) {
        this.color1 = c1;
        this.color2 = c2;
        
        String vertexShader = "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "void main() {\n" +
                "    v_color = a_color;\n" +
                "    v_texCoords = a_texCoord0;\n" +
                "    gl_Position = u_projTrans * a_position;\n" +
                "}";
                
        String fragmentShader = "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform float u_time;\n" +
                "uniform vec3 u_color1;\n" +
                "uniform vec3 u_color2;\n" +
                "void main() {\n" +
                "    vec2 uv = v_texCoords * 3.0;\n" +
                "    uv.x *= 1.77;\n" +
                "    vec2 p = uv;\n" +
                "    for(int i = 1; i < 5; i++) {\n" +
                "        float fi = float(i);\n" +
                "        vec2 newp = p;\n" +
                "        newp.x += 0.8 / fi * sin(fi * p.y + u_time * 2.0 + 0.3);\n" +
                "        newp.y += 0.8 / fi * cos(fi * p.x + u_time * 2.0 + 0.3);\n" +
                "        p = newp;\n" +
                "    }\n" +
                "    float t = sin(p.x + p.y) * 0.5 + 0.5;\n" +
                "    vec3 finalColor = mix(u_color1, u_color2, t);\n" +
                "    gl_FragColor = vec4(finalColor, 1.0);\n" +
                "}";
                
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexShader, fragmentShader);
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

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta * 0.8f; 
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shader != null && shader.isCompiled()) {
            batch.flush();
            ShaderProgram oldShader = batch.getShader();
            batch.setShader(shader);
            
            shader.setUniformf("u_time", time);
            shader.setUniformf("u_color1", color1.r, color1.g, color1.b);
            shader.setUniformf("u_color2", color2.r, color2.g, color2.b);
            
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            batch.draw(whiteTexture, getX(), getY(), getWidth(), getHeight());
            
            batch.flush();
            batch.setShader(oldShader);
        } else {
            batch.setColor(color1.r, color1.g, color1.b, parentAlpha);
            batch.draw(whiteTexture, getX(), getY(), getWidth(), getHeight());
        }
    }
}