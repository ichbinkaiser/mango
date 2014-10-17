package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Snake implements Runnable
{
    final static int GOING_UP = 0, GOING_RIGHT = 1, GOING_DOWN = 2, GOING_LEFT = 3;
	GameActivity gameactivity;
	Point position = new Point();
	Point pposition = new Point(); // previous position
	Random rnd = new Random();
	boolean collide = false; // has collided
    boolean dead = false; // is dead
    int spawnwave; // spawn wave animation
    int speed = 8, direction; // speed and direction;
    int length = 100, currentlength; //snake length

    ArrayList<SnakeBody> bodysegments = new ArrayList<SnakeBody>();

	Snake(GameActivity gameActivity, ArrayList<Snake> snake)
	{
		this.gameactivity = gameActivity;
        direction = rnd.nextInt(3);

        switch (direction)
        {
            case GOING_UP:
                position.x = rnd.nextInt((gameActivity.canvaswidth / 3) + (gameActivity.canvaswidth / 3));
                position.y = gameActivity.canvasheight;
                break;
            case GOING_RIGHT:
                position.x = 0;
                position.y = rnd.nextInt((gameActivity.canvasheight / 3) + (gameActivity.canvasheight / 3));
                break;
            case GOING_DOWN:
                position.x = rnd.nextInt((gameActivity.canvaswidth / 3) + (gameActivity.canvaswidth / 3));
                position.y = 0;
                break;
            case GOING_LEFT:
                position.x = gameActivity.canvaswidth;
                position.y = rnd.nextInt((gameActivity.canvasheight / 3) + (gameActivity.canvasheight / 3));
                break;
        }
        bodysegments.add(new SnakeBody(position, direction, bodysegments));
		start();
	}

	private boolean checkCollision(Point object) // head collision detection
	{
	//	return (((object.x <= getPosition().x + gameactivity.getBallSize() - 1) && (object.x >= getPosition().x - gameactivity.getBallSize() - 1) && ((object.y <= getPosition().y + gameactivity.getBallSize() - 1) && (object.y >= getPosition().y - gameactivity.getBallSize() - 1))));
	return true;
    }

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Snake");
		thread.start();
	}

	public void run()
	{
		while((gameactivity.running) && (!dead))
		{
			pposition.x = position.x;
			pposition.y = position.y;
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
                pposition.x = position.x;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.x > gameactivity.canvaswidth) // head has reached right wall
            {
                position.x = 0;
                pposition.x = position.x;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.y > gameactivity.canvasheight) // head has reached bottom wall
            {
                position.y = 0;
                pposition.y = position.y;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            else if (position.y < 0)
            {
                position.y = gameactivity.canvasheight;
                pposition.y = position.y;
                bodysegments.add(new SnakeBody(position, direction, bodysegments));
            }

            currentlength = 0;
            for (int bodysegmentscounter = 0; bodysegmentscounter < bodysegments.size(); bodysegmentscounter++)
            {
                currentlength += bodysegments.get(bodysegmentscounter).getLength();
            }

            if (currentlength > length)
                bodysegments.get(0).trim(currentlength - length);

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
	}

    public void setDirection(int direction)
    {
        if (((this.direction == GOING_LEFT) || (this.direction == GOING_RIGHT)) && ((direction == GOING_UP) || (direction == GOING_DOWN)))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction, bodysegments));
        }
        else if (((this.direction == GOING_DOWN) || (this.direction == GOING_UP)) && ((direction == GOING_LEFT) || (direction == GOING_RIGHT)))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction, bodysegments));
        }
    }
}