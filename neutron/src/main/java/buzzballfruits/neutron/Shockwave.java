package buzzballfruits.neutron;

import android.graphics.Point;

final class ShockWave
{
	final static int EXTRA_SMALL_WAVE = 0, SMALL_WAVE = 1, MEDIUM_WAVE = 2, LARGE_WAVE = 3;
	Point position = new Point();
	short life; // animation index life
	int type; // shockWaves type

	ShockWave(Point position, int type)
	{
		switch (type)
		{
			case ShockWave.EXTRA_SMALL_WAVE:
				this.life = 11;
				break;
			case ShockWave.SMALL_WAVE:
				this.life = 21;
				break;
			case ShockWave.MEDIUM_WAVE:
				this.life = 128;
				break;
			case ShockWave.LARGE_WAVE:
				this.life = 252;
		}
		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}

	public short getLife()
	{
		if (type == EXTRA_SMALL_WAVE || type == SMALL_WAVE)
		{
			return life -= 1;
		}
		else
		{
			return life -= 4;
		}
	}
}
