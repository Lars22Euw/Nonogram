import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Nonogram {
    STATE[][] solution;
    STATE[][] guess;
    List<Integer>[] columns;
    List<Integer>[] rows;
    int pos = 0;
    boolean inColumnPass = false;
    private int size;

    public Nonogram(int size) {
        var r = new Random(1);
        this.solution = new STATE[size][size];
        this.size = size;
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                solution[i][j] = STATE.values()[r.nextInt(2) + 1];
            }
        }
        this.guess = new STATE[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                guess[i][j] = STATE.UNKNOWN;
            }
        }
        this.columns = new ArrayList[size];
        for (var i = 0; i < size; i++) {
            columns[i] = new ArrayList<>();
            int c = 0;
            for (var j = 0; j < size; j++) {
                if (solution[j][i] == STATE.EMPTY) {
                    if (c != 0) {
                        columns[i].add(c);
                    }
                    c = 0;
                } else {
                    c++;
                }
            }
            if (c != 0) {
                columns[i].add(c);
            }
        }
        this.rows = new ArrayList[size];
        for (var i = 0; i < size; i++) {
            rows[i] = new ArrayList<>();
            int c = 0;
            for (var j = 0; j < size; j++) {
                if (solution[i][j] == STATE.EMPTY) {
                    if (c != 0) {
                        rows[i].add(c);
                    }
                    c = 0;
                } else {
                    c++;
                }
            }
            if (c != 0) {
                rows[i].add(c);
            }
        }
    }


    public void step() {
        var info = inColumnPass ? columns[pos] : rows[pos];
        var vector = getVector();
        var sum  = info.stream().reduce(Integer::sum).get() + info.size() - 1;
        var stride = columns.length - sum;
        int index = 0;
        // Fill based on stride in total view
        for (var entry : info) {
            int delta = entry - stride;
            if (delta > 0) {
                setEntries(vector, entry, index, stride);
            }
            index += entry + 1;
        }
        var filledSegments = new ArrayList<FilledSegment>();
        for (int i = 0; i < size; i++) {
            int endOfSegment = i;
            while(endOfSegment != size && vector[endOfSegment] == STATE.FILLED) {
                endOfSegment++;
            }
            int len = endOfSegment - i;
            if (len > 0) {
                filledSegments.add(new FilledSegment(i, endOfSegment));
            }
            i = endOfSegment;
        }
        var borderSegments = new ArrayList<BorderSegment>();
        for (int i = 0; i < size; i++) {
            int endOfSegment = i;
            while(endOfSegment != size && vector[endOfSegment] != STATE.EMPTY) {
                endOfSegment++;
            }
            int len = endOfSegment - i;
            if (len > 0) {
                borderSegments.add(new BorderSegment(i, endOfSegment));
            }
            i = endOfSegment;
        }

        FilledSegment.matchInfos(vector, info, filledSegments);
        BorderSegment.matchInfos(vector, info, borderSegments);

        int highestContainedPrior = -1;
        int lowestContainedPost = -1;
        for (int i = 0; i < borderSegments.size(); i++) {
            if (borderSegments.get(i).containedInfoIndices.isEmpty()) continue;
            if (i + 1 == borderSegments.size()) {
                lowestContainedPost = info.size();
            } else {
                lowestContainedPost = lowestNext(borderSegments, i + 1);
            }
            if (i != 0) {
                highestContainedPrior = highestBefore(borderSegments, i - 1);
            }
            if (borderSegments.get(i).containedInfoIndices.first() > highestContainedPrior &&
                    borderSegments.get(i).containedInfoIndices.last() < lowestContainedPost) {
                index = borderSegments.get(i).start;
                sum = borderSegments.get(i).containedInfoIndices.stream()
                        .map(info::get)
                        .reduce(Integer::sum).get()
                        + borderSegments.get(i).containedInfoIndices.size() - 1;
                stride = borderSegments.get(i).end - borderSegments.get(i).start - sum;
                for (var infoIndex : borderSegments.get(i).containedInfoIndices) {
                    var entry = info.get(infoIndex);
                    int delta = entry - stride;
                    if (delta > 0) {
                        setEntries(vector, entry, index, stride);
                    }
                    index += entry + 1;
                }
            }
        }

        // TODO: not yet existing segments, enter Emptys


        if (vector[0] == STATE.FILLED) {
            setEntries(vector, info.get(0), 0, 0);
        }
        if (vector[size - 1] == STATE.FILLED) {
            int entry = info.get(info.size() - 1);
            setEntries(vector, entry, size - entry, 0);
        }
        // fill up if one of the KNOWN values is maxed
        int empty = sum(vector, false);
        int filled = sum(vector, true);
        if (empty == size - info.stream().reduce(Integer::sum).get()) {
            for (int i = 0; i < size; i++) {
                vector[i] = vector[i] == STATE.UNKNOWN ? STATE.FILLED : vector[i];
            }
        }
        if (filled == info.stream().reduce(Integer::sum).get()) {
            for (int i = 0; i < size; i++) {
                vector[i] = vector[i] == STATE.UNKNOWN ? STATE.EMPTY : vector[i];
            }
        }
        cheating(vector, info);
        // commit and post-process
        saveChanges(vector);
        if (++pos == size) {
            inColumnPass = !inColumnPass;
            pos = 0;
        }
    }

    private void cheating(STATE[] vector, List<Integer> info) {
        var unknowns = unknowns(vector);
        for (var i : unknowns) {
            if (possible(vector, i, STATE.EMPTY, info)) {
                if (!possible(vector, i, STATE.FILLED, info)) {
                    vector[i] = STATE.EMPTY;
                }
            } else {
                vector[i] = STATE.FILLED;
            }
        }
    }

    private ThomasList<Integer> unknowns(STATE[] vector) {
        var unknowns = new ThomasList<Integer>();
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] == STATE.UNKNOWN) {
                unknowns.add(i);
            }
        }
        return unknowns;
    }

    private boolean possible(STATE[] vector, Integer checked, STATE state, List<Integer> info) {
        var copy = vector.clone();
        copy[checked] = state;
        var unknowns = unknowns(copy);
        for (var i : unknowns) {
            copy[i] = STATE.EMPTY;
        }
        while(true) {
            var changeIndex = 0;
            if (test(copy, info)) {
                return true;
            }
            while(copy[unknowns.get(changeIndex)] == STATE.FILLED) {
                copy[unknowns.get(changeIndex)] = STATE.EMPTY;
                changeIndex++;
                if (changeIndex == unknowns.size()) {
                    return false;
                }
            }
            copy[unknowns.get(changeIndex)] = STATE.FILLED;
        }
    }

    private boolean test(STATE[] copy, List<Integer> info) {
        var segments = new ArrayList<Segment>();
        for (int i = 0; i < size; i++) {
            int endOfSegment = i;
            while(endOfSegment != size && copy[endOfSegment] == STATE.FILLED) {
                endOfSegment++;
            }
            int len = endOfSegment - i;
            if (len > 0) {
                segments.add(new FilledSegment(i, endOfSegment));
            }
            i = endOfSegment;
        }
        if (segments.size() != info.size()) {
            return false;
        }
        for (int i = 0; i < info.size(); i++) {
            if (info.get(i) != segments.get(i).end - segments.get(i).start) {
                return false;
            }
        }
        return true;
    }

    private int lowestNext(List<BorderSegment> borderSegments, int i) {
        while(i < borderSegments.size() && borderSegments.get(i).containedInfoIndices.isEmpty()) {
            i++;
        }
        if (i == borderSegments.size()) return Integer.MAX_VALUE;
        return borderSegments.get(i).containedInfoIndices.first();
    }

    private int highestBefore(List<BorderSegment> borderSegments, int i) {
        while(i >= 0 && borderSegments.get(i).containedInfoIndices.isEmpty()) {
            i--;
        }
        if (i < 0) return -1;
        return borderSegments.get(i).containedInfoIndices.last();
    }

    private void fillSegment(Segment segment, Integer len, STATE[] vector) {
        var stride = len - (segment.end - segment.start);
        // find border 0,size or Empty
        int start = max(0, segment.start - stride);
        for (int i = segment.start-1; i >= start; i--) {
            if (vector[i] == STATE.EMPTY) {
                start = i+1;
                break;
            }
        }

        int end = min(size, segment.end + stride);
        for (int i = segment.end; i < end; i++) {
            if (vector[i] == STATE.EMPTY) {
                end = i;
                break;
            }
        }

        int newStride = (end - start) - len;
        setEntries(vector, len, start, newStride);
    }

    private int sum(STATE[] vector, boolean filled) {
        return (int) Arrays.stream(vector).filter(e -> filled ? e == STATE.FILLED : e == STATE.EMPTY).count();
    }

    private void saveChanges(STATE[] vector) {
        for (int i = 0; i < size; i++) {
            if (inColumnPass) {
                if (guess[i][pos] != STATE.UNKNOWN) {
                    assert(guess[i][pos] == vector[i]);
                }
                guess[i][pos] = vector[i];
            } else {
                if (guess[pos][i] != STATE.UNKNOWN) {
                    assert(guess[pos][i] == vector[i]);
                }
                guess[pos][i] = vector[i];
            }
        }
    }

    private STATE[] getVector() {
        var result = new STATE[size];
        for (int i = 0; i < size; i++) {
            result[i] = inColumnPass ? guess[i][pos] : guess[pos][i];
        }
        return result;
    }

    private void setEntries(STATE[] vector, int entry, int index, int stride) {
        if (stride == 0) {
            vector[max(index - 1, 0)] = STATE.EMPTY;
            vector[min(index + entry, size - 1)] = STATE.EMPTY;
        }
        for (int i = index + stride; i < index + entry; i++) {
            vector[i]= STATE.FILLED;
        }
    }
}
