package com.tourassistant.coderoids.home.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tourassistant.coderoids.BuildConfig;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.IntrestsAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.Profile;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class EditProfileFragment extends Fragment {
    TextView changePhoto;
    EditText userName, displayName, website, description, email, phoneNumber;
    CircleImageView cUserImage;
    File mPhotoFile;
    Blob profileImageBlob = null;
    Button btnSave;
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_GALLERY_PHOTO = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_edit, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View view) {
        cUserImage = view.findViewById(R.id.profile_photo);
        changePhoto = view.findViewById(R.id.changeProfilePhoto);
        displayName = view.findViewById(R.id.display_name);
        userName = view.findViewById(R.id.username);
        website = view.findViewById(R.id.website);
        description = view.findViewById(R.id.description);
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        btnSave = view.findViewById(R.id.btnSave);
        populateUserInfo();
        onClick();
    }

    private void onClick() {
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                saveContent(users);
            }
        });
    }

    private void selectImage() {
        final CharSequence[] items = {
                "Take Photo", "Choose from Library",
                "Cancel"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void populateUserInfo() {
        try {
            Profile profileList = AppHelper.currentProfileInstance;
            if (profileList != null) {
                if (!profileList.getPhoneNumber().toString().matches("")) {
                    phoneNumber.setText(profileList.getPhoneNumber().toString());
                }
                if (!profileList.getUserName().toString().matches("")) {
                    userName.setText(profileList.getUserName().toString());
                }
                if (!profileList.getEmail().toString().matches("")) {
                    email.setText(profileList.getEmail().toString());
                }
                if (!profileList.getAboutDescription().toString().matches("")) {
                    description.setText(profileList.getAboutDescription().toString());
                }
                if (!profileList.getDisplayName().toString().matches("")) {
                    displayName.setText(profileList.getDisplayName().toString());
                }
                if (!profileList.getWebsite().toString().matches("")) {
                    website.setText(profileList.getWebsite().toString());
                }
                if (profileList.getProfileImage() != null && !profileList.getProfileImage().toString().matches("")) {
                    byte[] bytes = profileList.getProfileImage().toBytes();
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    cUserImage.setImageBitmap(bmp);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void requestStoragePermission(boolean isCamera) {
        Dexter.withActivity(getActivity())
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
                        error -> Toast.makeText(getActivity().getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                .show())
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.tourassistant.coderoids.android.fileprovider", null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File fdelete = mPhotoFile;
                    Uri contentUri = Uri.fromFile(fdelete);
                    mediaScanIntent.setData(contentUri);
                    Matrix matrix = new Matrix();
                    Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentUri);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(photo, AppHelper.imageWidth, AppHelper.imageHeight, true);
                    Bitmap bitmapLowRes = imageRotation(scaledBitmap, matrix);
                    cUserImage.setImageBitmap(bitmapLowRes);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapLowRes.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    profileImageBlob = Blob.fromBytes(byteArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                try {
                    //mPhotoFile = mCompressor.compressToFile(new File(getRealPathFromUri(selectedImage)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
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
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
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

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void saveContent(FirebaseUser users) {
        try {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please Wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            String uid = users.getUid();
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            DocumentReference uidRef = rootRef.collection("Users").document(uid);
            Profile profile = new Profile();
            profile.setUserId(uid);
            if (profileImageBlob != null)
                profile.setProfileImage(profileImageBlob);
            else if (AppHelper.currentProfileInstance != null && AppHelper.currentProfileInstance.getProfileImage() != null)
                profile.setProfileImage(AppHelper.currentProfileInstance.getProfileImage());
            if (!userName.getText().toString().isEmpty()) {
                profile.setUserName(userName.getText().toString());
            }
            if (!displayName.getText().toString().isEmpty()) {
                profile.setDisplayName(displayName.getText().toString());
            }
            if (!website.getText().toString().isEmpty()) {
                profile.setWebsite(website.getText().toString());
            }
            if (!description.getText().toString().isEmpty()) {
                profile.setAboutDescription(description.getText().toString());
            }
            if (!email.getText().toString().isEmpty()) {
                profile.setEmail(email.getText().toString());
            }
            if (!phoneNumber.getText().toString().isEmpty()) {
                profile.setPhoneNumber(phoneNumber.getText().toString());
            }
            if (AppHelper.interestUser != null && !AppHelper.interestUser.toString().matches("")) {
                profile.setInterests(AppHelper.interestUser + "");
            } else
                profile.setInterests("");
            uidRef.set(profile);
            rootRef.collection("Users").document(users.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    try {
                        AppHelper.currentProfileInstance = documentSnapshot.toObject(Profile.class);
                        if (AppHelper.currentProfileInstance != null)
                            AppHelper.currentProfileInstance.setUserId(documentSnapshot.getId());
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Get real file path from URI
     */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}