package com.jatin.dropit.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.jatin.dropit.R;
import com.jatin.dropit.adapter.PaginationAdapterCallback;
import com.jatin.dropit.adapter.UserAdapter;
import com.jatin.dropit.api.UsersApi;
import com.jatin.dropit.api.UsersService;
import com.jatin.dropit.model.Response;
import com.jatin.dropit.model.UsersItem;
import com.jatin.dropit.util.PaginationScrollListener;
import java.util.List;
import java.util.concurrent.TimeoutException;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends BaseAnimationActivity implements PaginationAdapterCallback {


    UserAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private UsersService mUsersService;

    private int currentPage = 10;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);

        adapter = new UserAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 10;

                loadNextPage();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //init service and load data
        mUsersService = UsersApi.getClient().create(UsersService.class);

        loadFirstPage();

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFirstPage();
            }
        });

    }

    private void loadFirstPage() {
        hideErrorView();


        getResponse().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(final Call<Response> call, final retrofit2.Response<Response> response) {
                hideErrorView();

                List<UsersItem> results = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                adapter.addAll(results);

                if (hasMoreData(response)){
                    adapter.addLoadingFooter();
                }
                else {
                    isLastPage = true;
                }

            }

            @Override
            public void onFailure(final Call<Response> call, final Throwable t) {

            }
        });

    }

    private List<UsersItem> fetchResults(final retrofit2.Response<Response> response) {
        Response dataResponse = response.body();
        return dataResponse.getData().getUsers();
    }

    private boolean hasMoreData(final retrofit2.Response<Response> response){
        Response dataResponse = response.body();
        return dataResponse.getData().isHasMore();
    }

    private Call<Response> getResponse() {
        return mUsersService.getResponse(
                currentPage,
                10
        );
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    private void loadNextPage() {

        getResponse().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<UsersItem> results = fetchResults(response);
                adapter.addAll(results);

                if (hasMoreData(response)){
                    adapter.addLoadingFooter();
                }
                else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap back again to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
