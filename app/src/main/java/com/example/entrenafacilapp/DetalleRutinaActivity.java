package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleRutinaActivity extends AppCompatActivity {

    TextView tvNombre, tvDescripcion, tvTipo, tvDuracion, tvDia;
    ImageView ivFotoRutina;
    Button btnEditar, btnEliminar, btnCompletar;

    DBHelper dbHelper;
    int rutinaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_rutina);

        dbHelper = new DBHelper(this);

        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvTipo = findViewById(R.id.tvTipo);
        tvDuracion = findViewById(R.id.tvDuracion);
        tvDia = findViewById(R.id.tvDia);
        ivFotoRutina = findViewById(R.id.ivFotoRutina);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCompletar = findViewById(R.id.btnCompletar);

        rutinaId = getIntent().getIntExtra("rutina_id", -1);

        if (rutinaId != -1) {
            cargarDatosRutina(rutinaId);
        }

        // Botón editar
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleRutinaActivity.this, AddRutinaActivity.class);
            intent.putExtra("modo_edicion", true);
            intent.putExtra("rutina_id", rutinaId);
            startActivity(intent);
            finish();
        });

        // Botón eliminar
        btnEliminar.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int filas = db.delete("rutinas", "id = ?", new String[]{String.valueOf(rutinaId)});
            if (filas > 0) {
                Toast.makeText(this, "Rutina eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        // Botón marcar como completada
        btnCompletar.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            values.put("usuario_id", obtenerUsuarioId());
            values.put("rutina_id", rutinaId);
            values.put("fecha", fechaActual);

            long result = db.insert("progreso", null, values);
            if (result != -1) {
                Toast.makeText(this, "Rutina marcada como completada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al marcar como completada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosRutina(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT nombre, descripcion, tipo, duracion, dia_semana, foto_rutina FROM rutinas WHERE id = ?",
                new String[]{String.valueOf(id)}
        );

        if (cursor.moveToFirst()) {
            tvNombre.setText(cursor.getString(0));
            tvDescripcion.setText(cursor.getString(1));
            tvTipo.setText(cursor.getString(2));
            tvDuracion.setText(cursor.getInt(3) + " min");
            tvDia.setText(cursor.getString(4));

            String rutaFoto = cursor.getString(5);
            if (rutaFoto != null) {
                File imgFile = new File(rutaFoto);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivFotoRutina.setImageBitmap(bitmap);
                }
            }
        }

        cursor.close();
    }

    private int obtenerUsuarioId() {
        return getSharedPreferences("sesion", MODE_PRIVATE).getInt("usuario_id", -1);
    }
}
