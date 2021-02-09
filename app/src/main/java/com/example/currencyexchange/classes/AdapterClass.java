package com.example.currencyexchange.classes;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currencyexchange.R;

import java.util.ArrayList;

/**
 * Trieda AdapterClass, ktorá sa stará o vykreslenie listu všetkých kurzov.
 */
public class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolderClass> {

    private ArrayList<Item> items;
    private OnItemClickListener mListener;

    /**
     * Interface, obsahujuci hlavicku metody onItemCLick.
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Nastavy listener.
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    /**
     * Tato trieda opisuje vlastnosti jedneho prvku v RecyclerView.
     */
    public static class ViewHolderClass extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        ImageView mImageView;
        TextView mCurrShortCut;
        TextView mExchange;
        TextView mCurrencyName;
        CardView mCardView;

        /**
         * Konstruktor triedy.
         * @param itemView
         * @param listener
         */
        public ViewHolderClass(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            this.mImageView = itemView.findViewById(R.id.imageView);
            this.mCurrShortCut = itemView.findViewById(R.id.curr_name_text_view);
            this.mExchange = itemView.findViewById(R.id.exchange_text_view);
            this.mCurrencyName = itemView.findViewById(R.id.curr_all_name_text_view);
            this.mCardView = itemView.findViewById(R.id.mCardView);

            this.mCardView.setOnCreateContextMenuListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

        /**
         * Po dlhom kliknuti na prvok v liste sa zavola tato funkcia.
         * @param menu
         * @param v
         * @param menuInfo
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose option");
            menu.add(this.getAdapterPosition(),121,0,"Set as base currency");
            menu.add(this.getAdapterPosition(),122,1,"Add to favorites");
            menu.add(this.getAdapterPosition(),123,2,"Remove from favorites");
        }
    }

    /**
     * Konstruktor triedy.
     * @param items
     */
    public AdapterClass(ArrayList<Item> items) {
        this.items = items;
    }

    /**
     * Zavola sa vtedy ak RecyclerView potrebuje novy RecyclerView.ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_item, parent, false);
        ViewHolderClass vhc = new ViewHolderClass(v,mListener);
        return vhc;
    }

    /**
     * Metoda, ktora vykresli data na zadanej pozicii v RecyclerView.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolderClass holder, int position) {
        Item currItem = this.items.get(position);

        holder.mImageView.setImageResource(currItem.getImageResource());
        holder.mCurrShortCut.setText(currItem.getName());
        holder.mExchange.setText(currItem.getExchangeRateText());
        holder.mCurrencyName.setText(currItem.getCurrencyName());
    }

    /**
     * Vrati pocet prvkov listu.
     * @return
     */
    @Override
    public int getItemCount() {
        return this.items.size();
    }

    /**
     * Vrati skratku menovej jednotky kurzu.
     * @param position
     * @return
     */
    public String getNameOfItem(int position){
        return this.items.get(position).getName();
    }

    /**
     * Nastavi list itemov.
     * @param i
     */
    public void setItems(ArrayList<Item> i){
        this.items = i;
    }
}
