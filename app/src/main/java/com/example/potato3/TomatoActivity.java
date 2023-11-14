package com.example.potato3;

import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Date;
import android.content.Intent;
import android.graphics.Bitmap;
import android.content.pm.PackageManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.content.ClipData;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import java.io.ByteArrayOutputStream;
import android.graphics.BitmapFactory;
import com.example.potato3.ml.Tomato;


import java.util.ArrayList;
import java.util.List;
import android.Manifest;
public class TomatoActivity extends AppCompatActivity {

    Button clear, submit;
    int imageSize=256;
    TextView result;
    ImageSwitcher imageView;
    int PICK_IMAGE_MULTIPLE = 1;
    TextView total, imageCountText;
    ArrayList<Uri> mArrayUri;
    private List<String> modelOutputs = new ArrayList<>();

    int position = 0;
    private final int CAMERA_REQ_CODE = 100;
    private final int targetWidth = 600; // Adjust this width as needed
    private final int targetHeight = 800; // Adjust this height as needed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato);
        imageCountText = findViewById(R.id.imageCountText);
        total = findViewById(R.id.text);
        imageView = findViewById(R.id.image);
        clear = findViewById(R.id.clear);
        result= findViewById(R.id.result);
        submit =findViewById(R.id.submit);
        mArrayUri = new ArrayList<Uri>();
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        ImageButton previousButton = findViewById(R.id.previousButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        // showing all images in imageswitcher
        imageView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView1 = new ImageView(getApplicationContext());
                return imageView1;
            }
        });

        // click here to select next image
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < mArrayUri.size() - 1) {
                    // increase the position by 1
                    position++;
                    Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(position)), 700, 800);
                    Uri imageTempURI=getImageUri(resizedBitmap);
                    imageView.setImageURI(imageTempURI);


                    // Update the TextView with the current image number and total count
                    updateImageCountText();
                } else {
                    Toast.makeText(TomatoActivity.this, "Last Image Already Shown!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // click here to view previous image
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    // decrease the position by 1
                    position--;
                    Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(position)), 700, 800);
                    Uri imageTempURI=getImageUri(resizedBitmap);
                    imageView.setImageURI(imageTempURI);
                    // Update the TextView with the current image number and total count
                    updateImageCountText();
                }else {
                    Toast.makeText(TomatoActivity.this, "This is the First Image!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // click here to select images from gallery
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialising intent
                Intent intent = new Intent();
                // setting type to select to be image
                intent.setType("image/*");
                // allowing multiple image to be selected
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
        // click here to capture image from camera
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                    Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(iCamera, CAMERA_REQ_CODE);}
                else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA},1000);
                }
            }
        });


        // click here to clear the selected image
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayUri.size() > 0) {
                    mArrayUri.remove(position);
                    if(modelOutputs.size()>0){
                        modelOutputs.remove(position);}
                    if (position > 0) {
                        position--;
                    }
                    if (mArrayUri.size() > 0) {
                        Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(position)), 700, 800);
                        Uri imageTempURI=getImageUri(resizedBitmap);
                        imageView.setImageURI(imageTempURI);
                        // Update the TextView with the current image number and total count
                        updateImageCountText();
                    } else {
                        imageView.setImageResource(0); // No images left, clear the ImageSwitcher.
                        // Update the TextView with the current image number and total count
                        updateImageCountText();
                    }
                } else {
                    Toast.makeText(TomatoActivity.this, "No images to clear!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i=0;i<mArrayUri.size();i++){
                    Bitmap imag = uriToBitmap(mArrayUri.get(i));
                    int dimension = Math.min(imag.getWidth(), imag.getHeight());
                    imag = ThumbnailUtils.extractThumbnail(imag, dimension, dimension);
                    // Resize the captured image to a specific width and height
                    imag = resizeBitmap(imag, imageSize, imageSize);
                    modelOutputs.add(classifyImage(imag));
                }
                Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(0)), 700, 800);
                Uri imageTempURI=getImageUri(resizedBitmap);
                imageView.setImageURI(imageTempURI);
                position = 0;
                updateImageCountText();
            }
        });

    }


// ...

    // Convert Uri to Bitmap
    public Bitmap uriToBitmap(Uri uri) {
        try {
            // Use getContentResolver to open the Uri and decode it into a Bitmap
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String classifyImage(Bitmap image){
        String output="";
        try {
            Tomato model = Tomato.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Tomato.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Bacterial Spot", "Early Blight","Late Blight","Leaf Mold","Septoria Leaf Spot","Spider Mites","Target Spot","Curl Virus","Mosaic Virus","Healthy"};
            output = classes[maxPos];

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
            e.printStackTrace();
        }
        return output;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When an Image is picked
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
            // Get the Image from data
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    // adding imageuri in array
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    mArrayUri.add(imageUri);
                }
                // setting 1st selected image into image switcher
                Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(0)), 700, 800);
                Uri imageTempURI=getImageUri(resizedBitmap);
                imageView.setImageURI(imageTempURI);
                position = 0;
                // Update the TextView with the current image number and total count
                updateImageCountText();
            } else {
                Uri imageUri = data.getData();
                mArrayUri.add(imageUri);
                Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(0)), 700, 800);
                Uri imageTempURI=getImageUri(resizedBitmap);
                imageView.setImageURI(imageTempURI);
                position = 0;
                // Update the TextView with the current image number and total count
                updateImageCountText();
            }
        } else if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap img = (Bitmap) data.getExtras().get("data");

            Uri imageUri = getImageUri(img);
            if(imageUri != null){
                mArrayUri.add(imageUri);
                Bitmap resizedBitmap = resizeBitmap(uriToBitmap(mArrayUri.get(position)), 700, 800);
                Uri imageTempURI=getImageUri(resizedBitmap);
                imageView.setImageURI(imageTempURI);
                // Update the TextView with the current image number and total count
                updateImageCountText();}
        } else {
            // show this if no image is selected or captured
            Toast.makeText(this, "No image selected or captured", Toast.LENGTH_LONG).show();
        }
    }
    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    public Uri getImageUri(Bitmap inImage) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String uniqueFileName = "IMG_" + timeStamp + "_" + new Random().nextInt(1000) + ".jpg";
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, uniqueFileName, null);
            if (path != null) {
                return Uri.parse(path);
            } else {
                // Handle the case where the path is null
                // You can log an error or return a default URI
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Add this method to update the image count TextView
    private void updateImageCountText() {
        int totalImages = mArrayUri.size(); // Replace this with the actual count of images
        int currentImageNumber = position + 1; // Current image number (1-based index)
        if(totalImages==0){
            currentImageNumber=0;
        }
        String countText = "Image " + currentImageNumber + "/" + totalImages;
        imageCountText.setText(countText);
        if (modelOutputs.size() > 0) {
            result.setText(modelOutputs.get(position));
        } else {
            result.setText("Please submit some image(s)!");
        }
    }
}
