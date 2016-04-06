package com.crone.yandexmobdev.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;


// стандарный centerCrop от picasso не правильно размещал картинку, вырезая верхнею часть,
// для правильного размещения создана трансформация Bitmap под размеры ImageView
public class ImageTransformation {
    public static Transformation getTransformation(final int width, final int height) {
        return new Transformation() {

            @Override public Bitmap transform(Bitmap source) {
                Bitmap background = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.RGB_565);
                float originalWidth = source.getWidth(), originalHeight = source.getHeight();
                Canvas canvas = new Canvas(background);
                float scale = width/originalWidth;
                float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale)/15.0f;
                Matrix transformation = new Matrix();
                transformation.postTranslate(xTranslation, yTranslation);
                transformation.preScale(scale, scale);
                Paint paint = new Paint();
                paint.setFilterBitmap(true);
                canvas.drawBitmap(source, transformation, paint);

                if (background != source) {
                    source.recycle();
                }
                return background;
            }
            @Override public String key() { return "square()"; }
        };
    }
}
