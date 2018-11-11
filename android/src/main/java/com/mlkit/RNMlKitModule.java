
package com.mlkit;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RNMlKitModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private FirebaseVisionImage image;

  public RNMlKitModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.reactContext = reactContext;
  }

    @ReactMethod
    public void deviceTextRecognition(String uri, final Promise promise) {
      FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        recognizeText(uri, promise, detector);

    }

    @ReactMethod
    public void cloudTextRecognition(String uri, final Promise promise) {
      FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getCloudTextRecognizer();
      recognizeText(uri, promise, detector);
    }

    private void recognizeText(String uri, final Promise promise, final FirebaseVisionTextRecognizer detector) {
      try {
        image = FirebaseVisionImage.fromFilePath(this.reactContext, android.net.Uri.parse(uri));
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        promise.resolve(processResult(firebaseVisionText));
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                promise.reject(e);
                            }
                        });
        } catch (IOException e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void detectLandmark() {

    }

    /**
     * Converts firebaseVisionText into a map
     *
     * @param firebaseVisionText
     * @return
     */
    private WritableArray processResult(FirebaseVisionText firebaseVisionText) {
        WritableArray data = Arguments.createArray();
        WritableMap info;
        WritableMap coordinates;
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

        if (blocks.size() == 0) {
            return data;
        }

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            info = Arguments.createMap();
            coordinates = Arguments.createMap();

            Rect boundingBox = blocks.get(i).getBoundingBox();

            coordinates.putInt("top", boundingBox.top);
            coordinates.putInt("left", boundingBox.left);
            coordinates.putInt("width", boundingBox.width());
            coordinates.putInt("height", boundingBox.height());

            info.putMap("blockCoordinates", coordinates);
            info.putString("blockText", blocks.get(i).getText());
            info.putString("resultText", firebaseVisionText.getText());

            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                info.putString("lineText", lines.get(j).getText());

                for (int k = 0; k < elements.size(); k++) {
                    info.putString("elementText", elements.get(k).getText());
                }
            }

            data.pushMap(info);
        }

        return data;
    }


  @Override
  public String getName() {
    return "RNMlKit";
  }
}