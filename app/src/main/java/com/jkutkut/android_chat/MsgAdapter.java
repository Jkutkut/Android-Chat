package com.jkutkut.android_chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jkutkut.android_chat.model.Msg;

import java.util.ArrayList;

public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Msg> msgs;

    public MsgAdapter(ArrayList<Msg> msgs) {
        this.msgs = msgs;
    }

    public void clear() {
        msgs.clear();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg, parent, false);

        return new MsgViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MsgViewHolder) holder).bindMsg(msgs.get(position));
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

    private static class MsgViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtMsg;
        private final TextView txtSender;
        private final ImageView imgvMsg;

        public MsgViewHolder(@NonNull View parent) {
            super(parent);
            txtMsg = parent.findViewById(R.id.txtMsg);
            txtSender = parent.findViewById(R.id.txtSender);
            imgvMsg = parent.findViewById(R.id.imgvMsg);
        }

        public void bindMsg(Msg msg) {
            txtMsg.setText(msg.getMsg());
            txtSender.setText(msg.getSender());
            if (msg.getPhotoUrl() != null) {
                imgvMsg.setVisibility(View.VISIBLE);
                Glide.with(imgvMsg.getContext())
                    .load(msg.getPhotoUrl())
                    .into(imgvMsg);
            } else {
                imgvMsg.setVisibility(View.GONE);
            }
        }
    }
}
