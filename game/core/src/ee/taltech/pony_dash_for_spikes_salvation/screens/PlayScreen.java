package ee.taltech.pony_dash_for_spikes_salvation.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.pony_dash_for_spikes_salvation.Main;
import ee.taltech.pony_dash_for_spikes_salvation.Player;
import ee.taltech.pony_dash_for_spikes_salvation.ai.NPC;
import ee.taltech.pony_dash_for_spikes_salvation.items.*;
import ee.taltech.pony_dash_for_spikes_salvation.packets.PacketPlayerExitedGame;
import ee.taltech.pony_dash_for_spikes_salvation.scenes.Hud;
import ee.taltech.pony_dash_for_spikes_salvation.sprites.PonySprite;
import ee.taltech.pony_dash_for_spikes_salvation.tools.B2WorldCreator;
import ee.taltech.pony_dash_for_spikes_salvation.tools.WorldContactListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayScreen implements Screen {
    private final Main game;
    private final Hud hud;
    private final TextureAtlas atlas;
    private static final int WIDTH = 620;
    private static final int HEIGHT = 408;
    private static final float PPM = 100f; // pixels per meter
    private final OrthographicCamera gameCam;
    private final Viewport gamePort;

    private int ponyId;

    // Tiled
    final TmxMapLoader mapLoader;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    // Box2d muutujad
    private final World world;
    private final Box2DDebugRenderer b2dr;
    private PonySprite player;
    private final Texture cherry;
    private final Texture apple;
    private final Texture speechBubble;
    private Skin skin;
    private final BitmapFont font;

    // Power-ups
    Map<List<Integer>, InteractiveTileObject> powerUps = new HashMap<>();


    //Sound
    private Music music;
    /**
     * Gets ppm.
     *
     * @return the ppm
     */
    public static float getPPM() {
        return PPM;
    }

    public Music getMusic() {
        return music;
    }

    /**
     * Instantiates a new Play screen.
     * Temporarily has body defining and collision.
     *
     * @param game the game
     */
    public PlayScreen(Main game){
        this.game = game;
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(WIDTH / PPM, HEIGHT / PPM, gameCam);

        atlas = new TextureAtlas("pony_sprites.pack");
        ponyId = game.getPlayerSpriteId();

        // Loading map
        mapLoader = new TmxMapLoader();
        map  = mapLoader.load("Pony_dash_for_spike_salvation_map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map,1f / PPM);

        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        player = new PonySprite(world, this, game.getMyPlayer(), ponyId);
        game.getMyPlayer().setSprite(player);

        cherry = new Texture("Game Assets/cherry.png");
        apple = new Texture("Game Assets/apple.png");
        speechBubble = new Texture("speech_bubble.png");
        skin = new Skin(Gdx.files.internal("Skin/terramotherui/terra-mother-ui.json"));
        Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
        font = labelStyle.font;
        font.getData().setScale(0.65f); // Change scale of font

        // collision types
        world.setContactListener(new WorldContactListener());

        hud = new Hud(game.getBatch());
        new B2WorldCreator(world, map, game, hud, powerUps);

        music = game.getManager().get("Game Assets/Mlp Gameloft Background Music Extended.mp3", Music.class);
        music.setLooping(true);
        music.play();
    }

    public void updatePonyIdAndSprite(int ponyId) {
        this.ponyId = ponyId;
        player = new PonySprite(world, this, game.getMyPlayer(), ponyId);
        game.getMyPlayer().setSprite(player);
    }

    /**
     * Create new sprite.
     *
     * @param player the player
     */
    public void createNewSprite(Player player) {
        PonySprite sprite = new PonySprite(world, this, player, ponyId);
        player.setSprite(sprite);
    }

    /**
     * Gets atlas.
     *
     * @return the atlas
     */
    public TextureAtlas getAtlas() {
        return atlas;
    }

    /**
     * Handle input and define movements.
     */
    public void handleInput() {
        if (Duration.between(game.getMyPlayer().getCaptureTime(), LocalDateTime.now()).toMillis() > (5000)) { // Player is captured for 5 seconds and cannot move
            float xVelocity = player.getB2body().getLinearVelocity().x;

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && (player.getCurrentState().equals("run")
                    || player.getCurrentState().equals("standing"))) {
                setCorrectJumpingHeight();
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && xVelocity <= 2) {
                setCorrectSpeedRight();
            }

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && xVelocity >= -2) {
                setCorrectSpeedLeft();
            }

            // If button is not pressed down, moving stops
            if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                player.getB2body().setLinearVelocity(0, player.getB2body().getLinearVelocity().y);
            }
        }
        // Check if Esc is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exitGame();
        }

        // Update players position
        updatePlayerPosition("normal");
    }

    private void setCorrectJumpingHeight() {
        if (Duration.between(game.getMyPlayer().getGotCherryTime(), LocalDateTime.now()).toMillis() > (20000)) {
            player.getB2body().applyLinearImpulse(new Vector2(0, 4.5f), player.getB2body().getWorldCenter(), true);
        } else {
            // Give extra jumping height for 20 seconds
            player.getB2body().applyLinearImpulse(new Vector2(0, 7f), player.getB2body().getWorldCenter(), true);
        }
    }

    private void setCorrectSpeedRight() {
        if (Duration.between(game.getMyPlayer().getGotAppleTime(), LocalDateTime.now()).toMillis() > (20000)) {
            player.getB2body().applyLinearImpulse(new Vector2(0.1f, 0), player.getB2body().getWorldCenter(), true);
        } else {
            // Give extra speed with apple for 20 seconds
            player.getB2body().applyLinearImpulse(new Vector2(1f, 0), player.getB2body().getWorldCenter(), true);
        }
    }

    private void setCorrectSpeedLeft() {
        if (Duration.between(game.getMyPlayer().getGotAppleTime(), LocalDateTime.now()).toMillis() > (20000)) {
            player.getB2body().applyLinearImpulse(new Vector2(-0.1f, 0), player.getB2body().getWorldCenter(), true);
        } else {
            // Give extra speed with apple for 20 seconds
            player.getB2body().applyLinearImpulse(new Vector2(-1f, 0), player.getB2body().getWorldCenter(), true);
        }
    }

    private void exitGame() {
        game.setScreen(game.getMenuScreen());
        Player myPlayer = game.getMyPlayer();
        PacketPlayerExitedGame packet = new PacketPlayerExitedGame();
        packet.setGameId(myPlayer.getGameID());
        packet.setId(myPlayer.getId());
        game.sendPacketToServer(packet);
        Gdx.app.exit();
    }

    public void updatePlayerPosition(String situation) {
        Player myPlayer = game.getMyPlayer();
        if (situation.equals("normal")) {
            float box2DX = player.getB2body().getPosition().x;
            float box2DY = player.getB2body().getPosition().y;
            myPlayer.setX(box2DX);
            myPlayer.setY(box2DY);
            myPlayer.setTiledX(Math.round(box2DX * PPM));
            myPlayer.setTiledY(Math.round(box2DY * PPM));
        } else if (situation.equals("spikes2")) { // Player touched spikes in second part of map
            myPlayer.setTiledX(1135 * 16);
            myPlayer.setTiledY(26 * 16);
            myPlayer.setX((float) myPlayer.getTiledX() / 100);
            myPlayer.setY((float) myPlayer.getTiledY() / 100);
            player.getB2body().setTransform(new Vector2(myPlayer.getX(), myPlayer.getY()), player.getB2body().getAngle());
        } else { // Player touched spikes in third part of map
            myPlayer.setTiledX(2378 * 16);
            myPlayer.setTiledY(47 * 16);
            myPlayer.setX((float) myPlayer.getTiledX() / 100);
            myPlayer.setY((float) myPlayer.getTiledY() / 100);
            player.getB2body().setTransform(new Vector2(myPlayer.getX(), myPlayer.getY()), player.getB2body().getAngle());
        }
    }

    /**
     * Update screen.
     *
     * @param dt the dt
     */
    public void update(float dt) {
        hud.update(dt);
        if (!game.getMyPlayer().isTeleporting2() || !game.getMyPlayer().isTeleporting3()) {
            handleInput();
        }
        player.update(dt);
        updateAllPlayers(dt);

        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / PPM;
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / PPM;

        float cameraX = MathUtils.clamp(game.getMyPlayer().getX(), gameCam.viewportWidth / 2, mapWidth - gameCam.viewportWidth / 2);
        float cameraY = MathUtils.clamp(game.getMyPlayer().getY(), gameCam.viewportHeight / 2, mapHeight - gameCam.viewportHeight / 2);

        gameCam.position.set(cameraX, cameraY, 0);
        world.step(1 / 60f, 6, 2);
        gameCam.update();
        renderer.setView(gameCam);
    }

    /**
     * Update all players.
     *
     * @param dt the dt
     */
    public void updateAllPlayers(float dt) {
        Map<Integer, Player> playerMap = game.getPlayers();
        for (Map.Entry<Integer, Player> set : playerMap.entrySet()) {
            set.getValue().getSprite().update(dt);
        }
    }

    @Override
    public void show() {
        //Will use later
    }

    /**
     * Render the play-screen (background, world and players).
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor((float)0.941, (float)0.698, (float)0.784, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        b2dr.render(world, gameCam.combined);
        renderer.render();
        // b2dr.render(world, gameCam.combined); // renders box2drender lines
        game.getBatch().begin(); // Opens window

        // Check if the player is captured
        boolean isCaptured = Duration.between(game.getMyPlayer().getCaptureTime(), LocalDateTime.now()).toMillis() <= 5000;

        if (isCaptured) {
            // Calculate the position for the speech bubble
            float bubbleX = player.getB2body().getPosition().x + 270;
            float bubbleY = player.getB2body().getPosition().y + 230;

            // Draw the speech bubble
            game.getBatch().draw(speechBubble, bubbleX, bubbleY, 80, 40);

            font.setColor(Color.BLACK);
            float textX = bubbleX + 10f;
            float textY = bubbleY + 30f;

            // Draw the text
            font.draw(game.getBatch(), "*spilling tea*", textX, textY);
        }

        if (game.getMyPlayer().isTeleporting2()) {
            updatePlayerPosition("spikes2"); // Teleport the player
            game.getMyPlayer().setTeleporting2(false); // Reset the teleporting flag
        } else if (game.getMyPlayer().isTeleporting3()) {
            updatePlayerPosition("spikes3"); // Teleport the player
            game.getMyPlayer().setTeleporting3(false); // Reset the teleporting flag
        }

        update(delta);

        game.getBatch().setProjectionMatrix(gameCam.combined);
        // game.getBatch().setProjectionMatrix(hud.stage.getCamera().combined); // Renders the game-cam

        renderNPCs();
        renderAllPlayers();
        player.draw(game.getBatch());

        if (Duration.between(game.getMyPlayer().getGotCherryTime(), LocalDateTime.now()).toMillis() <= (20000)) {
            game.getBatch().draw(cherry, (float) (gameCam.position.x + 2.5), (float) (gameCam.position.y + 1.6), 0.3F, 0.3F);
        }
        if (Duration.between(game.getMyPlayer().getGotAppleTime(), LocalDateTime.now()).toMillis() <= (20000)) {
            game.getBatch().draw(apple, (float) (gameCam.position.x - 2.8), (float) (gameCam.position.y + 1.6), 0.3F, 0.3F);
        }

        game.getBatch().end();

        hud.stage.draw();
        game.sendPositionInfoToServer();
    }

    private void renderAllPlayers() {
        for (Map.Entry<Integer, Player> entry : game.getPlayers().entrySet()) {
            Player currentPlayer = entry.getValue();
            if (currentPlayer.getSprite() != null) {
                currentPlayer.getSprite().update(Gdx.graphics.getDeltaTime());
                currentPlayer.getSprite().draw(game.getBatch());
            }
        }
    }

    private void renderNPCs() {
        for (NPC npc: game.getBots()) {
            npc.update(Gdx.graphics.getDeltaTime());
            npc.draw(game.getBatch());
        }
    }

    /**
     * Delete power-up from map when someone else has taken it.
     * @param x coordinate of power-up
     * @param y coordinate of power-up
     */
    public void deletePowerUp(float x, float y) {
        Filter filter = new Filter();
        filter.categoryBits = Main.COLLECTED_BIT;
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(Math.round(x));
        coordinates.add(Math.round(y));
        powerUps.get(coordinates).getFixture().setFilterData(filter);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(16);
        layer.getCell((int)(x * PlayScreen.getPPM() / 16), (int)(y * PlayScreen.getPPM() / 16)).setTile(null);
    }

    /**
     * Updates the size of the viewport.
     *
     * @param width of viewport
     * @param height of viewport
     */
    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }


    @Override
    public void pause() {
        //Will use later
    }

    /**
     * Gets map.
     *
     * @return the map
     */
    public TiledMap getMap() {
        return map;
    }

    /**
     * Gets world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    @Override
    public void resume() {
        //Will use later
    }

    @Override
    public void hide() {
        //Will use later
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
        music.dispose();
        skin.dispose();
        font.dispose();
    }
}
