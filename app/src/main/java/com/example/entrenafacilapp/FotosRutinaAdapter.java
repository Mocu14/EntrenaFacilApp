package com.example.entrenafacilapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

/**
 * Adaptador encargado de mostrar las imágenes asociadas a una rutina.
 *
 * Este adaptador se utiliza junto con un ViewPager2 para permitir
 * al usuario deslizar entre varias imágenes de una misma rutina,
 * mejorando la experiencia visual respecto a una única imagen fija.
 */
public class FotosRutinaAdapter extends RecyclerView.Adapter<FotosRutinaAdapter.FotoViewHolder> {

    // Contexto de la aplicación o Activity que usa el adaptador
    private final Context context;

    // Lista de rutas absolutas a las imágenes guardadas en el almacenamiento interno
    private final List<String> listaFotos;

    /**
     * Constructor del adaptador.
     */
    public FotosRutinaAdapter(Context context, List<String> listaFotos) {
        this.context = context;
        this.listaFotos = listaFotos;
    }

    /**
     * Se encarga de crear cada una de las vistas individuales del carrusel.
     * Infla el layout XML que contiene un ImageView.
     */
    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tem_foto_rutina, parent, false);
        return new FotoViewHolder(view);
    }

    /**
     * Asocia una imagen concreta a cada posición del ViewPager.
     * A partir de la ruta almacenada en base de datos se carga el archivo
     * desde el almacenamiento interno y se convierte en un Bitmap.
     */
    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String ruta = listaFotos.get(position).trim();
        File imgFile = new File(ruta);

        // Se comprueba que el archivo existe antes de cargarlo
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.ivFoto.setImageBitmap(bitmap);
        }
    }

    /**
     * Devuelve el número total de imágenes de la rutina.
     */
    @Override
    public int getItemCount() {
        return listaFotos.size();
    }

    /**
     * ViewHolder que representa cada imagen dentro del carrusel.
     * Contiene únicamente un ImageView donde se muestra la foto.
     */
    public static class FotoViewHolder extends RecyclerView.ViewHolder {

        ImageView ivFoto;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
        }
    }
}
