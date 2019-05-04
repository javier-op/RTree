import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class TesteoFeo2 {
    public static void main(String[] args) {
        RNodeData nodo = new RNodeData();
        nodo.size = 1;
        nodo.type = 'n';
        nodo.index_in_parent = 0;
        nodo.parent_id = 1L;
        nodo.id = 2L;
        nodo.children_rectangles = new ArrayList<>();
        nodo.children_ids = new ArrayList<>();

        for(long i = 0; i < 90; i ++) {
            nodo.children_rectangles.add(new Rectangle(i, i+1, i, i+1));
        }

        File dir = new File("test");
        if(!dir.exists()) {
            dir.mkdir();
        }
        try {
            FileOutputStream fos = new FileOutputStream("test/test_node.bin");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(nodo);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
