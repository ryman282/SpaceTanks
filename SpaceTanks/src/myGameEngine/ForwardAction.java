package myGameEngine;

import a3.Tank;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.camera.ICamera;
import sage.input.action.AbstractInputAction;
import sage.physics.IPhysicsObject;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.scene.shape.Rectangle;
import sage.terrain.TerrainBlock;

public class ForwardAction extends AbstractInputAction{
	private Tank avatar;
	private IPhysicsObject tankP;
	
	public ForwardAction(Tank n, IPhysicsObject p) {
		avatar = n;
		tankP = p;
	}
	
	public void performAction(float time, Event e) {
		
		TriMesh t = avatar.getTop();
		
		Matrix3D rot = t.getLocalRotation();
		Vector3D dir = new Vector3D(0,0,-1);
		dir = dir.mult(rot);
		dir.scale((double)(avatar.getSpeed()*time));
		float dirX = (float)dir.getX();
		float dirY = (float)dir.getY();
		float dirZ = (float)dir.getZ();
		tankP.applyForce(dirX, dirY, dirZ, 0.0f, 0.0f, 0.0f);
	}
}
