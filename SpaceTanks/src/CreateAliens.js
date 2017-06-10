var JavaPackages = new JavaImporter(
 Packages.sage.scene.Group,
 Packages.graphicslib3D.Matrix3D,
 Packages.sage.model.loader.OBJLoader,
 Packages.sage.scene.TriMesh,
 Packages.sage.texture.Texture,
 Packages.sage.texture.TextureManager);
with(JavaPackages)
{
var rootNode = new Group();

	for(var count = x; count > 0; count--)
	{
		var enemyTex = TextureManager.loadTexture2D("textures/enemyUV.png");
		var c = new TriMesh();
		var loader = new OBJLoader();
		c = loader.loadModel("models/enemy.obj");
		c.setTexture(enemyTex);
		
		var mcl = new Matrix3D();
		mcl = c.getLocalTranslation();
		mcl.translate(Math.random()*100,0.5,Math.random()*100);
		c.setLocalTranslation(mcl);
	
		rootNode.addChild(c);
	}
}