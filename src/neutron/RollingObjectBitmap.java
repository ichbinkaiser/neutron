package neutron;

import android.graphics.Bitmap;

final public class RollingObjectBitmap 
{
	private RollingObjectFrame[] frames = new RollingObjectFrame[360];
	private int source_centerX, source_centerY, width, height;
	
	public RollingObjectBitmap(int centerX, int centerY, int width, int height)
	{
		this.source_centerX = centerX;
		this.source_centerY = centerY;
		this.height = height;
		this.width = width;
	}

	public RollingObjectFrame getFrame(int frame)
	{
		return frames[frame];
	}

	public void setFrame(Bitmap bitmap, int frame)
	{
		this.frames[frame] = new RollingObjectFrame();
		this.frames[frame].setBitmap(bitmap);
		this.frames[frame].setOffset(source_centerX - bitmap.getWidth() / 2, source_centerY - bitmap.getHeight() /2);
	}
	
	public void setFrame(RollingObjectFrame duplicate, int frame) //dublicate frame
	{
		this.frames[frame] = duplicate;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

}
