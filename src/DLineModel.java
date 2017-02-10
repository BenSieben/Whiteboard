import java.awt.*;

/**
 * The model for drawing lines
 */
public class DLineModel extends DShapeModel{

    private Point p1, p2; // the two points that connect to draw the line

    /**
     * Constructs a new DLineModel
     */
    public DLineModel() {
        super();
        p1 = new Point(10, 10);
        p2 = new Point(30, 30);
    }

    /**
     * Returns the Rectangle that acts as the bounds
     * of the line
     * @return a Rectangle defining the bounds of the line
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
    }

    /**
     * Returns the first point that creates
     * this line
     * @return the first point of this line
     */
    public Point getP1(){
        return p1;
    }

    /**
     * Returns the second point that creates this line
     * @return the second point that creates this line
     */
    public Point getP2(){
        return p2;
    }

    /**
     * Sets the first point of this line
     * to the argument point
     * @param p1 the point to set the first
     *           point of this line to
     */
    public void setP1(Point p1){
        this.p1 = p1;
        notifyModelListeners();
    }

    /**
     * Sets the second point of this line
     * to the argument point
     * @param p2 the point to set the second
     *           point of this line to
     */
    public void setP2(Point p2){
        this.p2 = p2;
        notifyModelListeners();
    }

    /**
     * Returns the "x-coordinate" of this line
     * @return the "X-coordinate" of this line
     */
    @Override
    public int getX() {
        return p1.x;
    }

    /**
     * Returns the "y-coordinate" of this line
     * @return the "y-coordinate" of this line
     */
    @Override
    public int getY() {
        return p1.y;
    }

    /**
     * Returns the "width" of this line
     * @return the "width" of this line
     */
    @Override
    public int getWidth() {
        return p2.x - p1.x;
    }

    /**
     * Returns the "height" of this line
     * @return the "height" of this line
     */
    @Override
    public int getHeight() {
        return p2.x - p1.x;
    }

    /**
     * Sets the "x-coordinate" of this line to be
     * the parameter
     * @param x the new "x-coordinate" of this line
     */
    @Override
    public void setX(int x) {
        int oldX = p1.x;
        p1.x += x - oldX;
        p2.x += x - oldX;
        notifyModelListeners();
    }

    /**
     * Sets the "y-coordinate" of this line to be
     * the parameter
     * @param y the new "y-coordinate" of this line
     */
    @Override
    public void setY(int y) {
        int oldY = p1.y;
        p1.y += y - oldY;
        p2.y += y - oldY;
        notifyModelListeners();
    }

    /**
     * Sets the "width" of this line to be
     * the parameter
     * @param width the new "width" of this line
     */
    @Override
    public void setWidth(int width) {
        int oldWidth = getWidth();
        if(p1.x < p2.x) {
            p2.x += width - oldWidth;
        }
        else {
            p1.x += width - oldWidth;
        }
        notifyModelListeners();
    }

    /**
     * Sets the "height" of this line to be
     * the parameter
     * @param height the new "height" of this line
     */
    @Override
    public void setHeight(int height) {
        int oldHeight = getHeight();
        if(p1.y < p2.y) {
            p2.y += height - oldHeight;
        }
        else {
            p1.y += height - oldHeight;
        }
        notifyModelListeners();
    }

    /**
     * Makes this model copy the attributes
     * of the parameter model (assumed to be a DLineModel)
     * @param other the model to copy the parameters of
     */
    @Override
    public void mimic(DShapeModel other) {
        super.mimic(other);
        setP1(((DLineModel)other).getP1());
        setP2(((DLineModel)other).getP2());
    }

    /**
     * Returns a string representation of the line model
     * @return a string representation of the line model
     */
    @Override
    public String toString() {
        return super.toString() + "; p1 = " + p1 + "; p2 = " + p2;
    }
}
