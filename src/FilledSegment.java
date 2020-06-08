import java.util.ArrayList;
import java.util.List;

public class FilledSegment extends Segment {
    List<Integer> possibleInfoIndices;

    public FilledSegment(int i, int endOfSegment) {
        super(i, endOfSegment);
        this.possibleInfoIndices = new ArrayList<>();
    }

    public static void matchInfos(STATE[] vector, List<Integer> info, List<FilledSegment> filledSegments) {
    }
}
