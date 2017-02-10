import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores coordinate information for single
 * DShapes
 */
public class DShapeModel {

    private int x, y, width, height; // the four values for drawing DShapes
    private Color color; // the color of the shape
    private List<ModelListener> modelListenerList; // list of listeners of this DShape

    // special field for networking purposes
    private int id;

    /**
     * Constructs a new generic DShapeModel
     */
    public DShapeModel() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        color = Color.GRAY;
        modelListenerList = new ArrayList<ModelListener>();
        id = -1;
    }

    /**
     * Returns the current "x-coordinate" of the model
     * @return the current "x-coordinate" of the model
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the current "y-coordinate" of the model
     * @return the current "y-coordinate" of the model
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the current "width" of the model
     * @return the current "width" of the model
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the current "height" of the model
     * @return the current "height" of the model
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the current Color of the model
     * @return the current Color of the model
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the id of this model
     * @return the id of this model
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the "x-coordinate" of the model to the argument
     * @param x the new "x-coordinate" of the model
     */
    public void setX(int x) {
        if(this.x != x) {
            this.x = x;
            notifyModelListeners();
        }
    }

    /**
     * Sets the "y-coordinate" of the model to the argument
     * @param y the new "y-coordinate" of the model
     */
    public void setY(int y) {
        if(this.y != y) {
            this.y = y;
            notifyModelListeners();
        }
    }

    /**
     * Sets the "width" of the model to the argument
     * @param width the new "width" of the model
     */
    public void setWidth(int width) {
        if(this.width != width) {
            this.width = width;
            notifyModelListeners();
        }
    }

    /**
     * Sets the "height" of the model to the argument
     * @param height the new "height" of the model
     */
    public void setHeight(int height) {
        if(this.height != height) {
            this.height = height;
            notifyModelListeners();
        }
    }

    /**
     * Sets the Color of the model to the argument
     * @param color the new Color of the model
     */
    public void setColor(Color color) {
        if(color != null) {
            this.color = color;
            notifyModelListeners();
        }
    }

    /**
     * Sets the id of the model to the argument
     * @param id the new id of the model
     */
    public void setId(int id) {
        this.id = id;
        notifyModelListeners();
    }

    /**
     * Notifies all current model listeners that
     * a change has occurred to the data of the model
     */
    protected void notifyModelListeners() {
        for(ModelListener modelListener : modelListenerList) {
            modelListener.modelChanged(this);
        }
    }

    /**
     * Returns a bounding rectangle of the generic shape model
     * @return a bounding rectangle of the generic shape model
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Adds a new model listener to listen to this model
     * @param listener the model listener to have listen to this model
     */
    public void addModelListener(ModelListener listener) {
        if(listener != null) {
            modelListenerList.add(listener);
        }
    }

    /**
     * Removes the parameter model listener from this model
     * @param listener the model listener to have stop listening to this model
     */
    public void removeModelListener(ModelListener listener) {
        for(int i = modelListenerList.size() - 1; i >= 0; i--) {
            if(listener.equals(modelListenerList.get(i))) { // remove the ModelListener that matches the parameter
                modelListenerList.remove(i);
                break;
            }
        }
    }

    /**
     * Makes this model copy the attributes
     * of the parameter model
     * @param other the model to copy the parameters of
     */
    public void mimic(DShapeModel other) {
        this.x = other.getX();
        this.y = other.getY();
        this.width = other.getWidth();
        this.height = other.getHeight();
        this.color = other.getColor();
        this.id = other.getId();
        notifyModelListeners();
    }

    /**
     * Returns a string representation of the model
     * @return a string representation of the model
     */
    @Override
    public String toString() {
        return "Shape model: id = " + id + ";x = " + x + "; y = " + y + "; width = " + width +
                "; height = " + height + "; color = " + color;
    }
}
