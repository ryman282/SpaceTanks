package a3;

import java.util.Vector;

import graphicslib3D.Matrix3D;
import sage.scene.SceneNode;
import sage.scene.shape.Rectangle;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class EnemyHealthBar extends Rectangle{
	private Texture health10 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar10.png");
	private Texture health9 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar9.png");
	private Texture health8 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar8.png");
	private Texture health7 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar7.png");
	private Texture health6 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar6.png");
	private Texture health5 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar5.png");
	private Texture health4 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar4.png");
	private Texture health3 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar3.png");
	private Texture health2 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar2.png");
	private Texture health1 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar1.png");
	private Texture health0 = TextureManager.loadTexture2D("textures/enemyhealthbar/enemyhealthbar0.png");
	
	private Vector<Texture> health = new Vector<Texture>();
	
	private Tank maintank;
	private SceneNode entity;
	private int hp;
	
	public EnemyHealthBar(int hp, SceneNode e, Tank mt) {
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
		
		entity = e;
		maintank = mt;
		this.hp = hp;
		
		Matrix3D ms = getWorldScale();
		ms.scale(-2.0f,-.25f,.5f);
		setLocalScale(ms);
		
		Matrix3D m = (Matrix3D) entity.getWorldTranslation().clone();
		m.translate(0, 5, 0);
		setLocalTranslation(m);
		
		setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		
		setTexture(health.elementAt((int)hp/10));
	}
	
	public void update(int hp) {
		
		Matrix3D m = (Matrix3D) entity.getWorldTranslation().clone();
		m.translate(0, 2, 0);
		setLocalTranslation(m);
		
		Matrix3D m2 = (Matrix3D) maintank.getTop().getWorldRotation().clone();
		m2.rotate(0,90,0);
		setLocalRotation(m2);
		
		this.hp = hp;
		
		setTexture(health.elementAt((int)hp/10));
	}
	
	public SceneNode getEntity() {
		return entity;
	}
	
	public int getHealth() {
		return hp;
	}
}
