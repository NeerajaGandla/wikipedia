package com.neeraja.wikipedia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neeraja.wikipedia.data.Page;
import com.neeraja.wikipedia.data.Query;
import com.neeraja.wikipedia.data.SearchResult;
import com.neeraja.wikipedia.data.WikiSearchResultData;
import com.neeraja.wikipedia.datacache.DataSource;
import com.neeraja.wikipedia.utils.ApiUtils;
import com.neeraja.wikipedia.utils.Constants;
import com.neeraja.wikipedia.utils.CustomException;
import com.neeraja.wikipedia.utils.Globals;
import com.neeraja.wikipedia.utils.HttpRequest;
import com.neeraja.wikipedia.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.atv_search)
    AutoCompleteTextView searchTv;
    @BindView(R.id.rv_search_results)
    RecyclerView searchResultsRv;
    @BindView(R.id.iv_search)
    ImageView searchIv;
    private Context mContext;
    private int count;
    private ProgressBar progressBar;
    private List<SearchResult> searchResults = new ArrayList<>();
    private ResultAdapter resultAdapter;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        dataSource = new DataSource(mContext);
        ButterKnife.bind(this);
        searchResultsRv.setLayoutManager(new LinearLayoutManager(mContext));
        searchResultsRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        resultAdapter = new ResultAdapter((ArrayList<SearchResult>) searchResults);
        searchResultsRv.setAdapter(resultAdapter);

        searchTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Utils.isValidArrayList(matchingResults)) {
                    SearchResult selectedResult = (SearchResult) parent.getItemAtPosition(position);//customersDataModels.get((int) id);
                    if (selectedResult != null) {
                        Intent intent = new Intent(mContext, DescriptionActivity.class);
//                        try {
                        String name = selectedResult.getName();
                        if (Utils.isValidString(name))
                            name = name.replace(" ", "_");
                        Log.d("", "onItemClick: " + name);
                        intent.putExtra("url", Constants.wikiUrl + Uri.encode(name));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
                        startActivity(intent);
                    }
//                    new GetResultsAsync(txt.toString()).execute();
                }
            }
        });

    }

    Handler mHandler = new Handler();

    @OnTextChanged(value = R.id.atv_search, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterSearchChanged(CharSequence text) {
        if (Utils.isValidString(text.toString())) {
            mHandler.removeCallbacksAndMessages(null);
            final String txt = text.toString();
            Runnable userStoppedTyping = new Runnable() {

                @Override
                public void run() {
                    // user didn't typed for 2 seconds, do whatever you want
                    if (Utils.isValidString(txt.toString())) {
                        if (Utils.getConnectivityStatus(mContext)) {
                            new GetResultsAsync(txt.toString()).execute();
                        } else {
                            showAlert("Check Your Internet Connection");
                        }
                    }
                }
            };

            mHandler.postDelayed(userStoppedTyping, 2000);
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.show();
    }

    ArrayList<SearchResult> matchingResults = null;
    ArrayAdapter<SearchResult> adapter = null;

    @OnTextChanged(value = R.id.atv_search, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onSearchChanged(CharSequence text) {
        if (Utils.isValidString(text.toString())) {
            matchingResults = dataSource.searchResultDb.getMatchingCachedResults(text.toString());
            if (Utils.isValidArrayList(matchingResults)) {
                adapter = new ArrayAdapter<SearchResult>(mContext, android.R.layout.simple_dropdown_item_1line, matchingResults);
                searchTv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                searchIv.setImageDrawable(getDrawable(R.drawable.ic_clear));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                searchIv.setImageDrawable(getDrawable(R.drawable.ic_action_name));
            }
        }
    }

    @OnClick(R.id.iv_search)
    public void onSearchOrClear(View view){
        String name = searchTv.getText().toString().trim();
        if (Utils.isValidString(name)) {
           searchTv.setText(null);
        } else {

        }
    }

    public class GetResultsAsync extends AsyncTask {
        private String query;

        public GetResultsAsync(String query) {
            this.query = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressBar = Utils.getProgressBar(mContext);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Globals.lastErrMsg = "";
                searchResults.clear();
                query = query.replace(" ","_");
                WikiSearchResultData response = (WikiSearchResultData) HttpRequest.getInputStreamFromUrl(ApiUtils.getSearchUrl(query), WikiSearchResultData.class, mContext);
                if (response != null) {
                    Query query = response.getQuery();
                    if (query != null) {
                        List<Page> pages = query.getPages();
                        for (Page page : pages) {
                            SearchResult searchResult = new SearchResult();
                            searchResult.setName(page.getTitle());
                            if (page.getThumbnail() != null)
                                searchResult.setImageUrl(page.getThumbnail().getSource());
                            if (page.getTerms() != null && page.getTerms().getDescription() != null)
                                searchResult.setDescription(page.getTerms().getDescription().get(0));
                            searchResults.add(searchResult);
                        }
                    }
                } else {
                    Globals.lastErrMsg = Constants.DATA_UNAVAILABLE;
                }
            } catch (CustomException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
//            if (progressBar != null)
//                Utils.dismissProgressBar();
            if (Utils.isValidArrayList((ArrayList<?>) searchResults)) {
                resultAdapter.updateData((ArrayList<SearchResult>) searchResults);
                resultAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(o);
        }
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private ArrayList<SearchResult> list;

        public class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_name)
            TextView nameTv;
            @BindView(R.id.tv_description)
            TextView descriptionTv;
            @BindView(R.id.iv_search_image)
            ImageView searchImgIv;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick
            void onClick(View view) {
                if (Utils.isValidArrayList(list)) {
                    Intent intent = new Intent(mContext, DescriptionActivity.class);
                    SearchResult result = list.get(getAdapterPosition());
//                    try {
                    String name = result.getName();
                    if (Utils.isValidString(name))
                        name = name.replace(" ", "_");
                    Log.d("", "onItemClick: " + name);
                    intent.putExtra("url", Constants.wikiUrl + Uri.encode(name));
//                    }
//                    catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    dataSource.searchResultDb.saveSearchResult(result);
                    startActivity(intent);
                }
            }

        }

        public ResultAdapter(ArrayList<SearchResult> myDataset) {
            list = myDataset;
        }

        private void updateData(ArrayList<SearchResult> dataList) {
            list = dataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_result, parent, false);
            ViewHolder ViewHolder = new ViewHolder(view);
            return ViewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SearchResult data = list.get(position);
            Picasso.with(mContext)
                    .load(data.getImageUrl())
                    .into(holder.searchImgIv);
            holder.nameTv.setText(data.getName());
            holder.descriptionTv.setText(data.getDescription());
        }

        @Override
        public int getItemCount() {
            count = list.size();
            return list.size();
        }

    }

}
