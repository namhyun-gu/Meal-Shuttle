package com.earlier.yma.meal;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.earlier.yma.R;
import com.earlier.yma.data.Meal;
import com.earlier.yma.searchschool.SearchSchoolActivity;
import com.earlier.yma.utilities.Utils;

import java.util.Date;

public class MealFragment extends Fragment implements MealContract.View {

    private MealContract.Presenter mPresenter;

    private MealAdapter mAdapter;
    private RecyclerView mMealView;
    private View mStubNoMeal;
    private ProgressBar mLoadingProgress;

    public MealFragment() {

    }

    public static MealFragment newInstance() {
        return new MealFragment();
    }

    @Override
    public void setPresenter(MealContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MealAdapter(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meal, container);

        mStubNoMeal = rootView.findViewById(R.id.stub_no_results);
        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.pb_loading);
        mMealView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mMealView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMealView.setHasFixedSize(true);
        mMealView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void showMeal(Meal meal) {
        mStubNoMeal.setVisibility(View.GONE);
        mMealView.setVisibility(View.VISIBLE);
        mLoadingProgress.setVisibility(View.GONE);
        mAdapter.setMeal(meal);
    }

    @Override
    public void showNoMeal() {
        mStubNoMeal.setVisibility(View.VISIBLE);
        mMealView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mAdapter.setMeal(null);
    }

    @Override
    public void showProgress() {
        mStubNoMeal.setVisibility(View.GONE);
        mMealView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSetupDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .content(getString(R.string.dialog_not_initalized_content))
                .positiveText(R.string.action_go_settings)
                .negativeText(R.string.close)
                .onPositive((dialog1, which) -> {
                    Intent intent = new Intent(getContext(), SearchSchoolActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                })
                .onNegative((dialog1, which) -> {
                    getActivity().finish();
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .build();
        dialog.show();
    }

    @Override
    public void updateTitle(Date date) {
        getActivity().setTitle(Utils.getDateToString(getContext(), date));
    }
}
