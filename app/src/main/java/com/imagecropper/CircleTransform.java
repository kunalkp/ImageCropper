package com.imagecropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class CircleTransform extends BitmapTransformation {
    /**
     * Parameterized constructor function
     *
     * @param context of caller view
     */
    public CircleTransform(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        try {
            CropCircleAsync async = new CropCircleAsync(pool, toTransform);
            async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return async.get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crop & create circular Bitmap.
     */
    private static class CropCircleAsync extends AsyncTask<Void, Void, Bitmap> {
        private BitmapPool pool;
        private Bitmap source;

        /**
         * Constructor function.
         *
         * @param pool   bitmap pool
         * @param source contains bitmap drawable
         */
        CropCircleAsync(@NonNull BitmapPool pool, @NonNull Bitmap source) {
            this.pool = pool;
            this.source = source;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (this.source == null) {
                return null;
            }

            int size = Math.min(this.source.getWidth(), this.source.getHeight());
            int x = (this.source.getWidth() - size) / 2;
            int y = (this.source.getHeight() - size) / 2;
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
            Bitmap result = this.pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}