package com.example.entrenafacilapp;

import android.content.SharedPreferences; // Para acceder a datos guardados de sesión
import android.database.Cursor;           // Para manejar resultados de consultas SQL
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;    // Para mostrar imagen desde archivo local
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;          // Imagen de perfil del usuario
import android.widget.TextView;          // Muestra la información del usuario
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;

public class PerfilFragment extends Fragment {

    // Elementos visuales donde se mostrará la info del usuario
    TextView tvEdad, tvPeso, tvAltura, tvSexo;
    ImageView ivFotoPerfil;

    DBHelper dbHelper;      // Clase para gestionar la base de datos
    int usuarioId;          // Guarda el ID del usuario actual

    // Este método se llama al crear la vista del fragmento
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflamos el layout que contiene el diseño del perfil
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Asociamos las variables con los elementos visuales
        tvEdad = view.findViewById(R.id.tvEdad);
        tvPeso = view.findViewById(R.id.tvPeso);
        tvAltura = view.findViewById(R.id.tvAltura);
        tvSexo = view.findViewById(R.id.tvSexo);
        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);

        // Creamos la base de datos
        dbHelper = new DBHelper(getContext());

        // Obtenemos el ID del usuario guardado en preferencias (sesión iniciada)
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1); // Si no hay ID, devuelve -1

        // Si no hay usuario, mostramos un mensaje y cerramos la actividad
        if (usuarioId == -1) {
            Toast.makeText(getContext(), "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            requireActivity().finish(); // Cierra la app o vuelve al login
            return view;
        }

        // Llamamos al método para cargar los datos del usuario
        cargarPerfil();

        return view;
    }

    // Método para cargar los datos del usuario desde SQLite
    private void cargarPerfil() {
        // Accedemos a la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Ejecutamos una consulta SQL para traer los datos del usuario actual
        Cursor cursor = db.rawQuery(
                "SELECT edad, peso, altura, sexo, foto_perfil FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(usuarioId)} // El ? se sustituye por el ID
        );

        // Si la consulta devuelve resultados
        if (cursor.moveToFirst()) {
            // Mostramos los datos en los TextViews
            tvEdad.setText("Edad: " + cursor.getInt(0));
            tvPeso.setText("Peso: " + cursor.getDouble(1) + " kg");
            tvAltura.setText("Altura: " + cursor.getDouble(2) + " cm");
            tvSexo.setText("Sexo: " + cursor.getString(3));

            // Obtenemos la ruta de la imagen de perfil (si existe)
            String ruta = cursor.getString(4);

            // Comprobamos si hay una imagen en esa ruta y la mostramos
            if (ruta != null && !ruta.isEmpty()) {
                File archivo = new File(ruta);
                if (archivo.exists()) {
                    ivFotoPerfil.setImageBitmap(BitmapFactory.decodeFile(ruta));
                } else {
                    // Si no existe el archivo, se informa al usuario
                    Toast.makeText(getContext(), "Imagen no encontrada en: " + ruta, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // Si no se encuentra el usuario (algo raro), se muestra mensaje
            Toast.makeText(getContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
        }

        // Cerramos el cursor para liberar memoria
        cursor.close();
    }
}
