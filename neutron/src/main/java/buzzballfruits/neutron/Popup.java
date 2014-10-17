package buzzballfruits.neutron;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
    final static byte SCOREUP = 0, LOSELIFE = 1, SOLO = 2;
	Point position = new Point();
	int life = 255; // animation index life
	int type; // popup message type
	Random rnd = new Random();
	int textindex; //random text index
	
	Popup(Point position, int type, int indexsize)
	{

        Random rnd = new Random();
        if (indexsize > 0);
            textindex = rnd.nextInt(indexsize);

        this.type = type;
        this.position.x = position.x;
		
		switch(type)
		{
            case SCOREUP:
                this.position.y = position.y + 255;
                break;
            case LOSELIFE:
            case SOLO:
                this.position.y = position.y - 255;
		}
	}

	public int getLife()
	{
		return life--;
	}
}
