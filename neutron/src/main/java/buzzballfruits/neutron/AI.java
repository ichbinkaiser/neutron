package buzzballfruits.neutron;

import android.graphics.Point;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

final class AI implements Runnable
{
	//test
	GameActivity gameActivity;
	CopyOnWriteArrayList<Ball> ball = new CopyOnWriteArrayList<>();

	Point playerPosition = new Point(); // Player location
	Point target = new Point(); // top balls threat

	AI(GameActivity gameActivity, CopyOnWriteArrayList<Ball> ball, Player player)
	{
		this.gameActivity = gameActivity;
		this.ball = ball;
		playerPosition = player.position;
		start();
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		target.x = gameActivity.canvasWidth / 2;

		////////////////////////// AI TARGETING LOGI ///////////////////////////////////////
		while (gameActivity.running) // AI Thread
		{
			target.y = 0;
			for (Ball currentBall : ball)
			{
				currentBall.canBeTargeted = currentBall.position.x <= playerPosition.x && currentBall.position.x <= playerPosition.x + gameActivity.smileyWidth; // if not true balls is a valid AI target

				if (!currentBall.dead && currentBall.isNotGoingUp() && currentBall.canBeTargeted && currentBall.position.y > target.y)
				{
					target.set(currentBall.position.x - gameActivity.smileyWidth / 2, currentBall.position.y - gameActivity.smileyHeight / 2); // check for priority target
				}
			}
			gameActivity.players[1].setDestination(target.x); // set AI target

			try
			{
				Thread.sleep(10);
			}

			catch (InterruptedException interruptedException)
			{
				interruptedException.printStackTrace();
				Log.e("AI", interruptedException.toString());
			}
		}
	}
}