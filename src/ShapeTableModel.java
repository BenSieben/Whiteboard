import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * An AbstractTable model used in the
 * whiteboard program to list out all
 * the qualities of all the shapes
 * currently on the canvas
 */
public class ShapeTableModel extends AbstractTableModel implements ModelListener{

    private String[] columnNames = {"X", "Y", "Width", "Height"}; // names of the columns in the table
    private List<DShape> data; // list of all the DShapes currently on the canvas

    /**
     * Creates a new shape table model
     */
    public ShapeTableModel() {
        super();
        data = new ArrayList<DShape>();
    }

    /**
     * Returns the name of the specified column of the table
     * @param col the column number to get the name of
     * @return the name of the specified column of the table
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Listener method that gets called when any DShapeModel that this
     * table model is listening to gets changed (and the table needs to be updated)
     * @param model the DShapeModel that triggered this listener
     */
    @Override
    public void modelChanged(DShapeModel model) {
        int row;
        for(row = 0; row < data.size(); row++) {
            if(data.get(row).getModel().equals(model)) { // found the shape that uses the argument model, which we must update
                break;
            }
        }

        // update the contents of the row with the updated shape model
        fireTableRowsUpdated(row, row);
    }

    /**
     * Returns the current number of data rows in the model
     * @return the current number of data rows in the model
     */
    @Override
    public int getRowCount() {
        return data.size();
    }

    /**
     * Gets the current number of columns in the model
     * @return the current number of columns in the model
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Method for rendering cells of the model in a JTable
     * @param rowIndex the row index to get from the model for displaying
     * @param columnIndex the col index to get from the model fro displaying
     * @return null if nothing is found, or else the Object at the specified row and column
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DShape shape = data.get(rowIndex);
        switch(columnIndex) { // determine which data to return based on the passed column index
            case 0:
                return shape.getModel().getX();
            case 1:
                return shape.getModel().getY();
            case 2:
                return shape.getModel().getWidth();
            case 3:
                return shape.getModel().getHeight();
            default:
                return null;
        }
    }

    /**
     * Gets the Class of the specified column in the table model
     * @param c the column number to check
     * @return the Class of the specified column number
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Sets the value of the specified row and col to the value
     * @param value the new value to place at the specified location
     * @param row the row of the location to update
     * @param col the col of the location to update
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        DShape shape = data.get(row);
        switch(col) { // determine which data to modify based on the passed column index
            case 0:
                shape.getModel().setX((Integer)value);
                break;
            case 1:
                shape.getModel().setY((Integer)value);
                break;
            case 2:
                shape.getModel().setWidth((Integer)value);
                break;
            case 3:
                shape.getModel().setHeight((Integer)value);
                break;
            default:
                // do nothing
        }
        fireTableRowsUpdated(row, row);
    }

    /**
     * Adds a new DShape to the table
     * @param shape the shape to add to the table
     */
    public void addShape(DShape shape) {
        shape.getModel().addModelListener(this);
        data.add(shape);
        fireTableDataChanged();
    }

    /**
     * Moves the specified DShape to the front (i.e., to
     * the end of the data list)
     * @param shape the DShape to move to the front
     */
    public void moveShapeToFront(DShape shape) {
        DShape matchShape = null;
        for (int i = 0; i < data.size(); i++) { // look for the shape to move to the front (i.e., at the end of data list)
            if(shape == data.get(i)) {
                matchShape = data.remove(i);
                data.add(matchShape); // add match shape to the end of data
                break;
            }
        }
        if(matchShape != null) {
            fireTableDataChanged();
        }
    }

    /**
     * Moves the specified DShape to the back (i.e., to
     * the first index of the data list)
     * @param shape
     */
    public void moveShapeToBack(DShape shape) {
        DShape matchShape = null;
        for (int i = 0; i < data.size(); i++) { // look for the shape to move to the back (i.e., at the beginning of data list)
            if(shape == data.get(i)) {
                matchShape = data.remove(i);
                data.add(0, matchShape); // add matchShape to the front of data
                break;
            }
        }
        if(matchShape != null) {
            fireTableDataChanged();
        }
    }

    /**
     * Removes the specified DShape from the table
     * @param shape the shape to remove from the table
     */
    public void removeShape(DShape shape) {
        for(int i = 0; i < data.size(); i++) { // loop through the data and remove first occurrence of argument found
            if(shape == data.get(i)) {
                data.get(i).getModel().removeModelListener(this);
                data.remove(i);
                break;
            }
        }
        fireTableDataChanged();
    }

    /**
     * Clears the data inside the table
     */
    public void clearData() {
        data = new ArrayList<DShape>();
    }
}
