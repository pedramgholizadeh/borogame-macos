package com.limelight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;

/* JADX INFO: loaded from: classes.dex */
public class Store extends Activity {
    private GameAdapter adapter;
    private List gameList = new ArrayList();
    private ListView listView;
    private SharedPreferences sharedPreferences;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R$xml.preferences, false);
        UiHelper.setLocale(this);
        setContentView(R$layout.store_grids);
        this.sharedPreferences = getSharedPreferences("AppPreferences", 0);
        this.listView = (ListView) findViewById(R$id.listView);
        this.listView.setDividerHeight(40);
        this.adapter = new GameAdapter(this, this.gameList);
        this.listView.setAdapter((ListAdapter) this.adapter);
        fetchGames();
        ImageButton profileSectionButton = (ImageButton) findViewById(R$id.profileSection);
        profileSectionButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        ImageButton ticketButton = (ImageButton) findViewById(R$id.ticket_button_store);
        ticketButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        ImageButton settingButton = (ImageButton) findViewById(R$id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$2(view);
            }
        });
        ImageButton gameStoreButton = (ImageButton) findViewById(R$id.store_button);
        gameStoreButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$3(view);
            }
        });
        ImageButton playgroundButton = (ImageButton) findViewById(R$id.playground_button);
        playgroundButton.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$4(view);
            }
        });
        completeOnCreate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View v) {
        Intent intent = new Intent(this, (Class<?>) ProfileSectionActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View v) {
        Intent intent = new Intent(this, (Class<?>) TicketsActivity.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View v) {
        Intent intent = new Intent(this, (Class<?>) StreamSettings.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(View v) {
        Intent intent = new Intent(this, (Class<?>) Store.class);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(View v) {
        Intent intent = new Intent(this, (Class<?>) AddComputerManually.class);
        startActivity(intent);
    }

    private void completeOnCreate() {
        UiHelper.setLocale(this);
        PreferenceManager.setDefaultValues(this, R$xml.preferences, true);
    }

    private void fetchGames() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(0, "https://borogame.com/api.php?store=true", null, new Response.Listener() { // from class: com.limelight.Store$$ExternalSyntheticLambda5
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                this.f$0.lambda$fetchGames$5((JSONArray) obj);
            }
        }, new Response.ErrorListener() { // from class: com.limelight.Store$$ExternalSyntheticLambda6
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                this.f$0.lambda$fetchGames$6(volleyError);
            }
        });
        queue.add(jsonArrayRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchGames$5(JSONArray response) {
        this.gameList.clear();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONArray gameArray = response.getJSONArray(i);
                String title = gameArray.getString(0).replace("[", "").replace("]", "").replace("\"", "");
                String imageUrl = "https://borogame.com/templates/borogame/dist/images/thumb/" + title.replace(" ", "_") + ".jpg";
                String gameUrl = gameArray.getString(2).replace("[", "").replace("]", "").replace("\"", "");
                HashMap<String, String> game = new HashMap<>();
                game.put("title", title);
                game.put("imageUrl", imageUrl);
                game.put("gameUrl", gameUrl);
                this.gameList.add(game);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.adapter.notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fetchGames$6(VolleyError error) {
        error.printStackTrace();
        Toast.makeText(this, "Error fetching data", 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    class GameAdapter extends BaseAdapter {
        private Context context;
        private List gameList;

        public GameAdapter(Context context, List list) {
            this.context = context;
            this.gameList = list;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.gameList.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            return this.gameList.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                RelativeLayout relativeLayout = new RelativeLayout(this.context);
                int padding = (int) (this.context.getResources().getDisplayMetrics().density * 20.0f);
                int spacing = (int) (this.context.getResources().getDisplayMetrics().density * 15.0f);
                relativeLayout.setPadding(padding, padding, padding, spacing);
                ImageView imageView = new ImageView(this.context);
                imageView.setId(View.generateViewId());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(-1, 300);
                imageView.setLayoutParams(imageParams);
                GradientDrawable imageBackground = new GradientDrawable();
                imageBackground.setCornerRadius(this.context.getResources().getDisplayMetrics().density * 10.0f);
                imageView.setClipToOutline(true);
                imageView.setOutlineProvider(new ViewOutlineProvider() { // from class: com.limelight.Store.GameAdapter.1
                    @Override // android.view.ViewOutlineProvider
                    public void getOutline(View view2, Outline outline) {
                        outline.setRoundRect(0, 0, view2.getWidth(), view2.getHeight(), GameAdapter.this.context.getResources().getDisplayMetrics().density * 10.0f);
                    }
                });
                imageView.setBackground(imageBackground);
                relativeLayout.addView(imageView);
                TextView titleTextView = new TextView(this.context);
                titleTextView.setId(View.generateViewId());
                titleTextView.setTextSize(18.0f);
                titleTextView.setPadding(10, 10, 10, 0);
                RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(-1, -2);
                titleParams.addRule(3, imageView.getId());
                titleParams.setMargins(0, 10, 0, 0);
                titleTextView.setLayoutParams(titleParams);
                relativeLayout.addView(titleTextView);
                GradientDrawable background = new GradientDrawable();
                background.setColor(352321535);
                background.setCornerRadius(this.context.getResources().getDisplayMetrics().density * 10.0f);
                background.setStroke(1, 1090519039);
                relativeLayout.setBackground(background);
                view = relativeLayout;
                view.setTag(new ViewHolder(titleTextView, imageView));
            }
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            final HashMap<String, String> game = (HashMap) this.gameList.get(position);
            viewHolder.titleTextView.setText(game.get("title"));
            Picasso.get().load(game.get("imageUrl")).fit().centerCrop().into(viewHolder.imageView);
            final String storedLicense = Store.this.sharedPreferences.getString("userLicense", null);
            view.setOnClickListener(new View.OnClickListener() { // from class: com.limelight.Store$GameAdapter$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    this.f$0.lambda$getView$0(game, storedLicense, view2);
                }
            });
            return view;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$getView$0(HashMap game, String storedLicense, View v) {
            String gameTitle = ((String) game.get("title")).replace(" ", "_");
            String ssoUrl = "https://borogame.com/api.php?sso=" + storedLicense + "&path=games.php/" + gameTitle;
            String gameUrl = "https://borogame.com/games.php/" + gameTitle;
            if (storedLicense != null) {
                Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(ssoUrl));
                this.context.startActivity(browserIntent);
            } else {
                Intent browserIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(gameUrl));
                this.context.startActivity(browserIntent2);
            }
        }

        private class ViewHolder {
            ImageView imageView;
            TextView titleTextView;

            ViewHolder(TextView titleTextView, ImageView imageView) {
                this.titleTextView = titleTextView;
                this.imageView = imageView;
            }
        }
    }
}
