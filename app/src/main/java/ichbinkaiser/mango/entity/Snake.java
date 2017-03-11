package ichbinkaiser.mango.entity;

import android.graphics.Point;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import ichbinkaiser.mango.activity.GameActivity;
import ichbinkaiser.mango.control.AI;

public class Snake implements Runnable {

    GameActivity gameactivity;
    Point position = new Point();
    boolean isAlive = true;
    int spawnWave; // spawn wave animation
    int speed; // speed and direction;
    Direction direction;
    int length = 100, currentLength; //snakeList length

    List<SnakeBody> bodySegments = new CopyOnWriteArrayList<>();

    public Snake(GameActivity gameactivity) {
        Random rnd = new Random();
        this.gameactivity = gameactivity;
        speed = 5;
        direction = Direction.values()[rnd.nextInt(3)];

        switch (direction) {
            case UP:
                position.x = rnd.nextInt((gameactivity.getCanvasWidth() / 3) + (gameactivity.getCanvasWidth() / 3));
                position.y = gameactivity.getCanvasHeight();
                break;
            case RIGHT:
                position.x = 0;
                position.y = rnd.nextInt((gameactivity.getCanvasHeight() / 3) + (gameactivity.getCanvasHeight() / 3));
                break;
            case DOWN:
                position.x = rnd.nextInt((gameactivity.getCanvasWidth() / 3) + (gameactivity.getCanvasWidth() / 3));
                position.y = 0;
                break;
            case LEFT:
                position.x = gameactivity.getCanvasWidth();
                position.y = rnd.nextInt((gameactivity.getCanvasHeight() / 3) + (gameactivity.getCanvasHeight() / 3));
                break;
        }
        bodySegments.add(new SnakeBody(position, direction, bodySegments));
        start();
    }

    public int getMoved(Point position) {
        if (direction == Direction.UP || direction == Direction.DOWN)
            return Math.abs(this.position.y - position.y);
        else
            return Math.abs(this.position.x - position.x);
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("Snake");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        if (gameactivity.getSnakes().size() > 1)
            gameactivity.getAI().add(new AI(gameactivity, gameactivity.getSnakes(), this));

        while (gameactivity.isRunning() && isAlive) {
            switch (direction) {
                case UP:
                    position.y -= speed;
                    bodySegments.get(bodySegments.size() - 1).startPoint.y = position.y;
                    break;
                case RIGHT:
                    position.x += speed;
                    bodySegments.get(bodySegments.size() - 1).startPoint.x = position.x;
                    break;
                case DOWN:
                    position.y += speed;
                    bodySegments.get(bodySegments.size() - 1).startPoint.y = position.y;
                    break;
                case LEFT:
                    position.x -= speed;
                    bodySegments.get(bodySegments.size() - 1).startPoint.x = position.x;
            }

            if (spawnWave > 0) // spawn_wave animation
            {
                gameactivity.getShockWave().add(new ShockWave(position, WaveType.EXTRA_SMALL_WAVE));
                spawnWave--;
            }

            if (position.x < 0) // head has reached left wall
            {
                position.x = gameactivity.getCanvasWidth();
                bodySegments.add(new SnakeBody(position, direction, bodySegments));
            } else if (position.x > gameactivity.getCanvasWidth()) // head has reached right wall
            {
                position.x = 0;
                bodySegments.add(new SnakeBody(position, direction, bodySegments));
            } else if (position.y > gameactivity.getCanvasHeight()) // head has reached bottom wall
            {
                position.y = 0;
                bodySegments.add(new SnakeBody(position, direction, bodySegments));
            } else if (position.y < 0) {
                position.y = gameactivity.getCanvasHeight();
                bodySegments.add(new SnakeBody(position, direction, bodySegments));
            }

            currentLength = 0;
            for (SnakeBody currentBodySegment : bodySegments) {
                currentLength += currentBodySegment.getLength();
            }

            if (currentLength > length)
                bodySegments.get(0).trim(currentLength - length);

            for (int snakeCounter = 0; snakeCounter < gameactivity.getSnakes().size(); snakeCounter++) {
                Snake currentSnake = gameactivity.getSnakes().get(snakeCounter);
                SnakeBody lastBodySegment = bodySegments.get(bodySegments.size() - 1);
                for (SnakeBody currentBodySegment : currentSnake.bodySegments) {
                    if (currentBodySegment != lastBodySegment && detectCollision(currentBodySegment, gameactivity.getHeadSize(), gameactivity.getHeadSize())) {
                        gameactivity.doShake(100);
                        if (gameactivity.getSnakes().get(0) != this) // check if this is the human player
                        {
                            gameactivity.getPopup().add(new Popup(position, PopupType.BOO));
                            dieAnimation();
                        } else {
                            gameactivity.stop();
                            GameActivity.getSoundmanager().playSound(SoundType.RESTART, 1);
                            gameactivity.showScore();
                        }
                        break;
                    }
                }
            }

            for (int foodCounter = 0; foodCounter < gameactivity.getFood().size(); foodCounter++) {
                Food currentFood = gameactivity.getFood().get(foodCounter);
                if (Math.abs(position.x - currentFood.position.x) <= gameactivity.getHeadSize() * 2 && Math.abs(position.y - currentFood.position.y) <= gameactivity.getHeadSize() * 2) {
                    gameactivity.getFood().get(foodCounter).eat();
                    gameactivity.doShake(50);
                    length += 2;

                    gameactivity.getPopup().add(new Popup(position, PopupType.YEY));

                    if (this == gameactivity.getSnakes().get(0)) {
                        gameactivity.addGameScore(2);
                    }
                }
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Snake", e.toString());
            }
        }
        gameactivity.getSnakes().remove(this); // remove this dead snake
    }

    public void setDirection(Direction direction) {
        if ((this.direction == Direction.LEFT
                || this.direction == Direction.RIGHT)
                && (direction == Direction.UP || direction == Direction.DOWN)) {
            this.direction = direction;
            bodySegments.add(new SnakeBody(position, direction, bodySegments));
        } else if ((this.direction == Direction.DOWN
                || this.direction == Direction.UP)
                && (direction == Direction.LEFT
                || direction == Direction.RIGHT)) {
            this.direction = direction;
            bodySegments.add(new SnakeBody(position, direction, bodySegments));
        }
    }

    /**
     * @param bodySegment
     * @param sensitivity
     * @param crossSection
     * @return
     */
    public boolean detectCollision(SnakeBody bodySegment, int sensitivity, int crossSection) {
        return collisionDetection(bodySegment, sensitivity, crossSection, direction);
    }

    public boolean detectCollision(SnakeBody bodySegment, int sensitivity, int crossSection, Direction direction) {
        return collisionDetection(bodySegment, sensitivity, crossSection, direction);
    }

    private boolean collisionDetection(SnakeBody bodySegment, int sensitivity, int crossSection, Direction direction) // relative to current snake direction
    {
        switch (direction) {
            case UP:
                if (bodySegment.direction == Direction.UP || bodySegment.direction == Direction.DOWN)
                    return collinearCheck(bodySegment, sensitivity, crossSection, direction);
                else
                    return parallelCheck(bodySegment)
                            && position.y < bodySegment.startPoint.y + sensitivity
                            && position.y > bodySegment.startPoint.y;
            case DOWN:
                if (bodySegment.direction == Direction.UP || bodySegment.direction == Direction.DOWN)
                    return collinearCheck(bodySegment, sensitivity, crossSection, direction);
                else
                    return parallelCheck(bodySegment)
                            && position.y > bodySegment.startPoint.y - sensitivity
                            && position.y < bodySegment.startPoint.y;
            case LEFT:
                if (bodySegment.direction == Direction.LEFT || bodySegment.direction == Direction.RIGHT)
                    return collinearCheck(bodySegment, sensitivity, crossSection, direction);
                else
                    return parallelCheck(bodySegment)
                            && position.x < bodySegment.startPoint.x + sensitivity
                            && position.x > bodySegment.startPoint.x;
            case RIGHT:
                if (bodySegment.direction == Direction.LEFT || bodySegment.direction == Direction.RIGHT)
                    return collinearCheck(bodySegment, sensitivity, crossSection, direction);
                else
                    return parallelCheck(bodySegment)
                            && position.x > bodySegment.startPoint.x - sensitivity
                            && position.x < bodySegment.startPoint.x;
            default:
                return false;
        }
    }

    private boolean parallelCheck(SnakeBody bodySegment) {
        switch (bodySegment.direction) {
            case UP:
                return position.y >= bodySegment.startPoint.y
                        && position.y <= bodySegment.endpoint.y;
            case DOWN:
                return position.y <= bodySegment.startPoint.y
                        && position.y >= bodySegment.endpoint.y;
            case LEFT:
                return position.x >= bodySegment.startPoint.x
                        && position.x <= bodySegment.endpoint.x;
            case RIGHT:
                return position.x <= bodySegment.startPoint.x
                        && position.x >= bodySegment.endpoint.x;
            default:
                return false;
        }
    }

    private boolean collinearCheck(SnakeBody bodySegment, int sensitivity, int width, Direction direction) {
        switch (bodySegment.direction) {
            case UP:
                if (direction == Direction.UP)
                    return Math.abs(bodySegment.startPoint.x - position.x) <= width
                            && position.y <= bodySegment.endpoint.y + sensitivity
                            && position.y >= bodySegment.startPoint.y;
                else
                    return Math.abs(bodySegment.startPoint.x - position.x) <= width
                            && position.y >= bodySegment.startPoint.y - sensitivity
                            && position.y <= bodySegment.endpoint.y;
            case DOWN:
                if (direction == Direction.UP)
                    return Math.abs(bodySegment.startPoint.x - position.x) <= width
                            && position.y <= bodySegment.startPoint.y + sensitivity
                            && position.y >= bodySegment.endpoint.y;
                else
                    return Math.abs(bodySegment.startPoint.x - position.x) <= width
                            && position.y >= bodySegment.endpoint.y - sensitivity
                            && position.y <= bodySegment.startPoint.y;
            case LEFT:
                if (direction == Direction.LEFT)
                    return Math.abs(bodySegment.startPoint.y - position.y) <= width
                            && position.x <= bodySegment.endpoint.x + sensitivity
                            && position.x >= bodySegment.startPoint.x;
                else
                    return Math.abs(bodySegment.startPoint.y - position.y) <= width
                            && position.x >= bodySegment.startPoint.x - sensitivity
                            && position.x <= bodySegment.endpoint.x;
            case RIGHT:
                if (direction == Direction.LEFT)
                    return Math.abs(bodySegment.startPoint.y - position.y) <= width
                            && position.x <= bodySegment.startPoint.x + sensitivity
                            && position.x >= bodySegment.endpoint.x;
                else
                    return Math.abs(bodySegment.startPoint.y - position.y) <= width
                            && position.x >= bodySegment.endpoint.x - sensitivity
                            && position.x <= bodySegment.startPoint.x;
            default:
                return false;
        }
    }

    private void dieAnimation() {
        gameactivity.getPopup().add(new Popup(position, PopupType.BUMP));
        for (int bodySegmentCounter = 0; bodySegmentCounter < bodySegments.size(); bodySegmentCounter++) {
            SnakeBody currentBodySegment = bodySegments.get(bodySegmentCounter);
            gameactivity.getShockWave().add(new ShockWave(currentBodySegment.startPoint, WaveType.LARGE_WAVE));
            switch (bodySegments.get(bodySegmentCounter).direction) {
                case DOWN:
                    for (int counter = currentBodySegment.startPoint.y; counter > currentBodySegment.endpoint.y; counter--) {
                        if (counter % 5 == 0)
                            gameactivity.getShockWave().add(new ShockWave(currentBodySegment.startPoint.x, counter));
                    }
                case UP:
                    for (int counter = currentBodySegment.startPoint.y; counter < currentBodySegment.endpoint.y; counter++) {
                        if (counter % 5 == 0)
                            gameactivity.getShockWave().add(new ShockWave(currentBodySegment.startPoint.x, counter));
                    }
                case LEFT:
                    for (int counter = currentBodySegment.startPoint.x; counter > currentBodySegment.endpoint.x; counter--) {
                        if (counter % 5 == 0)
                            gameactivity.getShockWave().add(new ShockWave(counter, currentBodySegment.endpoint.y));
                    }
                case RIGHT:
                    for (int counter = currentBodySegment.startPoint.x; counter < currentBodySegment.endpoint.x; counter++) {
                        if (counter % 5 == 0)
                            gameactivity.getShockWave().add(new ShockWave(counter, currentBodySegment.endpoint.y));
                    }
            }
        }
        isAlive = false;
    }

    public Point getPosition() {
        return position;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getSpeed() {
        return speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<SnakeBody> getBodySegments() {
        return bodySegments;
    }
}