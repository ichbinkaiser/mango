package ichbinkaiser.mango.entity;

import android.graphics.Point;

import java.util.Random;

public class Popup
{

	Point position = new Point();
	int counter = 255; // animation index counter
	PopupType type; // popup message type
	Random rnd = new Random();
	int textIndex = rnd.nextInt(10); // random text index
	
	Popup(Point position, PopupType type)
	{
		this.type = type;
		this.position.x = position.x;
        this.position.y = position.y - 255;
	}

	public int getCounter() 
	{
		return counter--;
	}

	public Point getPosition() {
		return position;
	}

	public PopupType getType() {
		return type;
	}

	public Random getRnd() {
		return rnd;
	}

	public int getTextIndex() {
		return textIndex;
	}
}
