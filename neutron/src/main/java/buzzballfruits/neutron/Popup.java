package buzzballfruits.neutron;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
    final static byte YEY = 0, BOO = 1, BUMP = 2;
	Point position = new Point();
	int life = 255; // animation index life
	int type; // popup message type
	int textindex; //random text index
	
	Popup(Point position, int type, int indexsize)
	{
        Random rnd = new Random();
        if (indexsize > 0);
            textindex = rnd.nextInt(indexsize);

        this.type = type;
        this.position.x = position.x;
        this.position.y = position.y - 255;
	}

	public int getLife()
	{
		return life--;
	}
}
