import java.io.*;
import java.util.ArrayList;

public class RTree {
    private RNodeData node;
    private long root_id;
    private long next_id;
    private String dir;
    private int max_size;
    private char mode;
    private ArrayList<Rectangle> search_result;

    public RTree(String dir_name, int size, char split_mode) {
        assert split_mode == 0 || split_mode == 1;
        dir = dir_name;
        root_id = 0L;
        next_id = 1L;
        max_size = size;
        mode = split_mode;
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

    private void insert(Rectangle value, Long node_id) {
        loadNode(node_id);
        if(node.type == 'l') {
            node.children_rectangles.add(value);
            node.size++;
            if(node.size > max_size) {
                split();
            } else {
                saveNode(node);
                updateMBR(node.parent_id, node.index_in_parent, value);
            }
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

    private void updateMBR(Long node_id, int rectangle_index, Rectangle extension) {
        if(node_id != null) {
            loadNode(node_id);
            Rectangle new_rectangle = extend(node.children_rectangles.get(rectangle_index), extension);
            node.children_rectangles.set(rectangle_index, new_rectangle);
            saveNode(node);
            updateMBR(node.parent_id, node.index_in_parent, extension);
        }
    }

    private void split() {
        RNodeData[] new_children;
        if(mode == 0) {
            new_children = linearSplit();
        } else {
            new_children = quadraticSplit();
        }
        loadNode(node.parent_id);
    }

    private RNodeData[] linearSplit() {
        RNodeData[] output = new RNodeData[]{};
        return output;
    }

    private RNodeData[] quadraticSplit() {
        RNodeData[] output = new RNodeData[]{};
        return output;
    }

    public ArrayList<Rectangle> search(double x0, double x1, double y0, double y1) {
        Rectangle value = new Rectangle(x0, x1, y0, y1);
        search_result = new ArrayList<Rectangle>();
        search(value, root_id);
        return search_result;
    }

    private void search(Rectangle value, Long node_id) {
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

    private void loadNode(Long node_id) {
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

    private void saveNode(RNodeData a_node) {
        String fileName = "n" + a_node.id + ".bin";
        try {
            FileOutputStream fos = new FileOutputStream(dir + "/" + fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(a_node);
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