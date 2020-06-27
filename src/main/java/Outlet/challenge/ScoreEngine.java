package Outlet.challenge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static java.lang.StrictMath.pow;

public class ScoreEngine {
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
     * @param sub
     * @return ImmutablePair<Integer, Double> scored
     */
    public static int score(String sub, ChallengeEntry entry){
        System.out.println("--SCORING--");
        if(sub==null || sub.isEmpty()) return 1;

        int[][] input = new int[ROWS][COLS];
        int[][] location = new int[DIST+1][2];
        int[] counted = new int[DIST+1];
        double[][] centers = new double[DIST+1][2];
        long[] votes = new long[DIST+1];
        int won = 0;
        double loc = 0;

        // Now, load in the input into the input array
        Scanner scan = new Scanner(sub);
        int temp;
        for(int r = 0; r < ROWS; r++){
            for(int c = 0; c < COLS; c++){
                try{
                    temp = scan.nextInt();
                    if (temp > 0 && temp <= DIST){
                        if(counted[temp]==0){
                            counted[temp] = 1;
                            location[temp][0] = r;
                            location[temp][1] = c;
                        }
                        centers[temp][0]+=r;
                        centers[temp][1]+=c;
                        input[r][c] = temp;
                    } else {
                        return 1;
                    }
                } catch (Exception e) {
                    return -1;
                }
            }
        }
        if(map==null) {
            try {
                initialize();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return -1;
            }
        }

        ArrayList<ImmutablePair<Integer, Integer>> dfs = new ArrayList<>();
        for(int i = 1; i < DIST+1; i++){
            centers[i][0]/=SIZE;
            centers[i][1]/=SIZE;
            dfs.add(new ImmutablePair<>(location[i][0], location[i][1]));
            input[location[i][0]][location[i][1]]*=-1;
            counted[i] = 0;
            while(dfs.size()>0){
                int r = dfs.get(dfs.size()-1).left;
                int c = dfs.get(dfs.size()-1).right;
                dfs.remove(dfs.size()-1);
                counted[i]++;
                votes[i]+=map[r][c];
                loc+=pow(r-centers[i][0],2)+pow(c-centers[i][1],2);
                if(r+1 < ROWS && input[r+1][c] == i){
                    input[r+1][c]*=-1;
                    dfs.add(new ImmutablePair<>(r + 1, c));
                }
                if(r-1 >= 0 && input[r-1][c] == i){
                    input[r-1][c]*=-1;
                    dfs.add(new ImmutablePair<>(r - 1, c));
                }
                if(c+1 < COLS && input[r][c+1] == i){
                    input[r][c+1]*=-1;
                    dfs.add(new ImmutablePair<>(r, c + 1));
                }
                if(c-1 >= 0 && input[r][c-1] == i){
                    input[r][c-1]*=-1;
                    dfs.add(new ImmutablePair<>(r, c - 1));
                }
            }
            if (counted[i] != SIZE){
                return 1;
            }
        }
        //calculate votes won
        for(int i = 1; i < DIST+1; i++)
            if (votes[i] > 0) won++;

        entry.addFRQRun(won, loc);
        return 0;
    }
}
