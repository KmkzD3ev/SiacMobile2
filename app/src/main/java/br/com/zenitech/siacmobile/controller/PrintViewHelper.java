package br.com.zenitech.siacmobile.controller;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import br.com.zenitech.siacmobile.util.DynamicUnitUtils;

public class PrintViewHelper {
    int WIDTH_DEFAULT = 360;
    int POSITION_DEFAULT = 0;
    int UNSPECIFIED_SIZE = 0;
    int DEFAULT_PADDING = 0;

    public Bitmap generateBitmapFromView(View view) {
        view.setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        view.measure(View.MeasureSpec.makeMeasureSpec(WIDTH_DEFAULT, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(UNSPECIFIED_SIZE, View.MeasureSpec.UNSPECIFIED));
        view.layout(POSITION_DEFAULT, POSITION_DEFAULT, view.getMeasuredWidth(),
                view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /*public Bitmap generateBitmapFromView(View view) {
        view.setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        view.measure(View.MeasureSpec.makeMeasureSpec(WIDTH_DEFAULT, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(UNSPECIFIED_SIZE, View.MeasureSpec.UNSPECIFIED));
        view.layout(POSITION_DEFAULT, POSITION_DEFAULT, view.getMeasuredWidth(),
                view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }*/

    public @NonNull
    Bitmap createBitmapFromView2(@NonNull View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    /**
     * Creates a bitmap from the supplied view.
     *
     * @param view   The view to get the bitmap.
     * @param width  The width for the bitmap.
     * @param height The height for the bitmap.
     * @return The bitmap from the supplied drawable.
     */
    public @NonNull
    Bitmap createBitmapFromView(@NonNull View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();

        if (background != null) {
            background.draw(canvas);
        }
        view.draw(canvas);

        return bitmap;
    }

    // IMAGEM GIRADA 90º
    public @NonNull
    Bitmap createBitmapFromView90(@NonNull View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        float degrees = 45; //rotation degree
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();

        if (background != null) {
            background.draw(canvas);
        }
        view.draw(canvas);

        //return bitmap;
        return RotateBitmap(bitmap, -90);
    }

    // IMAGEM GIRADA 90º
    public @NonNull
    Bitmap createBitmap(@NonNull View view) {
        if (view.getLayoutParams().width > 0 && view.getLayoutParams().height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(view.getLayoutParams().width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(view.getLayoutParams().height), View.MeasureSpec.EXACTLY));
        }
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //float degrees = 45; //rotation degree
        //Matrix matrix = new Matrix();
        //matrix.setRotate(degrees);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();

        if (background != null) {
            background.draw(canvas);
        }
        view.draw(canvas);

        //return bitmap;
        return RotateBitmap(bitmap, -90);
    }

    public Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
