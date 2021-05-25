package com.tourassistant.coderoids.starttrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.NewsFeedModel;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReportHazard extends AppCompatActivity {
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_GALLERY_PHOTO = 2;
    private int AUTOCOMPLETE_REQUEST_CODE = 3;
    File mPhotoFile;
    ImageView ivProofImages;
    TextView tvAddImage, tvGeoPoint;
    Blob profileImageBlob = null;
    GeoPoint point;
    Button btnSignUp, btnBack;
    String[] SPINNERLIST = {"Road Trip", "Beauty", "Adventure", "Nature", "Land Slide", "Snow", "Road Block", "Bad Weather", "other"};
    TextInputEditText etName, etTripTitle, etDesctiption;
    MaterialBetterSpinner materialDesignSpinner;
    Bitmap bitmapLowRes = null;
    ConstraintLayout contentNews;
    RelativeLayout rlEmpty;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_hazard);
        if (!Places.isInitialized()) {
            Places.initialize(this, this.getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);
        ivProofImages = findViewById(R.id.iv_proof_image);
        tvAddImage = findViewById(R.id.tl_images);
        tvGeoPoint = findViewById(R.id.tl_geo_point);
        btnSignUp = findViewById(R.id.btn_sign_up);
        etName = findViewById(R.id.et_name);
        etTripTitle = findViewById(R.id.et_title_name);
        etDesctiption = findViewById(R.id.et_desctiption);
        contentNews = findViewById(R.id.content_news);
        btnBack = findViewById(R.id.go_back);
        rlEmpty = findViewById(R.id.emtyView);
        rlEmpty.setVisibility(View.GONE);
        contentNews.setVisibility(View.VISIBLE);
        tvAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        tvGeoPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLocation();
            }
        });
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, SPINNERLIST);
        materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.spinner1);
        materialDesignSpinner.setAdapter(arrayAdapter);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNews();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void uploadNews() {
        try {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            String nameUser = etName.getText().toString();
            String etDesctiptionS = etDesctiption.getText().toString();
            String etTripTitleS = etTripTitle.getText().toString();
            if (!nameUser.matches("") && !nameUser.isEmpty() && !etDesctiptionS.isEmpty() && !etTripTitleS.isEmpty() && !materialDesignSpinner.getText().toString().isEmpty()
                    && !tvGeoPoint.getText().toString().isEmpty() && profileImageBlob != null) {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                NewsFeedModel newsFeedModel = new NewsFeedModel();
                newsFeedModel.setTitle(etTripTitleS);
                newsFeedModel.setUserName(nameUser);
                newsFeedModel.setDescription(etDesctiptionS);
                newsFeedModel.setHazardType(materialDesignSpinner.getText().toString());
                newsFeedModel.setNewsThumbNail(profileImageBlob);
                newsFeedModel.setGeoPoint(point);
                newsFeedModel.setDateInMillis(System.currentTimeMillis() + "");
                newsFeedModel.setUploadedById(AppHelper.currentProfileInstance.getUserId());
                if (AppHelper.tripEntityList != null && AppHelper.tripEntityList.getFirebaseId() != null) {
                    newsFeedModel.setTripId(AppHelper.tripEntityList.getFirebaseId());
                    rootRef.collection("PublicTrips").document(AppHelper.tripEntityList.getFirebaseId()).collection("NewsFeed").document().set(newsFeedModel);
                } else
                    newsFeedModel.setTripId("");

                rootRef.collection("NewsFeed").document().set(newsFeedModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            progressDialog.dismiss();
                            rlEmpty.setVisibility(View.VISIBLE);
                            contentNews.setVisibility(View.GONE);
                        }
                    }
                });

                rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId()).collection("NewsFeed").document().set(newsFeedModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            } else {
                Toast.makeText(this, "All Fields are Mandatory", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    private void manageLocation() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
        List<String> countriesArr = new ArrayList<>();
        countriesArr.add("PK");
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(countriesArr)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void selectImage() {
        final CharSequence[] items = {
                "Take Photo", "Choose from Library",
                "Cancel"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Take Photo")) {
                requestStoragePermission(true);
            } else if (items[item].equals("Choose from Library")) {
                requestStoragePermission(false);
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    private void requestStoragePermission(boolean isCamera) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(
                        error -> Toast.makeText(this, "Error occurred! ", Toast.LENGTH_SHORT)
                                .show())
                .onSameThread()
                .check();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.tourassistant.coderoids.android.fileprovider", null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage(
                "This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.tourassistant.coderoids.android.fileprovider",
                        photoFile);
                mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                tvGeoPoint.setText(place.getAddress());
                point = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
            }
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File fdelete = mPhotoFile;
                    Uri contentUri = Uri.fromFile(fdelete);
                    mediaScanIntent.setData(contentUri);
                    Matrix matrix = new Matrix();
                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(photo, AppHelper.imageWidth, AppHelper.imageHeight, true);
                    bitmapLowRes = imageRotation(scaledBitmap, matrix);
                    ivProofImages.setImageBitmap(bitmapLowRes);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapLowRes.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    profileImageBlob = Blob.fromBytes(byteArray);
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                try {
                    //mPhotoFile = mCompressor.compressToFile(new File(getRealPathFromUri(selectedImage)));
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public Bitmap imageRotation(Bitmap bitmap, Matrix matrix) {
        ExifInterface ei = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ei = new ExifInterface(mPhotoFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation =1;
        if(ei != null)
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }
}