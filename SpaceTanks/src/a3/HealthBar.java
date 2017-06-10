package a3;
import java.awt.Color;
import java.util.Vector;

import graphicslib3D.Matrix3D;
import sage.renderer.IRenderer;
import sage.scene.shape.Rectangle;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class HealthBar extends Rectangle{
	private Texture health10 = TextureManager.loadTexture2D("textures/healthbar/healthbar10.png");
	private Texture health9 = TextureManager.loadTexture2D("textures/healthbar/healthbar9.png");
	private Texture health8 = TextureManager.loadTexture2D("textures/healthbar/healthbar8.png");
	private Texture health7 = TextureManager.loadTexture2D("textures/healthbar/healthbar7.png");
	private Texture health6 = TextureManager.loadTexture2D("textures/healthbar/healthbar6.png");
	private Texture health5 = TextureManager.loadTexture2D("textures/healthbar/healthbar5.png");
	private Texture health4 = TextureManager.loadTexture2D("textures/healthbar/healthbar4.png");
	private Texture health3 = TextureManager.loadTexture2D("textures/healthbar/healthbar3.png");
	private Texture health2 = TextureManager.loadTexture2D("textures/healthbar/healthbar2.png");
	private Texture health1 = TextureManager.loadTexture2D("textures/healthbar/healthbar1.png");
	private Texture health0 = TextureManager.loadTexture2D("textures/healthbar/healthbar0.png");
	
	private Vector<Texture> health = new Vector<Texture>();
	
	public HealthBar() {
		health.add(health0);
		health.add(health1);
		health.add(health2);
		health.add(health3);
		health.add(health4);
		health.add(health5);
		health.add(health6);
		health.add(health7);
		health.add(health8);
		health.add(health9);
		health.add(health10);
		
		Matrix3D mt = getWorldTranslation();
		mt.translate(-.75, -.78, -.5);
		setWorldTranslation(mt);
		
		Matrix3D ms = getWorldScale();
		ms.scale(-.5f,-.15f,.5f);
		setWorldScale(ms);
		
		
		setTexture(health.elementAt(10));
		setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
	}
	
	public void update(int hp) {
		setTexture(health.elementAt((int)hp/10));
	}
}
