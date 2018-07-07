package com.android.asynctask;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTaskLoaderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bitmap> {

    ImageView imageView;

    final static String TAG = "AsyncTaskLoader";

    ExecutorService executorService;

    static ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageview);
        progressBar = findViewById(R.id.progressbar);
        executorService = Executors.newCachedThreadPool();
        Log.d(TAG,"onCreate");
    }

    public void click(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("URL","http://www.freeimageslive.com/galleries/objects/general/pics/woodenbox0482.jpg");
        getLoaderManager().initLoader(1,bundle,this);
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

    @Override
    public Loader<Bitmap> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG,"onCreateLoader");
        URL url = stringToURL(bundle.getString("URL"));
        return new ImageDownloadTask(this,url);
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        Log.d(TAG,"onLoadFinished");
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {

    }

    static class ImageDownloadTask extends AsyncTaskLoader<Bitmap> {

        URL url;

        public ImageDownloadTask(@NonNull Context context,URL url) {
            super(context);
            this.url = url;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            Log.d(TAG,"onStartLoading");
            progressBar.setVisibility(View.VISIBLE);

            forceLoad();
        }

        @Nullable
        @Override
        public Bitmap loadInBackground() {
            return startDownload(url);
        }

    }

    private static Bitmap startDownload(URL url) {
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
