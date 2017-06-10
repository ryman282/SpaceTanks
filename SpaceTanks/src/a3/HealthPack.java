package a3;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import sage.model.loader.OBJLoader;
import sage.scene.Group;
import sage.scene.TriMesh;
import sage.scene.shape.Cube;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class HealthPack extends Group{
	Texture healthpack = TextureManager.loadTexture2D("textures/healthpack.png");
	OBJLoader loader = new OBJLoader();
	private int ID;
	private boolean pickedUp = false;
	private Point3D pos;
	
	public HealthPack(int id, int x, int z) {
		TriMesh health1 = loader.loadModel("models/healthpack.obj");
		health1.setTexture(healthpack);
		addChild(health1);
		scale(2, 2, 2);
		
		pos = new Point3D(x,.75,z);
		
		Matrix3D h1 = getWorldTranslation();
		h1.translate(x, .75, z);
		health1.setLocalTranslation(h1);
		
		ID = id;
	}
	
	public boolean getPickedUp() {
		return pickedUp;
	}
	
	public void setPickedUp(boolean b) {
		pickedUp = b;
	}
	
	public int getID() {
		return ID;
	}
	
	public Point3D getPosition() {
		return pos;
	}
	
	public void setID(int id) {
		ID = id;
	}
}
