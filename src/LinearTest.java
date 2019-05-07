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
        n = (int) Math.pow(2, n);
        start_n = (int) Math.pow(2, start_n);

        ArrayList<Rectangle> insert_data = genN(n);
        ArrayList<Rectangle> search_data = genN(n / 10);
        RTree linear = new RTree("nodos_linear",90,60,'l');
        RTree quad = new RTree("nodos_quad",90,60,'q');

        ArrayList<Long> total_sizes = new ArrayList<>();
        ArrayList<Long> numbers_of_files = new ArrayList<>();
        ArrayList<Long> insert_time = new ArrayList<>();
        ArrayList<Long> search_time = new ArrayList<>();
        ArrayList<Long> insert_access = new ArrayList<>();
        ArrayList<Long> search_access = new ArrayList<>();
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
                if(i == 511) {
                    insert_time.add(System.currentTimeMillis() - start);
                    insert_access.add(linear.get_disk_access_counter());
                }
                else {
                    insert_time.add(System.currentTimeMillis() - start + insert_time.get(insert_time.size() - 1));
                    insert_access.add(linear.get_disk_access_counter() + insert_access.get(insert_access.size() - 1));
                }
                int n_search = next / 10;
                int counter_search = 0;
                System.out.println("Reached linear tree of size " + next + ", starting search.");
                linear.reset_disk_access_counter();
                start = System.currentTimeMillis();
                for(int j = 0; j < n_search; j++) {
                    if(j % (n_search / 10) == 0) {
                        System.out.println(counter_search + "% of data searched in linear, for n = " + next + ".");
                        counter_search += 10;
                    }
                    r = search_data.get(j);
                    linear.search(r.x0, r.x1, r.y0, r.y1);
                }
                search_access.add(linear.get_disk_access_counter());
                search_time.add(System.currentTimeMillis() - start);
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
                linear.reset_disk_access_counter();
                start = System.currentTimeMillis();
            }
            r = insert_data.get(i);
            linear.insert(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("insert_time_linear", insert_time);
        createFile("total_size_linear", total_sizes);
        createFile("numberof_files_linear", numbers_of_files);
        createFile("search_time_linear", search_time);
        createFile("insert_access_linear", insert_access);
        createFile("search_access_linear", search_access);

        insert_time = new ArrayList<>();
        total_sizes = new ArrayList<>();
        numbers_of_files = new ArrayList<>();
        search_time = new ArrayList<>();
        insert_access = new ArrayList<>();
        search_access = new ArrayList<>();
        dir = new File("nodos_quad");
        next = start_n;
        counter = 0;
        start = System.currentTimeMillis();
        for(int i = 0; i < n; i++) {
            if(i % (n / 10) == 0) {
                System.out.println(counter + "% of data inserted in quadratic.");
                counter += 10;
            }
            if(i == next - 1){
                if(i == 511) {
                    insert_time.add(System.currentTimeMillis() - start);
                    insert_access.add(quad.get_disk_access_counter());
                }
                else {
                    insert_time.add(System.currentTimeMillis() - start + insert_time.get(insert_time.size() - 1));
                    insert_access.add(quad.get_disk_access_counter() + insert_access.get(insert_access.size() - 1));
                }
                int n_search = next / 10;
                int counter_search = 0;
                System.out.println("Reached quadratic tree of size " + next + ", starting search.");
                quad.reset_disk_access_counter();
                start = System.currentTimeMillis();
                for(int j = 0; j < n_search; j++) {
                    if(j % (n_search / 10) == 0) {
                        System.out.println(counter_search + "% of data searched in quadratic, for n = " + next + ".");
                        counter_search += 10;
                    }
                    r = search_data.get(j);
                    quad.search(r.x0, r.x1, r.y0, r.y1);
                }
                search_access.add(quad.get_disk_access_counter());
                search_time.add(System.currentTimeMillis() - start);
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
                quad.reset_disk_access_counter();
                start = System.currentTimeMillis();
            }
            r = insert_data.get(i);
            quad.insert(r.x0, r.x1, r.y0, r.y1);
        }
        createFile("insert_time_quad", insert_time);
        createFile("total_size_quad", total_sizes);
        createFile("numberof_files_quad", numbers_of_files);
        createFile("search_time_quad", search_time);
        createFile("insert_access_quad", insert_access);
        createFile("search_access_quad", search_access);

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
        testN(13, 9);
    }
}
