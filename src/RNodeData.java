import java.io.Serializable;
import java.util.ArrayList;

public class RNodeData implements Serializable {
    public int size;
    public long id;
    public char type;
    public long parent_id;
    public ArrayList<Long> children_ids;
    public ArrayList<Rectangle> children_rectangles;
}
