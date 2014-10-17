package buzzballfruits.neutron;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Ball implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Ball> ball = new ArrayList<Ball>(); // ball pointer
	Player[] player; // player pointer

	float climb; // upward force
	float gravity; // downward force

	boolean dead = false; // ball is removed from ball list

	Point position = new Point();
	Point pposition = new Point(); // previous position

    Random rnd = new Random();
	int sidewardspeed = rnd.nextInt(5);
	boolean goingleft; // is left ball direction
	boolean collide; // has collided
	int spawnwave = 0;
	boolean targetable = true;

	Ball(GameActivity gameActivity, ArrayList<Ball> ball, Player[] player)
	{
		this.gameactivity = gameActivity;
		this.ball = ball;
		this.player = player;

		position.x = rnd.nextInt(gameActivity.canvaswidth);
		position.y = gameActivity.canvasheight;
		climb = -(float)(rnd.nextInt(3) + 12); // upward force
		gravity = 0;

		int number = rnd.nextInt(3);
        goingleft = (number > 1);
		start();
	}

	private boolean checkCollision(Point object) // ball collision detection
	{
		return (((object.x <= position.x + gameactivity.ballsize - 1) && (object.x >= position.x - gameactivity.ballsize - 1) && ((object.y <= position.y + gameactivity.ballsize - 1) && (object.y >= position.y - gameactivity.ballsize - 1))));
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Ball");
		thread.start();
	}

	public void run()
	{
		while ((gameactivity.running) && (!dead))
		{
            ///////////////////////////// BALL TO PLAYER COLLISION DETECTION //////////////////////////
			for (int playercounter = 0; playercounter < player.length; playercounter++)
			{
				if ((position.y >= player[playercounter].position.y) && (position.y <= player[playercounter].position.y + gameactivity.smileyheight) && (position.x >= player[playercounter].position.x) && (position.x <= player[playercounter].position.x + gameactivity.smileywidth) && (isNotGoingUp())) // player to ball collision detection
				{	
					climb = -(gravity * 2); // emulate gravity
					gravity = 0.0f;
					
					if (player[playercounter].position.y > player[playercounter].position.y)
						climb -= 6;
					
					gameactivity.shockwave.add(new Shockwave(position, Shockwave.EXTRA_SMALL_WAVE));

                    goingleft = player[playercounter].right;

					if (playercounter == 0)
					{
						gameactivity.gamescore++;
						gameactivity.doShake(40);
					}

					GameActivity.resourcemanager.playSound(ResourceManager.POP, 1);

					sidewardspeed = rnd.nextInt(5);
					gameactivity.popup.add(new Popup(position, Popup.SOLO, gameactivity.bumpstrings.length)); // popup text in score++
				}
			}
            //////////////////////// BALL TO BALL COLLSION DETECTION ///////////////////////////////
			for (int ballcounter = 0; ballcounter < 0; ballcounter++)
			{
                Ball currentball = ball.get(ballcounter);
				if ((this != currentball) && (!collide)) // if ball is not compared to itself and has not yet collided
				{
					if (checkCollision(currentball.position)) // ball collision detected
					{
						GameActivity.resourcemanager.playSound(ResourceManager.RESTART, ResourceManager.HIT);
                        goingleft = !((goingleft) && (!currentball.goingleft)); // go right if bumped ball is going left
                        currentball.goingleft = !goingleft; // reverse direction of the bumped ball
						sidewardspeed = rnd.nextInt(5);
                        currentball.collide = true;
					}
				}
			}
			collide = false;
            ////////////////////////// BALL MOVEMENT AND WORLD BOUNDARY INTERACTION //////////////////////////////////////
			pposition.x = position.x;
			pposition.y = position.y;

			position.y += climb + gravity * gravity;
			gravity += 0.05f;

			if (goingleft)
				position.x += sidewardspeed;
			else
				position.x -= sidewardspeed; // ball horizontal movement

			if (spawnwave > 0) // spawn_wave animation
			{
				gameactivity.shockwave.add(new Shockwave(position, Shockwave.SMALL_WAVE));
				spawnwave--;
			}

			if (position.x < 0) // ball has reached left wall
			{
				goingleft = true;
				GameActivity.resourcemanager.playSound(ResourceManager.POPWALL, 1);
			}

			if (position.x > gameactivity.canvaswidth) // ball has reached right wall
			{
				goingleft = false;
				GameActivity.resourcemanager.playSound(ResourceManager.POPWALL, 1);
			}

			if (position.y > gameactivity.canvasheight) // ball has fallen off screen
			{
				gameactivity.life--;
				gameactivity.popup.add(new Popup(position, 1, gameactivity.lostlifestrings.length));
				GameActivity.resourcemanager.playSound(ResourceManager.DOWN, 1);
				gameactivity.doShake(100);

				try 
				{
					Thread.sleep(1000);
				}

				catch (InterruptedException e) 
				{
					e.printStackTrace();
					Log.e("Ball", e.toString());
				}
				dead = true;
				GameActivity.resourcemanager.playSound(ResourceManager.DOWN, 1);
			}

			else // ball trailer effects
			{
				gameactivity.trail.add(new Trail(pposition, position));
				gameactivity.shockwave.add(new Shockwave(position, Shockwave.EXTRA_SMALL_WAVE));
			}

			try
			{
				Thread.sleep(40);
			}

			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("Ball", e.toString());
			}
		}
	}

	public boolean isNotGoingUp()
	{
		return (pposition.y <= position.y);
	}
}