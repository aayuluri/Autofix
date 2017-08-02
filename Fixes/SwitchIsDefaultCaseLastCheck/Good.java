package Fixes.SwitchIsDefaultCaseLastCheck;

import java.util.Random;

/**
 * Created by kanna on 7/31/17.
 */
public class Good {
    public static void main(String[] args) {
        Random random = new Random();
        int check = random.nextInt(3);
        check = 3;
        switch (check) {
            case 1:
                System.out.println("It's One");
                break;
            case 2:
                System.out.println("It's two");
                break;
            default:
        }
    }
}
