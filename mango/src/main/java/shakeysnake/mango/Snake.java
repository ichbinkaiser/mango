package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Snake implements Runnable
{
    final static byte GOING_UP = 0, GOING_RIGHT = 1, GOING_DOWN = 2, GOING_LEFT = 3;
	private GameActivity gameactivity;
	private Point position = new Point();
	private Point pposition = new Point(); // previous position
	private Random rnd = new Random();
	private boolean collide = false; // has collided
    private boolean dead = false; // is dead
    private int spawnwave; // spawn wave animation
    private byte speed = 8, direction; // speed and direction;
    private short length = 100, currentlength; //snake length

    private ArrayList<SnakeBody> bodysegments = new ArrayList<SnakeBody>();

	Snake(GameActivity gameActivity, ArrayList<Snake> snake)
	{
		this.gameactivity = gameActivity;
        direction = (byte)rnd.nextInt(3);

        switch (direction)
        {
            case GOING_UP:
                position.x = rnd.nextInt((gameActivity.getCanvasWidth() / 3) + (gameActivity.getCanvasWidth() / 3));
                position.y = gameActivity.getCanvasHeight();
                break;
            case GOING_RIGHT:
                position.x = 0;
                position.y = rnd.nextInt((gameActivity.getCanvasHeight() / 3) + (gameActivity.getCanvasHeight() / 3));
                break;
            case GOING_DOWN:
                position.x = rnd.nextInt((gameActivity.getCanvasWidth() / 3) + (gameActivity.getCanvasWidth() / 3));
                position.y = 0;
                break;
            case GOING_LEFT:
                position.x = gameActivity.getCanvasWidth();
                position.y = rnd.nextInt((gameActivity.getCanvasHeight() / 3) + (gameActivity.getCanvasHeight() / 3));
                break;
        }
        bodysegments.add(new SnakeBody(position, direction));
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
		while((gameactivity.isRunning()) && (!dead))
		{
			pposition.x = position.x;
			pposition.y = position.y;

            switch (direction)
            {
                case GOING_UP:
                    position.y -= speed;
                    bodysegments.get(bodysegments.size() -1).getStartPoint().y = position.y;
                    break;
                case GOING_RIGHT:
                    position.x += speed;
                    bodysegments.get(bodysegments.size() -1).getStartPoint().x = position.x;
                    break;
                case GOING_DOWN:
                    position.y += speed;
                    bodysegments.get(bodysegments.size() -1).getStartPoint().y = position.y;
                    break;
                case GOING_LEFT:
                    position.x -= speed;
                    bodysegments.get(bodysegments.size() -1).getStartPoint().x = position.x;
                    break;
            }

			if (spawnwave > 0) // spawn_wave animation
			{
				gameactivity.getShockwave().add(new Shockwave(position, 1));
				spawnwave--;
			}

			if (position.x < 0) // head has reached left wall
            {
                position.x = gameactivity.getCanvasWidth();
                pposition.x = position.x;
                bodysegments.add(new SnakeBody(position, direction));
            }

            else if (position.x > gameactivity.getCanvasWidth()) // head has reached right wall
            {
                position.x = 0;
                pposition.x = position.x;
                bodysegments.add(new SnakeBody(position, direction));
            }

            else if (position.y > gameactivity.getCanvasHeight()) // head has reached bottom wall
            {
                position.y = 0;
                pposition.y = position.y;
                bodysegments.add(new SnakeBody(position, direction));
            }

            else if (position.y < 0)
            {
                position.y = gameactivity.getCanvasHeight();
                pposition.y = position.y;
                bodysegments.add(new SnakeBody(position, direction));
            }

            currentlength = 0;
            for (int bodysegmentscounter = 0; bodysegmentscounter < bodysegments.size(); bodysegmentscounter++)
            {
                currentlength += bodysegments.get(bodysegmentscounter).getLength();
            }

            if (currentlength > length);
                if ((bodysegments.get(0).trim(currentlength - length)) && (bodysegments.size() > 1))
                {
                    bodysegments.remove(0);
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
	}

    public void setDirection(byte direction)
    {
        if (((this.direction == GOING_LEFT) || (this.direction == GOING_RIGHT)) && ((direction == GOING_UP) || (direction == GOING_DOWN)))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction));
        }
        else if (((this.direction == GOING_DOWN) || (this.direction == GOING_UP)) && ((direction == GOING_LEFT) || (direction == GOING_RIGHT)))
        {
            this.direction = direction;
            bodysegments.add(new SnakeBody(position, direction));
        }
    }

	public Point getPosition() 
	{
		return position;
	}

	public boolean isDead()
	{
		return dead;
	}

    public ArrayList<SnakeBody> getBody()
    {
        return bodysegments;
    }
}