package com.example.test3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.test3.dal.SQLiteHelper;
import com.example.test3.model.Song;

public class UpdateDeleteActivity extends AppCompatActivity implements View.OnClickListener {
    private Spinner inputGenre;
    private EditText inputName, inputSinger, inputAlbum;
    private Button btnUpdate, btnRemove, btnBack;

    private Song song;
    private SQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete);
        initView();
        db = new SQLiteHelper(this);
        btnUpdate.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("song");

        inputName.setText(song.getName());
        inputSinger.setText(song.getSinger());
        inputAlbum.setText(song.getAlbum());
        ArrayAdapter<String> genres = new ArrayAdapter<>(
                this, R.layout.item_spinner, getResources().getStringArray(R.array.category));

        for (int p = 0; p < inputGenre.getCount(); p++) {
            if (inputGenre.getItemAtPosition(p).equals(song.getGenre())) {
                inputGenre.setSelection(p);
                break;
            }
        }
    }

    private void initView() {
        inputGenre = findViewById(R.id.inputGenre);
        inputName = findViewById(R.id.inputName);
        inputSinger = findViewById(R.id.inputSinger);
        inputAlbum = findViewById(R.id.inputAlbum);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnRemove = findViewById(R.id.btnRemove);
        btnBack = findViewById(R.id.btnBack);

        inputGenre.setAdapter(new ArrayAdapter<String>(
                this, R.layout.item_spinner, getResources().getStringArray(R.array.category)));
    };

    @Override
    public void onClick(View v) {
        if (v == btnUpdate) {
            long id = song.getId();
            String name = inputName.getText().toString();
            String singer = inputSinger.getText().toString();
            String album = inputAlbum.getText().toString();
            String genre = inputGenre.getSelectedItem().toString();

            if (name.trim().equals("") || singer.trim().equals("") || album.trim().equals("")) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                Song song = new Song(id, name, singer, album, genre);
                db.updateItem(song);
                finish();
            }
        } else if (v == btnRemove) {
            long id = song.getId();
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Thông báo xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa " + song.getName() + " không?");
            builder.setIcon(R.drawable.icon_delete);
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.deleteItem(id);
                    finish();
                }
            });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (v == btnBack) {
            finish();
        }
    }
}