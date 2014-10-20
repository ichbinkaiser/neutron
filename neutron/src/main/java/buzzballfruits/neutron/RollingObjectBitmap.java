package buzzballfruits.neutron;

import android.graphics.Bitmap;
import android.graphics.Point;

final public class RollingObjectBitmap 
{
	RollingObjectFrame[] frames = new RollingObjectFrame[360];
	int source_centerX, source_centerY, width, height;
	
	RollingObjectBitmap(int centerX, int centerY, int width, int height)
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
		this.frames[frame].bitmap = bitmap;
		this.frames[frame].setOffset(source_centerX - bitmap.getWidth() / 2, source_centerY - bitmap.getHeight() / 2);
	}
	
	public void setFrame(RollingObjectFrame duplicate, int frame) //dublicate frame
	{
		this.frames[frame] = duplicate;
	}

    final public class RollingObjectFrame
    {
        Point offset = new Point();
        Bitmap bitmap;

        public void setOffset(int x, int y)
        {
            offset.set(x, y);
        }
    }
}
