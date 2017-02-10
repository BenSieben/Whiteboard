import java.awt.*;

/**
 * DShape which draws text
 */
public class DText extends DShape{

    /**
     * Creates a new DText shape
     */
    public DText() {
        super(new DTextModel());
    }

    /**
     * Draws the DText based on the current status of
     * the connected DTextModel
     * @param g the Graphics object needed to draw
     */
    @Override
    public void draw(Graphics g) {
        DTextModel model = (DTextModel)getModel();
        g.setColor(model.getColor());
        g.setFont(computeFont(g));
        Shape clip = g.getClip(); // save current clip
        g.setClip(clip.getBounds().createIntersection(getBounds())); //edit current clip to not go out of bounds

        // put bottom-left of text at (x, y + 3 / 4 * height) to not clip characters that go below the line (like "y" or "g")
        g.drawString(model.getText(), model.getX(), model.getY() + model.getHeight() * 3 / 4);

        g.setClip(clip); // restore the clip back to normal
    }

    /**
     * Computes the appropriate Font to use
     * when drawing the DText based on the size
     * of the current bounding rectangle
     * @param g the Graphics object needed to draw
     * @return the Font best suited for the current DText
     */
    private Font computeFont(Graphics g) {
        // initialize the font to be size 1 of same style as current model font
        double size = 1.0; // changes the size of the newFont by incrementing up until the text is out of bounds
        DTextModel model = (DTextModel)getModel();
        Font newFont = new Font(model.getTextFont().getFontName(), Font.PLAIN, (int)(size)); // holds current newFont value being tested
        FontMetrics newFontMetrics = g.getFontMetrics(newFont); // contains metrics for the new font to test the bounds of the font of different font sizes

        // loop through different sizes of fonts to see the largest font size usable based on the bounding rectangle of the model
        while(newFontMetrics.getHeight() <= model.getHeight()) { // "&& newFontMetrics.stringWidth(model.getText()) <= model.getWidth()" taken out since Font was said to only be a function of height in the program specifications
            size = (size * 1.10) + 1; // increase size of test font by about 10%

            // update newFont / newFontMetrics for this new font size
            newFont = new Font(model.getTextFont().getFontName(), Font.PLAIN, (int)(size));
            newFontMetrics = g.getFontMetrics(newFont);
        }

        // set size to one step back so it stays in bounds
        size = (size - 1) / 1.10;

        newFont = new Font(model.getTextFont().getFontName(), Font.PLAIN, (int)(size)); // set newFont to previous size

        return newFont;
    }
}
