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

        var segments = new ArrayList<Segment>();
        for (int i = 0; i < size; i++) {
            int endOfSegment = i;
            while(endOfSegment != size && vector[endOfSegment] == STATE.FILLED) {
                endOfSegment++;
            }
            int len = endOfSegment - i;
            if (len > 0) {
                segments.add(new Segment(i, endOfSegment));
            }
            i = endOfSegment;
        }
        // TODO: enter segments not in zipped fashion
        if (segments.size() == info.size()) {
            // can start from left
            for (int i = 0; i < info.size(); i++) {
                fillSegment(segments.get(i), info.get(i), vector);
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
        // commit and post-process
        saveChanges(vector);
        if (++pos == size) {
            inColumnPass = !inColumnPass;
            pos = 0;
        }
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
