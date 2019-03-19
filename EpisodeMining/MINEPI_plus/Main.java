package MINEPI_plus;

import java.io.IOException;



public class Main {

    public static void main(String[] args) throws IOException {
        AlgoMINEPI_plus MINEPI_plus = new AlgoMINEPI_plus(2,10,true);
        FreEpisodes freEpisodes = MINEPI_plus.runAlgorithm("data/example_1.txt","data/out_example_2.txt");
        MINEPI_plus.printStats();

    }


}
