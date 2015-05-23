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
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

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
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plansza); // tworzy bitmape z plansza.jpg
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
                Imgproc.GaussianBlur(dstMatImg, matImg, new Size(19, 19), 0);
                Imgproc.threshold(matImg, dstMatImg, 0,255,Imgproc.THRESH_OTSU);

                //Imgproc.cornerHarris(matImg, dstMatImg, 2, 3, 0.04, 1);dawid
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
