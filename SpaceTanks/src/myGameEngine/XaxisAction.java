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

public class XaxisAction extends AbstractInputAction{
	private Tank avatar;
	private IPhysicsObject tankP;
	
	public XaxisAction(Tank n, IPhysicsObject p) {
		avatar = n;
		tankP = p;
	}
	
	public void performAction(float time, Event e) {
		TriMesh t = avatar.getTop();
		Vector3D dir = new Vector3D(0,0,0);
		
		Matrix3D rot = t.getLocalRotation();
		if(e.getValue() < -0.3 ) {
			dir = new Vector3D(1,0,0);
		}else if(e.getValue() > 0.3) {
			dir = new Vector3D(-1,0,0);
		}else{
			dir = new Vector3D(0,0,0);
		}
		dir = dir.mult(rot);
		dir.scale((double)(avatar.getSpeed()*time));
		float dirX = (float)dir.getX();
		float dirY = (float)dir.getY();
		float dirZ = (float)dir.getZ();
		tankP.applyForce(dirX, dirY, dirZ, 0.0f, 0.0f, 0.0f);
	}
}
