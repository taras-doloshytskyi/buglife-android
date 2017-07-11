/*
 * Copyright (C) 2017 Buglife, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.buglife.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.TypedValue;

final class LoupeRenderer extends AnnotationRenderer {

    private static final int MAGNIFICATION_FACTOR = 2;

    private Bitmap mSourceBitmap;
    private final Matrix mMatrix = new Matrix();
    private final Paint mBorderPaint = new Paint();

    LoupeRenderer(float strokeWidth) {
        this(null, strokeWidth);
    }

    LoupeRenderer(Bitmap sourceBitmap, float strokeWidth) {
        mSourceBitmap = sourceBitmap;
        mBorderPaint.setStrokeWidth(strokeWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.BLACK);
    }

    void setSourceBitmap(@NonNull Bitmap bitmap) {
        mSourceBitmap = bitmap;
    }

    void drawAnnotation(Annotation annotation, Canvas canvas) {
        canvas.save();

        final float canvasWidth = canvas.getWidth();
        final float canvasHeight = canvas.getHeight();
        float radius = getLength(annotation, canvasWidth, canvasHeight);
        PointF center = getPointFromPercentPoint(annotation.getStartPercentPoint(), canvasWidth, canvasHeight);
        Path loupePath = new Path();
        loupePath.addCircle(center.x, center.y, radius, Path.Direction.CW);
        canvas.clipPath(loupePath);

        mMatrix.reset();

        // Scale the original bitmap up to the size of the canvas
        float scaleX = canvasWidth / mSourceBitmap.getWidth();
        float scaleY = canvasHeight / mSourceBitmap.getHeight();
        mMatrix.preScale(scaleX, scaleY);

        // Loupe magnification scale
        mMatrix.preScale(MAGNIFICATION_FACTOR, MAGNIFICATION_FACTOR);

        float px = center.x;
        float py = center.y;
        mMatrix.postTranslate(-px * (MAGNIFICATION_FACTOR - 1), -py * (MAGNIFICATION_FACTOR - 1));

        // Draw loupe contents
        canvas.drawBitmap(mSourceBitmap, mMatrix, null);
        // Draw loupe border
        canvas.drawCircle(center.x, center.y, radius, mBorderPaint);
        canvas.restore();
    }

    void test(Annotation annotation, Canvas canvas) {
        final float canvasWidth = canvas.getWidth();
        final float canvasHeight = canvas.getHeight();
        float radius = getLength(annotation, canvasWidth, canvasHeight);
        PointF center = getPointFromPercentPoint(annotation.getStartPercentPoint(), canvasWidth, canvasHeight);

        canvas.save();

        mMatrix.reset();
        float scaleX = canvasWidth / mSourceBitmap.getWidth();
        float scaleY = canvasHeight / mSourceBitmap.getHeight();
        mMatrix.preScale(scaleX, scaleY);

        canvas.drawBitmap(mSourceBitmap, mMatrix, null);
        // Draw loupe border
        canvas.drawCircle(center.x, center.y, radius, mBorderPaint);
        canvas.restore();
    }

    static float getStrokeWidth(Context context) {
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        return strokeWidth;
    }
}