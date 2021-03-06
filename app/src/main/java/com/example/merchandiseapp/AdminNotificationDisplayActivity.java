package com.example.merchandiseapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminNotificationDisplayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<String> groupNameList;
    private DatabaseReference notificationRef;
    private DatabaseReference uidReference;
    private TextView notificationEmpty;
    private int countCards;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_display);
        groupNameList = new ArrayList<String>();

        recyclerView = findViewById(R.id.notificationList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        notificationEmpty = findViewById(R.id.notiInfo);
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Admin");
        final Query queries = notificationRef.orderByChild("isApproved").equalTo("No");


        queries.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                countCards = (int) dataSnapshot.getChildrenCount();


                if(dataSnapshot.exists())
                {

                    DataExists(queries);
                }
                else
                {
                    NoDataExists();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void NoDataExists()
    {
        notificationEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void DataExists(Query queries)
    {

        FirebaseRecyclerOptions<Notifications> options = new FirebaseRecyclerOptions.Builder<Notifications>()
                .setQuery(queries, Notifications.class)
                .build();

        FirebaseRecyclerAdapter<Notifications, AdminNotificationViewHolder> adapter
                = new FirebaseRecyclerAdapter<Notifications, AdminNotificationViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull AdminNotificationViewHolder holder, int position, @NonNull final Notifications model)
            {

                holder.txtGroupName.setText( "GroupName : " + model.getGroupName());
                holder.txtEmail.setText("Email ID : " + model.getEmailID() );
                holder.txtContact.setText("Contact Details : " + model.getContact());
                groupNameList.add(model.getGroupName());
                holder.removeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        notificationRef.child(model.getGroupName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    System.out.println(Integer.toString(countCards));
                                    countCards--;
                                    Toast.makeText(AdminNotificationDisplayActivity.this, "Request Rejected Successfully", Toast.LENGTH_SHORT).show();
                                    if(countCards == 0)
                                    {
                                        Intent intent = new Intent(AdminNotificationDisplayActivity.this, AdminNotificationDisplayActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });


                    }


                });

                holder.approveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        notificationRef.child(model.getGroupName()).child("isApproved").setValue("Yes");
                        uidReference = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUID());
                        uidReference.child("notiList").setValue("1");
                        uidReference.child("Groups").child(model.getGroupName()).setValue("true") ;
                        uidReference.child("Authorized_Groups").child(model.getGroupName()).setValue("true") ;


                        Toast.makeText(AdminNotificationDisplayActivity.this, "Request Approved Successfully", Toast.LENGTH_SHORT).show();
                        /* Adding new Group Now */
                        DatabaseReference newNode = FirebaseDatabase.getInstance().getReference().child("Group").child(model.getGroupName()) ;
                        HashMap<String,String> a = new HashMap<>() ;
                        a.put(model.getUID(), model.getEmailID()) ;

                        newNode.child("Details").child("Contact").setValue(model.getContact()) ;
                        newNode.child("Details").child("EmailID").setValue(model.getEmailID()) ;
                        newNode.child("Details").child("GroupName").setValue(model.getGroupName()) ;
                        newNode.child("Details").child("UPI").setValue(model.getUPI()) ;
                        newNode.child("Details").child("Image_Location").setValue(model.getImage_Location()) ;

                        final DatabaseReference GroupAdd = FirebaseDatabase.getInstance().getReference().child("Group").child(model.getGroupName()).child("Details");

                        final HashMap<String,Object> productIDMap = new HashMap<>();
                        productIDMap.put("Contact", model.getContact());
                        productIDMap.put("EmailID", model.getEmailID());
                        productIDMap.put("GroupName", model.getGroupName());
                        productIDMap.put("UPI", model.getUPI());

                        GroupAdd.updateChildren(productIDMap).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {

                            }
                        });

                        final DatabaseReference GroupAdd2 = FirebaseDatabase.getInstance().getReference().child("Group").child(model.getGroupName()).child("Authorized_Members").child("EmailID");
                        final HashMap<String,Object> productIDMap2 = new HashMap<>();
                        productIDMap2.put(  Integer.toString(model.getEmailID().hashCode()), model.getEmailID());

                        GroupAdd2.updateChildren(productIDMap2).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {

                            }
                        });

                        final DatabaseReference GroupAdd3 = FirebaseDatabase.getInstance().getReference().child("Group").child(model.getGroupName()).child("Members").child("EmailID");

                        final HashMap<String,Object> productIDMap3 = new HashMap<>();
                        productIDMap3.put(  Integer.toString(model.getEmailID().hashCode()), model.getEmailID());

                        GroupAdd3.updateChildren(productIDMap3).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {

                            }
                        });


                    }
                });

            }

            @NonNull
            @Override
            public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notificationlayout, viewGroup, false);

                AdminNotificationViewHolder holder = new AdminNotificationViewHolder(view);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }



}
