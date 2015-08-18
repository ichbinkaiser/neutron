package buzzballfruits.neutron;

import android.graphics.Point;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

final public class BuzzBall implements Runnable
{
	Player[] player; // playerCount pointer
	GameActivity gameActivity;

	int rotation = 0;
	int type; // buzzBalls type
	int height, width;
	boolean dead = false, dying = false;

	float gravity = 0.0f;
	float climb = 3; // upward force
	int xMovement = 0;

	CopyOnWriteArrayList<Ball> balls;
	CopyOnWriteArrayList<Ball> ballsCollided = new CopyOnWriteArrayList<>(); // array list of balls that have collided with this buzzBalls

	Point position = new Point();
	Point previousPosition = new Point();

	Random rnd = new Random();

	float inertia = 0;
	int opacity = 255;
	int ground; // ground bounce for buzzBalls

	BuzzBall(GameActivity gameActivity, int type, int height, int width, CopyOnWriteArrayList<Ball> balls, Player[] player)
	{
		this.gameActivity = gameActivity;
		this.type = type;
		this.height = height;
		this.width = width;
		this.balls = balls;
		this.player = player;
		position.x = rnd.nextInt(gameActivity.canvasWidth - width);
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
		ground = (gameActivity.ground[1] - rnd.nextInt(gameActivity.ground[1] - gameActivity.ground[0])); // set random ground for buzzBalls in a specific range

		while (gameActivity.running && (!dead))
		{
			///////////////////////////// BUZZBALL ROTATION AND MOVEMENT //////////////////////////////////////
			roll = (int) gameActivity.rollAngle;

			if ((roll > 0) && (inertia < 10))
			{
				inertia += .05;
			}
			else if ((roll < 0) && (inertia > -10))
			{
				inertia -= .05;
			}

			rotation += (inertia); // rotation to positive target rotation

			if (rotation > 359)
			{
				rotation -= 360;
			}
			else if (rotation < 0)
			{
				rotation += 360;
			}

			position.y += climb + gravity;
			gravity += 0.025f;

			if (!dying)
			{
				if (position.x < 0) // left or right movement responding to collision with screen boundaries
				{
					xMovement = 2;
				}
				else if (position.x > gameActivity.canvasWidth - width)
				{
					xMovement = -2;
				}
			}
			position.x += xMovement;

			if ((position.y > ground) && (!dying))
			{
				dying = true;
				climb = -1; // emulate gravity
				gravity = 0.0f;

				if (rnd.nextBoolean())
				{
					inertia = 10;
					xMovement = 2;
				}

				else
				{
					inertia = -10;
					xMovement = -2;
				}
			}
			////////////////////// BUZZBALL TO BALL COLLISION DETECTION /////////////////////////////////
			for (Ball ball : balls)
			{
				if (ball.position.x >= position.x && ball.position.x <= position.x + width && ball.position.y >= position.y && ball.position.y <= position.y + height)
				{
					boolean collided = false;
					for (Ball ballCollided : ballsCollided)
					{
						if (ballCollided == ball)
						{
							collided = true;
						}
					}

					if (!collided && !dying)
					{

						if (ball.goingLeft)
						{
							ball.goingLeft = false;
							xMovement = 2;
						}

						else
						{
							ball.goingLeft = true;
							xMovement = -2;
						}
						ballsCollided.add(ball);
					}
				}
			}

			previousPosition.x = position.x;
			previousPosition.y = position.y;

			if (dying)
			{
				opacity--;
			}

			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException interruptedException)
			{
				interruptedException.printStackTrace();
				Log.e("BuzzBall", interruptedException.toString());
			}
		}
	}
}
