package cf.victorlopez.frasesretrofit.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cf.victorlopez.frasesretrofit.R;

public class DialogLogin extends DialogFragment {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private DialogListener listener;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.login, null);
        //Obtenemos componenetes
        editTextUsername = view.findViewById(R.id.edit_username);
        editTextPassword = view.findViewById(R.id.edit_password);
        //Asignamos título y los dos botones (Cancel/Login)
        builder.setView(view).setTitle("Sign In").setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String username = null;
                String password = null;
                listener.applyTexts(username, password);
            }
        }).setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                //Enviamos la información a la Activity
                listener.applyTexts(username, password);
            }
        });

        return builder.create();
    }

    /**
     * Método para inicializar el listener
     * @param context Listener
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "Listener no inicializado");
        }
    }

    /**
     * Interfaz que implementará la Activity responsable para recibir los datos
     */
    public interface DialogListener {
        void applyTexts(String username, String password);
    }
}
