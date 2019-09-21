package com.example.loginregistration;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import android.Manifest;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginregistration.model.Coordinates;
import com.example.loginregistration.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;

import de.hdodenhof.circleimageview.CircleImageView;

// https://www.freakyjolly.com/android-material-datepicker-and-timepicker-by-wdullaer-tutorial-by-example/#more-2649
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

public class Register extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    //    TODO: ADD IMAGE CROP FOR SAKE OF COMPLETENESS
    //    TODO: PREVENT OPEN AN ACTION TWICE
    //    TODO: LAYOUT DESIGN
    //    TODO: RUNTIME PERMISSION

    private static final int PICK_IMAGE = 0xbb;
    CircleImageView register_image;
    EditText register_name, register_email, register_password, register_address, editText_carrierNumber;
    RadioGroup register_gender_group;
    CountryCodePicker ccp;
    Spinner register_country_spinner, register_state_spinner, register_city_spinner;
    Button register_sign_btn;
    ImageButton selected_dbo_btn;
    TextView selected_dbo_text;
    Bitmap selectedPicture;

    DatePickerDialog datePickerDialog;
    int Year, Month, Day, Hour, Minute;
    Calendar calendar;
    String selectedDate = "";
    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;
    int SETTING_PERMISSION = 0xbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        requestStoragePermission();

        progressDialog = new ProgressDialog(Register.this);
        progressDialog.setMessage("Signing in...");

        calendar = Calendar.getInstance();

        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);
        Hour = calendar.get(Calendar.HOUR_OF_DAY);
        Minute = calendar.get(Calendar.MINUTE);


        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        register_image = findViewById(R.id.register_image);
        register_name = findViewById(R.id.register_name);
        register_email = findViewById(R.id.register_email);
        register_password = findViewById(R.id.register_password);
        register_gender_group = findViewById(R.id.register_gender_group);
        register_gender_group.check(R.id.male);
        register_address = findViewById(R.id.register_address);
        ccp = findViewById(R.id.ccp);
        editText_carrierNumber = findViewById(R.id.editText_carrierNumber);
        selected_dbo_text = findViewById(R.id.selected_dbo_text);
        selected_dbo_btn = findViewById(R.id.selected_dbo_btn);
        register_country_spinner = findViewById(R.id.register_country_spinner);
        register_state_spinner = findViewById(R.id.register_state_spinner);
        register_city_spinner = findViewById(R.id.register_city_spinner);
        register_sign_btn = findViewById(R.id.register_sign_btn);

        setCountrySpinnerData(register_country_spinner, getResources().getStringArray(R.array.countries));
        setStateSpinnerData(register_state_spinner, getResources().getStringArray(R.array.states_India));
        setCitiesSpinnerData(register_city_spinner, getResources().getStringArray(R.array.cities_Jharkhand));

        selected_dbo_btn.setOnClickListener(view -> datePicker());
        selected_dbo_text.setOnClickListener(view -> datePicker());

        register_sign_btn.setOnClickListener(view -> signUp());

        register_image.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(i, "Choose Profile Picture"), PICK_IMAGE);
        });

        ccp.registerCarrierNumberEditText(editText_carrierNumber);
    }

    private void signUp() {
        String name = register_name.getText().toString().trim();
        String email = register_email.getText().toString().trim();
        String password = register_password.getText().toString().trim();
        String gender = "male";
        String address = register_address.getText().toString().trim();
        String dob = selectedDate.trim();
        String country = register_country_spinner.getSelectedItem().toString();
        String state = register_state_spinner.getSelectedItem().toString();
        String city = register_city_spinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            register_name.setError("what's your name? write down");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            register_email.setError("Email is require!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            register_password.setError("Password is require!");
            return;
        }

        if (password.length() < 6) {
            register_password.setError("Password must be 6 character long");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            register_address.setError("Address is require");
            return;
        }

        if (address.length() < 6) {
            register_address.setError("Address must be 6 character long");
        }

        if (!ccp.isValidFullNumber()) {
            editText_carrierNumber.setError("Enter Correct Phone Number");
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            Toast.makeText(getApplicationContext(), "Select Date Of Birth", Toast.LENGTH_LONG).show();
            return;
        }

        if (country.equals("Select")) {
            Toast.makeText(getApplicationContext(), "Select Country", Toast.LENGTH_LONG).show();
            return;
        }

        if (state.equals("Select")) {
            Toast.makeText(getApplicationContext(), "Select State", Toast.LENGTH_LONG).show();
            return;
        }

        if (city.equals("Select")) {
            Toast.makeText(getApplicationContext(), "Select City", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedPicture == null) {
            Toast.makeText(getApplicationContext(), "Select Profile Picture", Toast.LENGTH_LONG).show();
            return;
        }

        switch (register_gender_group.getCheckedRadioButtonId()) {
            case R.id.female:
                gender = "female";
                break;
            case R.id.others:
                gender = "others";
                break;
        }

        progressDialog.show();

        String finalGender = gender;
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Register.this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.setMessage("Completed 1/3...");

                        // create user object and save that with the key of 'uid'
                        String uid = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                        String path = "users/" + uid;

                        // save image
                        if (selectedPicture != null) {
                            Uri image = getImageUri(getApplicationContext(), selectedPicture);
                            storageReference.child(path).
                                    putFile(image)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        progressDialog.setMessage("Completed 2/3...");

                                        //  save user object
                                        User newUser = new User(path, name, finalGender, address, dob, new Coordinates(22.22, 84.36), ccp.getFullNumberWithPlus(), country, state, city, city, null, email, 0);

                                        databaseReference.child("users").child(uid).setValue(newUser).addOnCompleteListener(task1 -> {
                                            Toast.makeText(getApplicationContext(), "Done " + newUser.getName(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Intent next = new Intent(Register.this, MainActivity.class);
                                            next.putExtra("userId", uid);
                                            startActivity(next);
                                            finish();
                                        });

                                    }).addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Ah! Something went wrong", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Select Profile Picture", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } else {
                        Toast.makeText(Register.this, Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressDialog.dismiss();
    }

    public void datePicker() {
        datePickerDialog = DatePickerDialog.newInstance(Register.this, Year, Month, Day);
        datePickerDialog.setThemeDark(false);
        datePickerDialog.setTitle("Choose Date of Birth");

        // Setting Max Date to previous 12 years form current year
        Calendar max_date_c = Calendar.getInstance();
        max_date_c.set(Calendar.YEAR, Year - 12);
        datePickerDialog.setMaxDate(max_date_c);

        datePickerDialog.setOnCancelListener(dialogInterface -> Toast.makeText(Register.this, "Please Choose D.O.B", Toast.LENGTH_SHORT).show());
        datePickerDialog.showYearPickerFirst(true);
        datePickerDialog.show(getSupportFragmentManager(), "DOB");
    }

    public void setCountrySpinnerData(Spinner spinner, final String[] data) {
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(Register.this, android.R.layout.simple_spinner_dropdown_item, data);
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String currentCountrySelected = register_country_spinner.getSelectedItem().toString();
                register_state_spinner.setSelection(0);
                register_city_spinner.setSelection(0);
                if (currentCountrySelected.equals("Select")) {
                    register_state_spinner.setEnabled(false);
                }
                if (!currentCountrySelected.equals("Select")) {
                    register_state_spinner.setEnabled(true);
                }
                register_city_spinner.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void setStateSpinnerData(Spinner spinner, final String[] data) {
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(Register.this, android.R.layout.simple_spinner_dropdown_item, data);
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String currentStateSelected = register_state_spinner.getSelectedItem().toString();
                if (currentStateSelected.equals("Select")) {
                    register_city_spinner.setEnabled(false);
                }

                if (!currentStateSelected.equals("Select")) {
                    String arrayStringName = "cities_" + currentStateSelected.replaceAll(" ", "_");
                    int resId = getCitiesResourceIdOfState(arrayStringName);
                    String[] cities = getResources().getStringArray(resId);
                    setCitiesSpinnerData(register_city_spinner, cities);
                    register_city_spinner.setEnabled(true);

                }
                register_city_spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void setCitiesSpinnerData(Spinner spinner, final String[] data) {
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(Register.this, android.R.layout.simple_spinner_dropdown_item, data);
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptor);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        selectedDate = date;
        selected_dbo_text.setText(date);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            // Get selected gallery image
            InputStream inputStream;
            try {
                Uri picture = data.getData();
                assert picture != null;
                inputStream = getApplicationContext().getContentResolver().openInputStream(picture);
                assert inputStream != null;
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap photo = BitmapFactory.decodeStream(bufferedInputStream);
                // Getting the image path using URI
                String Image_path = getPath(getApplicationContext(), picture);

                // change rotation here
                Bitmap rotatedPicture = modifyOrientation(photo, Image_path);
                selectedPicture = rotatedPicture;
                register_image.setImageBitmap(rotatedPicture);
                register_image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getCitiesResourceIdOfState(String state) {
        switch (state) {
            case "cities_Delhi":
                return R.array.cities_Delhi;
            case "cities_Chandigarh":
                return R.array.cities_Chhattisgarh;
            case "cities_Jharkhand":
                return R.array.cities_Jharkhand;
            case "cities_West_Bengal":
                return R.array.cities_West_Bengal;
            case "cities_Uttarakhand":
                return R.array.cities_Uttarakhand;
            case "cities_Uttar_Pradesh":
                return R.array.cities_Uttar_Pradesh;
            case "cities_Tripura":
                return R.array.cities_Tripura;
            case "cities_Telangana":
                return R.array.cities_Telangana;
            case "cities_Tamil_Nadu":
                return R.array.cities_Tamil_Nadu;
            case "cities_Sikkim":
                return R.array.cities_Sikkim;
            case "cities_Rajasthan":
                return R.array.cities_Rajasthan;
            case "cities_Punjab":
                return R.array.cities_Punjab;
            case "cities_Odisha":
                return R.array.cities_Odisha;
            case "cities_Nagaland":
                return R.array.cities_Nagaland;
            case "cities_Mizoram":
                return R.array.cities_Mizoram;
            case "cities_Meghalaya":
                return R.array.cities_Meghalaya;
            case "cities_Manipur":
                return R.array.cities_Manipur;
            case "cities_Maharashtra":
                return R.array.cities_Maharashtra;
            case "cities_Madhya_Pradesh":
                return R.array.cities_Madhya_Pradesh;
            case "cities_Lakshadweep":
                return R.array.cities_Lakshadweep;
            case "cities_Kerala":
                return R.array.cities_Kerala;
            case "cities_Karnataka":
                return R.array.cities_Karnataka;
            case "cities_Jammu_and_Kashmir":
                return R.array.cities_Jammu_and_Kashmir;
            case "cities_Himachal_Pradesh":
                return R.array.cities_Himachal_Pradesh;
            case "cities_Haryana":
                return R.array.cities_Haryana;
            case "cities_Gujarat":
                return R.array.cities_Gujarat;
            case "cities_Goa":
                return R.array.cities_Goa;
            case "cities_Chhattisgarh":
                return R.array.cities_Chhattisgarh;
            case "cities_Bihar":
                return R.array.cities_Bihar;
            case "cities_Assam":
                return R.array.cities_Assam;
            case "cities_Arunachal_Pradesh":
                return R.array.cities_Arunachal_Pradesh;
            case "cities_Andhra_Pradesh":
                return R.array.cities_Andhra_Pradesh;
        }
        return 0;
    }

    //*************************************************************//
    // IMAGE RELATED WORK BELOW
    //*************************************************************//

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        // final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        //if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        final String column = "_data";
        final String[] projection = {
                column
        };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Requesting multiple permissions (storage and location) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        // if (report.areAllPermissionsGranted()) {
                        // Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                        // }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setTitle("Need Permissions for Image Upload");
        builder.setMessage("This app needs permission to upload IMAGE. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTING_PERMISSION);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "rh", null);
        return Uri.parse(path);
    }
}