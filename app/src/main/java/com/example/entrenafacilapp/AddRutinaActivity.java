package com.example.entrenafacilapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

/**
 * Activity encargada de la creación y edición de rutinas de entrenamiento.
 *
 * Permite:
 *  - Introducir los datos básicos de la rutina.
 *  - Seleccionar uno o varios días de la semana.
 *  - Asociar una o varias imágenes a la rutina.
 *  - Guardar o actualizar la información en la base de datos.
 */
public class AddRutinaActivity extends AppCompatActivity {

    // ==========================
    // Componentes de la interfaz
    // ==========================
    private EditText etNombre, etDescripcion, etTipo, etDuracion;
    private TextView tvDiasSeleccionados;
    private Button btnGuardar, btnSeleccionarFoto;
    private ImageView ivFotoPreview;

    // ==========================
    // Gestión de días de la semana
    // ==========================

    // Lista de días disponibles para asignar a una rutina
    private final String[] diasSemana = {
            "Lunes", "Martes", "Miércoles",
            "Jueves", "Viernes", "Sábado", "Domingo"
    };

    // Vector que indica qué días están seleccionados
    private final boolean[] diasSeleccionados = new boolean[7];

    // Indica si la rutina se ejecuta todos los días
    private boolean todosLosDias = false;

    // ==========================
    // Datos y control
    // ==========================

    // Acceso a la base de datos
    private DBHelper dbHelper;

    // Identificador del usuario logueado
    private int usuarioId;

    // Indica si se está creando o editando una rutina
    private boolean modoEdicion = false;

    // Identificador de la rutina cuando se edita
    private int rutinaId = -1;

    // Lista de imágenes seleccionadas por el usuario
    private final List<Uri> fotosSeleccionadas = new ArrayList<>();

    /**
     * Lanza el selector de documentos del sistema para elegir múltiples imágenes.
     * Se utiliza para asociar varias fotos a una misma rutina.
     */
    private final ActivityResultLauncher<String[]> seleccionarMultiplesFotos =
            registerForActivityResult(
                    new ActivityResultContracts.OpenMultipleDocuments(),
                    uris -> {
                        if (uris != null && !uris.isEmpty()) {
                            fotosSeleccionadas.clear();
                            fotosSeleccionadas.addAll(uris);
                            // Se muestra la primera imagen como vista previa
                            ivFotoPreview.setImageURI(fotosSeleccionadas.get(0));
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rutina);

        // Inicialización del helper de base de datos
        dbHelper = new DBHelper(this);

        // Obtención del usuario logueado desde la sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Enlace de componentes visuales con el layout
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTipo = findViewById(R.id.etTipo);
        etDuracion = findViewById(R.id.etDuracion);
        tvDiasSeleccionados = findViewById(R.id.tvDiasSeleccionados);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        ivFotoPreview = findViewById(R.id.ivFotoRutina);

        // Listener para abrir el diálogo de selección de días
        tvDiasSeleccionados.setOnClickListener(v -> mostrarDialogoDias());

        // Se comprueba si se ha abierto la Activity en modo edición
        modoEdicion = getIntent().getBooleanExtra("modo_edicion", false);
        if (modoEdicion) {
            rutinaId = getIntent().getIntExtra("rutina_id", -1);
            if (rutinaId != -1) {
                cargarDatosRutina(rutinaId);
                btnGuardar.setText("Actualizar rutina");
            }
        }

        // Listener para seleccionar imágenes
        btnSeleccionarFoto.setOnClickListener(
                v -> seleccionarMultiplesFotos.launch(new String[]{"image/*"})
        );

        // Listener para guardar o actualizar la rutina
        btnGuardar.setOnClickListener(v -> guardarRutina());
    }

    /**
     * Muestra un diálogo de selección múltiple para elegir los días de la semana
     * en los que se realizará la rutina.
     */
    private void mostrarDialogoDias() {
        String[] diasOpciones = new String[diasSemana.length + 1];
        diasOpciones[0] = "Todos los días";
        System.arraycopy(diasSemana, 0, diasOpciones, 1, diasSemana.length);

        boolean[] seleccion = new boolean[diasOpciones.length];
        seleccion[0] = todosLosDias;
        for (int i = 0; i < diasSemana.length; i++) {
            seleccion[i + 1] = diasSeleccionados[i];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona los días");
        builder.setMultiChoiceItems(diasOpciones, seleccion, (dialogInterface, which, isChecked) -> {
            if (which == 0) { // Opción "Todos los días"
                todosLosDias = isChecked;
                if (todosLosDias) {
                    // Si se selecciona "Todos los días", se desmarcan los demás
                    for (int j = 0; j < diasSemana.length; j++) {
                        diasSeleccionados[j] = false;
                    }
                }
            } else {
                // Si no está activo "Todos los días", se permite marcar días individuales
                if (!todosLosDias) {
                    diasSeleccionados[which - 1] = isChecked;
                }
            }
        });

        builder.setPositiveButton("Aceptar", (d, w) -> actualizarTextoDias());
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * Actualiza el TextView que muestra los días seleccionados al usuario.
     */
    private void actualizarTextoDias() {
        if (todosLosDias) {
            tvDiasSeleccionados.setText("Todos los días");
        } else {
            List<String> seleccionados = new ArrayList<>();
            for (int i = 0; i < diasSemana.length; i++) {
                if (diasSeleccionados[i]) seleccionados.add(diasSemana[i]);
            }

            if (seleccionados.isEmpty()) {
                tvDiasSeleccionados.setText("Seleccionar días");
            } else {
                tvDiasSeleccionados.setText(TextUtils.join(", ", seleccionados));
            }
        }
    }

    /**
     * Valida los datos introducidos por el usuario y guarda o actualiza
     * la rutina en la base de datos.
     */
    private void guardarRutina() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();

        int duracion;
        try {
            duracion = Integer.parseInt(etDuracion.getText().toString().trim());
        } catch (Exception e) {
            Toast.makeText(this, "Duración inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de campos obligatorios
        if (nombre.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Procesado de los días seleccionados
        List<String> dias = new ArrayList<>();
        if (todosLosDias) {
            dias.add("Todos los días");
        } else {
            for (int i = 0; i < diasSemana.length; i++) {
                if (diasSeleccionados[i]) dias.add(diasSemana[i]);
            }
            if (dias.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("tipo", tipo);
        values.put("duracion", duracion);
        values.put("dia_semana", TextUtils.join(",", dias));

        // Guardado de las imágenes seleccionadas
        List<String> rutas = new ArrayList<>();
        for (Uri uri : fotosSeleccionadas) {
            String ruta = guardarImagenInterna(uri);
            if (ruta != null) rutas.add(ruta);
        }

        if (!rutas.isEmpty()) {
            values.put("fotos_rutina", TextUtils.join(",", rutas));
        }

        // Inserción o actualización en función del modo
        if (modoEdicion) {
            db.update("rutinas", values, "id=?", new String[]{String.valueOf(rutinaId)});
            Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
        } else {
            db.insert("rutinas", null, values);
            Toast.makeText(this, "Rutina guardada", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * Carga los datos de una rutina existente para permitir su edición.
     */
    private void cargarDatosRutina(int rutinaId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre, descripcion, tipo, duracion, dia_semana, fotos_rutina FROM rutinas WHERE id=?",
                new String[]{String.valueOf(rutinaId)}
        );

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0));
            etDescripcion.setText(cursor.getString(1));
            etTipo.setText(cursor.getString(2));
            etDuracion.setText(String.valueOf(cursor.getInt(3)));

            // Reconstrucción de los días seleccionados
            String dias = cursor.getString(4);
            if (!TextUtils.isEmpty(dias)) {
                if (dias.equals("Todos los días")) {
                    todosLosDias = true;
                } else {
                    String[] guardados = dias.split(",");
                    for (int i = 0; i < diasSemana.length; i++) {
                        diasSeleccionados[i] = false;
                        for (String d : guardados) {
                            if (diasSemana[i].equals(d)) {
                                diasSeleccionados[i] = true;
                            }
                        }
                    }
                }
                actualizarTextoDias();
            }

            // Carga de la primera imagen como vista previa
            String fotos = cursor.getString(5);
            if (!TextUtils.isEmpty(fotos)) {
                String[] rutas = fotos.split(",");
                ivFotoPreview.setImageURI(Uri.fromFile(new File(rutas[0])));
            }
        }

        cursor.close();
    }

    /**
     * Copia una imagen seleccionada al almacenamiento interno de la aplicación
     * y devuelve su ruta para almacenarla en la base de datos.
     */
    private String guardarImagenInterna(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "rutina_" + System.currentTimeMillis() + ".jpg");
            OutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            in.close();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
