package ichbinkaiser.mango;

import android.graphics.Point;
import android.util.Log;

import java.util.Random;

final class Food implements Runnable
{
    Point position;
    GameActivity gameActivity;
    boolean exists = true;

    Food(GameActivity gameActivity, Point position)
    {
        this.gameActivity = gameActivity;
        this.position = position;
        start();
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.setName("Food");
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
            gameActivity.shockWave.add(new Shock_WAVE(position, Shock_WAVE.waveType.SMALL_WAVE));
            gameActivity.food.remove(this);
        }
    }

    public void eat()
    {
        exists = false;
        gameActivity.food.remove(this);
    }
}


