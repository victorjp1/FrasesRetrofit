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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cf.victorlopez.frasesretrofit.enums.Modo;
import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.interfaces.IAPIService;
import cf.victorlopez.frasesretrofit.models.Autor;
import cf.victorlopez.frasesretrofit.rest.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAutor extends Fragment {

    private Button bAnterior, bSiguiente, bAction;
    private TextView tvTitulo;
    private IAPIService apiService;
    public Modo modo;
    private List<Autor> autores;
    private int offset;
    private int posicion;
    private EditText etNombre, etFechaNacimiento, etFechaMuerte, etProfesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_autor,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        offset = 0;
        posicion = 0;
        autores = new ArrayList<>();
        apiService = RestClient.getInstance(getContext());
        obtenerComponentes();

        modo = (Modo) getArguments().getSerializable(Modo.CODE_MODE);

        switch (modo){
            //Cuando el usuario ha pulsado la opción añadir autor
            case ADD:
                bSiguiente.setVisibility(View.INVISIBLE);
                bAnterior.setVisibility(View.INVISIBLE);
                bAction.setText(R.string.botonInsertar);
                tvTitulo.setText(R.string.tituloAddAutor);
                setListenersAdd();
                break;
            //Cuando el usuario ha pulsado la opción modificar autor
            case UPDATE:
                obtenerAutoresOffset(offset);
                bAction.setText(R.string.botonModificar);
                tvTitulo.setText(R.string.TituloModificarAutor);
                setListenersUpdate();
                break;
            //Cuando el usuario ha pulsado la opción delete autor
            case DELETE:
                obtenerAutoresOffset(offset);
                bAction.setText(R.string.botonEliminar);
                tvTitulo.setText(R.string.tituloEliminarAutor);
                setListenersDelete();
        }

    }

    /**
     * Método para asignar todos los listeners y modificaciones para la pantalla añadir
     */
    public void setListenersAdd(){
        bAction.setOnClickListener(v -> {

            String nombre = etNombre.getText().toString();
            String muerte = etFechaMuerte.getText().toString();
            String nacimiento = etFechaNacimiento.getText().toString();
            String profesion = etProfesion.getText().toString();
            boolean validado = validarDatos(nombre, muerte, nacimiento, profesion);

            if (validado){
                Autor autor = new Autor(-1,nombre, Integer.parseInt(muerte), nacimiento, profesion);
                addAutor(autor);
                resetCampos();
            }else{
                Toast.makeText(getActivity(), "Campos no correctos, revísalos por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para añadir un autor
     * @param autor Autor a añadir
     */
    public void addAutor(Autor autor){
        apiService.addAutor(autor).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getActivity(), "Autor añadido correctamente", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(), "No se ha podido añadir el autor", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido añadir", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "No se ha podido añadir el autor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Métodos para asignar el Listener al botón de la acción en caso de Modificar Autor
     */
    public void setListenersUpdate(){
        setListenersPaginador();
        bAction.setOnClickListener(v -> {
            Autor autor = autores.get(posicion);
            String nombre = etNombre.getText().toString();
            String fechaMuerte = etFechaMuerte.getText().toString();
            String fechaNacimiento =etFechaNacimiento.getText().toString();
            String profesion = etProfesion.getText().toString();

            boolean validado = validarDatos(nombre, fechaMuerte, fechaNacimiento, profesion);

            if (validado){
                int fNacimiento = Integer.parseInt(fechaNacimiento);
                autor.setProfesion(profesion);
                autor.setNacimiento(fNacimiento);
                autor.setMuerte(fechaMuerte);
                autor.setNombre(nombre);
                modificarAutor(autor);
                resetCampos();
            }else{
                Toast.makeText(getActivity(), "Campos no correctos, revísalos por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para modificar al autor
     * @param autor Autor a modificar
     */
    public void modificarAutor(Autor autor){
        apiService.updateAutor(autor).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getContext(), "Autor modificado correctamente", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getContext(), "No se ha podido modificar el autor", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getContext(), "No se ha podido modificar correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido modificar el autor", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para asignar todos los listeners y modificaciones para la pantalla eliminar
     */
    public void setListenersDelete(){
        setListenersPaginador();
        setEnabledCampos(false);
        bAction.setOnClickListener(v -> {
            deleteAutor(autores.get(posicion));
        });
    }

    /**
     * Método para borrar un autor
     * @param a Autor a borrar
     */
    public void deleteAutor(Autor a){
        apiService.deleteAutorById(a.getId()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()){
                    if (response.body()){
                        Toast.makeText(getActivity(), "Se ha eliminado correctamente el autor", Toast.LENGTH_LONG).show();
                        autores.remove(posicion);
                        putDatos();
                    }else{
                        Toast.makeText(getActivity(), "No se ha podido eliminar correctamente el autor", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "No se ha podido eliminar correctamente el autor", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para habilitar o deshabilitar la edición de los campos
     * @param enabled Que valor tendrá (habilitado/deshabilitado)
     */
    public void setEnabledCampos(boolean enabled){
        etNombre.setEnabled(enabled);
        etFechaNacimiento.setEnabled(enabled);
        etFechaMuerte.setEnabled(enabled);
        etProfesion.setEnabled(enabled);
    }

    /**
     * Método para asignar todos los listeners para poder usar los botos siguiente y anterior
     */
    public void setListenersPaginador(){
        bSiguiente.setOnClickListener(v -> {
            if (posicion < autores.size()-1){
                posicion++;
                putDatos();
            }else{
                offset += 10;
                obtenerAutoresOffset(offset);
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
     * Método para enlazar los componentes de la vista
     */
    public void obtenerComponentes(){
        etNombre = getActivity().findViewById(R.id.etNombreAutor);
        bAnterior = getActivity().findViewById(R.id.bAnterior);
        bSiguiente = getActivity().findViewById(R.id.bSiguiente);
        bAction = getActivity().findViewById(R.id.bAction);
        etFechaNacimiento = getActivity().findViewById(R.id.etFechaNacimientoAutor);
        etFechaMuerte = getActivity().findViewById(R.id.etFechaMuerteAutor);
        etProfesion = getActivity().findViewById(R.id.etProfesionAutor);
        tvTitulo = getActivity().findViewById(R.id.tvTituloFrase);
    }

    /**
     * Método que valida la información introducida por el usuario
     * @param nombre Nombre del autor
     * @param fechaMuerte Año de su muerte, puede ser vacío
     * @param fechaNacimiento Año de nacimiento
     * @param profesion Profesión del autor
     * @return true si son correctos
     */
    public boolean validarDatos(String nombre, String fechaMuerte, String fechaNacimiento, String profesion){
        boolean validado = true;
        Date fNacimiento = null;
        Date fMuerte = null;
        if (nombre.length() > 2){
            etNombre.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        }else{
            etNombre.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            validado = false;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy");

        try {
            fMuerte = format.parse(fechaMuerte);
            etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        }catch (Exception e){
            if (!fechaMuerte.equals("")){
                etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                validado = false;
            }else{
                validado = true;
                etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
            }
        }
        try {
            Integer.parseInt(fechaNacimiento);
            fNacimiento = format.parse(fechaNacimiento);
            etFechaNacimiento.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        }catch (Exception nfe){
            validado = false;
            etFechaNacimiento.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        }

        if (profesion.length() > 2){
            etProfesion.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        }else{
            etProfesion.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            validado = false;
        }
        if (validado){
            if (fMuerte.after(fNacimiento)){
                Toast.makeText(getActivity(), "El año de nacimiento no puede ser mayor al año de muerte", Toast.LENGTH_LONG).show();
                validado = false;
                etFechaNacimiento.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            }
            if (fMuerte.after(new Date())){
                Toast.makeText(getActivity(), "El año de muerte no puede ser superior al año actual", Toast.LENGTH_LONG).show();
                etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                validado = false;
            }
            if (fNacimiento.after(new Date())){
                Toast.makeText(getActivity(), "El año de nacimiento no puede ser superior al año actual", Toast.LENGTH_LONG).show();
                etFechaNacimiento.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                validado = false;
            }
        }
        return validado;
    }

    /**
     * Cuando volvemos a este Fragment nos interesa volver los campos a la normalidad
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
        etProfesion.setText("");
        etFechaMuerte.setText("");
        etFechaNacimiento.setText("");
        etNombre.setText("");
    }

    /**
     * Método para devolver los campos a su estado original
     */
    public void resetCampos(){
        etProfesion.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        etFechaMuerte.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        etFechaNacimiento.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        etNombre.getBackground().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
        if (modo == Modo.ADD){
            vaciarCampos();
        }
    }

    /**
     * Método para mostrar la información del autor
     */
    public void putDatos(){
        Autor autor = autores.get(posicion);
        etProfesion.setText(autor.getProfesion());
        etNombre.setText(autor.getNombre());
        etFechaMuerte.setText(autor.getMuerte());
        etFechaNacimiento.setText(String.valueOf(autor.getNacimiento()));
    }

    /**
     * Método para obtener 10 autores desde donde le indiquemos
     * @param offset Desde que posición empezará a consultar
     */
    public void obtenerAutoresOffset(int offset){
        apiService.getAutoresLimit10(offset).enqueue(new Callback<List<Autor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Autor>> call, @NonNull Response<List<Autor>> response) {
                if (response.isSuccessful()){
                    autores.addAll(response.body());
                    putDatos();
                }else{
                    Toast.makeText(getContext(), "No funciona correctamente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Autor>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "No se ha podido obtener categorias", Toast.LENGTH_LONG).show();
            }
        });
    }
}
