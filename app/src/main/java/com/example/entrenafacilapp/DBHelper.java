package com.example.entrenafacilapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "entrenafacil.db";
    public static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Usuarios
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "contrasena TEXT, " +
                "edad TEXT, " +
                "peso TEXT, " +
                "altura TEXT, " +
                "sexo TEXT, " +
                "foto_perfil TEXT)");

        // Rutinas
        db.execSQL("CREATE TABLE rutinas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +
                "nombre TEXT, " +
                "descripcion TEXT, " +
                "tipo TEXT, " +
                "duracion INTEGER, " +
                "dia_semana TEXT)");

        // Progreso
        db.execSQL("CREATE TABLE progreso (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +
                "rutina_id INTEGER, " +
                "fecha TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
