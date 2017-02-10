import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DShape which draws a line
 */
public class DLine extends DShape{

    /**
     * Constructs a new DLine for displaying on the canvas
     */
    public DLine() {
        super(new DLineModel());
    }

    /**
     * Draws the line on the canvas based on the current
     * model associated with this line
     * @param g the Graphics object to use for drawing
     */
    @Override
    public void draw(Graphics g) {
        DLineModel model = (DLineModel)getModel();
        g.setColor(model.getColor());
        g.drawLine(model.getP1().x, model.getP1().y, model.getP2().x, model.getP2().y);
    }

    /**
     * Returns the knobs of the DLine (one for
     * each of the two points that make up the line)
     * @return the knobs of the DLine
     */
    @Override
    public List<Point> getKnobs() {
        List<Point> knobs = new ArrayList<Point>();
        DLineModel model = (DLineModel)getModel();
        knobs.add(model.getP1()); // first point knob
        knobs.add(model.getP2()); // second point knob
        return knobs;
    }
}
