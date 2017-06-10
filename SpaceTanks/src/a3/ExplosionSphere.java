package a3;

import java.awt.Color;

import sage.scene.shape.Sphere;

public class ExplosionSphere extends Sphere{
	private Boolean gone = true;
	private float expandTimer = 0;
	private float expandLimit = 100;
	
	public ExplosionSphere(float limit) {
		expandLimit = limit;
		this.setColor(Color.gray);
	}
	
	public void update(float time) {
		expandTimer += time;
		if(expandTimer < expandLimit) {
			this.scale(1.2f, 1.2f, 1.2f);
		}else if(expandTimer >= expandLimit && expandTimer < expandLimit*2){
			this.scale(.8f, .8f, .8f);
		}else{
			gone = true;
		}
	}
	
	public boolean getGone() {
		return gone;
	}
}
