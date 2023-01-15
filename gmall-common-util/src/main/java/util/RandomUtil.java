package util;

import java.util.Random;

public class RandomUtil {
    private  RandomUtil() {
    }

    private  static volatile  Random random = null;

    private static final Object Lock = new Object();

    public static Random getRandom() {
        if (random == null){
            synchronized (Lock){
                if (random == null){
                    random = new Random();
                }
            }
        }
        return random;
    }
}
