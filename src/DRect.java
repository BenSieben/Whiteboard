import java.awt.*;

/**
 * DShape which draws a rectangle
 */
public class DRect extends DShape{

    /**
     * Constructs a new DRect
     */
    public DRect() {
        super(new DRectModel());
    }

    /**
     * Draws the DRect based on the current model
     * connected to the DRect
     * @param g the Graphics object needed to draw
     */
    @Override
    public void draw(Graphics g) {
        DRectModel model = (DRectModel)getModel();
        g.setColor(model.getColor());
        g.fillRect(model.getX(), model.getY(), model.getWidth(), model.getHeight());
    }
}
