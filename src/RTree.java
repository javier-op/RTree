import java.io.*;
import java.util.ArrayList;

public class RTree {
    private RNodeData node;
    private long root_id;
    private long next_id;
    private String dir;
    private int max_size;
    private ArrayList<Rectangle> search_result;
    // private ISplitter splitter;

    public RTree(String dir_name, int max_size) {
        this.dir = dir_name;
        root_id = 0L;
        next_id = 1L;
        this.max_size = max_size;
        //this.splitter = splitter;
        node = new RNodeData();
        node.id = root_id;
        node.type = 'l';
        node.size = 0;
        node.children_rectangles = new ArrayList<>();
        node.children_ids = new ArrayList<>();
        String fileName = "n" + node.id + ".bin";
        File dir = new File(dir_name);
        if(!dir.exists()) {
            dir.mkdir();
        }
        try {
            FileOutputStream fos = new FileOutputStream(dir + "/" + fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(node);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(double x0, double x1, double y0, double y1) {
        Rectangle value = new Rectangle(x0, x1, y0, y1);
        insert(value, root_id);
    }

    private void insert(Rectangle value, long node_id) {
        loadNode(node_id);
        if(node.type == 'l') {
            node.children_rectangles.add(value);
            node.size++;
            saveNode();
            // if(node.size > max_size) {
            //    splitter.split(node_id);
            //}
        } else if (node.type == 'n') {
            int best_node_index = 0;
            double best_node_area = Double.MAX_VALUE;
            Rectangle r;
            double area;
            for(int i = 0; i < node.children_rectangles.size(); i++) {
                r = node.children_rectangles.get(i);
                area = areaDifference(extend(r, value), r);
                if(intersect(value, r) &&  area < best_node_area) {
                    best_node_index = i;
                    best_node_area = area;
                }
            }
            insert(value, node.children_ids.get(best_node_index));
        }
    }

    public ArrayList<Rectangle> search(double x0, double x1, double y0, double y1) {
        Rectangle value = new Rectangle(x0, x1, y0, y1);
        search_result = new ArrayList<Rectangle>();
        search(value, root_id);
        return search_result;
    }

    private void search(Rectangle value, long node_id) {
        loadNode(node_id);
        ArrayList<Long> valid_nodes = new ArrayList<Long>();
        if(node.type == 'l') {
            for(int i = 0; i < node.children_rectangles.size(); i++) {
                if(intersect(value, node.children_rectangles.get(i))) {
                    search_result.add(node.children_rectangles.get(i));
                }
            }
        } else if (node.type == 'n') {
            for(int i = 0; i < node.children_rectangles.size(); i++) {
                if(intersect(value, node.children_rectangles.get(i))) {
                    valid_nodes.add(node.children_ids.get(i));
                }
            }
            for(long id : valid_nodes) {
                search(value, id);
            }
        }
    }

    private void loadNode(long node_id) {
        String fileName = "n" + node_id + ".bin";
        try {
            FileInputStream fis = new FileInputStream(dir + "/" + fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            node = (RNodeData) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveNode() {
        String fileName = "n" + node.id + ".bin";
        try {
            FileOutputStream fos = new FileOutputStream(dir + "/" + fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(node);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean intersect(Rectangle r0, Rectangle r1) {
        return r0.x0 <= r1.x1 && r0.x1 >= r1.x0 &&
                r0.y1 >= r1.y0 && r0.y0 <= r1.y1;
    }

    private double area(Rectangle r) {
        return (r.x1 - r.x0) * (r.y1 - r.y0);
    }

    private double areaDifference(Rectangle r0, Rectangle r1) {
        return Math.abs(
                (r0.x1 - r0.x0) * (r0.y1 - r0.y0) -
                (r1.x1 - r1.x0) * (r1.y1 - r1.y0)
        );
    }

    private Rectangle extend(Rectangle r0, Rectangle r1) {
        return new Rectangle(
                Math.min(r0.x0, r1.x0), Math.max(r0.x1, r1.x1),
                Math.min(r0.y0, r1.y0), Math.max(r0.y1, r1.y1)
        );
    }
}
