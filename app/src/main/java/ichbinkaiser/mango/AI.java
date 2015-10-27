package ichbinkaiser.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.List;
import java.util.Random;

/**
 * This is the AI opponent class. Each AI runs on its own thread and controls a 'Snake' object
 */
final class AI implements Runnable
{
	private GameActivity gameActivity;
	private List<Snake> snakeList;
	private Food target = null; // the food the snake will target to eat
    private Snake snake; // snake object this AI controls
    private Random rnd = new Random();
    private Point turnPoint = new Point();
    private boolean[] avoid = new boolean[]{false, false, false, false};

	/**
	 * Constructs the AI object
	 * @param gameActivity Must always be the calling GameActivity
	 * @param snakeList List of all the running snake objects in the GameActivity
	 * @param snake The snake object it will control
	 */
	AI(GameActivity gameActivity, List<Snake> snakeList, Snake snake)
	{
		this.gameActivity = gameActivity;
		this.snakeList = snakeList;
        this.snake = snake;
        turnPoint.set(snake.position.x, snake.position.y); // set current snake direction
        start();
	}

	private void start() // starts the AI thread
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	/**
	 * This is the main AI thread, this is where the AI targeting and obstacle avoidance logic runs
	 */
	public void run() // AI thread
	{
		while(gameActivity.running && snake.alive) // stop when snake is dead or game is no longer running
		{
            if (snake.getMoved(turnPoint) > snake.speed) // if the snake has moved from its last position
            {
                avoid = new boolean[]{false, false, false, false}; // set directions to avoid to clear (should always be 4 elements)
                for (Snake currentSnake : snakeList) // scan through all snakes in the game
                {
                    SnakeBody lastBodySegment = snake.bodySegments.get(snake.bodySegments.size() - 1); // get the last body segment of the snake
                    for (SnakeBody currentBodySegment : currentSnake.bodySegments) // scan through all the body segments of the current target snake if a possible collision is detected
                    {
                        for (Direction currentDirection : Direction.values()) // scan through all possible directions for possible collision
                        {
                            if (!avoid[currentDirection.ordinal()]) // if the direction is not already in the snakes avoid list, check for possible collision
                                avoid[currentDirection.ordinal()] = (currentBodySegment != lastBodySegment && snake.detectCollision(currentBodySegment, 50, gameActivity.headSize * 2, currentDirection));
                                // avoid the current direction if the current body segment is last body segment and a possible collision is detected
                        }
                    }
                }
            }

            switch (snake.direction) // perform evasive maneuvers if possible collisions are detected
            {
                case UP:
                    if (avoid[Direction.UP.ordinal()]) // if possible collision is detected going up, avoid up
                        turnTo(snake.direction);
                    break;
                case DOWN:
                    if (avoid[Direction.DOWN.ordinal()]) // if possible collision is detected going down, avoid down
                        turnTo(snake.direction);
                    break;
                case LEFT:
                    if (avoid[Direction.LEFT.ordinal()]) // if possible collision is detected going left, avoid left
                        turnTo(snake.direction);
                    break;
                case RIGHT:
                    if (avoid[Direction.RIGHT.ordinal()]) // if possible collision is detected going right, avoid right
                        turnTo(snake.direction);
            }

            if (snake.getMoved(turnPoint) > snake.speed && gameActivity.food.size() > 0) // if the snake has moved and food is on the field
            {
                int foodPriority = gameActivity.canvasHeight + gameActivity.canvasWidth; // set food priority to maximum possible value
                int currentFoodPriority;
                for (Food currentFood : gameActivity.food) // scan through all the food to find a priority target
                {
                    currentFoodPriority = Math.abs(snake.position.x - currentFood.position.x) + Math.abs(snake.position.y - currentFood.position.y);
                    if (currentFoodPriority < foodPriority)
                    {
                        foodPriority = currentFoodPriority;
                        target = currentFood;
                    }
                }

                if (snake.direction == Direction.UP || snake.direction == Direction.DOWN)
                {
	                // if snake is on the same Y axis as the target food or (((food is less than halfway across the screen vertically and ((snake direction is going up and target is on the left) or (snake direction is going down and target is on the right)))
                    if (isSameAxis(target.position.y, snake.position.y) || (isNear(target.position.y, snake.position.y, gameActivity.canvasHeight) && (snake.direction == Direction.UP && target.position.y > snake.position.y) || (snake.direction == Direction.DOWN && target.position.y < snake.position.y)))
                    {
                        if (target.position.x < snake.position.x)
                        {
	                        turnTo(Direction.LEFT, false);
                        }
                        else if (target.position.x > snake.position.x)
                        {
	                        turnTo(Direction.RIGHT, false);
                        }
                    }
                }

                else
                {
                    // if snake is on the same Y axis as the target food or (((food is less than halfway across the screen horizontally and ((snake direction is going left and target is down) or (snake direction is going right and target is up)))
                    if (isSameAxis(target.position.x, snake.position.x) || (isNear(target.position.x, snake.position.x, gameActivity.canvasWidth) && (snake.direction == Direction.LEFT && target.position.x > snake.position.x) || (snake.direction == Direction.RIGHT && target.position.x < snake.position.x)))
                    {
	                    turnTo(target.position.y > snake.position.y ? Direction.DOWN : Direction.UP, false);
	                    // if target position is DOWN, go DOWN else go UP
                    }
                }
            }

            // else if there's no food, just move around randomly
            else if (snake.getMoved(turnPoint) > snake.speed && gameActivity.food.indexOf(target) < 0 && rnd.nextInt(100) == 0)
            {
                turnTo(Direction.values()[rnd.nextInt(3)], true);
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

	private boolean isSameAxis(int targetValue, int snakeValue)
	{
		return Math.abs(targetValue - snakeValue) <= gameActivity.headSize * 2;
	}

	private boolean isNear(int targetValue, int snakeValue, int referenceValue)
	{
		return Math.abs(targetValue - snakeValue) < referenceValue / 2;
	}

	/**
	 * Change direction based on current direction
	 * @param currentDirection Snakes current direction of travel
	 */
    private void turnTo(Direction currentDirection)
    {
        if (currentDirection == Direction.UP || currentDirection == Direction.DOWN) // if snake's current direction is horizontal
	        if (rnd.nextBoolean())
		        turnTo(Direction.LEFT, true);
	        else
		        turnTo(Direction.RIGHT, true);
        else
		    if (rnd.nextBoolean())
			    turnTo(Direction.UP, true);
		    else
			    turnTo(Direction.DOWN, true);
    }

	/**
	 * Turns the controlled snake to a particular direction
	 * @param direction Direction the snake will turn to
	 * @param hasAlternate Snake has alternate direction to turn to in case passed direction can cause a collision
	 */
    private void turnTo(Direction direction, boolean hasAlternate)
    {
        switch (direction)
        {
            case UP:
                if (!avoid[Direction.UP.ordinal()]) // if up has no possible collision
                {
                    snake.setDirection(Direction.UP);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Direction.DOWN.ordinal()])
                {
                    snake.setDirection(Direction.DOWN);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case DOWN:
                if (!avoid[Direction.DOWN.ordinal()])
                {
                    snake.setDirection(Direction.DOWN);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Direction.UP.ordinal()])
                {
                    snake.setDirection(Direction.UP);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case LEFT:
                if (!avoid[Direction.LEFT.ordinal()])
                {
                    snake.setDirection(Direction.LEFT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Direction.RIGHT.ordinal()])
                {
                    snake.setDirection(Direction.RIGHT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
                break;
            case RIGHT:
                if (!avoid[Direction.RIGHT.ordinal()])
                {
                    snake.setDirection(Direction.RIGHT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }

                else if (hasAlternate && !avoid[Direction.LEFT.ordinal()])
                {
                    snake.setDirection(Direction.LEFT);
                    turnPoint.set(snake.position.x, snake.position.y);
                }
        }
    }
}