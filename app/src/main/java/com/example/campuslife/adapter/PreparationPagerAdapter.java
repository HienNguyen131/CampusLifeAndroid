package com.example.campuslife.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campuslife.fragment.PreparationFinanceFragment;
import com.example.campuslife.fragment.PreparationTasksFragment;

public class PreparationPagerAdapter extends FragmentStateAdapter {

    private final long activityId;
    private final long studentId;
    private final boolean showFinance;

    public PreparationPagerAdapter(@NonNull FragmentActivity fa, long activityId, long studentId, boolean showFinance) {
        super(fa);
        this.activityId = activityId;
        this.studentId = studentId;
        this.showFinance = showFinance;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (showFinance) {
            if (position == 0)
                return PreparationTasksFragment.newInstance(activityId, studentId);
            return PreparationFinanceFragment.newInstance(activityId);
        }
        return PreparationTasksFragment.newInstance(activityId, studentId);
    }

    @Override
    public int getItemCount() {
        return showFinance ? 2 : 1;
    }
}
