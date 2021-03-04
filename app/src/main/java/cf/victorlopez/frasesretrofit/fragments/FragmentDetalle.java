package cf.victorlopez.frasesretrofit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.models.Frase;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actúa como detalle de las frases y es reciclada para la pantalla de frase del día
 * Necesita una frase como argumento o null
 */
public class FragmentDetalle extends Fragment {
    private TextView tvAutor, tvCategoria, tvContenido, tvFecha;
    public static final String FRASE = "cf.victorlopez.frase";
    private Frase frase;
    private IAPIService apiService;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_frase,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvAutor = getActivity().findViewById(R.id.tvAutorDetalle);
        apiService = RestClient.getInstance(getActivity());
        tvCategoria = getActivity().findViewById(R.id.tvCategoriaDetalle);
        tvContenido = getActivity().findViewById(R.id.tvContenidoDetalle);
        tvFecha = getActivity().findViewById(R.id.tvFechaDetalle);
        TextView tvTitulo = getActivity().findViewById(R.id.tvTituloDetalle);

        frase = (Frase)getArguments().getSerializable(FRASE);
        if (frase == null){
            getFraseDelDia();//Realiza una llamada a putDatos
            tvTitulo.setText("Frase del día");
        }else{
            putDatos(frase);
        }
    }

    /**
     * Método para poner los datos
     * @param frase
     */
    public void putDatos(Frase frase){
        tvFecha.setText(" " + frase.getFechaProgramada());
        tvContenido.setText(frase.getTexto());
        tvCategoria.setText(" " + frase.getCategoria().getNombre());
        tvAutor.setText(frase.getAutor().getNombre());
    }
    /**
     * Método para obtener la frase del día, la guardará en la variable de la clase y mostrará sus datos
     */
    public void getFraseDelDia() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM--dd");
        String date = format.format(new Date());
        apiService.getFraseDelDia(date).enqueue(new Callback<Frase>() {
            @Override
            public void onResponse(@NonNull Call<Frase> call, @NonNull Response<Frase> response) {
                if(response.isSuccessful()) {
                    frase = response.body();
                    putDatos(frase);
                }else{
                    Toast.makeText(getActivity(), "No se ha podido obtener la frase del día", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Frase> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
