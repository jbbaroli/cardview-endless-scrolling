package com.jeanoliveira.demo3;

import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private List<Games> lstGames;
    private EndlessRecyclerViewScrollListener scrollListener;
    private RecyclerViewAdapter recyclerViewAdapter;
    private OkHttpHandler okHttpHandler;
    private RecyclerView recyclerView;
    private Parcelable recyclerViewState;

    GridLayoutManager gridLayoutManager;

    EditText etGameName;

    Button btnSearch;

    int numberOfPages = 1;
    int currentPage;
    int newItems;

    String baseAddress = "https://www.giantbomb.com/api/search/?api_key=";
    String apiKey = "f0458f75619cf8fcf55830a21a02d7f64dce981d";
    String format = "&format=json";
    String resource = "&resources=game";
    String page = "&page=";
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstGames = new ArrayList<>();

        recyclerView = findViewById(R.id.layRecyclerView);

        btnSearch = findViewById(R.id.btnSearch);
        etGameName = findViewById(R.id.etGameName);

        gridLayoutManager = new GridLayoutManager(this, 2);

        btnSearch.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = 1;
                String query = "&query=" + etGameName.getText().toString().trim();
                url = baseAddress + apiKey + format + query + resource + page;

                okHttpHandler = new OkHttpHandler();
                okHttpHandler.execute(url + String.valueOf(currentPage));

                if (lstGames.size() > 0) {
                    lstGames.clear();
                    recyclerViewAdapter.notifyDataSetChanged();
                    scrollListener.resetState();
                }
            }
        });
    }

    public class OkHttpHandler extends AsyncTask {
        OkHttpClient client = new OkHttpClient();
//        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object[] params) {
            Request.Builder builder = new Request.Builder();
            builder.url(params[0].toString());
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (recyclerViewAdapter == null) {
                recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, lstGames);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            recyclerView.setAdapter(recyclerViewAdapter);

            numberOfPages = parseResponse(o.toString());

            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if(numberOfPages > currentPage){
                        currentPage ++;
                        loadNextPage(currentPage);
                    }
                }
            };

            recyclerView.addOnScrollListener(scrollListener);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    public void loadNextPage(int page) {
        okHttpHandler = new OkHttpHandler();
        okHttpHandler.execute(url + String.valueOf(page));
        recyclerViewAdapter.notifyItemRangeInserted(lstGames.size(), newItems);
    }

    private int parseResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);
            if (json.getString("error").equals("OK")) {
                newItems = Integer.valueOf(json.getString("number_of_page_results"));
                numberOfPages = Integer.valueOf(json.getString("number_of_total_results"));
                numberOfPages = (int)Math.ceil(numberOfPages / Integer.valueOf(json.getString("limit")));
                JSONArray parentArray = json.getJSONArray("results");

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject image = parentArray.getJSONObject(i).getJSONObject("image");
                    lstGames.add(new Games(parentArray.getJSONObject(i).getString("name"), image.getString("small_url")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return numberOfPages;
    }
}
