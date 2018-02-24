package test;

import model.World;
import org.junit.Assert;

public class TestUtil {
    public static void do1000Ticks(World world) {
        for (int i = 0; i < 1000; i++) {
            world.tick();
        }
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Assert.fail();
        }
    }
}
