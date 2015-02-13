package ichbinkaiser.mango;

import android.graphics.Point;

import java.util.Random;

final class Shockwave 
{
    final static int EXTRA_SMALL_WAVE = 0, SMALL_WAVE = 1, MEDIUM_WAVE = 2, LARGE_WAVE = 3, FOODSPAWN_WAVE = 4;
	Point position = new Point();
	int life; // animation index life
	int type; // shockwave type
	
	Shockwave(Point position, int type)
	{
        setType(type);
		this.position.x = position.x;
		this.position.y = position.y;
	}

    Shockwave(int x, int y, int type)
    {
        setType(type);
        this.position.x = x;
        this.position.y = y;
    }

    Shockwave(GameActivity gameActivity)
    {
        Random rnd = new Random();
        this.type = FOODSPAWN_WAVE;
        position.set(rnd.nextInt(gameActivity.canvaswidth - (gameActivity.headsize * 2) + gameActivity.headsize), rnd.nextInt(gameActivity.canvasheight - (gameActivity.headsize * 2) + gameActivity.headsize));
        life = 252;
    }

    private void setType(int type)
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
    }

    public int getLife()
    {
        if (type == EXTRA_SMALL_WAVE || type == SMALL_WAVE)
            return life -= 1;
        else
            return life -= 4;
    }
}
