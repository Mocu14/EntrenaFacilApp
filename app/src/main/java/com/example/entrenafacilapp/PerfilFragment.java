package com.example.entrenafacilapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;

/**
 * Fragment que muestra la información del perfil del usuario.
 *
 * Recupera los datos del usuario actualmente logueado desde SQLite
 * y muestra sus características personales y foto de perfil.
 */
public class PerfilFragment extends Fragment {

    // TextViews para mostrar los datos del usuario
    TextView tvEdad, tvPeso, tvAltura, tvSexo;
    ImageView ivFotoPerfil; // ImageView para la foto de perfil

    DBHelper dbHelper;      // Helper para manejar la base de datos
    int usuarioId;          // ID del usuario actualmente autenticado

    /**
     * Método que se llama al crear la vista del fragmento.
     * Se encarga de inflar el layout, enlazar variables y cargar los datos.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflamos el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Enlazamos las variables con los elementos visuales del layout
        tvEdad = view.findViewById(R.id.tvEdad);
        tvPeso = view.findViewById(R.id.tvPeso);
        tvAltura = view.findViewById(R.id.tvAltura);
        tvSexo = view.findViewById(R.id.tvSexo);
        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);

        // Inicializamos el helper de la base de datos
        dbHelper = new DBHelper(getContext());

        // Obtenemos el ID del usuario actualmente logueado desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1); // -1 indica que no hay sesión activa

        // Si no hay sesión activa, mostramos mensaje y cerramos la actividad
        if (usuarioId == -1) {
            Toast.makeText(getContext(), "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            requireActivity().finish(); // Cierra la actividad actual
            return view;
        }

        // Cargamos los datos del perfil del usuario
        cargarPerfil();

        return view;
    }

    /**
     * Método que obtiene los datos del usuario desde la base de datos
     * y los asigna a los elementos visuales del fragmento.
     */
    private void cargarPerfil() {
        // Abrimos la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL para obtener edad, peso, altura, sexo y ruta de foto de perfil
        Cursor cursor = db.rawQuery(
                "SELECT edad, peso, altura, sexo, foto_perfil FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(usuarioId)} // Sustituimos ? por el ID
        );

        // Si la consulta devuelve resultados
        if (cursor.moveToFirst()) {
            // Mostramos los datos en los TextViews
            tvEdad.setText("Edad: " + cursor.getInt(0));
            tvPeso.setText("Peso: " + cursor.getDouble(1) + " kg");
            tvAltura.setText("Altura: " + cursor.getDouble(2) + " cm");
            tvSexo.setText("Sexo: " + cursor.getString(3));

            // Mostramos la imagen de perfil si existe
            String ruta = cursor.getString(4);
            if (ruta != null && !ruta.isEmpty()) {
                File archivo = new File(ruta);
                if (archivo.exists()) {
                    ivFotoPerfil.setImageBitmap(BitmapFactory.decodeFile(ruta));
                } else {
                    Toast.makeText(getContext(), "Imagen no encontrada en: " + ruta, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // Mensaje si no se encuentra el usuario (caso raro)
            Toast.makeText(getContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
        }

        // Cerramos el cursor para liberar memoria
        cursor.close();
    }
}
