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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

        //----------------------------------- CIRCLES -------------------------------------------------------
         Log.i("zaczynam","circles");
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
                //Log.i("circles", "image type = " + String.valueOf(circleMat.type()) + " channels =" + String.valueOf(circleMat.channels()));
                Imgproc.HoughCircles(circleMat, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, circleMat.rows() / 26, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);
                Imgproc.cvtColor(circleMat, circleMat, Imgproc.COLOR_BayerRG2RGB);
                List<double[]> circlesCoordinates = new ArrayList<>();
                if (circles.cols() > 0) {
                    for (int i = 0; i < circles.cols(); i++) {
                        double vCircle[] = circles.get(0,i);

                        if (vCircle == null)
                            break;

                        Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                        int radius = (int)Math.round(vCircle[2]);

                        // draw the found circle
                        Core.circle(sourceImg, pt, radius, new Scalar(0,255,0), 2);
                        Core.circle(sourceImg, pt, 3, new Scalar(0,0,255), 2);

                        //podmienic vCircle[2] z promienia na kolor
                        double[] color = new double[3];
                        color = sourceImg.get((int)vCircle[1], (int)vCircle[0]);
                        if (color[0]==255){ //kolor bialy = 1
                            vCircle[2]=1;
                        }
                        else //kolor czaeny = -1
                            vCircle[2]=-1;

                        //wpycha wspolrzedne i kod koloru do listy
                        circlesCoordinates.add(vCircle);

                /*
                        //wyswietlanie wspolrzednych i kolorow kolek
                        String s = String.valueOf(color[0]);
                        String ss = String.valueOf(color[1]);
                        String sss = String.valueOf(color[2]);
                        String x = String.valueOf(vCircle[0]);
                        String y = String.valueOf(vCircle[1]);
                        String r = String.valueOf(vCircle[2]);

                        Log.i("kolorRGB", s+" "+ss+" "+sss);
                        Log.i("wspolrzedne x y r",x+" "+y+" "+r);
                */



                    }
                }
                Log.i("zaczynam", "threshold i erozje");
        //----------------------------------- CIRCLES END-------------------------------------------------------


                Imgproc.adaptiveThreshold(matImg, dstMatImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);





        //------------------------------------ EROSION -----------------------------
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


        //------------------------------------ EROSION END -----------------------------------
               // Point[] points = new Point[];
                Log.i("zaczynam","laczenie erozji");


                //laczenie kontorow z 4 erozji
                List<MatOfPoint> wszystkoJedno = new ArrayList<MatOfPoint>();
                wszystkoJedno.addAll(contours); //rozmiar = 1280
                wszystkoJedno.addAll(contours1);
                wszystkoJedno.addAll(contours2);
                wszystkoJedno.addAll(contours3);

                List<Point> points = new ArrayList<Point>();
                //wyciaganie tylko srodkow z kontorow
                for (int i = 0; i < wszystkoJedno.size()-1; i++) {
                    Rect r = Imgproc.boundingRect(wszystkoJedno.get(i));
                    points.add(i,new Point(r.x+r.width/2,r.y+r.height/2));
                }

                //int SS = points.size();
                //String ss = String.valueOf(SS);
                //Log.d("size of points with duplicates", ss);

                //usuniecie duplikatow z points
                Set<Point> hs = new HashSet<>();
                hs.addAll(points);
                wszystkoJedno.clear();
                points.clear();
                points.addAll(hs);

        //punktow przeciec jest za duzo wiec trzeba si pozbyc nadmiaru
            Log.i("tworze", "mat do cross");
                Mat crossImg = new Mat();
                Utils.bitmapToMat(bitmap, crossImg);
                crossImg.setTo(new Scalar(0, 0, 0));//wypelnienie maciezy czarnym kolorem


            Log.i("rysuje", "kolejne przeciecia");
                //rysowanie kolek na wszystkich przecieciach

                for (int i = 0; i < points.size(); i++) {
                    Core.circle(sourceImg, points.get(i), 20, new Scalar(255,0,0),10);
                    Core.circle(crossImg, points.get(i), 40, new Scalar(255,255,255),-1);//rysownie na specjalnej czarnj macierzy
                }


                Imgproc.cvtColor(crossImg, crossImg, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(crossImg, crossImg, new Size(19, 19), 0);

                Mat circles2 = new Mat();
                //circles.release();//usuwa poprzednia zawartosc

                Imgproc.HoughCircles(crossImg, circles2, Imgproc.CV_HOUGH_GRADIENT, 2.0, crossImg.rows() / 35, 100.0, 20.0, 35, 100);
                Imgproc.cvtColor(crossImg, crossImg, Imgproc.COLOR_BayerRG2RGB);

                List<double[]> crossCoordinates = new ArrayList<double[]>();
                if (circles2.cols() > 0) {
                    Log.i("jestem", "w circles2.cols>0");
                    for (int i = 0; i < circles2.cols(); i++) {
                        double vCircle[] = circles2.get(0,i);
                        if (vCircle == null)
                            break;


                        Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                        int radius = (int)Math.round(vCircle[2]);
                        // draw the found circle
                        Core.circle(crossImg, pt, radius, new Scalar(255,0,0), 4);
                        Core.circle(crossImg, pt, 3, new Scalar(0,0,255), 2);

                        //podmienic vCircle[2] z promienia na 0
                        vCircle[2]=0;
                        //wpycha wspolrzedne i kod koloru do listy
                        crossCoordinates.add(vCircle);

                /*
                        //wyswietlanie wspolrzednych i 0 na trzecim miejscu

                        String x = String.valueOf(vCircle[0]);
                        String y = String.valueOf(vCircle[1]);
                        String r = String.valueOf(vCircle[2]);
                        Log.i("wspolrzedne x y r",x+" "+y+" "+r);
                */



                    }
                }
                else
                    Log.i("circles2",String.valueOf(circles2.size()));
                points.clear();

        //---------------------------------- koniec pozbywania nadmiaru przeciec ---------



                //specjalny comperator do listy tablic
                final java.util.Comparator<double[]> comp = new java.util.Comparator<double[]>() {
                    public int compare(double[] a, double[] b) {
                        if (a[1] == b[1]){ //a i b sa wjednym rzedzie (porownuje y)
                            return Double.compare(a[0], b[0]);
                        }
                        else
                            return Double.compare(a[1], b[1]);
                    }
                };



                double tmp[] = new double[3];



         /*       //wypisuje wszystkie posortowane punkty przeciec
                Log.i("WSZYSTKIE CROSS", "patrz");
                Collections.sort(crossCoordinates, comp);
                for (int i = 0; i < crossCoordinates.size(); i++) {
                    tmp = crossCoordinates.get(i);
                    Log.i("x y 0", String.valueOf(tmp[0]) + " " + String.valueOf(tmp[1]) + " " + String.valueOf(tmp[2]));
                }
                Log.i("CrossCoordinates",String.valueOf(crossCoordinates.size()));
        */



            /*
                Log.i("circleCcoordinates", String.valueOf(circlesCoordinates.size()));

                Log.i("WSZYSTKIE CIRCLES","");
                for (int i = 0; i < circlesCoordinates.size(); i++) {
                    tmp = circlesCoordinates.get(i);
                    Log.i("x y kolor", String.valueOf(tmp[0]) + " " + String.valueOf(tmp[1]) + " " + String.valueOf(tmp[2]));
                }
            */

                List<double[]> coordinates = new ArrayList<double[]>();

                coordinates.addAll(circlesCoordinates);
                coordinates.addAll(crossCoordinates);
                Collections.sort(coordinates, comp);
            /*
                //wypisuje wszystkie posortowane punkty
                Log.i("WSZYSTKIE punkty", "patrz");
                for (int i = 0; i < coordinates.size(); i++) {
                    tmp = coordinates.get(i);
                    Log.i("x y 0", String.valueOf(tmp[0]) + " " + String.valueOf(tmp[1]) + " " + String.valueOf(tmp[2]));
                }

            */



                int x=1, y=1;
       //OSTATECZNE WYLICZANIE NUMEROW LINII
                Log.i("coordinates", String.valueOf(coordinates.size()));
                for (int i = 0; i < coordinates.size(); i++) {

                    //tmp = coordinates.get(i);
                    //Log.i("x y kolor", String.valueOf(tmp[0]) + " " + String.valueOf(tmp[1]) + " " + String.valueOf(tmp[2]));
                    //Log.i("",String.valueOf(i%19+1) + " " + String.valueOf(Math.floor(i / 19)+1) + " " + String.valueOf(tmp[2]));
                    tmp = coordinates.get(i);
                    if (tmp[2] == 1){
                        //Log.i("x", String.valueOf(tmp[1]) + "\t" + "y: "+ String.valueOf(tmp[0]) + "\t" + String.valueOf(tmp[2])+"\t"+"kamien bialy");
                        //Log.i("x", String.valueOf((int)Math.floor(i / 19)+1) + "\t" + "y: "+ String.valueOf(i%19+1) + "\t" + String.valueOf((int)tmp[2])+"\t"+"kamien bialy");
                        Log.i("x", String.valueOf(y) + "\t" + "y: "+ String.valueOf(x) + "\t" + String.valueOf((int)tmp[2])+"\t"+"kamien bialy");
                    }
                    else if (tmp[2] == -1){
                        //Log.i("x", String.valueOf(tmp[1]) + "\t" + "y: "+ String.valueOf(tmp[0]) + "\t" + String.valueOf(tmp[2])+"\t"+"kamien czarny");
                        //Log.i("x", String.valueOf((int)Math.floor(i / 19)+1) + "\t" + "y: " + String.valueOf(i%19+1) + "\t" + String.valueOf((int)tmp[2])+"\t"+"kamien czarny");
                        Log.i("x", String.valueOf(y) + "\t" + "y: "+ String.valueOf(x) + "\t" + String.valueOf((int)tmp[2])+"\t"+"kamien czarny");
                    }
                    else
                        //Log.i("x", String.valueOf(tmp[1]) + "\t" + "y: "+ String.valueOf(tmp[0]) + "\t" + String.valueOf(tmp[2]));
                        //Log.i("x", String.valueOf((int)Math.floor(i / 19)+1) + "\t" + "y: " + String.valueOf(i%19+1) + "\t" + String.valueOf((int)tmp[2]));
                        Log.i("x", String.valueOf(y) + "\t" + "y: "+ String.valueOf(x) + "\t" + String.valueOf((int)tmp[2]));

                    if (x<19)//kolumny
                        x++;
                    else{
                        x=1;
                        y++;
                    }//



                }





                //Utils.matToBitmap(crossImg, bitmap); //do przeciec
                Utils.matToBitmap(sourceImg, bitmap);
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
