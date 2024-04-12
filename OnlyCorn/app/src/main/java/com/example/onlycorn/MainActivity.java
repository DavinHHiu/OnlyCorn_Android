package com.example.onlycorn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.onlycorn.fragment.HomeFragment;
import com.example.onlycorn.fragment.PostFragment;
import com.example.onlycorn.fragment.ProfileFragment;
import com.example.onlycorn.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Menu menu = bottomNav.getMenu();
                MenuItem item1 = menu.getItem(0);
                item1.setIcon(R.drawable.icon_home_outline);
                MenuItem item2 = menu.getItem(1);
                item2.setIcon(R.drawable.icon_search_outline);
                MenuItem item3 = menu.getItem(2);
                item3.setIcon(R.drawable.icon_add_outline);
                MenuItem item4 = menu.getItem(3);
                item4.setIcon(R.drawable.icon_profile_outline);

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
                } else if (id == R.id.navProfile) {
                    item.setIcon(R.drawable.icon_profile);
                    selectedFragment = new ProfileFragment();
                }
                loadFragment(selectedFragment);
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }
}