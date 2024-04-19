package com.example.onlycorn.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.onlycorn.R;
import com.example.onlycorn.fragments.HomeFragment;
import com.example.onlycorn.fragments.PostFragment;
import com.example.onlycorn.fragments.ProfileFragment;
import com.example.onlycorn.fragments.SearchFragment;
import com.example.onlycorn.fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        preSetupPage();

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                resetIconBottomNav();
                setUpPage(item);
                return true;
            }
        });
    }

    private void preSetupPage() {
        Menu menu = bottomNav.getMenu();
        MenuItem item1 = menu.getItem(0);
        item1.setIcon(R.drawable.icon_home);

        loadFragment(new HomeFragment());
    }

    private void setUpPage(MenuItem item) {
        Fragment selectedFragment = null;
        int id = item.getItemId();
        if (id == R.id.navHome) {
            item.setIcon(R.drawable.icon_home);
            selectedFragment = new HomeFragment();
        } else if (id == R.id.navSearch) {
            item.setIcon(R.drawable.icon_search);
            selectedFragment = new SearchFragment();
        } else if (id == R.id.navAdd) {
            item.setIcon(R.drawable.icon_add);
            selectedFragment = new PostFragment();
        } else if (id == R.id.navUser) {
            item.setIcon(R.drawable.icon_users);
            selectedFragment = new UserFragment();
        } else if (id == R.id.navProfile) {
            item.setIcon(R.drawable.icon_profile);
            selectedFragment = new ProfileFragment(getApplicationContext());
        }
        loadFragment(selectedFragment);
    }

    private void resetIconBottomNav() {
        Menu menu = bottomNav.getMenu();
        MenuItem item1 = menu.getItem(0);
        item1.setIcon(R.drawable.icon_home_outline);
        MenuItem item2 = menu.getItem(1);
        item2.setIcon(R.drawable.icon_search_outline);
        MenuItem item3 = menu.getItem(2);
        item3.setIcon(R.drawable.icon_add_outline);
        MenuItem item4 = menu.getItem(3);
        item4.setIcon(R.drawable.icon_users_outline);
        MenuItem item5 = menu.getItem(4);
        item5.setIcon(R.drawable.icon_profile_outline);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }
}