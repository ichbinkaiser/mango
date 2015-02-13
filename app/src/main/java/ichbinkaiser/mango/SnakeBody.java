package ichbinkaiser.mango;

import android.graphics.Point;

import java.util.ArrayList;

final class SnakeBody
{
	Point startpoint = new Point(); //start point
	Point endpoint = new Point(); //end point
    int direction;
    ArrayList<SnakeBody> bodysegments;

	SnakeBody(Point startpoint, int direction, ArrayList<SnakeBody> bodysegment)
	{
		this.startpoint.x = startpoint.x;
		this.startpoint.y = startpoint.y;
		this.endpoint.x = startpoint.x;
		this.endpoint.y = startpoint.y;
        this.direction = direction;
        this.bodysegments = bodysegment;
	}

    public void trim(int length)
    {
        switch (direction)
        {
            case Snake.GOING_UP:
                endpoint.y -= length;
                break;
            case Snake.GOING_DOWN:
                endpoint.y += length;
                break;
            case Snake.GOING_LEFT:
                endpoint.x -= length;
                break;
            case Snake.GOING_RIGHT:
                endpoint.x += length;
        }
        if (getLength() < 0)
            bodysegments.remove(this);
    }

    public int getLength()
    {
        switch (direction)
        {
            case Snake.GOING_UP:
                return endpoint.y - startpoint.y;
            case Snake.GOING_DOWN:
                return startpoint.y - endpoint.y;
            case Snake.GOING_LEFT:
                return endpoint.x - startpoint.x;
            case Snake.GOING_RIGHT:
                return startpoint.x - endpoint.x;
            default:
                return 0;
        }
    }
}
