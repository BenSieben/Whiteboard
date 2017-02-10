import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of all drawable shapes
 * in the whiteboard program
 */
public class DShape implements ModelListener{

    private DShapeModel model; // pointer to a DShapeModel, which holds information on how to draw shapes

    /**
     * Constructs a new generic DShape
     */
    public DShape() {
        model = null;
    }

    /**
     * Constructs a new generic DShape, connecting
     * it to the specified DShapeModel
     * @param model the DShapeModel to associate
     *              with this DShape
     */
    protected DShape(DShapeModel model) {
        this.model = model;
        this.model.addModelListener(this);
    }

    /**
     * Draws the generic DShape (does nothing)
     * @param g the Graphics object needed to draw
     *          DShape's subclasses
     */
    public void draw(Graphics g) {
        // do nothing, as we are a DShape which should never be drawn (only subclasses should be drawn)
    }

    /**
     * Returns the DShapeModel connected with this DShape
     */
    public DShapeModel getModel() {
        return model;
    }

    /**
     * Sets the DShapeModel connected with this DShape to
     * be the parameter
     * @param model the DShapeModel to connect with this DShape
     */
    public void setModel(DShapeModel model) {
        if(this.model != null) {
            this.model.removeModelListener(this);
        }
        this.model = model;
        this.model.addModelListener(this);
    }

    /**
     * Returns the Rectangle that creates the basic boundaries
     * of the DShape
     * @return the Rectangle that bounds this DShape
     */
    public Rectangle getBounds() {
        return model.getBounds();
    }

    /**
     * Gets the points for all the knobs associated
     * with this DShape
     * @return the points for all the knobs associated
     * with this DShape
     */
    public List<Point> getKnobs() {
        List<Point> knobs = new ArrayList<Point>();
        knobs.add(new Point(model.getX(), model.getY())); // "upper-left" knob
        knobs.add(new Point(model.getX() + model.getWidth(), model.getY())); // "upper-right" knob
        knobs.add(new Point(model.getX(), model.getY() + model.getHeight())); // "lower-left" knob
        knobs.add(new Point(model.getX() + model.getWidth(), model.getY() + model.getHeight())); // "lower-right" knob
        return knobs;
    }

    /**
     * Returns a String representation of the DShape, describing all
     * the qualities of the connected model
     * @return a string representation of the DShape
     */
    public String toString() {
        return "DShape: x = " + model.getX() + "; y = " + model.getY() + "; width = " + model.getWidth() +
                "; height = " + model.getHeight() + "; color = " + model.getColor();
    }

    /**
     * Listener method that gets activated when the connected
     * model for this DShape sends an alert that it has been changed
     * @param model the DShapeModel that sent the notification message
     */
    @Override
    public void modelChanged(DShapeModel model) {
        // draw();
        //System.err.println("DShape changed: " + toString());
    }
}
