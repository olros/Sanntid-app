package com.olros.bussapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemClickListener {

    public ArrayList<Model> holdeplasser;
    private boolean mItemClicked;
    private Context mContext;
    private static final int list_item = 1;
    private static final int card_item = 2;
    private static final int horizontal_item = 3;
    private static final int top_item = 4;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private LatLng mLatestLocation;

    public ModelAdapter(Context context,ArrayList<Model> arrayList) {
        mContext = context;
        holdeplasser = arrayList;
    }

    // determine which layout to use for the row
    @Override
    public int getItemViewType(int position) {
        Model item = holdeplasser.get(position);
        if (item.getType() == Model.ItemType.list_item) {
            return list_item;
        } else if (item.getType() == Model.ItemType.card_item) {
            return card_item;
        } else if (item.getType() == Model.ItemType.horizontal_item) {
            return horizontal_item;
        } else if (item.getType() == Model.ItemType.top_item) {
            return top_item;
        } else {
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == list_item) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            return new ViewHolderList(view, this);
        } else if (viewType == card_item) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_row, parent, false);
            return new ViewHolderCard(view, this);
        } else if (viewType == horizontal_item) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_row, null);
            return new ViewHolderHorizontal(view);
        } else if (viewType == top_item) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_row, null);
            return new ViewHolderTop(view);
        } else {
            throw new RuntimeException("The type has to be ONE or TWO or THREE or FOUR");
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case list_item:
                initLayoutList((ViewHolderList)holder, position);
                break;

            case card_item:
                initLayoutCard((ViewHolderCard)holder, position);
                break;

            case horizontal_item:
                initLayoutHorizontalCard((ViewHolderHorizontal)holder, position);
                break;

            case top_item:
                initLayoutTopCard((ViewHolderTop)holder, position);
                break;
        }
    }

    private void initLayoutList(ViewHolderList holder, int position) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng mLatestLocation = new LatLng(lat, lng);
                String distance = Utils.formatDistanceBetween(mLatestLocation, holdeplasser.get(position).getLocation());
                if (TextUtils.isEmpty(distance)) {
                    //holder.descriptionView.setVisibility(View.GONE);
                    holder.descriptionView.setText("");
                } else {
                    holder.descriptionView.setVisibility(View.VISIBLE);
                    holder.descriptionView.setText(distance);
                }
            } else {
                //holder.descriptionView.setVisibility(View.GONE);
                holder.descriptionView.setText("");
            }
        } else {
            //holder.descriptionView.setVisibility(View.GONE);
            holder.descriptionView.setText("");
        }
        holder.nameView.setText(holdeplasser.get(position).getName());
    }

    private void initLayoutCard(final ViewHolderCard holder,final int pos) {
        holder.nameView.setText(holdeplasser.get(pos).getName());
        holder.descriptionView.setText(R.string.favourite_stopplace);

        /*holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_favourite(pos);
            }
        });*/
    }

    private void initLayoutHorizontalCard(final ViewHolderHorizontal holder,final int pos) {
        ArrayList<Horizontal> horizontalItems = holdeplasser.get(pos).getHorizontal();

        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(horizontalItems, mContext);

        holder.recycler_view2.setHasFixedSize(true);
        holder.recycler_view2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.recycler_view2.setAdapter(horizontalAdapter);
    }

    private void initLayoutTopCard(final ViewHolderTop holder,final int pos) {
        holder.headerView.setText(R.string.app_name);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(mContext, android.R.layout.simple_dropdown_item_1line);
        holder.autoCompleteTextView.setThreshold(2);
        holder.autoCompleteTextView.setAdapter(autoSuggestAdapter);
        holder.autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        holder.autoCompleteTextView.setText(autoSuggestAdapter.getObject(position).getName() + ", " + autoSuggestAdapter.getObject(position).getLocality());
                        Intent intent = new Intent(mContext, BusActivity.class);
                        intent.putExtra("navn", autoSuggestAdapter.getObject(position).getName());
                        intent.putExtra("holdeplass", autoSuggestAdapter.getObject(position).getId());
                        mContext.startActivity(intent);
                    }
                });

        holder.autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(holder.autoCompleteTextView.getText())) {
                        makeApiCall(holder.autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });


        holder.btn_top_avganger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext, "Avganger ble klikket på", Toast.LENGTH_SHORT).show();
                if(mContext instanceof MainActivity){
                    ((MainActivity)mContext).loadData();
                }
                holder.infoRowText.setText(R.string.closest_stops);
                holder.btn_top_avganger.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_button_selected_background));
                holder.btn_top_favoritter.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_button_not_selected_background));
            }
        });

        holder.btn_top_favoritter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext, "Favoritter ble klikket på", Toast.LENGTH_SHORT).show();
                if(mContext instanceof MainActivity){
                    ((MainActivity)mContext).loadFavourites();
                }
                holder.infoRowText.setText(R.string.favourites);
                holder.btn_top_avganger.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_button_not_selected_background));
                holder.btn_top_favoritter.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_button_selected_background));
            }
        });

        holder.btn_top_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationDrawerFragmentMain bottomNavigationDrawerFragmentMain = new BottomNavigationDrawerFragmentMain();
                bottomNavigationDrawerFragmentMain.show(((FragmentActivity)mContext).getSupportFragmentManager(), bottomNavigationDrawerFragmentMain.getTag());
            }
        });

        CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) {

            public void onTick(long millisUntilFinished) {
                Calendar c = Calendar.getInstance();
                //holder.clockView.setText(c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND));
                holder.clockView.setText(String.format("%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
            }
            public void onFinish() {

            }
        };
        newtimer.start();
    }

    private void makeApiCall(String text) {
        ApiCall.make(mContext, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<SearchModel> searchList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("features");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        JSONObject properties = row.getJSONObject("properties");
                        searchList.add(new SearchModel(properties.getString("name"), properties.getString("locality"), properties.getString("id").substring(14), properties.getString("distance")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(searchList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public static class ViewHolderCard extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public TextView nameView, descriptionView;
        public ImageView overflow;
        com.olros.bussapp.ItemClickListener mItemClickListener;

        public ViewHolderCard(View view, ItemClickListener itemClickListener) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.txt_Name);
            descriptionView = (TextView) view.findViewById(R.id.txt_Description);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            mItemClickListener = itemClickListener;
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public static class ViewHolderHorizontal extends RecyclerView.ViewHolder {
        public RecyclerView recycler_view2;

        public ViewHolderHorizontal(View view) {
            super(view);
            this.recycler_view2 = (RecyclerView) view.findViewById(R.id.recycler_view2);
        }
    }

    public static class ViewHolderTop extends RecyclerView.ViewHolder {
        public TextView headerView, clockView, infoRowText;
        public AppCompatAutoCompleteTextView autoCompleteTextView;
        public AppCompatButton btn_top_avganger, btn_top_favoritter;
        public View btn_top_more;

        public ViewHolderTop(View view) {
            super(view);
            this.headerView = (TextView) view.findViewById(R.id.txt_Header);
            this.clockView = (TextView) view.findViewById(R.id.txt_Clock);
            this.infoRowText = (TextView) view.findViewById(R.id.infoRowText);
            this.autoCompleteTextView = (AppCompatAutoCompleteTextView) view.findViewById(R.id.txt_Edit);
            this.btn_top_avganger = (AppCompatButton) view.findViewById(R.id.btn_top_avganger);
            this.btn_top_favoritter = (AppCompatButton) view.findViewById(R.id.btn_top_favoritter);
            this.btn_top_more = (View) view.findViewById(R.id.btn_top_more);
        }
    }

    @Override
    public int getItemCount() {
        return holdeplasser.size();
    }

    @Override
    public void onItemClick(View view, int position) {
        mItemClicked = true;
        String navn = holdeplasser.get(position).getName();
        String holdeplass = holdeplasser.get(position).getHoldeplass();
        Intent intent = new Intent(mContext, BusActivity.class);
        intent.putExtra("navn", navn);
        intent.putExtra("holdeplass", holdeplass);
        mContext.startActivity(intent);
    }

    public static class ViewHolderList extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView nameView;
        TextView descriptionView;
        com.olros.bussapp.ItemClickListener mItemClickListener;

        public ViewHolderList(View view, ItemClickListener itemClickListener) {
            super(view);

            nameView = (TextView) view.findViewById(R.id.txt_Name);
            descriptionView = (TextView) view.findViewById(R.id.txt_Description);
            mItemClickListener = itemClickListener;
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setFilter(ArrayList<Model> arrayList) {
        holdeplasser = new ArrayList<>();
        holdeplasser.addAll(arrayList);
        notifyDataSetChanged();
    }

    /**
     * Delete favourite bus stop
     */
    public void delete_favourite(final int position) {
        final int holdeplass_position = position;
        AlertDialog.Builder alertbox = new AlertDialog.Builder(mContext);
        alertbox.setMessage(R.string.sure_you_want_to_remove + holdeplasser.get(position).getName() + R.string.from_your_favourites);
        alertbox.setTitle(R.string.delete_favourite);
        alertbox.setIcon(R.drawable.ic_delete);

        alertbox.setPositiveButton(R.string.yes,
            new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int position) {
                    SharedPreferences pref = mContext.getSharedPreferences("Favourites", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    String slett_holdeplass = holdeplasser.get(holdeplass_position).getHoldeplass();
                    editor.remove(slett_holdeplass);
                    editor.apply();
                    Intent intent = new Intent(mContext, MainActivity.class);
                    ((Activity)mContext).finish();
                    mContext.startActivity(intent);
                    Toast.makeText(mContext, R.string.favourite_was_deleted, Toast.LENGTH_SHORT).show();
                }
            });
        alertbox.setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
        });
        alertbox.create();
        alertbox.show();
    }
}

interface ItemClickListener {
    void onItemClick(View view, int position);
}