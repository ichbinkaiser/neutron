package ichbinkaiser.neutron;

import android.graphics.Point;

final class ShockWave
{
	Point position = new Point();
	short life; // animation index life
	ShockWaveType type; // ShockWave type

	ShockWave(Point position, ShockWaveType type)
	{
		switch (type)
		{
			case EXTRA_SMALL_WAVE:
				this.life = 11;
				break;
			case SMALL_WAVE:
				this.life = 21;
				break;
			case MEDIUM_WAVE:
				this.life = 128;
				break;
			case LARGE_WAVE:
				this.life = 252;
		}
		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}

	public short getLife()
	{
		if (type == ShockWaveType.EXTRA_SMALL_WAVE || type == ShockWaveType.SMALL_WAVE)
		{
			return life -= 1;
		}
		else
		{
			return life -= 4;
		}
	}
}
