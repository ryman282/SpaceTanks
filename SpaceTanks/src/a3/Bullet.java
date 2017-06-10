package a3;

import java.awt.Color;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

public class Bullet extends sage.scene.shape.Cylinder{
	
	private Point3D location;
	private Vector3D direction;
	private double speed = .4;
	private float timer;
	private String team = "blue";
	private boolean bounce = false;
	
	public Bullet(Point3D p, Vector3D d, float time, String t) {
		team = t;
		location = p;
		direction = d;
		timer = time;
		direction.setY(0);
		
		direction.normalize();
		direction.mult(speed);
		
		setHeight(.5);
		setRadius(.1);
		setSlices(40);
		setStacks(40);
		setSolid(true);
		
		if(team == "blue") {
			setColor(Color.cyan);
		}
		if(team == "orange") {
			setColor(Color.orange);
		}
		if(team == "red") {
			setColor(Color.red);
		}
		
		Matrix3D m = getLocalTranslation();
		m.translate(location.getX() + direction.getX()*speed*10, location.getY() + direction.getY()*speed*10, location.getZ() + direction.getZ()*speed*10);
		setWorldTranslation(m);
		
		double degrees = Math.toDegrees(Math.atan(direction.getX()/direction.getZ()));
		Matrix3D rotM = getLocalRotation();
		rotM.rotate(degrees, new Vector3D(0,1,0));
		setWorldRotation(rotM);
		
	}
	
	public void update() {
		Matrix3D m = getWorldTranslation();
		m.translate(direction.getX()*speed, direction.getY()*speed, direction.getZ()*speed);
		setLocalTranslation(m);
	}
	
	public Point3D getPosition() {
		Point3D pos = new Point3D(this.getLocalTranslation().getCol(3));
		return pos;
	}
	
	public void setTeam(String s) {
		team = s;
		
		if(team == "blue") {
			setColor(Color.cyan);
		}
		if(team == "orange") {
			setColor(Color.orange);
		}
	}
	
	public String getTeam() {
		return team;
	}
	
	public float getTimer() {
		return timer;
	}
	
	public void setSpeed(double s) {
		speed = s;
	}
	
	public boolean getBounce() {
		return bounce;
	}
	
	public void setBounce(boolean b) {
		bounce = b;
	}

}
