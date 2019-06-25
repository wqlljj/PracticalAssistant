package com.example.sx.practicalassistant.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.service.BootReceiver;
import com.example.sx.practicalassistant.utils.RequestPermissionUtils;
import com.example.sx.practicalassistant.view.fragment.AboutFragment;
import com.example.sx.practicalassistant.view.fragment.HelpFragment;
import com.example.sx.practicalassistant.view.fragment.HomeFragment;
import com.example.sx.practicalassistant.view.fragment.SettingFragment;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private LinearLayout drawer_home;
    private RelativeLayout drawer_setting;
    private LinearLayout drawer_about;
    private RelativeLayout drawer_help;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int currentPage;
    private FragmentManager mFragmentManager;
    private HomeFragment homeFragment;
    private HelpFragment helpFragment;
    private SettingFragment settingFragment;
    private AboutFragment aboutFragment;
    public static MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mainActivity=this;
        init();
        setTabSelection(1);
        RequestPermissionUtils.getPermissions(this);
        Intent intent =new Intent(this, BootReceiver.class);
        intent.setAction("com.example.sx.practicalassistant.keepalive");
        sendBroadcast(intent);
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer_home = (LinearLayout) findViewById(R.id.drawer_home);
        drawer_setting = (RelativeLayout) findViewById(R.id.drawer_setting);
        drawer_about = (LinearLayout) findViewById(R.id.drawer_about);
        drawer_help = (RelativeLayout)findViewById(R.id.drawer_help);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        drawer_home.setOnClickListener(this);
        drawer_setting.setOnClickListener(this);
        drawer_about.setOnClickListener(this);
        drawer_help.setOnClickListener(this);
    }
    private void setTabSelection(int i) {
        currentPage = i;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        hideFragments(transaction);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        switch (i) {
            case 1:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment().newInstance();
                    transaction.add(R.id.content_frame, homeFragment);
                } else {
                    transaction.show(homeFragment);
                }
                break;
            case 2:
                if (helpFragment == null) {
                    helpFragment = new HelpFragment().newInstance();
                    transaction.add(R.id.content_frame, helpFragment);
                } else {
                    transaction.show(helpFragment);
                }
                break;
            case 3:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment().newInstance();
                    transaction.add(R.id.content_frame, settingFragment);
                } else {
                    transaction.show(settingFragment);
                }
                break;
            case 4:
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment().newInstance();
                    transaction.add(R.id.content_frame, aboutFragment);
                } else {
                    transaction.show(aboutFragment);
                }
                break;
        }
        transaction.commit();

    }




    /**
     * 将所有的Fragment都置为隐藏状态
     *
     * @param transaction
     *    用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
        if (aboutFragment != null) {
            transaction.hide(aboutFragment);
        }
        if (helpFragment != null) {
            transaction.hide(helpFragment);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_home:
                setTabSelection(1);
                drawer_home.setSelected(true);
                drawer_setting.setSelected(false);
                drawer_about.setSelected(false);
                drawer_help.setSelected(false);

//                mToolbar.setNavigationIcon(R.drawable.ic_choose);
                mToolbar.setTitle(getResources().getString(R.string.app_name));
                break;
            case R.id.drawer_help:
                setTabSelection(2);
                drawer_help.setSelected(true);
                drawer_home.setSelected(false);
                drawer_setting.setSelected(false);
                drawer_about.setSelected(false);


//                mToolbar.setNavigationIcon(R.drawable.ic_choose);
                mToolbar.setTitle(getResources().getString(R.string.helper));
                break;
            case R.id.drawer_setting:
                setTabSelection(3);
                drawer_setting.setSelected(true);
                drawer_home.setSelected(false);
                drawer_about.setSelected(false);
                drawer_help.setSelected(false);

//                mToolbar.setNavigationIcon(R.drawable.ic_back);
                mToolbar.setTitle(getResources().getString(R.string.setting));
                break;
            case R.id.drawer_about:
                setTabSelection(4);
                drawer_about.setSelected(true);
                drawer_home.setSelected(false);
                drawer_setting.setSelected(false);
                drawer_help.setSelected(false);

//                mToolbar.setNavigationIcon(R.drawable.ic_back);
                mToolbar.setTitle(getResources().getString(R.string.about));
                break;
            default:
                break;
        }
        mDrawerLayout.closeDrawers();
    }
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
            if(currentPage!=1){
                onClick(drawer_home);
            }else if((System.currentTimeMillis()-exitTime)>2000){
               Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_SHORT).show();
               exitTime=System.currentTimeMillis();
               } else{
               finish();
               System.exit(0);
               }
            return true;
            }
        return super.onKeyDown(keyCode,event);
    }
}
