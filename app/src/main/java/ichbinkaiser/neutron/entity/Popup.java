package ichbinkaiser.neutron.entity;

import android.graphics.Point;

import java.util.Random;

public class Popup {

    Point position = new Point();
    int life = 255; // animation index life
    PopupType type; // popups message type
    int textIndex; //random text index

    Popup(Point position, PopupType type, int textIndexSize) {
        Random rnd = new Random();
        if (textIndexSize > 0) {
            textIndex = rnd.nextInt(textIndexSize);
        }

        this.type = type;
        this.position.x = position.x;
        this.position.y = position.y - 255;
    }

    public int getLife() {
        return life--;
    }

    public PopupType getType() {
        return type;
    }

    public Point getPosition() {
        return position;
    }

    public int getTextIndex() {
        return textIndex;
    }
}
