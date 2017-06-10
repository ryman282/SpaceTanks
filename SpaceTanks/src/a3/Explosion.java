package a3;

import java.util.Iterator;
import java.util.Vector;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import sage.scene.Group;
import sage.scene.SceneNode;

public class Explosion extends Group {
	private Point3D location;
	private int size = 0;
	private boolean done = false;
	
	public Explosion(Point3D p, int s) {
		location = p;
		size = s;
		
		Matrix3D m = getWorldTranslation();
		m.translate(location.getX(), location.getY(), location.getZ());
		setWorldTranslation(m);
		
		for(int i = 0; i < size; i++) {
			Point3D ep = new Point3D(Math.random()*20,Math.random()*20,Math.random()*20);
			ExplosionSphere e = new ExplosionSphere(10000);
			
			Matrix3D em = getWorldTranslation();
			e.translate((float)ep.getX(), (float)ep.getY(), (float)ep.getZ());
			e.setLocalTranslation(em);
			
			this.addChild(e);
		}
	}
	
	public void update(float time) {
		Iterator<SceneNode> itr = this.iterator();
		while(itr.hasNext()) {
			ExplosionSphere es = (ExplosionSphere) itr.next();
			if(es.getGone() == true) {
				itr.remove();
			}else{
				es.update(time);
			}
		}
	}
	
	public boolean getDone() {
		return done;
	}
}
