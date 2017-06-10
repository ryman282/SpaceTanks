package a3;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import myGameEngine.BackwardAction;
import myGameEngine.BoostAction;
import myGameEngine.BoostFireAction;
import myGameEngine.FireAction;
import myGameEngine.ForwardAction;
import myGameEngine.LeftAction;
import myGameEngine.MineAction;
import myGameEngine.MyDisplaySystem;
import myGameEngine.QuitAction;
import myGameEngine.RightAction;
import myGameEngine.XaxisAction;
import myGameEngine.YaxisAction;
import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.camera.controllers.ThirdPersonOrbitCameraController;
import sage.display.*;
import sage.display.DisplaySystem;
import sage.scene.Controller;
import sage.scene.Group;
import sage.scene.HUDImage;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.SceneNode.CULL_MODE;
import sage.scene.SceneNode.RENDER_MODE;
import sage.scene.shape.Cube;
import sage.scene.shape.Cylinder;
import sage.scene.shape.Line;
import sage.scene.shape.Rectangle;
import sage.scene.shape.Sphere;
import sage.terrain.ImageBasedHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;
import sage.event.*;
import sage.input.IInputManager;
import sage.input.ThirdPersonCameraController;
import sage.model.loader.OBJLoader;
import sage.networking.IGameConnection.ProtocolType;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.renderer.IRenderer;

public class Game extends BaseGame {
	
	//GAME START UP VARIABLES
	private float teamTimer = 0;
	private float gameStartTimer = 0;
	private boolean playerReady = false;
	
	//GAME COMPONENTS
	private IRenderer renderer;
	private IDisplaySystem display;
	private ICamera cam;
	private Camera3PcontrollerKeyboardMouse camCtrl;
	
	//SERVER/CLIENT CONNECTION
	private InetAddress addr;
	private int portNum;
	private ProtocolType serverProtocol;
	private Client thisClient;
	private boolean connectionStatus;
	private Vector3D playerPosition;
	
	//INPUT VARIABLES
	private IInputManager im;
	private String kbName;
	private String mName;
	private String gpName;
	private Boolean dead = false;
	private Boolean gamepad = false;
	
	//ENITITY GROUPS
	private Group bullets = new Group();
	private Group mines = new Group();
	private Group entities = new Group();
	private Group blueTeamGroup = new Group();
	private Group orangeTeamGroup = new Group();
	private Group ghostGroup = new Group();
	private Group healthPacks = new Group();
	private Group healthBars = new Group();
	
	//HUD VARIABLES
	int p1Health = 100;
	private HUDString blueTeam, orangeTeam, gameStartString;
	private HealthBar hb;
	private BoostBar bb;
	private MineBar mb;
	
	//GAME OBJECTS
	private EnemyHealthBar eh;
	private SkyBox sb;
	Tank p1;
	private UFO npc1, npc2, npc3, npc4;
	private Cube c1, c2, c3, c4, c5, c6, c7, c8;
	
	//PHYSICS STUFF
	private IPhysicsEngine physicsEngine;
	private IPhysicsObject tankP, ballP, recP, npcP, wall1, wall2, wall3, wall4, wall5, wall6, wall7, wall8; 
	private Sphere ball;
	
	//TERRAIN
	private TerrainBlock myTerrain;
	private Rectangle rec;
	
	//AUDIO VARIABLES
	private IAudioManager audioMgr;
	private AudioResource laser;
	private Sound fireSound;
	private AudioResource boost;
	private Sound boostSound;
	
	public Object lock = new Object();
	
	//MISC VARIABLES
	private float time;
	
	private float ghostUpdateTimer, ghostUpdateCounter;
	private float oldTime;
	private float newTime;
	
	private String team;
	
	private float clientCallTime;
	
	private EventManager ev;
	
	
	public Game(String serverAddress, String serverPort) {
		System.out.println("Game created");
		try {
			if(!serverAddress.equals("")) {
				addr = InetAddress.getByName(serverAddress);
			} else {
				addr = InetAddress.getLocalHost();
				System.out.println(addr.toString());
			}
			if(!serverPort.equals("")) {
				portNum = Integer.parseInt(serverPort);
			} else {
				portNum = 8282;
			}
			serverProtocol = ProtocolType.TCP;
			connectionStatus = false;
			playerPosition = new Vector3D();
		} catch (UnknownHostException e) {
		}
		
	}
	
	protected void initGame() {
		
		//create client and identify if connection was made
		
		try {
			thisClient = new Client(addr, portNum, serverProtocol, this);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//if the client is there, process intial packets
		
		if(thisClient != null) {
			connectionStatus = true;
			thisClient.processPackets();
		}
		
		//create custom display for FSEM, and get renderer
		
		display = initDisplay();
		renderer = display.getRenderer();
		
		//set no cursor
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Cursor noCursor = tk.createCustomCursor(tk.getImage(""), new Point(), "NoCursor");
		renderer.getCanvas().setCursor(noCursor);
		
		//set up camera
		
		cam = new JOGLCamera(renderer);
		cam.setPerspectiveFrustum(45, 1, 0.01, 1000);
		cam.setViewport(0.0,1.0,0.0,1.0);
		
		//set up HUD for camera
		
		initHUD();
		
		//do rest of initial game setup
		
		initGameObjects();
		initPhysicsSystem();
		createSagePhysicsWorld();
		
		oldTime = time;
		newTime = time;
		ghostUpdateCounter = 0;
		
		super.update((float)0.0);
	}
	
	private IDisplaySystem initDisplay() {
		display = new MyDisplaySystem(1920,1080,24,60,true,"sage.renderer.jogl.JOGLRenderer");
		System.out.println("\n Waiting for display creation...");
		int count = 0;
		
		
		while(!display.isCreated())
		{
			try
			{Thread.sleep(10);}
			catch (InterruptedException e)
			{throw new RuntimeException("Display creation interreupted");}
			
			count++;
			System.out.print("+");
			if(count % 80 == 0){System.out.println();}
			
			if(count > 2000)
			{throw new RuntimeException("Unable to create display");
			}
		}
		System.out.println();
		return display;
	}
	
	public void closeDisplay() {
		display.close();
	}
	
	private void initHUD() {
		
		bb = new BoostBar();
		cam.addToHUD(bb);
		
		hb = new HealthBar();
		cam.addToHUD(hb);
		/*
		mb = new MineBar();
		cam.addToHUD(mb);*/
		
		blueTeam = new HUDString("0");
		blueTeam.setLocation(.4,.9);
		blueTeam.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		blueTeam.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		blueTeam.setColor(Color.BLUE);
		
		orangeTeam = new HUDString("0");
		orangeTeam.setLocation(.6,.9);
		orangeTeam.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		orangeTeam.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		orangeTeam.setColor(Color.ORANGE);
		
		gameStartString = new HUDString("Game starts in: " + (60-gameStartTimer));
		gameStartString.setLocation(.45,.95);
		gameStartString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		gameStartString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		gameStartString.setColor(Color.WHITE);
		
		cam.addToHUD(gameStartString);
		cam.addToHUD(blueTeam);
		cam.addToHUD(orangeTeam);
				
	}
	
	private void initInputs() {
		im = getInputManager();
		
		if(im.getFirstGamepadName() != null) {
			gpName = im.getFirstGamepadName();
			camCtrl = new Camera3PcontrollerKeyboardMouse(this,cam,renderer,p1,im,gpName,true);
		}else{
			mName = im.getMouseName();
			camCtrl = new Camera3PcontrollerKeyboardMouse(this,cam,renderer,p1,im,mName,false);
		}
		
		QuitAction quit = new QuitAction(this);
		kbName = im.getKeyboardName();
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, quit,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}
	
	public void checkInputs() {
		if(im.getFirstGamepadName() != null) {
			controllerInputs();
		}else{
			keyBoardMouseInputs();
		}
	}
	
	public void controllerInputs() {
		XaxisAction xaxis = new XaxisAction(p1,tankP);
		YaxisAction yaxis = new YaxisAction(p1,tankP);
		BoostFireAction boostfire = new BoostFireAction(p1,tankP,this,cam);
		MineAction mine = new MineAction(p1,this);
		
		gpName = im.getFirstGamepadName();
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, xaxis,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, yaxis,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Z, boostfire, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		//im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, mine, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}
	
	public void keyBoardMouseInputs() {
		ForwardAction forward = new ForwardAction(p1, tankP);
		BackwardAction backward = new BackwardAction(p1, tankP);
		RightAction right = new RightAction(p1, tankP);
		LeftAction left = new LeftAction(p1, tankP);
		FireAction fire = new FireAction(p1,this,cam);
		BoostAction boost = new BoostAction(p1,this);
		MineAction mine = new MineAction(p1,this);
		
		kbName = im.getKeyboardName();
		 

		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, forward,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, backward,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, right,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, left,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, boost, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		im.associateAction(mName, net.java.games.input.Component.Identifier.Button.LEFT, fire,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		//im.associateAction(mName, net.java.games.input.Component.Identifier.Button.RIGHT, mine,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}
	
	public void disableKeyBoardMouse() {
		kbName = im.getKeyboardName();
		mName = im.getMouseName();
		
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, null,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, null,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, null,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, null,IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, null, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		im.associateAction(mName, net.java.games.input.Component.Identifier.Button.LEFT, null,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}
	
	public void updateGameStartTimer(int time) {
		gameStartTimer = (float)time;
		if(gameStartTimer > 60 && playerReady == true) {
			checkInputs();
			playerReady = false;
		}
	}
	
	public void updateGameStartTimer(float ms) {
		gameStartTimer += ms;
		if(gameStartTimer > 10 && playerReady == true) {
			checkInputs();
			playerReady = false;
		}
	}
	
	private void initGameObjects() {
		
		Texture t1 = TextureManager.loadTexture2D("textures/skybox/space_ft.png");
		Texture t2 = TextureManager.loadTexture2D("textures/skybox/space_rt.png");
		Texture t3 = TextureManager.loadTexture2D("textures/skybox/space_lf.png");
		Texture t4 = TextureManager.loadTexture2D("textures/skybox/space_bk.png");
		Texture t5 = TextureManager.loadTexture2D("textures/skybox/space_up.png");
		Texture t6 = TextureManager.loadTexture2D("textures/skybox/space_dn.png");
		
		sb = new SkyBox();
		sb.scale(100, 100, 100);
		sb.translate(0,0,0);
		sb.setTexture(SkyBox.Face.North, t1);
		sb.setTexture(SkyBox.Face.West, t2);
		sb.setTexture(SkyBox.Face.East, t3);
		sb.setTexture(SkyBox.Face.South, t4);
		sb.setTexture(SkyBox.Face.Up, t5);
		sb.setTexture(SkyBox.Face.Down, t6);
		
		addGameWorldObject(sb);
		
		addGameWorldObject(healthPacks);
		
		initHealthPack(1,10,10);
		initHealthPack(2,10,114);
		initHealthPack(3,114,10);
		initHealthPack(4,114,114);
		
		//ground
		rec = new Rectangle();
		Matrix3D recMT = rec.getLocalTranslation();
		Matrix3D recMS = rec.getLocalScale();
		Matrix3D recMR = rec.getLocalRotation();
		rec.setLocalTranslation(recMT);
		recMS.scale(500, 500, 500);
		rec.setLocalScale(recMS);
		recMR.rotate(90,0,0);
		rec.setLocalRotation(recMR);
		rec.setCullMode(CULL_MODE.ALWAYS);
		rec.updateLocalBound();
		addGameWorldObject(rec);
		
		addGameWorldObject(bullets);
		addGameWorldObject(mines);
		
		c1 = new Cube();
		Matrix3D c1tm = c1.getWorldTranslation();
		c1tm.translate(0, 5, 62.5);
		c1.setLocalTranslation(c1tm);
		Matrix3D c1sm = c1.getWorldScale();
		c1sm.scale(4, 5, 57.5);
		c1.setLocalScale(c1sm);
		c1.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c1);
		
		c2 = new Cube();
		Matrix3D c2tm = c2.getWorldTranslation();
		c2tm.translate(62.5, 5, 0);
		c2.setLocalTranslation(c2tm);
		Matrix3D c2sm = c2.getWorldScale();
		c2sm.scale(57.5, 5, 4);
		c2.setLocalScale(c2sm);
		c2.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c2);
		
		c3 = new Cube();
		Matrix3D c3tm = c3.getWorldTranslation();
		c3tm.translate(124, 5, 62.5);
		c3.setLocalTranslation(c3tm);
		Matrix3D c3sm = c3.getWorldScale();
		c3sm.scale(4, 5, 57.5);
		c3.setLocalScale(c3sm);
		c3.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c3);
		
		c4 = new Cube();
		Matrix3D c4tm = c4.getWorldTranslation();
		c4tm.translate(62.5, 5, 124);
		c4.setLocalTranslation(c4tm);
		Matrix3D c4sm = c4.getWorldScale();
		c4sm.scale(57.5, 5, 4);
		c4.setLocalScale(c4sm);
		c4.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c4);
		
		c5 = new Cube();
		Matrix3D c5tm = c5.getWorldTranslation();
		c5tm.translate(62, 5, 31);
		c5.setLocalTranslation(c5tm);
		Matrix3D c5sm = c5.getWorldScale();
		c5sm.scale(7, 5, 14);
		c5.setLocalScale(c5sm);
		c5.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c5);
		
		c6 = new Cube();
		Matrix3D c6tm = c6.getWorldTranslation();
		c6tm.translate(62, 5, 92);
		c6.setLocalTranslation(c6tm);
		Matrix3D c6sm = c6.getWorldScale();
		c6sm.scale(7, 5, 14);
		c6.setLocalScale(c6sm);
		c6.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c6);
		
		c7 = new Cube();
		Matrix3D c7tm = c7.getWorldTranslation();
		c7tm.translate(28.5, 5, 61.5);
		c7.setLocalTranslation(c7tm);
		Matrix3D c7sm = c7.getWorldScale();
		c7sm.scale(6.5, 5, 14.5);
		c7.setLocalScale(c7sm);
		c7.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c7);
		
		c8 = new Cube();
		Matrix3D c8tm = c8.getWorldTranslation();
		c8tm.translate(95.5, 5, 61.5);
		c8.setLocalTranslation(c8tm);
		Matrix3D c8sm = c8.getWorldScale();
		c8sm.scale(6.5, 5, 14.5);
		c8.setLocalScale(c8sm);
		c8.setCullMode(CULL_MODE.ALWAYS);
		addGameWorldObject(c8);
		
		p1 = new Tank();
		Matrix3D p1MT = p1.getLocalTranslation();
		p1MT.translate(50, 20, 50);
		p1.setLocalTranslation(p1MT);
		addGameWorldObject(p1);
		p1.updateGeometricState(1.0f, true);
		p1.setTankTexture("blue");
		
		addRec(new Point3D(10,.3,10),5,Color.blue);
		addRec(new Point3D(10,.3,114),5,Color.blue);
		addRec(new Point3D(114,.3,10),5,Color.orange);
		addRec(new Point3D(114,.3,114),5,Color.orange);
		
		
		
	/*	Point3D origin = new Point3D(0,0,0);
		Point3D xEnd = new Point3D(100,0,0);
		Point3D yEnd = new Point3D(0,100,0);
		Point3D zEnd = new Point3D(0,0,100);
		Line xAxis = new Line(origin,xEnd,Color.red,2);
		Line yAxis = new Line(origin,yEnd,Color.green,2);
		Line zAxis = new Line(origin,zEnd,Color.blue,2);
		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);*/
		
		ImageTerrain myTerObj = new ImageTerrain(display);
		myTerrain = myTerObj.getTerrain();
		myTerrain.translate(0, 13, 0);
		addGameWorldObject(myTerrain);
		
		if(thisClient!=null) {
			thisClient.importNPCS();
		}
		
		addGameWorldObject(ghostGroup);
		addGameWorldObject(healthBars);
	}
	
	public void initHealthPack(int id, int x, int z) {

		HealthPack health =  new HealthPack(id,x,z);
		healthPacks.addChild(health);
	}
	
	
	public void initPhysicsSystem()
	{
		String engine = "sage.physics.ODE4J.ODE4JPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		float[] gravity = {0,-5.0f,0};
		physicsEngine.setGravity(gravity);
	}
	
	public void initAudio()
	{
		audioMgr = AudioManagerFactory.createAudioManager("sage.audio.joal.JOALAudioManager");
		if(!audioMgr.initialize())
		{
			System.out.println("Audio Manager failed to initialize!");
		}
		boost = audioMgr.createAudioResource("soundeffects/boost.wav", AudioResourceType.AUDIO_SAMPLE);
		laser = audioMgr.createAudioResource("soundeffects/laser.wav", AudioResourceType.AUDIO_SAMPLE);
		
		boostSound = new Sound(boost, SoundType.SOUND_EFFECT, 100, false);
		boostSound.initialize(audioMgr);
		boostSound.setMaxDistance(50.0f);
		boostSound.setMinDistance(3.0f);
		boostSound.setRollOff(5.0f);
		
		fireSound = new Sound(laser, SoundType.SOUND_EFFECT, 100, false);
		fireSound.initialize(audioMgr);
		fireSound.setMaxDistance(50.0f);
		fireSound.setMinDistance(3.0f);
		fireSound.setRollOff(5.0f);
		
		setEarParameters();
	}
	
	public void playLaserSound(Point3D p) {
		fireSound.setLocation(p);
		fireSound.play(100,false);
	}
	
	public void playBoostSound(Point3D p) {
		boostSound.setLocation(p);
		boostSound.play(1,false);
	}
	
	private void setEarParameters() {
		Matrix3D avatarDir = (Matrix3D) (p1.getWorldRotation().clone());
		float cameraAz = camCtrl.getAzimuth();
		avatarDir.rotateY(180.0f-cameraAz);
		Vector3D cameraDir = new Vector3D(0,0,1);
		cameraDir = cameraDir.mult(avatarDir);
		
		audioMgr.getEar().setLocation(cam.getLocation());
		audioMgr.getEar().setOrientation(cameraDir, new Vector3D(0,1,0));
	}
	
	private void setTankPhysics() {
		float mass = 10.0f;
		float[] size = {1.6f, 1.0f, 1.6f};
		tankP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass, p1.getWorldTransform().getValues(), size);
		tankP.setBounciness(0.0f);
		tankP.setFriction(0.0f);
		tankP.setDamping(0.03f, 0.03f);
		p1.setPhysicsObject(tankP);
		tankP.applyForce(0.001f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	private void createSagePhysicsWorld() {
		
		float wallmass = 100000;
		
		float[] wallsize1 = {(float)c1.getLocalScale().getRow(0).getX()*2,(float)c1.getLocalScale().getRow(1).getY()*2,(float)c1.getLocalScale().getRow(2).getZ()*2};
		wall1 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c1.getWorldTransform().getValues(), wallsize1);
		wall1.setDamping(100f,100f);
		
		float[] wallsize2 = {(float)c2.getLocalScale().getRow(0).getX()*2,(float)c2.getLocalScale().getRow(1).getY()*2,(float)c2.getLocalScale().getRow(2).getZ()*2};
		wall2 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c2.getWorldTransform().getValues(), wallsize2);
		wall2.setDamping(100f,100f);
		
		float[] wallsize3 = {(float)c3.getLocalScale().getRow(0).getX()*2,(float)c3.getLocalScale().getRow(1).getY()*2,(float)c3.getLocalScale().getRow(2).getZ()*2};
		wall3 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c3.getWorldTransform().getValues(), wallsize3);
		wall3.setDamping(100f,100f);
		
		float[] wallsize4 = {(float)c4.getLocalScale().getRow(0).getX()*2,(float)c4.getLocalScale().getRow(1).getY()*2,(float)c4.getLocalScale().getRow(2).getZ()*2};
		wall4 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c4.getWorldTransform().getValues(), wallsize4);
		wall4.setDamping(100f,100f);
		
		float[] wallsize5 = {(float)c5.getLocalScale().getRow(0).getX()*2,(float)c5.getLocalScale().getRow(1).getY()*2,(float)c5.getLocalScale().getRow(2).getZ()*2};
		wall5 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c5.getWorldTransform().getValues(), wallsize5);
		wall5.setDamping(100f,100f);
		
		float[] wallsize6 = {(float)c6.getLocalScale().getRow(0).getX()*2,(float)c6.getLocalScale().getRow(1).getY()*2,(float)c6.getLocalScale().getRow(2).getZ()*2};
		wall6 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c6.getWorldTransform().getValues(), wallsize6);
		wall6.setDamping(100f,100f);
		
		float[] wallsize7 = {(float)c7.getLocalScale().getRow(0).getX()*2,(float)c7.getLocalScale().getRow(1).getY()*2,(float)c7.getLocalScale().getRow(2).getZ()*2};
		wall7 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c7.getWorldTransform().getValues(), wallsize7);
		wall7.setDamping(100f,100f);
		
		float[] wallsize8 = {(float)c8.getLocalScale().getRow(0).getX()*2,(float)c8.getLocalScale().getRow(1).getY()*2,(float)c8.getLocalScale().getRow(2).getZ()*2};
		wall8 = physicsEngine.addBoxObject(physicsEngine.nextUID(), wallmass, c8.getWorldTransform().getValues(), wallsize8);
		wall8.setDamping(100f,100f);
		
		float up[] = {0f, 0.90f, 0};
		recP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), rec.getWorldTransform().getValues(), up, 0.0f);
		recP.setBounciness(0.0f);
		recP.setFriction(1.0f);
	}
	
	public void update(float elapsedTimeMS) {
		
		if(this.thisClient == null) {
			updateGameStartTimer(elapsedTimeMS/1000);
		}
		
		gameStartUpDelay();
		
		time += elapsedTimeMS;
		
		updateHUD(elapsedTimeMS);
		
		p1.update(elapsedTimeMS, p1Health);
		//e.update(p1Health, p1);
		
		/*
		//check for player 1 collision with walls
		if(myTerrain.getHeight((float)p1.getPosition().getX(),(float)p1.getPosition().getZ()) > 0) {
			float[] v = tankP.getLinearVelocity();
			v[0] *= -2;
			v[2] *= -2;
			tankP.setLinearVelocity(v);
		}
		*/
		
		float[] v2 = {0,0,0}; 
		if(p1.getPhysicsObject() != null) {
			v2 = tankP.getLinearVelocity();
		}
		Point3D p = new Point3D(cam.getLocation().getX() + v2[0],0,cam.getLocation().getZ() + v2[2]);
		double d = myTerrain.getHeight((float)(p.getX()-cam.getViewDirection().getX()*3),(float)(p.getZ()-cam.getViewDirection().getZ()*3));
		double dout = myTerrain.getHeight((float)(p.getX()-cam.getViewDirection().getX()*6),(float)(p.getZ()-cam.getViewDirection().getZ()*6));
		
		float zoomDistance = 0;
		
		if(camCtrl != null) {
			
			camCtrl.update(elapsedTimeMS);
			
			if(d > 0 || Double.isNaN(d)) {
				if((camCtrl.getDistance()) > .5f) {
					//zooming = true;
					zoomDistance -= .25;
				}
			}
			if(dout == 0) {
				if((camCtrl.getDistance()) < 15.0f) {
					zoomDistance += .25;
				}
			}
			
			camCtrl.Zoom(zoomDistance);
		}

			if(thisClient != null) {
				thisClient.processPackets();
				if(team == null) {
					team = thisClient.getTeam();
				}
				calculateGhostTime();
			}

		
		Matrix3D mat;
		physicsEngine.update(20.0f);
		for(SceneNode s : getGameWorld()) {
			if(s.getPhysicsObject()!=null) {
				mat = new Matrix3D(s.getPhysicsObject().getTransform());
				s.getLocalTranslation().setCol(3, mat.getCol(3));
			}
		}
		
		sb.setLocalTranslation(p1.getWorldTranslation());
		
		
		//enemy health bar updates
		
		/*Iterator<SceneNode> itrhb = healthBars.iterator();
		while(itrhb.hasNext()) {
			EnemyHealthBar eb = (EnemyHealthBar) itrhb.next();
			SceneNode sn = eb.getEntity();
			if(sn.getLocalTranslation()==null) {
				itrhb.remove();
				removeGameWorldObject(eb);
			}
			if(sn instanceof Tank) {
				Tank t = (Tank) sn;
				eb.update( t.getHealth());
				if(eb.getHealth()==0) {
					itrhb.remove();
					removeGameWorldObject(eb);
				}
			} else if(sn instanceof UFO) {
				UFO npc = (UFO) sn;
				eb.update(npc.getHealth());
				if(eb.getHealth()==0) {
					itrhb.remove();
					removeGameWorldObject(eb);
				}
			}
		}*/
		
		//health pack updates
		
		Iterator<SceneNode> itrh = healthPacks.iterator();
		while(itrh.hasNext()) {
			HealthPack health = (HealthPack) itrh.next();
				health.rotate(.5f, new Vector3D(0,1,0));
				if(p1.getWorldBound().contains(health.getPosition())) {
					if(p1Health < 100 && dead==false) {
						if(p1Health <= 50) {
							p1Health += 50;
						}else{
							p1Health = 100;
						}
						thisClient.sendRemoveHealthPack(health.getID());
						itrh.remove();
						removeGameWorldObject(health);
						//need to remove healthpack server side here
					}
				}
				if(health.getPickedUp() == true) {
					itrh.remove();
					removeGameWorldObject(health);
				}
			}
		
		/*Iterator<SceneNode> itrm = mines.iterator();
		while(itrm.hasNext()) {
			Mine mine = (Mine) itrm.next();
			if(dead == false && p1.getHurt() == false) {
				if(p1.getWorldBound().contains(mine.getPosition())) {
					 if(p1.getTeam() != mine.getTeam()) {
						p1Health-=30;
						if(p1Health <= 0) {
							playerDie();
						}else{
							p1.setHurt(true);
						}
						itrm.remove();
						removeGameWorldObject(mine);
					 }
					 p1.setCanMine(false);
				}
			}
			for(SceneNode sn : ghostGroup) {
				GhostAvatar ga = (GhostAvatar) sn;
				if(ga.getWorldBound() != null) {
					if(ga.getWorldBound().contains(mine.getPosition())) {
						itrm.remove();
						removeGameWorldObject(mine);
					}
				}
			}	
		}*/
		
		Iterator<SceneNode> itr = bullets.iterator();
		while(itr.hasNext()) {
			Bullet bullet = (Bullet) itr.next();
			bullet.update();
			if(myTerrain.getHeight((float)bullet.getPosition().getX(), (float)bullet.getPosition().getZ()) > 0) {
				itr.remove();
				removeGameWorldObject(bullet);
			}
			if(npc1 != null && !npc1.getDead() && npc1.bulletCheck(bullet)) {
				if(npc1.getDead()) {
					this.removeNPC(npc1.getID());
					thisClient.sendNPCDead(npc1);
				}
				itr.remove();
				removeGameWorldObject(bullet);
				thisClient.sendNPCHurt(npc1);
			}
			if(npc2 != null && !npc2.getDead() && npc2.bulletCheck(bullet)) {
				if(npc2.getDead()) {
					this.removeNPC(npc2.getID());
					thisClient.sendNPCDead(npc2);
				}
				itr.remove();
				removeGameWorldObject(bullet);
				thisClient.sendNPCHurt(npc2);
			}
			if(npc3 != null && !npc3.getDead() && npc3.bulletCheck(bullet)) {
				if(npc3.getDead()) {
					this.removeNPC(npc3.getID());
					thisClient.sendNPCDead(npc3);
				}
				itr.remove();
				removeGameWorldObject(bullet);
				thisClient.sendNPCHurt(npc3);
			}
			if(npc4 != null && !npc4.getDead() && npc4.bulletCheck(bullet)) {
				if(npc4.getDead()) {
					this.removeNPC(npc4.getID());
					thisClient.sendNPCDead(npc4);
				}
				itr.remove();
				removeGameWorldObject(bullet);
				thisClient.sendNPCHurt(npc4);
			}
			if(dead == false && p1.getHurt() == false) {
				if(p1.getWorldBound().contains(bullet.getPosition())) {
					 if(p1.getTeam() != bullet.getTeam()) {
						p1Health-=10;
						if(p1Health <= 0) {
							playerDie();
						}else{
							p1.setHurt(true);
						}
					 }
					itr.remove();
					removeGameWorldObject(bullet);
				}
			}
			
			Iterator<SceneNode> i = ghostGroup.iterator();
			while(i.hasNext()) {
				GhostAvatar ga = (GhostAvatar) i.next();
				if(ga.getWorldBound() != null) {
					if(ga.getWorldBound().contains(bullet.getPosition())) {
						itr.remove();
						removeGameWorldObject(bullet);
					}
				}
			}
			
			/*Iterator<SceneNode> i = ghostGroup.iterator();
			while(i.hasNext()) {
				GhostAvatar ga = (GhostAvatar) i.next();
				if(ga.getWorldBound().contains(bullet.getPosition())) {
					itr.remove();
					removeGameWorldObject(bullet);
				}
			}	*/
		}
		
		super.update(elapsedTimeMS);
	}
	
	public void setTeamPosition(Tank t, String team) {
		Point3D orange = new Point3D (107,5,58);
		Point3D blue = new Point3D (9,5,58);
		
		Matrix3D tmat = new Matrix3D();
		tmat.setToIdentity();
		if(team == "blue") {
			System.out.println("blue");
			tmat.translate(blue.getX()+Math.random()*8,blue.getY(),blue.getZ()+Math.random()*8);
			t.setWorldTranslation(tmat);
		}else{
			System.out.println("orange");
			tmat.translate(orange.getX()+Math.random()*8,orange.getY(),orange.getZ()+Math.random()*8);
			t.setWorldTranslation(tmat);
		}
	}
	
	private void gameStartUpDelay() {
		teamTimer++;
		if(teamTimer == 500) {
			p1.setTeam(team);
			if(p1.getTeam() == "blue") {
				blueTeamGroup.addChild(p1);
			}else{
				orangeTeamGroup.addChild(p1);
			}
			setTeamPosition(p1,p1.getTeam());
			setTankPhysics();
			initInputs();
			initAudio();
			playerReady = true;
		}
	}
	
	private void updateHUD(float elapsedTimeMS) {
		hb.update(p1Health);
		
		//mb.update(p1.getMines(), dead);
		
		if(dead == false) {
			if(p1.getCanBoost() == false && p1.getSpeed() == p1.getBoostSpeed()) {
				bb.update(p1.getBoost(),p1.getBoostMax(),"boost",false);
			}else{
				bb.update(p1.getBoost(),p1.getBoostMax(),"charge",false);
			}
		}else{
			bb.update(p1.getBoost(),p1.getBoostMax(),"charge",true);
		}
		
		if((60-gameStartTimer) > 0) {
			DecimalFormat df = new DecimalFormat("0");
			gameStartString.setText("Game starts in: " + df.format(Math.ceil(60-gameStartTimer)));
		}else{
			cam.getHUD().removeChild(gameStartString);
		}
		blueTeam.setText(""+blueTeamGroup.getNumberOfChildren());
		orangeTeam.setText(""+orangeTeamGroup.getNumberOfChildren());
		
	}
	
	public void updateHeight() {

		for(SceneNode sn : entities) {
		Point3D entLoc = new Point3D(sn.getLocalTranslation().getCol(3));
		float x = (float) entLoc.getX();
		float z = (float) entLoc.getZ();
		float terHeight = myTerrain.getHeight(x, z);
		Point3D groundHeight = new Point3D(rec.getLocalTranslation().getCol(3));
		float groundY = (float) groundHeight.getY();
		float desiredHeight = terHeight + (float)myTerrain.getOrigin().getY() + 0.5f;
		if(groundY+2.0 > terHeight) {
			desiredHeight = groundY + (float)myTerrain.getOrigin().getY() + 2.5f;
		}
		sn.getLocalTranslation().setElementAt(1, 3, desiredHeight);
		}
	}
	
	protected void render()
	{
		renderer.setCamera(cam);
		super.render();
	}
	
	public void shutdown() {
		display.close();
		super.shutdown();
		if(thisClient != null) {
			 thisClient.sendByeMessage();
			 try {
				 thisClient.shutdown();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		}
	}
	
	/*public void createExplosion(Point3D point, int size) {
		Explosion e = new Explosion(point,size);
		explosions.addChild(e);
		addGameWorldObject(e);
	}*/
	
	public void setIsConnected(boolean val) {
		this.connectionStatus = val;
	}
	
	public Boolean getDead() {
		return dead;
	}
	
	public Point3D getPlayerPosition() {
		return p1.getPosition();
	}
	
	public void addRec(Point3D point, int size, Color color) {
		Rectangle wallrec = new Rectangle();
		Matrix3D recMT = wallrec.getLocalTranslation();
		Matrix3D recMS = wallrec.getLocalScale();
		Matrix3D recMR = wallrec.getLocalRotation();
		recMR.rotate(90,0,0);
		recMT.translate(point.getX(),point.getY(),point.getZ());
		wallrec.setLocalTranslation(recMT);
		recMS.scale(size,size,size);
		wallrec.setLocalRotation(recMR);
		wallrec.setLocalScale(recMS);
		wallrec.setColor(color);
		addGameWorldObject(wallrec);
	}
	
	public void addGhost(UUID ghostID, Point3D pos, String teamColor) {  //need team information sent from server so it can be properly initialized
		GhostAvatar ghost = new GhostAvatar(ghostID, pos, this, teamColor);
		if(teamColor.equals("blue")) {
			ghost.setTeam("blue");
			blueTeamGroup.addChild(ghost);
		} else {
			ghost.setTeam("orange");
			orangeTeamGroup.addChild(ghost);
		}
		ghostGroup.addChild(ghost);
		addGameWorldObject(ghost);
		
		EnemyHealthBar eh = new EnemyHealthBar(100,ghost,p1);
		addGameWorldObject(eh);
		ghost.setHealthBar(eh);
		
		ghost.updateWorldBound();
	}

	public void moveGhost(UUID ghostID, Point3D pos) {
		Vector<GhostAvatar> ghosts = thisClient.getGhosts();
		for( SceneNode sn : ghostGroup) {
			GhostAvatar ghost = (GhostAvatar) sn;
			if(ghostID.equals(ghost.getID())) {
				Matrix3D mat = ghost.getWorldTranslation();
				Point3D oldPos = new Point3D(mat.getCol(3));
				float x = (float)(pos.getX() - oldPos.getX());
				float y = (float)(pos.getY() - oldPos.getY());
				float z = (float)(pos.getZ() - oldPos.getZ());
				mat.translate(x, y, z);
				ghost.setLocalTranslation(mat);
			}
		}
	}
	
	public void removeGhost(UUID ghostID) {
		Iterator<SceneNode> itr = ghostGroup.iterator();
		while(itr.hasNext()) {
			GhostAvatar ghost = (GhostAvatar) itr.next();
			if(ghost.getID().equals(ghostID)) {
				if(ghost.getTeam() == "blue") {
					blueTeamGroup.removeChild(ghost);
				} else {
					orangeTeamGroup.addChild(ghost);
				}
				itr.remove();
				removeGameWorldObject(ghost.getHealthBar());
				removeGameWorldObject(ghost);
			}
		}
	}

	public void createBullet(Point3D p, Vector3D v) {
		Bullet b = new Bullet(p,v,time,p1.getTeam());
		bullets.addChild(b);
		if(thisClient != null) {
			thisClient.sendTankAttackMessage(p, v);
		}
	}
	
	public void createEnemyBullet(Point3D p, Vector3D v, String team) {
		if(team.equals("blue")) {
			team = "blue";
		} else {
			team = "orange";
		}
		Bullet b = new Bullet(p,v,time,team);
		bullets.addChild(b);
		playLaserSound(p);
	}
	
	public void createNPCBullet(UFO npc) {
		if(!npc.getDead()) {
		Point3D p = npc.getLocation();
		Vector3D v = npc.getDirection();
		NPCBullet b = new NPCBullet(p,v,time);
		bullets.addChild(b);
		playLaserSound(p);
		}
	}
	/*
	public void createMine(Point3D p) {
		Mine m = new Mine(p1.getTeam());
		mines.addChild(m);
		
		Matrix3D matm = m.getLocalTranslation();
		matm.translate(p.getX(), p.getY(), p.getZ());
		m.setWorldTranslation(matm);
		
		if(thisClient != null) {
			thisClient.sendCreateMineMessage(p,p1.getTeam());
		}
	}
	
	public void createEnemyMine(Point3D p,String team) {
		if(team.equals("blue")) {
			team = "blue";
		} else {
			team = "orange";
		}
		
		Mine m = new Mine(team);
		mines.addChild(m);
		
		Matrix3D matm = m.getLocalTranslation();
		matm.translate(p.getX(), p.getY(), p.getZ());
		m.setWorldTranslation(matm);
	}
*/
	public void playerDie() {
		dead = true;
		p1Health = 0;
		deathInputs();
		p1.setTankTexture("white");
		if(thisClient != null) {
			thisClient.sendDeadMessage(thisClient.getID());
		}
	}
	
	public void removeHealthPack(int id) {
		Iterator<SceneNode> itr = healthPacks.iterator();
		while(itr.hasNext()) {
			HealthPack health = (HealthPack) itr.next();
			if(id == health.getID()) {
				health.setPickedUp(true);
			}
		}
	}
	
	public void deathInputs() {
		
		if(im.getFirstGamepadName() == null) {
			im.associateAction(mName, net.java.games.input.Component.Identifier.Button.LEFT, null,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(mName, net.java.games.input.Component.Identifier.Button.RIGHT, null,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, null,IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}else{ 
			im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Z, null, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, null, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}
	}

	public void moveGhost(UUID ghostID, Point3D pos, Vector3D ghostGunVec, Vector3D ghostVec, int hp, boolean boost, boolean hurt) { //need hp information sent from server to update ghostAvatar's health
		Vector<GhostAvatar> ghosts = thisClient.getGhosts();
		double degrees = Math.toDegrees(Math.atan(ghostVec.getX()/ghostVec.getZ()));
		for( SceneNode sn : ghostGroup) {
			GhostAvatar ghost = (GhostAvatar) sn;
			if(ghostID.equals(ghost.getID())) {
				
				ghost.setHealth(hp);
				ghost.getHealthBar().update(ghost.getHealth());
				
				Matrix3D mat = ghost.getWorldTranslation();
				Point3D oldPos = new Point3D(mat.getCol(3));
				float x = (float)(pos.getX() - oldPos.getX());
				float y = (float)(pos.getY() - oldPos.getY());
				float z = (float)(pos.getZ() - oldPos.getZ());
				mat.translate(x, y, z);
				ghost.setLocalTranslation(mat);
				
				TriMesh t = ghost.getTop();
				TriMesh b = ghost.getBottom();
				
				ghostGunVec = ghostGunVec.normalize();
				Vector3D up = new Vector3D(0,1,0);
				Vector3D right = up.cross(ghostGunVec).normalize();
				Vector3D backwards = right.cross(up).normalize();
				Matrix3D rot = new Matrix3D();
				rot.setRow(0, right);
				rot.setRow(1, up);
				rot.setRow(2, backwards);
				t.setLocalRotation(rot.inverse());
				
				ghostVec = ghostVec.normalize();
				Vector3D up2 = new Vector3D(0,1,0);
				Vector3D right2 = up2.cross(ghostVec).normalize();
				Vector3D backwards2 = right2.cross(up2).normalize();
				Matrix3D rot2 = new Matrix3D();
				rot2.setRow(0, right2);
				rot2.setRow(1, up2);
				rot2.setRow(2, backwards2);
				b.setLocalRotation(rot2.inverse());
				
				ghost.calcTexture(hurt, boost);
				
			}
		}
	}
	
	private void calculateGhostTime() {
		newTime = time;
		ghostUpdateTimer = newTime - oldTime;
		if(ghostUpdateTimer > 100.0) {
			ghostUpdateCounter++;
			ghostUpdateTimer = 0;
			oldTime = time;
			newTime = time;
			thisClient.sendUpdateGhostPosition(p1.getPosition(), cam.getViewDirection(), p1.getVector(), p1.getHealth(), p1.getBoosting(), p1.getHurt());
		}
	}

	public void createNPC(Point3D pos1, int id1, Point3D pos2, int id2, Point3D pos3, int id3, Point3D pos4, int id4) {
		npc1 = new UFO(id1);
		npc1.setLocation(pos1);
		Matrix3D mat = npc1.getLocalTranslation();
		mat.translate(pos1.getX(), pos1.getY(), pos1.getZ());
		npc1.setWorldTranslation(mat);
		this.addGameWorldObject(npc1);
		
		EnemyHealthBar eh = new EnemyHealthBar(100,npc1,p1);
		addGameWorldObject(eh);
		npc1.setHealthBar(eh);
		
		float mass3 = 5.0f;
		float[] size = {1.0f, 1.0f, 1.0f};
		npcP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass3, npc1.getWorldTransform().getValues(), size);
		npcP.setBounciness(0.0f);
		npcP.setFriction(0.0f);
		npcP.setDamping(0.01f, 0.01f);
		npc1.setPhysicsObject(npcP);
		
		npc2 = new UFO(id2);
		npc2.setLocation(pos2);
		mat = npc2.getLocalTranslation();
		mat.translate(pos2.getX(), pos2.getY(), pos2.getZ());
		npc2.setWorldTranslation(mat);
		this.addGameWorldObject(npc2);
		
		EnemyHealthBar eh2 = new EnemyHealthBar(100,npc2,p1);
		addGameWorldObject(eh2);
		npc2.setHealthBar(eh2);
		
		npcP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass3, npc2.getWorldTransform().getValues(), size);
		npcP.setBounciness(0.0f);
		npcP.setFriction(0.0f);
		npcP.setDamping(0.01f, 0.01f);
		npc2.setPhysicsObject(npcP);
		
		npc3 = new UFO(id3);
		npc3.setLocation(pos3);
		mat = npc3.getLocalTranslation();
		mat.translate(pos3.getX(), pos3.getY(), pos3.getZ());
		npc3.setWorldTranslation(mat);
		this.addGameWorldObject(npc3);
		
		EnemyHealthBar eh3 = new EnemyHealthBar(100,npc3,p1);
		addGameWorldObject(eh3);
		npc3.setHealthBar(eh3);
		
		npcP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass3, npc3.getWorldTransform().getValues(), size);
		npcP.setBounciness(0.0f);
		npcP.setFriction(0.0f);
		npcP.setDamping(0.01f, 0.01f);
		npc3.setPhysicsObject(npcP);
		
		npc4 = new UFO(id4);
		npc4.setLocation(pos4);
		mat = npc4.getLocalTranslation();
		mat.translate(pos4.getX(), pos4.getY(), pos4.getZ());
		npc4.setWorldTranslation(mat);
		this.addGameWorldObject(npc4);
		
		EnemyHealthBar eh4 = new EnemyHealthBar(100,npc4,p1);
		addGameWorldObject(eh4);
		npc4.setHealthBar(eh4);
		
		npcP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass3, npc4.getWorldTransform().getValues(), size);
		npcP.setBounciness(0.0f);
		npcP.setFriction(0.0f);
		npcP.setDamping(0.01f, 0.01f);
		npc4.setPhysicsObject(npcP);
	}

	public void checkForAvatarNear(Point3D pos, int id) {
		if(!dead) {
			float distance = (float) pos.distanceTo(p1.getPosition());
			if(distance < 10.0) {
				this.thisClient.sendNPCNear(true, id, distance);
			} else {
				this.thisClient.sendNPCNear(false, id, distance);
			}
		}
	}
	
	public void checkForAvatarMediumNear(Point3D pos, int id) {
		if(!dead) {
			float distance = (float) pos.distanceTo(p1.getPosition());
			if(distance < 30.0) {
				this.thisClient.sendNPCMediumNear(true, id, distance);
			} else {
				this.thisClient.sendNPCMediumNear(false, id, distance);
			}
		}
	}

	public void makeNPCBig(int id) {
		if(id==1) {
			npc1.getBig();
		} else if(id==2) {
			npc2.getBig();
		} else if(id==3) {
			npc3.getBig();
		} else if(id==4) {
			npc4.getBig();
		}
	}
	
	public void makeNPCSmall(int id) {
		System.out.println("SMALL");
		if(id==1) {
			npc1.getSmall();
		} else if(id==2) {
			npc2.getSmall();
		} else if(id==3) {
			npc3.getSmall();
		} else if(id==4) {
			npc4.getSmall();
		}
	}

	public void updateNPCLocation(int id) {
		if(id==1) {
			updateNPCLocation(npc1);
		} else if(id==2) {
			updateNPCLocation(npc2);
		} else if(id==3) {
			updateNPCLocation(npc3);
		} else if(id==4) {
			updateNPCLocation(npc4);
		}
	}
	//overloaded method
	private void updateNPCLocation(UFO npc) {
		if(npc!=null) {
			Point3D pos = new Point3D(npc.getWorldTranslation().getCol(3));
			npc.setLocation(pos);
			thisClient.updateNPCLocation(pos, npc.getID());
			npc.getHealthBar().update(npc.getHealth());
		}
	}

	public void lookAtAvatar(UUID ghostID, int id) {
		if(id==1) {
			lookAtAvatar(ghostID, npc1);
		} else if(id==2) {
			lookAtAvatar(ghostID, npc2);
		} else if(id==3) {
			lookAtAvatar(ghostID, npc3);
		} else if(id==4) {
			lookAtAvatar(ghostID, npc4);
		}
	}

	private void lookAtAvatar(UUID ghostID, UFO npc) {
		if(ghostID.equals(thisClient.getID()) && !dead) {
			npc.lookAtMe(p1.getPosition());
			createNPCBullet(npc);
			thisClient.sendNPCAttackMessage(npc.getID());
		} else {
			Iterator<GhostAvatar> itr = thisClient.getGhosts().iterator();
			while(itr.hasNext()) {
				GhostAvatar g = itr.next();
				if(ghostID.equals(g.getID())) {
					npc.lookAtMe(g.getPosition());
				}
			}
		}
	}

	public void updateNPC(Point3D pos, int id) {
		if(id==1) {
			updateNPCLocation(npc1);
		} else if(id==2) {
			updateNPCLocation(npc2);
		} else if(id==3) {
			updateNPCLocation(npc3);
		} else if(id==4) {
			updateNPCLocation(npc4);
		}
	}
	//overloaded method
	private void updateNPC(Point3D pos, UFO npc) {
		if(npc!=null) {
			npc.setLocation(pos);
			Matrix3D mat = npc.getLocalTranslation();
			mat.translate(pos.getX(), pos.getY(), pos.getZ());
			npc.setWorldTranslation(mat);
			//npc.getHealthBar().update(npc.getHealth());
		}
	}

	public Tank getPlayer() {
		return p1;
	}

	public void removeNPC(int id) {
		UFO npc = getNPC(id);
		npc.setDead(true);
		this.removeGameWorldObject(npc.getHealthBar());
		this.removeGameWorldObject(npc);
	}
	
	public void hurtNPC(int id) {
		UFO npc = getNPC(id);
		npc.setNPCTexture("hurt");
	}

	public void npcNotHurt(int id) {
		UFO npc = getNPC(id);
		npc.setNPCTexture("skin");
	}
	
	public void setTeam(String s) {
		team = s;
	}
	
	public UFO getNPC(int id) {
		if(id==1) {
			return npc1;
		} else if(id==2) {
			return npc2;
		} else if(id==3) {
			return npc3;
		} else {
			return npc4;
		}
	}
}
