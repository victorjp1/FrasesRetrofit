package cf.victorlopez.frasesretrofit.exceptions;

/**
 * Clase usada para cuando no encontramos el usuario en la BD
 */
public class UserNotExistException extends Exception{
    private String message;

    /**
     * Constructor de la excepción
     * @param message Mensaje a mostrar
     */
    public UserNotExistException(String message) {
        super(message);
        this.message = message;
    }
}
