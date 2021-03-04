package cf.victorlopez.frasesretrofit.fragments;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cf.victorlopez.frasesretrofit.enums.Modo;
import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.enums.ResultadoAdd;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.models.Autor;
import cf.victorlopez.frasesretrofit.models.Categoria;
import cf.victorlopez.frasesretrofit.models.Frase;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentFrase extends Fragment {
    private EditText etFecha, etContenido;
    private TextView tvTitulo;
    private Spinner sAutor, sCategoria;
    private Button bAnterior, bSiguiente, bAction;
    private IAPIService apiService;
    private Modo modo;
    private List<Categoria> categorias;
    private List<Autor> autores;
    private List<Frase> frases;
    private int offset;
    private int posicion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frase,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        frases = new ArrayList<>();
        offset = 0;
        posicion = 0;
        apiService = RestClient.getInstance(getContext());
        obtenerComponentes();
        sAutor.setEnabled(false);
        sCategoria.setEnabled(false);

        modo = (Modo) getArguments().getSerializable(Modo.CODE_MODE);

        getCategorias();
        getAutores();

        switch (modo){
            case ADD:
                //Cuando el usuario ha pulsado la opción añadir frase
                bSiguiente.setVisibility(View.INVISIBLE);
                bAnterior.setVisibility(View.INVISIBLE);
                bAction.setText(R.string.botonInsertar);
                tvTitulo.setText(R.string.TituloAddFrase);
                setListenersAdd();
                break;
            case UPDATE:
                //Cuando el usuario ha pulsado la opción modificar frase
                obtenerFrasesOffset(offset);
                bAction.setText(R.string.botonModificar);
                tvTitulo.setText(R.string.TituloModificarFrase);
                setListenersUpdate();
                break;
            case DELETE:
                //Cuando el usuario ha pulsado la opción eliminar frase
                obtenerFrasesOffset(offset);
                bAction.setText(R.string.botonEliminar);
                tvTitulo.setText(R.string.tituloEliminarFrase);
                setListenersDelete();
                break;
        }

    }

    /**
     * Método para obtener los componentes
     */
    public void obtenerComponentes(){
        tvTitulo = getActivity().findViewById(R.id.tvTituloFrase);
        etFecha = getActivity().findViewById(R.id.etFechaProgramadaFrase);
        etContenido = getActivity().findViewById(R.id.etContenidoFrase);
        sAutor = getActivity().findViewById(R.id.sAutorFrase);
        sCategoria = getActivity().findViewById(R.id.sCategoriasFrase);
        bAnterior = getActivity().findViewById(R.id.bAnterior);
        bSiguiente = getActivity().findViewById(R.id.bSiguiente);
        bAction = getActivity().findViewById(R.id.bAction);
    }

    /**
     * Método para asignar el comportamiento de los botones en la pantalla de añadir
     */
    public void setListenersAdd(){
        bAction.setOnClickListener(v -> {
            etFecha.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            String fecha = etFecha.getText().toString();
            String contenido = etContenido.getText().toString();
            //La posición del Spinner coincide con la posición del array de objetos
            Categoria cat = categorias.get(sCategoria.getSelectedItemPosition());
            Autor autor = autores.get(sAutor.getSelectedItemPosition());

            boolean validado = validarDatos(contenido, fecha);

            if (validado){
                Frase frase = new Frase(contenido, fecha, autor, cat);
                Log.e("Frase para añadir", frase.toString());
                addFrase(frase);
            }else{
                Toast.makeText(getActivity(), "Campos no correctos, revísalos por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para añadir la frase
     * @param frase Frase a añadir
     */
    public void addFrase(Frase frase){
        apiService.addFrase(frase).enqueue(new Callback<ResultadoAdd>() {
            @Override
            public void onResponse(@NonNull Call<ResultadoAdd> call, @NonNull Response<ResultadoAdd> response) {
                if (response.isSuccessful()){
                    switch (response.body()){
                        case OK:
                            Toast.makeText(getActivity(), "Frase añadida correctamente", Toast.LENGTH_LONG).show();
                            resetCampos();
                            break;
                        case FECHA_EXISTENTE:
                            etFecha.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                            Toast.makeText(getActivity(), "Ya existe una frase con esa fecha programada", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido añadir correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultadoAdd> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se ha podido añadir la frase", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para asignar el comportamiento de los botones de la pantalla de Modificar
     */
    public void setListenersUpdate(){
        setListenerPaginador();
        bAction.setOnClickListener(v -> {
            Frase frase = frases.get(posicion);
            Autor autor = autores.get(sAutor.getSelectedItemPosition());
            Categoria cat = categorias.get(sCategoria.getSelectedItemPosition());
            String fecha = etFecha.getText().toString();
            String contenido = etContenido.getText().toString();

            boolean validado = validarDatos(contenido, fecha);

            if (validado){
                Log.e("Frase para añadir", frase.toString());
                frase.setAutor(autor);
                frase.setCategoria(cat);
                frase.setFechaProgramada(fecha);
                frase.setTexto(contenido);
                modificarFrase(frase);
                resetCampos();
            }else{
                Toast.makeText(getActivity(), "Campos no correctos, revísalos por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para modificar una frase
     * @param frase Frase a modificar
     */
    public void modificarFrase(Frase frase){
        apiService.updateFrase(frase).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getContext(), "Frase modificada correctamente", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getContext(), "Ya existe una frase con esa fecha programada", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getContext(), "No se ha podido modificar correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido modificar la frase", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método que asigna listeners necesarios para la pantalla de Eliminar
     */
    public void setListenersDelete(){
        setListenerPaginador();
        bAction.setOnClickListener(v -> {
            deleteFrase(frases.get(posicion));
        });
    }

    /**
     * Método para eliminar una frase
     * @param frase Frase a eliminar
     */
    public void deleteFrase(Frase frase){
        apiService.deleteFraseById(frase.getId()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getActivity(), "Se ha eliminado correctamente la frase", Toast.LENGTH_LONG).show();
                        frases.remove(posicion);
                        putDatos();
                    }else{
                        Toast.makeText(getActivity(), "No se ha podido eliminar correctamente la frase", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido eliminar correctamente la frase", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se ha podido eliminar correctamente la frase", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para asignar los listeners a los botones que nos permiten paginar (Anterior/Siguiente)
     */
    public void setListenerPaginador(){
        bSiguiente.setOnClickListener(v -> {
            if (posicion < frases.size()-1){
                posicion++;
                putDatos();
            }else{
                offset += 10;
                obtenerFrasesOffset(offset);
            }
            resetCampos();
        });
        bAnterior.setOnClickListener(v -> {
            if (posicion > 0){
                posicion--;
                putDatos();
            }else{
                Toast.makeText(getContext(), "Estamos en el primer registro!", Toast.LENGTH_LONG).show();
            }
            resetCampos();
        });
    }

    /**
     * Método para validar datos introducidos por el usuario
     * @param contenido Contenido de la frase (texto)
     * @param fecha Fecha programada para la frase
     * @return true si los datos son correctos, false si no lo son
     */
    public boolean validarDatos(String contenido, String fecha){
        boolean validado = true;
        if (contenido.length() > 3){
            etContenido.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        }else{
            validado = false;
            etContenido.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        }

        try {
            //Formato de fecha (día/mes/año)
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
            //Comprobación de la fecha
            formatoFecha.parse(fecha);
            etFecha.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        } catch (ParseException e) {
            //Si la fecha no es correcta, pasará por aquí
            etFecha.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            validado = false;
        }
        return validado;
    }

    /**
     * Nos interesa resetear los campos si
     */
    @Override
    public void onResume() {
        super.onResume();
        resetCampos();
    }

    /**
     * Método para vaciar los campos
     */
    public void vaciarCampos(){
        etFecha.setText("");
        etContenido.setText("");
        sAutor.setSelection(0);
        sCategoria.setSelection(0);
    }

    /**
     * Método para resetear los colores de los campos y si nos encontramos en pantallas
     * en las que se deban vaciar los datos también lo haremos
     */
    public void resetCampos(){
        if (modo == Modo.ADD){
            vaciarCampos();
        }
        etFecha.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        etContenido.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Método para mostrar los datos de la frase según la posición
     */
    public void putDatos(){
        Frase frase = frases.get(posicion);
        etFecha.setText(frase.getFechaProgramada());
        etContenido.setText(frase.getTexto());
        selectCategoria(frase.getCategoria());
        selectAutor(frase.getAutor());
    }

    /**
     * Método para poner los nombres de los autores en el Spinner
     * @param datos Autores a colocar
     */
    public void putAutoresSpinner(List<Autor> datos){
        String[] autoresString = new String[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            autoresString[i] = datos.get(i).getNombre();
        }
        sAutor.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, autoresString));
        sAutor.setEnabled(true);
    }
    /**
     * Método para poner los nombres de las categorías en el Spinner
     * @param datos Categorías a colocar
     */
    public void putCategoriasSpinner(List<Categoria> datos){
        String[] categoriasString = new String[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            categoriasString[i] = datos.get(i).getNombre();
        }
        sCategoria.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, categoriasString));
        sCategoria.setEnabled(true);
    }

    /**
     * Método para obtener las categorías del server
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
     * Método para obtener los autores del server
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
     * Método para seleccionar categoría en el Spinner
     * @param cat categoria a seleccionar
     */
    public void selectCategoria(Categoria cat){
        int posicion = 0;
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getId() == cat.getId()){
                posicion = i;
                break;
            }
        }
        sCategoria.setSelection(posicion);
    }
    /**
     * Método para seleccionar el autor en el Spinner
     * @param autor Autor a seleccionar
     */
    public void selectAutor(Autor autor){
        int posicion = 0;
        for (int i = 0; i < autores.size(); i++) {
            if (autores.get(i).getId() == autor.getId()){
                posicion = i;
                break;
            }
        }
        sAutor.setSelection(posicion);
    }

    /**
     * Obtener frases de 10 en 10 desde donde le indiquemos
     * @param offset Desde que posición empieza a contar
     */
    public void obtenerFrasesOffset(int offset){
        apiService.getFrasesLimit10(offset).enqueue(new Callback<List<Frase>>() {
            @Override
            public void onResponse(@NonNull Call<List<Frase>> call, @NonNull Response<List<Frase>> response) {
                if (response.isSuccessful()){
                    frases.addAll(response.body());
                    putDatos();
                }else{
                    Toast.makeText(getContext(), "No funciona correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Frase>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido obtener frases", Toast.LENGTH_LONG).show();
            }
        });
    }
}
