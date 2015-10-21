package ichbinkaiser.mango;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
	enum popupType
	{
		YEY,
		BOO,
		BUMP
	}

	Point position = new Point();
	int counter = 255; // animation index counter
	popupType type; // popup message type
	Random rnd = new Random();
	int textIndex = rnd.nextInt(10); // random text index
	
	Popup(Point position, popupType type)
	{
		this.type = type;
		this.position.x = position.x;
        this.position.y = position.y - 255;
	}

	public int getCounter() 
	{
		return counter--;
	}
}
