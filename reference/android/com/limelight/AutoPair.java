package com.limelight;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/* JADX INFO: loaded from: classes.dex */
public class AutoPair {
    public void sendPinPairRequest(final String pinStr, final String ipAddress) {
        new Thread(new Runnable() { // from class: com.limelight.AutoPair$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                AutoPair.lambda$sendPinPairRequest$0(pinStr, ipAddress);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$sendPinPairRequest$0(String pinStr, String ipAddress) {
        try {
            String urlString = "https://borogame.com/api.php?pinPair=" + pinStr + "&ip=" + ipAddress;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlString).build();
            try {
                Response response = client.newCall(request).execute();
                try {
                    if (response.isSuccessful()) {
                        System.out.println("Request sent successfully.");
                    } else {
                        System.out.println("Failed to send request. Response code: " + response.code());
                    }
                    response.close();
                } catch (Throwable th) {
                    if (response != null) {
                        try {
                            response.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (IOException e) {
                System.err.println("Error during the request: " + e.getMessage());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
