package fcu.app.distributionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout layout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
        測試註冊功能用的
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);

         */

        FirebaseApp.initializeApp(this);
        layout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);//toolbar套用樣式
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);//toolbar點擊監聽

        setSupportActionBar(toolbar);
        //可用於toolbar及側滑選單綁定(抽屜式)
        toggle = new ActionBarDrawerToggle(
                this,layout,toolbar, R.string.menu_open, R.string.menu_close);
        // ✅ 設定漢堡圖標的顏色
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white)); // 或用你想要的顏色
        layout.addDrawerListener(toggle); // 設定監聽
        toggle.syncState();               // 同步狀態（讓三條線 icon 正確顯示）

        //Fragment accountsFragment = AccountsFragment.newInstance("", "");
        Fragment exchangeFragment = ExchangeRateFragment.newInstance();
        Fragment friendsFragment = FriendsFragment.newInstance();
        Fragment groupsFragment = GroupsFragment.newInstance("", "");
        Fragment suggestionFragment = new SuggestionFragment();

        setCurrentFragment(groupsFragment);

        //側滑選單項目點擊監聽
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //判斷按下哪個按鈕
                int id = item.getItemId();
                if (id == R.id.action_exchange) {
                    Toast.makeText(MainActivity.this, "匯率查詢", Toast.LENGTH_SHORT).show();
                    setCurrentFragment(exchangeFragment);
                    layout.closeDrawer(GravityCompat.START); // <-- 加這行關閉抽屜
                    return true;
                } else if (id == R.id.action_groups) {
                    Toast.makeText(MainActivity.this, "群組", Toast.LENGTH_SHORT).show();
                    Fragment accountsFragment = AccountsFragment.newInstance("", "chat");
                    setCurrentFragment(groupsFragment);
                    layout.closeDrawer(GravityCompat.START); // <-- 加這行關閉抽屜
                    return true;
                } else if (id == R.id.action_friends) {
                    Toast.makeText(MainActivity.this, "好友", Toast.LENGTH_SHORT).show();
                    setCurrentFragment(friendsFragment);
                    layout.closeDrawer(GravityCompat.START); // <-- 加這行關閉抽屜
                    return true;
                } else if (id == R.id.action_suggestion) {
                    Toast.makeText(MainActivity.this, "旅遊建議", Toast.LENGTH_SHORT).show();
                    setCurrentFragment(suggestionFragment);
                    layout.closeDrawer(GravityCompat.START); // <-- 加這行關閉抽屜
                    return true;
                } else if (id == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();  // <-- 真正執行登出1
                    Toast.makeText(MainActivity.this, "登出成功", Toast.LENGTH_SHORT).show();
                    layout.closeDrawer(GravityCompat.START); // <-- 加這行關閉抽屜
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
    //複寫此方法解析toolbar menu樣式

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater(); // 建立MenuInflater
        inflater.inflate(R.menu.toolbar_menu, menu); // 解析 toolbar_menu.xml 並加到 menu 上
        return super.onCreateOptionsMenu(menu);
    }
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }
    public void switchToFriendsFragment() {
        Fragment friendsFragment = FriendsFragment.newInstance();
        setCurrentFragment(friendsFragment);
    }
    public void switchToGroupsFragment() {
        Fragment groupsFragment = GroupsFragment.newInstance("", "");
        setCurrentFragment(groupsFragment);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main); // 這裡要對 fragment 的 id

        if (currentFragment instanceof SuggestionFragment) {
            SuggestionFragment suggestionFragment = (SuggestionFragment) currentFragment;
            if (suggestionFragment.canGoBack()) {
                suggestionFragment.goBack();
                return; // 中斷，不再執行 super
            }
        }
        super.onBackPressed(); // 沒有特殊處理時才讓系統處理返回
    }
}