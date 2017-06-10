package myGameEngine;

import a3.Game;
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

public class BoostFireAction extends AbstractInputAction{
	private Tank avatar;
	private IPhysicsObject tankP;
	private Game game;
	private ICamera cam;
	
	public BoostFireAction(Tank n, IPhysicsObject p, Game game, ICamera cam) {
		avatar = n;
		tankP = p;
		this.game = game;
		this.cam = cam;
	}
	
	public void performAction(float time, Event e) {
		if(e.getValue() < -0.3) {
			if(avatar.getFire() == true) {
				game.createBullet(game.getPlayerPosition(),cam.getViewDirection());
				game.playLaserSound(avatar.getPosition());
				avatar.setFire(false);
				}
		}else if(e.getValue() > 0.3) {
			if(avatar.getCanBoost() == true) {
				avatar.setSpeed(avatar.getBoostSpeed());
				avatar.setBoosting(true);
				avatar.setTankTexture(avatar.getTeam());
				game.playBoostSound(avatar.getPosition());
				avatar.setCanBoost(false);
			}
		}else{
		}
	}
}
