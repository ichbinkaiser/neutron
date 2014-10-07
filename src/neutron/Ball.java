package neutron;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Point;
import android.util.Log;

final class Ball implements Runnable
{
	private GameActivity gameactivity;
	private ArrayList<Ball> ball = new ArrayList<Ball>(); // ball pointer
	private Player[] player; // player pointer
	private float climb; // upward force
	private float gravity; // downward force
	private boolean dead = false; // ball is removed from ball list
	private Point position = new Point();
	private Point pposition = new Point(); // previous position
	private Random rnd = new Random();
	private int sidewardspeed = rnd.nextInt(5);
	private boolean goingleft; // is left ball direction
	private boolean collide; // has collided
	private int spawnwave = 0;
	private boolean targetable = true;

	Ball(GameActivity gameActivity, ArrayList<Ball> ball, Player[] player)
	{
		this.gameactivity = gameActivity;
		this.ball = ball;
		this.player = player;

		position.x = rnd.nextInt(gameActivity.getCanvasWidth());
		position.y = gameActivity.getCanvasHeight();
		climb = (float) -(rnd.nextInt(3) + 12); // upward force
		gravity = 0;

		int number = rnd.nextInt(3);

        goingleft = (number > 1);
		start();
	}

	private boolean checkCollision(Point object) // ball collision detection
	{
		if (((object.x <= getPosition().x + gameactivity.getBallSize() - 1) && (object.x >= getPosition().x - gameactivity.getBallSize() - 1) && ((object.y <= getPosition().y + gameactivity.getBallSize() - 1) && (object.y >= getPosition().y - gameactivity.getBallSize() - 1))))
			return true;
		else
			return false;
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Ball");
		thread.start();
	}

	public void run()
	{
		while((gameactivity.isRunning()) && (!dead))
		{
			for (int playercounter = 0; playercounter < player.length; playercounter++) // player collision logic
			{
				if ((position.y >= player[playercounter].getPosition().y) && (position.y <= player[playercounter].getPosition().y + gameactivity.getSmileyHeight()) && (position.x >= player[playercounter].getPosition().x) && (position.x <= player[playercounter].getPosition().x + gameactivity.getSmileyWidth()) && (isNotGoingUp())) // player to ball collision detection
				{	
					climb = -(gravity * 2); // emulate gravity
					gravity = 0.0f;
					
					if (player[playercounter].getPposition().y > player[playercounter].getPosition().y)
						climb -= 6;
					
					gameactivity.getShockwave().add(new Shockwave(position, 0));

					if (player[playercounter].isRight())
						goingleft = true;
					else
						goingleft = false; // player.right toggle

					if (playercounter == 0)
					{
						gameactivity.setGameScore(gameactivity.getGameScore() + 1);
						gameactivity.doShake(40);
					}
					GameActivity.getResourceManager().playSound(1, 1);

					sidewardspeed = rnd.nextInt(5);
					gameactivity.getPopup().add(new Popup(position, 2)); // popup text in score++
				}
			}

			for (int ballcounter = ball.size() - 1; ballcounter >= 0; ballcounter--) // ball to ball collision detection
			{
				if ((this != ball.get(ballcounter)) && (!collide)) // if ball is not compared to itself and has not yet collided
				{
					if (checkCollision(ball.get(ballcounter).getPosition())) // ball collision detected
					{
						GameActivity.getResourceManager().playSound(6, 1);
						if ((goingleft) && (!ball.get(ballcounter).goingleft)) 
						{
							goingleft = false;
							ball.get(ballcounter).goingleft = true;
						}
						else 
						{
							goingleft = true;
							ball.get(ballcounter).goingleft = false;
						}
						sidewardspeed = rnd.nextInt(5);
						ball.get(ballcounter).collide = true;
					}
				}
			}
			collide = false;

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
				gameactivity.getShockwave().add(new Shockwave(position, 1));
				spawnwave--;
			}

			if (position.x < 0) // ball has reached left wall
			{
				goingleft = true;
				GameActivity.getResourceManager().playSound(4, 1);
			}

			if (position.x > gameactivity.getCanvasWidth()) // ball has reached right wall
			{
				goingleft = false;
				GameActivity.getResourceManager().playSound(4, 1);
			}

			if (position.y > gameactivity.getCanvasHeight()) // ball has fallen off screen
			{
				gameactivity.setLife(gameactivity.getLife() - 1);
				gameactivity.getPopup().add(new Popup(position, 1));
				GameActivity.getResourceManager().playSound(2, 1);
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
				GameActivity.getResourceManager().playSound(8, 1);
			}
			else
			{
				gameactivity.getTrail().add(new Trail(pposition, position));
				gameactivity.getShockwave().add(new Shockwave(position, 0));
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

	public Point getPosition() 
	{
		return position;
	}

	public boolean isGoingLeft()
	{
		return goingleft;
	}

	public boolean isTargetable()
	{
		return targetable;
	}

	public void setTargetable(boolean targetable)
	{
		this.targetable = targetable;
	}

	public void setGoingleft(boolean goingleft)
	{
		this.goingleft = goingleft;
	}

	public void setPosition(Point position) 
	{
		this.position = position;
	}

	public boolean isNotGoingUp()
	{
		return (pposition.y <= position.y);
	}

	public boolean isDead()
	{
		return dead;
	}
}