package neutron;

import java.util.Random;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

final class Player implements Runnable
{
	private GameActivity gameActivity;
	private Point position = new Point();
	private Point pposition = new Point(); // last position
	private float climb; // upward force
	private float gravity = 0.0f; // downward force
	private int destination;
	private boolean right = true; // is left direction
	private boolean jumping = false; // is jumping
	private int speedX, speedY; // side speed
	private int ground; // basepoint for player
	private Random rnd = new Random();
	private float shadowL, shadowT, shadowR, shadowB, shadowEdge; // shadow properties
	private int shadowOpacity;
	private RectF shadow = new RectF();
	private int jumpheight;

	Player(GameActivity gameActivity, int ground) 
	{
		this.gameActivity = gameActivity;
		this.ground = ground;
		start();
	}

	private void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Player");
		thread.start();
	}

	public void run() 
	{
		int edge = gameActivity.getCanvasWidth() - gameActivity.getSmileyWidth();
		shadowT = ground + (gameActivity.getSmileyHeight() - gameActivity.getSmileyHeight() / 4);
		shadowB = ground + gameActivity.getSmileyHeight();
		while (gameActivity.isRunning())
		{
			if ((position.x < destination) && (position.x < edge)) // player move right
				if (Math.abs(destination - position.x) > 10)
					position.x += 5;
				else
					position.x++;

			else if ((position.x > destination) && (position.x > 0)) // player move left
			{
				if (Math.abs(destination - position.x) > 10)
					position.x -= 5;
				else
					position.x--;
			}

			if (position.x > pposition.x)
				right = true; // bottom player has moved right		
			else if (position.x < pposition.x)
				right = false; // bottom player has moved left

			pposition.set(position.x, position.y);

			if (jumping)
			{
				position.y += climb + gravity * gravity;
				gravity += 0.05f;
			}

			if ((jumping) && (position.y > ground))
			{
				jumping = false;
				position.y = ground;
				gravity = 0.0f;
			}

			jumpheight = (ground - position.y) / 10;
			
			if (jumpheight < gameActivity.getSmileyWidth()/4)
			{
				shadowEdge = jumpheight;
				shadowOpacity = 50 - jumpheight * 2;
			}
			
			shadowL = position.x + shadowEdge;
			shadowR = position.x + gameActivity.getSmileyWidth() - shadowEdge;

			shadow.set(shadowL, shadowT, shadowR, shadowB);

			try
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("Player", e.toString());
			}
		}
	}

	public void jump() // do jump
	{
		climb = (float) -(rnd.nextInt(2) + 6); // upward force
		jumping = true;
	}

	public Point getPosition()
	{
		return position;
	}

	public boolean isRight()
	{
		return right;
	}

	public Point getPposition()
	{
		return pposition;
	}

	public void setDestination(float roll)
	{
		if (roll<0)
			destination = (int) (position.x + Math.abs(roll*5));
		else if (roll>0)
			destination = (int) (position.x - Math.abs(roll*5));
	}

	public void setDestination(int destination)
	{
		this.destination = destination;
	}
	
	public int getGround()
	{
		return ground;
	}

	public RectF getShadow()
	{
		return shadow;
	}
	
	public int getShadowOpacity()
	{
		return shadowOpacity;
	}
}