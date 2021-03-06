package com.asa.findmyvolunteer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.List;


public class VolunteerFragment extends Fragment {
    BackendlessCollection<VictimData> victims;
    BackendlessDataQuery dataQuery = new BackendlessDataQuery();
    QueryOptions queryOptions = new QueryOptions();
    List<VictimData> totalVictims=new ArrayList<>();
    RecyclerView recyclerView;
    CardView cardView;
    RecyclerAdapter mAdapter;
    SwipeRefreshLayout swipeContainer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer, container, false);
        recyclerView=(RecyclerView) view.findViewById(R.id.recycler_view);
        swipeContainer=(SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter=new RecyclerAdapter(totalVictims);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                VictimData victimData = totalVictims.get(position);
                if(victimData.getSos().equals("true")) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SOSMap.class);
                    intent.putExtra("sos", victimData);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(getActivity().getApplicationContext(), VolunteerMap.class);
                    intent.putExtra("victims", victimData);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        getData();


        return view;
    }
    private void getData(){
        queryOptions.addRelated("location");
        dataQuery.setQueryOptions(queryOptions);
        Backendless.Persistence.of(VictimData.class).find(dataQuery,new AsyncCallback<BackendlessCollection<VictimData>>() {
            @Override
            public void handleResponse(BackendlessCollection<VictimData> victimDataBackendlessCollection) {
                Log.i("Data ","Received !");
                    totalVictims.clear();
                    totalVictims.addAll(victimDataBackendlessCollection.getCurrentPage());
                    mAdapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.i("Data ","Failed !");
            }
        });
    }
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private VolunteerFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final VolunteerFragment.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
    public void clear() {
        totalVictims.clear();
        mAdapter.notifyDataSetChanged();
    }
}

