package a3;

import java.io.File;
import java.util.Iterator;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.scene.Group;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class UFO extends Group{
	private Group avatar;
	private TriMesh model;
	private double size;
	private Point3D location;
	private Matrix3D rotMat;
	private Vector3D direction;
	private int id;
	private int health;
	private boolean dead;
	Texture skin = TextureManager.loadTexture2D("textures/enemyUV.png");
	Texture hurtTexture = TextureManager.loadTexture2D("textures/hurtNPC.png");
	private EnemyHealthBar eh;
	
	public UFO(int id) {
		//this.avatar = getPlayerAvatar();
		//initAvatar();
		//avatar.translate(50, 10, 50);
		//avatar.scale(.7f, .7f, .2f);
		//this.addChild(avatar);
		//this.updateLocalBound();
		this.id = id;
		location = new Point3D(0,0,0);
		model = new TriMesh();
		OBJLoader loader = new OBJLoader();
		model = loader.loadModel("models/enemy.obj");
		model.setTexture(skin); 
		size = 1.0;
		rotMat = this.getLocalRotation();
		rotMat.rotateY(90.0);
		this.setLocalRotation(rotMat);
		Matrix3D scaleMat = this.getLocalScale();
		scaleMat.scale(.7, .7, .7);
		this.setLocalScale(scaleMat);
		this.addChild(model);
		this.updateLocalBound();
		this.updateWorldBound();
		health = 100;
		dead = false;
		
		System.out.println("UFO Created");
	}
	
	public void setDead(boolean b) {
		dead = b;
	}
	
	private void initAvatar() {
		Iterator<SceneNode> itr = avatar.getChildren();
		while(itr.hasNext()) {
			Model3DTriMesh mesh = (Model3DTriMesh) itr.next();
			System.out.println(mesh.toString());
			mesh.startAnimation("Enemy_firing");
		}
	}
	
	private Group getPlayerAvatar() {
		Group model = null;
		OgreXMLParser loader = new OgreXMLParser();
		loader.setVerbose(true);
		try {
			String slash = File.separator;
			model = loader.loadModel("materials" + slash + "enemy.mesh.xml", "materials" + slash + "enemy.material", "materials" + slash + "enemy.skeleton.xml");
			model.updateGeometricState(0, true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return model;
	}
	
	public Group getAvatar() {
		return avatar;
	}
	
	public void getSmall() {
		Matrix3D mat = this.getLocalScale();
		mat.scale(0.5f, 0.5f, 0.5f);
		this.setLocalScale(mat);
		this.size = 1.0f;
	}
	
	public void getBig() {
		Matrix3D mat = this.getLocalScale();
		mat.scale(2.0f, 2.0f, 2.0f);
		this.setLocalScale(mat);
		this.size = 2.0f;
	}
	
	public void update(float time) {
		Iterator<SceneNode> itr = avatar.getChildren();
		while(itr.hasNext()) {
			Model3DTriMesh submesh = (Model3DTriMesh) itr.next();
			submesh.updateAnimation(time);
		}
		updateGeometricState(time, true);
	}
	
	public void setLocation(Point3D pos) {
		this.location = pos;
	}
	
	public Point3D getLocation() {
		return this.location;
	}
	
	public Vector3D getDirection() {
		return direction;
	}

	public void lookAtMe(Point3D pos) {
		Vector3D dir = new Vector3D(location.minus(pos)).normalize();
		direction = dir.mult(-1);
		Vector3D up = new Vector3D(0,1,0);
		Vector3D right = up.cross(dir).normalize();
		Vector3D backwards = right.cross(up).normalize();
		Matrix3D rot = new Matrix3D();
		rot.setRow(0, right);
		rot.setRow(1, up);
		rot.setRow(2, backwards);
		rot.rotateY(90);
		this.setLocalRotation(rot.inverse());
	}
	
	public int getID() {
		return id;
	}
	
	public boolean bulletCheck(Bullet b) {
		if(this.getWorldBound().contains(b.getPosition())) {
			hurt();
			if(health <= 0) {
				dead = true;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean getDead() {
		return dead;
	}
	
	public void hurt() {
		if(health > 0) {
			health = health - 20;
			setNPCTexture("hurt");
		}
	}
	
	public void setNPCTexture(String s) {
		if(s.equals("hurt")) {
			model.setTexture(hurtTexture);
		} else if(s.equals("skin")) {
			model.setTexture(skin);
		}
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int hp) {
		health = hp;
	}
	
	public void setHealthBar(EnemyHealthBar eh) {
		this.eh = eh;
	}
	
	public EnemyHealthBar getHealthBar() {
		return eh;
	}
}
