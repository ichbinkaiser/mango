package ichbinkaiser.mango.entity;

import android.graphics.Point;
import android.util.Log;

import java.util.Random;

import ichbinkaiser.mango.activity.GameActivity;

/**
 * This is the food object that the snakes eat. It runs on its own thread.
 */
public class Food implements Runnable
{
    Point position;
    GameActivity gameActivity;
    boolean exists = true;

    /**
     *
     * @param gameActivity must always be the calling GameActivity
     * @param position location where the food will appear
     */
    public Food(GameActivity gameActivity, Point position)
    {
        this.gameActivity = gameActivity;
        this.position = position;
        start();
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.setName("Food");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run()
    {
        Random rnd = new Random();
        try
        {
            Thread.sleep((5 + rnd.nextInt(15)) * 1000);
        }

        catch (InterruptedException e)
        {
            e.printStackTrace();
            Log.e("Food", e.toString());
        }

        if (exists)
        {
            gameActivity.getShockWave().add(new ShockWave(position, WaveType.SMALL_WAVE));
            gameActivity.getFood().remove(this);
        }
    }

    public void eat()
    {
        exists = false;
        gameActivity.getFood().remove(this);
    }

    public Point getPosition() {
        return position;
    }

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public boolean isExists() {
        return exists;
    }
}


