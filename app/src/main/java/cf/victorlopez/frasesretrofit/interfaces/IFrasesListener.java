package cf.victorlopez.frasesretrofit.interfaces;

import cf.victorlopez.frasesretrofit.models.Frase;

public interface IFrasesListener {
    /**
     * Método que se invocará cuando se seleccione una frase
     * @param frase Frase seleccionada
     */
    void onFraseSeleccionada(Frase frase);
}
