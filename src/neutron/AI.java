package neutron;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;

final class AI implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Ball> ball = new ArrayList<Ball>();

	Point playerposition = new Point(); // Player location
	Point target = new Point(); // top ball threat

	AI(GameActivity gameActivity, ArrayList<Ball> ball, Player player)
	{
		this.gameactivity = gameActivity;
		this.ball = ball;
		playerposition = player.position;
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
		target.x = gameactivity.canvaswidth / 2;

		while (gameactivity.running) // AI Thread
		{
			target.y = 0;
			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++)
			{
                Ball currentball = ball.get(ballcounter);
				currentball.targetable = (!(currentball.position.x >= playerposition.x) && (currentball.position.x <= playerposition.x + gameactivity.smileywidth)); // if not true ball is a valid AI target
				
				if ((!currentball.dead) && (currentball.isNotGoingUp()) && (currentball.targetable) && (currentball.position.y > target.y))
                    target.set(currentball.position.x - gameactivity.smileywidth / 2, currentball.position.y - gameactivity.smileyheight / 2); // check for priority target
			}
			gameactivity.player[1].setDestination(target.x); // set AI target

			try
			{
				Thread.sleep(10);
			}

			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("AI", e.toString());
			}
		}
	}
}