package com.example.android.drawingapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.android.drawingapp.view.DrawingView;

public class MainActivity extends AppCompatActivity {

    // Needed to instantiate DrawingView to call methods
    private DrawingView drawingView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView widthImageView;
    private AlertDialog dialogLineWidth;
    private AlertDialog dialogColor;
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate drawingView
        drawingView = findViewById(R.id.view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // add menu option items and subsequent actions to take
        switch (item.getItemId()) {
            case R.id.id_clear:
                drawingView.clear();
                break;

            case R.id.id_save:
                drawingView.saveToInternalStorage();
                break;

            case R.id.id_color:
                showColorDialog();
                break;

            case R.id.id_line_width:
                showLineWidthDialog();
                break;

            case R.id.id_erase:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void showColorDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);
        alphaSeekBar = view.findViewById(R.id.id_alphaSeekBar);
        redSeekBar = view.findViewById(R.id.id_redSeekBar);
        greenSeekBar = view.findViewById(R.id.id_greenSeekBar);
        blueSeekBar = view.findViewById(R.id.id_blueSeekBar);
        colorView = view.findViewById(R.id.id_view_color);

        // register SeekBar event Listeners using sake click listener
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChange);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChange);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChange);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChange);

        // get previous color values and show user what they previously selected
        int color = drawingView.getDrawingColor();
        // where int color is the color that the user has selected to use
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.id_button_set_color);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setDrawingColor(Color.argb(
                        alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(),
                        blueSeekBar.getProgress()
                ));

                dialogColor.dismiss();
            }
        });

        currentAlertDialog.setView(view);
        currentAlertDialog.setTitle("Choose Color");
        dialogColor = currentAlertDialog.create();
        dialogColor.show();
    }

    // TODO: what is the root and why is it null here?
    void showLineWidthDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);

        // View to be inflated and then passed through at end to see the dialog box
        View view = getLayoutInflater().inflate(R.layout.width_dialog, null);

        final SeekBar widthSeekBar = view.findViewById(R.id.id_width_seekbar);
        Button setLineWidthButton = view.findViewById(R.id.id_width_dialog_button);
        widthImageView = view.findViewById(R.id.id_image_view);

        // get previous width value and show user wha they previously selected
        widthSeekBar.setProgress(drawingView.getLineWidth());

        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get progress of line width from the SeekBar view, and then pass that
                // value into the .setLineWidth method from the DrawingView class instance
                drawingView.setLineWidth(widthSeekBar.getProgress());
                dialogLineWidth.dismiss();
                currentAlertDialog = null;
            }
        });

        // retrieve values of seekbar
        // also, create a line that shows the width for the user to see
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);

        // We must use .setView() and pass the view create to actually see the dialog box
        currentAlertDialog.setView(view);
        dialogLineWidth = currentAlertDialog.create();
        dialogLineWidth.setTitle("Set Line Width");
        dialogLineWidth.show();
    }

    private SeekBar.OnSeekBarChangeListener colorSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // .argb() has four parameters - int is for the alpha parameter passed
            // by alphaSeekBar.getProgress(); rgb comes from their respective SeekBars
            drawingView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));

            // display current color
            colorView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener widthSeekBarChange = new SeekBar.OnSeekBarChangeListener() {

        // Create a line for the user to see in order to visualize the brush width
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // Configure a paint object for the current seekbar
            Paint p = new Paint();
            p.setColor(drawingView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            widthImageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
