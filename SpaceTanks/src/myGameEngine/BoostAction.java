package myGameEngine;

import a3.Game;
import a3.Tank;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.input.action.AbstractInputAction;
import sage.physics.IPhysicsObject;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.scene.shape.Rectangle;
import sage.terrain.TerrainBlock;

public class BoostAction extends AbstractInputAction{
	private Tank avatar;
	private Game game;
	
	public BoostAction(Tank n, Game g) {
		avatar = n;
		game = g;
	}

	public void performAction(float time, Event e) {
		if(avatar.getCanBoost() == true) {
			avatar.setSpeed(avatar.getBoostSpeed());
			avatar.setBoosting(true);
			avatar.setTankTexture(avatar.getTeam());
			game.playBoostSound(avatar.getPosition());
			avatar.setCanBoost(false);
		}
	}
}
