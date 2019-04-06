package com.sccodesoft.dago;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sccodesoft.dago.Model.TripHostory;
import com.sccodesoft.dago.R;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView tripHistory;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    String currentuserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tripHistory = (RecyclerView)findViewById(R.id.trip_history_list_recycler);
        tripHistory.setLayoutManager(new LinearLayoutManager(this));

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();

        FirebaseRecyclerOptions<TripHostory> options =
                new FirebaseRecyclerOptions.Builder<TripHostory>()
                        .setQuery(rootRef.child("DriverTripHistory").child(currentuserid),TripHostory.class)
                        .build();

        FirebaseRecyclerAdapter<TripHostory,HistoryViewHolder> adapter =
                new FirebaseRecyclerAdapter<TripHostory, HistoryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull HistoryViewHolder holder, int position, @NonNull TripHostory model) {

                        holder.txtDate.setText(model.getDate());
                        holder.txtFare.setText(model.getFare());
                        holder.txtDuration.setText(model.getDuration());
                        holder.txtDistance.setText(model.getDistance());
                        holder.txtFrom.setText(model.getFrom());
                        holder.txtTo.setText(model.getTo());

                    }

                    @NonNull
                    @Override
                    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trip_history,parent,false);
                        HistoryViewHolder historyViewHolder = new HistoryViewHolder(view);
                        return historyViewHolder;
                    }
                };
        tripHistory.setAdapter(adapter);
        adapter.startListening();

    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder
    {
        public TextView txtDate,txtFrom,txtTo,txtDistance,txtDuration,txtFare;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            txtDate = (TextView)itemView.findViewById(R.id.txtDate);
            txtFrom = (TextView)itemView.findViewById(R.id.txtFrom);
            txtTo = (TextView)itemView.findViewById(R.id.txtTo);
            txtFare = (TextView)itemView.findViewById(R.id.txtFare);
            txtDistance = (TextView)itemView.findViewById(R.id.txtDistance);
            txtDuration = (TextView)itemView.findViewById(R.id.txtDuration);

        }
    }
}
