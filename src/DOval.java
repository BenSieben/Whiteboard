import java.awt.*;

/**
 * DShape which draws an oval
 */
public class DOval extends DShape{

    /**
     * Creates a new DOval
     */
    public DOval() {
        super(new DOvalModel());
    }

    /**
     * Draws the DOval based on the current model
     * that is associated with the DOval
     * @param g the Graphics object needed to draw
     */
    @Override
    public void draw(Graphics g) {
        DOvalModel model = (DOvalModel)getModel();
        g.setColor(model.getColor());
        g.fillOval(model.getX(), model.getY(), model.getWidth(), model.getHeight());
    }
}
