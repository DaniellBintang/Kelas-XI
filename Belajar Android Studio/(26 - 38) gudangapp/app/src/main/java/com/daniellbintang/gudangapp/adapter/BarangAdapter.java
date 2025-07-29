package com.daniellbintang.gudangapp.adapter;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daniellbintang.gudangapp.R;
import com.daniellbintang.gudangapp.model.Barang;

import java.util.List;

public class BarangAdapter extends RecyclerView.Adapter<BarangAdapter.ViewHolder> {

    public interface OnMenuClickListener {
        void onEdit(Barang barang);
        void onDelete(Barang barang);
    }

    private List<Barang> listBarang;
    private OnMenuClickListener listener;
    private Context context;

    public BarangAdapter(Context context, List<Barang> listBarang, OnMenuClickListener listener) {
        this.context = context;
        this.listBarang = listBarang;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barang, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Barang barang = listBarang.get(position);
        holder.tvNama.setText(barang.getNama());
        holder.tvStok.setText("Stok: " + barang.getStok());
        holder.tvHarga.setText("Harga: Rp " + String.format("%,.0f", barang.getHarga()));

        holder.imgMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.imgMenu);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_barang, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    listener.onEdit(barang);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    listener.onDelete(barang);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return listBarang.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStok, tvHarga;
        ImageView imgMenu;
        ViewHolder(View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaBarang);
            tvStok = itemView.findViewById(R.id.tvStokBarang);
            tvHarga = itemView.findViewById(R.id.tvHargaBarang);
            imgMenu = itemView.findViewById(R.id.imgMenu);
        }
    }
}