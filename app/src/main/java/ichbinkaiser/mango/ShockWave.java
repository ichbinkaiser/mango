package ichbinkaiser.mango;

import android.graphics.Point;

import java.util.Random;

final class ShockWave
{
    enum waveType
    {
	    EXTRA_SMALL_WAVE,
	    SMALL_WAVE,
	    MEDIUM_WAVE,
	    LARGE_WAVE,
	    FOOD_SPAWN_WAVE
    }

	Point position = new Point();
	int life; // animation index life
	waveType type; // shockWave type
	
	ShockWave(Point position, waveType type)
	{
        setType(type);
		this.position.x = position.x;
		this.position.y = position.y;
	}

    ShockWave(int x, int y)
    {
        setType(waveType.SMALL_WAVE);
        this.position.x = x;
        this.position.y = y;
    }

    ShockWave(GameActivity gameActivity)
    {
        Random rnd = new Random();
        this.type = waveType.FOOD_SPAWN_WAVE;
        position.set(rnd.nextInt(gameActivity.canvasWidth - (gameActivity.headSize * 2) + gameActivity.headSize), rnd.nextInt(gameActivity.canvasHeight - (gameActivity.headSize * 2) + gameActivity.headSize));
        life = 252;
    }

    private void setType(waveType type)
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
        if (type == waveType.EXTRA_SMALL_WAVE || type == waveType.SMALL_WAVE)
            return life -= 1;
        else
            return life -= 4;
    }
}
