package com.example.signuplogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Social extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Upload> uploadList;
    DatabaseReference databaseReference, likeref;
    private ProgressBar progressBar;
    Boolean testclick = false;
    TextView post;

    SharedPreferences mSharedPref;
    String username;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_review);

        mSharedPref = getSharedPreferences("SharedPref", MODE_PRIVATE);
        username = mSharedPref.getString("userName", "null");


        recyclerView = findViewById(R.id.reView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.recycleProgressId);
        post=findViewById(R.id.post);


        uploadList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("post");
        likeref =  FirebaseDatabase.getInstance().getReference("likes");



        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Social.this, Post.class);
                startActivity(intent);
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot datasnap : snapshot.getChildren()){
                    Upload upload = datasnap.getValue(Upload.class);
                    uploadList.add(upload);
                }

                Toast.makeText(Social.this, uploadList.size()+"", Toast.LENGTH_SHORT).show();

                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(Social.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });



        FirebaseRecyclerOptions<Upload> options =
                new FirebaseRecyclerOptions.Builder<Upload>()
                        .setQuery(databaseReference, Upload.class)
                        .build();


        FirebaseRecyclerAdapter<Upload, MyViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Upload, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Upload model) {
                Upload upload = uploadList.get(position);
                holder.textView.setText(upload.getImageName());
                holder.userTextView.setText(upload.getUsername());
                Picasso.get().load(upload.getImageUri())
                        .fit()
                        .centerCrop()
                        .into(holder.imageView);
//
//                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                String userId = firebaseUser.getUid();
                String postkey = getRef(position).getKey();
//
                holder.getLikeStatus(postkey,username);
//
//
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testclick = true;

                        likeref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(testclick == true){
                                    if(snapshot.child(postkey).hasChild(username)){
                                        likeref.child(postkey).child(username).removeValue();

                                        testclick = false;
                                    }
                                    else{
                                        likeref.child(postkey).child(username).setValue(true);

                                        testclick = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
//
                holder.commentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),CommentPanel.class);
                        intent.putExtra("postkey",postkey);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample,parent,false);
                return new MyViewHolder(view);

            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

        //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_main,menu);
//        return super.onCreateOptionsMenu(menu);
//    }
    }




}
