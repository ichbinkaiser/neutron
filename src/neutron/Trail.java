package neutron;

import android.graphics.Point;

final class Trail 
{
	private Point startpoint = new Point(); //start point
	private Point endpoint = new Point(); //end point
	private int counter = 10; // animation index
	
	Trail(Point startpoint, Point endpoint)
	{
		this.startpoint.x = startpoint.x;
		this.startpoint.y = startpoint.y;
		this.endpoint.x = endpoint.x;
		this.endpoint.y = endpoint.y;
	}
	
	int calcSize()
	{
		switch (counter)
		{
		case 9:
		case 8:
		case 7:
			return 1;
		case 6:
		case 5:
		case 4:
			return 2;
		case 3:
		case 2:
		case 1:
			return 3;
		default:
			return 0;
		}
	}
	
	public int getCounter() 
	{
		return counter;
	}

	public void setCounter(int counter) 
	{
		this.counter = counter;
	}

	public Point getStartPoint() 
	{
		return startpoint;
	}

	public Point getEndPoint() 
	{
		return endpoint;
	}
}
