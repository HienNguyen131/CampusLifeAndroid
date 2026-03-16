package com.example.campuslife.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.CreateActivityRequest;
import com.example.campuslife.entity.Department;
import com.example.campuslife.entity.Student;
import com.example.campuslife.api.StudentApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEventActivity extends AppCompatActivity {

    private ImageView ivBannerPreview;
    private FrameLayout flUploadBanner;
    private View llUploadPrompt;
    private Uri selectedImageUri;

    private AutoCompleteTextView actvOrganizer;
    private ChipGroup cgOrganizers;
    private List<com.example.campuslife.entity.Department> allDepartments = new ArrayList<>();
    private ArrayAdapter<String> organizerAdapter;
    private List<Long> selectedOrganizerIds = new ArrayList<>();

    private EditText etEventName, etMaxPoints, etDescription, etLocation, etTicketQuantity, etBenefits, etRequirements, etShareLink, etContactInfo;
    private TextView tvStartDate, tvEndDate, tvRegDeadline;
    private Spinner spinnerEventType;
    private SwitchMaterial swRequiresSubmission, swRequiresApproval, swMandatory, swUnlimitedTickets, swImportant, swDraft;
    private MaterialButton btnCreateEvent;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private Calendar regCalendar = Calendar.getInstance();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm", Locale.US);

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initViews();
        setupSpinners();
        setupDatePickers();
        setupImagePicker();
        setupLogics();
        setupOrganizerAutocomplete();

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        btnCreateEvent.setOnClickListener(v -> submitEvent(swDraft.isChecked()));
        
        android.view.View saveDraftBtn = findViewById(R.id.tvSaveDraft);
        if (saveDraftBtn != null) {
            saveDraftBtn.setVisibility(android.view.View.GONE);
        }
    }

    private void initViews() {
        ivBannerPreview = findViewById(R.id.ivBannerPreview);
        flUploadBanner = findViewById(R.id.flUploadBanner);
        llUploadPrompt = findViewById(R.id.llUploadPrompt);
        actvOrganizer = findViewById(R.id.actvOrganizer);
        cgOrganizers = findViewById(R.id.cgOrganizers);

        etEventName = findViewById(R.id.etEventName);
        etMaxPoints = findViewById(R.id.etMaxPoints);
        etDescription = findViewById(R.id.etDescription);
        etBenefits = findViewById(R.id.etBenefits);
        etRequirements = findViewById(R.id.etRequirements);
        etLocation = findViewById(R.id.etLocation);
        etShareLink = findViewById(R.id.etShareLink);
        etContactInfo = findViewById(R.id.etContactInfo);
        etTicketQuantity = findViewById(R.id.etTicketQuantity);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvRegDeadline = findViewById(R.id.tvRegDeadline);
        spinnerEventType = findViewById(R.id.spinnerEventType);

        swRequiresSubmission = findViewById(R.id.swRequiresSubmission);
        swRequiresApproval = findViewById(R.id.swRequiresApproval);
        swMandatory = findViewById(R.id.swMandatory);
        swUnlimitedTickets = findViewById(R.id.swUnlimitedTickets);
        swImportant = findViewById(R.id.swImportant);
        swDraft = findViewById(R.id.swDraft);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
    }

    private void setupSpinners() {
        String[] types = {"Sự kiện", "Mini Game", "Công tác xã hội", "Chuyên đề doanh nghiệp"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerEventType.setAdapter(adapter);
    }

    private void setupDatePickers() {
        tvStartDate.setOnClickListener(v -> showDateTimePicker(startCalendar, tvStartDate));
        tvEndDate.setOnClickListener(v -> showDateTimePicker(endCalendar, tvEndDate));
        tvRegDeadline.setOnClickListener(v -> showDateTimePicker(regCalendar, tvRegDeadline));
    }

    private void showDateTimePicker(Calendar calendar, TextView targetView) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                targetView.setText(displayFormat.format(calendar.getTime()));
                // Reset errors if any
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupImagePicker() {
        flUploadBanner.setOnClickListener(v -> openGallery());
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivBannerPreview.setImageURI(selectedImageUri);
                        ivBannerPreview.setVisibility(View.VISIBLE);
                        llUploadPrompt.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    private void setupLogics() {
        swImportant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSpecialEventConstraints();
        });

        swMandatory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSpecialEventConstraints();
        });

        swUnlimitedTickets.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etTicketQuantity.setText("");
                etTicketQuantity.setEnabled(false);
            } else if (!swImportant.isChecked() && !swMandatory.isChecked()) {
                etTicketQuantity.setEnabled(true);
            }
        });
    }

    private void updateSpecialEventConstraints() {
        boolean isSpecial = swImportant.isChecked() || swMandatory.isChecked();
        if (isSpecial) {
            swUnlimitedTickets.setChecked(true);
            swUnlimitedTickets.setEnabled(false);
            etTicketQuantity.setText("");
            etTicketQuantity.setEnabled(false);
            
            swRequiresApproval.setChecked(false);
            swRequiresApproval.setEnabled(false);
        } else {
            swUnlimitedTickets.setEnabled(true);
            swRequiresApproval.setEnabled(true);
        }
    }

    private List<String> listDisplayNames = new ArrayList<>();

    private void setupOrganizerAutocomplete() {
        organizerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listDisplayNames);
        actvOrganizer.setAdapter(organizerAdapter);
        actvOrganizer.setThreshold(1);

        actvOrganizer.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = organizerAdapter.getItem(position);
            for (com.example.campuslife.entity.Department d : allDepartments) {
                String label = d.getName() + " (ID: " + d.getId() + ")";
                if (label.equals(selectedName)) {
                    if (!selectedOrganizerIds.contains(d.getId())) {
                        addOrganizerChip(d);
                    }
                    break;
                }
            }
            actvOrganizer.setText("");
        });

        loadDepartments();
    }

    private void loadDepartments() {
        ApiClient.departments(this).getAll().enqueue(new Callback<List<com.example.campuslife.entity.Department>>() {
            @Override
            public void onResponse(Call<List<com.example.campuslife.entity.Department>> call, Response<List<com.example.campuslife.entity.Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allDepartments = response.body();
                    List<String> names = new ArrayList<>();
                    if (allDepartments != null) {
                        for (com.example.campuslife.entity.Department d : allDepartments) {
                            names.add(d.getName() + " (ID: " + d.getId() + ")");
                        }
                    }
                    listDisplayNames.clear();
                    listDisplayNames.addAll(names);
                    organizerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.campuslife.entity.Department>> call, Throwable t) {
                // Ignore failure quietly
            }
        });
    }

    private void addOrganizerChip(com.example.campuslife.entity.Department dept) {
        Long idToAdd = dept.getId();
        selectedOrganizerIds.add(idToAdd);
        Chip chip = new Chip(this);
        chip.setText(dept.getName());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            cgOrganizers.removeView(chip);
            selectedOrganizerIds.remove(idToAdd);
        });
        cgOrganizers.addView(chip);
    }

    private void submitEvent(boolean isDraft) {
        if (!validateForm()) return;

        btnCreateEvent.setEnabled(false);
        btnCreateEvent.setText("Đang chạy...");

        if (selectedImageUri != null) {
            uploadImage(isDraft);
        } else {
            createEvent(isDraft, null);
        }
    }

    private boolean validateForm() {
        if (etEventName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sự kiện", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedOrganizerIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn BTC", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvStartDate.getText().toString().contains("mm/dd")) {
            Toast.makeText(this, "Vui lòng chọn thời gian bắt đầu", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvEndDate.getText().toString().contains("mm/dd")) {
            Toast.makeText(this, "Vui lòng chọn thời gian kết thúc", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etLocation.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa điểm", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadImage(boolean isDraft) {
        try {
            InputStream is = getContentResolver().openInputStream(selectedImageUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileBytes = buffer.toByteArray();

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "banner_" + System.currentTimeMillis() + ".jpg", requestFile);

            ApiClient.upload(this).uploadImage(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> bodyMap = response.body();
                        if (Boolean.TRUE.equals(bodyMap.get("status"))) {
                            Object dataObj = bodyMap.get("data");
                            String bannerUrl = "";
                            if (dataObj instanceof String) {
                                bannerUrl = (String) dataObj;
                            } else if (dataObj instanceof Map) {
                                bannerUrl = (String) ((Map<?, ?>) dataObj).get("bannerUrl");
                            }
                            createEvent(isDraft, bannerUrl);
                        } else {
                            handleError("Lỗi upload ảnh: " + bodyMap.get("message"));
                        }
                    } else {
                        handleError("Upload ảnh thất bại");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    handleError("Lỗi kết nối upload: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            handleError("Lỗi đọc file: " + e.getMessage());
        }
    }

    private void createEvent(boolean isDraft, String uploadedBannerUrl) {
        CreateActivityRequest req = new CreateActivityRequest();
        req.name = etEventName.getText().toString().trim();

        int typePos = spinnerEventType.getSelectedItemPosition();
        if (typePos == 0) req.type = "SUKIEN";
        else if (typePos == 1) req.type = "MINIGAME";
        else if (typePos == 2) req.type = "CONG_TAC_XA_HOI";
        else req.type = "CHUYEN_DE_DOANH_NGHIEP";

        req.scoreType = "REN_LUYEN";
        req.description = etDescription.getText().toString().trim();
        req.location = etLocation.getText().toString().trim();

        req.startDate = dateFormat.format(startCalendar.getTime());
        req.endDate = dateFormat.format(endCalendar.getTime());
        if (!tvRegDeadline.getText().toString().contains("mm/dd")) {
            req.registrationDeadline = dateFormat.format(regCalendar.getTime());
        }

        try { req.maxPoints = Double.parseDouble(etMaxPoints.getText().toString()); } catch (Exception e) { req.maxPoints = 0.0; }
        
        if (!swUnlimitedTickets.isChecked() && !etTicketQuantity.getText().toString().isEmpty()) {
            try { req.ticketQuantity = Integer.parseInt(etTicketQuantity.getText().toString()); } catch (Exception e) {}
        }

        req.requiresSubmission = swRequiresSubmission.isChecked();
        req.requiresApproval = swRequiresApproval.isChecked();
        req.mandatoryForFacultyStudents = swMandatory.isChecked();
        req.isImportant = swImportant.isChecked();
        req.isDraft = isDraft;
        req.benefits = etBenefits.getText().toString().trim();
        req.requirements = etRequirements.getText().toString().trim();
        req.contactInfo = etContactInfo.getText().toString().trim();
        req.shareLink = etShareLink.getText().toString().trim();
        req.bannerUrl = uploadedBannerUrl != null ? uploadedBannerUrl : "";
        req.organizerIds = selectedOrganizerIds;

        ApiClient.activities(this).createActivity(req).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(Call<ApiResponse<Activity>> call, Response<ApiResponse<Activity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(CreateEventActivity.this, "Tạo sự kiện thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleError("Không thể tạo sự kiện: " + (response.body() != null ? response.body().getMessage() : "Error"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Activity>> call, Throwable t) {
                handleError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        btnCreateEvent.setEnabled(true);
        btnCreateEvent.setText("TẠO SỰ KIỆN");
    }
}
