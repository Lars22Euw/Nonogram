import java.util.ArrayList;
import java.util.List;

public class BorderSegment extends Segment {
    ThomasList<Integer> containedInfoIndices;

    public BorderSegment(int i, int endOfSegment) {
        super(i, endOfSegment);
        this.containedInfoIndices = new ThomasList<>();
    }

    public static void matchInfos(STATE[] vector, List<Integer> info, List<BorderSegment> borderSegments) {
        for (var i = 0; i < info.size(); i++) {
            for (var j = 0; j < borderSegments.size(); j++) {
                if (BorderSegment.edgePossible(j, borderSegments, i, info)) {
                    borderSegments.get(j).containedInfoIndices.add(i);
                }
            }
        }
    }

    private static boolean edgePossible(int bsIndex, List<BorderSegment> borderSegments, int infoIndex, List<Integer> info) {
        var infoEntries = info.get(infoIndex);
        var bs = borderSegments.get(bsIndex);
        // first check if all infos left and right can fit.
        // left
        var leftBs = 0;
        var leftOccupied = 0;
        for (int i = 0; i < infoIndex; i++) {
            var len = borderSegments.get(leftBs).end - borderSegments.get(leftBs).start - leftOccupied;
            if (leftBs > bsIndex) {
                return false;
            }
            if (len >= info.get(i)) {
                leftOccupied += info.get(i) + 1;
            } else {
                leftBs++;
                i--;
                leftOccupied = 0;
            }
        }
        // right
        var rightBs = borderSegments.size() - 1;
        var rightOccupied = 0;
        for (int i = info.size() - 1; i > infoIndex; i--) {
            var len = borderSegments.get(rightBs).end - borderSegments.get(rightBs).start - rightOccupied;
            if (rightBs < bsIndex) {
                return false;
            }
            if (len >= info.get(i)) {
                rightOccupied += info.get(i) + 1;
            } else {
                rightBs--;
                i++;
                rightOccupied = 0;
            }
        }
        var occupied = (rightBs == bsIndex ? rightOccupied : 0) + (leftBs == bsIndex ? leftOccupied : 0);
        return bs.end - bs.start - occupied >= infoEntries;
    }



}
