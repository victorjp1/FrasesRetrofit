package cf.victorlopez.frasesretrofit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cf.victorlopez.frasesretrofit.adapters.AdapterFrases;
import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.interfaces.IFrasesListener;
import cf.victorlopez.frasesretrofit.models.Autor;
import cf.victorlopez.frasesretrofit.models.Categoria;
import cf.victorlopez.frasesretrofit.models.Frase;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentConsultar extends Fragment implements IFrasesListener {
    public static final String BUSCAR_POR_AUTOR = "autor";
    public static final String BUSCAR_POR_CATEGORIA = "categoria";
    public static final String BUSCAR_FRASES = "frases";
    public static final String BUSCAR_POR = "modo";
    private IAPIService apiService;
    private String modo;
    private List<Frase> frases;
    private List<Categoria> categorias;
    private Spinner spinner;
    private AdapterFrases adapter;
    private List<Autor> autores;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consultar,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        modo = getArguments().getString(BUSCAR_POR);

        apiService = RestClient.getInstance(getActivity().getApplicationContext());
        TextView tvTitulo = getActivity().findViewById(R.id.tvTituloConsultarPor);
        spinner = getActivity().findViewById(R.id.sCategoriaAutor);
        spinner.setEnabled(false);

        RecyclerView rvListado = getActivity().findViewById(R.id.rvFrases);
        adapter = new AdapterFrases(new ArrayList<>(), this);
        rvListado.setAdapter(adapter);
        rvListado.setHasFixedSize(true);
        LinearLayoutManager lyManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvListado.setLayoutManager(lyManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),lyManager.getOrientation());
        rvListado.addItemDecoration(dividerItemDecoration);

        switch (modo){
            //Si estamos en la ventana de buscar por categorías obtenemos las categorías
            case BUSCAR_POR_CATEGORIA:
                tvTitulo.setText("Consultar frases por " + modo);
                getCategorias();
                break;
            //Si estamos en la ventana de buscar por autor buscamos los autores
            case BUSCAR_POR_AUTOR:
                tvTitulo.setText("Consultar frases por " + modo);
                getAutores();
                break;
            //Si estamos en la ventana de frases no buscamos nada, simplemente buscamos todas las frases
            case BUSCAR_FRASES:
                tvTitulo.setText("Consultar frases");
                spinner.setVisibility(View.GONE);
                getFrases();
                break;
        }
        if (modo.equals(BUSCAR_POR_CATEGORIA)){
        }else if(modo.equals(BUSCAR_POR_AUTOR)){
            getAutores();
        }
        //Al haber realizado un cambio en el Spinner refrescamos las frases
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (modo.equals(BUSCAR_POR_CATEGORIA)){
                    Categoria cat = categorias.get(spinner.getSelectedItemPosition());
                    getFrasesByCategoria(cat);
                }else if(modo.equals(BUSCAR_POR_AUTOR)){
                    Autor autor = autores.get(spinner.getSelectedItemPosition());
                    getFrasesByAutor(autor);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getActivity(), "Selecciona una opción", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para obtener las frases y mostrarlas en el RecyclerView
     */
    public void getFrases() {
        apiService.getFrases().enqueue(new Callback<List<Frase>>() {
            @Override
            public void onResponse(@NonNull Call<List<Frase>> call, @NonNull Response<List<Frase>> response) {
                if(response.isSuccessful()) {
                    frases = response.body();
                    adapter.setFrases(frases);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Frase>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para obtener las frases por autor
     * @param autor Autor al que buscar
     */
    public void getFrasesByAutor(Autor autor){
        apiService.getFrasesByAutor(autor.getId()).enqueue(new Callback<List<Frase>>() {
            @Override
            public void onResponse(@NonNull Call<List<Frase>> call, @NonNull Response<List<Frase>> response) {
                if (response.isSuccessful()){
                    frases = response.body();
                    adapter.setFrases(frases);
                }else{
                    Toast.makeText(getActivity(), "No se puede encontrar frases con ese autor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Frase>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se puede encontrar frases con ese autor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para obtener las frases por categoría
     * @param cat Categoría a buscar
     */
    public void getFrasesByCategoria(Categoria cat){
        apiService.getFraseByCategoria(cat.getId()).enqueue(new Callback<List<Frase>>() {
            @Override
            public void onResponse(@NonNull Call<List<Frase>> call, @NonNull Response<List<Frase>> response) {
                if (response.isSuccessful()){
                    frases = response.body();
                    adapter.setFrases(frases);
                }else{
                    Toast.makeText(getActivity(), "No se puede encontrar frases con esa categoria", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Frase>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se puede encontrar frases con esa categoria", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para obtener las categorías y colocarlas en el Spinner
     */
    public void getCategorias() {
        apiService.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(@NonNull Call<List<Categoria>> call, @NonNull Response<List<Categoria>> response) {
                if(response.isSuccessful()) {
                    categorias = response.body();
                    putCategoriasSpinner(categorias);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Categoria>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    /**
     * Método para obtener los autores y colocarlos en el Spinner
     */
    public void getAutores() {
        apiService.getAutores().enqueue(new Callback<List<Autor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Autor>> call, @NonNull Response<List<Autor>> response) {
                if(response.isSuccessful()) {
                    autores = response.body();
                    putAutoresSpinner(autores);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Autor>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para colocar autores en el Spinner (solo el nombre)
     */
    public void putAutoresSpinner(List<Autor> datos){
        String[] autoresString = new String[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            autoresString[i] = datos.get(i).getNombre();
        }
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, autoresString));
        spinner.setEnabled(true);
    }

    /**
     * Método para obtener las categorías en el Spinner (solo el nombre)
     * @param datos datos que colocar al spinner
     */
    public void putCategoriasSpinner(List<Categoria> datos){
        String[] categoriasString = new String[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            categoriasString[i] = datos.get(i).getNombre();
        }
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, categoriasString));
        spinner.setEnabled(true);
    }

    /**
     * Evento que se dispara cuando seleccionamos una clase
     * @param frase Frase seleccionada
     */
    @Override
    public void onFraseSeleccionada(Frase frase) {
        FragmentDetalle fragment = new FragmentDetalle();
        Bundle args = new Bundle();
        args.putSerializable(FragmentDetalle.FRASE, frase);
        fragment.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("lista").commit();
    }

}
