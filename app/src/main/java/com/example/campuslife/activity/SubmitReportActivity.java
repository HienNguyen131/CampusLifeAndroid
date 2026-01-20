package com.example.campuslife.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.SubmissionResponse;
import com.example.campuslife.utils.FileUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitReportActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_FILE = 100;

    private ImageView btnBack;
    private TextView tvTitle;
    private TextInputEditText edtEmail;
    private MaterialButton btnUpload, btnSubmit, btnUpdate, btnDelete;
    private File selectedFile = null;
    private Long currentSubmissionId = null;
    private long taskId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_submit);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        edtEmail = findViewById(R.id.edtEmail);
        btnUpload = findViewById(R.id.btnUpload);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        String title = getIntent().getStringExtra("task_name");
        taskId = getIntent().getLongExtra("task_id", -1);
        if (title != null && !title.isEmpty()) tvTitle.setText(title);

        btnBack.setOnClickListener(v -> finish());
        btnUpload.setOnClickListener(v -> openFilePicker());

        if (taskId != -1) loadMySubmissions(taskId);

        // Nộp bài
        btnSubmit.setOnClickListener(v -> {
            String content = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            if (content.isEmpty() && selectedFile == null) {
                Toast.makeText(this, "Vui lòng nhập nội dung hoặc tải file!", Toast.LENGTH_SHORT).show();
                return;
            }
            submitTask(taskId, content, selectedFile);
        });

        // Cập nhật bài nộp
        btnUpdate.setOnClickListener(v -> {
            String content = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            if (currentSubmissionId == null) {
                Toast.makeText(this, "Không tìm thấy bài để cập nhật!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateSubmission(currentSubmissionId, content, selectedFile);
        });

        // Xoá bài nộp
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    // Mở chọn file
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn file bài nộp"), REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String filePath = FileUtils.getPath(this, uri);
            if (filePath != null) {
                selectedFile = new File(filePath);
                Toast.makeText(this, "Đã chọn file: " + selectedFile.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không lấy được đường dẫn file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Tải bài đã nộp (nếu có)
    private void loadMySubmissions(long taskId) {
        ApiClient.activityReports(this)
                .getMySubmissions(taskId)
                .enqueue(new Callback<ApiResponse<SubmissionResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SubmissionResponse>> call,
                                           Response<ApiResponse<SubmissionResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            SubmissionResponse s = response.body().getData();
                            if (s != null) {
                                currentSubmissionId = s.getId();
                                edtEmail.setText(s.getContent());

                                btnSubmit.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.VISIBLE);
                                btnDelete.setVisibility(View.VISIBLE);

                                if (s.getFileUrls() != null && !s.getFileUrls().isEmpty()) {
                                    btnUpload.setText("Xem bài đã nộp");
                                    btnUpload.setOnClickListener(v -> {
                                        String fileUrl = s.getFileUrls().get(0);
                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(BuildConfig.BASE_URL + fileUrl));
                                        startActivity(intent);
                                    });
                                }
                            }
                        } else {
                            btnSubmit.setVisibility(View.VISIBLE);
                            btnUpdate.setVisibility(View.GONE);
                            btnDelete.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SubmissionResponse>> call, Throwable t) {
                        Toast.makeText(SubmitReportActivity.this,
                                "Lỗi tải bài nộp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Gửi bài nộp mới
    private void submitTask(long taskId, String content, File selectedFile) {
        MultipartBody.Part filePart = createFilePart(selectedFile);
        RequestBody textBody = RequestBody.create(MediaType.parse("text/plain"), content);

        ApiClient.activityReports(this)
                .submitTask(taskId, textBody, filePart)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call,
                                           Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Object> res = response.body();
                            if (res.isStatus()) {
                                Toast.makeText(SubmitReportActivity.this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(SubmitReportActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SubmitReportActivity.this, "Nộp bài thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(SubmitReportActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Cập nhật bài nộp
    private void updateSubmission(long submissionId, String content, File selectedFile) {
        MultipartBody.Part filePart = createFilePart(selectedFile);
        RequestBody textBody = RequestBody.create(MediaType.parse("text/plain"), content);

        ApiClient.activityReports(this)
                .updateSubmission(submissionId, textBody, filePart)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call,
                                           Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Object> res = response.body();
                            if (res.isStatus()) {
                                Toast.makeText(SubmitReportActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(SubmitReportActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SubmitReportActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(SubmitReportActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private MultipartBody.Part createFilePart(File file) {
        if (file != null && file.exists()) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            return MultipartBody.Part.createFormData("files", file.getName(), reqFile);
        }
        return null;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá bài nộp")
                .setMessage("Bạn có chắc muốn xoá bài nộp này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteSubmission())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteSubmission() {
        ApiClient.activityReports(this)
                .deleteSubmission(currentSubmissionId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call,
                                           Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Object> res = response.body();
                            if (res.isStatus()) {
                                Toast.makeText(SubmitReportActivity.this, "Xoá thành công!", Toast.LENGTH_SHORT).show();
                                resetForm();
                            } else {
                                Toast.makeText(SubmitReportActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SubmitReportActivity.this, "Không thể xoá!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(SubmitReportActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetForm() {
        edtEmail.setText("");
        selectedFile = null;
        currentSubmissionId = null;
        btnSubmit.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnUpload.setText("Upload your file to the system");
        btnUpload.setOnClickListener(v2 -> openFilePicker());
    }
}
