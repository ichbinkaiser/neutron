package ichbinkaiser.neutron.entity;

import android.graphics.Bitmap;
import android.graphics.Point;

public class RollingObjectBitmap {
    RollingObjectFrame[] frames = new RollingObjectFrame[360];
    int source_centerX, source_centerY, width, height;

    public RollingObjectBitmap(int centerX, int centerY, int width, int height) {
        this.source_centerX = centerX;
        this.source_centerY = centerY;
        this.height = height;
        this.width = width;
    }

    public RollingObjectFrame getFrame(int frame) {
        return frame < 0 || frame > 360 ? frames[0] : frames[frame];
    }

    public void setFrame(Bitmap bitmap, int frame) {
        this.frames[frame] = new RollingObjectFrame();
        this.frames[frame].bitmap = bitmap;
        this.frames[frame].setOffset(source_centerX - bitmap.getWidth() / 2, source_centerY - bitmap.getHeight() / 2);
    }

    public void setFrame(RollingObjectFrame duplicate, int frame) //dublicate frame
    {
        this.frames[frame] = duplicate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public class RollingObjectFrame {
        Point offset = new Point();
        Bitmap bitmap;

        public void setOffset(int x, int y) {
            offset.set(x, y);
        }

        public Point getOffset() {
            return offset;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

    }
}
