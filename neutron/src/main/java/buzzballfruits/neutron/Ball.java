package buzzballfruits.neutron;

import android.graphics.Point;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

final class Ball implements Runnable
{
	GameActivity gameActivity;
	CopyOnWriteArrayList<Ball> balls = new CopyOnWriteArrayList<>(); // balls pointer
	Player[] players; // playerCount pointer

	float climb; // upward force
	float gravity; // downward force

	boolean dead = false; // balls is removed from balls list

	Point position = new Point();
	Point previousPosition = new Point(); // previous position

	Random rnd = new Random();
	int sidewaysSpeed = rnd.nextInt(5);
	boolean goingLeft; // is left balls direction
	boolean collide; // has collided
	int spawnWave = 0;
	boolean canBeTargeted = true;

	Ball(GameActivity gameActivity, CopyOnWriteArrayList<Ball> balls, Player[] players)
	{
		this.gameActivity = gameActivity;
		this.balls = balls;
		this.players = players;

		position.x = rnd.nextInt(gameActivity.canvasWidth);
		position.y = gameActivity.canvasHeight;
		climb = -(float) (rnd.nextInt(3) + 12); // upward force
		gravity = 0;

		int number = rnd.nextInt(3);
		goingLeft = number > 1;
		start();
	}

	private boolean checkCollision(Point object) // balls collision detection
	{
		return object.x <= position.x + gameActivity.ballSize - 1 && object.x >= position.x - gameActivity.ballSize - 1 && object.y <= position.y + gameActivity.ballSize - 1 && object.y >= position.y - gameActivity.ballSize - 1;
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Ball");
		thread.start();
	}

	public void run()
	{
		while (gameActivity.running && !dead)
		{
			///////////////////////////// BALL TO PLAYER COLLISION DETECTION //////////////////////////
			for (Player currentPlayer : players)
			{
				if (position.y >= currentPlayer.position.y && position.y <= currentPlayer.position.y + gameActivity.smileyHeight && position.x >= currentPlayer.position.x && position.x <= currentPlayer.position.x + gameActivity.smileyWidth && isNotGoingUp()) // playerCount to balls collision detection
				{
					climb = -(gravity * 2); // emulate gravity
					gravity = 0.0f;

					gameActivity.shockWaves.add(new ShockWave(position, ShockWave.EXTRA_SMALL_WAVE));
					goingLeft = currentPlayer.right;

					if (currentPlayer == players[0]) // if human players
					{
						gameActivity.gameScore++;
						gameActivity.doShake(40);
					}

					GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.POP, 1);

					sidewaysSpeed = rnd.nextInt(5);
					gameActivity.popups.add(new Popup(position, Popup.Type.BUMP, gameActivity.yeyStrings.length)); // popups text in score++
				}
			}
			//////////////////////// BALL TO BALL COLLISION DETECTION ///////////////////////////////
			for (Ball currentBall : balls)
			{
				if (this != currentBall && !collide) // if balls is not compared to itself and has not yet collided
				{
					if (checkCollision(currentBall.position)) // balls collision detected
					{
						GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.RESTART, 1);
						goingLeft = !goingLeft && !currentBall.goingLeft; // go right if bumped balls is going left
						currentBall.goingLeft = !goingLeft; // reverse direction of the bumped balls
						sidewaysSpeed = rnd.nextInt(5);
						currentBall.collide = true;
					}
				}
			}
			collide = false;
			////////////////////////// BALL MOVEMENT AND WORLD BOUNDARY INTERACTION //////////////////////////////////////
			previousPosition.x = position.x;
			previousPosition.y = position.y;

			position.y += climb + gravity * gravity;
			gravity += 0.05f;

			if (goingLeft)
			{
				position.x += sidewaysSpeed;
			}
			else
			{
				position.x -= sidewaysSpeed; // balls horizontal movement
			}

			if (spawnWave > 0) // spawn_wave animation
			{
				gameActivity.shockWaves.add(new ShockWave(position, ShockWave.SMALL_WAVE));
				spawnWave--;
			}

			if (position.x < 0) // balls has reached left wall
			{
				goingLeft = true;
				GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.POP_WALL, 1);
			}

			if (position.x > gameActivity.canvasWidth) // balls has reached right wall
			{
				goingLeft = false;
				GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.POP_WALL, 1);
			}

			if (position.y > gameActivity.canvasHeight) // balls has fallen off screen
			{
				gameActivity.life--;
				gameActivity.popups.add(new Popup(position, Popup.Type.YEY, gameActivity.booStrings.length));
				GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.DOWN, 1);
				gameActivity.doShake(100);

				try
				{
					Thread.sleep(1000);
				}

				catch (InterruptedException interuptedException)
				{
					interuptedException.printStackTrace();
					Log.e("Ball", interuptedException.toString());
				}
				dead = true;
				GameActivity.RESOURCEMANAGER.playSound(ResourceManager.Sound.DOWN, 1);
			}

			else // balls trailer effects
			{
				gameActivity.trails.add(new Trail(previousPosition, position));
				gameActivity.shockWaves.add(new ShockWave(position, ShockWave.EXTRA_SMALL_WAVE));
			}

			try
			{
				Thread.sleep(40);
			}

			catch (InterruptedException interruptedException)
			{
				interruptedException.printStackTrace();
				Log.e("Ball", interruptedException.toString());
			}
		}
	}

	public boolean isNotGoingUp()
	{
		return (previousPosition.y <= position.y);
	}
}