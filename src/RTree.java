import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class RTree {
    private RNodeData node;
    private long root_id;
    private long next_id;
    private String dir;
    private int max_size;
    private char mode;
    private ArrayList<Rectangle> search_result;

    /**
     * Constructor de RTree.
     * @param dir_name Nombre del directorio que contiene los nodos.
     * @param size Tamaño maximo de cada nodo.
     * @param split_mode 0 indica linearSplit, 1 indica quadraticSplit.
     */
    public RTree(String dir_name, int size, char split_mode) {
        assert split_mode == 'l' || split_mode == 'q';
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

    /**
     * Crea el objeto rectángulo y llama su inserción en el nodo raiz.
     * @param x0 Cooredenada menor en el eje x del rectangulo.
     * @param x1 Cooredenada mayor en el eje x del rectangulo.
     * @param y0 Cooredenada menor en el eje y del rectangulo.
     * @param y1 Cooredenada mayor en el eje y del rectangulo.
     */
    public void insert(double x0, double x1, double y0, double y1) {
        Rectangle value = new Rectangle(x0, x1, y0, y1);
        insert(value, root_id);
    }

    /**
     * Trata de insertar el rectángulo en el subarbol identificado por node_id,
     * si el nodo es una hoja lo inserta en este nodo, sino encuentra el mejor
     * nodo en el que se deba insertar.
     * @param value Rectángulo a insertar.
     * @param node_id Identificador del subarbol en el que se inserta.
     */
    private void insert(Rectangle value, Long node_id) {
        loadNode(node_id);
        if(node.type == 'l') {
            node.children_rectangles.add(value);
            node.size++;
            if(node.size > max_size) {
                split();
            } else {
                saveNode(node);
                if(node.parent_id != null)
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

    /**
     * Actualiza el MBR del nodo en el padre hasta llegar a la raiz.
     * @param node_id Identificador del nodo.
     * @param rectangle_index Indice del hijo en este nodo.
     * @param extension Rectángulo que se insertó.
     */
    private void updateMBR(Long node_id, int rectangle_index, Rectangle extension) {
        loadNode(node_id);
        Rectangle new_rectangle = extend(node.children_rectangles.get(rectangle_index), extension);
        node.children_rectangles.set(rectangle_index, new_rectangle);
        saveNode(node);
        if(node.parent_id != null)
            updateMBR(node.parent_id, node.index_in_parent, extension);
    }

    /**
     * Realiza la operación split en el nodo cargado en node, si mode es 0 llama a
     * linearSplit, si mode es 1 llama a quadraticSplit.
     */
    private void split() {
        RNodeData[] new_children; // new_children es un arreglo con los 2 nodos nuevos

        if(mode == 'l') { // se elige la heuristica para hacer el split
            new_children = linearSplit();
        } else {
            new_children = quadraticSplit();
        }

        if(node.parent_id == null) { // si el nodo es la raiz, creo una nueva raiz
            node.size = 1;
            node.type = 'n';
            node.id = next_id++;
            node.index_in_parent = null;
            node.children_rectangles = new ArrayList<Rectangle>();
            node.children_rectangles.add(new Rectangle(0, 0, 0, 0));
            node.children_ids = new ArrayList<Long>();
            node.children_ids.add(0L);
            new_children[0].index_in_parent = 0;
            root_id = node.id;
        } else { // sino cargo al padre
            loadNode(node.parent_id);
        }

        // introduzco el primero de los nodos nuevos en el lugar donde estaba el original en el padre
        new_children[0].parent_id = node.id;
        node.children_rectangles.set(new_children[0].index_in_parent, getMBR(new_children[0]));
        node.children_ids.set(new_children[0].index_in_parent, new_children[0].id);
        saveNode(new_children[0]);


        // introduzco el segundo de los nodos nuevos en la ultima posicion en el padre
        new_children[1].parent_id = node.id;
        node.children_rectangles.add(getMBR(new_children[1]));
        node.children_ids.add(new_children[1].id);
        new_children[1].index_in_parent = node.size;
        saveNode(new_children[1]);

        // aumento el tamaño del nodo padre
        node.size++;
        saveNode(node);

        if(new_children[0].type == 'n') {
            updateChildren(new_children[0].children_ids, new_children[0].id);
        }
        if(new_children[1].type == 'n') {
            updateChildren(new_children[1].children_ids, new_children[1].id);
        }
        new_children[0] = null;
        new_children[1] = null;

        // si el nodo padre se pasa del tamaño maximo, llamo a split de nuevo
        if(node.size > max_size) {
            split();
        }
    }

    /**
     * Aplica linear split en el nodo cargado en node.
     * @return Retorna los dos nodos resultantes al realizar linear split.
     */
    private RNodeData[] linearSplit() {
        double min_x1 = Double.MAX_VALUE;
        double max_x0 = Double.MIN_VALUE;
        double min_y1 = Double.MAX_VALUE;
        double max_y0 = Double.MIN_VALUE;
        double min_x = Double.MAX_VALUE;
        double max_x = Double.MIN_VALUE;
        double min_y = Double.MAX_VALUE;
        double max_y = Double.MIN_VALUE;
        int index_x_low = 0; // index_x_low es el rectangulo mas a la izquierda
        int index_x_high = 0; // index_x_high es el rectangulo mas a la derecha
        int index_y_low= 0; // index_y_low es el rectangulo mas abajo
        int index_y_high = 0; // index_y_high es el rectangulo mas arriba
        Rectangle r;
        for(int i = 0; i < node.size; i++) {
            r = node.children_rectangles.get(i);
            if(r.x1 < min_x1) {
                min_x1 = r.x1;
                index_x_low = i;
            }
            if(r.x0 > max_x0) {
                max_x0 = r.x0;
                index_x_high = i;
            }
            if(r.y1 < min_y1) {
                min_y1 = r.y1;
                index_y_low = i;
            }
            if(r.y0 > max_y0) {
                max_y0 = r.y0;
                index_y_high = i;
            }
            min_x = Math.min(min_x, Math.min(r.x0, r.x1));
            max_x = Math.max(max_x, Math.max(r.x0, r.x1));
            min_y = Math.min(min_y, Math.min(r.y0, r.y1));
            max_y = Math.max(max_y, Math.max(r.y0, r.y1));
        }
        // elijo los indices de la dimension en que las distancias normalizadas sean mas grandes
        if((max_x0 - min_x1)/(max_x - min_x) > (max_y0 - min_y1)/(max_y - min_y)) {
            return generateNewNodes(index_x_low, index_x_high);
        }
        return generateNewNodes(index_y_low, index_y_high);
    }

    // TODO elegir los nodos con la heuristica de quadratic split
    /**
     * Aplica quadratic split en el nodo cargado en node.
     * @return Retorna los dos nodos resultantes al realizar quadratic split.
     */


    private RNodeData[] quadraticSplit() {
        int[] par = {0, 0};
        Rectangle r1;
        Rectangle r2;
        Rectangle MBR;
        Double area_mas_basura = 0.0;
        Double area_basura = 0.0;
        for (int i = 0; i < node.size; i++) {
            for (int j = 0; j < node.size; j++) {
                if (i != j){
                    r1 = node.children_rectangles.get(i);
                    r2 = node.children_rectangles.get(j);
                    MBR = extend(r1, r2);
                    if (intersect(r1, r2))
                        area_basura = MBR.getArea() - (r1.getArea() + r2.getArea()) + areaInter(r1,r2);
                    else
                        area_basura = MBR.getArea() - (r1.getArea() + r2.getArea());
                    if (area_basura > area_mas_basura) {
                        area_mas_basura = area_basura;
                        par[0] = i;
                        par[1] = j;
                    }
                }
            }
        }
        return generateNewNodes(par[0], par[1]);
    }

    // TODO crear los nuevos nodos usando los nodos en index0 e index1 de node como base
    // TODO recordar diferecniar en el caso nodo y hoja

    /**
     * Utiliza los rectángulos guardados en node en el indice index0 y index1 como base
     * para generar dos nuevos nodos.
     * @param index0 Primer índice.
     * @param index1 Segundo índice.
     * @return Dos nodos nuevos.
     */
    private RNodeData[] generateNewNodes(int index0, int index1) {
        RNodeData node0 = new RNodeData();
        node0.parent_id = node.parent_id; // se va a insertar en el mismo padre
        node0.id = node.id; // para este reutilizamos el archivo
        node0.size = 1;
        node0.children_rectangles = new ArrayList<Rectangle>();
        node0.children_ids = new ArrayList<Long>();
        node0.index_in_parent = node.index_in_parent; // reutilizamos la posición tambien
        node0.children_rectangles.add(node.children_rectangles.get(index0));
        RNodeData node1 = new RNodeData();
        node1.parent_id = node.parent_id; // se va a insertar en el mismo padre
        node1.id = next_id++; // este va a ser un archivo nuevo
        node1.size = 1;
        node1.children_rectangles = new ArrayList<Rectangle>();
        node1.children_ids = new ArrayList<Long>();
        node1.children_rectangles.add(node.children_rectangles.get(index1));
        // en node1 la posición en el padre todavia no esta definida, se hace despues en split
        Rectangle mbr0 = node.children_rectangles.get(index0);
        Rectangle mbr1 = node.children_rectangles.get(index1);
        Rectangle current;
        if(node.type == 'l') {
            node0.type = 'l';
            node1.type = 'l';
            for(int i = 0; i < node.size; i++) {
                if(i == index0 || i == index1) continue;
                current = node.children_rectangles.get(i);
                if(areaDifference(extend(mbr1, current), current) > areaDifference(extend(mbr0, current), current)){
                    node0.children_rectangles.add(current);
                    node0.size++;
                    mbr0 = extend(mbr0, current);
                } else {
                    node1.children_rectangles.add(current);
                    node1.size++;
                    mbr1 = extend(mbr1, current);
                }
            }
        } else if(node.type == 'n') {
            node0.type = 'n';
            node0.children_ids.add(node.children_ids.get(index0));
            node1.type = 'n';
            node1.children_ids.add(node.children_ids.get(index1));
            long current_id;
            for(int i = 0; i < node.size; i++) {
                if(i == index0 || i == index1) continue;
                current = node.children_rectangles.get(i);
                current_id = node.children_ids.get(i);
                if(areaDifference(extend(mbr1, current), current) > areaDifference(extend(mbr0, current), current)){
                    node0.children_rectangles.add(current);
                    node0.children_ids.add(current_id);
                    node0.size++;
                    mbr0 = extend(mbr0, current);
                } else {
                    node1.children_rectangles.add(current);
                    node1.children_ids.add(current_id);
                    node1.size++;
                    mbr1 = extend(mbr1, current);
                }
            }
        }
        return new RNodeData[]{node0, node1};
    }

    /**
     * Actualiza el parent_id de los nodos identificados en el arreglo children por parent_id.
     * @param children Lista con los nodos a los que se debe a actualizar el parent_id
     * @param new_parent_id El nuevo parent_id
     */
    private void updateChildren(ArrayList<Long> children, long new_parent_id) {
        for(int i = 0; i < children.size(); i++) {
            loadNode(children.get(i));
            node.parent_id = new_parent_id;
            node.index_in_parent = i;
            saveNode(node);
        }
    }

    /**
     * Calcula el MBR de un nodo a_node.
     * @param a_node Un nodo.
     * @return Rectángulo que corresonde al MBR del nodo.
     */
    private Rectangle getMBR(RNodeData a_node) {
        Rectangle output = a_node.children_rectangles.get(0);
        for(int i = 1; i < a_node.size; i++) {
            output = extend(output, a_node.children_rectangles.get(i));
        }
        return output;
    }

    /**
     * Crea el objeto rectángulo y llama su busqueda en el nodo raiz.
     * @param x0 Cooredenada menor en el eje x del rectangulo.
     * @param x1 Cooredenada mayor en el eje x del rectangulo.
     * @param y0 Cooredenada menor en el eje y del rectangulo.
     * @param y1 Cooredenada mayor en el eje y del rectangulo.
     */
    public ArrayList<Rectangle> search(double x0, double x1, double y0, double y1) {
        Rectangle value = new Rectangle(x0, x1, y0, y1);
        search_result = new ArrayList<Rectangle>();
        search(value, root_id);
        return search_result;
    }

    /**
     * Busca todos los nodos de las hojas del subarbol identificado por node_id que intersecten
     * a value.
     * @param value Rectángulo del cual se buscan sus intersecciones.
     * @param node_id Identificador del subarbol en el que se busca las intersecciones.
     */
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

    /**
     * Intenta cargar un nodo en node desde un archivo.
     * @param node_id Identificador del nodo.
     */
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

    /**
     * Intenta guardar el nodo a_node en un archivo.
     * @param a_node El nodo a guardar.
     */
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

    /**
     * Verifica si dos rectángulos se intersectan.
     * @param r0 Primer rectángulo.
     * @param r1 Segundo rectángulo.
     * @return Retorna true si intersectan, false si no.
     */
    private boolean intersect(Rectangle r0, Rectangle r1) {
        return r0.x0 <= r1.x1 && r0.x1 >= r1.x0 &&
                r0.y1 >= r1.y0 && r0.y0 <= r1.y1;
    }

    /**
     * Calcula la diferencia en el área de dos rectángulos.
     * @param r0 Primer rectángulo.
     * @param r1 Segundo rectángulo.
     * @return Retorna el valor de la diferencia.
     */
    private double areaDifference(Rectangle r0, Rectangle r1) {
        return Math.abs(
                (r0.x1 - r0.x0) * (r0.y1 - r0.y0) -
                        (r1.x1 - r1.x0) * (r1.y1 - r1.y0)
        );
    }

    /**
     * Calcula el área intersectada por dos rectángulos.
     * @param r0 Primer rectángulo.
     * @param r1 Segundo rectángulo.
     * @return Retorna el área de la interseccion.
     */
    private double areaInter(Rectangle r0, Rectangle r1) {
        Double[] arrx = {r0.x0, r0.x1, r1.x0, r1.x1};
        Arrays.sort(arrx);
        Double[] arry = {r0.y0, r0.y1, r1.y0, r1.y1};
        Arrays.sort(arry);
        return (arrx[2]-arrx[1])*(arry[2]-arry[1]);
    }

    /**
     * Calcula el Minimum Bounding Rectangle que contiene a dos rectángulos.
     * @param r0 Primer rectángulo.
     * @param r1 Segundo Rectángulo.
     * @return Un nuevo rectángulo que corresponde al MBR de r0 y r1.
     */
    private Rectangle extend(Rectangle r0, Rectangle r1) {
        return new Rectangle(
                Math.min(r0.x0, r1.x0), Math.max(r0.x1, r1.x1),
                Math.min(r0.y0, r1.y0), Math.max(r0.y1, r1.y1)
        );
    }
}