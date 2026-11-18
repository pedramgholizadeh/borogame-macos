package com.limelight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.limelight.preferences.AddComputerManually;
import com.limelight.preferences.StreamSettings;
import com.limelight.utils.UiHelper;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class ProfileSectionActivity extends Activity {
    private EditText licenseInput;
    private LinearLayout licenseSection;
    private LinearLayout profileDetails;
    private SharedPreferences sharedPreferences;
    private TextView validationMessage;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R$layout.profile_section);
        UiHelper.setLocale(this);
        this.sharedPreferences = getSharedPreferences("AppPreferences", 0);
        this.licenseInput = (EditText) findViewById(R$id.licenseInput);
        this.validationMessage = (TextView) findViewById(R$id.noGameMessage);
        this.licenseSection = (LinearLayout) findViewById(R$id.no_pc_found_layout);
        this.profileDetails = (LinearLayout) findViewById(R$id.profileDetails);
        Button validateButton = (Button) findViewById(R$id.validateButton);
        validateButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        Button deleteLicenseButton = (Button) findViewById(R$id.deleteLicenseButton);
        deleteLicenseButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        checkStoredLicense();
        ImageButton gameStoreButton = (ImageButton) findViewById(R$id.store_button);
        gameStoreButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$2(view);
            }
        });
        ImageButton profileSectionButton = (ImageButton) findViewById(R$id.profileSection);
        profileSectionButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$3(view);
            }
        });
        ImageButton ticketButton = (ImageButton) findViewById(R$id.ticket_button_store);
        ticketButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$4(view);
            }
        });
        ImageButton settingButton = (ImageButton) findViewById(R$id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$5(view);
            }
        });
        ImageButton playgroundButton = (ImageButton) findViewById(R$id.playground_button);
        playgroundButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$6(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View v) {
        validateLicense();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View v) {
        confirmDeleteLicense();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View v) {
        Intent intent = new Intent(this, (Class<?>) Store.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(View v) {
        Intent intent = new Intent(this, (Class<?>) TicketsActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$5(View v) {
        Intent intent = new Intent(this, (Class<?>) StreamSettings.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$6(View v) {
        Intent intent = new Intent(this, (Class<?>) AddComputerManually.class);
        startActivity(intent);
    }

    private void checkStoredLicense() {
        String storedLicense = this.sharedPreferences.getString("userLicense", null);
        Button faqButton = (Button) findViewById(R$id.faqButton);
        final String faqSsoUrl = "https://borogame.com/api.php?sso=" + storedLicense + "&path=faq.php/";
        final String faqUrl = "https://borogame.com/faq.php";
        if (storedLicense != null) {
            this.licenseSection.setVisibility(8);
            fetchProfileDetails(storedLicense);
            faqButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda10
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$checkStoredLicense$7(faqSsoUrl, view);
                }
            });
        } else {
            this.licenseSection.setVisibility(0);
            faqButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda11
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$checkStoredLicense$8(faqUrl, view);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$checkStoredLicense$7(String faqSsoUrl, View v) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(faqSsoUrl));
        startActivity(browserIntent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$checkStoredLicense$8(String faqUrl, View v) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(faqUrl));
        startActivity(browserIntent);
    }

    private void validateLicense() {
        final String userLicense = this.licenseInput.getText().toString().trim();
        String url = "https://borogame.com/api.php?license=" + userLicense;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0, url, null, new Response.Listener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda8
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                this.f$0.lambda$validateLicense$9(userLicense, (JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda9
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                this.f$0.lambda$validateLicense$10(volleyError);
            }
        });
        queue.add(jsonObjectRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$validateLicense$9(String userLicense, JSONObject response) {
        try {
            if (response.getBoolean("validation")) {
                this.validationMessage.setText(getResources().getString(R$string.validation_success));
                this.validationMessage.setVisibility(0);
                this.sharedPreferences.edit().putString("userLicense", userLicense).apply();
                fetchProfileDetails(userLicense);
            } else {
                this.validationMessage.setText(getResources().getString(R$string.invalid_license));
                this.validationMessage.setVisibility(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.validationMessage.setText(getResources().getString(R$string.validation_error));
            this.validationMessage.setVisibility(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$validateLicense$10(VolleyError error) {
        error.printStackTrace();
        this.validationMessage.setText(getResources().getString(R$string.validation_error));
        this.validationMessage.setVisibility(0);
    }

    private void fetchProfileDetails(String userLicense) {
        String url = "https://borogame.com/api.php?license=" + userLicense;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0, url, null, new Response.Listener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda12
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                this.f$0.lambda$fetchProfileDetails$11((JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda13
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                this.f$0.lambda$fetchProfileDetails$12(volleyError);
            }
        });
        queue.add(jsonObjectRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchProfileDetails$11(JSONObject response) {
        try {
            String nic = response.getString("nic");
            String fullname = response.getString("fullName");
            String email = response.getString("email");
            int cards = response.getInt("cards");
            ((TextView) findViewById(R$id.avatar)).setText(nic);
            ((TextView) findViewById(R$id.fullname)).setText(fullname);
            ((TextView) findViewById(R$id.email)).setText(email);
            ((TextView) findViewById(R$id.cards)).setText("" + cards);
            this.profileDetails.setVisibility(0);
            this.licenseSection.setVisibility(8);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching profile details", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchProfileDetails$12(VolleyError error) {
        error.printStackTrace();
        Toast.makeText(this, "Error fetching profile details", 0).show();
    }

    private void confirmDeleteLicense() {
        new AlertDialog.Builder(this).setTitle(getResources().getString(R$string.delete_license_title)).setMessage(getResources().getString(R$string.delete_license_info)).setPositiveButton(getResources().getString(R$string.ok), new DialogInterface.OnClickListener() { // from class: com.limelight.ProfileSectionActivity$$ExternalSyntheticLambda7
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.lambda$confirmDeleteLicense$13(dialogInterface, i);
            }
        }).setNegativeButton(getResources().getString(R$string.cancel), (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmDeleteLicense$13(DialogInterface dialog, int which) {
        this.sharedPreferences.edit().remove("userLicense").apply();
        this.profileDetails.setVisibility(8);
        this.licenseSection.setVisibility(0);
    }
}
