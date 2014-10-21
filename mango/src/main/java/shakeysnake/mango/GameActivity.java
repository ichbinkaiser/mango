package shakeysnake.mango;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends Activity implements SensorEventListener
{
	int canvasheight;
	int canvaswidth;
	int midpoint; // canvas horizontal midpoint
	int life = 50;
	int gamescore = 0;
	int ballcount = 5;
	boolean running = true; // game running
	boolean gameover = false;
	static String score;
	int headsize; // snakelist head
	boolean sologame = true;
	int players;
	static SoundManager soundmanager = new SoundManager(); // global sound manager
	ArrayList<Popup> popup = new ArrayList<Popup>(); // popup messages array list
	ArrayList<Shockwave> shockwave = new ArrayList<Shockwave>(); // shockwave animation list
	ArrayList<Snake> snakes = new ArrayList<Snake>(); // snakes list
    ArrayList<Food> food = new ArrayList<Food>(); // food list
    ArrayList<AI> AI = new ArrayList<AI>(); // snakes list
	PowerManager.WakeLock wakelock;
	GameSurfaceThread gamesurfacethread;
	SurfaceHolder surfaceholder;
	SensorManager sensormanager;
	Sensor orientation;
	float rollangle = 0;
	Random rnd = new Random();
    Point uptouch, downtouch; // player uptouch position

	@Override 
	public void onCreate(Bundle savedinstancestate) 
	{
		super.onCreate(savedinstancestate);

		Log.i(getLocalClassName(), "Activity started");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		this.wakelock.acquire();

		if (getIntent().getIntExtra("BALLS_COUNT", -1) > 0)
			ballcount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve snakes count from main activity
		
		sologame = getIntent().getBooleanExtra("SOLO_GAME", false);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		if (sologame)
			players = 1;
		else
			players = 2;

		LinearLayout lLayout = new LinearLayout(getApplicationContext());
		MyDraw mydraw = new MyDraw(getApplicationContext()); // set SurfaceView
		lLayout.addView(mydraw);
		setContentView(lLayout);
		
		sensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientation = sensormanager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensormanager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(getLocalClassName(), "Activity stopped");
		this.wakelock.release();
	}

	public void onPause()
	{
		super.onPause();
		finish(); // disallow pausing
		sensormanager.unregisterListener(this);
	}

	public void onResume()
	{
		super.onResume();
		sensormanager.registerListener(this,  orientation, SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void showScore() // show score screen
	{
		Intent scoreIntent = new Intent(this, ScoreActivity.class);
		scoreIntent.putExtra(score, Integer.toString(gamescore));
		startActivity(scoreIntent);
		Log.i(getLocalClassName(), "Game ended");
		finish();
	}

	public void doShake(int time) // phone vibrate
	{
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	private class GlobalThread implements Runnable
	{
        GlobalThread()
        {
            start();
        }

		public void start()
		{
			Thread thread = new Thread(this);
			thread.setName("GlobalThread");
			thread.start();
		}
		
		public void run()
		{
			while (running)
			{
				if ((life < 0) && (!gameover)) // game over condition
				{
					running =  false;
					gameover = true;
					soundmanager.playSound(7, 1);
					showScore();
				}

                if (rnd.nextInt(100) == 0)
                    shockwave.add(new Shockwave(GameActivity.this));
				
				try
				{
					Thread.sleep(40);
				}

				catch (InterruptedException e)
				{
					e.printStackTrace();
					Log.e("GlobalThread", e.toString());
				}
			}
		}
	}

	public class MyDraw extends SurfaceView implements Callback
	{
		Bitmap back; // background

		String[] extralifestrings = new String[] {"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
		String[] lostlifestrings = new String[] {"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};
		String[] bumpstrings = new String[] {"BUMP!", "TOINK!", "BOINK!", "BAM!", "WABAM!"};
		String[] zoomstrings = new String[] {"ZOOM!", "WOOSH!", "SUPER MODE!", "ZOOMBA!", "WARPSPEED!"};
		Paint foodpaint = new Paint(); // food paint
        Paint snakejointpaint = new Paint();
        Paint snakepaint = new Paint();
		Paint scoretext = new Paint();
		Paint popuptext = new Paint();
		Paint circlestrokepaint = new Paint();
		GlobalThread globalthread;

		public MyDraw(Context context)
		{
			super(context);

			surfaceholder = getHolder();
			surfaceholder.addCallback(this);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			canvaswidth = metrics.widthPixels;
			canvasheight = metrics.heightPixels;
			midpoint = canvaswidth / 2;

            downtouch = new Point();
            uptouch = new Point();

			back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), canvaswidth, canvasheight, true);
			Log.i(getLocalClassName(), "Portrait background created");

			Typeface myType = Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL);
			scoretext.setColor(Color.WHITE);
			scoretext.setTypeface(myType);

			popuptext.setTypeface(myType);
			popuptext.setTextAlign(Align.CENTER);

			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
			{
				popuptext.setTextSize(8);
				scoretext.setTextSize(9);
				headsize = 2;
				Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
			}

			else
			{
				popuptext.setTextSize(12);
				scoretext.setTextSize(15);
				headsize = 4;
				Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
			}

            foodpaint.setColor(Color.WHITE);
            snakepaint.setColor(Color.WHITE);
            snakepaint.setStyle(Paint.Style.STROKE);
            snakepaint.setStrokeWidth(headsize * 2);
			circlestrokepaint.setStyle(Paint.Style.STROKE);

			if (snakes.size() == 1)
				Log.i(getLocalClassName(), "Snake initialized");
			else
				Log.i(getLocalClassName(), "Balls initialized");

            snakes.add(new Snake(GameActivity.this));
            snakes.add(new Snake(GameActivity.this));
            snakes.add(new Snake(GameActivity.this));
            snakes.add(new Snake(GameActivity.this));
			globalthread = new GlobalThread();
		}

		public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
		{
			running = false ;
			Log.i(getLocalClassName(), "Surface destroyed");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
		{
			Log.i(getLocalClassName(), "Surface changed");
		}

		public void surfaceCreated(SurfaceHolder holder) // when user enters game
		{
			gamesurfacethread = new GameSurfaceThread(GameActivity.this, holder, this);
			Log.i(getLocalClassName(), "Surface created");
		}
		
		public boolean onTouchEvent(MotionEvent event)
		{
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                downtouch.x = (int)event.getX();
                downtouch.y = (int)event.getY();
            }

			if (event.getAction() == MotionEvent.ACTION_UP)
			{
                uptouch.x = (int)event.getX();
                uptouch.y = (int)event.getY();

                if (Math.abs(downtouch.x - uptouch.x) > (Math.abs(downtouch.y - uptouch.y))) // go with X axis movement
                {
                    if (downtouch.x > uptouch.x)
                        snakes.get(0).setDirection(Snake.GOING_LEFT);
                    else
                        snakes.get(0).setDirection(Snake.GOING_RIGHT);
                }
                else
                    if (downtouch.y > uptouch.y)
                        snakes.get(0).setDirection(Snake.GOING_UP);
                    else
                        snakes.get(0).setDirection(Snake.GOING_DOWN);
			}
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawBitmap(back, 0, 0, null);

            for (int foodcounter = 0; foodcounter < food.size(); foodcounter++)
            {
                canvas.drawCircle(food.get(foodcounter).position.x, food.get(foodcounter).position.y, headsize, foodpaint);
            }

			for (int snakecounter = 0; snakecounter < snakes.size(); snakecounter++) // snakes drawer
			{
                Snake currentsnake = snakes.get(snakecounter);
				if (currentsnake.dead)
					snakes.remove(snakecounter);
				else
                {
                    if (snakecounter == 0)
                    {
                        snakejointpaint.setColor(Color.GRAY);
                        snakepaint.setColor(Color.GRAY);
                    }
                    else
                    {
                        snakejointpaint.setColor(Color.WHITE);
                        snakepaint.setColor(Color.WHITE);
                    }

                    canvas.drawCircle(currentsnake.position.x, currentsnake.position.y, headsize, snakejointpaint);
                    for (int bodycounter = 0; bodycounter < currentsnake.bodysegments.size(); bodycounter++)
                    {
                        SnakeBody currentsegment = currentsnake.bodysegments.get(bodycounter);
                        canvas.drawLine(currentsegment.startpoint.x, currentsegment.startpoint.y, currentsegment.endpoint.x, currentsegment.endpoint.y, snakepaint);
                        canvas.drawCircle(currentsegment.endpoint.x, currentsegment.endpoint.y, headsize, snakejointpaint);
                    }
                }
			}

            for (int shockwavecounter = 0; shockwavecounter < shockwave.size(); shockwavecounter++)  // shockwave drawer
            {
                Shockwave currentshockwave = shockwave.get(shockwavecounter);
                if (currentshockwave.getLife() > 0) // bump animation
                {
                    int currentshockwavelife = currentshockwave.getLife();
                    switch (currentshockwave.type)
                    {
                        case Shockwave.EXTRA_SMALL_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 23, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y, 11 - currentshockwavelife, circlestrokepaint);
                            break;
                        case Shockwave.SMALL_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 12, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(2);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y, 21 - currentshockwavelife, circlestrokepaint);
                            break;
                        case Shockwave.MEDIUM_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 2, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y, 128 - currentshockwavelife, circlestrokepaint);
                            break;
                        case Shockwave.LARGE_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y, 252 - currentshockwavelife, circlestrokepaint);
                            break;
                        case Shockwave.FOODSPAWN_WAVE:
                            circlestrokepaint.setColor(Color.argb(252 - currentshockwavelife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y, currentshockwavelife, circlestrokepaint);
                    }
                }
                else
                    shockwave.remove(shockwavecounter); // remove dead shockwave
            }

            for (int popupcounter = 0; popupcounter < popup.size(); popupcounter++) // popup text drawer
            {
                Popup currentpopup = popup.get(popupcounter);
                if (currentpopup.getCounter() > 0) // if popup text is to be shown
                {
                    popuptext.setColor(Color.argb(popup.get(popupcounter).getCounter(), 255, 255, 255)); // text fade effect
                    switch (popup.get(popupcounter).type)
                    {
                        case Popup.SCOREUP:
                            canvas.drawText(extralifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y - currentpopup.getCounter(), popuptext);
                            break;
                        case Popup.LOSELIFE:
                            canvas.drawText(lostlifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y + currentpopup.getCounter(), popuptext);
                            break;
                        case Popup.SOLO:
                            canvas.drawText(extralifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y + currentpopup.getCounter(), popuptext);
                    }
                }
                else
                    popup.remove(popupcounter); // remove dead popup
            }
			
			if (life > 0)
				canvas.drawText("Snake Count: " + Integer.toString(snakes.size()) + " " + "Score: " + Integer.toString(gamescore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasheight - 10, scoretext);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int integer)
	{
		Log.i(getLocalClassName(), "Accuracy changed");
	}

	public void onSensorChanged(SensorEvent event)
	{
		rollangle = event.values[2];
	}
}