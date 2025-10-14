package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AddRutinaActivity extends AppCompatActivity {

    EditText etNombre, etDescripcion, etTipo, etDuracion;
    Spinner spinnerDia;
    Button btnGuardar, btnSeleccionarFoto;
    ImageView ivFotoRutina;

    DBHelper dbHelper;

    int rutinaId = -1;
    boolean modoEdicion = false;
    int usuarioId;

    Uri fotoUri = null;

    private final ActivityResultLauncher<String> seleccionarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUri = uri;
                    ivFotoRutina.setImageURI(fotoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rutina);

        dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTipo = findViewById(R.id.etTipo);
        etDuracion = findViewById(R.id.etDuracion);
        spinnerDia = findViewById(R.id.spinnerDia);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        ivFotoRutina = findViewById(R.id.ivFotoRutina);

        String[] dias = {"Todos los días", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapter);

        modoEdicion = getIntent().getBooleanExtra("modo_edicion", false);
        if (modoEdicion) {
            rutinaId = getIntent().getIntExtra("rutina_id", -1);
            cargarDatosRutina(rutinaId);
        }

        btnGuardar.setOnClickListener(view -> guardarRutina());
        btnSeleccionarFoto.setOnClickListener(v -> seleccionarFotoLauncher.launch("image/*"));
    }

    private void cargarDatosRutina(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, descripcion, tipo, duracion, dia_semana, foto_rutina FROM rutinas WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0));
            etDescripcion.setText(cursor.getString(1));
            etTipo.setText(cursor.getString(2));
            etDuracion.setText(String.valueOf(cursor.getInt(3)));

            String dia = cursor.getString(4);
            for (int i = 0; i < spinnerDia.getCount(); i++) {
                if (spinnerDia.getItemAtPosition(i).toString().equalsIgnoreCase(dia)) {
                    spinnerDia.setSelection(i);
                    break;
                }
            }

            String rutaFoto = cursor.getString(5);
            if (rutaFoto != null) {
                File imgFile = new File(rutaFoto);
                if (imgFile.exists()) {
                    ivFotoRutina.setImageBitmap(android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                }
            }
        }
        cursor.close();
    }

    private void guardarRutina() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String dia = spinnerDia.getSelectedItem().toString();
        int duracion;

        try {
            duracion = Integer.parseInt(etDuracion.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duración inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("tipo", tipo);
        values.put("duracion", duracion);
        values.put("dia_semana", dia);

        if (fotoUri != null) {
            String rutaInterna = guardarImagenInterna(fotoUri);
            if (rutaInterna != null) {
                values.put("foto_rutina", rutaInterna);
            }
        }

        if (modoEdicion) {
            int filas = db.update("rutinas", values, "id = ?", new String[]{String.valueOf(rutinaId)});
            if (filas > 0) {
                Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            long result = db.insert("rutinas", null, values);
            if (result != -1) {
                Toast.makeText(this, "Rutina guardada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String guardarImagenInterna(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String nombreArchivo = "rutina_" + System.currentTimeMillis() + ".jpg";
            File archivo = new File(getFilesDir(), nombreArchivo);
            OutputStream outputStream = new FileOutputStream(archivo);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return archivo.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
