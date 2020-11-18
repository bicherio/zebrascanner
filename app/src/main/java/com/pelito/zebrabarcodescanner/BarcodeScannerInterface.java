package com.pelito.zebrabarcodescanner;

import android.view.KeyEvent;

public interface BarcodeScannerInterface {
        void activar();
        void desactivar();
        void activarLectura();
        void desactivarLectura();
        void dispatchKeyEventPreIme(KeyEvent event);
        void setScannerListener(BarcodeScannerListener listener);

        interface BarcodeScannerListener {
            void codigoLido(String codigo);
        }
}
