package com.demo.savemymoney.main;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.demo.savemymoney.goal.GoalFragment;
import com.demo.savemymoney.R;
import com.demo.savemymoney.common.BaseActivity;
import com.demo.savemymoney.graphics.GraphicsActivity;
import com.demo.savemymoney.login.LoginActivity;
import com.demo.savemymoney.monto.MontoFragment;
import com.demo.savemymoney.service.ClockNotifyService;

import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainActivityPresenter.View {

    MainActivityPresenter presenter;
    private Menu menu;

   Intent serviceIntent;
    ClockNotifyService clockNotifyService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

              serviceIntent = new Intent(this,ClockNotifyService.class);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        presenter = new MainActivityPresenter(this, this);
        navigateFragment(MainFragment.newInstance());
    }


    private void setUserInformation(NavigationView navigationView) {
        AppCompatTextView userName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        AppCompatTextView userMail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_mail);
        userName.setText(mAuth.getCurrentUser().getDisplayName());
        userMail.setText(mAuth.getCurrentUser().getEmail());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.distributed) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            configureNavigationToolbar(R.string.main_income_title);
            navigateFragment(MainFragment.newInstance());
        } else if (id == R.id.nav_incomes) {
            configureNavigationToolbar(R.string.income_update_title);
            navigateFragment(new MontoFragment());
        } else if (id == R.id.nav_reports) {
            goTo(GraphicsActivity.class);
        } else if (id == R.id.nav_goal) {
            configureNavigationToolbar(R.string.goal_title);
            navigateFragment(new GoalFragment());
        } else if (id == R.id.nav_exit) {
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void configureNavigationToolbar(int title) {
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorPrimary));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void navigateFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isUserSignedIn())
            goTo(LoginActivity.class);

        presenter.checkIfHasIncome();
        NavigationView navigationView = findViewById(R.id.nav_view);
        setUserInformation(navigationView);
    }

    @Override
    protected void onResume() {
        super.onResume();
     startService(serviceIntent);
     bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }
   private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            clockNotifyService = ((ClockNotifyService.ClockBinder) iBinder).getClockBinder();


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            clockNotifyService = null;
        }
    };



    public void signOut() {
        mAuth.signOut();
        goTo(LoginActivity.class);
    }

    @Override
    public void showProgress(int resId) {
        showProgressDialog(resId);
    }

    @Override
    public void hideProgress() {
        hideProgressDialog();
    }

    @Override
    public void showError(String message) {
        showErrorMessage(message);
    }

    @Override
    public void notifyToRegisterIncome() {
        SweetAlertDialog alert = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.success_title))
                .setContentText(getString(R.string.main_income_not_exist_message))
                .setConfirmText(getString(R.string.main_income_confirm_register))
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    navigateFragment(new MontoFragment());
                });
        alert.setCancelable(false);
        alert.show();
    }

    public void changeDistributed(String title) {
        menu.getItem(0).setTitle(title);
    }
}
