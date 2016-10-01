package ichbinkaiser.neutron;

import android.graphics.Point;

import java.util.Random;

final class Popup
{

	Point position = new Point();
	int life = 255; // animation index life
	PopupType type; // popups message type
	int textIndex; //random text index

	Popup(Point position, PopupType type, int textIndexSize)
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
