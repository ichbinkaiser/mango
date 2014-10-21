package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class AI implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Snake> snakelist = new ArrayList<Snake>();
	Food target = null;
    Snake snake;
    boolean alive = true;
    Random rnd = new Random();
    int sensitivity = 50;
    Point turnpoint = new Point();

	AI(GameActivity gameActivity, ArrayList<Snake> snakelist, Snake snake)
	{
		this.gameactivity = gameActivity;
		this.snakelist = snakelist;
        this.snake = snake;
        turnpoint.set(snake.position.x, snake.position.y);
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
            if (gameactivity.food.size() > 0)
            {
                int foodpriority = gameactivity.canvasheight + gameactivity.canvaswidth;
                int currentfoodpriority;
                for (int foodcounter = 0; foodcounter < gameactivity.food.size(); foodcounter++)
                {
                    currentfoodpriority = Math.abs(snake.position.x - gameactivity.food.get(foodcounter).position.x) + Math.abs(snake.position.y - gameactivity.food.get(foodcounter).position.y);
                    if (currentfoodpriority < foodpriority)
                    {
                        foodpriority = currentfoodpriority;
                        target = gameactivity.food.get(foodcounter);
                    }
                }

                if (snake.direction == Snake.GOING_UP || snake.direction == Snake.GOING_DOWN)
                {
                    if (Math.abs(target.position.y - snake.position.y) <= gameactivity.headsize * 2)
                    {
                        if (target.position.x < snake.position.x)
                            snake.setDirection(Snake.GOING_LEFT);
                        else if (target.position.x > snake.position.x)
                            snake.setDirection(Snake.GOING_RIGHT);
                    }
                }

                else
                {
                    if (Math.abs(target.position.x - snake.position.x) <= gameactivity.headsize * 2)
                    {
                        if (target.position.y > snake.position.y)
                            snake.setDirection(Snake.GOING_DOWN);
                        else if (target.position.y < snake.position.y)
                            snake.setDirection(Snake.GOING_UP);
                    }
                }
            }

            if (gameactivity.food.indexOf(target) < 0 && rnd.nextInt(100) == 0)
                snake.setDirection(rnd.nextInt(3));

            for (int snakecounter = 0; snakecounter < gameactivity.snakes.size(); snakecounter++)
            {
                Snake currentsnake = gameactivity.snakes.get(snakecounter);
                SnakeBody lastbodysegment = snake.bodysegments.get(snake.bodysegments.size() - 1);
                for (int bodysegmentcounter = 0; bodysegmentcounter < currentsnake.bodysegments.size(); bodysegmentcounter++)
                {
                    SnakeBody currentbodysegment = currentsnake.bodysegments.get(bodysegmentcounter);
                    if (currentbodysegment != lastbodysegment && snake.detectCollision(currentbodysegment, sensitivity, gameactivity.headsize * 2) && snake.getMoved(turnpoint) > snake.speed * 5)
                    {
                        if (snake.direction == Snake.GOING_UP || snake.direction == Snake.GOING_DOWN)
                        {
                            if (rnd.nextBoolean())
                                snake.setDirection(Snake.GOING_LEFT);
                            else
                                snake.setDirection(Snake.GOING_RIGHT);
                        }

                        else
                        {

                        if (rnd.nextBoolean())
                            snake.setDirection(Snake.GOING_UP);
                        else
                            snake.setDirection(Snake.GOING_DOWN);
                        }
                        turnpoint.set(snake.position.x, snake.position.y);
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
}