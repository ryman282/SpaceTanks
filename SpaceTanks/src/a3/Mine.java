package a3;

import java.awt.Color;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.scene.Group;
import sage.scene.shape.Cylinder;

public class Mine extends Group {
	private String team = "blue";
	
	
	public Mine(String teamColor) {
		this.team = teamColor;
		addChild(createBottom());
		addChild(createTop(this.team));
	}
	
	private Cylinder createTop(String t) {
		Cylinder top = new Cylinder();
		
		top.setHeight(.1);
		top.setRadius(.1);
		top.setSlices(40);
		top.setStacks(40);
		top.setSolid(true);
		
		if(t == "blue") {
			top.setColor(Color.cyan);
		}else{
			top.setColor(Color.orange);
		}
		
		top.rotate(90,new Vector3D(1,0,0));
		top.translate(0f,.1f,0f);
		
		return top;
	}
	
	private Cylinder createBottom() {
		Cylinder bottom = new Cylinder();
		bottom.setColor(Color.DARK_GRAY);
		
		bottom.setHeight(.25);
		bottom.setRadius(.5);
		bottom.setSlices(40);
		bottom.setStacks(40);
		bottom.setSolid(true);
		
		bottom.rotate(90,new Vector3D(1,0,0));
		
		return bottom;
	}
	
	public Point3D getPosition() {
		Matrix3D pos = (Matrix3D) this.getLocalTranslation().clone();
		return new Point3D(pos.getCol(3).getX(),pos.getCol(3).getY()+.25,pos.getCol(3).getZ());
	}
	
	public String getTeam() {
		return this.team;
	}
}
