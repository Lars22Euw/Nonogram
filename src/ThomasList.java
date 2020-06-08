import java.util.ArrayList;

public class ThomasList<E> extends ArrayList<E> {
    public ThomasList() {
        super();
    }

    public E first() {
        return this.get(0);
    }

    public E last() {
        return this.get(this.size() - 1);
    }
}
