package a3;
import java.awt.Color;
import java.util.Vector;

import graphicslib3D.Matrix3D;
import sage.renderer.IRenderer;
import sage.scene.shape.Rectangle;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class MineBar extends Rectangle{
	private Texture mines3 = TextureManager.loadTexture2D("textures/mines/minesgraphic3.png");
	private Texture mines2 = TextureManager.loadTexture2D("textures/mines/minesgraphic2.png");
	private Texture mines1 = TextureManager.loadTexture2D("textures/mines/minesgraphic1.png");
	private Texture mines0 = TextureManager.loadTexture2D("textures/mines/minesgraphic0.png");
	
	private Vector<Texture> mines = new Vector<Texture>();
	
	public MineBar() {
		mines.add(mines0);
		mines.add(mines1);
		mines.add(mines2);
		mines.add(mines3);
		
		
		Matrix3D mt = getWorldTranslation();
		mt.translate(.75, -.93, -.5);
		setWorldTranslation(mt);
		
		Matrix3D ms = getWorldScale();
		ms.scale(-.5f,-.15f,.5f);
		setWorldScale(ms);
		
		
		setTexture(mines.elementAt(3));
		setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
	}
	
	public void update(int m, Boolean dead) {
		if(dead == false) {
			this.setTexture(mines.elementAt(m));
		}else{
			this.setTexture(mines.elementAt(0));
		}
	}
}
