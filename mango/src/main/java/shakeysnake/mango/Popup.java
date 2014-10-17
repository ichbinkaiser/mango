package shakeysnake.mango;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
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
		case 0:
			this.position.y = position.y + 255;
			break;
		case 1:
			this.position.y = position.y - 255;
			break;
		case 2:
			this.position.y = position.y - 255;
			break;
		}
	}

	public int getCounter() 
	{
		return counter--;
	}
}
