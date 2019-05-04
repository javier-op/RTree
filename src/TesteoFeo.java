import java.util.ArrayList;

public class TesteoFeo {


    public static void main(String[] args) {
        RTree arbolito = new RTree("nodos",90,60, 'q');


        for(double i=0.0;i<950;i++){
            arbolito.insert(i,i+1, i, i+1);

        }

        ArrayList<Rectangle> results = arbolito.search(0,1, 0, 1);

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
    }
}