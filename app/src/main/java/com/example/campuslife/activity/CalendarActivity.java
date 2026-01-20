package com.example.campuslife.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.CalendarAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.ActivityRegistrationResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarActivity extends AppCompatActivity {

    ImageView btnBack, btnPrevMonth, btnNextMonth;
    TextView txtTitle, txtMonth;
    RecyclerView calendarRecycler;

    LocalDate currentMonth;


    List<ActivityRegistrationResponse> registrations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();

        currentMonth = LocalDate.now();
        setMonthTitle();

        fetchMyRegistrations();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtMonth = findViewById(R.id.txtMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarRecycler = findViewById(R.id.calendarRecycler);
        txtTitle = findViewById(R.id.txtTilte);

        txtTitle.setText("Activity schedule");

        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            setupCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            setupCalendar();
        });
    }

    private void setMonthTitle() {
        txtMonth.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
    }

    private void fetchMyRegistrations() {

        ApiClient.activityRegistrations(this)
                .getMyRegistrationsStatus("APPROVED")
                .enqueue(new Callback<ApiResponse<List<ActivityRegistrationResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivityRegistrationResponse>>> call,
                                           Response<ApiResponse<List<ActivityRegistrationResponse>>> resp) {

                        if (!resp.isSuccessful() || resp.body() == null) {
                            Toast.makeText(CalendarActivity.this,
                                    "Lỗi API: " + resp.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        registrations = resp.body().getData();
                        setupCalendar();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivityRegistrationResponse>>> call, Throwable t) {
                        Toast.makeText(CalendarActivity.this,
                                "Lỗi: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupCalendar() {
        setMonthTitle();

        List<LocalDate> days = getDaysInMonth(currentMonth);
        Map<String, List<ActivityRegistrationResponse>> eventMap = buildEventMap();

        CalendarAdapter adapter = new CalendarAdapter(
                days,
                eventMap,
                (date, events) -> showEventDialog(date, events)
        );

        calendarRecycler.setLayoutManager(new GridLayoutManager(this, 7));
        calendarRecycler.setAdapter(adapter);
    }

    private List<LocalDate> getDaysInMonth(LocalDate month) {

        List<LocalDate> days = new ArrayList<>();

        LocalDate firstDay = month.withDayOfMonth(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();

        LocalDate startDate = firstDay.minusDays(startDayOfWeek - 1);

        for (int i = 0; i < 42; i++) {
            days.add(startDate.plusDays(i));
        }

        return days;
    }


    private Map<String, List<ActivityRegistrationResponse>> buildEventMap() {

        Map<String, List<ActivityRegistrationResponse>> map = new HashMap<>();

        for (ActivityRegistrationResponse r : registrations) {

            if (r.getActivityStartDate() == null) continue;

            String key = r.getActivityStartDate().substring(0, 10); // yyyy-MM-dd

            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(r);
        }

        return map;
    }


    private void showEventDialog(LocalDate date, List<ActivityRegistrationResponse> events) {

        if (events.isEmpty()) {
            Toast.makeText(this, "No event", Toast.LENGTH_SHORT).show();
            return;
        }

        if (events.size() == 1) {
            openTicketDetail(events.get(0).getId());
            return;
        }

        String[] items = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            items[i] = events.get(i).getActivityName() +
                    " @ " +
                    events.get(i).getActivityLocation();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select event")
                .setItems(items, (dialog, which) ->
                        openTicketDetail(events.get(which).getId()))
                .show();
    }

    private void openTicketDetail(Long registrationId) {
        Intent i = new Intent(this, TicketDetailActivity.class);
        i.putExtra("registration_id", registrationId);
        startActivity(i);
    }
}
