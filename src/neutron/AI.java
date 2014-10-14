package neutron;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;

final class AI implements Runnable
{
	private GameActivity gameactivity;
	private ArrayList<Ball> ball = new ArrayList<Ball>();
	private Point playerposition = new Point(); // Player location
	private Point target = new Point(); // top ball threat

	AI(GameActivity gameActivity, ArrayList<Ball> ball, Player player)
	{
		this.gameactivity = gameActivity;
		this.ball = ball;
		playerposition = player.getPosition();
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		target.x = gameactivity.getCanvasWidth() / 2;

		while(gameactivity.isRunning()) // AI Thread
		{
			target.y = 0;
			for (int ballcounter = ball.size() - 1; ballcounter >= 0; ballcounter--)
			{
                Ball currentball = ball.get(ballcounter);
				if ((currentball.getPosition().x >= playerposition.x) && (currentball.getPosition().x <= playerposition.x + gameactivity.getSmileyWidth()))
                    currentball.setTargetable(false);
				else
                    currentball.setTargetable(true); // if true ball a valid AI target
				
				if ((!currentball.isDead()) && (currentball.isNotGoingUp()) && (currentball.isTargetable()) && (currentball.getPosition().y > target.y))
					target.set(currentball.getPosition().x - gameactivity.getSmileyWidth() / 2, currentball.getPosition().y - gameactivity.getSmileyHeight() / 2); // check for priority target
			}
			gameactivity.getPlayer()[1].setDestination(target.x); // set AI target

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