package neutron;

import android.graphics.Bitmap;
import android.graphics.Point;

final public class RollingObjectBitmap 
{
	private RollingObjectFrame[] frames = new RollingObjectFrame[360];
	private short source_centerX, source_centerY, width, height;
	
	public RollingObjectBitmap(short centerX, short centerY, short width, short height)
	{
		this.source_centerX = centerX;
		this.source_centerY = centerY;
		this.height = height;
		this.width = width;
	}

	public RollingObjectFrame getFrame(short frame)
	{
		return frames[frame];
	}

	public void setFrame(Bitmap bitmap, short frame)
	{
		this.frames[frame] = new RollingObjectFrame();
		this.frames[frame].setBitmap(bitmap);
		this.frames[frame].setOffset((short)(source_centerX - bitmap.getWidth() / 2), (short)(source_centerY - bitmap.getHeight() /2));
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

    final public class RollingObjectFrame
    {
        private Point offset = new Point();
        private Bitmap bitmap;

        public Point getOffset()
        {
            return offset;
        }
        public void setOffset(short x, short y)
        {
            this.offset.set(x, y);
        }
        public Bitmap getBitmap()
        {
            return bitmap;
        }
        public void setBitmap(Bitmap bitmap)
        {
            this.bitmap = bitmap;
        }
    }
}
