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
				if ((ball.get(ballcounter).getPosition().x >= playerposition.x) && (ball.get(ballcounter).getPosition().x <= playerposition.x + gameactivity.getSmileyWidth()))
					ball.get(ballcounter).setTargetable(false);
				else
					ball.get(ballcounter).setTargetable(true); // if true ball a valid AI target
				
				if ((!ball.get(ballcounter).isDead()) && (ball.get(ballcounter).isNotGoingUp()) && (ball.get(ballcounter).isTargetable()) && (ball.get(ballcounter).getPosition().y > target.y))
					target.set(ball.get(ballcounter).getPosition().x - gameactivity.getSmileyWidth() / 2, ball.get(ballcounter).getPosition().y - gameactivity.getSmileyHeight() / 2); // check for priority target
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