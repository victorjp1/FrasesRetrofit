package cf.victorlopez.frasesretrofit.interfaces;

import java.util.List;

import cf.victorlopez.frasesretrofit.enums.ResultadoAdd;
import cf.victorlopez.frasesretrofit.models.Autor;
import cf.victorlopez.frasesretrofit.models.Categoria;
import cf.victorlopez.frasesretrofit.models.Frase;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IAPIService {
    /**
     * Método que pide todas las frases
     * @return Todas las frases
     */
    @GET("frase/all")
    Call<List<Frase>> getFrases();

    /**
     * Método para modificar frases
     * @param frase Frase a modificar
     * @return Si se ha podido modificar o no
     */
    @PUT("/frase/update")
    Call<Boolean> updateFrase(@Body Frase frase);

    /**
     * Método para añadir frases
     * @param frase Frase a añadir
     * @return true si se ha podido añadir correctamente
     */
    @POST("frase/add")
    Call<ResultadoAdd> addFrase(@Body Frase frase);

    /**
     * Método para añadir frases
     * @param texto Contenido de la frase
     * @param fechaProgramada Fecha programada de la frase
     * @param idAutor ID del autor de la frase
     * @param idCategoria ID de la categoría de la frase
     * @return true si se ha podido añadir
     */
    @POST("frase/addValues")
    @FormUrlEncoded
    Call<Boolean> addFraseValues(@Field("texto") String texto,
                                 @Field("fechaProgramada") String fechaProgramada,
                                 @Field("idAutor") int idAutor,
                                 @Field("idCategoria")int idCategoria);

    /**
     * Método para eliminar frases
     * @param id ID de la frase a eliminar
     * @return true si se ha podido eliminar
     */
    @DELETE("frase/{id}")
    Call<Boolean> deleteFraseById(@Path("id") Integer id);

    /**
     * Método para obtener la frase del día
     * @param dia Dia de la frase
     * @return Frase obtenida
     */
    @GET("frase/dia/{dia}")
    Call<Frase> getFraseDelDia(@Path("dia") String dia);

    /**
     * Método para obtener una frase
     * @param id ID de la frase
     * @return Frase obtenida
     */
    @GET("/frase")
    Call<Frase> getFrase(@Field("id") Integer id);

    /**
     * Método para obtener las frases filtradas por autor
     * @param id ID del autor
     * @return Lista de frases de ese autor
     */
    @GET("/frase/autor/{id}")
    Call<List<Frase>> getFrasesByAutor(@Path("id") Integer id);

    /**
     * Método para obtener las frases por categoría
     * @param id ID de la categoría
     * @return Lista de frases de esa categoría
     */
    @GET("/frase/categoria/{id}")
    Call<List<Frase>> getFraseByCategoria(@Path("id") Integer id);

    /**
     * Método para obtener 10 frases a partir de un índice
     * @param offset Índice de la búsqueda
     * @return Lista de frases
     */
    @GET("/frase/all/{offset}")
    Call<List<Frase>> getFrasesLimit10(@Path("offset") Integer offset);

    /**
     * Método para obtener 10 categorías a partir de un índice
     * @param offset Índice de búsqueda
     * @return Lista de categorías
     */
    @GET("/categoria/all/{offset}")
    Call<List<Categoria>> getCategoriasLimit10(@Path("offset") Integer offset);

    /**
     * Método para obtener todas las categorías
     * @return Lista de categorías
     */
    @GET("categoria/all")
    Call<List<Categoria>> getCategorias();

    /**
     * Método para modificar categorías
     * @param cat Categoría a modificar
     * @return true si se ha podido modificar correctamente
     */
    @PUT("/categoria/update")
    Call<Boolean> updateCategoria(@Body Categoria cat);

    /**
     * Método para añadir una categoría (El ID será ignorado)
     * @param categoria Categoría a añadir
     * @return resultado de la operación
     */
    @POST("categoria/add")
    Call<Boolean> addCategoria(@Body Categoria categoria);

    /**
     * Método para borrar una categoría
     * @param id ID de la categoría a borrar
     * @return Resultado de la operación
     */
    @DELETE("categoria/{id}")
    Call<Boolean> deleteCategoriaById(@Path("id") Integer id);

    /**
     * Método para obtener todos los autores desde un ínice
     * @param offset Índice desde el cual buscar
     * @return Lista de autores
     */
    @GET("/autor/all/{offset}")
    Call<List<Autor>> getAutoresLimit10(@Path("offset") Integer offset);

    /**
     * Método para obtener todos los autores
     * @return Lista de autores
     */
    @GET("autor/all")
    Call<List<Autor>> getAutores();

    /**
     * Método para modificar un autor
     * @param autor Autor a modificar
     * @return Resultado de la operación
     */
    @PUT("/autor/update")
    Call<Boolean> updateAutor(@Body Autor autor);

    /**
     * Método para añadir un autor
     * @param autor Autor a añadir
     * @return Resultado de la operación
     */
    @POST("autor/add")
    Call<Boolean> addAutor(@Body Autor autor);

    /**
     * Método para eliminar un autor
     * @param id ID del autor
     * @return Resultado de la operación
     */
    @DELETE("autor/{id}")
    Call<Boolean> deleteAutorById(@Path("id") Integer id);
}
