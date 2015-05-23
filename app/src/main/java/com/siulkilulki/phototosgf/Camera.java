package com.siulkilulki.phototosgf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Created by siulkilulki on 2015-05-01.
 */
public class Camera extends Activity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    String mCurrentPhotoPath;
    private PhotoViewAttacher mAttacher;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Gets the directory
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",             /* sufix */
                storageDir  /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return  image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex){
                Log.i("siulkilulki.camera", "Error occured while creating the File");
            }
            // Continue only if the File was succesfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.i("siulkilulki.camera", mCurrentPhotoPath);
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        dispatchTakePictureIntent();
        //finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {//jak jest zrobione zdjecie to sie wykonuje
            Log.i("siulkilulki.camera","photo taken and saved, probably");

            Mat image = Highgui.imread(mCurrentPhotoPath, Imgproc.COLOR_BGR2BGRA);
            Mat dstImage = image;
            Imgproc.GaussianBlur(image, dstImage, new Size(19,19), 0);
            //List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            //Imgproc.threshold(dstImage, image, 255, Imgproc.THRESH_BINARY, Imgproc.THRESH_BINARY);
            //Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            //Imgproc.drawContours(imageA, contours, 1, new Scalar(0,0,255));
            Highgui.imwrite(mCurrentPhotoPath, dstImage);
            if (image == null)
                Log.i("siulkilulki.camera", "image not read properly to Mat obj");
            else
                Log.i("siulkilulki.camera", "image read properly to Mat obj");

            mImageView = (ImageView) findViewById(R.id.camera_background);
            Bitmap bMap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mImageView.setImageBitmap(bMap);
            mAttacher = new PhotoViewAttacher(mImageView);//umozliwia zoom

        }
    }
}
