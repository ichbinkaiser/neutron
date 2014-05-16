package neutron;

import android.graphics.Bitmap;
import android.graphics.Point;

final public class RollingObjectFrame 
{
	private Point offset = new Point();
	private Bitmap bitmap;

	public Point getOffset() 
	{
		return offset;
	}
	public void setOffset(int x, int y) 
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
