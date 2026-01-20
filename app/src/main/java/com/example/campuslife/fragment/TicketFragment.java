package com.example.campuslife.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;

import com.example.campuslife.activity.CalendarActivity;
import com.example.campuslife.activity.ProfileDetailActivity;
import com.example.campuslife.activity.ScanQRActivity;
import com.example.campuslife.activity.SearchActivity;
import com.example.campuslife.activity.SearchRegisActivity;
import com.example.campuslife.adapter.TicketAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.RegistrationApi;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketFragment extends Fragment {
    private RecyclerView rvMyTicket;
    private TicketAdapter adapter;
    private ImageButton btnCalendar,btnSearch,btnQR;
    private MaterialButton btnAll,btnUpcoming,btnPast,btnCancel;

    private List<ActivityRegistrationResponse> ticketList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_ticketcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Search clicked", Toast.LENGTH_SHORT).show()
        );
        btnCalendar=view.findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), CalendarActivity.class);
            startActivity(intent);
        });
        btnQR=view.findViewById(R.id.btnQR);
        btnQR.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), ScanQRActivity.class);
            startActivity(intent);
        });
        rvMyTicket = view.findViewById(R.id.rvMyTicket);
        rvMyTicket.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TicketAdapter(ticketList);
        rvMyTicket.setAdapter(adapter);
        btnAll = view.findViewById(R.id.btnAll);
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchRegisActivity.class);
            startActivity(intent);
        });
        loadTicketsAll();
        btnAll.setOnClickListener(v -> {
            selectButton(btnAll);
            loadTicketsAll();
        });

        btnUpcoming.setOnClickListener(v -> {
            selectButton(btnUpcoming);
            loadTickets("APPROVED");
        });

        btnPast.setOnClickListener(v -> {
            selectButton(btnPast);
            loadTickets("ATTENDED");
        });

        btnCancel.setOnClickListener(v -> {
            selectButton(btnCancel);
            loadTickets("CANCELLED");
        });
        selectButton(btnAll);
        loadTicketsAll();


    }
    private void selectButton(MaterialButton selectedBtn) {

        btnAll.setChecked(false);
        btnUpcoming.setChecked(false);
        btnPast.setChecked(false);
        btnCancel.setChecked(false);

        selectedBtn.setChecked(true);
    }



    private void loadTicketsAll() {
        RegistrationApi api = ApiClient.activityRegistrations(requireContext());

        api.getMyRegistrations().enqueue(new Callback<ApiResponse<List<ActivityRegistrationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ActivityRegistrationResponse>>> call,
                                   Response<ApiResponse<List<ActivityRegistrationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ticketList.clear();
                    ticketList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Không tải được vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ActivityRegistrationResponse>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTickets(String status) {
        RegistrationApi api = ApiClient.activityRegistrations(requireContext());

        api.getMyRegistrationsStatus(status).enqueue(new Callback<ApiResponse<List<ActivityRegistrationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ActivityRegistrationResponse>>> call,
                                   Response<ApiResponse<List<ActivityRegistrationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ticketList.clear();
                    ticketList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Không tải được vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ActivityRegistrationResponse>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}