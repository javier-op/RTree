import java.util.ArrayList;
import java.util.Random;

public class TesteoFeo {

    public static void main(String[] args) {

        RTree arbolito = new RTree("nodos",90,60, 'q');

        long startTime = System.currentTimeMillis();
        for(double i=0.0;i<950;i++){
            Rectangle r = arbolito.rectangleGenerator();
            arbolito.insert(r.x0, r.x1, r.y0, r.y1);
        }
        long endTime = System.currentTimeMillis() - startTime;
        ArrayList<Rectangle> results = arbolito.search(600,601, 600, 601);

        System.out.println(results.size());
        String template = "x0=%.2f, x1=%.2f, y0=%.2f, y1=%.2f\n";
        System.out.println(String.format(
                template,
                results.get(0).x0,
                results.get(0).x1,
                results.get(0).y0,
                results.get(0).y1)
        );

        System.out.println(String.format(
                template,
                results.get(1).x0,
                results.get(1).x1,
                results.get(1).y0,
                results.get(1).y1)
        );

        System.out.println(endTime);
    }
}