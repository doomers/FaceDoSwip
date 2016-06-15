package in.doomers.facedoswip;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private static int RESULT_LOAD_IMAGE = 1;
     private Bitmap mbitmap;
    private SparseArray<Face> mFaces;
    Bitmap facebitmap;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


          Button mButton = (Button)findViewById(R.id.button);
          mButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent i = new Intent(
                          Intent.ACTION_PICK,
                          android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                  startActivityForResult(i, RESULT_LOAD_IMAGE);
              }
          });


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


            mbitmap=BitmapFactory.decodeFile(picturePath);
            ImageView imageView =(ImageView)findViewById(R.id.imageView);
            imageView.setImageBitmap(mbitmap);
            setDetector();
            drawFaceBox();


        }


    }
    public void setDetector(){
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();


        Detector<Face> safeDetector = new SafeFaceDetector(detector);

        if (!safeDetector.isOperational()) {
            Toast.makeText(this, "Detector are having issues", Toast.LENGTH_LONG).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(mbitmap).build();
            mFaces = safeDetector.detect(frame);
            safeDetector.release();
        }

    }
    public void drawFaceBox(){

            //This should be defined as a member variable rather than
            //being created on each onDraw request, but left here for
            //emphasis.



            float left = 0;
            float top = 0;
            float right = 0;
            float bottom = 0;

            for( int i = 0; i < mFaces.size(); i++ ) {
                Face face = mFaces.valueAt(i);

                left =  ( face.getPosition().x  );
                top =  ( face.getPosition().y );
                right = ( face.getPosition().x + face.getWidth() );
                bottom = ( face.getPosition().y + face.getHeight() );



                try {
                    facebitmap = Bitmap.createBitmap(mbitmap,(int)face.getPosition().x, (int)face.getPosition().y, (int)face.getWidth(), (int)face.getHeight());
                    facebitmap = Bitmap.createScaledBitmap(facebitmap,(int)(face.getWidth()*0.5),(int)(face.getHeight()*0.5),false);
                }catch (IllegalArgumentException e){

                }

                //this code would store images into externals storage
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/req_images");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);
                Log.i("image", "" + file);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    facebitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Toast.makeText(this,"Image Saved",Toast.LENGTH_LONG).show();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        Intent i = new Intent(MainActivity.this,SwipeActivity.class);
        startActivity(i);

    }
}

