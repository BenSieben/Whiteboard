import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * The Canvas class is a JPanel which
 * is where users will be able to draw
 * in the program
 */
public class Canvas extends JPanel{

    public static final int KNOB_SIZE = 9; // number of pixels for knobs
    public static final int NOT_DRAGGING = 0; // dragMode is this if no dragging was going on before
    public static final int DRAG_MOVING = 1; // dragMode is this if dragging is currently moving a shape
    public static final int DRAG_RESIZING = 2; // dragMode is this if dragging is currently resizing a shape
    public static final int DRAGGING_NOTHING = 3; // dragMode is this if dragging is currently dragging nothing

    private List<DShape> shapeList; // list of all shapes to draw on the Canvas
    private DShape selected; // currently selected shape on the canvas
    private int dragMode; // keeps track of whether or not a drag is in progress and if so, what kind of drag is in progress
    private int xOffset, yOffset; // keeps track of offset when dragging a shape around
    private int xAnchor, yAnchor; // keeps track of model anchor when resizing a shape

    /**
     * Constructs a new Canvas for drawing shapes
     */
    public Canvas() {
        shapeList = new ArrayList<DShape>();
        selected = null;
        dragMode = NOT_DRAGGING;
        xOffset = 0;
        yOffset = 0;
        xAnchor = 0;
        yAnchor = 0;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
        setFocusable(true);
    }

    /**
     * Paints all the shapes currently on the canvas
     * @param g the Graphics object used for drawing all the shapes
     */
    @Override
    public void paintComponent(Graphics g) {
        try {
            List<Point> knobs = null;
            super.paintComponent(g);
            for(DShape shape : shapeList) {
                shape.draw(g);
                if(shape == selected) { // do knob painting for the selected shape
                    knobs = selected.getKnobs();
                }
        /* The following code draws the bounding rectangles and a line across it (for debugging)
        g.setColor(Color.GRAY);
        Rectangle r = shape.getBounds();
        g.drawRect(r.x, r.y, r.width, r.height);
        g.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
        */
            }

            if(knobs != null) { // draw knobs at the end if there is currently a selected shape
                for(Point p : knobs) {
                    g.setColor(Color.BLACK);
                    g.fillRect(p.x - (KNOB_SIZE / 2), p.y - (KNOB_SIZE / 2), KNOB_SIZE, KNOB_SIZE);
                }
            }
        }
        catch(ConcurrentModificationException ex) {
            // this error sometimes occurs when trying to repaint many whiteboards at the same time
            // As far as can be observed, this error does not break the painting of the canvas in any way
            // and is essentially harmless
            // For that reason it has been chosen to simply do nothing when this exception occurs

            //System.err.println("Error: concurrent modification exception in paintComponent method of Canvas" +
            //        "(note: as far as can be observed, this error does not negatively harm the program at all)");
            //ex.printStackTrace();
        }
    }

    /**
     * Adds a new shape to the canvas, if the parameter
     * shape model is valid
     * @param model the shape model to make a shape object for
     */
    public void addShape(DShapeModel model) {
        if(model != null) {
            DShape shape = null; // the DShape to hold whatever shape correlates to the parameter model
            if(model instanceof DRectModel) { // make a rectangle and add it to the shape list
                shape = new DRect();
            }
            else if(model instanceof DOvalModel) { // make an oval and add it to the shape list
                shape = new DOval();
            }
            else if(model instanceof DLineModel) { // make a line and add it to the shape list
                shape = new DLine();
            }
            else if(model instanceof DTextModel) { // make a text and add it to the shape list
                shape = new DText();
            }

            //Add the shape if an appropriate DShapeModel was found
            if(shape != null) {
                shape.setModel(model);
                shapeList.add(shape);

                // set the just-created shape to be the selected shape
                selected = shapeList.get(shapeList.size() - 1);
            }
            repaint();
        }
    }

    /**
     * Returns the current list of shapes in the canvas
     * @return the current list of shapes in the canvas
     */
    public List<DShape> getShapeList() {
        return shapeList;
    }

    /**
     * Returns the currently selected shape on the canvas
     * @return the currently selected shape on the canvas
     * (null if nothing is selected)
     */
    public DShape getSelectedShape() {
        return selected;
    }

    /**
     * Sets the currently selected shape to be the parameter
     * shape
     * @param selectedShape the shape to select on the canvas
     */
    public void setSelectedShape(DShape selectedShape) {
        selected = selectedShape;
    }

    /**
     * Resets the selected shape on the canvas to null
     * to deselect any shape that might have been selected
     */
    public void resetSelectedShape() {
        selected = null;
    }

    /**
     * Tells the canvas to stop acting as if
     * something is being dragged on the screen
     */
    public void stopDragging() {
        dragMode = NOT_DRAGGING;
        xOffset = 0;
        yOffset = 0;
    }

    /**
     * Returns the current drag mode of the canvas
     * @return the current drag mode of the canvas
     */
    public int getDragMode() {
        return dragMode;
    }

    /**
     * Sets the drag mode to the parameter integer
     * @param dragMode the new drag mode to set the canvas to
     */
    public void setDragMode(int dragMode) {
        this.dragMode = dragMode;
    }

    /**
     * Sets the anchor of the canvas (for resizing) to be at the
     * specified coordinates
     * @param x the x-coordinate of the anchor
     * @param y the y-coordinate of the anchor
     */
    public void setAnchor(int x, int y) {
        xAnchor = x;
        yAnchor = y;
    }

    /**
     * Sets the offset of canvas (for moving) to be at
     * the specified coordinates
     * @param x the x-coordinate of the offset point
     * @param y the y-coordinate of the offset point
     */
    public void setOffset(int x, int y) {
        xOffset = x;
        yOffset = y;
    }

    /**
     * Returns the current x-coordinate of the offset on the canvas
     * @return the current x-coordinate of the offset on the canvas
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Returns the current y-coordinate of the offset on the canvas
     * @return the current y-coordinate of the offset on the canvas
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Returns the current x-coordinate of the anchor on the canvas
     * @return the current x-coordinate of the anchor on the canvas
     */
    public int getXAnchor() {
        return xAnchor;
    }

    /**
     * Returns the current y-coordinate of the anchor on the canvas
     * @return the current y-coordinate of the anchor on the canvas
     */
    public int getYAnchor() {
        return yAnchor;
    }

    /**
     * Clears the canvas of all shapes,
     * essentially resetting the canvas completely
     */
    public void clearCanvas() {
        shapeList = new ArrayList<DShape>();
        selected = null;
        dragMode = NOT_DRAGGING;
        xOffset = 0;
        yOffset = 0;
        xAnchor = 0;
        yAnchor = 0;
        repaint();
    }

    /**
     * Returns the BufferedImage created from the current
     * state of the Canvas
     * @return
     */
    public BufferedImage getBufferedImage() {
        // set selected to nothing (to not draw knobs in image)
        resetSelectedShape();
        repaint();

        // get buffered image of current canvas appearance
        BufferedImage image = (BufferedImage)createImage(getWidth(), getHeight());

        // get graphics object pointing to buffered image, and call paintAll
        Graphics imageGraphics = image.getGraphics();
        paintAll(imageGraphics);
        imageGraphics.dispose();

        return image;
    }
}
