package com.example.cactranslationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {
    //UI Views
    private MaterialButton inputImageBtn, recognizeTextBtn;
    private ShapeableImageView imageView;
    private EditText recognizedEdText;

    //Bitmap of imaage taken from camera
    private Bitmap photo = null;

    //handles request for camera permissions
    private static final int ONLY_CAM_REQUEST_CODE = 102;
    private static final int REQUEST_CODE = 201;

    //array of permission required to pick image from Camera, Gallery
    private String[] onlyCamPermission;

    //progress dialog
    private ProgressDialog progressDialog;

    //TextRecognizer from google ML kit
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //spinner dropdown menu code
        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.language_Spn);

        //populate based on language
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.language_options_Eng,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        dropdown.setAdapter(adapter);


        //init UI Views
        inputImageBtn = findViewById(R.id.inputImg_Btn);
        recognizeTextBtn = findViewById(R.id.recognize_Btn);
        imageView = findViewById(R.id.image_imgV);
        recognizedEdText = findViewById(R.id.recognizedText_editT);

        //init arrays of permission required for camera, gallery


        onlyCamPermission = new String[]{Manifest.permission.CAMERA};



        //init setup the progress dialog, show while text from image is being recognized
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init TextRecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //handle click, show input image dialog
        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPopupPermission();
                //showInputImageDialog();
            }
        });

        //handle click, start recognizing text from image took from camera/gallery
        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if image is picked or not, picked if imageUri not null
                if (photo == null) {
                    //imageUri null have not yet picked can't recognize text
                    Toast.makeText(MainActivity.this, "Pick image first..", Toast.LENGTH_SHORT).show();
                }
                else {
                    //iamgeUri is not null we have picked can recognize text
                    recognizeTextFromImage();
                }
            }
        });

    }

    private void recognizeTextFromImage() {
        //set message show progress dialog
        progressDialog.setMessage("Preparing iamge....");
        progressDialog.show();

        //prepare inputimage from image bitmap
        InputImage inputImage = InputImage.fromBitmap(photo, 0);
        //image prepared, we are about to start text recognition proces, change progress message
        progressDialog.setMessage("Recognizing text....");
        //start text recognition process from image
        Task<Text> textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        //process completed, dismiss dialog
                        progressDialog.dismiss();
                        //get the recognized text
                        String recognizedText = text.getText();
                        //set the recognized text to edit text
                        recognizedEdText.setText(recognizedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed recognizing text from image, dismiss dialog, show reason in toast
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed recognizing text due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //old code used popup with the option of gallery or camera
    private void noPopupPermission()
    {
        if(checkOnlyCamPermission())
        {
            pickImageCamera();
        }
        else {
            requestOnlyCamPermission();
        }
    }

    private void pickImageCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            //image taken from camera
            //image in imageuri from using pickImageCamera()
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
        else
        {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkOnlyCamPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestOnlyCamPermission() {
        //request camera permissions
        ActivityCompat.requestPermissions(this, onlyCamPermission, ONLY_CAM_REQUEST_CODE);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0){
                    //check if storage permissions granted, contains boolean results either true or false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //check if storage permission is granted or not
                    if (storageAccepted){
                        //storage permission granted, we can launch gallery intent
                        pickImageCamera();
                    }
                    else {
                        //storage permission denied, can't launch gallery intent
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                        requestOnlyCamPermission();
                    }
                }
    }
}