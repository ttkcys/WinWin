package com.yusudhanwinwin.winwin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.iammert.library.readablebottombar.ReadableBottomBar;
import com.yusudhanwinwin.winwin.Fragments.HomeFragment;
import com.yusudhanwinwin.winwin.Fragments.ProfileFragment;
import com.yusudhanwinwin.winwin.Fragments.RewardsFragment;
import com.yusudhanwinwin.winwin.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,new HomeFragment());
        transaction.commit();

        binding.readableBottomBar.setOnItemSelectListener(new ReadableBottomBar.ItemSelectListener() {
            @Override
            public void onItemSelected(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (i){
                    case 0:
                        transaction.replace(R.id.container, new HomeFragment());
                        break;
                    case 1:
                        transaction.replace(R.id.container, new RewardsFragment());
                        break;
                    case 2:
                        transaction.replace(R.id.container, new ProfileFragment());
                        break;
                }

                transaction.commit();
            }
        });
    }
}