package cf.victorlopez.frasesretrofit.broadcast;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import cf.victorlopez.frasesretrofit.activities.MainActivity;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.models.Frase;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BroadcastAlarmFrase extends BroadcastReceiver {
    public static final int ID_ALERTA_NOTIFICACION = 12;
    private Context context;
    private IAPIService apiService;
    private static final String CHANNEL_ID="CH_01";
    @Override
    public void onReceive(Context context, Intent intent) {
        apiService = RestClient.getInstance(context);
        this.context = context;
        getFraseDelDia();
    }

    /**
     * Método para obtener la frase del día para poder mostrar crear la notificación
     */
    public void getFraseDelDia() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM--dd");
        String date = format.format(new Date());
        apiService.getFraseDelDia(date).enqueue(new Callback<Frase>() {
            @Override
            public void onResponse(@NonNull Call<Frase> call, @NonNull Response<Frase> response) {
                if(response.isSuccessful()) {
                    Frase frase = response.body();
                    crearNotificacion(frase);
                }else{
                    Toast.makeText(context, "No se ha podido obtener la frase del día", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Frase> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para crear una notificación de la frase del día
     * @param frase Frase que contendrá la notificación
     */
    private void crearNotificacion(Frase frase){
        crearCanalNotificacion();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Frase del dia!!")
                .setContentText(frase.getTexto())
                .setContentInfo(frase.getAutor().getNombre());
        //Creamos el PendingIntent
        Intent intent = new Intent (context, MainActivity.class);
        intent.putExtra("frase", "frase chula");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Asignamos el PendingIntent que será ejecutado al pulsar sobre la notificación
        mBuilder.setContentIntent(pendingIntent);
        //Finalmente mostrarmos la notificación
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_ALERTA_NOTIFICACION, mBuilder.build());

    }

    /**
     *  Examinamos la versión y si es posible se creará un canal de notificación
     */
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Mi canal";
            String descripcion = "Mi canal de notificación ";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            channel.setDescription(descripcion);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
