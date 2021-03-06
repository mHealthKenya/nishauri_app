package com.mhealth.nishauri.Fragments.Lab;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.fxn.stash.Stash;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mhealth.nishauri.Models.Dependant;
import com.mhealth.nishauri.Models.User;
import com.mhealth.nishauri.Models.ViralLoad;
import com.mhealth.nishauri.R;
import com.mhealth.nishauri.adapters.DependantAdapter;
import com.mhealth.nishauri.adapters.ViralLoadAdapter;
import com.mhealth.nishauri.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.RECEIVER_VISIBLE_TO_INSTANT_APPS;
import static com.mhealth.nishauri.utils.AppController.TAG;


public class ViralLoadResultsFragment extends Fragment {


    private Unbinder unbinder;
    private View root;
    private Context context;

    private User loggedInUser;
    private ViralLoadAdapter mAdapter;
    private ArrayList<ViralLoad> viralLoadArrayList;

    private ProgressDialog pDialog;

    @BindView(R.id.shimmer_my_container)
    ShimmerFrameLayout shimmer_my_container;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.request_lyt)
    LinearLayout request_lyt;

    @BindView(R.id.no_result_lyt)
    LinearLayout no_result_lyt;

    @BindView(R.id.error_lyt)
    LinearLayout error_lyt;

    @BindView(R.id.fab_request_viral)
    ExtendedFloatingActionButton fab_request_viral_results;

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        this.context = ctx;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_viral_load_results, container, false);
        unbinder = ButterKnife.bind(this, root);

        pDialog = new ProgressDialog(context);
        pDialog.setTitle("Loading...");
        pDialog.setMessage("Getting Results...");
        pDialog.setCancelable(true);

        loggedInUser = (User) Stash.getObject(Constants.AUTH_TOKEN, User.class);


        viralLoadArrayList = new ArrayList<>();
        mAdapter = new ViralLoadAdapter(context, viralLoadArrayList);


        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new ViralLoadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ViralLoad clickedItem = viralLoadArrayList.get(position);

            }
        });

        fab_request_viral_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request_lyt.setVisibility(View.GONE);
                pDialog.show();
                loadViralLoad();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onResume() {
        super.onResume();
        shimmer_my_container.startShimmerAnimation();
    }

    @Override
    public void onPause() {
        shimmer_my_container.stopShimmerAnimation();
        super.onPause();
    }

    private void loadViralLoad() {

        String auth_token = loggedInUser.getAuth_token();


        AndroidNetworking.get(Constants.ENDPOINT+Constants.VIRAL_LOAD)
                .addHeaders("Authorization","Token "+ auth_token)
                .addHeaders("Content-Type", "application.json")
                .addHeaders("Accept", "*/*")
                .addHeaders("Accept", "gzip, deflate, br")
                .addHeaders("Connection","keep-alive")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.e(TAG, response.toString());

                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.hide();
                            pDialog.cancel();
                        }



                        viralLoadArrayList.clear();

                        if (recyclerView!=null)
                            recyclerView.setVisibility(View.VISIBLE);

                        if (shimmer_my_container!=null){
                            shimmer_my_container.stopShimmerAnimation();
                            shimmer_my_container.setVisibility(View.GONE);
                        }

                        try {

                            String  message = response.has("message") ? response.getString("message") : "" ;


                            if (message.contains("No results for the given CCC Number were found")){
                                no_result_lyt.setVisibility(View.VISIBLE);
                                Snackbar.make(root.findViewById(R.id.frag_viral_load),message, Snackbar.LENGTH_LONG).show();

                            }
                            JSONArray myArray = response.getJSONArray("data");


                            if (myArray.length() > 0){


                                for (int i = 0; i < myArray.length(); i++) {

                                    JSONObject item = (JSONObject) myArray.get(i);


                                    int  id = item.has("id") ? item.getInt("id") : 0;
                                    String r_id = item.has("r_id") ? item.getString("r_id") : "";
                                    String result_type = item.has("result_type") ? item.getString("result_type") : "";
                                    String result_content = item.has("result_content") ? item.getString("result_content") : "";
                                    String date_collected = item.has("date_collected") ? item.getString("date_collected") : "";
                                    String lab_name = item.has("lab_name") ? item.getString("lab_name") : "";
                                    int  user = item.has("user") ? item.getInt("user") : 0;


                                    ViralLoad newResult = new ViralLoad(id,r_id,result_type,result_content,date_collected,lab_name,user);

                                    viralLoadArrayList.add(newResult);
                                    mAdapter.notifyDataSetChanged();



                                }

                            }else if (response.getJSONObject("data").has("message")){
                                //not data found

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.hide();
                                    pDialog.cancel();
                                }

                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();


                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.hide();
                                pDialog.cancel();
                            }
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error

                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.hide();
                            pDialog.cancel();
                        }


                        Log.e(TAG, String.valueOf(error.getErrorCode()));
                        if (error.getErrorCode() == 0){

                            no_result_lyt.setVisibility(View.VISIBLE);
                        }
                        else{

                            error_lyt.setVisibility(View.VISIBLE);
                            Snackbar.make(root.findViewById(R.id.frag_viral_load), "Error: " + error.getErrorBody(), Snackbar.LENGTH_LONG).show();

                        }


                    }
                });
    }
}
