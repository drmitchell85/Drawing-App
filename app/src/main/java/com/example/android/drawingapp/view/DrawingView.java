package com.example.android.drawingapp.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class DrawingView extends View {

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Remember to call needed variables in constructor
        init();
    }

    void init() {
        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        // If not defined, defaults to fill and stroke
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(7);
        // Can make line end more square or round
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        // Set up our paths
        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();
    }

    /**
     * Initialize our bitmap
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Size and behavior
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        // Create canvas
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Create a bitmap on our screen to drawn on
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        // For loop to go through path map
        for (Integer key : pathMap.keySet()) {
            // Takes in the path and the paint
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Even type we are receiving
        int action = event.getActionMasked();
        // What is being used to touch. The pointer - (finger, mouse, etc)
        int actionIndex = event.getActionIndex();

        // gets coordinates of where the user has tapped
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_UP) {

            touchStarted(event.getX(actionIndex),
                    event.getY(actionIndex),
                    event.getPointerId(actionIndex));


        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {

            touchEnded(event.getPointerId(actionIndex));

        } else {
            touchMoved(event);
        }
        // Redraw this screen so the user can see things happening
        invalidate();

        return true;
    }

    /**
     * Tell us where the user is tracing
     */
    private void touchMoved(MotionEvent event) {

        for (int i = 0; i < event.getPointerCount(); i++) {

            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId); // create actual index

            if (pathMap.containsKey(pointerId)) {
                // then we need new coordinates bc user finger is now moving
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                // Now, calculate how far the user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                // If distance is significant enough to be considered movement, then
                if (deltaX >= TOUCH_TOLERANCE ||
                        deltaY >= TOUCH_TOLERANCE) {
                    // move the path to the new location
                    path.quadTo(point.x, point.y,
                            (newX + point.x) / 2,
                            (newY + point.y) / 2);

                    // store the new coordinates
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    /**
     * Erase screen and saved points
     */
    public void clear() {
        pathMap.clear(); // remove all of the previous paths
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate(); // refresh the screen
    }

    private void touchEnded(int pointerId) {
        Path path = pathMap.get(pointerId); // get the corresponding path
        bitmapCanvas.drawPath(path, paintLine); // draw to bitmapCanvas
        path.reset();
    }

    /**
     * Tell us if the motion has started
     */
    private void touchStarted(float x, float y, int pointerId) {
        // Create path to see actual line
        Path path; // store the path for the given touch
        Point point; // store the last point in path

        if (pathMap.containsKey(pointerId)) {
            path = pathMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            // we know this is empty, and therefore a new touch
            path = new Path();
            pathMap.put(pointerId, path);
            point = new Point();
            previousPointMap.put(pointerId, point);
        }

        // move to the coordinates
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }


    public void saveToInternalStorage() {
        ContextWrapper cw = new ContextWrapper(getContext());
        String fileName = "Draw" + System.currentTimeMillis();
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
                Toast message = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
            }
        }
        // return directory.getAbsolutePath();
    }

    public void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            // ImageView img = (ImageView) findViewById(R.id.imgPicker);
            // img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
