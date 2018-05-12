package ichbinkaiser.mango.activity;

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
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

import ichbinkaiser.mango.control.AI;
import ichbinkaiser.mango.entity.Direction;
import ichbinkaiser.mango.entity.Food;
import ichbinkaiser.mango.entity.Popup;
import ichbinkaiser.mango.entity.ShockWave;
import ichbinkaiser.mango.entity.Snake;
import ichbinkaiser.mango.entity.SnakeBody;
import lombok.Getter;

/**
 * Main game play activity
 */
public class GameActivity extends Activity {
    
    private Random rnd = new Random();
    private Point upTouch, downTouch; // player up touch position
    private WakeLock wakelock;
    private GameSurfaceThread gamesurfacethread;
    private SurfaceHolder surfaceholder;
    private SensorManager sensormanager;
    private Sensor orientation;

    private int gameScore = 0;
    private int AICount = 3;
    private boolean soloGame = true;

    @Getter
    private List<Popup> popup = new CopyOnWriteArrayList<>(); // popup messages array list

    @Getter
    private List<ShockWave> shockWave = new CopyOnWriteArrayList<>(); // shockwave animation list

    @Getter
    private List<Snake> snakes = new CopyOnWriteArrayList<>(); // snakes list

    @Getter
    private List<Food> food = new CopyOnWriteArrayList<>(); // food list

    @Getter
    private List<AI> AI = new CopyOnWriteArrayList<>(); // snakes list

    @Getter
    private static String score;

    @Getter
    private int canvasHeight;

    @Getter
    private int canvasWidth;

    @Getter
    private boolean isRunning = true; // game isRunning

    @Getter
    private int headSize; // snakeList head

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getLocalClassName(), "Activity stopped");
        this.wakelock.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish(); // disallow pausing
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void showScore() // show score screen
    {
        isRunning = false;
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

    public void stop() {
        isRunning = false;
    }

    public void addGameScore(int addend) {
        gameScore += addend;
    }

    private static class GameSurfaceThread extends Thread {
        GameActivity gameActivity;
        SurfaceHolder surfaceHolder;
        GameScreen gameScreen;

        public GameSurfaceThread(GameActivity gameActivity, SurfaceHolder holder, GameScreen drawMain) {
            this.gameActivity = gameActivity;
            setName("SurfaceView");
            surfaceHolder = holder;
            gameScreen = drawMain;
            start();
        }

        @Override
        public void run() {
            Canvas canvas = null;
            while (gameActivity.isRunning) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    gameScreen.screenDraw(canvas);
                } catch (NullPointerException e) {
                    Log.e(this.gameActivity.getLocalClassName(), e.toString());
                } finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    /**
     * Main thread to hadle in game events
     */
    private class GlobalThread implements Runnable {
        GlobalThread() {
            start();
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("GlobalThread");
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void run() {
            while (isRunning) {
                if (rnd.nextInt(100) == 0)
                    shockWave.add(new ShockWave(GameActivity.this));

                if (snakes.size() - 1 < AICount)
                    snakes.add(new Snake(GameActivity.this));

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("GlobalThread", e.toString());
                }
            }
        }
    }

    /**
     * Handles drawing of elements onto the screen
     */
    public class GameScreen extends SurfaceView implements Callback {
        /**
         * Positive feedback texts
         */
        String[] yeyStrings = new String[]{
                "OH YEAH!",
                "WOHOOO!",
                "YEAH BABY!",
                "WOOOT!",
                "AWESOME!",
                "COOL!",
                "GREAT!",
                "YEAH!!",
                "WAY TO GO!",
                "YOU ROCK!"
        };

        /**
         * Negative feedback texts
         */
        String[] booStrings = new String[]{
                "YOU SUCK!",
                "LOSER!",
                "GO HOME!",
                "REALLY?!",
                "WIMP!",
                "SUCKER!",
                "HAHAHA!",
                "YOU MAD?!",
                "DIE!",
                "BOOM!"
        };

        /**
         * Collision feedback texts
         */
        String[] bumpStrings = new String[]{
                "BUMP!",
                "TOINK!",
                "BOINK!",
                "BAM!",
                "WABAM!"
        };

        /**
         * Speedup texts
         */
        String[] zoomStrings = new String[]{
                "ZOOM!",
                "WOOSH!",
                "SUPER MODE!",
                "ZOOMBA!",
                "WARPSPEED!"
        };

        Paint foodPaint = new Paint(); // food paint
        Paint snakeJointPaint = new Paint();
        Paint snakePaint = new Paint();
        Paint scoreText = new Paint();
        Paint popupText = new Paint();
        Paint circleStrokePaint = new Paint();
        Paint backgroundPaint = new Paint();
        GlobalThread globalThread;

        public GameScreen(Context context) {
            super(context);

            surfaceholder = getHolder();
            surfaceholder.addCallback(this);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            canvasWidth = metrics.widthPixels;
            canvasHeight = metrics.heightPixels;

            downTouch = new Point();
            upTouch = new Point();

            Typeface myType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            scoreText.setColor(Color.WHITE);
            scoreText.setTypeface(myType);

            popupText.setTypeface(myType);
            popupText.setTextAlign(Align.CENTER);

            if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) { // adjust to low DPI
                popupText.setTextSize(8);
                scoreText.setTextSize(9);
                headSize = 2;
                Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
            } else {
                popupText.setTextSize(12);
                scoreText.setTextSize(15);
                headSize = 4;
                Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
            }

            backgroundPaint.setColor(Color.BLACK);
            foodPaint.setColor(Color.WHITE);
            snakePaint.setColor(Color.WHITE);
            snakePaint.setStyle(Paint.Style.STROKE);
            snakePaint.setStrokeWidth(headSize * 2);
            circleStrokePaint.setStyle(Paint.Style.STROKE);

            snakes.add(new Snake(GameActivity.this)); // Add player

            if (snakes.size() == 1)
                Log.i(getLocalClassName(), "Snake initialized");
            else
                Log.i(getLocalClassName(), "Snakes initialized");

            globalThread = new GlobalThread();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
        {
            isRunning = false;
            Log.i(getLocalClassName(), "Surface destroyed");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(getLocalClassName(), "Surface changed");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) // when user enters game
        {
            gamesurfacethread = new GameSurfaceThread(GameActivity.this, holder, this);
            Log.i(getLocalClassName(), "Surface created");
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downTouch.x = (int) event.getX();
                downTouch.y = (int) event.getY();
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                upTouch.x = (int) event.getX();
                upTouch.y = (int) event.getY();

                if (snakes.size() > 0 && Math.abs(downTouch.x - upTouch.x) > (Math.abs(downTouch.y - upTouch.y))) { // go with X axis movement
                    if (downTouch.x > upTouch.x)
                        snakes.get(0).setDirection(Direction.LEFT);
                    else
                        snakes.get(0).setDirection(Direction.RIGHT);
                } else if (downTouch.y > upTouch.y)
                    snakes.get(0).setDirection(Direction.UP);
                else
                    snakes.get(0).setDirection(Direction.DOWN);
            }
            return true;
        }

        public void screenDraw(Canvas canvas) {
            canvas.drawRect(0, 0, canvasWidth, canvasHeight, backgroundPaint);

            for (int foodCounter = 0; foodCounter < food.size(); foodCounter++) {
                canvas.drawCircle(food.get(foodCounter).getPosition().x,
                        food.get(foodCounter).getPosition().y,
                        headSize,
                        foodPaint);
            }

            for (int snakeCounter = 0; snakeCounter < snakes.size(); snakeCounter++) { // snakes drawer
                Snake currentSnake = snakes.get(snakeCounter);
                if (currentSnake.isAlive()) {
                    if (snakeCounter == 0) {
                        snakeJointPaint.setColor(Color.GRAY);
                        snakePaint.setColor(Color.GRAY);
                    } else {
                        snakeJointPaint.setColor(Color.WHITE);
                        snakePaint.setColor(Color.WHITE);
                    }

                    canvas.drawCircle(currentSnake.getPosition().x,
                            currentSnake.getPosition().y,
                            headSize,
                            snakeJointPaint);

                    for (int bodyCounter = 0; bodyCounter < currentSnake.getBodySegments().size(); bodyCounter++) {
                        SnakeBody currentSegment = currentSnake.getBodySegments().get(bodyCounter);

                        canvas.drawLine(currentSegment.getStartPoint().x,
                                currentSegment.getStartPoint().y,
                                currentSegment.getEndpoint().x,
                                currentSegment.getEndpoint().y,
                                snakePaint);

                        canvas.drawCircle(currentSegment.getEndpoint().x,
                                currentSegment.getEndpoint().y,
                                headSize,
                                snakeJointPaint);
                    }
                }
            }

            for (int shockWaveCounter = 0; shockWaveCounter < shockWave.size(); shockWaveCounter++) {  // shock wave drawer
                ShockWave currentShockWave = shockWave.get(shockWaveCounter);
                if (currentShockWave.getLife() > 0) // bump animation
                {
                    int currentShockWaveLife = currentShockWave.getLife();
                    switch (currentShockWave.getType()) {
                        case EXTRA_SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x,
                                    currentShockWave.getPosition().y,
                                    11 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(2);
                            canvas.drawCircle(currentShockWave.getPosition().x,
                                    currentShockWave.getPosition().y,
                                    21 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case MEDIUM_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x,
                                    currentShockWave.getPosition().y,
                                    128 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case LARGE_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x,
                                    currentShockWave.getPosition().y,
                                    252 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case FOOD_SPAWN_WAVE:
                            if (currentShockWaveLife < 5)
                                food.add(new Food(GameActivity.this, currentShockWave.getPosition()));

                            circleStrokePaint.setColor(Color.argb(252 - currentShockWaveLife, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x,
                                    currentShockWave.getPosition().y,
                                    currentShockWaveLife,
                                    circleStrokePaint);
                    }
                } else
                    shockWave.remove(shockWaveCounter); // remove alive shockwave
            }

            for (int popupCounter = 0; popupCounter < popup.size(); popupCounter++) { // popup text drawer
                Popup currentPopup = popup.get(popupCounter);
                if (currentPopup.getCounter() > 0) { // if popup text is to be shown
                    popupText.setColor(Color.argb(popup.get(popupCounter).getCounter(), 255, 255, 255)); // text fade effect
                    switch (popup.get(popupCounter).getType()) {
                        case BOO:
                            canvas.drawText(booStrings[currentPopup.getTextIndex()],
                                    currentPopup.getPosition().x,
                                    currentPopup.getPosition().y + currentPopup.getCounter(),
                                    popupText);
                            break;
                        case YEY:
                            canvas.drawText(yeyStrings[currentPopup.getTextIndex()],
                                    currentPopup.getPosition().x,
                                    currentPopup.getPosition().y + currentPopup.getCounter(),
                                    popupText);
                    }
                } else
                    popup.remove(popupCounter); // remove alive popup
            }

            canvas.drawText("Score: " + Integer.toString(gameScore), 10, canvasHeight - 10, scoreText);
        }
    }
}