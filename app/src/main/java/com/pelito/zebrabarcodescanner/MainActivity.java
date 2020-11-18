package com.pelito.zebrabarcodescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements BarcodeScannerInterface.BarcodeScannerListener {

    BarcodeScannerInterface barcodeScannerZebra;
    TextView codeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView tvBuildeModel = findViewById(R.id.tvBuildeModel);
        tvBuildeModel.setText(Build.MODEL);

        codeTextView = findViewById(R.id.textview);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcodeScannerZebra = BarcodeScannerZebra.getBarcodeScanner(MainActivity.this);
                barcodeScannerZebra.activar();
                barcodeScannerZebra.setScannerListener(MainActivity.this);
                barcodeScannerZebra.activarLectura();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void codigoLido(String codigo) {
        codeTextView.setText(codigo);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}