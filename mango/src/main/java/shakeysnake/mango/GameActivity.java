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
	int headsize;
	boolean sologame = true;
	int players;
	static SoundManager soundmanager = new SoundManager(); // global sound manager
	ArrayList<Popup> popup = new ArrayList<Popup>(); // popup messages array list
	ArrayList<Shockwave> shockwave = new ArrayList<Shockwave>(); // shockwave animation list
	ArrayList<Snake> snakes = new ArrayList<Snake>(); // whiteball array list
	PowerManager.WakeLock wakelock;
	GameSurfaceThread gamesurfacethread;
	SurfaceHolder surfaceholder;
	SensorManager sensormanager;
	Sensor orientation;
	float rollangle = 0;
	Random rnd = new Random();

	AI ai; // set AI
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
					gamesurfacethread.setFlag(false);
					soundmanager.playSound(7, 1);
					showScore();
				}
				
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
		Paint pint = new Paint(); // snakes paint
        Paint snakepaint = new Paint();
		Paint scoretext = new Paint();
		Paint popuptext = new Paint();
		Paint balltrail = new Paint(); // snakes tail
		Paint circlestrokepaint = new Paint();
		Paint centerlinepaint = new Paint();
		Paint shadowpaint = new Paint();
		Paint buzzballpaint = new Paint();
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

            snakes.add(new Snake(GameActivity.this, snakes));

			back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), canvaswidth, canvasheight, true);
			Log.i(getLocalClassName(), "Portrait background created");

			Typeface myType = Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL);
			scoretext.setColor(Color.WHITE);
			scoretext.setTypeface(myType);

			popuptext.setTypeface(myType);
			popuptext.setTextAlign(Align.CENTER);

			centerlinepaint.setStrokeWidth(3);

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

			pint.setColor(Color.WHITE);
            snakepaint.setColor(Color.WHITE);
            snakepaint.setStyle(Paint.Style.STROKE);
            snakepaint.setStrokeWidth(3);
			circlestrokepaint.setStyle(Paint.Style.STROKE);

			if (snakes.size() == 1)
				Log.i(getLocalClassName(), "Snake initialized");
			else
				Log.i(getLocalClassName(), "Balls initialized");

			globalthread = new GlobalThread();
			globalthread.start();
		}

		public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
		{
			gamesurfacethread.setFlag(false);
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
			gamesurfacethread.setFlag(true);
			gamesurfacethread.start();
			Log.i(getLocalClassName(), "Surface created");
		}
		
		public boolean onTouchEvent(MotionEvent event)
		{
			int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN)
            {
                downtouch.x = (int)event.getX();
                downtouch.y = (int)event.getY();
            }

			if (action == MotionEvent.ACTION_UP)
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

			for (int snakecounter = snakes.size() - 1; snakecounter >= 0; snakecounter--) // snakes drawer
			{
                Snake currentsnake = snakes.get(snakecounter);
				if (currentsnake.isDead())
					snakes.remove(snakecounter);
				else
                {
                    canvas.drawCircle(currentsnake.getPosition().x, currentsnake.getPosition().y, headsize, pint);
                    for (int bodycounter = 0; bodycounter < currentsnake.getBody().size(); bodycounter++)
                    {
                        SnakeBody currentsegment = currentsnake.getBody().get(bodycounter);
                        canvas.drawLine(currentsegment.getStartPoint().x, currentsegment.getStartPoint().y, currentsegment.getEndPoint().x, currentsegment.getEndPoint().y, snakepaint);
                    }
                }
			}

            for (int shockwavecounter = shockwave.size() - 1; shockwavecounter >= 0; shockwavecounter--)  // shockwave drawer
            {
                Shockwave currentshockwave = shockwave.get(shockwavecounter);
                if (currentshockwave.getLife() > 0) // bump animation
                {
                    int currentshockwavelife = currentshockwave.getLife();
                    switch (currentshockwave.getType())
                    {
                        case 0: // is small wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 23,255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,11 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 1: // is medium wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 12, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(2);
                            canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,21 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 2: // is big wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 2, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,128 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 3: // is super big animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,252 - currentshockwavelife, circlestrokepaint);
                            break;
                    }
                }
                else
                    shockwave.remove(shockwavecounter); // remove dead shockwave
            }

            for (int popupcounter = popup.size() - 1; popupcounter >= 0; popupcounter--) // popup text drawer
            {
                if (popup.get(popupcounter).getCounter() > 0) // if popup text is to be shown
                {
                    popuptext.setColor(Color.argb(popup.get(popupcounter).getCounter(), 255, 255, 255)); // text fade effect
                    Popup currentpopup = popup.get(popupcounter);

                    switch (popup.get(popupcounter).getType())
                    {
                        case 0: // scoreup
                            canvas.drawText(extralifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y - currentpopup.getCounter(), popuptext);
                            break;
                        case 1: // lose life
                            canvas.drawText(lostlifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y + currentpopup.getCounter(), popuptext);
                            break;
                        case 2: // solo
                            canvas.drawText(extralifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y + currentpopup.getCounter(), popuptext);
                            break;
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