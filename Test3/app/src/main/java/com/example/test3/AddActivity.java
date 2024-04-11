package com.example.test3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.test3.dal.SQLiteHelper;
import com.example.test3.model.Song;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {
    private Spinner inputGenre;
    private EditText inputName, inputSinger, inputAlbum;
    private Button btnAdd, btnBack;
    private SQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        initView();

        btnAdd.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void initView() {
        inputGenre = findViewById(R.id.inputGenre);
        inputName = findViewById(R.id.inputName);
        inputSinger = findViewById(R.id.inputSinger);
        inputAlbum = findViewById(R.id.inputAlbum);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        db = new SQLiteHelper(getApplicationContext());

        inputGenre.setAdapter(new ArrayAdapter<String>(
                this, R.layout.item_spinner, getResources().getStringArray(R.array.category)));
    }

    @Override
    public void onClick(View v) {
        if (v == btnAdd) {
            String name = inputName.getText().toString();
            String singer = inputSinger.getText().toString();
            String album = inputAlbum.getText().toString();
            String genre = inputGenre.getSelectedItem().toString();

            if (name.trim().equals("") || singer.trim().equals("") || album.trim().equals("")) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                Song song = new Song(name, singer, album, genre);
                db.addItem(song);
                finish();
            }
        } else if (v == btnBack) {
            finish();
        }
    }
}