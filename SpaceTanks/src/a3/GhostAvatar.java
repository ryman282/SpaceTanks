package a3;

import java.awt.Color;
import java.util.UUID;

import graphicslib3D.Point3D;
import sage.scene.shape.Cylinder;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class GhostAvatar extends Tank{
	private UUID id;
	private Point3D pos;
	private Game game;
	private EnemyHealthBar eh;
	
	public GhostAvatar(UUID id, Point3D pos, Game game, String teamColor) {
		this.id = id;
		this.pos = pos;
		this.game = game;
		setTeam(teamColor);
		this.translate((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
	}
	
	public void calcTexture(boolean hurt, boolean boost) {
		setBoosting(boost);
		if(hurt == true) {
			this.setTankTexture("hurt");
		}else{
			this.setTankTexture(this.getTeam());
		}
	}
	
	public UUID getID () {
		return id;
	}
	
	public void setPosition(Point3D pos) {
		this.pos = pos;
	}
	
	public Point3D getPosition() {
		return pos;
	}
	
	public void setHealthBar(EnemyHealthBar healthbar) {
		eh = healthbar;
	}
	
	public EnemyHealthBar getHealthBar() {
		return eh;
	}
}
