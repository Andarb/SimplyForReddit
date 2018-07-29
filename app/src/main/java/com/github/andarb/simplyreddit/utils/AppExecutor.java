package com.github.andarb.simplyreddit.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static Executor sExecutor;

    public static Executor getExecutor() {
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        return sExecutor;
    }
}
