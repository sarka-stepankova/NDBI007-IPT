import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CormackHashingTest {

    private CormackHashing cormackHashing;

    @BeforeEach
    public void setUp() {
        cormackHashing = new CormackHashing();
    }

    @Test
    public void testDirectoryIsFreeForValue() {
        // When the directory position is free
        int freePositionValue = 14;
        assertTrue(cormackHashing.directoryIsFreeForValue(freePositionValue));

        // When the directory position is not free
        int nonFreePositionValue = 42;
        // Make the position in the directory non-free
        try {
            cormackHashing.insert(nonFreePositionValue);
        } catch (DuplicateValueException e) {
            fail("Unexpected DuplicateValueException during insertion.");
        }
        assertFalse(cormackHashing.directoryIsFreeForValue(nonFreePositionValue));
    }
}
