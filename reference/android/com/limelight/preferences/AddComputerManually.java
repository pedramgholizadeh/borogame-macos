package com.limelight.preferences;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.limelight.PcView;
import com.limelight.ProfileSectionActivity;
import com.limelight.R$id;
import com.limelight.R$layout;
import com.limelight.R$string;
import com.limelight.Store;
import com.limelight.TicketsActivity;
import com.limelight.computers.ComputerManagerService;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.jni.MoonBridge;
import com.limelight.utils.Dialog;
import com.limelight.utils.SpinnerDialog;
import com.limelight.utils.UiHelper;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class AddComputerManually extends Activity {
    private static final LinkedBlockingQueue computersToAdd = new LinkedBlockingQueue();
    private static TextView hostText;
    private Thread addThread;
    private ComputerManagerService.ComputerManagerBinder managerBinder;
    private final ServiceConnection serviceConnection = new ServiceConnection() { // from class: com.limelight.preferences.AddComputerManually.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder binder) {
            AddComputerManually.this.managerBinder = (ComputerManagerService.ComputerManagerBinder) binder;
            AddComputerManually.this.startAddThread();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            AddComputerManually.this.joinAddThread();
            AddComputerManually.this.managerBinder = null;
        }
    };

    private boolean isWrongSubnetSiteLocalAddress(String address) {
        try {
            InetAddress targetAddress = InetAddress.getByName(address);
            if (!(targetAddress instanceof Inet4Address) || !targetAddress.isSiteLocalAddress()) {
                return false;
            }
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if ((addr.getAddress() instanceof Inet4Address) && addr.getAddress().isSiteLocalAddress()) {
                        byte[] targetAddrBytes = targetAddress.getAddress();
                        byte[] ifaceAddrBytes = addr.getAddress().getAddress();
                        boolean addressMatches = true;
                        for (int i = 0; i < addr.getNetworkPrefixLength(); i++) {
                            if ((ifaceAddrBytes[i / 8] & (1 << (i % 8))) != (targetAddrBytes[i / 8] & (1 << (i % 8)))) {
                                addressMatches = false;
                                break;
                            }
                        }
                        if (addressMatches) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private URI parseRawUserInputToUri(String rawUserInput) {
        try {
            URI uri = new URI("moonlight://" + rawUserInput);
            if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                return uri;
            }
        } catch (URISyntaxException e) {
        }
        try {
            URI uri2 = new URI("moonlight://[" + rawUserInput + "]");
            if (uri2.getHost() != null && !uri2.getHost().isEmpty()) {
                return uri2;
            }
            return null;
        } catch (URISyntaxException e2) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doAddPc(String rawUserInput) throws InterruptedException {
        boolean success;
        int portTestResult;
        String dialogText;
        boolean wrongSiteLocal = false;
        boolean invalidInput = false;
        SpinnerDialog dialog = SpinnerDialog.displayDialog(this, getResources().getString(R$string.title_add_pc), getResources().getString(R$string.msg_add_pc), false);
        try {
            ComputerDetails details = new ComputerDetails();
            URI uri = parseRawUserInputToUri(rawUserInput);
            if (uri != null && uri.getHost() != null && !uri.getHost().isEmpty()) {
                String host = uri.getHost();
                int port = uri.getPort();
                if (port == -1) {
                    port = 47989;
                }
                details.manualAddress = new ComputerDetails.AddressTuple(host, port);
                success = this.managerBinder.addComputerBlocking(details);
                if (!success) {
                    wrongSiteLocal = isWrongSubnetSiteLocalAddress(host);
                }
            } else {
                success = false;
                invalidInput = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            success = false;
            invalidInput = true;
        } catch (InterruptedException e2) {
            dialog.dismiss();
            throw e2;
        }
        if (!success && !wrongSiteLocal && !invalidInput) {
            portTestResult = MoonBridge.testClientConnectivity("android.conntest.moonlight-stream.org", 443, 3);
        } else {
            portTestResult = -1;
        }
        dialog.dismiss();
        if (invalidInput) {
            Dialog.displayDialog((Activity) this, getResources().getString(R$string.conn_error_title), getResources().getString(R$string.addpc_unknown_host), false);
            return;
        }
        if (wrongSiteLocal) {
            Dialog.displayDialog((Activity) this, getResources().getString(R$string.conn_error_title), getResources().getString(R$string.addpc_wrong_sitelocal), false);
            return;
        }
        if (!success) {
            if (portTestResult != -1 && portTestResult != 0) {
                dialogText = getResources().getString(R$string.nettest_text_blocked);
            } else {
                dialogText = getResources().getString(R$string.addpc_fail);
            }
            Dialog.displayDialog((Activity) this, getResources().getString(R$string.conn_error_title), dialogText, false);
            return;
        }
        runOnUiThread(new Runnable() { // from class: com.limelight.preferences.AddComputerManually.2
            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(AddComputerManually.this, AddComputerManually.this.getResources().getString(R$string.addpc_success), 1).show();
                if (!AddComputerManually.this.isFinishing()) {
                    AddComputerManually.this.finish();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAddThread() {
        this.addThread = new Thread() { // from class: com.limelight.preferences.AddComputerManually.3
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                while (!isInterrupted()) {
                    try {
                        String computer = (String) AddComputerManually.computersToAdd.take();
                        AddComputerManually.this.doAddPc(computer);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        this.addThread.setName("UI - AddComputerManually");
        this.addThread.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void joinAddThread() {
        if (this.addThread != null) {
            this.addThread.interrupt();
            try {
                this.addThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            this.addThread = null;
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", 0);
        sharedPreferences.getString("userLicense", null);
        Dialog.closeDialogs();
        SpinnerDialog.closeDialogs(this);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Intent intent = new Intent(this, (Class<?>) Store.class);
        startActivity(intent, null);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", 0);
        String userLicense = sharedPreferences.getString("userLicense", null);
        if (this.managerBinder != null) {
            joinAddThread();
            unbindService(this.serviceConnection);
        }
        if (userLicense != null) {
            Intent intent = new Intent(this, (Class<?>) PcView.class);
            startActivity(intent);
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiHelper.setLocale(this);
        setContentView(R$layout.activity_add_computer_manually);
        final View noLicense = findViewById(R$id.no_license);
        noLicense.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        UiHelper.notifyNewRootView(this);
        ImageButton gameStoreButton = (ImageButton) findViewById(R$id.store_button);
        gameStoreButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        ImageButton profileSectionButton = (ImageButton) findViewById(R$id.profileSection);
        profileSectionButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$2(view);
            }
        });
        ImageButton ticketButton = (ImageButton) findViewById(R$id.ticket_button_store);
        ticketButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$3(view);
            }
        });
        ImageButton settingButton = (ImageButton) findViewById(R$id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$4(view);
            }
        });
        ImageButton playgroundButton = (ImageButton) findViewById(R$id.playground_button);
        playgroundButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$5(view);
            }
        });
        hostText = (TextView) findViewById(R$id.hostTextView);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", 0);
        final String userLicense = sharedPreferences.getString("userLicense", null);
        final View progressBar = findViewById(R$id.progressBar);
        progressBar.setVisibility(0);
        if (userLicense != null) {
            noLicense.setVisibility(4);
            new Handler().postDelayed(new Runnable() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onCreate$10(userLicense, progressBar, noLicense);
                }
            }, 2000L);
        } else {
            noLicense.setVisibility(0);
            progressBar.setVisibility(4);
            View loader = findViewById(R$id.loader);
            loader.setVisibility(4);
        }
        hostText.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.limelight.preferences.AddComputerManually.4
            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == 6 || (keyEvent != null && keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 66)) {
                    return AddComputerManually.this.handleDoneEvent();
                }
                if (actionId != 7) {
                    return false;
                }
                InputMethodManager imm = (InputMethodManager) AddComputerManually.this.getSystemService("input_method");
                imm.hideSoftInputFromWindow(AddComputerManually.hostText.getWindowToken(), 0);
                return false;
            }
        });
        findViewById(R$id.addPcButton).setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                AddComputerManually.this.handleDoneEvent();
            }
        });
        bindService(new Intent(this, (Class<?>) ComputerManagerService.class), this.serviceConnection, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View v) {
        Intent intent = new Intent(this, (Class<?>) Store.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(View v) {
        Intent intent = new Intent(this, (Class<?>) TicketsActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(View v) {
        Intent intent = new Intent(this, (Class<?>) StreamSettings.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$5(View v) {
        Intent intent = new Intent(this, (Class<?>) AddComputerManually.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$10(final String userLicense, final View progressBar, final View noLicense) {
        String url = "https://borogame.com/api.php?fetchHost=" + userLicense;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0, url, null, new Response.Listener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda7
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                this.f$0.lambda$onCreate$8(progressBar, userLicense, noLicense, (JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda8
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$8(View progressBar, final String userLicense, View noLicense, JSONObject response) {
        try {
            String ip = response.getString("ip");
            hostText.setText(ip);
            if (!ip.equals("0")) {
                handleDoneEvent();
            } else {
                View loader = findViewById(R$id.loader);
                loader.setVisibility(4);
                progressBar.setVisibility(4);
                View NoGameFound = findViewById(R$id.no_pc_found_layout);
                NoGameFound.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda9
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onCreate$6(userLicense, view);
                    }
                });
                NoGameFound.setVisibility(0);
                View NoGameFoundButton = findViewById(R$id.createPlayGround);
                NoGameFoundButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.preferences.AddComputerManually$$ExternalSyntheticLambda10
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onCreate$7(userLicense, view);
                    }
                });
                noLicense.setVisibility(4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$6(String userLicense, View v) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://borogame.com/api.php?sso=" + userLicense + "&path=games.php"));
        startActivity(browserIntent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$7(String userLicense, View v) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://borogame.com/api.php?sso=" + userLicense + "&path=games.php"));
        startActivity(browserIntent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handleDoneEvent() {
        String hostAddress = hostText.getText().toString().trim();
        if (hostAddress.length() == 0) {
            Toast.makeText(this, getResources().getString(R$string.addpc_enter_ip), 1).show();
            return true;
        }
        computersToAdd.add(hostAddress);
        return false;
    }
}
