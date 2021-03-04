package cf.victorlopez.frasesretrofit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import cf.victorlopez.frasesretrofit.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Reemplazamos el fragment actual por el de preferencias
        SettingsFragment sFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, sFragment)
                .commit();
    }
}
