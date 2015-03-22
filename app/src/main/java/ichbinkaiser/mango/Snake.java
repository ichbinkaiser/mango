package ichbinkaiser.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Snake implements Runnable
{
    final static int GOING_UP = 0, GOING_RIGHT = 1, GOING_DOWN = 2, GOING_LEFT = 3;
	GameActivity gameactivity;
	Point position = new Point();
    boolean alive = true;
    int spawnwave; // spawn wave animation
    int speed, direction; // speed and direction;
    int length = 100, currentlength; //snakeList length

    ArrayList<SnakeBody> bodysegments = new ArrayList<SnakeBody>();

	Snake(GameActivity gameactivity)
	{
        Random rnd = new Random();
		this.gameactivity = gameactivity;
        speed = 5;
        direction = rnd.nextInt(3);

        switch (direction)
        {
            case GOING_UP:
                position.x = rnd.nextInt((gameactivity.canvaswidth / 3) + (gameactivity.canvaswidth / 3));
                position.y = gameactivity.canvasheight;
                break;
            case GOING_RIGHT:
                position.x = 0;
                position.y = rnd.nextInt((gameactivity.canvasheight / 3) + (gameactivity.canvasheight / 3));
                break;
            case GOING_DOWN:
                position.x = rnd.nextInt((gameactivity.canvaswidth / 3) + (gameactivity.canvaswidth / 3));
                position.y = 0;
                break;
            case GOING_LEFT:
                position.x = gameactivity.canvaswidth;
                position.y = rnd.nextInt((gameactivity.canvasheight / 3) + (gameactivity.canvasheight / 3));
                break;
        }
        bodysegments.add(new SnakeBody(position, direction, bodysegments));
		start();
	}

    public int getMoved(Point position)
    {
        if (direction == GOING_UP || direction == GOING_DOWN)
            return Math.abs(this.position.y - position.y);
        else
            return Math.abs(this.position.x - position.x);
    }

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Snake");
		thread.start();
	}

	public void run()
	{
        if (gameactivity.snakes.size() > 1)
            gameactivity.AI.add(new AI (gameactivity, gameactivity.snakes, this));

		while(gameactivity.running && alive)
		{
            switch (direction)
            {
                case GOING_UP:
                    position.y -= speed;
                    bodysegments.get(bodysegments.size() - 1).startpoint.y = position.y;
                    break;
                case GOING_RIGHT:
                    position.x += speed;
                    bodysegments.get(bodysegments.size() - 1).startpoint.x = position.x;
                    break;
                case GOING_DOWN:
                    position.y += speed;
                    bodysegments.get(bodysegments.size() - 1).startpoint.y = position.y;
                    break;
                case GOING_LEFT:
                    position.x -= speed;
                    bodysegments.get(bodysegments.size() - 1).startpoint.x = position.x;
            }

			if (spawnwave > 0) // spawn_wave animation
			{
				gameactivity.shockwave.add(new Shockwave(position, 1));
				spawnwave--;
			}

			if (position.x < 0) // head has reached left wall
            {
                position.x = gameactivity.canvaswidth;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.x > gameactivity.canvaswidth) // head has reached right wall
            {
                position.x = 0;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.y > gameactivity.canvasheight) // head has reached bottom wall
            {
                position.y = 0;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.y < 0)
            {
                position.y = gameactivity.canvasheight;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            currentlength = 0;
            for (int bodysegmentscounter = 0; bodysegmentscounter < bodysegments.size(); bodysegmentscounter++)
            {
                currentlength += bodysegments.get(bodysegmentscounter).getLength();
            }

            if (currentlength > length)
                bodysegments.get(0).trim(currentlength - length);

            for (int snakecounter = 0; snakecounter < gameactivity.snakes.size(); snakecounter++)
            {
                Snake currentsnake = gameactivity.snakes.get(snakecounter);
                SnakeBody lastbodysegment = bodysegments.get(bodysegments.size() - 1);
                for (int bodysegmentcounter = 0; bodysegmentcounter < currentsnake.bodysegments.size(); bodysegmentcounter++)
                {
                    SnakeBody currentbodysegment = currentsnake.bodysegments.get(bodysegmentcounter);
                    if (currentbodysegment != lastbodysegment && detectCollision(currentbodysegment, gameactivity.headsize, gameactivity.headsize))
                    {
                        gameactivity.doShake(100);
                        if (gameactivity.snakes.get(0) != this) // check if this is the human player
                        {
                            gameactivity.popup.add(new Popup(position, Popup.BOO));
                            dieAnimation();
                        }
                        else
                        {
                            gameactivity.running =  false;
                            gameactivity.soundmanager.playSound(SoundManager.RESTART, 1);
                            gameactivity.showScore();
                        }
                        break;
                    }
                }
            }

            for (int foodcounter = 0; foodcounter < gameactivity.food.size(); foodcounter++)
            {
                Food currentfood = gameactivity.food.get(foodcounter);
                if (Math.abs(position.x - currentfood.position.x) <= gameactivity.headsize * 2 && Math.abs(position.y - currentfood.position.y) <= gameactivity.headsize * 2)
                {
                    gameactivity.food.get(foodcounter).eat();
                    gameactivity.doShake(50);
                    length += 2;

                    gameactivity.popup.add(new Popup(position, Popup.YEY));

                    if (this == gameactivity.snakes.get(0))
                    {
                        gameactivity.gamescore += 2;
                    }
                }
            }

			try
			{
				Thread.sleep(40);
			}

			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("Snake", e.toString());
			}
		}
        gameactivity.snakes.remove(this); // remove this dead snake
	}

    public void setDirection(int direction)
    {
        if ((this.direction == GOING_LEFT || this.direction == GOING_RIGHT) && (direction == GOING_UP || direction == GOING_DOWN))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction, bodysegments));
        }
        else if ((this.direction == GOING_DOWN || this.direction == GOING_UP) && (direction == GOING_LEFT || direction == GOING_RIGHT))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction, bodysegments));
        }
    }

    public boolean detectCollision(SnakeBody bodysegment, int sensitivity, int crossection)
    {
        return collisionDetection(bodysegment, sensitivity, crossection, direction);
    }

    public boolean detectCollision(SnakeBody bodysegment, int sensitivity, int crossection, int direction)
    {
        return collisionDetection(bodysegment, sensitivity, crossection, direction);
    }

    private boolean collisionDetection(SnakeBody bodysegment, int sensitivity, int crossection, int direction) // relative to current snake direction
    {
        switch (direction)
        {
            case GOING_UP:
                if (bodysegment.direction == GOING_UP || bodysegment.direction == GOING_DOWN)
                    return collinearCheck(bodysegment, sensitivity, crossection, direction);
                else
                    return parallelCheck(bodysegment) && position.y < bodysegment.startpoint.y + sensitivity && position.y > bodysegment.startpoint.y;
            case GOING_DOWN:
                if (bodysegment.direction == GOING_UP || bodysegment.direction == GOING_DOWN)
                    return collinearCheck(bodysegment, sensitivity, crossection, direction);
                else
                    return parallelCheck(bodysegment) && position.y > bodysegment.startpoint.y - sensitivity && position.y < bodysegment.startpoint.y;
            case Snake.GOING_LEFT:
                if (bodysegment.direction == GOING_LEFT || bodysegment.direction == GOING_RIGHT)
                    return collinearCheck(bodysegment, sensitivity, crossection, direction);
                else
                    return parallelCheck(bodysegment) && position.x < bodysegment.startpoint.x + sensitivity && position.x > bodysegment.startpoint.x;
            case Snake.GOING_RIGHT:
                if (bodysegment.direction == GOING_LEFT || bodysegment.direction == GOING_RIGHT)
                    return collinearCheck(bodysegment, sensitivity, crossection, direction);
                else
                    return parallelCheck(bodysegment) && position.x > bodysegment.startpoint.x - sensitivity && position.x < bodysegment.startpoint.x;
            default:
                return false;
        }
    }

    private boolean parallelCheck(SnakeBody bodysegment)
    {
        switch (bodysegment.direction)
        {
            case GOING_UP:
                return position.y >= bodysegment.startpoint.y && position.y <= bodysegment.endpoint.y;
            case GOING_DOWN:
                return position.y <= bodysegment.startpoint.y && position.y >= bodysegment.endpoint.y;
            case GOING_LEFT:
                return position.x >= bodysegment.startpoint.x && position.x <= bodysegment.endpoint.x;
            case GOING_RIGHT:
                return position.x <= bodysegment.startpoint.x && position.x >= bodysegment.endpoint.x;
            default:
                return false;
        }
    }

    private boolean collinearCheck(SnakeBody bodysegment, int sensitivity, int width, int direction)
    {
        switch (bodysegment.direction)
        {
            case GOING_UP:
                if (direction == GOING_UP)
                    return Math.abs(bodysegment.startpoint.x - position.x) <= width && position.y <= bodysegment.endpoint.y + sensitivity && position.y >= bodysegment.startpoint.y;
                else
                    return Math.abs(bodysegment.startpoint.x - position.x) <= width && position.y >= bodysegment.startpoint.y - sensitivity && position.y <= bodysegment.endpoint.y;
            case GOING_DOWN:
                if (direction == GOING_UP)
                    return Math.abs(bodysegment.startpoint.x - position.x) <= width && position.y <= bodysegment.startpoint.y + sensitivity && position.y >= bodysegment.endpoint.y;
                else
                    return Math.abs(bodysegment.startpoint.x - position.x) <= width && position.y >= bodysegment.endpoint.y - sensitivity && position.y <= bodysegment.startpoint.y;
            case GOING_LEFT:
                if (direction == GOING_LEFT)
                    return Math.abs(bodysegment.startpoint.y - position.y) <= width && position.x <= bodysegment.endpoint.x + sensitivity && position.x >= bodysegment.startpoint.x;
                else
                    return Math.abs(bodysegment.startpoint.y - position.y) <= width && position.x >= bodysegment.startpoint.x - sensitivity && position.x <= bodysegment.endpoint.x;
            case GOING_RIGHT:
                if (direction == GOING_LEFT)
                    return Math.abs(bodysegment.startpoint.y - position.y) <= width && position.x <= bodysegment.startpoint.x + sensitivity && position.x >= bodysegment.endpoint.x;
                else
                    return Math.abs(bodysegment.startpoint.y - position.y) <= width && position.x >= bodysegment.endpoint.x - sensitivity && position.x <= bodysegment.startpoint.x;
            default:
                return false;
        }
    }

    private void dieAnimation()
    {
        gameactivity.popup.add(new Popup(position, Popup.BUMP));
        for (int bodysegmentcounter = 0; bodysegmentcounter < bodysegments.size(); bodysegmentcounter++)
        {
            SnakeBody currentbodysegment = bodysegments.get(bodysegmentcounter);
            gameactivity.shockwave.add(new Shockwave(currentbodysegment.startpoint, Shockwave.LARGE_WAVE));
            switch (bodysegments.get(bodysegmentcounter).direction)
            {
                case Snake.GOING_DOWN:
                    for (int counter = currentbodysegment.startpoint.y; counter > currentbodysegment.endpoint.y; counter--)
                    {
                        if (counter % 5 == 0)
                            gameactivity.shockwave.add(new Shockwave(currentbodysegment.startpoint.x, counter, Shockwave.SMALL_WAVE));
                    }
                case Snake.GOING_UP:
                    for (int counter = currentbodysegment.startpoint.y; counter < currentbodysegment.endpoint.y; counter++)
                    {
                        if (counter % 5 == 0)
                            gameactivity.shockwave.add(new Shockwave(currentbodysegment.startpoint.x, counter, Shockwave.SMALL_WAVE));
                    }
                case Snake.GOING_LEFT:
                    for (int counter = currentbodysegment.startpoint.x; counter > currentbodysegment.endpoint.x; counter--)
                    {
                        if (counter % 5 == 0)
                            gameactivity.shockwave.add(new Shockwave(counter, currentbodysegment.endpoint.y, Shockwave.SMALL_WAVE));
                    }
                case Snake.GOING_RIGHT:
                    for (int counter = currentbodysegment.startpoint.x; counter < currentbodysegment.endpoint.x; counter++)
                    {
                        if (counter % 5 == 0)
                            gameactivity.shockwave.add(new Shockwave(counter, currentbodysegment.endpoint.y, Shockwave.SMALL_WAVE));
                    }
            }
        }
        alive = false;
    }
}