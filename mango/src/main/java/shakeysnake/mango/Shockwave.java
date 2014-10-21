package shakeysnake.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.Random;

final class Shockwave 
{
    final static int EXTRA_SMALL_WAVE = 0, SMALL_WAVE = 1, MEDIUM_WAVE = 2, LARGE_WAVE = 3, FOODSPAWN_WAVE = 4;
	Point position = new Point();
	int life; // animation index life
	int type; // shockwave type
    GameActivity gameActivity;
    Random rnd = new Random();
	
	Shockwave(Point position, int type)
	{
		switch (type)
		{
            case EXTRA_SMALL_WAVE:
                life = 11;
                break;
            case SMALL_WAVE:
                life = 21;
                break;
            case MEDIUM_WAVE:
                life = 128;
                break;
            case LARGE_WAVE:
                life = 252;
                break;
		}
		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}

    Shockwave(GameActivity gameActivity)
    {
        this.type = FOODSPAWN_WAVE;
        this.gameActivity = gameActivity;
        position.set(rnd.nextInt(gameActivity.canvaswidth - (gameActivity.headsize * 2) + gameActivity.headsize), rnd.nextInt(gameActivity.canvasheight - (gameActivity.headsize * 2) + gameActivity.headsize));
        life = 252;
    }


    public int getLife()
    {
        switch (type)
        {
            case EXTRA_SMALL_WAVE:
            case SMALL_WAVE:
                return life -= 1;
            case MEDIUM_WAVE:
            case LARGE_WAVE:
                return life -= 4;
            case FOODSPAWN_WAVE:
                if (life < 5)
                    gameActivity.food.add(new Food(gameActivity, position));

                return life -= 4;
            default:
                return 0;
        }
    }
}
