import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Gui gui = new Gui();

        // printTestExampleInTerminal();
    }

    static void printTestExampleInTerminal() {
        CormackHashing cormack = new CormackHashing();

        int[] values = {14, 17, 10, 21, 28, 42};
        //int[] values = {421, 356, 169, 457, 748, 956, 187, 982, 307, 652, 306, 689, 537, 541, 697};

        for (int value : values) {
            // Comment following line for not step by step output
            cormack.printStructure();
            try {
                cormack.insert(value);
            } catch (DuplicateValueException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }

        cormack.printStructure();
    }
}