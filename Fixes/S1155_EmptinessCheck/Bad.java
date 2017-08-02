package Fixes.S1155_EmptinessCheck;

import java.util.ArrayList;

/**
 * Created by kanna on 7/31/17.
 */
public class Bad {

    public static void main(String[] args) {
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        for(int i = 0;i < 10;i++) {
            integerArrayList.add(i);
        }

        while(integerArrayList.size() > 0) {
            integerArrayList.remove(0);
        }
    }

}
