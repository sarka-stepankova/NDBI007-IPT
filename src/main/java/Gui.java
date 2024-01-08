import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Graphical User Interface for step-by-step Cormack Hashing method.
 */
public class Gui extends JFrame {
    private CormackHashing cormackHashing;
    private JTextArea textArea;
    private JTextField valueField;
    private JButton insertButton;

    private JPanel rightPanel;
    private CardLayout cardLayout;
    private int stepNumber = 0;
    private JTextArea stepsTextArea;
    private boolean directoryIsFree;

    /**
     * Constructs the GUI and initializes it with CormackHashing.
     */
    public Gui() {
        int directorySize = askForDirectorySize();

        cormackHashing = new CormackHashing(directorySize);

        setTitle("Cormack Hashing");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private int askForDirectorySize() {
        while (true) {
            String userInput = JOptionPane.showInputDialog("Set directory size:", 7);
            try {
                int directorySize = Integer.parseInt(userInput);
                if (directorySize > 0) {
                    return directorySize;
                }
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid positive number");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid positive number");
            }
        }
    }

    /**
     * Initializes the UI components.
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        valueField = new JTextField(7);
        insertButton = new JButton("Insert Value");
        JButton infoButton = new JButton("Info");

        ActionListener insertAction = e -> handleInsert();

        insertButton.addActionListener(insertAction);

        valueField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleInsert();
                }
            }
        });

        infoButton.addActionListener(e -> showInfoWindow());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        updateTextArea();

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Insert value from 0 to 100000: "));
        inputPanel.add(valueField);
        inputPanel.add(insertButton);
        inputPanel.add(infoButton);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        rightPanel = new JPanel();
        cardLayout = new CardLayout();
        rightPanel.setLayout(cardLayout);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    /**
     * Updates the main text area with the current state of cormackHashing.
     */
    private void updateTextArea() {
        textArea.setText(cormackHashing.getCurrentState());
    }

    /**
     * Handles the insert process, firstly validates input and then shows the panel with steps.
     */
    private void handleInsert() {
        if (catchBadInserts()) {
            directoryIsFree = cormackHashing.directoryIsFreeForValue(getEnteredValue());
            if (insertValue()) {
                initializeRightPanel();
                createStepsPanelAndShowFirstStep();
                disableInputAndInsertButton();
            }
        }
    }

    /**
     * Retrieves the int value entered in the input text field.
     *
     * @return Entered integer value.
     */
    private int getEnteredValue() {
        return Integer.parseInt(valueField.getText());
    }

    /**
     * Initializes the right panel, for new steps.
     */
    private void initializeRightPanel() {
        rightPanel.removeAll();
        cardLayout.show(rightPanel, "default");
        revalidate();
        repaint();
    }

    /**
     * Creates a panel for displaying steps and shows the first step.
     */
    private void createStepsPanelAndShowFirstStep() {
        int value = Integer.parseInt(valueField.getText());
        JPanel stepsPanel = new JPanel(new BorderLayout());

        JLabel step0Label = new JLabel("Inserting " + value);
        step0Label.setPreferredSize(new Dimension(200, step0Label.getPreferredSize().height));
        stepsPanel.add(step0Label, BorderLayout.NORTH);

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> showNextStep(stepsPanel, value, nextButton));
        stepsPanel.add(nextButton, BorderLayout.SOUTH);

        int padding = 10;
        stepsPanel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        rightPanel.add(stepsPanel, "insertSteps");
        cardLayout.show(rightPanel, "insertSteps");
        revalidate();
        repaint();
    }

    /**
     * Displays the next step in the sequence.
     *
     * @param stepsPanel  Panel for steps.
     * @param valueForInserting Value being inserted.
     * @param nextButton  Button to proceed to the next step.
     */
    private void showNextStep(JPanel stepsPanel, int valueForInserting, JButton nextButton) {
        stepNumber += 1;

        if (stepNumber == 1) {
            stepsTextArea = new JTextArea("k = " + valueForInserting + ", s = 7\n" +
                    "h(k,s) = k mod s = " + cormackHashing.primaryHashFunction(valueForInserting) + "\n");
            stepsTextArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(stepsTextArea);
            stepsPanel.add(scrollPane);

            rightPanel.add(stepsPanel, "insertSteps");
            cardLayout.show(rightPanel, "insertSteps");
            revalidate();
            repaint();
            return;
        }

        if (stepNumber == 2 && directoryIsFree) {
            stepsTextArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            stepsTextArea.append("\nPosition " + cormackHashing.primaryHashFunction(valueForInserting)
                    + " is free in the Directory. We can add the value " + valueForInserting + ".");
            nextButton.setText("Add");
            return;
        }

        if (stepNumber == 3 && directoryIsFree) {
            updateTextArea();
            nextButton.setEnabled(false);
            enableInputAndInsertButton();
            stepNumber = 0;
            return;
        }

        if (stepNumber == 2) {
            stepsTextArea.append("\nPosition " + cormackHashing.primaryHashFunction(valueForInserting)
                    + " is NOT free in the\nDirectory. We have to\nreorder the colliding values.");
        }

        if (stepNumber == 3) {
            int position = cormackHashing.primaryHashFunction(valueForInserting);
            List<Integer> values = cormackHashing.getValuesForReinsertingWithInsertedValue(position);
            int[] IRTuple = cormackHashing.getIAndRFromNthDirectoryRecord(position);

            stepsTextArea.append("\n\n");
            for (Integer value : values) {
                if (value == -1) {
                    continue;
                }
                stepsTextArea.append("(" + value + " >> " + IRTuple[0] + ") mod " + IRTuple[1] + " = " +
                        cormackHashing.secondaryHashFunction(value, IRTuple[0], IRTuple[1]) + "\n");
            }

            stepsTextArea.append("\ni = " + IRTuple[0] + ", r = " + IRTuple[1]);
        }

        if (stepNumber == 4) {
            stepsTextArea.append("\n\nAfter reordering colliding\nvalues," +
                    " with the new value,\nwe can reinsert them.\n");
            nextButton.setText("Reinsert");
            return;
        }

        if (stepNumber == 5) {
            updateTextArea();
            nextButton.setEnabled(false);
            enableInputAndInsertButton();
            stepNumber = 0;
        }
    }

    /**
     * Disables input and insert button (brings attention to steps panel).
     */
    private void disableInputAndInsertButton() {
        valueField.setText("");
        valueField.setEditable(false);
        valueField.setFocusable(false);

        insertButton.setEnabled(false);
    }

    /**
     * Enables input and insert button, after successful value step-by-step insert.
     */
    private void enableInputAndInsertButton() {
        valueField.setText("");
        valueField.setEditable(true);
        valueField.setFocusable(true);

        insertButton.setEnabled(true);
    }

    /**
     * Attempts to insert the value into CormackHashing, handles duplicate exception.
     *
     * @return True if the insert was successful, false otherwise.
     */
    private boolean insertValue() {
        try {
            int value = Integer.parseInt(valueField.getText());
            cormackHashing.insert(value);
            return true;
        } catch (DuplicateValueException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            valueField.setText("");
            return false;
        }
    }

    /**
     * Validates the entered value for insertion, displays error message if needed.
     *
     * @return True if the value for inserting is valid, false otherwise.
     */
    private boolean catchBadInserts() {
        try {
            int value = Integer.parseInt(valueField.getText());
            if (value < 0 || value > 100000) {
                JOptionPane.showMessageDialog(null, "Please enter a value between 0 and 100000.");
                valueField.setText("");
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid integer value.");
            valueField.setText("");
            return false;
        }
    }

    /**
     * Displays information about Cormack static hashing method in a new window.
     */
    private void showInfoWindow() {
        JFrame infoFrame = new JFrame("Cormack Hashing Info");
        JTextArea infoTextArea = new JTextArea();
        infoTextArea.setEditable(false);

        infoTextArea.append("Cormack Hashing\n");
        infoTextArea.append("- perfect static hashing method\n\n");
        infoTextArea.append("- directory has size s\n");
        infoTextArea.append("- p = pointer to the primary file\n");
        infoTextArea.append("- i = index of perfect hash function to be used\n");
        infoTextArea.append("- r = number of colliding records in the primary file\n\n");
        infoTextArea.append("- we set position in directory with primary hash function, then\nwe order the colliding subset with secondary hash function\n");
        infoTextArea.append("- primary (initial) hash function h(k,s) = k mod s\n");
        infoTextArea.append("- secondary hash function h_i(k,r) = (k >> i) mod r\n");
        infoTextArea.append("    - if we get collisions we increment i by 1, if we keep getting\ncollisions for every i, we increment r by 1 (and\ncount again from i = 0)\n");

        JScrollPane infoScrollPane = new JScrollPane(infoTextArea);

        infoFrame.add(infoScrollPane);
        infoFrame.setSize(400, 300);
        infoFrame.setLocationRelativeTo(this);
        infoFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Gui::new);
    }
}
