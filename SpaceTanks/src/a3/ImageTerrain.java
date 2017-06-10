package a3;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.display.IDisplaySystem;
import sage.scene.state.RenderState;
import sage.scene.state.TextureState;
import sage.terrain.AbstractHeightMap;
import sage.terrain.HillHeightMap;
import sage.terrain.ImageBasedHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class ImageTerrain {
	
	private ImageBasedHeightMap myHeightMap;
	private TerrainBlock imageTerrain;
	private TextureState groundState;
	private Texture groundTexture;
	
	public ImageTerrain(IDisplaySystem display) {
		myHeightMap = new ImageBasedHeightMap("textures/height.png");
		imageTerrain = createTerBlock(myHeightMap);
		
		groundTexture = TextureManager.loadTexture2D("textures/ground.jpg");
		groundTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		groundState = (TextureState)display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		groundState.setTexture(groundTexture,0);
		groundState.setEnabled(true);
		
		imageTerrain.setRenderState(groundState);
	}
	
	private TerrainBlock createTerBlock(AbstractHeightMap heightMap) {
		float heightScale = 0.05f;
		Vector3D terrainScale = new Vector3D(1, heightScale, 1);
		int terrainSize = heightMap.getSize();
		float cornerHeight = heightMap.getTrueHeightAtPoint(0, 0) * heightScale;
		Point3D terrainOrigin = new Point3D(0, -cornerHeight, 0);
		String name = "Terrain:" + heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale, heightMap.getHeightData(), terrainOrigin);
		
		return tb;
	}
	
	public TerrainBlock getTerrain() {
		return imageTerrain;
	}
}
