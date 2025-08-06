package com.example.universalyogaadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.InstanceViewHolder> {

    private List<ClassInstance> instanceList;
    private OnInstanceListener listener;

    public interface OnInstanceListener {
        void onEditInstance(ClassInstance instance);
        void onDeleteInstance(ClassInstance instance);
    }

    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceListener listener) {
        this.instanceList = instanceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_instance, parent, false);
        return new InstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceViewHolder holder, int position) {
        ClassInstance instance = instanceList.get(position);
        holder.bind(instance, listener);
    }

    @Override
    public int getItemCount() {
        return instanceList.size();
    }

    static class InstanceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvTeacher, tvComments;
        private ImageButton btnDelete;

        public InstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_instance_date);
            tvTeacher = itemView.findViewById(R.id.tv_instance_teacher);
            tvComments = itemView.findViewById(R.id.tv_instance_comments);
            btnDelete = itemView.findViewById(R.id.btn_delete_instance);
        }

        public void bind(final ClassInstance instance, final OnInstanceListener listener) {
            // Lấy Context từ itemView
            Context context = itemView.getContext();

            tvDate.setText(instance.getDate());

            // Lấy chuỗi định dạng từ resource và gán giá trị
            tvTeacher.setText(context.getString(R.string.teacher_label, instance.getTeacher()));

            if (instance.getComments() != null && !instance.getComments().isEmpty()) {
                tvComments.setText(context.getString(R.string.comments_label, instance.getComments()));
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onEditInstance(instance));
            btnDelete.setOnClickListener(v -> listener.onDeleteInstance(instance));
        }
    }
}