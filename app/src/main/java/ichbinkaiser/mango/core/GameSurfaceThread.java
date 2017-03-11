package ichbinkaiser.mango.core;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import ichbinkaiser.mango.activity.GameActivity;
import ichbinkaiser.mango.activity.GameActivity.GameScreen;

public class GameSurfaceThread extends Thread
{
	GameActivity gameActivity;
	SurfaceHolder surfaceHolder;
	GameScreen gameScreen;
	
	public GameSurfaceThread(GameActivity gameActivity, SurfaceHolder holder , GameScreen drawMain)
	{
		this.gameActivity = gameActivity;
		setName("SurfaceView");
		surfaceHolder = holder;
		gameScreen = drawMain;
        start();
	}

	@Override
	public void run()
	{
		Canvas canvas = null;
		while(gameActivity.isRunning())
		{
			try
			{
				canvas = surfaceHolder.lockCanvas(null);
				gameScreen.screenDraw(canvas);
			}

			catch (NullPointerException e)
			{
				Log.e(this.gameActivity.getLocalClassName(), e.toString());
			}

			finally
			{
				if(canvas != null)
					surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
}