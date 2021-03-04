package cf.victorlopez.frasesretrofit.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cf.victorlopez.frasesretrofit.broadcast.BroadcastAlarmFrase;

public class AlarmFraseManager {
    private Context context;
    private final static String HORA_DEFAULT = "08:00";

    public AlarmFraseManager(Context context) {
        this.context = context;
    }

    /**
     * Método que comprueba si existe alguna alarma en el sistema
     * @return true si existe una alarma, false si no existe ninguna
     */
    public boolean hasAlarm(){
        boolean alarmUp = (PendingIntent.getBroadcast(context.getApplicationContext(), 0,
                new Intent(context, BroadcastAlarmFrase.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        return alarmUp;
    }

    /**
     * Método para cancelar una alarma dirigida a BroadcastAlarmFrase
     */
    public void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), BroadcastAlarmFrase.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent,0);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Método para poner una alarma dirigida a BroadcastAlarmFrase
     */
    public void setAlarm(){
        Calendar c = Calendar.getInstance();
        Date date;
        Date dateDefault = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String fecha = prefs.getString("hora", HORA_DEFAULT).trim();
        DateFormat format = new SimpleDateFormat("HH:mm");
        try{
            dateDefault = format.parse(HORA_DEFAULT);
            date = format.parse(fecha);
        }catch (ParseException pee){
            date = dateDefault;
        }
        //Obtener de preferences
        c.setTime(date);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), BroadcastAlarmFrase.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent,0);

        alarmManager.setRepeating(android.app.AlarmManager.RTC, c.getTimeInMillis(), android.app.AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
