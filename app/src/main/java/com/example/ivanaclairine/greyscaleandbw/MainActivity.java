package com.example.ivanaclairine.greyscaleandbw;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity {

    private static int RESULT_LOAD_IMAGE = 1;
    private ImageButton buttonLoadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLoadImage = (ImageButton) findViewById(R.id.buttonLoadPictureImage);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            Log.d("PicturePath: ", picturePath);
            final Bitmap originalBm = BitmapFactory.decodeFile(picturePath);
            int[] pix = new int[originalBm.getWidth() * originalBm.getHeight()];
            originalBm.getPixels(pix, 0, originalBm.getWidth(), 0, 0, originalBm.getWidth(), originalBm.getHeight());


            //hilangkan load button
            buttonLoadImage.setVisibility(View.INVISIBLE);

            //ambil komponen
            final SeekBar seekBarBWValue = (SeekBar) findViewById(R.id.SeekBarBWValue);
            final TextView BWValue = (TextView)findViewById(R.id.BWValue);

            final Bitmap bmTemp = Bitmap.createScaledBitmap(originalBm, 600, 800, true);

            final ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(bmTemp);

            Set<Integer> colors = new HashSet<>();
            int w = bmTemp.getWidth();
            int h = bmTemp.getHeight();
            for(int y = 0; y < h; y++) {
                for(int x = 0; x < w; x++) {
                    int pixel = bmTemp.getPixel(x,y);
                        colors.add(pixel);
                }
            }

            final TextView txt = (TextView) findViewById(R.id.numbOfColors);
            txt.setText("Jumlah Warna: " + String.valueOf(colors.size()));

            final Button buttonGreyscale = (Button) findViewById(R.id.buttonGreyscale);
            buttonGreyscale.setText("Grayscale");
            buttonGreyscale.setVisibility(View.VISIBLE);
            buttonGreyscale.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Bitmap bitmapGrayScale = Bitmap.createScaledBitmap(originalBm, 600, 800, true);
                    bitmapGrayScale = toGrayscaleRGB(bitmapGrayScale);
                    imageView.setImageBitmap(bitmapGrayScale);
                    txt.setVisibility(View.INVISIBLE);

                    Button revertButton = (Button) findViewById(R.id.revertButton);
                    revertButton.setVisibility(View.VISIBLE);
                    revertButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View arg0){
                            imageView.setImageBitmap(bmTemp);
                            txt.setVisibility(View.VISIBLE);
                            seekBarBWValue.setVisibility(View.INVISIBLE);
                            BWValue.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });

            Button buttonBW = (Button) findViewById(R.id.buttonBW);
            buttonBW.setText("Black & White");
            buttonBW.setVisibility(View.VISIBLE);
            buttonBW.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    buttonGreyscale.setVisibility(View.INVISIBLE);
                    txt.setVisibility(View.INVISIBLE);

                    //buat button undo
                    Button revertButton = (Button) findViewById(R.id.revertButton);
                    revertButton.setVisibility(View.VISIBLE);
                    revertButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View arg0){
                            imageView.setImageBitmap(bmTemp);
                            txt.setVisibility(View.VISIBLE);
                            seekBarBWValue.setVisibility(View.INVISIBLE);
                            BWValue.setVisibility(View.INVISIBLE);
                            buttonGreyscale.setVisibility(View.VISIBLE);
                        }
                    });

                    //tampilkan SeekBar
                    seekBarBWValue.setVisibility(View.VISIBLE);

                    seekBarBWValue.setProgress(0);
                    seekBarBWValue.incrementProgressBy(1);
                    seekBarBWValue.setMax(256);


                    BWValue.setVisibility(View.VISIBLE);
                    BWValue.setText("Threshold: " + seekBarBWValue.getProgress());
                    seekBarBWValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        int progress = 0;

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                            progress = progressValue;
                            BWValue.setText("Threshold: " + progress);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                           //gets current value of SeekBar
                            int progress = seekBarBWValue.getProgress();
                            Bitmap bitmapGrayScale = Bitmap.createScaledBitmap(originalBm, 600, 800, true);
                            bitmapGrayScale = toBlackAndWhiteCustom(bitmapGrayScale, progress);
                            imageView.setImageBitmap(bitmapGrayScale);
                            txt.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            });
        }
    }

    //Grayscale pake Colormatrix
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    //grayscale pake rata rata RGB
    public Bitmap toGrayscaleRGB(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        int[] pix = new int[width * height];
        bmpOriginal.getPixels(pix, 0, width, 0, 0, width, height);

        int R, G, B,Y;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++)
            {
                int index = y * width + x;
                int Red = (pix[index] >> 16) & 0xff;     //bitwise shifting
                int Green = (pix[index] >> 8) & 0xff;
                int Blue = pix[index] & 0xff;
//                Log.d("Pixel: ", String.valueOf(pix[index]));
//                Log.d("Red: " , String.valueOf(Red));
//                Log.d("Green: " , String.valueOf(Green));
//                Log.d("Blue: " , String.valueOf(Blue));

                int average = (Red + Green + Blue) / 3;
                //R,G.B - Red, Green, Blue
                //to restore the values after RGB modification, use
                //next statement
                pix[index] = 0xff000000 | (average << 16) | (average << 8) | average;
            }}

        Bitmap bmpGrayscale = Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888);
        return bmpGrayscale;
    }

    //Buat jadi black and White pake rata rata RGB
    public Bitmap toBlackAndWhite(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        int[] pix = new int[width * height];
        bmpOriginal.getPixels(pix, 0, width, 0, 0, width, height);

        int R, G, B,Y;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++)
            {
                int index = y * width + x;
                int Red = (pix[index] >> 16) & 0xff;     //bitwise shifting
                int Green = (pix[index] >> 8) & 0xff;
                int Blue = pix[index] & 0xff;
//                Log.d("Pixel: ", String.valueOf(pix[index]));
//                Log.d("Red: " , String.valueOf(Red));
//                Log.d("Green: " , String.valueOf(Green));
//                Log.d("Blue: " , String.valueOf(Blue));

                int average = (Red + Green + Blue) / 3;
                int color = 255;
                if(average < 128)
                    color = 1;
                //R,G.B - Red, Green, Blue
                //to restore the values after RGB modification, use
                //next statement
                pix[index] = 0xff000000 | (color << 16) | (color << 8) | color;
            }}

        Bitmap bmpGrayscale = Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888);
        return bmpGrayscale;
    }

    //Buat jadi black and White pake rata rata RGB
    public Bitmap toBlackAndWhiteCustom(Bitmap bmpOriginal, int threshold)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        int[] pix = new int[width * height];
        bmpOriginal.getPixels(pix, 0, width, 0, 0, width, height);

        int R, G, B,Y;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++)
            {
                int index = y * width + x;
                int Red = (pix[index] >> 16) & 0xff;     //bitwise shifting
                int Green = (pix[index] >> 8) & 0xff;
                int Blue = pix[index] & 0xff;
//                Log.d("Pixel: ", String.valueOf(pix[index]));
//                Log.d("Red: " , String.valueOf(Red));
//                Log.d("Green: " , String.valueOf(Green));
//                Log.d("Blue: " , String.valueOf(Blue));

                int average = (Red + Green + Blue) / 3;
                int color = 255;
                if(average < threshold)
                    color = 1;
                //R,G.B - Red, Green, Blue
                //to restore the values after RGB modification, use
                //next statement
                pix[index] = 0xff000000 | (color << 16) | (color << 8) | color;
            }}

        Bitmap bmpGrayscale = Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888);
        return bmpGrayscale;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
