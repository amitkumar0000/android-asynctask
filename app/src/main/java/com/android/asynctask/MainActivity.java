package com.android.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    final String TAG = "AsyncTask";

    ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageview);
        executorService = Executors.newCachedThreadPool();
    }

    public void click(View view) {
        new ImageDownloadTask(this).execute(stringToURL("http://www.freeimageslive.com/galleries/objects/general/pics/woodenbox0482.jpg"));
        new ImageDownloadTask(this).executeOnExecutor(executorService,stringToURL("http://www.freeimageslive.com/galleries/objects/general/pics/woodenbox0482.jpg"));
    }

    URL stringToURL(String url) {
        URL url1 = null;
        try {
            url1 = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url1;
    }

    class ImageDownloadTask extends AsyncTask<URL, String, Bitmap> {
        Context context;

        public ImageDownloadTask(Context context){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG,"onPreExecute");

        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            Bitmap bitmap = null;

            for (URL url : urls) {
                bitmap = startDownload(url);
            }

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG,"onProgressUpdate");

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.d(TAG,"onPostExecute bitmap Size:: " +bitmap.getByteCount()/1024 + " KB");

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap startDownload(URL url) {
        Log.d(TAG," Start Download from url :: "+url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//            Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
            Bitmap bmp = decodeSampledBitmapFromStream(bufferedInputStream,100,100);
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }



    public static Bitmap decodeSampledBitmapFromStream(InputStream inputStream, int reqWidth, int reqHeight) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            int n;
            byte[] buffer = new byte[1024];
            while ((n = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, n);
            }
            return decodeSampledBitmapFromByteArray(outputStream.toByteArray(), reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int
            reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
