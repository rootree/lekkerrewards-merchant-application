package com.lekkerrewards.merchant.activities;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.lekkerrewards.merchant.LekkerApplication;
import com.lekkerrewards.merchant.R;
import com.lekkerrewards.merchant.entities.Customer;
import com.lekkerrewards.merchant.entities.Qr;
import com.lekkerrewards.merchant.exceptions.CheckInException;
import com.splunk.mint.Mint;

import java.util.List;

public class ScanningActivity extends BaseActivity {

    private static final String TAG = ScanningActivity.class.getSimpleName();
    private CompoundBarcodeView barcodeView;
    private Dialog dialog;

    private BarcodeCallback callback = new BarcodeCallback() {

        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {

                barcodeView.pause();

                String QR = result.getText();
                LekkerApplication.lastQR = QR;

                Qr qrCard;
                try {

                    qrCard = ((LekkerApplication) getApplication()).getQRByScannedCode(QR);

                } catch (Exception e) {

                    Mint.logException(e);

                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(LekkerApplication.class.toString(), e.getMessage(), e);

                    barcodeView.destroyDrawingCache();
                    barcodeView.resume();

                    returnBack();

                    return;
                }

                Customer customer = qrCard.fkCustomer;

                if (customer == null) {

                    Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                    intent.putExtra("qr-code", qrCard.code);
                    startActivity(intent);

                } else {

                    try {

                        ((LekkerApplication) getApplication()).checkInByQr(qrCard);

                        Intent intent = new Intent(getApplicationContext(), QrCheckConfirmedActivity.class);
                        intent.putExtra("user-age", 30);
                        intent.putExtra("user-name", "Roman");

                        startActivity(intent);

                        finish();

                    } catch (CheckInException e) {


                        Intent intent = new Intent(getApplicationContext(), CustomerActivity.class);
                        intent.putExtra("already_check_in", e.getMessage());
                        startActivity(intent);

                        finish();

                    } catch (Exception e) {

                        Mint.logException(e);

                        Log.d(LekkerApplication.class.toString(), e.getMessage(), e);

                        ((LekkerApplication) getApplication()).showMessage(e.getMessage());
                        returnBack();

                    }

                }

                barcodeView.destroyDrawingCache();

                //barcodeView.resume();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ((LekkerApplication) getApplication()).setCurrentActivity(this);

        setContentView(R.layout.scanning_qr);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        Button btnTwo = (Button) findViewById(R.id.return_btn);

        btnTwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                returnBack();
            }
        });


        Button btnStart = (Button) findViewById(R.id.checkIn_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(getApplicationContext(), EmailCheckActivity.class);
                startActivity(intent);
                finish();
            }
        });

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeSingle(callback);

        barcodeView.setStatusText(null);

        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        settings.setContinuousFocusEnabled(true);
        settings.setMeteringEnabled(true);
        settings.setBarcodeSceneModeEnabled(true);
        settings.setAutoFocusEnabled(true);
        settings.setAutoTorchEnabled(true);
        settings.setExposureEnabled(true);
        barcodeView.getBarcodeView().setCameraSettings(settings);

    }

    public void onClickTurmsLink(View v) {

        dialog = new Dialog(this); // context, this etc.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.web_dialog);
        dialog.setTitle(R.string.turms_of_use);
        dialog.show();

        WebView webView = (WebView)dialog.findViewById(R.id.dialog_info);
        webView.loadUrl("file:///android_asset/turm_of_use.html");

        Button btnStart = (Button) dialog.findViewById(R.id.dialog_ok);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }

    public void onClickPrivacyLink(View v) {
        dialog = new Dialog(this); // context, this etc.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.web_dialog);
        dialog.setTitle(R.string.privacy_policy);
        dialog.show();

        WebView webView = (WebView)dialog.findViewById(R.id.dialog_info);
        webView.loadUrl("file:///android_asset/privacy_policy.html");

        Button btnStart = (Button) dialog.findViewById(R.id.dialog_ok);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.destroyDrawingCache();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {

        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.destroyDrawingCache();
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

     private void returnBack(){
         Intent intent = new Intent();
         intent.putExtra("dog-years", 210);
         setResult(RESULT_OK, intent);
         finish();
         barcodeView.destroyDrawingCache();
     }

    @Override
      public void onBackPressed() {
    }
}

