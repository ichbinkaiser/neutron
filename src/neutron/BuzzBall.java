package neutron;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Point;
import android.util.Log;

final public class BuzzBall implements Runnable
{
	private GameActivity gameactivity;
	private int rotation = 0;
	private int type, height, width;
	private boolean dead = false, dying = false;
	private float gravity = 0.0f;
	private float climb = 3; // upward force
	private int xmovement = 0;
	private ArrayList<Ball> ball;
	private ArrayList<Ball>	ballcollided = new ArrayList<Ball>(); // array list of balls that have collided with this buzzball
	private int ground; //groung bounce for buzzball
	private Player[] player; // player pointer
	private Point position = new Point(); Point pposition = new Point();
	private Random rnd = new Random();
	private float inertia = 0;
	private int opacity = 255;

	BuzzBall(GameActivity gameActivity, int type, int height, int width, ArrayList<Ball> ball, Player[] player)
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
		int roll;

		ground = player[1].getGround() - rnd.nextInt(player[1].getGround() - player[0].getGround()); //rnd.nextInt(player[1].getGround()); // set random ground for buzzball in a specific range

		while(gameactivity.isRunning() && (!dead))
		{
			roll = (int) gameactivity.getRollAngle();

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


			for (int ballcounter = ball.size() - 1; ballcounter >= 0; ballcounter--)
			{
				if ((ball.get(ballcounter).getPosition().x >= position.x) && (ball.get(ballcounter).getPosition().x <= position.x + width) && (ball.get(ballcounter).getPosition().y >= position.y) && (ball.get(ballcounter).getPosition().y <= position.y + height))
				{
					boolean collided = false;

					for (int ballcollidedcounter = ballcollided.size() - 1; ballcollidedcounter >= 0; ballcollidedcounter--)
					{
						if (ballcollided.get(ballcollidedcounter) == ball.get(ballcounter)) collided = true;
					}

					if ((!collided) && (!dying))
					{

						if (ball.get(ballcounter).isGoingLeft())
						{
							ball.get(ballcounter).setGoingleft(false);
							xmovement = 2;
						}
						else 
						{
							ball.get(ballcounter).setGoingleft(true);
							xmovement = -2;
						}
						ballcollided.add(ball.get(ballcounter));
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

	public int getOpacity()
	{
		return opacity;
	}

	public Point getPosition() 
	{
		return position;
	}

	public void setPosition(Point position) 
	{
		this.position = position;
	}

	public int getRotation()
	{
		return rotation;
	}

	public int getType()
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

	public void setXmovement(int xmovement)
	{
		this.xmovement = xmovement;
	}

	public boolean isGoingUp()
	{
		return (pposition.y > position.y);
	}

}
