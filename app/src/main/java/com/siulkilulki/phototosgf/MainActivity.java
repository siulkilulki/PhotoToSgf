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
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.senab.photoview.PhotoViewAttacher;

import static org.opencv.core.Core.circle;

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

                Utils.bitmapToMat(bitmap, matImg);
                //tempMat.convertTo(matImg, CvType.CV_32FC1);
                //matImg.convertTo(matImg, CvType.CV_32FC1);
                Mat dstMatImg = new Mat(matImg.rows(), matImg.cols(), matImg.type());
                Imgproc.cvtColor(matImg, dstMatImg, Imgproc.COLOR_BGR2GRAY); //dstMatImg bedzie czarno-biale
                Imgproc.GaussianBlur(dstMatImg, matImg, new Size(19, 19), 0);
                //Imgproc.threshold(matImg, dstMatImg, 0, 255, Imgproc.THRESH_OTSU);

                Mat sourceImg = new Mat();
                Utils.bitmapToMat(bitmap, sourceImg);
                Mat grayImg =  new Mat(sourceImg.rows(), sourceImg.cols(), sourceImg.type());
                Imgproc.cvtColor(sourceImg, grayImg, Imgproc.COLOR_BGR2GRAY);

                Mat blurImg = new Mat(sourceImg.rows(), sourceImg.cols(), sourceImg.type());
                Imgproc.GaussianBlur(grayImg, blurImg, new Size(19, 19), 0);
                Mat circleMat =  new Mat(sourceImg.rows(), sourceImg.cols(), sourceImg.type());
                circleMat = blurImg;
                //Imgproc.adaptiveThreshold(blurImg, circleMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 4);
                Mat circles = new Mat();
                double iCannyUpperThreshold = 100.0;
                int iMinRadius = 10;
                int iMaxRadius = 100;
                double iAccumulator = 100.0;
                Log.i("circles", "image type = " + String.valueOf(circleMat.type()) + " channels =" + String.valueOf(circleMat.channels()));
                Imgproc.HoughCircles(circleMat, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, circleMat.rows() / 20, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);
                Imgproc.cvtColor(circleMat, circleMat, Imgproc.COLOR_BayerRG2RGB);
                List<double[]> circlesCoordinates = new ArrayList<>();
                if (circles.cols() > 0) {
                    for (int i = 0; i < circles.cols(); i++) {
                        double vCircle[] = circles.get(0,i);
                        circlesCoordinates.add(vCircle);
                        if (vCircle == null)
                            break;

                        Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                        int radius = (int)Math.round(vCircle[2]);
                        // draw the found circle
                        Core.circle(circleMat, pt, radius, new Scalar(0,255,0), 2);
                        Core.circle(circleMat, pt, 3, new Scalar(0,0,255), 2);
                    }
                }
                Log.i("circles", "circle columns = "+String.valueOf(circles.cols()));

                Imgproc.adaptiveThreshold(matImg, dstMatImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);



///*

            //-----------------------------EROZJA-----------------------------
                final int sideSize = 40;
                Mat kernel = new Mat(sideSize,sideSize, CvType.CV_8U);
                /*byte data[] = new byte[625];
                //tworzenie tablicy bajtów, jedynki są w ostatniej kolumnie i ostatnim wierszu
                for (int i = 0; i < 625; i++) {
                    if (i%25 == 24 || i >= 600) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }

                }*/
                //  _|
                byte data[] = new byte[sideSize*sideSize];
                for (int i = 0; i < sideSize*sideSize; i++) {
                    if(i%sideSize == sideSize-1 || i >= sideSize*sideSize-sideSize) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }
                }

                kernel.put(0, 0, data); // wpycha tablice bajtów do kernela
                //Mat test = new Mat();
                //test = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));
    //---------------------------------------------------------------------------------------------
    // do sprawdzania czy poprawny kernel
    /*            byte[] return_buff = new byte[(int) (kernel.total() * kernel.channels())];
                kernel.get(0, 0, return_buff);
                for (int i = 0; i < return_buff.length; i++) {
                    Log.i("kernel","i ="+ String.valueOf(i)+": "+String.valueOf(return_buff[i]));
                }
    */
                //---------------------------------------------------------------------------------------------
                Point anchor = new Point(sideSize-1,sideSize-1);// ustawienie anchora w prawym dolnym rogu
                Imgproc.erode(dstMatImg, matImg, kernel, anchor, 1);

            //    Log.i("kernel", "kernel total = " + String.valueOf(kernel.total()));
            //    Log.i("kernel", "kernel height = "+String.valueOf(kernel.height()));
            //    Log.i("kernel", "kernel width = "+String.valueOf(kernel.width()));
                //Imgproc.cornerHarris(matImg, dstMatImg, 2, 3, 0.04, 1);dawid

                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(matImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


                //------------------------------------------------------------
                //   ^^|
                for (int i = 0; i < sideSize*sideSize; i++) {
                    if(i%sideSize == sideSize-1 || i < sideSize) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }
                }

                kernel.put(0, 0, data); // wpycha tablice bajtów do kernela

                anchor = new Point(sideSize-1,0);// ustawienie anchora w prawym dolnym rogu

                Imgproc.erode(dstMatImg, matImg, kernel, anchor, 1);

                List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
                Mat hierarchy1 = new Mat();
                Imgproc.findContours(matImg, contours1, hierarchy1, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


                //-----------------------------------------------------------------------

                //   |^^
                for (int i = 0; i < sideSize*sideSize; i++) {
                    if(i%sideSize == 0 || i < sideSize) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }
                }

                kernel.put(0, 0, data); // wpycha tablice bajtów do kernela

                anchor = new Point(0,0);// ustawienie anchora w prawym dolnym rogu

                Imgproc.erode(dstMatImg, matImg, kernel, anchor, 1);

                List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
                Mat hierarchy2 = new Mat();
                Imgproc.findContours(matImg, contours2, hierarchy2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


                //-----------------------------------------------------------------------
                //   |_   =   L
                for (int i = 0; i < sideSize*sideSize; i++) {
                    if(i%sideSize == 0 || i >= sideSize*sideSize-sideSize) {
                        data[i] = 1;
                    }
                    else {
                        data[i] = 0;
                    }
                }

                kernel.put(0, 0, data); // wpycha tablice bajtów do kernela

                anchor = new Point(0,sideSize-1);// ustawienie anchora w prawym dolnym rogu

                Imgproc.erode(dstMatImg, matImg, kernel, anchor, 1);

                List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();
                Mat hierarchy3 = new Mat();
                Imgproc.findContours(matImg, contours3, hierarchy3, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


                //-----------------------------------------------------------------------
               // Point[] points = new Point[];




                List<MatOfPoint> wszystkoJedno = new ArrayList<MatOfPoint>();
                wszystkoJedno.addAll(contours); //rozmiar = 1280
                wszystkoJedno.addAll(contours1);
                wszystkoJedno.addAll(contours2);
                wszystkoJedno.addAll(contours3);

                List<Point> points = new ArrayList<Point>();

                for (int i = 0; i < wszystkoJedno.size()-1; i++) {
                    Rect r = Imgproc.boundingRect(wszystkoJedno.get(i));
                    //Core.rectangle(mat, new Point(r.x - 10, r.y - 10), new Point(r.x + r.width + 10, r.y + r.height + 10), new Scalar(0, 0, 255), 2, 8, 0);
                    points.add(i,new Point(r.x+r.width/2,r.y+r.height/2));
                }

                int SS = points.size();
                String ss = String.valueOf(SS);
                Log.d("size of points with d", ss);

                Set<Point> hs = new HashSet<>();
                hs.addAll(points);
                wszystkoJedno.clear();
                points.clear();
                points.addAll(hs);

                SS = points.size();
                ss = String.valueOf(SS);
                Log.d("size of points ", ss);

                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                for (int i = 0; i < points.size(); i++) {
                    //Log.i("JEST w rysowaniu", "kolek");
                    Core.circle(mat, points.get(i), 20, new Scalar(255,0,0),10);

                }
                double tmp[] = new double[3];
                List<double[]> crossCoordinates = new ArrayList<double[]>();

                for (int i = 0; i < points.size(); i++) {
                    tmp[0]=points.get(i).x;
                    tmp[1]=points.get(i).y;
                    tmp[2]=0;
                    crossCoordinates.add(i, tmp);

                }



            /*
                    int SS = contours.size()+contours1.size()+contours2.size()+contours3.size();
                    String ss = String.valueOf(SS);
                    int S = wszystkoJedno.size();
                    String s = String.valueOf(S);
                    Log.d("Not Empty ", s);
                    Log.d("Not Empty ", ss);

             //   for (int j=0; j<10; j++) {
             //       String N = String.valueOf(contours.get(j).total());
             //       Log.i("TOTAL POINT START", N);

              //  }
                Point[] tt = new Point[20];

                Mat matt = wszystkoJedno.get(1);
                String t = matt.toString();
                //Point m = matt[0][0];
                //String M = String.valueOf(matt[0][0]);
                Log.i("TOTAL POINT END", t);

            */


//*/
                Utils.matToBitmap(mat, bitmap);
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
