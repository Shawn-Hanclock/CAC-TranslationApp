package com.example.cactranslationapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
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

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //UI Views
    private MaterialButton inputImageBtn, recognizeTextBtn;
    private ShapeableImageView imageView;
    private EditText recognizedEdText;

    //TAG
    private static final String TAG = "MAIN_TAG";
    //Uri of imaage taken from camera/gallery
    private Uri imageUri = null;

    //handles the result of camera/gallery permissions
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    //array of permission required to pick image from Camera, Gallery
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //progress dialog
    private ProgressDialog progressDialog;

    //TextRecognizer
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init UI Views
        inputImageBtn = findViewById(R.id.inputImg_Btn);
        recognizeTextBtn = findViewById(R.id.recognize_Btn);
        imageView = findViewById(R.id.image_imgV);
        recognizedEdText = findViewById(R.id.recognizedText_editT);

        //init arrays of permission required for camera, gallery
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
                showInputImageDialog();
            }
        });

        //handle click, start recognizing text from image took from camera/gallery
        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if image is picked or not, picked if imageUri not null
                if (imageUri == null) {
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
        Log.d(TAG, "recognizedTextFromImage: ");
        //set message show progress dialog
        progressDialog.setMessage("Preparing iamge....");
        progressDialog.show();

        try {
            //prepare inputimage from image uri
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
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
                            Log.d(TAG, "OnSuccess: recognizedText:"+recognizedText);
                            //set the recognized text to edit text
                            recognizedEdText.setText(recognizedText);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed recognizing text from image, dismiss dialog, show reason in toast
                            progressDialog.dismiss();
                            Log.e(TAG,"onFailure: ", e);
                            Toast.makeText(MainActivity.this, "Failed recognizing text due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            //Exception occured while preparing inputimage, dismiss dialog, show reason in toast
            progressDialog.dismiss();
            Log.e(TAG, "recognizeTextFromImage: ", e);
            Toast.makeText(this, "Failed preparing image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputImageBtn);

        //add items camera, gallery to popup menu
        popupMenu.getMenu().add(Menu.NONE, 1,1, "CAMERA");
        popupMenu.getMenu().add(Menu.NONE, 2,2, "GALLERY");

        //show popupmenu
        popupMenu.show();

        //handle popupmenu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get item id that is clicked from popupmenu
                int id = item.getItemId();
                if (id == 1) {
                    //camera is click, check if camera permissions are granted or not
                    Log.d(TAG, "onMenuItemCLick: Camera Clicked...");
                    if (checkCameraPermissions())
                    {
                        //camera permissions granted, we can launch camera intent
                        pickImageCamera();
                    }
                    else {
                        //camera permissions not granted, request the camera permissions
                        requestCameraPermissions();
                    }
                }
                else if (id == 2) {
                    //gallery is clicked, check if storage permission is granted or not
                    Log.d(TAG, "onMenuItemClick: Gallery Clicked");
                    if (checkStoragePermission()) {
                        //storage permission granted, we can launch the gallery intent
                        pickImageGallery();
                    }
                    else {
                        //storage permission not granted, request permissions
                        requestStoragePermission();
                    }
                }
                return true;
            }
        });
    }

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //here the image is received
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //image picked
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri "+imageUri);
                        //set to imageView
                        imageView.setImageURI(imageUri);
                    }
                    else{
                        Log.d(TAG, "onActivityResult: cancelled");
                        //imaged cancelled
                        Toast.makeText(MainActivity.this, "Cancelled..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //image is recieved if taken from camera
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        //image taken from camera
                        //image in imageuri from using pickImageCamera()
                        Log.d(TAG, "onActivityResult: imageUri "+imageUri);
                        imageView.setImageURI(imageUri);
                    }
                    else{
                        //cancelled
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(MainActivity.this, "Cancelled..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission() {
        //check if storage permission is allowed
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        //check if camera and storage permission are allowed
        boolean cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return cameraResult && storageResult;
    }

    private void requestCameraPermissions() {
        //request camera permissions
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                //check if some action from permission dialog performed
                if(grantResults.length > 0){
                    //check if camera, storage permissions granted, contains boolean results either true or false
                    boolean cameraAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    //check if both permissions are granted or not
                    if (cameraAccepted && storageAccepted){
                        //both permissions(camera & gallery are granted, we can launch camera intent
                        pickImageCamera();
                    }
                    else{
                        //one or both permissions are denied, can't launch camera intent
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    //neither allowed not denied, rather cancelled
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                //check if some action from permission dialog performed or not allow/deny
                if (grantResults.length > 0){
                    //check if storage permissions granted, contains boolean results either true or false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //check if storage permission is granted or not
                    if (storageAccepted){
                        //storage permission granted, we can launch gallery intent
                        pickImageGallery();
                    }
                    else {
                        //storage permission denied, can't launch gallery intent
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}