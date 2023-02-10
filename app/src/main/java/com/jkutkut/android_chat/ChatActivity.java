package com.jkutkut.android_chat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jkutkut.android_chat.model.Msg;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    public static final int RESULT_EXIT = 0;
    public static final int RESULT_LOGOUT = 1;

    public static final String USER_KEY = "user";

    private MsgAdapter adapter;

    private ArrayList<Msg> msgs;
    private String user;
    private Uri photoUri;
    private ActivityResultLauncher<Intent> photoSelectorActivity;

    private DatabaseReference msgDBRef;
    private StorageReference photoStorageRef;
    private ChildEventListener msgChildListener;

    private EditText etxtMsg;
    private ImageView imgvPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        photoUri = null;
        msgs = new ArrayList<>();
        user = getIntent().getStringExtra(USER_KEY);

        etxtMsg = findViewById(R.id.etxtMsg);
        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> sendMsg());
        ImageButton btnPhoto = findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(v -> getPhoto());
        imgvPreview = findViewById(R.id.imgvPreview);
        imgvPreview.setVisibility(ImageButton.INVISIBLE);

        RecyclerView rvMsgs = findViewById(R.id.rvMsgs);
        adapter = new MsgAdapter(msgs);
        rvMsgs.setLayoutManager(new LinearLayoutManager(this));
        rvMsgs.setAdapter(adapter);
        rvMsgs.setItemAnimator(new DefaultItemAnimator());

        photoSelectorActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    Uri uri = result.getData().getData();
                    photoUri = uri;
                    imgvPreview.setVisibility(ImageButton.VISIBLE);
                    Glide.with(this).load(uri).into(imgvPreview);
                }
            }
        );

        // Firebase
        msgDBRef = FirebaseDatabase
            .getInstance()
            .getReference()
            .child("msgs");

        photoStorageRef = FirebaseStorage
            .getInstance()
            .getReference()
            .child("imgs");

        addMsgListener();
    }

    private void sendMsg() {
        String msg = etxtMsg.getText().toString().trim();
        if (msg.isEmpty() && photoUri == null)
            return;
        Date d = new Date();
        String id = d + "_" + user;
        if (photoUri != null) {
            StorageReference photoRef = photoStorageRef.child(id);
            UploadTask ut = photoRef.putFile(photoUri);
            ut.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
               if (!task.isSuccessful())
                   return;
               Uri downloadUri = task.getResult();
               msgDBRef.child(id)
                    .setValue(new Msg(id, msg, user, downloadUri.toString()));
               photoUri = null;
            });
        }
        else {
            msgDBRef.child(id)
                    .setValue(new Msg(id, msg, user, null));
        }
        etxtMsg.setText("");
        imgvPreview.setVisibility(ImageButton.INVISIBLE);
    }

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
//        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        photoSelectorActivity.launch(intent);
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
                assert msg != null;
                msgs.add(msg);
                adapter.notifyItemInserted(msgs.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Msg m = dataSnapshot.getValue(Msg.class);
                assert m != null;
                m.setSender("System");
                m.setMsg("Msg removed"); // TODO - Export to XML
                m.setPhotoUrl(null);
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
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        msgDBRef.addChildEventListener(msgChildListener);
    }

    // *************** Options menu ***************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.mnExit) {
            this.confirmExit();
            return true;
        }
        else {
            this.confirmLogout();
            return super.onOptionsItemSelected(item);
        }
    }

    private void confirmExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getText(R.string.exit_confirmation_msg));
        builder.setPositiveButton(
                R.string.exit_confirmation_positive,
                (dialog, which) -> {
                    setResult(RESULT_EXIT);
                    finish();
                }
        );
        builder.setNegativeButton(
                R.string.exit_confirmation_negative,
                (dialog, which) -> dialog.cancel()
        );
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(true);
        ad.show();
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getText(R.string.logout_confirmation_msg));
        builder.setPositiveButton(
                R.string.exit_confirmation_positive,
                (dialog, which) -> {
                    setResult(RESULT_LOGOUT);
                    finish();
                }
        );
        builder.setNegativeButton(
                R.string.exit_confirmation_negative,
                (dialog, which) -> dialog.cancel()
        );
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(true);
        ad.show();
    }
}