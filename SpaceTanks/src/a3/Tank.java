package a3;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import sage.scene.Material;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.event.IEventListener;
import sage.event.IGameEvent;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.scene.Group;
import sage.scene.TriMesh;
import sage.scene.shape.Cube;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class Tank extends Group {
	private Texture bluetank = TextureManager.loadTexture2D("textures/tank/bluetank.png");
	private Texture bluetankgun = TextureManager.loadTexture2D("textures/tank/bluetankgun.png");
	private Texture bluetankboost = TextureManager.loadTexture2D("textures/tank/bluetankboost.png");
	private Texture orangetank = TextureManager.loadTexture2D("textures/tank/orangetank.png");
	private Texture orangetankgun = TextureManager.loadTexture2D("textures/tank/orangetankgun.png");
	private Texture orangetankboost = TextureManager.loadTexture2D("textures/tank/orangetankboost.png");
	private Texture whitetank = TextureManager.loadTexture2D("textures/tank/whitetank.png");
	private Texture whitetankgun = TextureManager.loadTexture2D("textures/tank/whitetankgun.png");
	private Texture hurttank = TextureManager.loadTexture2D("textures/tank/hurttank.png");
	
	private TriMesh bottom, top;
	
	private Point3D point1,point2;
	private boolean move;
	private Vector3D movement;
	
	private int health = 100;
	
	private String team = "blue";
	
	private float speed = 5.0f;
	private float normalSpeed = 5.0f;
	private float boostSpeed = 15.0f;
	
	private boolean boosting = false;
	
	private float boost = 5000f;
	private float boostMax = 5000f;
	private boolean canBoost = true;
	
	private int mineTimer = 0;
	private int mineTimerMax = 200;
	private boolean canMine = true;
	
	private int fireTimer = 0;
	private int fireTimerMax = 500;
	private boolean canFire = true;
	
	private int hurtTimer = 0;
	private int hurtTimerMax = 200;
	private boolean hurt = false;
	
	private int mines = 3;
	
	private Game game;
	
	public Tank() {
		
		move = false;
		
		bottom = new TriMesh();
		OBJLoader loader = new OBJLoader();
		bottom = loader.loadModel("models/tank.obj");
		this.addChild(bottom);
		this.updateLocalBound();
	
		Matrix3D tankR = getLocalRotation();
		tankR.rotate(0, 90, 0);
		bottom.setLocalRotation(tankR);
		top = new TriMesh();
		OBJLoader loader2 = new OBJLoader();
		top = loader2.loadModel("models/tank_gun.obj");
		this.addChild(top);
		this.updateLocalBound();
		
		setTankTexture(team);
		
		top.setLocalRotation(tankR);
		
		point1 = getPosition();
		point2 = getPosition();
	}
	
	public void update(float elapsedTimeMS, int hp) {
		
		health = hp;
		
		if(hurt == true) {
			hurtTimer += elapsedTimeMS;
			if(hurtTimer > hurtTimerMax) {
				setTankTexture(team);
				hurt = false;
				hurtTimer = 0;
			}
		}
		
		if(canFire == false) {
			fireTimer += elapsedTimeMS;
			if(fireTimer > fireTimerMax) {
				canFire = true;
				fireTimer = 0;
			}
		}
		
		if(canMine == false) {
			mineTimer += elapsedTimeMS;
			if(mineTimer > mineTimerMax) {
				canMine = true;
				mineTimer = 0;
			}
		}
		
		if(canBoost == false) {
			if(getSpeed() == getBoostSpeed()) {
				boost -= elapsedTimeMS*5;
				if(boost < 0) {
					boosting = false;
					setSpeed(getNormalSpeed());
					boost = 0;
					if(hurt == false && health > 0) {
						setTankTexture(getTeam());
					}
				}
			}
			if(getSpeed() == getNormalSpeed()) {
				boost += elapsedTimeMS;
				if(boost > boostMax) {
					canBoost = true;
					boost = boostMax;
				}
			}
		}
		
		
		updatePosition();
		
	}
	
	public int getMines() {
		return mines;
	}
	
	public void setMines(int n) {
		mines = n;
	}
	
	public void setCanMine(boolean b) {
		canMine = b;
	}
	
	public boolean getCanMine() {
		return canMine;
	}
	
	public void updatePosition() {
		point1 = getPosition();
		
		if(!point1.equals(point2)) {
			if(point1.distanceTo(point2) > 0.01) {
				setMove(true);
			} else {
				setMove(false);
			}
		} else {
			setMove(false);
		}
		
		Vector3D direction = new Vector3D(point2.minus(point1));
		setVector(direction);
		
		
		direction = direction.normalize();
		Vector3D up = new Vector3D(0,1,0);
		Vector3D right = up.cross(direction).normalize();
		Vector3D backwards = right.cross(up).normalize();
		Matrix3D rot = new Matrix3D();
		rot.setRow(0, right);
		rot.setRow(1, up);
		rot.setRow(2, backwards);
		getBottom().setLocalRotation(rot.inverse());
		
		point2 = getPosition();
	}
	
	public void setTankTexture(String s) {
		if(s == "blue") {
			if(boosting) {
				bottom.setTexture(bluetankboost);
			}else{
				bottom.setTexture(bluetank);
			}
			top.setTexture(bluetankgun);
		}else if(s == "orange") {
			if(boosting) {
				bottom.setTexture(orangetankboost);
			}else{
				bottom.setTexture(orangetank);
			}
			top.setTexture(orangetankgun);
		}else if(s == "white") {
			bottom.setTexture(whitetank);
			top.setTexture(whitetankgun);
		}else if(s == "hurt") {
			bottom.setTexture(hurttank);
			top.setTexture(hurttank);
		}
	}
	
	public void setTeam(String s) {
		team = s;
		setTankTexture(team);
	}
	
	public String getTeam() {
		return team;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float f) {
		speed = f;
	}
	public float getNormalSpeed() {
		return normalSpeed;
	}
	
	public float getBoostSpeed() {
		return boostSpeed;
	}
	
	public TriMesh getTop() {
		return top;
	}
	
	public TriMesh getBottom() {
		return bottom;
	}
	
	public void setBoosting(boolean b) {
		boosting = b;
	}
	
	public boolean getBoosting() {
		return boosting;
	}
	
	public Point3D getPosition() {
		Matrix3D m = (Matrix3D)this.getLocalTranslation().clone();
		Point3D pos = new Point3D(m.getCol(3));
		return pos;
	}
	
	public void setMove(boolean b) {
		move = b;
	}
	
	public boolean getMove() {
		return move;
	}
	
	public Vector3D getVector() {
		return movement;
	}
	
	public void setVector(Vector3D v) {
		movement = v;
	}
	
	public void setFire(Boolean b) {
		canFire = b;
	}
	
	public Boolean getFire() {
		return canFire;
	}
	
	public void setCanBoost(Boolean b) {
		canBoost = b;
	}
	
	public Boolean getCanBoost() {
		return canBoost;
	}
	
	public float getBoost() {
		return boost;
	}
	
	public float getBoostMax() {
		return boostMax;
	}
	
	public void setHurt(Boolean b) {
		hurt = b;
		if(hurt == true) {
			setTankTexture("hurt");
		}
	}
	
	public Boolean getHurt() {
		return hurt;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int h) {
		health = h;
	}
}
