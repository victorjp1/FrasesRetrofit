package cf.victorlopez.frasesretrofit.fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cf.victorlopez.frasesretrofit.enums.Modo;
import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.models.Categoria;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategoria extends Fragment {

    private EditText etNombre;
    private Button bAnterior, bSiguiente, bAction;
    private IAPIService apiService;
    private List<Categoria> categorias;
    private Modo modo;
    private int offset;
    private int posicion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categoria,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        categorias = new ArrayList<>();
        offset = 0;
        posicion = 0;
        apiService = RestClient.getInstance(getContext());
        TextView tvTitulo = getActivity().findViewById(R.id.tvTituloFrase);
        etNombre = getActivity().findViewById(R.id.etNombreCategoria);
        bAnterior = getActivity().findViewById(R.id.bAnterior);
        bSiguiente = getActivity().findViewById(R.id.bSiguiente);
        bAction = getActivity().findViewById(R.id.bAction);

        modo = (Modo) getArguments().getSerializable(Modo.CODE_MODE);

        switch (modo){
            case ADD:
                //Cuando el usuario ha pulsado la opción añadir categoria
                bSiguiente.setVisibility(View.INVISIBLE);
                bAnterior.setVisibility(View.INVISIBLE);
                bAction.setText(R.string.botonInsertar);
                tvTitulo.setText(R.string.TituloAddCategoria);
                setListenersAdd();
                break;
            case UPDATE:
                //Cuando el usuario ha pulsado la opción modificar categoria
                obtenerCategoriasOffset(offset);
                bAction.setText(R.string.botonModificar);
                tvTitulo.setText(R.string.TituloModificarCategoria);
                setListenersUpdate();
                break;
            case DELETE:
                //Cuando el usuario ha pulsado la opción eliminar categoria
                obtenerCategoriasOffset(offset);
                bAction.setText(R.string.botonEliminar);
                tvTitulo.setText(R.string.tituloEliminarCategoria);
                setListenersDelete();
        }
    }

    /**
     * Método para asignar el listener y sus acciones del botón añadir
     */
    public void setListenersAdd(){
        bAction.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            if (nombre.length() > 2){
                addCategoria(new Categoria(-1,nombre));
                etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
                etNombre.setText("");
            }else{
                etNombre.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                Toast.makeText(getActivity(), "El nombre debe contener mínimo 3 caracteres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para añadir la categoría
     * @param cat Categoría a añadir
     */
    public void addCategoria(Categoria cat){
        apiService.addCategoria(cat).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getActivity(), "Categoría añadida correctamente", Toast.LENGTH_LONG).show();
                    }else{
                        etNombre.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                        Toast.makeText(getActivity(), "Ya existe una categoría con ese nombre", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido añadir", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se ha podido añadir la categoría", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para inicializar todos los botones de la pantalla Update
     */
    public void setListenersUpdate(){
        setListenerPaginador();
        bAction.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            if (nombre.length() > 2){
                Categoria cat = categorias.get(posicion);
                cat.setNombre(nombre);
                modificarCategoria(cat);
                etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
            }else{
                etNombre.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                Toast.makeText(getActivity(), "El nombre debe contener mínimo 3 caracteres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para modificar una categoría
     * @param cat Categoría a modificar
     */
    public void modificarCategoria(Categoria cat){
        apiService.updateCategoria(cat).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getContext(), "Categoría modificada correctamente", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getContext(), "Ya existe una categoría con ese nombre", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getContext(), "No se ha podido modificar correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido modificar la categoría", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para asignar el listener para los botones de la pantalla Eliminar Cataegoria
     */
    public void setListenersDelete(){
        setListenerPaginador();
        etNombre.setEnabled(false);
        bAction.setOnClickListener(v -> deleteCategoria(categorias.get(posicion)));
    }

    /**
     * Método para borrar una categoría
     * @param cat Categoría a borrar
     */
    public void deleteCategoria(Categoria cat){
        apiService.deleteCategoriaById(cat.getId()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getActivity(), "Se ha eliminado correctamente la categoría", Toast.LENGTH_LONG).show();
                        categorias.remove(posicion);
                        putDatos();
                    }else{
                        Toast.makeText(getActivity(), "No se ha podido eliminar correctamente la categoría", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido eliminar correctamente la categoría", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para crear los listener que controlan los botones atras y siguiente
     */
    public void setListenerPaginador(){
        bSiguiente.setOnClickListener(v -> {
            if (posicion < categorias.size()-1){
                posicion++;
                putDatos();
            }else{
                offset += 10;
                obtenerCategoriasOffset(offset);
            }
            etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        });
        bAnterior.setOnClickListener(v -> {
            if (posicion > 0){
                posicion--;
                putDatos();
            }else{
                Toast.makeText(getContext(), "Estamos en el primer registro!", Toast.LENGTH_LONG).show();
            }
            etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        });
    }


    /**
     * Método que sobrescribimos para resetear el color del campo al volver a este Fragment
     */
    @Override
    public void onResume() {
        super.onResume();
        if (modo == Modo.ADD){
            etNombre.setText("");
        }
        etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Método para poner los datos sobre la posición actual
     */
    public void putDatos(){
        etNombre.setText(categorias.get(posicion).getNombre());
    }

    /**
     * Métoedo para obtener categorías empezando por donde le indiquemos
     * @param offset desde que parte de la lista empieza a leer
     */
    public void obtenerCategoriasOffset(int offset){
        apiService.getCategoriasLimit10(offset).enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(@NonNull Call<List<Categoria>> call, @NonNull Response<List<Categoria>> response) {
                if (response.isSuccessful()){
                    categorias.addAll(response.body());
                    putDatos();
                }else{
                    Toast.makeText(getContext(), "No funciona correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Categoria>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido obtener categorias", Toast.LENGTH_LONG).show();
            }
        });
    }
}
