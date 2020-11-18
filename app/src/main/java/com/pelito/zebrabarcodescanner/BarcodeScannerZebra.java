package com.pelito.zebrabarcodescanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;

import java.util.ArrayList;

class BarcodeScannerZebra implements BarcodeScannerInterface, EMDKManager.EMDKListener, Scanner.DataListener {
    private Context context;
    private BarcodeScannerListener listener;
    private EMDKManager emdkManager = null;
    private Scanner scanner = null;
    public static final String TAG = "BarcodeScannerZebra";
    long lastMilisegundos;
    boolean read = false;
    private EMDKResults results;

    public BarcodeScannerZebra(Context context) {
        this.context = context;
    }

    @Override public void activar() {
        try {
            if(results == null) {
                results = EMDKManager.getEMDKManager(context, this);
                if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                    Log.d("ERROR",
                            "Status: EMDKManager object request failed! (" + results.statusCode + ") on activar() in BarcodeScannerZebra class");
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    @Override public void desactivar() {
        try {

            // releases the scanner hardware resources for other application
            // to use. You must call this as soon as you're done with the
            // scanning.
            if ((scanner != null) && (scanner.isEnabled())) {
                scanner.disable();
                scanner = null;
            }

            // Clean up the objects created by EMDK manager
            if ((emdkManager != null)) {
                try {
                    emdkManager.release();
                    emdkManager = null;
                } catch (Exception ex) {
                    //No es necesario lanzar excepciones aquí.
                }
            }
            results = null;
            Intent i = new Intent();
            i.setAction("com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN");
            i.putExtra("com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER", "DISABLE_PLUGIN");
            context.sendBroadcast(i);
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    @Override public void activarLectura() {
        try {
            activar();
            if (scanner != null) {
                scanner.addDataListener(this);
                scanner.read();
            } else {
                read = true;
            }
            Log.d(TAG, "scanner.read: " + System.currentTimeMillis());
        } catch (Exception e) {
            read = true;
        }
    }

    @Override public void desactivarLectura() {
        try {
            if (scanner != null) {
                scanner.cancelRead();
                scanner.removeDataListener(this);
            }
            read = false;
        } catch (Exception e) {
            //Não precisa logar erro aqui.
        }
    }

    @Override public void dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (event.getKeyCode()
                == KeyEvent.KEYCODE_BUTTON_L1 || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R1)
                && event.getRepeatCount() == 0) {
            Log.d(TAG, "dispatchKeyEventPreIme: " + System.currentTimeMillis());
            activarLectura();
        } else if (event.getAction() == KeyEvent.ACTION_UP && (event.getKeyCode()
                == KeyEvent.KEYCODE_BUTTON_L1 || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R1)) {
            desactivarLectura();
        }
    }

    @Override public void setScannerListener(BarcodeScannerListener listener) {
        this.listener = listener;
    }

    @Override public void onOpened(EMDKManager emdkManager) {
        try {
            this.emdkManager = emdkManager;
            // Get the Barcode Manager object
            BarcodeManager barcodeManager =
                    (BarcodeManager) this.emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            // scanner = barcodeManager.getDevice(list.get(0));
            // Enable the scanner
            while (scanner == null) {
                Thread.sleep(200);
                scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            }
            scanner.enable();
            // Add data and status listeners
            scanner.addDataListener(this);
            //scanner.addStatusListener(this);
            // The trigger type is set to HARD by default and HARD is not
            // implemented in this release.
            // So set to SOFT_ALWAYS
            scanner.triggerType = Scanner.TriggerType.SOFT_ALWAYS;
            configuracion();
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        } finally {
            if (read) {
                try {
                    scanner.read();
                } catch (ScannerException e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        }
    }

    private void configuracion() {
        try {
            //Set some decoder parameters to scanner
            ScannerConfig config = scanner.getConfig();
            config.decoderParams.code128.enabled = true;
            config.decoderParams.code128.length1 = 2;
            config.decoderParams.code128.length2 = 20;
            config.decoderParams.code39.enabled = true;
            config.decoderParams.code39.length1 = 2;
            config.decoderParams.code39.length2 = 20;
            config.decoderParams.i2of5.enabled = true;
            config.decoderParams.i2of5.length1 = 2;
            config.decoderParams.i2of5.length2 = 14;
            config.readerParams.readerSpecific.laserSpecific.beamTimer = 5000;
            config.readerParams.readerSpecific.laserSpecific.continuousRead.isContinuousScan = true;
            config.readerParams.readerSpecific.laserSpecific.continuousRead.differentSymbolTimeout = 5000;
            config.readerParams.readerSpecific.laserSpecific.continuousRead.sameSymbolTimeout = 5000;
            config.decoderParams.pdf417.enabled = true;

            config.readerParams.readerSpecific.imagerSpecific.pickList = ScannerConfig.PickList.ENABLED;

            scanner.setConfig(config);
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    @Override public void onClosed() {
        try {
            if (emdkManager != null) {
                // Release all the resources
                emdkManager.release();
                emdkManager = null;
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    @Override public void onData(ScanDataCollection scanDataCollection) {
        try {
            if ((scanDataCollection != null) && (scanDataCollection.getResult()
                    == ScannerResults.SUCCESS)) {
                ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
                for (ScanDataCollection.ScanData data : scanData) {
                    if (listener != null) {
                        if ((System.currentTimeMillis() - lastMilisegundos) > 150) {
                            listener.codigoLido(data.getData());
                        }
                        lastMilisegundos = System.currentTimeMillis();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        } finally {
            desactivarLectura();
        }
    }

    public static BarcodeScannerInterface getBarcodeScanner(Context context){
        switch (Build.MODEL) {
            //        const val ZEBRA_TC55 = "TC55"
            //        const val ZEBRA_TC75 = "TC75"
            case "TC55":
            case "TC75":
                return new BarcodeScannerZebra(context);

        }

        return null;
    }
}
