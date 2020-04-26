package Outlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static java.lang.StrictMath.pow;

public class ScoreEngine {
    public static final Short NUM_PROBLEMS = 12;    // The number of programming problems there are

    private static final int ROWS = 2000;
    private static final int COLS = 3000;
    private static final int DIST = 150;
    private static final int SIZE = 40000;

    private static int[][] map = null;

    /**
     * Loads up the map file.
     * @return
     * @throws FileNotFoundException
     */
    public static int initialize() throws FileNotFoundException {
        Scanner scan = new Scanner(new File("/opt/TXCS/map.txt"));
        map = new int[ROWS][COLS];
        if (!scan.hasNext()){
            return -1;
        }
        for(int r = 0; r < ROWS; r++){
            for(int c = 0; c < COLS; c++){
                try{
                    map[r][c] = scan.nextInt();
                } catch (Exception e) {
                    System.out.println("--MAP ERROR--");
                    return -2;
                }
            }
        }
        System.out.println("--MAP Initialized--");
        return 0;
    }

    /**
     * Scores a submission, returning the # districts won, the location
     * @param probNum
     * @return boolean success
     */
    public static boolean score(short probNum, byte[] bytes){
        System.out.println("--SCORING--");
        if(probNum<0 || probNum >= NUM_PROBLEMS) return false;

        /**
         * Sam's stuff here
         */

        return false;
    }
}
