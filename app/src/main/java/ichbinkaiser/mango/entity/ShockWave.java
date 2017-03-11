package ichbinkaiser.mango.entity;

import android.graphics.Point;

import java.util.Random;

import ichbinkaiser.mango.activity.GameActivity;

public class ShockWave
{

    Point position = new Point();
	int life; // animation index life
	WaveType type; // shockWave type
	
	public ShockWave(Point position, WaveType type)
	{
        setType(type);
		this.position.x = position.x;
		this.position.y = position.y;
	}

    public ShockWave(int x, int y)
    {
        setType(WaveType.SMALL_WAVE);
        this.position.x = x;
        this.position.y = y;
    }

    public ShockWave(GameActivity gameActivity)
    {
        Random rnd = new Random();
        this.type = WaveType.FOOD_SPAWN_WAVE;
        position.set(rnd.nextInt(gameActivity.getCanvasHeight()- (gameActivity.getHeadSize() * 2) + gameActivity.getHeadSize()),
                rnd.nextInt(gameActivity.getCanvasHeight() - (gameActivity.getHeadSize() * 2) + gameActivity.getHeadSize()));
        life = 252;
    }

    public void setType(WaveType type)
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
        if (type == WaveType.EXTRA_SMALL_WAVE || type == WaveType.SMALL_WAVE)
            return life -= 1;
        else
            return life -= 4;
    }

    public Point getPosition() {
        return position;
    }

    public WaveType getType() {
        return type;
    }
}
