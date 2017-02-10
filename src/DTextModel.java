import java.awt.*;

/**
 * The model for drawing text
 */
public class DTextModel extends DShapeModel{

    private String text; // holds text for the DText object connected with this model
    private Font textFont; // holds the font for the text

    /**
     * Creates a new DTextModel
     */
    public DTextModel() {
        super();
        text = "Hello";
        textFont = new Font("Dialog", Font.PLAIN, 1);
    }

    /**
     * Returns the current text of the model
     * @return the current text of the model
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the current font of the model
     * @return the current font of the model
     */
    public Font getTextFont() {
        return textFont;
    }

    /**
     * Sets the current text of the model to be the parameter
     * @param text the new text to give to this model
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the current font of the model to be the parameter
     * @param font the new font to give to this model
     */
    public void setTextFont(Font font) {
        textFont = font;
    }

    /**
     * Makes this model copy the attributes
     * of the parameter model (assumed to be a DTextModel)
     * @param other the model to copy the parameters of
     */
    @Override
    public void mimic(DShapeModel other) {
        super.mimic(other);
        setText(((DTextModel)other).getText());
        setTextFont(((DTextModel)other).getTextFont());
    }

    /**
     * Returns a string representation of the line model
     * @return a string representation of the line model
     */
    @Override
    public String toString() {
        return super.toString() + "; text = " + text + "; font = " + textFont;
    }
}
