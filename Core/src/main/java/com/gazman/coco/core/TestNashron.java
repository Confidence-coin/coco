package com.gazman.coco.core;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by Ilya Gazman on 3/13/2018.
 */
public class TestNashron {

    public static void main(String... args) {
        long startTime = 0;
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval("function go(data){data[1] = 90;return data;}");
            Invocable invocable = (Invocable) engine;
            int[] data = new int[3];
            data[1] = 1;
            data[2] = 2;
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                invocable.invokeFunction("go", data);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
}
