package a3;

import java.awt.Color;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

public class NPCBullet extends Bullet{

	public NPCBullet(Point3D p, Vector3D d, float time) {
		super(p, d, time,"red");
		super.setSpeed(.1);
	}

}
