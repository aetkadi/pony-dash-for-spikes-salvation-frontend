package ee.taltech.pony_dash_for_spikes_salvation.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.pony_dash_for_spikes_salvation.Main;
import ee.taltech.pony_dash_for_spikes_salvation.sprites.PonySprite;

public class PlayScreen implements Screen {
    private final Main game;
    // public static Texture texture = null; // ajutine
    private final OrthographicCamera gameCam;
    private final Viewport gamePort;

    // Tiled
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // Box2d muutujad
    private World world;
    private Box2DDebugRenderer b2dr;
    private PonySprite player;

    public PlayScreen(Main game){
        this.game = game;
        // texture = new Texture("twilight_sparkle_one.png");
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(Main.WIDTH / Main.PPM, Main.HEIGHT / Main.PPM, gameCam);

        // Loading map
        mapLoader = new TmxMapLoader();
        map  = mapLoader.load("testmap..tmx");
        renderer = new OrthogonalTiledMapRenderer(map,1 / Main.PPM);

        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        player = new PonySprite(this.getWorld());

        // Ajutine, tuleb hiljem ümber tõsta
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        // Platform Layer 10
        MapLayer collisionLayerPlatform= map.getLayers().get(10);

        for (MapObject object : collisionLayerPlatform.getObjects()) {
            if (object instanceof PolygonMapObject) {
                Polygon rect = ((PolygonMapObject) object).getPolygon();

                Rectangle boundingRectangle = rect.getBoundingRectangle();

                bdef.position.set((boundingRectangle.x + boundingRectangle.width / 2) / Main.PPM,
                        (boundingRectangle.y + boundingRectangle.height / 2) / Main.PPM);

                shape.setAsBox(boundingRectangle.width / 2 / Main.PPM, boundingRectangle.height / 2 / Main.PPM);

                body = world.createBody(bdef);
                fdef.shape = shape;
                body.createFixture(fdef);
            }
        }
    }

    public  void hanelInput(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
            player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2)
            player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2)
            player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
    }

    public void update(float dt) {
        hanelInput(dt);
        gameCam.position.x = player.b2body.getPosition().x;
        gameCam.position.y = player.b2body.getPosition().y;
        world.step(1/60f, 6, 2);
        gameCam.update();
        renderer.setView(gameCam);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        update(delta);
        Gdx.gl.glClearColor((float)0.941, (float)0.698, (float)0.784, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        b2dr.render(world, gameCam.combined);
        renderer.render();
        b2dr.render(world, gameCam.combined);
        game.batch.setProjectionMatrix(gameCam.combined); // Renderdab pildi kaameraga kaasa
        game.batch.begin(); // Opens window
        game.makeAllPlayersMove();
        game.makePlayerMove();
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
