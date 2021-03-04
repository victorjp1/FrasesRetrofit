package cf.victorlopez.frasesretrofit.enums;

import java.io.Serializable;

/**
 * Clase que sirve para saber que opción del menú seleccionan los usuarios
 */
public enum Modo implements Serializable {
    UPDATE, ADD, DELETE;

    public static final String CODE_MODE = "cf.victorlopez.mode";
}
