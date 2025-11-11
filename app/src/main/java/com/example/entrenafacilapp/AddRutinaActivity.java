package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.ArrayList;
import java.util.List;

public class AddRutinaActivity extends AppCompatActivity {

    EditText etNombre, etDescripcion, etTipo, etDuracion;
    Spinner spinnerDia;
    Button btnGuardar, btnSeleccionarFoto;
    ImageView ivFotoPreview;

    DBHelper dbHelper;
    int usuarioId;
    boolean modoEdicion = false;
    int rutinaId = -1;

    List<Uri> fotosSeleccionadas = new ArrayList<>();

    private final ActivityResultLauncher<String[]> seleccionarMultiplesFotos =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    fotosSeleccionadas.clear();
                    fotosSeleccionadas.addAll(uris);
                    ivFotoPreview.setImageURI(fotosSeleccionadas.get(0));
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
        ivFotoPreview = findViewById(R.id.ivFotoRutina);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos los días", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"});
        spinnerDia.setAdapter(adapter);

        modoEdicion = getIntent().getBooleanExtra("modo_edicion", false);
        if (modoEdicion) {
            rutinaId = getIntent().getIntExtra("rutina_id", -1);
            if (rutinaId != -1) {
                cargarDatosRutina(rutinaId);
                btnGuardar.setText("Actualizar rutina");
            }
        }

        btnSeleccionarFoto.setOnClickListener(v -> seleccionarMultiplesFotos.launch(new String[]{"image/*"}));
        btnGuardar.setOnClickListener(v -> guardarRutina());
    }

    private void guardarRutina() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String dia = spinnerDia.getSelectedItem().toString();
        int duracion;

        try {
            duracion = Integer.parseInt(etDuracion.getText().toString().trim());
        } catch (Exception e) {
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

        List<String> rutasGuardadas = new ArrayList<>();
        for (Uri uri : fotosSeleccionadas) {
            String ruta = guardarImagenInterna(uri);
            if (ruta != null) rutasGuardadas.add(ruta);
        }

        if (!rutasGuardadas.isEmpty()) {
            values.put("fotos_rutina", TextUtils.join(",", rutasGuardadas));
        }

        if (modoEdicion) {
            db.update("rutinas", values, "id=?", new String[]{String.valueOf(rutinaId)});
            Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
        } else {
            db.insert("rutinas", null, values);
            Toast.makeText(this, "Rutina guardada", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void cargarDatosRutina(int rutinaId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre, descripcion, tipo, duracion, dia_semana, fotos_rutina FROM rutinas WHERE id = ?",
                new String[]{String.valueOf(rutinaId)}
        );

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0));
            etDescripcion.setText(cursor.getString(1));
            etTipo.setText(cursor.getString(2));
            etDuracion.setText(String.valueOf(cursor.getInt(3)));

            String dia = cursor.getString(4);
            ArrayAdapter adapter = (ArrayAdapter) spinnerDia.getAdapter();
            int pos = adapter.getPosition(dia);
            spinnerDia.setSelection(pos);

            String fotos = cursor.getString(5);
            if (!TextUtils.isEmpty(fotos)) {
                String[] rutas = fotos.split(",");
                if (rutas.length > 0) {
                    ivFotoPreview.setImageURI(Uri.fromFile(new File(rutas[0])));
                }
            }
        }
        cursor.close();
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

            inputStream.close();
            outputStream.close();
            return archivo.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
