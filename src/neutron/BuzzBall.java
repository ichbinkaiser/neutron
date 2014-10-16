package neutron;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Point;
import android.util.Log;

final public class BuzzBall implements Runnable
{
    Player[] player; // player pointer
	GameActivity gameactivity;

	int rotation = 0;
    int type; // buzzball type
	int height, width;
	boolean dead = false, dying = false;

	float gravity = 0.0f;
	float climb = 3; // upward force
	int xmovement = 0;

	ArrayList<Ball> ball;
	ArrayList<Ball>	ballcollided = new ArrayList<Ball>(); // array list of balls that have collided with this buzzball

    Point position = new Point();
    Point pposition = new Point();

    Random rnd = new Random();

    float inertia = 0;
	int opacity = 255;
    int ground; //groung bounce for buzzball

	BuzzBall(GameActivity gameActivity, int type, int height, int width, ArrayList<Ball> ball, Player[] player)
	{
		this.gameactivity = gameActivity;
		this.type = type;
		this.height = height;
		this.width = width;
		this.ball = ball;
		this.player = player;
		position.x = rnd.nextInt(gameActivity.canvaswidth - width);
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
		ground = (gameactivity.ground[1] - rnd.nextInt(gameactivity.ground[1] - gameactivity.ground[0])); // set random ground for buzzball in a specific range

		while(gameactivity.running && (!dead))
		{
            ///////////////////////////// BUZZBALL ROTATION AND MOVEMENT //////////////////////////////////////
			roll = (int)gameactivity.rollangle;

			if ((roll > 0) && (inertia < 10))
				inertia += .05;
			else if ((roll < 0) && (inertia > -10))
				inertia -= .05;

			rotation += (inertia); // rotation to positive target rotation

			if (rotation > 359) rotation -= 360;
			else if (rotation < 0) rotation += 360;

			position.y += climb + gravity;
			gravity += 0.025f;

			if (!dying)
			{
				if (position.x < 0) // left or right movement responding to collision with screen boundaries
					xmovement = 2;
				else if (position.x > gameactivity.canvaswidth - width)
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
            ////////////////////// BUZZBALL TO BALL COLLISION DETECTION /////////////////////////////////
			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++)
			{
                Ball currentball = ball.get(ballcounter);
				if ((currentball.position.x >= position.x) && (currentball.position.x <= position.x + width) && (currentball.position.y >= position.y) && (currentball.position.y <= position.y + height))
				{
					boolean collided = false;
					for (int ballcollidedcounter = (ballcollided.size() - 1); ballcollidedcounter >= 0; ballcollidedcounter--)
					{
						if (ballcollided.get(ballcollidedcounter) == ball.get(ballcounter)) collided = true;
					}

					if ((!collided) && (!dying))
					{

						if (currentball.goingleft)
						{
                            currentball.goingleft = false;
							xmovement = 2;
						}
						else 
						{
                            currentball.goingleft = true;
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
}
