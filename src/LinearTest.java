import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.io.FileUtils;

public class LinearTest {

    public static Rectangle rectangleGenerator(){
        double leftLimit = 0D;
        double rightLimit = 499900D;
        double x0 = leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
        double y0 = leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
        leftLimit = 1D;
        rightLimit = 100D;
        double x1 = x0 + leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
        double y1 = y0 + leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
        return new Rectangle(x0, x1, y0, y1);
    }

    private static ArrayList<Rectangle> genN(long n) {
        ArrayList<Rectangle> output = new ArrayList<Rectangle>();
        for(long i = 0; i < n; i++) {
            output.add(rectangleGenerator());
        }
        return output;
    }

    private static void createFile(String name, ArrayList<Long> arreglo) {
        StringBuilder output = new StringBuilder();
        for(long l: arreglo) {
            output.append(l);
            output.append("\n");
        }
        try (FileWriter writer = new FileWriter(name); BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(output.toString());
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    private static void testN(int n, int start_n) {
        ArrayList<Rectangle> insert_data = genN(n);
        ArrayList<Rectangle> search_data = genN(n / 10);
        RTree linear = new RTree("nodos_linear",90,60,'l');
        RTree quad = new RTree("nodos_quad",90,60,'q');

        ArrayList<Long> total_sizes = new ArrayList<>();
        ArrayList<Long> numbers_of_files = new ArrayList<>();
        ArrayList<Long> array = new ArrayList<>();
        long start;
        long total_size;
        long number_of_files;
        int counter = 0;

        Rectangle r;
        File dir = new File("nodos_linear");
        File[] directoryListing;
        int next = start_n;
        start = System.currentTimeMillis();
        for(int i = 0; i < n; i++) {
            if(i % (n / 10) == 0) {
                System.out.println(counter + "% of data inserted in linear.");
                counter += 10;
            }
            if(i == next - 1){
                if(i == 511)
                    array.add(System.currentTimeMillis() - start);
                else
                    array.add(System.currentTimeMillis() - start + array.get(array.size()-1));
                directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    total_size = 0;
                    number_of_files = 0;
                    for (File child : directoryListing) {
                        total_size += child.length();
                        number_of_files++;
                    }
                    total_sizes.add(total_size);
                    numbers_of_files.add(number_of_files);
                }
                next *= 2;
                start = System.currentTimeMillis();
            }
            r = insert_data.get(i);
            linear.insert(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("insert_time_lineal", array);
        createFile("total_size_lineal", total_sizes);
        createFile("numberof_files_lineal", numbers_of_files);

        array = new ArrayList<>();
        total_sizes = new ArrayList<>();
        numbers_of_files = new ArrayList<>();
        dir = new File("nodos_quad");
        next = start_n;
        counter = 0;
        start = System.currentTimeMillis();
        for(int i = 0; i < n; i++) {
            if(i % (n / 10) == 0) {
                System.out.println(counter + "% of data inserted in quad.");
                counter += 10;
            }
            if(i == next - 1){
                if(i == 511)
                    array.add(System.currentTimeMillis() - start);
                else
                    array.add(System.currentTimeMillis() - start + array.get(array.size()-1));
                directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    total_size = 0;
                    number_of_files = 0;
                    for (File child : directoryListing) {
                        total_size += child.length();
                        number_of_files++;
                    }
                    total_sizes.add(total_size);
                    numbers_of_files.add(number_of_files);
                }
                next *= 2;
                start= System.currentTimeMillis();
            }
            r = insert_data.get(i);
            quad.insert(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("insert_time_quad", array);
        createFile("total_size_quad", total_sizes);
        createFile("numberof_files_quad", numbers_of_files);

        array = new ArrayList<>();
        next = start_n / 10;
        start = System.currentTimeMillis();
        counter = 0;
        for(int i = 0; i < n / 10; i++) {
            if(i % (n / 100) == 0) {
                System.out.println(counter + "% of data searched in linear.");
                counter += 10;
            }
            if(i == next - 1){
                array.add(System.currentTimeMillis() - start);
                next *= 2;
            }
            r = search_data.get(i);
            linear.search(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("search_time_lineal", array);

        array = new ArrayList<>();
        next = start_n / 10;
        start = System.currentTimeMillis();
        counter = 0;
        for(int i = 0; i < n / 10; i++) {
            if(i % (n / 100) == 0) {
                System.out.println(counter + "% of data searched in quad.");
                counter += 10;
            }
            if(i == next - 1){
                array.add(System.currentTimeMillis() - start);
                next *= 2;
            }
            r = search_data.get(i);
            quad.search(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("search_time_quad", array);
        dir = new File("nodos_linear");
        try {
            FileUtils.deleteDirectory(dir);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        dir = new File("nodos_quad");
        try {
            FileUtils.deleteDirectory(dir);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testN(131072, 512);
    }
}
