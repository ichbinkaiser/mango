package ichbinkaiser.mango;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameActivity extends Activity implements SensorEventListener
{
	int canvasHeight;
	int canvasWidth;
	int midpoint; // canvas horizontal midpoint
	int gameScore = 0;
	int AICount = 3;
	boolean running = true; // game running
	static String score;
	int headSize; // snakeList head
	boolean soloGame = true;
	static SoundManager soundmanager = new SoundManager(); // global sound manager
	List<Popup> popup = new CopyOnWriteArrayList<>(); // popup messages array list
	List<Shock_WAVE> shockWave = new CopyOnWriteArrayList<>(); // shockwave animation list
	List<Snake> snakes = new CopyOnWriteArrayList<>(); // snakes list
	List<Food> food = new CopyOnWriteArrayList<>(); // food list
	List<AI> AI = new CopyOnWriteArrayList<>(); // snakes list
	PowerManager.WakeLock wakelock;
	GameSurfaceThread gamesurfacethread;
	SurfaceHolder surfaceholder;
	SensorManager sensormanager;
	Sensor orientation;
	float rollAngle = 0;
	Random rnd = new Random();
    Point upTouch, downTouch; // player up touch position

	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i(getLocalClassName(), "Activity started");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		this.wakelock.acquire();
		
		soloGame = getIntent().getBooleanExtra("SOLO_GAME", false);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        if (soloGame)
            AICount = 0;
        else if (getIntent().getIntExtra("AI_COUNT", -1) > 0)
            AICount = getIntent().getIntExtra("AI_COUNT", -1); // retrieve snakes count from main activity

		LinearLayout lLayout = new LinearLayout(getApplicationContext());
		GameScreen gameScreen = new GameScreen(getApplicationContext()); // set SurfaceView
		lLayout.addView(gameScreen);
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

	void showScore() // show score screen
	{
		running = false;
		Intent scoreIntent = new Intent(this, ScoreActivity.class);
		scoreIntent.putExtra(score, Integer.toString(gameScore));
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
                if (rnd.nextInt(100) == 0)
                    shockWave.add(new Shock_WAVE(GameActivity.this));

                if (snakes.size() - 1 < AICount)
                    snakes.add(new Snake(GameActivity.this));
				
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

	public class GameScreen extends SurfaceView implements Callback
	{
		String[] yeystrings = new String[] {"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
		String[] boostrings = new String[] {"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};
		String[] bumpstrings = new String[] {"BUMP!", "TOINK!", "BOINK!", "BAM!", "WABAM!"};
		String[] zoomstrings = new String[] {"ZOOM!", "WOOSH!", "SUPER MODE!", "ZOOMBA!", "WARPSPEED!"};
		Paint foodpaint = new Paint(); // food paint
        Paint snakejointpaint = new Paint();
        Paint snakepaint = new Paint();
		Paint scoretext = new Paint();
		Paint popuptext = new Paint();
		Paint circlestrokepaint = new Paint();
        Paint bgpaint = new Paint();
		GlobalThread globalthread;

		public GameScreen(Context context)
		{
			super(context);

			surfaceholder = getHolder();
			surfaceholder.addCallback(this);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			canvasWidth = metrics.widthPixels;
			canvasHeight = metrics.heightPixels;
			midpoint = canvasWidth / 2;

            downTouch = new Point();
            upTouch = new Point();

			Typeface myType = Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL);
			scoretext.setColor(Color.WHITE);
			scoretext.setTypeface(myType);

			popuptext.setTypeface(myType);
			popuptext.setTextAlign(Align.CENTER);

			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
			{
				popuptext.setTextSize(8);
				scoretext.setTextSize(9);
				headSize = 2;
				Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
			}

			else
			{
				popuptext.setTextSize(12);
				scoretext.setTextSize(15);
				headSize = 4;
				Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
			}

            bgpaint.setColor(Color.BLACK);
            foodpaint.setColor(Color.WHITE);
            snakepaint.setColor(Color.WHITE);
            snakepaint.setStyle(Paint.Style.STROKE);
            snakepaint.setStrokeWidth(headSize * 2);
			circlestrokepaint.setStyle(Paint.Style.STROKE);

            snakes.add(new Snake(GameActivity.this)); // Add player

			if (snakes.size() == 1)
				Log.i(getLocalClassName(), "Snake initialized");
			else
				Log.i(getLocalClassName(), "Snakes initialized");

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
                downTouch.x = (int)event.getX();
                downTouch.y = (int)event.getY();
            }

			if (event.getAction() == MotionEvent.ACTION_UP)
			{
                upTouch.x = (int)event.getX();
                upTouch.y = (int)event.getY();

                if (snakes.size() > 0 && Math.abs(downTouch.x - upTouch.x) > (Math.abs(downTouch.y - upTouch.y))) // go with X axis movement
                {
                    if (downTouch.x > upTouch.x)
                        snakes.get(0).setDirection(Snake.Direction.LEFT);
                    else
                        snakes.get(0).setDirection(Snake.Direction.RIGHT);
                }
                else
                    if (downTouch.y > upTouch.y)
                        snakes.get(0).setDirection(Snake.Direction.UP);
                    else
                        snakes.get(0).setDirection(Snake.Direction.DOWN);
			}
			return true;
		}

		protected void screenDraw(Canvas canvas)
		{
			canvas.drawRect(0, 0, canvasWidth, canvasHeight, bgpaint);

            for (int foodCounter = 0; foodCounter < food.size(); foodCounter++)
            {
                canvas.drawCircle(food.get(foodCounter).position.x, food.get(foodCounter).position.y, headSize, foodpaint);
            }

			for (int snakeCounter = 0; snakeCounter < snakes.size(); snakeCounter++) // snakes drawer
			{
                Snake currentSnake = snakes.get(snakeCounter);
				if (currentSnake.alive)
                {
                    if (snakeCounter == 0)
                    {
                        snakejointpaint.setColor(Color.GRAY);
                        snakepaint.setColor(Color.GRAY);
                    }
                    else
                    {
                        snakejointpaint.setColor(Color.WHITE);
                        snakepaint.setColor(Color.WHITE);
                    }

                    canvas.drawCircle(currentSnake.position.x, currentSnake.position.y, headSize, snakejointpaint);
                    for (int bodyCounter = 0; bodyCounter < currentSnake.bodySegments.size(); bodyCounter++)
                    {
                        SnakeBody currentSegment = currentSnake.bodySegments.get(bodyCounter);
                        canvas.drawLine(currentSegment.startPoint.x, currentSegment.startPoint.y, currentSegment.endpoint.x, currentSegment.endpoint.y, snakepaint);
                        canvas.drawCircle(currentSegment.endpoint.x, currentSegment.endpoint.y, headSize, snakejointpaint);
                    }
                }
			}

            for (int shockWaveCounter = 0; shockWaveCounter < shockWave.size(); shockWaveCounter++)  // shockwave drawer
            {
                Shock_WAVE currentShockWave = shockWave.get(shockWaveCounter);
                if (currentShockWave.getLife() > 0) // bump animation
                {
                    int currentShockWaveLife = currentShockWave.getLife();
                    switch (currentShockWave.type)
                    {
                        case EXTRA_SMALL_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 11 - currentShockWaveLife, circlestrokepaint);
                            break;
                        case SMALL_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(2);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 21 - currentShockWaveLife, circlestrokepaint);
                            break;
                        case MEDIUM_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 128 - currentShockWaveLife, circlestrokepaint);
                            break;
                        case LARGE_WAVE:
                            circlestrokepaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 252 - currentShockWaveLife, circlestrokepaint);
                            break;
                        case FOOD_SPAWN_WAVE:
                            if (currentShockWaveLife < 5)
                                food.add(new Food(GameActivity.this, currentShockWave.position));

                            circlestrokepaint.setColor(Color.argb(252 - currentShockWaveLife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, currentShockWaveLife, circlestrokepaint);
                    }
                }
                else
                    shockWave.remove(shockWaveCounter); // remove alive shockwave
            }

            for (int popupCounter = 0; popupCounter < popup.size(); popupCounter++) // popup text drawer
            {
                Popup currentPopup = popup.get(popupCounter);
                if (currentPopup.getCounter() > 0) // if popup text is to be shown
                {
                    popuptext.setColor(Color.argb(popup.get(popupCounter).getCounter(), 255, 255, 255)); // text fade effect
                    switch (popup.get(popupCounter).type)
                    {
                        case BOO:
                            canvas.drawText(boostrings[currentPopup.textIndex], currentPopup.position.x, currentPopup.position.y + currentPopup.getCounter(), popuptext);
                            break;
                        case YEY:
                            canvas.drawText(yeystrings[currentPopup.textIndex], currentPopup.position.x, currentPopup.position.y + currentPopup.getCounter(), popuptext);
                    }
                }
                else
                    popup.remove(popupCounter); // remove alive popup
            }

			canvas.drawText("Score: " + Integer.toString(gameScore), 10, canvasHeight - 10, scoretext);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int integer)
	{
		Log.i(getLocalClassName(), "Accuracy changed");
	}

	public void onSensorChanged(SensorEvent event)
	{
		rollAngle = event.values[2];
	}
}