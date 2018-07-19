package com.gazman.coco.android;

import android.util.Log;
import com.gazman.coco.core.hash.Sha256Hash;

public class HelloLifeCycle {

    public void sayHi(){
        Log.d("lifeCycle", "Hello " + Sha256Hash.hash("Android"));
    }
}
