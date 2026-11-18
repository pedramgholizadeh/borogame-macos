package com.limelight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.limelight.preferences.AddComputerManually;
import com.limelight.preferences.StreamSettings;
import com.limelight.utils.UiHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class TicketsActivity extends Activity {
    private LinearLayout licenseSection;
    private Button newTicket;
    private LinearLayout noTicket;
    private SharedPreferences sharedPreferences;
    private LinearLayout ticketsSection;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R$layout.activity_tickets);
        this.sharedPreferences = getSharedPreferences("AppPreferences", 0);
        UiHelper.setLocale(this);
        this.licenseSection = (LinearLayout) findViewById(R$id.no_license_layout);
        this.ticketsSection = (LinearLayout) findViewById(R$id.ticketsSection);
        this.noTicket = (LinearLayout) findViewById(R$id.no_ticket_found_layout);
        this.newTicket = (Button) findViewById(R$id.newticket);
        checkStoredLicense();
        ImageButton gameStoreButton = (ImageButton) findViewById(R$id.store_button);
        gameStoreButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        ImageButton profileSectionButton = (ImageButton) findViewById(R$id.profileSection);
        profileSectionButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        ImageButton ticketButton = (ImageButton) findViewById(R$id.ticket_button_store);
        ticketButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$2(view);
            }
        });
        ImageButton settingButton = (ImageButton) findViewById(R$id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$3(view);
            }
        });
        ImageButton playgroundButton = (ImageButton) findViewById(R$id.playground_button);
        playgroundButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$4(view);
            }
        });
        this.licenseSection.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$5(view);
            }
        });
        this.newTicket.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$6(view);
            }
        });
        this.noTicket.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$7(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View v) {
        Intent intent = new Intent(this, (Class<?>) Store.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View v) {
        Intent intent = new Intent(this, (Class<?>) TicketsActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(View v) {
        Intent intent = new Intent(this, (Class<?>) StreamSettings.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(View v) {
        Intent intent = new Intent(this, (Class<?>) AddComputerManually.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$5(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$6(View v) {
        String storedLicense = this.sharedPreferences.getString("userLicense", null);
        String ssoUrl = "https://borogame.com/api.php?sso=" + storedLicense + "&path=submitticket.php";
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(ssoUrl));
        startActivity(browserIntent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$7(View v) {
        String storedLicense = this.sharedPreferences.getString("userLicense", null);
        String ssoUrl = "https://borogame.com/api.php?sso=" + storedLicense + "&path=submitticket.php";
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(ssoUrl));
        startActivity(browserIntent);
    }

    private void checkStoredLicense() {
        String storedLicense = this.sharedPreferences.getString("userLicense", null);
        if (storedLicense != null) {
            this.licenseSection.setVisibility(8);
            fetchTickets(storedLicense);
            this.newTicket.setVisibility(0);
        } else {
            this.newTicket.setVisibility(8);
            this.licenseSection.setVisibility(0);
        }
    }

    private void fetchTickets(final String userLicense) {
        String url = "https://borogame.com/api.php?tickets=" + userLicense;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(0, url, null, new Response.Listener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda8
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                this.f$0.lambda$fetchTickets$8(userLicense, (JSONArray) obj);
            }
        }, new Response.ErrorListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda9
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                this.f$0.lambda$fetchTickets$9(volleyError);
            }
        });
        queue.add(jsonArrayRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchTickets$8(String userLicense, JSONArray response) {
        try {
            if (response.length() == 0) {
                this.noTicket.setVisibility(0);
                this.newTicket.setVisibility(0);
            } else {
                displayTickets(response, userLicense);
                this.noTicket.setVisibility(4);
                this.newTicket.setVisibility(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching tickets" + response.toString(), 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchTickets$9(VolleyError error) {
        error.printStackTrace();
        Toast.makeText(this, getResources().getString(R$string.no_ticket_found_toast), 0).show();
        this.noTicket.setVisibility(0);
    }

    private void displayTickets(JSONArray tickets, final String userLicense) throws JSONException {
        int i = 0;
        this.ticketsSection.setVisibility(0);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        int i2 = 1;
        linearLayout.setOrientation(1);
        int i3 = 0;
        while (i3 < tickets.length()) {
            JSONObject ticket = tickets.getJSONObject(i3);
            String subject = ticket.getString("subject");
            String deptname = ticket.getString("deptname");
            String status = ticket.getString("status");
            String date = ticket.getString("date");
            final String tid = ticket.getString("url");
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(i2);
            linearLayout2.setPadding(20, 25, 20, 25);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(i, i, i, 10);
            linearLayout2.setLayoutParams(params);
            linearLayout2.setBackground(createItemBackground());
            linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.TicketsActivity$$ExternalSyntheticLambda10
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$displayTickets$10(userLicense, tid, view);
                }
            });
            TextView subjectView = new TextView(this);
            subjectView.setText(subject);
            subjectView.setTextSize(18.0f);
            subjectView.setPadding(16, 0, 16, 15);
            linearLayout2.addView(subjectView);
            LinearLayout linearLayout3 = new LinearLayout(this);
            linearLayout3.setOrientation(0);
            linearLayout3.setPadding(16, 0, 16, 15);
            TextView deptnameView = new TextView(this);
            deptnameView.setText(getDeptText(deptname));
            deptnameView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            deptnameView.setGravity(3);
            linearLayout3.addView(deptnameView);
            TextView statusView = new TextView(this);
            statusView.setText(getStatusText(status));
            statusView.setTextColor(getStatusTextColor(status));
            statusView.setPadding(8, 8, 8, 8);
            GradientDrawable statusBackground = new GradientDrawable();
            statusBackground.setColor(getStatusColor(status));
            statusBackground.setCornerRadius(getResources().getDisplayMetrics().density * 5.0f);
            statusView.setBackground(statusBackground);
            linearLayout3.addView(statusView);
            linearLayout2.addView(linearLayout3);
            TextView dateView = new TextView(this);
            dateView.setText(date);
            dateView.setGravity(1);
            dateView.setPadding(16, 15, 16, 8);
            linearLayout2.addView(dateView);
            linearLayout.addView(linearLayout2);
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(-1, 20));
            linearLayout.addView(divider);
            i3++;
            i = 0;
            i2 = 1;
        }
        scrollView.addView(linearLayout);
        this.ticketsSection.addView(scrollView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$displayTickets$10(String userLicense, String tid, View v) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://borogame.com/api.php?sso=" + userLicense + "&path=api.php?" + tid));
        startActivity(browserIntent);
    }

    private GradientDrawable createItemBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(352321535);
        background.setCornerRadius(getResources().getDisplayMetrics().density * 10.0f);
        background.setStroke(1, 1090519039);
        return background;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:17:0x0030  */
    private int getStatusColor(String status) {
        switch (status) {
            case "Closed":
                return -13949388;
            case "Open":
                return -15324093;
            case "Answered":
                return -15060427;
            case "Customer-Reply":
                return -13751505;
            default:
                return -16777216;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:17:0x0031  */
    private int getStatusTextColor(String status) {
        switch (status) {
            case "Closed":
                return -2737863;
            case "Open":
                return -16357720;
            case "Answered":
                return -13852353;
            case "Customer-Reply":
                return -563449;
            default:
                return -1;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:17:0x0030  */
    private String getStatusText(String status) {
        switch (status) {
            case "Closed":
                return getResources().getString(R$string.closed);
            case "Open":
                return getResources().getString(R$string.open);
            case "Answered":
                return getResources().getString(R$string.answered);
            case "Customer-Reply":
                return getResources().getString(R$string.customer_Reply);
            default:
                return "Unknown Status";
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:14:0x0026  */
    private String getDeptText(String deptname) {
        switch (deptname) {
            case "پشتیبانی فنی":
                return getResources().getString(R$string.tech_support);
            case "پشتیبانی محصول":
                return getResources().getString(R$string.customer_service);
            case "امور مالی":
                return getResources().getString(R$string.financial_affair);
            default:
                return "Unknown";
        }
    }
}
