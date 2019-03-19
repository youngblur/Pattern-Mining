package EMMA;



import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        AlgoEMMA EMMA = new AlgoEMMA(2,2,true);
        FreEpisodes freEpisodes = EMMA.runAlgorithm("data/example_1.txt","data/out_example_1.txt");
        EMMA.printStats();


    }


}
