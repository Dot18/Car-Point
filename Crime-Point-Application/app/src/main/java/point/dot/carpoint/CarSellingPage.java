package point.dot.carpoint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;
import point.dot.carpoint.Helper.InternetCheck;

//this class is used for cloud or local image labelling using Google Firebase MLKit.
public class CarSellingPage extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetect, nextButton;

    android.app.AlertDialog waitingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_selling_page);

        cameraView = (CameraView)findViewById(R.id.camera_view);
        btnDetect = (Button)findViewById(R.id.ImageRecButton);
        nextButton = (Button)findViewById(R.id.ImageRecNextButton);

        //progress bar (with moving points)
        waitingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recognising ... ")
                .setCancelable(false)
                .build();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(),cameraView.getHeight(),false);

                cameraView.stop();
                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarSellingPage.this, CarDetailsPrice.class);
                startActivity(intent);
            }
        });
    }

    private void runDetector(Bitmap bitmap) {

        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        //checking if a user has internet connection or not. Running in cloud servers or locally
        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
                if(internet)
                //cloud - more than 11K labels
                {
                    FirebaseVisionCloudDetectorOptions options =
                            new FirebaseVisionCloudDetectorOptions.Builder()
                            .setMaxResults(1)          //only one result, but with highest confidence
                            .build();
                    FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);

                    detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {

                                    processDataResultCloud(firebaseVisionCloudLabels);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("image recognition error", e.getMessage());
                                }
                            });
                }
                //offline - about 400 labels
                else
                {
                    FirebaseVisionLabelDetectorOptions options =
                            new FirebaseVisionLabelDetectorOptions.Builder()
                                    .setConfidenceThreshold(0.8f)          //only one result, but with highest confidence
                                    .build();
                    FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);

                    detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                                    processDataResultOffline(firebaseVisionLabels);
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("image recognition error", e.getMessage());
                                }
                            });
                }
            }

            //show the image label for Cloud processing
            private void processDataResultCloud(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {

                for(FirebaseVisionCloudLabel label : firebaseVisionCloudLabels)
                {
                    Toast.makeText(getApplicationContext(),"Cloud results: "+label.getLabel()+ " and percentage: " +label.getConfidence(), Toast.LENGTH_LONG).show();  //instead of this - getApplicationContext was used
                }
                if(waitingDialog.isShowing())
                    waitingDialog.dismiss();
            }

            //show the image label for offline processing
            private void processDataResultOffline(List<FirebaseVisionLabel> firebaseVisionLabels) {

                for(FirebaseVisionLabel label : firebaseVisionLabels)
                {
                    Toast.makeText(getApplicationContext(), "Offline results: "+label.getLabel() + " and percentage: " +label.getConfidence(), Toast.LENGTH_LONG).show();  //instead of this - getApplicationContext was used
                }

                if(waitingDialog.isShowing())
                    waitingDialog.dismiss();
            }
        });
    }
}
