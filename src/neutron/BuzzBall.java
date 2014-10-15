package neutron;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Point;
import android.util.Log;

final public class BuzzBall implements Runnable
{
	private GameActivity gameactivity;
	private short rotation = 0;
    private byte type;
	private short  height, width;
	private boolean dead = false, dying = false;
	private float gravity = 0.0f;
	private float climb = 3; // upward force
	private short xmovement = 0;
	private ArrayList<Ball> ball;
	private ArrayList<Ball>	ballcollided = new ArrayList<Ball>(); // array list of balls that have collided with this buzzball
	private short ground; //groung bounce for buzzball
	private Player[] player; // player pointer
	private Point position = new Point(); Point pposition = new Point();
	private Random rnd = new Random();
	private float inertia = 0;
	private short opacity = 255;

	BuzzBall(GameActivity gameActivity, byte type, short height, short width, ArrayList<Ball> ball, Player[] player)
	{
		this.gameactivity = gameActivity;
		this.type = type;
		this.height = height;
		this.width = width;
		this.ball = ball;
		this.player = player;
		position.x = rnd.nextInt(gameActivity.getCanvasWidth() - width);
		position.y = 0 - height;
		start();
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("BuzzBall");
		thread.start();
	}

	public void run()
	{
		short roll;

		ground = (short)(player[1].getGround() - rnd.nextInt(player[1].getGround() - player[0].getGround())); // set random ground for buzzball in a specific range

		while(gameactivity.isRunning() && (!dead))
		{
			roll = (short) gameactivity.getRollAngle();

			if ((roll > 0) && (inertia < 10))
				inertia += .05;
			else if ((roll < 0) && (inertia > -10))
				inertia -= .05;

			rotation += (int) (inertia); // rotation to positive target rotation 

			if (rotation > 359) rotation -= 360;
			else if (rotation < 0) rotation += 360;

			position.y += climb + gravity;
			gravity += 0.025f;

			if (!dying)
			{
				if (position.x < 0) // left or right movement responding to collision with screen boundaries
					xmovement = 2;
				else if (position.x > gameactivity.getCanvasWidth() - width)
					xmovement = -2;
			}

			position.x += xmovement;

			if ((position.y > ground) && (!dying))
			{
				dying = true;
				climb = -1; // emulate gravity
				gravity = 0.0f;

				if (rnd.nextBoolean())
				{
					inertia = 10;
					xmovement = 2;
				}
				else
				{
					inertia = -10;
					xmovement = -2;
				}
			}


			for (short ballcounter = (short)(ball.size() - 1); ballcounter >= 0; ballcounter--)
			{
                Ball currentball = ball.get(ballcounter);
				if ((currentball.getPosition().x >= position.x) && (currentball.getPosition().x <= position.x + width) && (currentball.getPosition().y >= position.y) && (currentball.getPosition().y <= position.y + height))
				{
					boolean collided = false;

					for (short ballcollidedcounter = (short)(ballcollided.size() - 1); ballcollidedcounter >= 0; ballcollidedcounter--)
					{
						if (ballcollided.get(ballcollidedcounter) == ball.get(ballcounter)) collided = true;
					}

					if ((!collided) && (!dying))
					{

						if (currentball.isGoingLeft())
						{
                            currentball.setGoingleft(false);
							xmovement = 2;
						}
						else 
						{
                            currentball.setGoingleft(true);
							xmovement = -2;
						}
						ballcollided.add(currentball);
					}
				}
			}

			pposition.x = position.x;
			pposition.y = position.y;

			if (dying)
				opacity--;

			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("BuzzBall", e.toString());
			}
		}
	}

	public short getOpacity()
	{
		return opacity;
	}

	public Point getPosition() 
	{
		return position;
	}

	public short getRotation()
	{
		return rotation;
	}

	public byte getType()
	{
		return type;
	}

	public boolean isDead()
	{
		return dead;
	}

	public void kill()
	{
		dead = true;
	}

}
