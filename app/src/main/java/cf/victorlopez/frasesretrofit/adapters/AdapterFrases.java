package cf.victorlopez.frasesretrofit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.interfaces.IFrasesListener;
import cf.victorlopez.frasesretrofit.models.Frase;

public class AdapterFrases extends RecyclerView.Adapter<AdapterFrases.FrasesViewHolder> {
    private List<Frase> frases;
    private IFrasesListener listener;

    /**
     * Constructor del adaptador
     * @param frases Frases a mostrar
     * @param listener Listener para cuando se seleccione un campo
     */
    public AdapterFrases(List<Frase> frases, IFrasesListener listener) {
        this.frases = frases;
        this.listener = listener;
    }

    /**
     * Método que crea ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public AdapterFrases.FrasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_consultar_frase, parent, false);
        return new FrasesViewHolder(itemView, listener, frases);
    }

    /**
     * Método para actualizar la lista de frases
     * @param frases Nueva lista
     */
    public void setFrases(List<Frase> frases){
        this.frases = frases;
        notifyDataSetChanged();
    }

    /**
     * Método para enviar información al item del listado
     * @param holder Item en el que se pintará
     * @param position Posición de la lista
     */
    @Override
    public void onBindViewHolder(@NonNull AdapterFrases.FrasesViewHolder holder, int position) {
        Frase frase = frases.get(position);
        holder.bindFrase(frase);
    }

    /**
     * Devuelve el tamaño de la lista
     * @return Tamaño de la lista
     */
    @Override
    public int getItemCount() {
        return frases.size();
    }

    public static class FrasesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvAutor, tvCategoria, tvFrase, tvFechaProgramada;
        private IFrasesListener listener;
        private List<Frase> frases;

        /**
         * Constructor FrasesViewHolder
         * @param itemView Vista asociada
         * @param listener Se ejecutará cuando se pulse un elemento
         * @param frases Lista de frases
         */
        public FrasesViewHolder(@NonNull View itemView, IFrasesListener listener, List<Frase> frases) {
            super(itemView);
            this.tvAutor = itemView.findViewById(R.id.tvAutorConsultarFrase);
            this.tvCategoria = itemView.findViewById(R.id.tvCategoiraConsultarFrase);
            this.tvFrase = itemView.findViewById(R.id.tvFraseConsultarFrase);
            this.tvFechaProgramada = itemView.findViewById(R.id.tvFechaPrgListado);
            this.listener = listener;
            this.frases = frases;
            itemView.setOnClickListener(this);
        }

        /**
         * Método para mostrar los datos de la frase
         * @param frase Frase a mostrar
         */
        public void bindFrase(Frase frase) {
            tvFrase.setText("Frase: " + frase.getTexto());
            tvCategoria.setText("Categoria: " + frase.getCategoria().getNombre());
            tvAutor.setText("Autor: " + frase.getAutor().getNombre());
            tvFechaProgramada.setText("Fecha programada: " + frase.getFechaProgramada());
        }

        /**
         * Método que se lanza cuando se pulsa sobre un elemento, llamaremos
         * al método del listener
         * @param v Vista
         */
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onFraseSeleccionada(frases.get(getAdapterPosition()));
            }
        }
    }
}

