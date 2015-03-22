package buzzballfruits.neutron;

import android.graphics.Point;

import java.util.Random;

final class Popup
{
	enum Type
	{
		YEY, BOO, BUMP
	}

	Point position = new Point();
	int life = 255; // animation index life
	Type type; // popups message type
	int textIndex; //random text index

	Popup(Point position, Type type, int textIndexSize)
	{
		Random rnd = new Random();
		if (textIndexSize > 0)
		{
			textIndex = rnd.nextInt(textIndexSize);
		}

		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y - 255;
	}

	public int getLife()
	{
		return life--;
	}
}
