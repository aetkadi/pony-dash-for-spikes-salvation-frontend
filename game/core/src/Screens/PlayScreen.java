package Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.pony_dash_for_spikes_salvation.pony_dash_for_spikes_salvation_game;

public class PlayScreen implements Screen {
    private final pony_dash_for_spikes_salvation_game game;
    public static Texture texture = null; // ajutine
    private final OrthographicCamera gameCam;
    private final Viewport gamePort;

    public PlayScreen(pony_dash_for_spikes_salvation_game game){
        this.game = game;
        texture = new Texture("twilight.png");
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(1920, 1080, gameCam);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor((float)0.941, (float)0.698, (float)0.784, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(gameCam.combined); // Renderdab pildi kaameraga kaasa
        game.batch.begin(); // Opens window
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
