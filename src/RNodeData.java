import java.io.Serializable;
import java.util.ArrayList;

public class RNodeData implements Serializable {
    public int size;
    public char type;
    public Long id;
    public Long parent_id;
    public Integer index_in_parent;
    public ArrayList<Long> children_ids;
    public ArrayList<Rectangle> children_rectangles;
}