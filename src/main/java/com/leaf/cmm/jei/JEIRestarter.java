package com.leaf.cmm.jei;

public final class JEIRestarter {
    public static Runnable JEI_RESTART;

    public static void restart() {
        if (JEI_RESTART != null) {
            JEI_RESTART.run();
        }
    }
}
