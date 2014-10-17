package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;
import java.util.ArrayList;

final class AI implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Snake> snake = new ArrayList<Snake>();
	Point target = new Point(); // top snake threat

	AI(GameActivity gameActivity, ArrayList<Snake> snake)
	{
		this.gameactivity = gameActivity;
		this.snake = snake;
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		target.x = gameactivity.getCanvasWidth() / 2;

		while(gameactivity.isRunning()) // AI Thread
		{
			target.y = 0;

			try
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("AI", e.toString());
			}
		}
	}
}