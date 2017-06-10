package a3;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;

import javax.swing.SwingUtilities;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Component.Identifier.Key;
import sage.camera.ICamera;
import sage.input.IInputManager;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.renderer.IRenderer;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.util.MathUtils;

public class Camera3PcontrollerKeyboardMouse {
	private ICamera cam;
	private Tank target;
	private float cameraAzimuth;
	private float cameraElevation;
	private float cameraDistanceFromTarget;
	private float cameraElevationLimitUpper, cameraElevationLimitLower;
	private Point3D targetPos;
	private Vector3D worldUpVec;
	private Robot robot;
	private Dimension dim;
	private Point center;
	private Canvas c;
	private Game g;
	private boolean gp;
	
	public Camera3PcontrollerKeyboardMouse(Game g, ICamera cam, IRenderer r, Tank target, IInputManager inputMgr, String mouseName, boolean gp) {
		this.gp = gp;
		this.g = g;
		this.cam = cam;
		this.target = target;
		worldUpVec = new Vector3D(0,1,0);
		cameraDistanceFromTarget = 15.0f;
		cameraAzimuth = 0;
		cameraElevation = 8.0f;
		
		cameraElevationLimitUpper = 8.0f;
		cameraElevationLimitLower = 8.0f;
		
		c = r.getCanvas();
		dim = c.getSize();
		center = new Point(dim.width/2, dim.height/2);
		System.out.println(center.getX() + " " + center.getY());
		
		try
		{robot = new Robot();}
		catch(AWTException ex)
		{throw new RuntimeException("couldn't create robot!");}
		
		setupInput(inputMgr, mouseName);
		
		
		update(0.0f);
	}
	
	private void setupInput(IInputManager im, String mn) {
		
		IAction orbitYAction = new OrbitYAction();
		IAction orbitXAction = new OrbitXAction();
		
		if(gp == false) {
			im.associateAction(mn,Axis.Y,orbitYAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(mn,Axis.X,orbitXAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}else{
			im.associateAction(mn,Axis.RY,orbitYAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(mn,Axis.RX,orbitXAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
	}
	
	public void updateInput(IInputManager im, String mn, boolean gp) {
		this.gp = gp;
		
		IAction orbitYAction = new OrbitYAction();
		IAction orbitXAction = new OrbitXAction();
		
		if(this.gp == false) {
			im.associateAction(mn,Axis.Y,orbitYAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(mn,Axis.X,orbitXAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}else{
			im.associateAction(mn,Axis.RY,orbitYAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(mn,Axis.RX,orbitXAction,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
	}

	public void update(float time) {
		updateTarget();
		updateCameraPosition();

		Point p = new Point((int)center.getX(),(int)center.getY());
		SwingUtilities.convertPointToScreen(p, c);
		robot.mouseMove((int)p.getX(),(int)p.getY());
		
		cam.lookAt(targetPos, worldUpVec);
	}
	
	public void updateTarget()
	{
		targetPos = new Point3D(target.getWorldTranslation().getCol(3));
		targetPos.setY(targetPos.getY()+2);
		
		
		//rotate turret with camera
		TriMesh t = target.getTop();
		
		Vector3D x = cam.getRightAxis();
		
		Vector3D z = (x.cross(worldUpVec).mult(-1));
		
		Matrix3D tm = new Matrix3D();
		tm.setRow(0, x);
		tm.setRow(1, worldUpVec);
		tm.setRow(2, z);
		tm.rotate(180, worldUpVec);
		t.setLocalRotation(tm);
		
	}
	
	private void updateCameraPosition() {
		double theta = cameraAzimuth;
		double phi = cameraElevation;
		double r = cameraDistanceFromTarget;
		
		Point3D relativePosition = MathUtils.sphericalToCartesian(theta,phi,r);
		Point3D desiredCameraLoc = relativePosition.add(targetPos);
		cam.setLocation(desiredCameraLoc);
	}
	
	private class OrbitXAction extends AbstractInputAction {
	
		public void performAction(float time, Event e) {
			cameraAzimuth -= e.getValue()/2;
			cameraAzimuth = cameraAzimuth % 360;
		}
	}
	
	private class OrbitYAction extends AbstractInputAction {
		
		public void performAction(float time, Event e) {
			if((cameraElevation + e.getValue()) <= cameraElevationLimitUpper && (cameraElevation + e.getValue()) >= (cameraElevationLimitLower)) {
				cameraElevation += e.getValue()/2;
				cameraElevation = cameraElevation % 360;
			}else if(cameraElevation + e.getValue() > cameraElevationLimitUpper) {
				cameraElevation = cameraElevationLimitUpper;
			}else if (cameraElevation + e.getValue() < cameraElevationLimitLower){
				cameraElevation = cameraElevationLimitLower;
			}
		}
	}
	
	public void Zoom(float zoomAmount){
		cameraDistanceFromTarget += zoomAmount;
	}

	public float getAzimuth() {
		return cameraAzimuth;
	}
	
	public float getDistance() {
		return cameraDistanceFromTarget;
	}
}
