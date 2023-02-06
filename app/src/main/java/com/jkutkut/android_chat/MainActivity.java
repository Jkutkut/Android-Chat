package com.jkutkut.android_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jkutkut.android_chat.model.Msg;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MsgAdapter adapter;

    private ArrayList<Msg> msgs;

    private DatabaseReference msgDBRef;
    private ChildEventListener msgChildListener;

    private EditText etxtMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etxtMsg = findViewById(R.id.etxtMsg);
        Button btnSend = findViewById(R.id.btnSend);

        msgs = new ArrayList<>();

        RecyclerView rvMsgs = findViewById(R.id.rvMsgs);
        adapter = new MsgAdapter(msgs);
        rvMsgs.setLayoutManager(new LinearLayoutManager(this));
        rvMsgs.setAdapter(adapter);
        rvMsgs.setItemAnimator(new DefaultItemAnimator());

        msgDBRef = FirebaseDatabase
            .getInstance()
            .getReference()
            .child("msgs");

        addMsgListener();

        btnSend.setOnClickListener(v -> sendMsg());
    }

    private void sendMsg() {
        final String user = "user"; // TODO
        String msg = etxtMsg.getText().toString().trim();
        System.out.println("msg = " + msg);
        if (!msg.isEmpty()) {
            Date fecha = new Date();
            String id = fecha + "_" + user;
            msgDBRef.child(id)
                    .setValue(new Msg(id, msg, user));
            etxtMsg.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (msgDBRef != null) {
            msgDBRef.removeEventListener(msgChildListener);
            msgChildListener = null;
        }
        adapter.clear();
    }

    public void addMsgListener() {
        if (msgDBRef == null)
            return;
        msgChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                System.out.println(dataSnapshot.getValue());
                System.out.println(s);
                Msg msg = dataSnapshot.getValue(Msg.class);
                msgs.add(msg);
                adapter.notifyItemInserted(msgs.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Msg m = dataSnapshot.getValue(Msg.class);
                assert m != null;
                m.setMsg("El mensaje ha sido eliminado");
                int pos = -1;
                for (int i = 0; i < msgs.size(); i++) {
                    if (m.getId().equals(msgs.get(i).getId())) {
                        msgs.set(i, m);
                        pos = i;
                        break;
                    }
                }
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        msgDBRef.addChildEventListener(msgChildListener);
    }

}