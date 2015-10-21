package ichbinkaiser.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is the AI opponent class. Each AI runs on its own thread and controls a 'Snake' object
 */
final class AI implements Runnable
{
	private GameActivity gameActivity;
	private List<Snake> snakeList = new ArrayList<>();
	private Food target = null;
    private Snake snake;
    private Random rnd = new Random();
    private int sensitivity = 50;
    private Point turnPoint = new Point();
    private boolean[] avoid = new boolean[]{false, false, false, false};

	/**
	 *
	 * @param gameActivity must always be the calling GameActivity
	 * @param snakeList list of all the running snake objects in the GameActivity
	 * @param snake the snake object it will control
	 */
	AI(GameActivity gameActivity, List<Snake> snakeList, Snake snake)
	{
		this.gameActivity = gameActivity;
		this.snakeList = snakeList;
        this.snake = snake;
        turnPoint.set(snake.position.x, snake.position.y);
        start();
	}

	private void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run() // AI thread
	{
		while(gameActivity.running && snake.alive) // stop when snake is dead or game is no longer running
		{
            if (snake.getMoved(turnPoint) > snake.speed)
            {
                avoid = new boolean[]{false, false, false, false};
                for (Snake currentSnake : gameActivity.snakes)
                {
                    SnakeBody lastBodySegment = snake.bodySegments.get(snake.bodySegments.size() - 1);
                    for (int bodySegmentCounter = 0; bodySegmentCounter < currentSnake.bodySegments.size(); bodySegmentCounter++)
                    {
                        SnakeBody currentBodySegment = currentSnake.bodySegments.get(bodySegmentCounter);

                        for (Snake.Direction currentDirection : Snake.Direction.values())
                        {
                            if (!avoid[currentDirection.ordinal()])
                                avoid[currentDirection.ordinal()] = currentBodySegment != lastBodySegment && snake.detectCollision(currentBodySegment, sensitivity, gameActivity.headSize * 2, currentDirection);
                        }
                    }
                }
            }

            switch (snake.direction)
            {
                case UP:
                    if (avoid[Snake.Direction.UP.ordinal()])
                        turnTo(false);
                    break;
                case DOWN:
                    if (avoid[Snake.Direction.DOWN.ordinal()])
                        turnTo(false);
                    break;
                case LEFT:
                    if (avoid[Snake.Direction.LEFT.ordinal()])
                        turnTo(true);
                    break;
                case RIGHT:
                    if (avoid[Snake.Direction.RIGHT.ordinal()])
                        turnTo(true);
            }

            if (snake.getMoved(turnPoint) > snake.speed
		            && gameActivity.food.size() > 0)
            {
                int foodPriority = gameActivity.canvasHeight + gameActivity.canvasWidth;
                int currentFoodPriority;
                for (Food currentFood : gameActivity.food)
                {
                    currentFoodPriority = Math.abs(snake.position.x - currentFood.position.x) + Math.abs(snake.position.y - currentFood.position.y);
                    if (currentFoodPriority < foodPriority)
                    {
                        foodPriority = currentFoodPriority;
                        target = currentFood;
                    }
                }

                if (snake.direction == Snake.Direction.UP || snake.direction == Snake.Direction.DOWN)
                {
                    if (Math.abs(target.position.y - snake.position.y) <= gameActivity.headSize * 2
		                    || (Math.abs(target.position.y - snake.position.y) < gameActivity.canvasHeight / 2
		                    && (snake.direction == Snake.Direction.UP && target.position.y > snake.position.y)
		                    || (snake.direction == Snake.Direction.DOWN && target.position.y < snake.position.y)))
                    {
                        if (target.position.x < snake.position.x)
                            turnTo(Snake.Direction.LEFT, false);
                        else if (target.position.x > snake.position.x)
                            turnTo(Snake.Direction.RIGHT, false);
                    }
                }

                else
                {
                    if (Math.abs(target.position.x - snake.position.x) <= gameActivity.headSize * 2
		                    || (Math.abs(target.position.x - snake.position.x) < gameActivity.canvasWidth / 2
		                    && (snake.direction == Snake.Direction.LEFT && target.position.x > snake.position.x)
		                    || (snake.direction == Snake.Direction.RIGHT && target.position.x < snake.position.x)))
                    {
                        if (target.position.y > snake.position.y)
                            turnTo(Snake.Direction.DOWN, false);
                        else if (target.position.y < snake.position.y)
                            turnTo(Snake.Direction.UP, false);
                    }
                }
            }

            else if (snake.getMoved(turnPoint) > snake.speed && gameActivity.food.indexOf(target) < 0 && rnd.nextInt(100) == 0)
            {
                turnTo(Snake.Direction.values()[rnd.nextInt(3)], true);
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
                turnTo(Snake.Direction.UP, true);
            else
                turnTo(Snake.Direction.DOWN, true);
        else
            if (rnd.nextBoolean())
                turnTo(Snake.Direction.LEFT, true);
            else
                turnTo(Snake.Direction.RIGHT, true);
    }

    private void turnTo(Snake.Direction direction, boolean hasAlternate)
    {
        switch (direction)
        {
            case UP:
                if (!avoid[Snake.Direction.UP.ordinal()])
                {
                    snake.setDirection(Snake.Direction.UP);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Snake.Direction.DOWN.ordinal()])
                {
                    snake.setDirection(Snake.Direction.DOWN);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case DOWN:
                if (!avoid[Snake.Direction.DOWN.ordinal()])
                {
                    snake.setDirection(Snake.Direction.DOWN);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Snake.Direction.UP.ordinal()])
                {
                    snake.setDirection(Snake.Direction.UP);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case LEFT:
                if (!avoid[Snake.Direction.LEFT.ordinal()])
                {
                    snake.setDirection(Snake.Direction.LEFT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Snake.Direction.RIGHT.ordinal()])
                {
                    snake.setDirection(Snake.Direction.RIGHT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case RIGHT:
                if (!avoid[Snake.Direction.RIGHT.ordinal()])
                {
                    snake.setDirection(Snake.Direction.RIGHT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Snake.Direction.LEFT.ordinal()])
                {
                    snake.setDirection(Snake.Direction.LEFT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
        }
    }
}