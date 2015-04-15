/**
 * Stores the result of the comparison operation between two audio files
 */
public class ResultSet {
    public boolean result;
    public int lhsOffset;
    public int rhsOffset;

    public ResultSet(boolean result, int lhsOffset, int rhsOffset) {
        this.result = result;
        this.lhsOffset = lhsOffset;
        this.rhsOffset = rhsOffset;
    }
}
