package shakeysnake.mango;

import android.graphics.Point;

final class Shockwave 
{
	private Point position = new Point();
	private int life; // animation index life
	private int type; // shockwave type
	
	Shockwave(Point position, int type)
	{
		switch (type)
		{
		case 0: // extra small wave
			setLife(11);
			break;
		case 1: // small wave
			setLife(21);
			break;
		case 2: // medium wave
			setLife(128);
			break;
		case 3: // large wave
			setLife(252);
			break;
		}
		this.setType(type);
		this.getPosition().x = position.x;
		this.getPosition().y = position.y;
	}

    public int getLife()
    {
        switch (type)
        {
            case 0 :
            case 1 :
                return life -= 1;
            case 2 :
            case 3 :
                return life -= 4;
            default :
                return life;
        }
    }

	public void setLife(int life)
	{
		this.life = life;
	}

	public int getType() 
	{
		return type;
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public Point getPosition() 
	{
		return position;
	}
}
