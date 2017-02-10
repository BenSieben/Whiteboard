import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Whiteboard class is the JFrame
 * that contains the entire GUI of the
 * program. It also contains the main
 * method for running the program
 */
public class Whiteboard extends JFrame {

    // Regular whiteboard fields
    private Canvas canvas; // the canvas where shapes get drawn
    private JTextField drawTextField; // the text field where drawn text can be modified (in text drawn)
    private JComboBox fontComboBox; // the combo box where drawn text can be modified (in font)
    private JTable shapeTable; // table that shows the qualities of all the models used for the shapes currently on the canvas
    private ShapeTableModel shapeTableModel; // abstract table model for the shape table JTable

    // Special whiteboard networking fields
    private static final int NOT_NETWORKING = 0;
    private static final int SERVER_MODE = 1;
    private static final int CLIENT_MODE = 2;
    private int networkingStatus; // contains which of the three networking statuses above the whiteboard is currently in

    private ServerAccepter serverAccepter; // server accepter takes in clients as they want to join
    private ClientHandler clientHandler; // client handler helps new clients connect to the server
    private List<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>(); // list of object streams to send data to from the server
    private int idCounter; // keeps track of next id to give to next added shape

    /**
     * Constructs a new Whiteboard frame for
     * the program
     */
    public Whiteboard() {
        networkingStatus = NOT_NETWORKING;
        idCounter = 0;

        canvas = new Canvas();

        setTitle("Whiteboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // mouse listener on canvas to see what the user has clicked on
        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            @Override
            public void mouseClicked(MouseEvent e) {

            }
            @Override
            public void mouseReleased(MouseEvent e) {
                canvas.stopDragging();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                canvas.stopDragging();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                if(canvas.getDragMode() == Canvas.NOT_DRAGGING) { //this code runs if we are not in the process of a drag already
                    boolean foundShape = false; //goes true if knob is found to be pressed / shape is found to be pressed
                    int anchorPoint = -1; // for knob selection, saves opposite knob was pressed (if any)

                    //Check if any knob was pressed for resizing
                    DShape selected = canvas.getSelectedShape();
                    if(selected != null) {
                        List<Point> knobs = selected.getKnobs();
                        for(int i = 0; i < knobs.size(); i++) {
                            Point currentKnob = knobs.get(i);
                            // Check if mouse click was in bounds of the current knob
                            if(mouseX >= (currentKnob.x) - (Canvas.KNOB_SIZE / 2) &&
                                    mouseX <= (currentKnob.x) - (Canvas.KNOB_SIZE / 2) + Canvas.KNOB_SIZE &&
                                    mouseY >= (currentKnob.y) - (Canvas.KNOB_SIZE / 2) &&
                                    mouseY <= (currentKnob.y) - (Canvas.KNOB_SIZE / 2) + Canvas.KNOB_SIZE) {

                                // get appropriate anchor point depending on which knob was selected
                                switch (i){
                                    case 0 :
                                        anchorPoint = knobs.size() - 1; //3 for 4 knobs, 1 for 2 knobs
                                        break;
                                    case 1 :
                                        anchorPoint = knobs.size() - 2; // 2 for 4 knobs, 0 for 2 knobs
                                        break;
                                    case 2 :
                                        anchorPoint = 1; // 1 for 4 knobs
                                        break;
                                    case 3 :
                                        anchorPoint = 0; // 0 for 4 knobs
                                        break;
                                }
                                foundShape = true;
                                canvas.setDragMode(Canvas.DRAG_RESIZING);
                            }
                        }
                    }

                    //If no knobs pressed, check if shape was pressed for moving
                    List<DShape> shapeList = canvas.getShapeList();
                    if(!foundShape) {
                        for(int i = shapeList.size() - 1; i >= 0; i--) { // go in reverse to check shapes in the front first
                            DShape shape = shapeList.get(i);
                            if(shape.getBounds().contains(mouseX, mouseY)) { // this is true if the mouse click was inside the given shape
                                foundShape = true;
                                selected = shape;
                                canvas.setSelectedShape(selected);
                                canvas.setDragMode(Canvas.DRAG_MOVING);
                                if(selected instanceof DText) { // set JTextField / JComboBox to be selectable
                                    drawTextField.setEnabled(true);
                                    fontComboBox.setEnabled(true);
                                    drawTextField.setText(((DTextModel)selected.getModel()).getText());
                                }
                                else { // set JTextField / JComboBox to not be selectable
                                    drawTextField.setText("Edit drawn text here!");
                                    drawTextField.setEnabled(false);
                                    fontComboBox.setEnabled(false);
                                }
                                break;
                            }
                        }
                    }

                    if(!foundShape) { //deselect if no shape was clicked on
                        canvas.resetSelectedShape();
                        // since we deselected, the drawTextField / fontComboBox should be disabled
                        drawTextField.setText("Edit drawn text here!");
                        drawTextField.setEnabled(false);
                        fontComboBox.setEnabled(false);
                    }
                    else {
                        // Set up an anchor if we have pressed a knob
                        if(selected != null && canvas.getDragMode() == Canvas.DRAG_RESIZING) {
                            // Anchor is the "opposite corner" of the selected knob
                            List<Point> knobs = selected.getKnobs();
                            canvas.setAnchor(knobs.get(anchorPoint).x, knobs.get(anchorPoint).y);
                        }

                        // Set up xOffset / yOffset if we have pressed a shape
                        else if(selected != null && canvas.getDragMode() == Canvas.DRAG_MOVING) {
                            canvas.setOffset(selected.getModel().getX() - mouseX, selected.getModel().getY() - mouseY);
                        }
                    }

                    // If no knob / shape was dragged, then set dragMode to DRAGGING_NOTHING
                    if(canvas.getDragMode() == Canvas.NOT_DRAGGING) {
                        canvas.setDragMode(Canvas.DRAGGING_NOTHING);
                    }
                }

                canvas.repaint();
            }
        });

        // mouse motion listener to listen for motion
        canvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(networkingStatus != CLIENT_MODE) { // drag only works if we are not a client
                    int mouseX = e.getX();
                    int mouseY = e.getY();

                    DShape selected = canvas.getSelectedShape();
                    if(canvas.getDragMode() == Canvas.DRAG_MOVING && selected != null) { //need to move the shape
                        selected.getModel().setX(mouseX + canvas.getXOffset());
                        selected.getModel().setY(mouseY + canvas.getYOffset());
                        canvas.repaint();

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            DShapeModel model = selected.getModel();
                            messageClients(instruction, model);
                        }
                    }
                    else if(canvas.getDragMode() == Canvas.DRAG_RESIZING && selected != null) { //need to resize the shape
                        // must compute the new x, y, width, and height of the shape based on the anchor and current mouse position
                        if(selected instanceof DLine) { // must do different "resizing" for lines
                            if(canvas.getXAnchor() == ((DLineModel)selected.getModel()).getP1().x &&
                                    canvas.getYAnchor() == ((DLineModel)selected.getModel()).getP1().y) { /// anchor is p1 (move p2)
                                ((DLineModel)selected.getModel()).setP2(new Point(mouseX, mouseY));
                            }
                            else { // anchor is p2 (move p1)
                                ((DLineModel)selected.getModel()).setP1(new Point(mouseX, mouseY));
                            }
                        }
                        else { // do regular "resizing" for any other shape
                            DShapeModel selectedModel = selected.getModel();
                            selectedModel.setX(Math.min(mouseX, canvas.getXAnchor()));
                            selectedModel.setY(Math.min(mouseY, canvas.getYAnchor()));
                            selectedModel.setWidth(Math.abs(mouseX - canvas.getXAnchor()));
                            selectedModel.setHeight(Math.abs(mouseY - canvas.getYAnchor()));
                        }
                        canvas.repaint();

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            DShapeModel model = selected.getModel();
                            messageClients(instruction, model);
                        }
                    }

                    //System.err.println("drag, server mode = " + networkingStatus);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new GridLayout(0, 1));

        //Set up first row of tool panel
        JPanel firstRow = new JPanel();
        firstRow.setLayout(new GridLayout(1, 0));
        JLabel addShapeLabel = new JLabel("   Add Shape   ");
        final JButton addRectButton = new JButton("Rect");
        addRectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    // add a new DRect to the GUI
                    addShapeModel(new DRectModel());

                    // update clients
                    if(networkingStatus == SERVER_MODE) {
                        String instruction = "add";
                        DShapeModel model = canvas.getSelectedShape().getModel();
                        messageClients(instruction, model);
                    }
                }
            }
        });
        final JButton addOvalButton = new JButton("Oval");
        addOvalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    // add a new DOval to the GUI
                    addShapeModel(new DOvalModel());

                    // update clients
                    if(networkingStatus == SERVER_MODE) {
                        String instruction = "add";
                        DShapeModel model = canvas.getSelectedShape().getModel();
                        messageClients(instruction, model);
                    }
                }
            }
        });
        final JButton addLineButton = new JButton("Line");
        addLineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    // add a new DLine to the GUI
                    addShapeModel(new DLineModel());

                    // update clients
                    if(networkingStatus == SERVER_MODE) {
                        String instruction = "add";
                        DShapeModel model = canvas.getSelectedShape().getModel();
                        messageClients(instruction, model);
                    }
                }
            }
        });
        final JButton addTextButton = new JButton("Text");
        addTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    // add a new DText to the GUI
                    DTextModel m = new DTextModel();
                    addShapeModel(m);
                    drawTextField.setText(m.getText()); // also add the text of the DText to the text field

                    // update clients
                    if(networkingStatus == SERVER_MODE) {
                        String instruction = "add";
                        DShapeModel model = canvas.getSelectedShape().getModel();
                        messageClients(instruction, model);
                    }
                }
            }
        });

        firstRow.add(addShapeLabel);
        firstRow.add(addRectButton);
        firstRow.add(addOvalButton);
        firstRow.add(addLineButton);
        firstRow.add(addTextButton);

        toolPanel.add(firstRow);

        //Set up second row of tool panel
        final JButton setColorButton = new JButton("Set Color");
        setColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    if(canvas.getSelectedShape() != null) { // only show dialog if something is selected
                        Color oldColor = canvas.getSelectedShape().getModel().getColor();
                        Color selectedColor = JColorChooser.showDialog(null, "Choose Selected Shape Color", oldColor); // open a dialog to get a color selection from the user
                        if(selectedColor != null) {
                            canvas.getSelectedShape().getModel().setColor(selectedColor);
                            canvas.repaint();

                            // update clients
                            if(networkingStatus == SERVER_MODE) {
                                String instruction = "change";
                                DShapeModel model = canvas.getSelectedShape().getModel();
                                messageClients(instruction, model);
                            }
                        }
                    }
                }
            }
        });

        toolPanel.add(setColorButton);

        //Set up third row of tool panel
        JPanel thirdRow = new JPanel();
        thirdRow.setLayout(new GridLayout(1, 0));
        drawTextField = new JTextField("Edit drawn text here!", 10);
        drawTextField.setMaximumSize(new Dimension(10, 200));
        drawTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { // user adds a character
                if(networkingStatus != CLIENT_MODE) { // text field only works as a non-client
                    // if current selection is text, then we need to update text
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape instanceof DText) {
                        DTextModel model = (DTextModel)selectedShape.getModel();
                        model.setText(drawTextField.getText());

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            messageClients(instruction, model);
                        }
                    }
                    canvas.repaint();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) { // user deletes a character
                if(networkingStatus != CLIENT_MODE) { // text field only works as a non-client
                    // if current selection is text, then we need to update text
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape instanceof DText) {
                        DTextModel model = (DTextModel)selectedShape.getModel();
                        model.setText(drawTextField.getText());

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            messageClients(instruction, model);
                        }
                    }
                    canvas.repaint();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        drawTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { // mouse clicked on the text field
                if(networkingStatus != CLIENT_MODE) { // text field only works as a non-client
                    // if current selection is text, then we need to update text field to match text drawing
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape instanceof DText) {
                        DTextModel model = (DTextModel)selectedShape.getModel();
                        drawTextField.setText(model.getText());

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            messageClients(instruction, model);
                        }
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) { // mouse clicked somewhere else on the frame
            }
        });

        GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment(); // used to get all font names for the comboBox of all fonts
        String[] allFonts = localGraphicsEnvironment.getAvailableFontFamilyNames();
        fontComboBox = new JComboBox(allFonts);
        fontComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // combo box only works as a non-client
                    // if current selection is text, then we need to update text field font
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape instanceof DText) {
                        DTextModel model = (DTextModel)selectedShape.getModel();
                        model.setTextFont(new Font((String)fontComboBox.getSelectedItem(), Font.PLAIN, 1));

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "change";
                            messageClients(instruction, model);
                        }
                    }
                    canvas.repaint();
                }
            }
        });

        // The text field and combo box start off as disabled
        drawTextField.setEnabled(false);
        fontComboBox.setEnabled(false);

        thirdRow.add(drawTextField);
        thirdRow.add(fontComboBox);

        toolPanel.add(thirdRow);

        //Set up fourth row of tool panel
        JPanel fourthRow = new JPanel();
        fourthRow.setLayout(new GridLayout(1, 0));
        final JButton moveToFrontButton = new JButton("Move to Front");
        moveToFrontButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape != null) {

                        DShapeModel moveModel = selectedShape.getModel();
                        moveShapeModelToFront(selectedShape.getModel()); // move currently selected shape

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "front";
                            messageClients(instruction, moveModel);
                        }
                    }
                }
            }
        });
        final JButton moveToBackButton = new JButton("Move to Back");
        moveToBackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    DShape selectedShape = canvas.getSelectedShape();
                    if(selectedShape != null) {

                        DShapeModel moveModel = selectedShape.getModel(); // extract current model
                        moveShapeModelToBack(moveModel); // move currently selected shape

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "back";
                            messageClients(instruction, moveModel);
                        }
                    }
                }
            }
        });
        final JButton removeShapeButton = new JButton("Remove Shape");
        removeShapeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    DShape removeShape = canvas.getSelectedShape();
                    if(removeShape != null) {

                        removeShapeModel(removeShape.getModel()); // remove the currently selected shape

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            String instruction = "remove";
                            DShapeModel model = removeShape.getModel();
                            messageClients(instruction, model);
                        }
                    }
                }
            }
        });

        fourthRow.add(moveToFrontButton);
        fourthRow.add(moveToBackButton);
        fourthRow.add(removeShapeButton);

        toolPanel.add(fourthRow);

        // Set up fifth row of tool panel
        JPanel fifthRow = new JPanel();
        fifthRow.setLayout(new GridLayout(1, 0));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    String fileName = JOptionPane.showInputDialog("File Name", null);
                    if(fileName != null) {
                        File file = new File(fileName);
                        saveFile(file);
                    }
                }
            }
        });
        JButton openButton = new JButton("Open");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    String fileName = JOptionPane.showInputDialog("File Name", null);
                    if(fileName != null) {
                        File file = new File(fileName);
                        openFile(file);

                        // update clients
                        if(networkingStatus == SERVER_MODE) {
                            // set all the clients to the loaded whiteboard
                            for(ObjectOutputStream output : outputs) {
                                clientSetup(output);
                            }
                        }
                    }
                }
            }
        });
        JButton exportButton = new JButton("Export Image");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(networkingStatus != CLIENT_MODE) { // button only works as a non-client
                    String fileName = JOptionPane.showInputDialog("File Name", null);
                    if(fileName != null) {
                        File file = new File(fileName);
                        exportImage(file);
                    }
                }
            }
        });

        fifthRow.add(saveButton);
        fifthRow.add(openButton);
        fifthRow.add(exportButton);

        toolPanel.add(fifthRow);

        // Set up sixth row of tool panel
        JPanel sixthRow = new JPanel();
        sixthRow.setLayout(new GridLayout(1, 0));
        final JLabel networkingStatusLabel = new JLabel("        Not Networking");
        final JButton serverStartButton = new JButton("Server Start");
        final JButton clientStartButton = new JButton("Client Start");
        serverStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // disable networking buttons
                serverStartButton.setEnabled(false);
                clientStartButton.setEnabled(false);
                networkingStatusLabel.setText("        Server Mode ON");

                // start server mode
                startServerMode();
            }
        });
        clientStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // disable networking buttons
                serverStartButton.setEnabled(false);
                clientStartButton.setEnabled(false);
                networkingStatusLabel.setText("        Client Mode ON");

                // start client mode
                startClientMode();
            }
        });

        sixthRow.add(networkingStatusLabel);
        sixthRow.add(serverStartButton);
        sixthRow.add(clientStartButton);

        toolPanel.add(sixthRow);

        //Set up table of tool panel
        shapeTableModel = new ShapeTableModel();
        shapeTable = new JTable(shapeTableModel);
        JScrollPane tableScrollPane = new JScrollPane(shapeTable);
        shapeTable.setFillsViewportHeight(true);
        tableScrollPane.setPreferredSize(new Dimension(400, 200));
        shapeTable.setEnabled(false); // do not let users manually modify table contents
        shapeTable.getTableHeader().setReorderingAllowed(false); // do not let users move columns around

        JPanel outerToolPanel = new JPanel();
        outerToolPanel.setLayout(new GridLayout(0, 1));

        outerToolPanel.add(toolPanel);
        outerToolPanel.add(tableScrollPane);

        //Add completed tool panel to WEST of Whiteboard
        add(outerToolPanel, BorderLayout.WEST);

        //Add canvas to CENTER of the Whiteboard
        add(canvas, BorderLayout.CENTER);

        setPreferredSize(new Dimension(800, 400));
        setMinimumSize(new Dimension(800, 400));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Adds a new shape model to the canvas / table
     * in the whiteboard
     * @param model the model to add to the whiteboard
     */
    private void addShapeModel(DShapeModel model) {

        if(networkingStatus != CLIENT_MODE) {
            // only use default values / id if we are the server / operating locally
            // the clients' calls to add should actually have corrected models already
            // give the model the default parameters
            model.setX(10);
            model.setY(10);
            model.setWidth(20);
            model.setHeight(20);
            model.setId(idCounter);
            idCounter++;
        }

        // add the model to the canvas and table
        canvas.addShape(model);
        shapeTableModel.addShape(canvas.getSelectedShape()); // this works because canvas automatically selects newly created shapes

        if(canvas.getSelectedShape() instanceof DText) { // set JTextField / JComboBox to be selectable since we made a DText object
            drawTextField.setEnabled(true);
            fontComboBox.setEnabled(true);
            drawTextField.setText(((DTextModel)canvas.getSelectedShape().getModel()).getText());
        }
        else { // set JTextField / JComboBox to not be selectable since we did not make a DText object
            drawTextField.setText("Edit drawn text here!");
            drawTextField.setEnabled(false);
            fontComboBox.setEnabled(false);
        }
    }

    /**
     * Removes the argument shape model from the whiteboard
     * @param removeShapeModel the shape to remove from the whiteboard
     */
    private void removeShapeModel(DShapeModel removeShapeModel) {
        List<DShape> canvasShapes = canvas.getShapeList();
        for(int i = 0; i < canvasShapes.size(); i++) {
            if(canvasShapes.get(i).getModel().getId() == removeShapeModel.getId()) { // remove match found
                DShape removeShape = canvasShapes.remove(i);
                removeShape.getModel().removeModelListener(removeShape);
                shapeTableModel.removeShape(removeShape);
                canvas.resetSelectedShape();
                canvas.repaint();

                // since we deleted, the drawTextField / fontComboBox should be disabled
                drawTextField.setText("Edit drawn text here!");
                drawTextField.setEnabled(false);
                fontComboBox.setEnabled(false);
                break;
            }
        }
    }

    /**
     * Moves the parameter model to the front of the canvas
     * @param model the model of the shape to move to the front
     */
    private void moveShapeModelToFront(DShapeModel model) {
        List<DShape> canvasShapes = canvas.getShapeList();
        for(int i = 0; i < canvasShapes.size(); i++) {
            if(canvasShapes.get(i).getModel().getId() == model.getId()) { // found matching shape
                DShape selectedShape = canvasShapes.remove(i);
                canvasShapes.add(selectedShape);
                shapeTableModel.moveShapeToFront(selectedShape);
                canvas.repaint();
                break;
            }
        }
    }

    /**
     * Moves the paramter model to the back of the canvas
     * @param model the model of the shape to move to the back
     */
    private void moveShapeModelToBack(DShapeModel model) {
        List<DShape> canvasShapes = canvas.getShapeList();
        for(int i = 0; i < canvasShapes.size(); i++) {
            if(canvasShapes.get(i).getModel().getId() == model.getId()) { // found matching model
                DShape selectedShape = canvasShapes.remove(i);
                canvasShapes.add(0, selectedShape);
                shapeTableModel.moveShapeToBack(selectedShape);
                canvas.repaint();
                break;
            }
        }
    }

    /**
     * Makes the model in this whiteboard match the
     * updated model (by comparing id numbers)
     * @param updatedModel the new version of the model to use
     */
    private void mimicModel(DShapeModel updatedModel) {
        List<DShape> canvasShapes = canvas.getShapeList();
        for(int i = 0; i < canvasShapes.size(); i++) {
            if(canvasShapes.get(i).getModel().getId() == updatedModel.getId()) { // found matching model
                canvasShapes.get(i).getModel().mimic(updatedModel);
                break;
            }
        }
        canvas.repaint();
    }

    /**
     * Clears the canvas and table of the
     * whiteboard completely
     */
    private void clearBoard() {
        canvas.clearCanvas(); // clear the canvas of everything
        shapeTableModel.clearData(); // clear the shape table model of everything
    }

    /**
     * Opens up the file, setting the whiteboard
     * to be the contents of the loaded file
     * @param file the file to load from
     */
    private void openFile(File file) {
        try {
            // check to make sure the .xml extension gets added to the file
            String fileName = file.getName().toLowerCase();
            if(fileName.lastIndexOf(".xml") != fileName.length() - 4) {
                file = new File(file.getName() + ".xml");
            }

            // create the XML decoder to read the file
            XMLDecoder fileDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));

            // read in the DModels saved in the file
            DShapeModel[] shapeModels = (DShapeModel[])(fileDecoder.readObject());

            // now we can clear the canvas / table, as the file open was successful
            clearBoard();

            // load the shapeModels array back into the canvas and table model
            // also reset the id counter
            idCounter = 0;
            for(DShapeModel model : shapeModels) {
                canvas.addShape(model);
                shapeTableModel.addShape(canvas.getSelectedShape());
                idCounter++;
            }

            fileDecoder.close();
        }
        catch(IOException ex) {
            System.err.println("Error opening file \"" + file + "\". Open operation aborted");
            // ex.printStackTrace();
        }
    }

    /**
     * Saves the current canvas contents to the file
     * @param file the file to save the canvas contents to
     */
    private void saveFile(File file) {
        try {
            // check to make sure the .xml extension gets added to the file
            String fileName = file.getName().toLowerCase();
            if(fileName.lastIndexOf(".xml") != fileName.length() - 4) {
                file = new File(file.getName() + ".xml");
            }

            // create the XML encoder to write the file
            XMLEncoder fileEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));

            // make an array of the current shapes on the canvas
            DShape[] canvasShapes = canvas.getShapeList().toArray(new DShape[0]);

            // make an array of the models of the current shapes on the canvas
            DShapeModel[] canvasModels = new DShapeModel[canvasShapes.length];
            for (int i = 0; i < canvasShapes.length; i++) {
                canvasModels[i] = canvasShapes[i].getModel();
            }

            // write the shape model array to the file
            fileEncoder.writeObject(canvasModels);

            fileEncoder.close();
        }
        catch(IOException ex) {
            System.err.println("Error saving file \"" + file + "\". Save operation aborted");
            // ex.printStackTrace();
        }
    }

    /**
     * Exports the current canvas contents
     * to a PNG file
     * @param file the name of the PNG file
     */
    private void exportImage(File file) {
        // get buffered image of current canvas appearance
        BufferedImage image = canvas.getBufferedImage();

        // try to write the file out
        try {
            // check to make sure the .png extension gets added to the file
            String fileName = file.getName().toLowerCase();
            if(fileName.lastIndexOf(".png") != fileName.length() - 4) {
                file = new File(file.getName() + ".png");
            }
            ImageIO.write(image, "png", file); // note: this method call can throw a FileNotFoundException on its own (for bad file names)
        }
        catch(IOException ex) {
            System.err.println("Error exporting image to \"" + file + "\" (IO exception). Export operation aborted");
            // ex.printStackTrace();
        }
    }

    /**
     * Starts the server mode of the whiteboard
     */
    private void startServerMode() {
        networkingStatus = SERVER_MODE;

        // Get the desired port number from the user
        String portNumber = JOptionPane.showInputDialog("Run Server on Port", "9264");
        if(portNumber != null) {
            serverAccepter = new ServerAccepter(Integer.parseInt(portNumber.trim()));
            serverAccepter.start();
        }
    }

    /**
     * Starts the client mode of the whiteboard
     */
    private void startClientMode() {
        networkingStatus = CLIENT_MODE;

        // Get the desired host:port number from the user
        String ipAddress = JOptionPane.showInputDialog("Connect to Host:Port", "127.0.0.1:9264");
        if(ipAddress != null) {
            ipAddress = ipAddress.trim();
            String name = ipAddress.substring(0, ipAddress.indexOf(":"));
            int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
            clientHandler = new ClientHandler(name, port);
            clientHandler.start();
        }
    }

    /**
     * Messages all the current clients with a message and new model
     * @param instruction the instruction sent by the server
     * @param updatedModel the shape model sent by the server
     */
    private synchronized void messageClients(String instruction, DShapeModel updatedModel) {
        // convert model into xml
        OutputStream memStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(memStream);
        encoder.writeObject(updatedModel);
        encoder.close();
        String modelMessage = memStream.toString();

        // send xml message to all clients
        for (int i = outputs.size() - 1; i >= 0; i--) {
            ObjectOutputStream output = outputs.get(i);
            try {
                output.writeObject(instruction); // send instruction to currently iterated client
                output.writeObject(modelMessage); // send model to currently iterated client
                output.flush();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                outputs.remove(i); // remove sockets from list that do not work
            }
        }
    }

    /**
     * Adds the parameter object stream to the list of streams
     * that the server has to send messages to
     * Synchronized to avoid conflicts
     * @param output the new output
     */
    private synchronized void addOutput(ObjectOutputStream output) {
        outputs.add(output);

        // update  newly added client to match server here
        clientSetup(output);
    }

    /**
     * Performs a setup for clients of
     * the server whiteboard by making the clients match the server
     * @param output the output stream of the client to setup
     */
    private synchronized void clientSetup(ObjectOutputStream output) {
        if(networkingStatus == SERVER_MODE) {

            // first tell client to reset their whiteboard
            String instruction = "clear";
            DShapeModel model = new DShapeModel();
            OutputStream memStream = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder(memStream);
            encoder.writeObject(model);
            encoder.close();
            String modelMessage = memStream.toString();


            try {
                output.writeObject(instruction); // send instruction to currently iterated client
                output.writeObject(modelMessage); // send model to currently iterated client
                output.flush();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            // now add all current shapes in the whiteboard server to the client
            instruction = "add";
            List<DShape> canvasShapes = canvas.getShapeList();
            for(DShape shape : canvasShapes) {
                try {
                    output.writeObject(instruction); // send instruction to currently iterated client
                    model = shape.getModel(); // get the model of each shape in the server's canvas

                    memStream = new ByteArrayOutputStream();
                    encoder = new XMLEncoder(memStream);
                    encoder.writeObject(model);
                    encoder.close();
                    modelMessage = memStream.toString();

                    output.writeObject(modelMessage); // send model to currently iterated client
                    output.flush();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Main method of Whiteboard program, which
     * simply creates a new Whiteboard
     * @param args (currently unused)
     */
    public static void main(String[] args) {
        new Whiteboard();
    }

    /**
     * Acts as a continuous accepter
     * of incoming clients to be added
     * to this network
     */
    private class ServerAccepter extends Thread {
        private int port; // the port number of this ServerAccepter

        /**
         * Creates a new ServerAccepter that goes to
         * the argument port
         * @param port the port to set the ServerAccepter to
         */
        ServerAccepter(int port) {
            this.port = port;
        }

        /**
         * Sets up the ServerAccepter to continuously
         * run, waiting for new client connections
         */
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while(true) { // keep waiting for clients until the program closes
                    Socket toClient = null;
                    toClient = serverSocket.accept();

                    // add output stream to the list of output streams for sending messages
                    addOutput(new ObjectOutputStream(toClient.getOutputStream()));
                }
            }
            catch(IOException ex) {
                System.err.println("Error setting up server socket with port " + port + ". ServerAccepter closed.");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Client runs this for handling incoming messages
     */
    private class ClientHandler extends Thread {
        private String name; // name of the IP address for the client
        private int port; // port number the client is connected to

        /**
         * Creates a new client handler
         * for managing the client connection
         * @param name the name of the IP address to connect to
         * @param port the port of the IP address to connect to
         */
        ClientHandler(String name, int port) {
            this.name = name;
            this.port = port;
        }

        /**
         * Sets up the ClientHandler to continuously
         * wait for messages from the server
         */
        public void run() {
            try {
                Socket toServer = new Socket(name, port); // connect to server with given name / port
                ObjectInputStream in = new ObjectInputStream(toServer.getInputStream()); // get input stream to read from server

                while(true) {
                    String instructionString = (String)in.readObject(); // get incoming instruction from server
                    String xmlModelString = (String)in.readObject(); // get incoming model from server

                    // decode the incoming model from the server
                    XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlModelString.getBytes()));
                    DShapeModel updatedModel = (DShapeModel)decoder.readObject();

                    if(instructionString.equals("add")) { // added a new shape
                        addShapeModel(updatedModel);
                    }
                    else if(instructionString.equals("remove")) { // remove a shape
                        removeShapeModel(updatedModel);
                    }
                    else if(instructionString.equals("front")) { // move model to front
                        moveShapeModelToFront(updatedModel);
                    }
                    else if(instructionString.equals("back")) { // move model to back
                        moveShapeModelToBack(updatedModel);
                    }
                    else if(instructionString.equals("change")) { // catch-all for any other change
                        mimicModel(updatedModel);
                    }
                    else if(instructionString.equals("clear")) { // empty the whiteboard
                        clearBoard();
                    }

                }
            }
            catch(Exception ex) { // this exception gets thrown if we close server while client(s) are open
                System.err.println("Error: client lost connection with server");
                //ex.printStackTrace();
            }
        }
    }
}