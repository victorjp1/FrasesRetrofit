package cf.victorlopez.frasesretrofit.rest;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import cf.victorlopez.frasesretrofit.interfaces.IAPIService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static IAPIService instance;

    /** Lo hacemos privado para evitar que se puedan crear instancias de esta forma */
    private RestClient() {

    }

    /**
     * Método para obtener una instancia del IAPIService
     * @param applicationContext Contexto de la aplicación
     *                           Necesario para acceder a preferencias
     * @return Instancia de IAPIService
     */
    public static synchronized IAPIService getInstance(Context applicationContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String url ="http://" + prefs.getString("ip", "192.168.0.105");
        url += ":" + prefs.getString("puerto", "8080");
        if(instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(IAPIService.class);
        }
        return instance;
    }

    /**
     * Método para volver a contruir la instancia, por si se modifica la IP o puerto
     * @param context Contexto de la aplicación
     */
    public static synchronized void rebuild(Context context){
        instance = null;
        getInstance(context);
    }
}
