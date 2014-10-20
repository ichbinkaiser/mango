package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;
import java.util.ArrayList;
import java.util.Random;

final class AI implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Snake> snakelist = new ArrayList<Snake>();
	Point target = new Point(); // top snakelist threat
    Snake snake;
    boolean alive = true;
    Random rnd = new Random();
    int sensitivity = 20;

	AI(GameActivity gameActivity, ArrayList<Snake> snakelist, Snake snake)
	{
		this.gameactivity = gameActivity;
		this.snakelist = snakelist;
        this.snake = snake;
        start();
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		while(gameactivity.running) // AI Thread
		{
            for (int snakecounter = 0; snakecounter < gameactivity.snakes.size(); snakecounter++)
            {
                Snake currentsnake = gameactivity.snakes.get(snakecounter);
                if (currentsnake != snake)
                {
                    for (int bodysegmentcounter = 0; bodysegmentcounter < currentsnake.bodysegments.size(); bodysegmentcounter++)
                    {
                        SnakeBody currentbodysegment = currentsnake.bodysegments.get(bodysegmentcounter);
                        if (snake.isHitting(currentbodysegment, sensitivity) && snake.isIntersecting(currentbodysegment) || snake.isHeadOn(currentsnake, sensitivity))
                        {
                            evade();
                            break;
                        }
                    }
                }
            }

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

    private void evade()
    {
        if (snake.evaded)
        {
            switch (snake.direction)
            {
                case Snake.GOING_UP:
                case Snake.GOING_DOWN:
                    if (rnd.nextBoolean())
                        snake.setDirection(Snake.GOING_LEFT);
                    else
                        snake.setDirection(Snake.GOING_RIGHT);
                    break;
                case Snake.GOING_LEFT:
                case Snake.GOING_RIGHT:
                    if (rnd.nextBoolean())
                        snake.setDirection(Snake.GOING_UP);
                    else
                        snake.setDirection(Snake.GOING_DOWN);
            }
        snake.evaded = false;
        }
    }
}