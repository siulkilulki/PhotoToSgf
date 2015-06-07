package com.siulkilulki.phototosgf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;




import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

import static org.opencv.core.Core.bitwise_not;

public class MainActivity extends ActionBarActivity {
    private ImageView mainImView;
    private Button button;
    private Context context = this;
    private PhotoViewAttacher mAttacher; // umozliwia zoom obrazka
    Bitmap bitmap;
    static {
        if(!OpenCVLoader.initDebug()){ /* initialize opencv library */
            Log.i("siulkilulki.opencv.main","opencv initialization failed");
        } else {
            Log.i("siulkilulki.opencv.main","opencv initialization successful");
        }
    }

    public void startCameraActivity(View view) {
        Intent intent = new Intent(this, Camera.class);
        //  starts Camera.java activity
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //ustawia layout na activity_main
        mainImView = (ImageView) findViewById(R.id.mainImageView //mainImageView jest w pliku layoutowym activity_main.xml. Przypisujemy to view do mainImView.
        );
        button = (Button) findViewById(R.id.buttonProcessImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String imageUri = "drawable://" + R.drawable.android_robot;
                //Uri path = Uri.parse("android.resource://com.segf4ult.test/" + R.drawable.android_robot);
                //String imageUri = path.toString();

                //Mat matImg = Utils.loadResource( context , R.drawable.android_robot, Highgui.CV_LOAD_IMAGE_COLOR);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plansza1); // tworzy bitmape z plansza.jpg
                //Mat matImg = Highgui.imread(imageUri);
                //if (matImg == null)
                  //  Log.i("siulkilulki.main",String.valueOf(matImg.cols()));
                //Bitmap bitmap1 = Bitmap.createBitmap(matImg.cols(), matImg.rows(), Bitmap.Config.ARGB_8888);
                Mat matImg = new Mat();
                Mat tempMat = new Mat();
                Utils.bitmapToMat(bitmap, matImg);
                //tempMat.convertTo(matImg, CvType.CV_32FC1);
                //matImg.convertTo(matImg, CvType.CV_32FC1);
                Mat dstMatImg = new Mat(matImg.rows(), matImg.cols(), matImg.type());
                Imgproc.cvtColor(matImg, dstMatImg, Imgproc.COLOR_BGR2GRAY); //dstMatImg bedzie czarno-biale
     //           Imgproc.GaussianBlur(dstMatImg, matImg, new Size(19, 19), 0);
            //    Imgproc.threshold(matImg, dstMatImg, 0, 255, Imgproc.THRESH_OTSU);
     //           Imgproc.adaptiveThreshold(matImg, dstMatImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);//bylo 15
            //    bitwise_not ( dstMatImg, matImg ); //inwersja bialego i czarnego

                //vector<Vec3f> circles;
                Mat circles = new Mat();
                Imgproc.HoughCircles(dstMatImg, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 35, 100, 50, 15, 25);



                String dump = circles.dump();
                Log.d("MAT", dump);
                if (circles.empty())
                    Log.d("EMPTY","E");
                else {

                    Log.d("Not Empty", "");
                }
/*
                Mat kernel = new Mat(35,35, CvType.CV_8U);
                byte data[] = new byte[1225];
                //tworzenie tablicy bajtów, jedynki są w ostatniej kolumnie i ostatnim wierszu
                for (int i = 0; i < 1225; i++) {
                    if (i%35 == 34 || i >= 1190) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }

                }

                kernel.put(0, 0, data); // wpycha tablice bajtów do kernela
                //Mat test = new Mat();
                //test = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));

                // do sprawdzania czy poprawny kernel
                byte[] return_buff = new byte[(int) (kernel.total() * kernel.channels())];
                kernel.get(0, 0, return_buff);
                for (int i = 0; i < return_buff.length; i++) {
                    Log.i("kernel","i ="+ String.valueOf(i)+": "+String.valueOf(return_buff[i]));
                }
                Point anchor = new Point(34,34);// ustawienie anchora w prawym dolnym rogu
                Point anchor1 = new Point(-1,-1);
                Imgproc.erode(matImg, matImg, kernel, anchor, 1);

                Log.i("kernel", "kernel total = " + String.valueOf(kernel.total()));
                Log.i("kernel", "kernel height = "+String.valueOf(kernel.height()));
                Log.i("kernel", "kernel width = "+String.valueOf(kernel.width()));
                //Imgproc.cornerHarris(matImg, dstMatImg, 2, 3, 0.04, 1);dawid


*/
                Utils.matToBitmap(dstMatImg, bitmap);
                //Highgui.imwrite(imageUri, img);
                mainImView.setImageBitmap(bitmap);//wyswietla "bitmap" w mainImView
                mAttacher = new PhotoViewAttacher(mainImView); // umozliwia zoom


            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
