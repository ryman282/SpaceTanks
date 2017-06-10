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

public class MineAction extends AbstractInputAction{
	private Tank avatar;
	private Game game;
	private ICamera camera;
	
	public MineAction(Tank n, Game g) {
		avatar = n;
		game = g;
	}

	public void performAction(float time, Event e) {
		if(avatar.getMines() > 0 && avatar.getCanMine()) {
		//	game.createMine(avatar.getPosition());
			avatar.setMines(avatar.getMines()-1);
			avatar.setCanMine(false);
		}
	}
}