import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.io.FileUtils;

public class LinearTest {

    private static void testN(long n, int iterations, String output_path, char mode) {
        RTree tree;
        double a, b, c, d;
        Random generator = new Random();
        ArrayList<Long> insert_times = new ArrayList<>();
        ArrayList<Long> total_sizes = new ArrayList<>();
        ArrayList<Long> numbers_of_files = new ArrayList<>();
        ArrayList<Long> node_size_averages = new ArrayList<>();
        ArrayList<Long> search_times = new ArrayList<>();
        long start;
        long total_size;
        long number_of_files;
        long ten_percent = n / 10;
        long ten_ten_percent = ten_percent / 10;
        int counter;
        for(int i = 0; i < iterations; i++) {
            System.out.println("Starting iteration " + i + ".");
            tree = new RTree("nodos",90,60, mode);

            counter = 0;
            System.out.println("Starting insertion test, iteration " + i + ".");
            start = System.currentTimeMillis();
            for (long j = 0; j < n; j++) {
                if(j % ten_percent == 0) {
                    System.out.println(counter + "% of data inserted.");
                    counter += 10;
                }
                a = 100 * generator.nextDouble();
                b = 100 * generator.nextDouble();
                c = 100 * generator.nextDouble();
                d = 100 * generator.nextDouble();
                tree.insert(Math.min(a, b), Math.max(a, b), Math.min(c, d), Math.max(c, d));
            }
            insert_times.add(System.currentTimeMillis() - start);
            System.out.println("100% of data inserted.\n");

            System.out.println("Starting memory analysis, iteration " + i + ".");
            total_size = 0;
            number_of_files = 0;
            File dir = new File("nodos");
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    total_size += child.length();
                    number_of_files++;
                }
            }
            total_sizes.add(total_size);
            numbers_of_files.add(number_of_files);
            node_size_averages.add(total_size / number_of_files);
            System.out.println("\n");

            counter = 0;
            System.out.println("Starting search test, iteration " + i + ".");
            start = System.currentTimeMillis();
            for (long j = 0; j < ten_percent; j++) {
                if(j % ten_ten_percent== 0) {
                    System.out.println(counter + "% of data searched.");
                    counter += 10;
                }
                a = 100 * generator.nextDouble();
                b = 100 * generator.nextDouble();
                c = 100 * generator.nextDouble();
                d = 100 * generator.nextDouble();
                tree.search(Math.min(a, b), Math.max(a, b), Math.min(c, d), Math.max(c, d));
            }
            search_times.add(System.currentTimeMillis() - start);
            System.out.println("100% of data searched.");

            System.out.println("Deleting nodes, iteration " + i + ".");
            System.out.println("=================================\n");
            try {
                FileUtils.deleteDirectory(dir);
            }
            catch (IOException e) {
                System.err.format("IOException: %s%n", e);
            }
        }
        StringBuilder output = new StringBuilder();
        output.append("insert_times\t");
        for(int i = 0; i < iterations; i++) {
            output.append(insert_times.get(i));
            if(i == iterations - 1) {
                output.append("\n");
            } else {
                output.append("\t");
            }
        }
        output.append("total_sizes\t");
        for(int i = 0; i < iterations; i++) {
            output.append(total_sizes.get(i));
            if(i == iterations - 1) {
                output.append("\n");
            } else {
                output.append("\t");
            }
        }
        output.append("number_of_files\t");
        for(int i = 0; i < iterations; i++) {
            output.append(numbers_of_files.get(i));
            if(i == iterations - 1) {
                output.append("\n");
            } else {
                output.append("\t");
            }
        }
        output.append("node_size_av\t");
        for(int i = 0; i < iterations; i++) {
            output.append(node_size_averages.get(i));
            if(i == iterations - 1) {
                output.append("\n");
            } else {
                output.append("\t");
            }
        }
        output.append("search_times\t");
        for(int i = 0; i < iterations; i++) {
            output.append(search_times.get(i));
            if(i == iterations - 1) {
                output.append("\n");
            } else {
                output.append("\t");
            }
        }

        try (FileWriter writer = new FileWriter(output_path); BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(output.toString());
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public static void main(String[] args) {
        long n = 262144; //2**9
        int iter = 1;
        for(int i=0; i<9; i++) {
            //if(i == 0) {
            //    String file_name2 = "quadratic_" + n;
            //    testN(n, iter, file_name2, 'q');
            //}else {
                String file_name = "lineal_" + n;
                testN(n, iter, file_name, 'l');
                String file_name2 = "quadratic_" + n;
                testN(n, iter, file_name2, 'q');
            //}
            n *= 2;
        }
    }
}
