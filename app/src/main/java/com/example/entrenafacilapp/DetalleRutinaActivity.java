package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad que muestra los detalles de una rutina específica.
 * Permite marcarla como completada, editarla o eliminarla.
 */
public class DetalleRutinaActivity extends AppCompatActivity {

    // Elementos de la interfaz
    TextView tvNombre, tvDescripcion, tvTipo, tvDuracion;
    Button btnCompletar, btnEliminar, btnEditar;

    // Base de datos
    DBHelper dbHelper;
    int rutinaId;     // ID de la rutina seleccionada
    int usuarioId;    // ID del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_rutina);

        // Enlazar elementos del layout con variables de Java
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvTipo = findViewById(R.id.tvTipo);
        tvDuracion = findViewById(R.id.tvDuracion);
        btnCompletar = findViewById(R.id.btnCompletar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnEditar = findViewById(R.id.btnEditar);

        // Instancia del helper de la base de datos
        dbHelper = new DBHelper(this);

        // Obtener ID de la rutina enviada desde la actividad anterior
        rutinaId = getIntent().getIntExtra("rutina_id", -1);

        // Obtener el ID del usuario desde las preferencias guardadas
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Cargar información de la rutina desde la base de datos
        cargarDetalles(rutinaId);

        // Acciones de los botones
        btnCompletar.setOnClickListener(v -> marcarComoCompletada());
        btnEliminar.setOnClickListener(v -> eliminarRutina());
        btnEditar.setOnClickListener(v -> editarRutina());
    }

    /**
     * Carga los datos de la rutina desde la base de datos y los muestra en pantalla.
     */
    private void cargarDetalles(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, descripcion, tipo, duracion FROM rutinas WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            // Mostrar en los campos de texto
            tvNombre.setText(cursor.getString(0));
            tvDescripcion.setText(cursor.getString(1));
            tvTipo.setText(cursor.getString(2));
            tvDuracion.setText(cursor.getInt(3) + " minutos");
        }

        cursor.close(); // Siempre cerrar el cursor
    }

    /**
     * Marca la rutina como completada si corresponde al día actual o es "Todos los días".
     */
    private void marcarComoCompletada() {
        // Obtener el día actual (ej: Lunes, Martes...)
        String diaActual = new SimpleDateFormat("EEEE", new Locale("es", "ES")).format(new Date());
        diaActual = diaActual.substring(0, 1).toUpperCase() + diaActual.substring(1); // Capitalizar

        // Verificar que la rutina es válida para hoy
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT dia_semana FROM rutinas WHERE id = ?", new String[]{String.valueOf(rutinaId)});

        if (cursor.moveToFirst()) {
            String diaRutina = cursor.getString(0);

            // Si no corresponde al día de hoy ni es "Todos los días", no se puede marcar
            if (!diaRutina.equals("Todos los días") && !diaRutina.equals(diaActual)) {
                Toast.makeText(this, "Solo puedes hacer las rutinas del " + diaActual + " o las de Todos los días", Toast.LENGTH_LONG).show();
                cursor.close();
                return;
            }
        }
        cursor.close();

        // Insertar en la tabla de progreso la rutina completada
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("rutina_id", rutinaId);
        values.put("fecha", fechaActual);

        long result = dbWrite.insert("progreso", null, values);

        // Confirmar al usuario si fue exitoso o no
        if (result != -1) {
            Toast.makeText(this, "Rutina marcada como completada", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la pantalla
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Elimina la rutina actual de la base de datos.
     */
    private void eliminarRutina() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int filasEliminadas = db.delete("rutinas", "id = ?", new String[]{String.valueOf(rutinaId)});

        // Mostrar resultado al usuario
        if (filasEliminadas > 0) {
            Toast.makeText(this, "Rutina eliminada", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre la pantalla de edición de rutina con los datos actuales.
     */
    private void editarRutina() {
        Intent intent = new Intent(DetalleRutinaActivity.this, AddRutinaActivity.class);
        intent.putExtra("modo_edicion", true); // Indica que es edición
        intent.putExtra("rutina_id", rutinaId); // Envia el ID de la rutina
        startActivity(intent);
    }
}
