package shakeysnake.mango;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
    final static int SCOREUP = 0, LOSELIFE = 1, SOLO = 2;
	Point position = new Point();
	int counter = 255; // animation index counter
	int type; // popup message type
	Random rnd = new Random();
	int textindex = rnd.nextInt(10); // random text index
	
	Popup(Point position, int type)
	{
		this.type = type;
		this.position.x = position.x;
		
		switch(type)
		{
            case SCOREUP:
                this.position.y = position.y + 255;
                break;
            case LOSELIFE:
                this.position.y = position.y - 255;
                break;
            case SOLO:
                this.position.y = position.y - 255;
		}
	}

	public int getCounter() 
	{
		return counter--;
	}
}
