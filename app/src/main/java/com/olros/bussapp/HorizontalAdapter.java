package com.olros.bussapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>{
    private ArrayList<Horizontal> horizontalList;
    Context context;

    public HorizontalAdapter(ArrayList<Horizontal> horizontalList, Context context){
        this.horizontalList= horizontalList;
        this.context = context;
    }

    @Override
    public HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate the layout file
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_fav_card, null);
        HorizontalViewHolder mh = new HorizontalViewHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(HorizontalViewHolder holder, final int position) {
        holder.horizontalImageView.setImageResource(horizontalList.get(position).getHoldeplass_icon());
        holder.horizontalTextView.setText(horizontalList.get(position).getName());
        holder.horizontalCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class activityClass = MainActivity.class;
                try {
                    activityClass = Class.forName(horizontalList.get(position).getIntent_activity());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, activityClass);
                intent.putExtra("navn", horizontalList.get(position).getName());
                intent.putExtra("holdeplass", horizontalList.get(position).getHoldeplass());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return horizontalList.size();
    }

    public class HorizontalViewHolder extends RecyclerView.ViewHolder {
        ImageView horizontalImageView;
        TextView horizontalTextView;
        MaterialCardView horizontalCardView;
        LinearLayoutCompat horizontalLayoutView;
        public HorizontalViewHolder(View view) {
            super(view);
            horizontalImageView=view.findViewById(R.id.horizontalImageView);
            horizontalTextView=view.findViewById(R.id.horizontalTextView);
            horizontalCardView=view.findViewById(R.id.horizontalCardView);
            horizontalLayoutView=view.findViewById(R.id.horizontalLayoutView);
        }
    }
}