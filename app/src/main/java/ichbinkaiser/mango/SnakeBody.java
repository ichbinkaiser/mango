package ichbinkaiser.mango;

import android.graphics.Point;

import java.util.List;

final class SnakeBody
{
	Point startPoint = new Point(); //start point
	Point endpoint = new Point(); //end point
    Snake.Direction direction;
    List<SnakeBody> bodySegments;

	SnakeBody(Point startPoint, Snake.Direction direction, List<SnakeBody> bodySegment)
	{
		this.startPoint.x = startPoint.x;
		this.startPoint.y = startPoint.y;
		this.endpoint.x = startPoint.x;
		this.endpoint.y = startPoint.y;
        this.direction = direction;
        this.bodySegments = bodySegment;
	}

    public void trim(int length)
    {
        switch (direction)
        {
            case UP:
                endpoint.y -= length;
                break;
            case DOWN:
                endpoint.y += length;
                break;
            case LEFT:
                endpoint.x -= length;
                break;
            case RIGHT:
                endpoint.x += length;
        }
        if (getLength() < 0)
            bodySegments.remove(this);
    }

    public int getLength()
    {
        switch (direction)
        {
            case UP:
                return endpoint.y - startPoint.y;
            case DOWN:
                return startPoint.y - endpoint.y;
            case LEFT:
                return endpoint.x - startPoint.x;
            case RIGHT:
                return startPoint.x - endpoint.x;
            default:
                return 0;
        }
    }
}
