package net.kq.kqcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class KQCanvas extends View {
    protected Paint paint, line, eraser;
    protected List<Drawable> lines = new ArrayList<Drawable>();
    protected final int MAX_ARGB = 255, MAX_WIDTH = 80, STANDARD_STICKER_SIZE = 500;
    protected boolean paintMode = true, lineMode = false, eraserMode = false, stickerAdd = false,
            stickerFirstTouchFlag = true;
    protected Paint bitmapPaint, bitmapEraser;
    protected Canvas canvas;
    protected Bitmap bitmap;
    protected Context context;
    protected float paintX, paintY;
    protected float eraserX, eraserY;
    private final float TOUCH_TOLERANCE = 4;
    protected Line curLine;
    protected KeworkerPath curPath;
    protected Sticker curSticker;
    protected short stickNumb;
    private String commands;
    private Bitmap fromServer;
    private boolean frozen;

    public KQCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        frozen = false;
        this.context = context;
        commands = "";
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        bitmapEraser = new Paint(Paint.DITHER_FLAG);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        line = new Paint();
        eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setDither(true);
        eraser.setStyle(Paint.Style.STROKE);
        eraser.setStrokeJoin(Paint.Join.ROUND);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setARGB(MAX_ARGB, MAX_ARGB, MAX_ARGB, MAX_ARGB);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        this.canvas.drawColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).onDraw(canvas);
            lines.get(i).onDraw(this.canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (true) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    onActionDown(event.getX(), event.getY());
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    onActionPointerDown(event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1));
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (paintMode) {
                        onPaintActionMove(event.getX(), event.getY());
                    }
                    else if (lineMode) {
                        curLine.setEndXY(event.getX(), event.getY());
                    }
                    else if (eraserMode) {
                        onEraserActionMove(event.getX(), event.getY());
                    }
                    else if (stickerAdd) {
                        if (event.getPointerCount() == 1) {
                            curSticker.resetXY(event.getX(), event.getY());
                        }
                        else {
//                            curSticker.resetWH(pi(stickerX1, stickerY1, stickerX2, stickerY2),
//                                    pi(event.getX(0), event.getY(0),
//                                            event.getX(1), event.getY(1)));
                        }
                    }
                    invalidate();
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    if (paintMode) {
                        onPaintActionUp();
                    }
                    else if (lineMode) {
                        curLine.setEndXY(event.getX(), event.getY());
                    }
                    else if (eraserMode) {
                        onEraserActionUp();
                    }
                    invalidate();
                    break;
                }
            }
        }
        return true;
    }

    private void onActionDown(float x, float y) {
        if (paintMode) {
            onPaintActionDown(x, y);
            curPath = (KeworkerPath) lines.get(lines.size() - 1);
            invalidate();
        }
        else if (lineMode) {
            lines.add(new Line(x, y));
            curLine = (Line) lines.get(lines.size() - 1);
            curLine.setColorAndWidth(paint.getColor(), paint.getStrokeWidth());
        }
        else if (eraserMode) {
            onEraserActionDown(x, y);
            curPath = (KeworkerPath) lines.get(lines.size() - 1);
            invalidate();
        }
    }

    private void onActionPointerDown(float x0, float y0, float x1, float y1) {
        if (stickerAdd) {
        }
    }



    private void onPaintActionDown(float x, float y) {
        Path path = new Path();
        path.reset();
        path.moveTo(x, y);
        lines.add(new KeworkerPath(paint, path, bitmap, bitmapPaint));
        paintX = x;
        paintY = y;
    }

    private void onPaintActionMove(float x, float y) {
        float dx = Math.abs(x - paintX);
        float dy = Math.abs(y - paintY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            curPath.path.quadTo(paintX, paintY, (x + paintX)/2, (y + paintY)/2);
            paintX = x;
            paintY = y;
        }
    }

    private void onPaintActionUp() {
        curPath.path.lineTo(paintX, paintY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
    }

    private void onEraserActionDown(float x, float y) {
        Path path = new Path();
        path.reset();
        path.moveTo(x, y);
        lines.add(new KeworkerPath(eraser, path, bitmap, bitmapEraser));
        eraserX = x;
        eraserY = y;
    }

    private void onEraserActionMove(float x, float y) {
        float dx = Math.abs(x - eraserX);
        float dy = Math.abs(y - eraserY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            curPath.path.quadTo(eraserX, eraserY,
                    (x + eraserX) / 2, (y + eraserY) / 2);
            eraserX = x;
            eraserY = y;
        }
    }

    private void onEraserActionUp() {
        curPath.path.lineTo(eraserX, eraserY);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
    }

    public List<Drawable> getLines() {
        return lines;
    }

    public void setLines(List<Drawable> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaintARGB(int a, int r, int g, int b) /*throws KeworkerException*/ {
        if (a >= 0 && a <= MAX_ARGB && r >= 0 && r <= MAX_ARGB
                && g >= 0 && g <= MAX_ARGB && b >= 0 && b <= MAX_ARGB) {
            paint.setARGB(a, r, g, b);
            line.setARGB(a, r, g, b);
        }
        else {
//            throw new KeworkerException("You cant set this alpha " + a +
//                    ", red " + r + ", green" + g + ", blue" + b + " params");
        }
    }

    public Paint getEraser() {
        return eraser;
    }

    public void setWidth(float width) /*throws KeworkerException*/ {
        if (width * MAX_WIDTH / 100 > 0
                && width * MAX_WIDTH / 100 <= MAX_WIDTH) {
            paint.setStrokeWidth(width * MAX_WIDTH / 100);
            line.setStrokeWidth(width * MAX_WIDTH / 100);
            eraser.setStrokeWidth(width * MAX_WIDTH / 100);
        }
        else {
//            throw new KeworkerException("You cant set this width " + width + " params");
        }
    }

    public void back() {
        if (lines.size() > 0) {
            lines.remove(lines.size() - 1);
            invalidate();
            //            commands += "\\b";
        }
    }

    public void setPaintMode() {
        paintMode = true;
        lineMode = false;
        eraserMode = false;
        stickerAdd = false;
        //        commands += "\\pm";
    }

    public void setLineMode() {
        paintMode = false;
        lineMode = true;
        eraserMode = false;
        stickerAdd = false;
        //        commands += "\\lm";
    }

    public void setEraserMode() {
        paintMode = false;
        lineMode = false;
        eraserMode = true;
        stickerAdd = false;
        //        commands += "\\em";
    }

    public void setStickerMode(short stickNumb) /*throws KeworkerException*/ {
        if (stickNumb < 1 || stickNumb > 6) {
//            throw new KeworkerException("We haven't sticker with number " +
//                    stickNumb + ", I'm sorry.");
        }
        else {
            this.stickNumb = (short) (stickNumb - 1);
            stickerFirstTouchFlag = true;
            stickerAdd = true;
            paintMode = false;
            lineMode = false;
            eraserMode = false;
            //            commands += "\\sm/" + stickNumb;
        }
    }

    public double pi(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2));
    }

    public void setBitmap(int pixels[]) {
        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, canvas.getWidth(), 0, 0,
                canvas.getWidth(), canvas.getHeight());
        fromServer = bitmap;
    }

    public  Bitmap getBitmapFromServ() {
        return fromServer;
    }

    public void drawBitmap() {
        lines = new ArrayList<Drawable>();
        invalidate();
    }

    public void reset() {
        lines = new ArrayList<Drawable>();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public class Line implements Drawable {
        private float x1, y1, x2, y2;
        private Paint paint;

        public Line(float x1, float y1) {
            this.x1 = x1;
            this.y1 = y1;
            paint = new Paint();
        }

        public Line(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            paint = new Paint();
        }

        public void setEndXY(float x, float y) {
            x2 = x;
            y2 = y;
        }

        public void setColorAndWidth(int color, float width) {
            paint.setColor(color);
            paint.setStrokeWidth(width);
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

    public class KeworkerPath implements Drawable, Cloneable {
        private Paint bitmapPaint;
        private Bitmap bitmap;
        private Paint paint;
        private Path path;

        public KeworkerPath(Paint paint, Path path, Bitmap bitmap, Paint bitmapPaint) {
            this.paint = new Paint();
            this.paint.setColor(paint.getColor());
            this.paint.setAntiAlias(true);
            this.paint.setDither(true);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeJoin(Paint.Join.ROUND);
            this.paint.setStrokeCap(Paint.Cap.ROUND);
            this.paint.setStrokeWidth(paint.getStrokeWidth());
            this.path = path;
            this.bitmap = bitmap;
            this.bitmapPaint = new Paint();
            this.bitmapPaint.setColor(paint.getColor());
            this.bitmapPaint.setStrokeWidth(paint.getStrokeWidth());
            this.bitmapPaint.setAntiAlias(true);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
            canvas.drawPath(this.path, paint);
        }
    }

    public class Sticker implements Drawable {
        private Bitmap bitmap;
        private float x, y;
        private Paint paint;
        private int id;
        private int width, height;

        public Sticker(int id, int width, int height,
                       float x, float y) {
            paint = new Paint();
            this.id = id;
            bitmap = BitmapFactory.decodeResource(getResources(), id);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            this.x = x;
            this.y = y;
        }

        public void resetWH(double s1, double s2) {
            int wh = (int) Math.floor((s2 * bitmap.getWidth()) / s1);
            if (wh > 99 && wh < 2001) {
                bitmap = BitmapFactory.decodeResource(getResources(), id);
                bitmap = Bitmap.createScaledBitmap(bitmap, wh, wh, true);
            }
        }
        public void resetXY(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(bitmap, (x - (bitmap.getWidth() / 2f)),
                    (y - bitmap.getHeight() / 2f), paint);
        }
    }

    public interface Drawable {
        void onDraw(Canvas canvas);
    }
}