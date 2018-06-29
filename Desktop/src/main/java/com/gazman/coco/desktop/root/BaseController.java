package com.gazman.coco.desktop.root;

import com.gazman.coco.desktop.ScreensController;
import com.gazman.lifecycle.Factory;

/**
 * Created by Ilya Gazman on 3/9/2018.
 */
public class BaseController {
    protected ScreensController screens = Factory.inject(ScreensController.class);
}
