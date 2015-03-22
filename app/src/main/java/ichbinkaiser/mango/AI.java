package ichbinkaiser.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class AI implements Runnable
{
	GameActivity gameActivity;
	ArrayList<Snake> snakeList = new ArrayList<>();
	Food target = null;
    Snake snake;
    Random rnd = new Random();
    int sensitivity = 50;
    Point turnpoint = new Point();
    boolean avoid[] = new boolean[]{false, false, false, false};

	AI(GameActivity gameActivity, ArrayList<Snake> snakeList, Snake snake)
	{
		this.gameActivity = gameActivity;
		this.snakeList = snakeList;
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
		while(gameActivity.running && snake.alive) // AI Thread
		{

            if (snake.getMoved(turnpoint) > snake.speed)
            {
                avoid = new boolean[]{false, false, false, false};
                for (Snake currentSnake : gameActivity.snakes)
                {
                    SnakeBody lastBodySegment = snake.bodysegments.get(snake.bodysegments.size() - 1);
                    for (int bodySegmentCounter = 0; bodySegmentCounter < currentSnake.bodysegments.size(); bodySegmentCounter++)
                    {
                        SnakeBody currentBodySegment = currentSnake.bodysegments.get(bodySegmentCounter);

                        for (int directionCounter = 0; directionCounter < 4; directionCounter++)
                        {
                            if (!avoid[directionCounter])
                                avoid[directionCounter] = currentBodySegment != lastBodySegment && snake.detectCollision(currentBodySegment, sensitivity, gameActivity.headsize * 2, directionCounter);
                        }
                    }
                }
            }

            switch (snake.direction)
            {
                case Snake.GOING_UP:
                    if (avoid[Snake.GOING_UP])
                        turnTo(false);
                    break;
                case Snake.GOING_DOWN:
                    if (avoid[Snake.GOING_DOWN])
                        turnTo(false);
                    break;
                case Snake.GOING_LEFT:
                    if (avoid[Snake.GOING_LEFT])
                        turnTo(true);
                    break;
                case Snake.GOING_RIGHT:
                    if (avoid[Snake.GOING_RIGHT])
                        turnTo(true);
            }

            if (snake.getMoved(turnpoint) > snake.speed && gameActivity.food.size() > 0)
            {
                int foodPriority = gameActivity.canvasheight + gameActivity.canvaswidth;
                int currentfoodpriority;
                for (int foodcounter = 0; foodcounter < gameActivity.food.size(); foodcounter++)
                {
                    currentfoodpriority = Math.abs(snake.position.x - gameActivity.food.get(foodcounter).position.x) + Math.abs(snake.position.y - gameActivity.food.get(foodcounter).position.y);
                    if (currentfoodpriority < foodPriority)
                    {
                        foodPriority = currentfoodpriority;
                        target = gameActivity.food.get(foodcounter);
                    }
                }

                if (snake.direction == Snake.GOING_UP || snake.direction == Snake.GOING_DOWN)
                {
                    if (Math.abs(target.position.y - snake.position.y) <= gameActivity.headsize * 2 || (Math.abs(target.position.y - snake.position.y) < gameActivity.canvasheight / 2 && (snake.direction == Snake.GOING_UP && target.position.y > snake.position.y) || (snake.direction == Snake.GOING_DOWN && target.position.y < snake.position.y)))
                    {
                        if (target.position.x < snake.position.x)
                            turnTo(Snake.GOING_LEFT, false);
                        else if (target.position.x > snake.position.x)
                            turnTo(Snake.GOING_RIGHT, false);
                    }
                }

                else
                {
                    if (Math.abs(target.position.x - snake.position.x) <= gameActivity.headsize * 2 || (Math.abs(target.position.x - snake.position.x) < gameActivity.canvaswidth / 2 && (snake.direction == Snake.GOING_LEFT && target.position.x > snake.position.x) || (snake.direction == Snake.GOING_RIGHT && target.position.x < snake.position.x)))
                    {
                        if (target.position.y > snake.position.y)
                            turnTo(Snake.GOING_DOWN, false);
                        else if (target.position.y < snake.position.y)
                            turnTo(Snake.GOING_UP, false);
                    }
                }
            }

            else if (snake.getMoved(turnpoint) > snake.speed && gameActivity.food.indexOf(target) < 0 && rnd.nextInt(100) == 0)
            {
                turnTo(rnd.nextInt(3), true);
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
        gameActivity.AI.remove(this); //remove this dead AI
	}

    private void turnTo(boolean isVertical)
    {
        if (isVertical)
            if (rnd.nextBoolean())
                turnTo(Snake.GOING_UP, true);
            else
                turnTo(Snake.GOING_DOWN, true);
        else
            if (rnd.nextBoolean())
                turnTo(Snake.GOING_LEFT, true);
            else
                turnTo(Snake.GOING_RIGHT, true);
    }

    private void turnTo(int direction, boolean hasAlternate)
    {
        switch (direction)
        {
            case Snake.GOING_UP:
                if (!avoid[Snake.GOING_UP])
                {
                    snake.setDirection(Snake.GOING_UP);
                    turnpoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[snake.GOING_DOWN]);
                {
                    snake.setDirection(Snake.GOING_DOWN);
                    turnpoint.set(snake.position.x, snake.position.y);
                }
                break;
            case Snake.GOING_DOWN:
                if (!avoid[Snake.GOING_DOWN])
                {
                    snake.setDirection(Snake.GOING_DOWN);
                    turnpoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[snake.GOING_UP]);
                {
                    snake.setDirection(Snake.GOING_UP);
                    turnpoint.set(snake.position.x, snake.position.y);
                }
                break;
            case Snake.GOING_LEFT:
                if (!avoid[Snake.GOING_LEFT])
                {
                    snake.setDirection(Snake.GOING_LEFT);
                    turnpoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[snake.GOING_RIGHT]);
                {
                    snake.setDirection(Snake.GOING_RIGHT);
                    turnpoint.set(snake.position.x, snake.position.y);
                }
                break;
            case Snake.GOING_RIGHT:
                if (!avoid[Snake.GOING_RIGHT])
                {
                    snake.setDirection(Snake.GOING_RIGHT);
                    turnpoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[snake.GOING_LEFT]);
                {
                    snake.setDirection(Snake.GOING_LEFT);
                    turnpoint.set(snake.position.x, snake.position.y);
                }
        }
    }
}