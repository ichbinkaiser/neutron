package ichbinkaiser.neutron.core;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import ichbinkaiser.neutron.activity.GameActivity;
import ichbinkaiser.neutron.activity.GameActivity.GameScreen;

public class GameSurfaceThread extends Thread {
    GameActivity gameActivity;
    SurfaceHolder surfaceHolder;
    GameScreen gameScreen;

    public GameSurfaceThread(GameActivity gameActivity, SurfaceHolder holder, GameScreen gameScreen) {
        this.gameActivity = gameActivity;
        setName("SurfaceView");
        surfaceHolder = holder;
        this.gameScreen = gameScreen;
        start();
    }

    public void run() {
        Canvas canvas = null;
        while (gameActivity.isRunning()) {
            try {
                canvas = surfaceHolder.lockCanvas(null);
                gameScreen.screenDraw(canvas);
            } catch (NullPointerException e) {
                Log.e(this.gameActivity.getLocalClassName(), e.toString());
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}