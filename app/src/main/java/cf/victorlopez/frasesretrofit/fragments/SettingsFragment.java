package cf.victorlopez.frasesretrofit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.alarms.AlarmFraseManager;
import cf.victorlopez.frasesretrofit.rest.RestClient;

public class SettingsFragment extends PreferenceFragmentCompat {

    private EditTextPreference hora;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey);
        hora = findPreference("hora");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //Si cambian las preferencias...
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            switch (key){
                //Si cambia la hora actualizamos la alarma
                case "hora":
                    AlarmFraseManager alarmManager = new AlarmFraseManager(getContext());
                    if (alarmManager.hasAlarm()){
                        alarmManager.cancelAlarm();
                    }
                    alarmManager.setAlarm();
                    break;
                //Si cambia la IP o puerto volvemos a generar la conexión
                case "ip":
                case "puerto":
                    RestClient.rebuild(getContext());
                    break;
                case "notificacion":
                    AlarmFraseManager alarmFraseManager = new AlarmFraseManager(getContext());
                    //Si cambia a notificar true habilitaremos el campo de hora
                    //y volveremos a iniciar la alarma
                    if (sharedPreferences.getBoolean(key, true)){
                        hora.setEnabled(true);
                        alarmFraseManager.setAlarm();
                    }else{
                        //Si cambia a false, deshabilitamos el campo de hora
                        //y además desactivamos la alarma
                        hora.setEnabled(false);
                        if (alarmFraseManager.hasAlarm()){
                            alarmFraseManager.cancelAlarm();
                        }
                    }
                    break;
            }
        });
    }
}
