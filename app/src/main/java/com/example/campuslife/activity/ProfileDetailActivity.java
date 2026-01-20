package com.example.campuslife.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.api.AddressApi;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.ProfileAPI;
import com.example.campuslife.entity.Address;
import com.example.campuslife.entity.Department;
import com.example.campuslife.entity.Student;
import com.example.campuslife.entity.StudentClass;
import com.example.campuslife.entity.StudentProfileUpdateRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileDetailActivity extends AppCompatActivity {

    // UI
    private ImageView imgAvatar;
    private TextView btnChangePhoto;
    private TextInputEditText edtFullName, edtPhone, edtBirthDate, edtEmail, edtStudentCode, edtAddress;
    private AutoCompleteTextView edtDepartment, edtClassName, edtCity, edtDistrict;
    private MaterialButton btnSave;

    // State
    private boolean isEditing = false;

    // Avatar
    private String avatarUrl = null;

    // Original values from server (để gửi lại khi update)
    private String originalFullName;
    private String originalStudentCode;
    private String originalPhone;
    private String originalDob;
    private String originalAvatarUrl;

    // Address state
    private boolean hasAddress = false;
    private int originalProvinceCode = 0;
    private int originalWardCode = 0;

    // Province / Ward
    private final List<Province> provinceList = new ArrayList<>();
    private final List<String> provinceNames = new ArrayList<>();
    private ArrayAdapter<String> cityAdapter;
    private ArrayAdapter<String> districtAdapter;
    private Province selectedProvince = null;
    private Ward selectedWard = null;

    // Department & Class
    private final List<Department> departmentList = new ArrayList<>();
    private final List<String> departmentNames = new ArrayList<>();
    private ArrayAdapter<String> departmentAdapter;

    private final List<StudentClass> classList = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    private ArrayAdapter<String> classAdapter;

    // Pick image launcher
    private static final int PICK_IMAGE = 1001;
    private Uri imageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        initViews();
        lockAllFields();

        loadProvincesJson();
        setupCityDropdown();
        loadDepartments();
        loadProfile();
        loadAddress();

        // Events
        btnChangePhoto.setOnClickListener(v -> openGallery());

        btnSave.setOnClickListener(v -> {
            if (!isEditing) {
                enableEditingMode();
            } else {
                saveAll();
            }
        });

        edtCity.setOnItemClickListener((parent, view, position, id) -> onProvinceSelected(position));

        edtDepartment.setOnItemClickListener((parent, view, position, id) -> {
            Department selected = departmentList.get(position);
            loadClasses(selected.getId());
        });
    }

    // INIT
    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        edtEmail = findViewById(R.id.edtEmail);
        edtStudentCode = findViewById(R.id.edtStudentCode);
        edtAddress = findViewById(R.id.edtAddress);

        edtDepartment = findViewById(R.id.edtDepartment);
        edtClassName = findViewById(R.id.edtClassName);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);

        btnSave = findViewById(R.id.btnSave);

        edtBirthDate.setFocusable(false);
        edtBirthDate.setOnClickListener(v -> showDatePickerDialog());
    }


    private void lockAllFields() {
        setEditable(edtPhone, false);
        setEditable(edtBirthDate, false);
        setEditable(edtDepartment, false);
        setEditable(edtClassName, false);
        setEditable(edtCity, false);
        setEditable(edtDistrict, false);
        setEditable(edtAddress, false);
        setEditable(edtFullName, false);
        setEditable(edtEmail, false);
        setEditable(edtStudentCode, false);
        btnSave.setText("Edit");
        isEditing = false;
    }

    private void enableEditingMode() {
        setEditable(edtPhone, true);
        setEditable(edtBirthDate, true);
        setEditable(edtDepartment, true);
        setEditable(edtClassName, true);
        setEditable(edtCity, true);
        setEditable(edtDistrict, true);
        setEditable(edtAddress, true);

        setEditable(edtFullName, false);
        setEditable(edtEmail, false);
        setEditable(edtStudentCode, false);

        btnSave.setText("Update");
        isEditing = true;
    }

    private void setEditable(TextInputEditText view, boolean editable) {
        view.setFocusable(editable);
        view.setFocusableInTouchMode(editable);
        view.setClickable(editable);
        view.setCursorVisible(editable);
    }

    private void setEditable(AutoCompleteTextView view, boolean editable) {
        view.setFocusable(editable);
        view.setFocusableInTouchMode(editable);
        view.setClickable(editable);
    }


    // LOAD PROFILE
    private void loadProfile() {
        ProfileAPI api = ApiClient.profile(this);
        api.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call,
                                   Response<ApiResponse<Student>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                Student s = response.body().getData();
                if (s == null) return;

                // Hiển thị
                edtFullName.setText(s.getFullName());
                edtEmail.setText(s.getEmail());
                edtStudentCode.setText(s.getStudentCode());
                edtPhone.setText(s.getPhone());

                if (s.getDob() != null) {
                    // BE trả yyyy-MM-dd → hiển thị nguyên
                    edtBirthDate.setText(s.getDob());
                }

                avatarUrl = s.getAvatarUrl()
                        == null
                        ? ""
                        : s.getAvatarUrl()
                        .replace("http://localhost:8080", "http://196.169.1.192:8080");


//                avatarUrl = avatarUrl.replace("http://localhost:8080", "http://10.0.2.2:8080");


                Glide.with(ProfileDetailActivity.this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_profile_2)
                        .into(imgAvatar);

                if (s.getDepartmentName() != null) {
                    edtDepartment.setText(s.getDepartmentName(), false);
                }

                if (s.getClassName() != null) {
                    edtClassName.setText(s.getClassName(), false);
                }

                // Lưu lại original
                originalFullName = s.getFullName();
                originalStudentCode = s.getStudentCode();
                originalPhone = s.getPhone();
                originalDob = s.getDob();
                originalAvatarUrl = s.getAvatarUrl();
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                Toast.makeText(ProfileDetailActivity.this, "Load profile failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // LOAD ADDRESS

    private void loadAddress() {
        AddressApi api = ApiClient.address(this);
        api.getMyAddress().enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(Call<ApiResponse<Address>> call,
                                   Response<ApiResponse<Address>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                Address address = response.body().getData();
                if (address == null) {
                    hasAddress = false;
                    return;
                }

                hasAddress = true;
                originalProvinceCode = Math.toIntExact(address.getProvinceCode());
                originalWardCode = Math.toIntExact(address.getWardCode());

                edtCity.setText(address.getProvinceName(), false);
                edtDistrict.setText(address.getWardName(), false);
                edtAddress.setText(address.getStreet());
            }

            @Override
            public void onFailure(Call<ApiResponse<Address>> call, Throwable t) {
                Toast.makeText(ProfileDetailActivity.this, "Load address failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SAVE ALL (PROFILE + ADDRESS)

    private void saveAll() {
        updateProfile();
        updateAddress();
        lockAllFields();
        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
    }

    // PROFILE UPDATE
    private void updateProfile() {
        StudentProfileUpdateRequest req = new StudentProfileUpdateRequest();

        // Full name + studentCode
        req.setFullName(originalFullName);
        req.setStudentCode(originalStudentCode);
        req.setAvatarUrl(avatarUrl != null ? avatarUrl : originalAvatarUrl);

        // Phone
        req.setPhone(edtPhone.getText().toString().trim());

        // Avatar
        req.setAvatarUrl(avatarUrl != null ? avatarUrl : originalAvatarUrl);

        // DOB
        String dobInput = edtBirthDate.getText().toString().trim();
        if (!dobInput.isEmpty()) {
            req.setDob(convertDob(dobInput));
        } else {
            req.setDob(originalDob);
        }


        String depName = edtDepartment.getText().toString().trim();
        Long depId = null;
        for (Department d : departmentList) {
            if (d.getName().equals(depName)) {
                depId = d.getId();
                break;
            }
        }
        req.setDepartmentId(depId);


        String className = edtClassName.getText().toString().trim();
        Long classId = null;
        for (StudentClass sc : classList) {
            if (sc.getClassName().equals(className)) {
                classId = sc.getId();
                break;
            }
        }
        req.setClassId(classId);

        ApiClient.profile(this).updateMyProfile(req)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call,
                                           Response<ApiResponse<Object>> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(ProfileDetailActivity.this, "Update profile failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(ProfileDetailActivity.this, "Update profile error", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void updateAddress() {
        AddressApi api = ApiClient.address(this);

        String provinceName = edtCity.getText().toString().trim();
        String wardName = edtDistrict.getText().toString().trim();
        String street = edtAddress.getText().toString().trim();

        int provinceCode = selectedProvince != null ? selectedProvince.code : originalProvinceCode;
        int wardCode = selectedWard != null ? selectedWard.code : originalWardCode;

        if (!hasAddress) {
            // Tạo mới
            api.createMyAddress(
                    provinceCode,
                    provinceName,
                    wardCode,
                    wardName,
                    street,
                    ""
            ).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) { }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { }
            });
        } else {
            // Cập nhật
            api.updateMyAddress(
                    provinceCode,
                    provinceName,
                    wardCode,
                    wardName,
                    street,
                    ""
            ).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) { }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { }
            });
        }
    }


    private void loadProvincesJson() {
        try {
            InputStream is = getAssets().open("danhmucxaphuong.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray arr = new JSONArray(json);
            provinceList.clear();
            provinceNames.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                Province p = new Province();
                p.code = obj.getInt("matinhTMS");
                p.name = obj.getString("tentinhmoi");
                p.wards = new ArrayList<>();

                JSONArray wardArr = obj.getJSONArray("phuongxa");
                for (int j = 0; j < wardArr.length(); j++) {
                    JSONObject w = wardArr.getJSONObject(j);
                    Ward ward = new Ward();
                    ward.code = w.getInt("maphuongxa");
                    ward.name = w.getString("tenphuongxa");
                    p.wards.add(ward);
                }

                provinceList.add(p);
                provinceNames.add(p.name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCityDropdown() {
        cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                provinceNames);
        edtCity.setAdapter(cityAdapter);
    }

    private void onProvinceSelected(int index) {
        if (index < 0 || index >= provinceList.size()) return;

        selectedProvince = provinceList.get(index);
        List<String> wardNames = new ArrayList<>();
        for (Ward w : selectedProvince.wards) {
            wardNames.add(w.name);
        }

        districtAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                wardNames);
        edtDistrict.setAdapter(districtAdapter);

        edtDistrict.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < selectedProvince.wards.size()) {
                selectedWard = selectedProvince.wards.get(position);
            }
        });
    }


    private void loadDepartments() {
        ApiClient.address(this).getAll().enqueue(new Callback<List<Department>>() {
            @Override
            public void onResponse(Call<List<Department>> call,
                                   Response<List<Department>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                departmentList.clear();
                departmentNames.clear();

                departmentList.addAll(response.body());
                for (Department d : departmentList) {
                    departmentNames.add(d.getName());
                }

                departmentAdapter = new ArrayAdapter<>(ProfileDetailActivity.this,
                        android.R.layout.simple_list_item_1,
                        departmentNames);
                edtDepartment.setAdapter(departmentAdapter);
            }

            @Override
            public void onFailure(Call<List<Department>> call, Throwable t) {
                Toast.makeText(ProfileDetailActivity.this, "Load departments failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClasses(Long departmentId) {
        ApiClient.departments(this)
                .getClassByDepartment(departmentId)
                .enqueue(new Callback<ApiResponse<List<StudentClass>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<StudentClass>>> call,
                                           Response<ApiResponse<List<StudentClass>>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        List<StudentClass> list = response.body().getData();
                        if (list == null) return;

                        classList.clear();
                        classNames.clear();

                        classList.addAll(list);
                        for (StudentClass sc : classList) {
                            classNames.add(sc.getClassName());
                        }

                        classAdapter = new ArrayAdapter<>(ProfileDetailActivity.this,
                                android.R.layout.simple_list_item_1,
                                classNames);
                        edtClassName.setAdapter(classAdapter);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StudentClass>>> call, Throwable t) {
                        Toast.makeText(ProfileDetailActivity.this, "Load classes failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();
            if (imageUri == null) return;

            // preview ảnh
            Glide.with(this).load(imageUri).into(imgAvatar);

            // upload lên server
            uploadImageToServer(imageUri);
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }
    private void uploadImageToServer(Uri uri) {

        File file;
        try {
            file = getFileFromUri(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot read file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody reqFile = RequestBody.create(
                MediaType.parse("image/*"),
                file
        );

        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file",
                file.getName(),
                reqFile
        );

        ApiClient.profile(this).uploadImage(body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(ProfileDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        avatarUrl = response.body().get("data").toString();  // 🔥 URL từ server
                        Toast.makeText(ProfileDetailActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(ProfileDetailActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void showDatePickerDialog() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int year = c.get(java.util.Calendar.YEAR);
        int month = c.get(java.util.Calendar.MONTH);
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String ui = String.format("%02d/%02d/%04d", d, m + 1, y);
                    edtBirthDate.setText(ui);
                },
                year, month, day
        );
        dialog.show();
    }

    private String convertDob(String input) {
        // Nếu dạng dd/MM/yyyy → convert sang yyyy-MM-dd
        if (input.contains("/")) {
            String[] parts = input.split("/");
            if (parts.length == 3) {
                String day = parts[0];
                String month = parts[1];
                String year = parts[2];
                return year + "-" + two(month) + "-" + two(day);
            }
        }
        // nếu đã đúng dạng yyyy-MM-dd thì giữ nguyên
        return input;
    }

    private String two(String s) {
        return s.length() == 1 ? "0" + s : s;
    }


    private static class Province {
        int code;           // matinhTMS
        String name;        // tentinhmoi
        List<Ward> wards;
    }

    private static class Ward {
        int code;           // maphuongxa
        String name;        // tenphuongxa
    }
}
