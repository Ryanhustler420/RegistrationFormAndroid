package com.example.loginregistration;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import android.Manifest;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

public class Register extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    //    TODO: PREVENT OPEN AN ACTION TWICE
    //    TODO: LAYOUT DESIGN
    //    TODO: CURRENT LOCATION FETCHING WHILE FILLING FORM AND SAVE IN LAST LOCATION

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 0xbb;

    CircleImageView register_image;
    EditText register_name, register_email, register_password, register_address, editText_carrierNumber;
    RadioGroup register_gender_group;
    CountryCodePicker ccp;
    Spinner register_country_spinner, register_state_spinner, register_city_spinner;
    Button register_sign_btn;
    ImageButton selected_dbo_btn;
    TextView selected_dbo_text;
    Uri selectedPicture;

    DatePickerDialog datePickerDialog;
    int Year, Month, Day, Hour, Minute;
    Calendar calendar;
    String selectedDate = "";
    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        progressDialog = new ProgressDialog(Register.this);
        progressDialog.setMessage("Signing in...");

        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);
        Hour = calendar.get(Calendar.HOUR_OF_DAY);
        Minute = calendar.get(Calendar.MINUTE);

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

        ccp.registerCarrierNumberEditText(editText_carrierNumber);

        register_image.setOnClickListener(view -> onProfileImageClick());

        // loadProfileDefault();

        // Clearing older images from cache directory
        // don't call this line if you want to choose multiple images in the same activity
        // call this once the bitmap(s) usage is over
        ImagePickerActivity.clearCache(this);
    }

    private void loadProfile(String url) {
        Log.d(TAG, "Image cache path: " + url);
        Picasso.get().load(url)
                .into(register_image);
        register_image.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
    }

    //    private void loadProfileDefault() {
    //        Picasso.get().load(R.drawable.ic_photo_camera_black_24dp)
    //                .into(register_image);
    //        register_image.setColorFilter(ContextCompat.getColor(this, R.color.profile_default_tint));
    //    }

    void onProfileImageClick() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialogInterface, which) -> {
            dialogInterface.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialogInterface, which) -> dialogInterface.cancel());
        builder.show();
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(Register.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(Register.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                Uri uri = data.getParcelableExtra("path");
                selectedPicture = uri;
                // loading profile image from local cache
                loadProfile(uri.toString());
                //try {
                // You can update this bitmap to your server
                //Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                //} catch (IOException e) {
                //e.printStackTrace();
                //}
            }
        }
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
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
                            storageReference.child(path).
                                    putFile(selectedPicture)
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
}