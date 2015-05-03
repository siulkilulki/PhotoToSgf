package com.siulkilulki.phototosgf;

/**
 * Created by siulkilulki on 2015-05-02.
 */
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class ImageProcessing {
    static {
        if(!OpenCVLoader.initDebug()){ /* initialize opencv library */
            Log.i("siulkilulki.opencv", "opencv initialization failed");
        } else {
            Log.i("siulkilulki.opencv","opencv initialization successful");
        }
    }

}
