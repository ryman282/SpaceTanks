package a3;
import java.awt.Color;
import java.util.Vector;

import graphicslib3D.Matrix3D;
import sage.renderer.IRenderer;
import sage.scene.shape.Rectangle;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class BoostBar extends Rectangle{
	private Texture deadtexture = TextureManager.loadTexture2D("textures/boostbar/deadboostbar.png");
	private Texture charge5 = TextureManager.loadTexture2D("textures/boostbar/charge5.png");
	private Texture charge4 = TextureManager.loadTexture2D("textures/boostbar/charge4.png");
	private Texture charge3 = TextureManager.loadTexture2D("textures/boostbar/charge3.png");
	private Texture charge2 = TextureManager.loadTexture2D("textures/boostbar/charge2.png");
	private Texture charge1 = TextureManager.loadTexture2D("textures/boostbar/charge1.png");
	private Texture charge0 = TextureManager.loadTexture2D("textures/boostbar/charge0.png");
	private Texture boost5 = TextureManager.loadTexture2D("textures/boostbar/boost5.png");
	private Texture boost4 = TextureManager.loadTexture2D("textures/boostbar/boost4.png");
	private Texture boost3 = TextureManager.loadTexture2D("textures/boostbar/boost3.png");
	private Texture boost2 = TextureManager.loadTexture2D("textures/boostbar/boost2.png");
	private Texture boost1 = TextureManager.loadTexture2D("textures/boostbar/boost1.png");
	private Texture boost0 = TextureManager.loadTexture2D("textures/boostbar/boost0.png");
	
	private Vector<Texture> charge = new Vector<Texture>();
	private Vector<Texture> boost = new Vector<Texture>();
	
	public BoostBar() {
		charge.add(charge0);
		charge.add(charge1);
		charge.add(charge2);
		charge.add(charge3);
		charge.add(charge4);
		charge.add(charge5);
		
		boost.add(boost0);
		boost.add(boost1);
		boost.add(boost2);
		boost.add(boost3);
		boost.add(boost4);
		boost.add(boost5);
		
		
		Matrix3D mt = getWorldTranslation();
		mt.translate(-.75, -.93, -.5);
		setWorldTranslation(mt);
		
		Matrix3D ms = getWorldScale();
		ms.scale(-.5f,-.15f,.5f);
		setWorldScale(ms);
		
		
		setTexture(charge.elementAt(5));
		setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
	}
	
	public void update(float boostNum, float boostNumMax, String state, Boolean dead) {
		if(dead == false) {
			if(state == "charge") {
				this.setTexture(charge.elementAt((int)(boostNum/boostNumMax*5)));
			}else{
				this.setTexture(boost.elementAt((int)(boostNum/boostNumMax*5)));
			}
		}else{
			this.setTexture(deadtexture);
		}
	}
}
