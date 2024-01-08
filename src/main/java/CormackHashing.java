import java.util.*;

/**
 * Cormack Hashing class implementing a perfect static hashing method.
 */
public class CormackHashing {

    /**
     * Inner class representing a record in the Directory
     */
    private static class DirectoryRecord {
        /** position in the directory computed by primary hash function */
        int position;
        /** index of locally perfect hashing function to be used. */
        int i;
        /** number of collisions in primary file */
        int r;
        /** pointer to start of the primary file */
        int p;

        DirectoryRecord(int position, int i, int r, int p) {
            this.position = position;
            this.i = i;
            this.r = r;
            this.p = p;
        }
    }

    /**
     * Inner class representing a record in the primary file.
     */
    private static class PrimaryRecord {
        int key;
        int value;

        PrimaryRecord(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private List<DirectoryRecord> directory;
    private List<PrimaryRecord> primaryFile;

    public CormackHashing() {
        this(7);
    }

    /**
     * Adds together initialization of the directory and primary file
     */
    public CormackHashing(int directorySize) {
        initializeDirectory(directorySize);
        initializePrimaryFile();
    }

    /**
     * Initializes the directory with empty directory records.
     */
    private void initializeDirectory(int directorySize) {
        directory = new ArrayList<>(directorySize);
        for (int i = 0; i < directorySize; i++) {
            directory.add(new DirectoryRecord(i,0, 0, 0));
        }
    }

    /**
     * Initializes empty primary file.
     */
    private void initializePrimaryFile() {
        primaryFile = new ArrayList<>();
    }

    /**
     * Computes the result of the primary hash function for a given key.
     *
     * @param key Value for hashing.
     * @return Result of the primary hash function.
     */
    public int primaryHashFunction(int key) {
        return key % directory.size();
    }

    /**
     * Computes the result of the secondary hash function for a given key, i, and r.
     *
     * @param key Key for hashing.
     * @param i Index of the locally perfect hashing function.
     * @param r Number of collisions in the primary file.
     * @return Result of the secondary hash function.
     * @throws ArithmeticException If r is zero.
     */
    public int secondaryHashFunction(int key, int i, int r) {
        if (r == 0) {
            throw new ArithmeticException("Division by zero, if this happens sth is calling secondaryHashFunction from wrong place.");
        }
        return (key >> i) % r;
    }

    /**
     * Checks if a directory record is not yet occupied.
     *
     * @param dr Directory record to check.
     * @return True if the record is free, false otherwise.
     */
    private boolean directoryRecordIsFree(DirectoryRecord dr) {
        return (dr.p == 0 && dr.r == 0 && dr.i == 0);
    }

    /**
     * Reorders a list of colliding values using the secondary hash function.
     *
     * @param values List of values to reorder.
     * @param r Number of collisions in the primary file.
     * @param directoryRecord Directory record with information about the position.
     * @return Reordered list of values.
     */
    private List<Integer> reorderList(List<Integer> values, int r, DirectoryRecord directoryRecord) {
        int i;
        r = directoryRecord.r + 1;  // repair for: r is needed to be incremented every time we add a key
        while (true) {
            i = hashValuesAreNotCollidingForR(r, values);
            if (i > -1) {
                break;
            }

            r++;
        }

        int length = r;
        List<Integer> reorderedList = new ArrayList<>(Collections.nCopies(length, -1));
        for (Integer value : values) {
            if (value == -1) {
                continue;
            }
            int position = secondaryHashFunction(value, i, r);
            reorderedList.set(position, value);
        }

        directoryRecord.i = i;
        directoryRecord.r = r;
        return reorderedList;
    }

    public int hashValuesAreNotCollidingForR(int r, List<Integer> values) {
        int i = 0;

        while (true) {
            List<Integer> secondaryHashFuncOnValues = new ArrayList<>();
            for (int value : values) {
                // repair for: r is needed to be incremented every time we add a key, it caused problems here
                // if there would be more than one white space, it would always collide
                if (value == -1) {
                    continue;
                }

                secondaryHashFuncOnValues.add(secondaryHashFunction(value, i, r));
            }

            if (isAllZeroes(secondaryHashFuncOnValues)) {
                return -1;
            }

            if (areNotCollidingValues(secondaryHashFuncOnValues)) {
                return i;
            }

            i++;
        }
    }

    public boolean isAllZeroes(List<Integer> list) {
        for (int item : list) {
            if (item != 0) {
                return false;
            }
        }

        return true;
    }

    public boolean areNotCollidingValues(List<Integer> list) {
        Set<Integer> setFromList = new HashSet<>(list);
        return setFromList.size() == list.size();
    }

    /**
     * Inserts a value into CormackHashing, throws duplicate exception.
     *
     * @param value Value to insert.
     * @throws DuplicateValueException If the value already exists in the primary file.
     */
    public void insert(int value) throws DuplicateValueException {
        int positionInDirectory = primaryHashFunction(value);

        // is this position free?
        DirectoryRecord valueFromDirectory = directory.get(positionInDirectory);
        boolean isFree = directoryRecordIsFree(valueFromDirectory);

        // do not insert values that has been inserted
        for (int i = 0; i < valueFromDirectory.r; i++) {
            int collidingValue = primaryFile.get(valueFromDirectory.p + i).value;

            if (value == collidingValue) {
                throw new DuplicateValueException("Value " + value + " already exists in the hash table.");
            }
        }

        // If the position is free, just insert
        if (isFree) {
            valueFromDirectory.r = 1;
            valueFromDirectory.p = primaryFile.size();
            primaryFile.add(new PrimaryRecord(valueFromDirectory.p, value));
            return;
        }

        // Position is not free, need to reorder values that are here + the new value
        // Firstly creating list of values to reorder
        List<Integer> oneClass = new ArrayList<>();
        for (int i = valueFromDirectory.p; i < valueFromDirectory.p + valueFromDirectory.r; i++) {
            oneClass.add(primaryFile.get(i).value);
            // set old values to -1
            primaryFile.set(i, new PrimaryRecord(i, -1));
        }
        oneClass.add(value);

        // If at the end of primary file, remove values for reinserting with new value
        if (valueFromDirectory.p + valueFromDirectory.r >= primaryFile.size()) {
            for (int i = 0; i < valueFromDirectory.r; i++) {
                primaryFile.remove(primaryFile.size() - 1);
            }
        }

        List<Integer> reorderedList = reorderList(oneClass, valueFromDirectory.r, valueFromDirectory);

        valueFromDirectory.p = primaryFile.size();

        for (int i = 0; i < reorderedList.size(); i++) {
            primaryFile.add(new PrimaryRecord(valueFromDirectory.p+i, reorderedList.get(i)));
        }

    }

    /**
     * Prints the current directory and primary file to the console.
     */
    public void printStructure() {
        System.out.println("\nDIRECTORY =========");
        System.out.println("Position i r p");
        for (DirectoryRecord directoryRecord: directory) {
            System.out.printf("    %d    %d %d %d%n", directoryRecord.position, directoryRecord.i, directoryRecord.r, directoryRecord.p);
        }

        System.out.println("\nPRIMARY FILE =========");
        System.out.println("Key value");
        for (PrimaryRecord primaryRecord: primaryFile) {
            System.out.printf(" %d  %d%n", primaryRecord.key, primaryRecord.value);
        }
    }

    /**
     * Retrieves the current state of CormackHashing as a formatted string.
     *
     * @return Current state of CormackHashing.
     */
    public String getCurrentState() {
        StringBuilder state = new StringBuilder();
        state.append("\nDIRECTORY =========\n");
        state.append("Position i r p\n");
        for (DirectoryRecord directoryRecord : directory) {
            state.append(String.format("     %d     %d %d %d%n", directoryRecord.position, directoryRecord.i, directoryRecord.r, directoryRecord.p));
        }

        state.append("\nPRIMARY FILE =========\n");
        state.append("Key value\n");
        for (PrimaryRecord primaryRecord : primaryFile) {
            state.append(String.format(" %d  %d%n", primaryRecord.key, primaryRecord.value));
        }

        return state.toString();
    }

    /**
     * Checks if the directory position is free for inserting a value.
     *
     * @param value Value to check.
     * @return True if the directory position is free, false otherwise.
     */
    public boolean directoryIsFreeForValue(int value) {
        int positionInDirectory = primaryHashFunction(value);
        DirectoryRecord valueFromDirectory = directory.get(positionInDirectory);
        return directoryRecordIsFree(valueFromDirectory);
    }

    /**
     * Retrieves the values that need to be reinserted when inserting a new value.
     *
     * @param value Value for which reinsertion of values is required.
     * @return List of values for reinsertion.
     */
    public List<Integer> getValuesForReinsertingWithInsertedValue(int value) {
        int positionInDirectory = primaryHashFunction(value);
        DirectoryRecord valueFromDirectory = directory.get(positionInDirectory);

        List<Integer> values = new ArrayList<>();
        for (int i = valueFromDirectory.p; i < valueFromDirectory.p + valueFromDirectory.r; i++) {
            values.add(primaryFile.get(i).value);
        }

        return values;
    }

    /**
     * Retrieves the index `i` and number of collisions `r` from the nth record in the directory.
     *
     * @param n Index of the directory record.
     * @return Array containing i and r.
     */
    public int[] getIAndRFromNthDirectoryRecord(int n) {
        DirectoryRecord valueFromDirectory = directory.get(n);
        return new int[]{valueFromDirectory.i, valueFromDirectory.r};
    }
}
