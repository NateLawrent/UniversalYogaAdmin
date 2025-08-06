package com.example.universalyogaadmin;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private Context context;
    private OnCourseListener onCourseListener; // Interface để xử lý sự kiện click

    // Interface để gửi sự kiện về cho Activity
    public interface OnCourseListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public CourseAdapter(Context context, List<Course> courseList, OnCourseListener onCourseListener) {
        this.context = context;
        this.courseList = courseList;
        this.onCourseListener = onCourseListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        Context context = holder.itemView.getContext(); // Lấy context từ view

        holder.tvCourseName.setText(course.getCourseName());

        // Lấy định dạng từ resources
        String scheduleFormat = context.getString(R.string.course_schedule_format);
        String priceFormat = context.getString(R.string.price_label_format);

        // Tạo chuỗi đã được dịch
        String schedule = String.format(scheduleFormat, course.getDayOfWeek(), course.getDuration());
        String price = String.format(Locale.US, priceFormat, course.getPrice());

        holder.tvCourseSchedule.setText(schedule);
        holder.tvCoursePrice.setText(price);

        // Bắt sự kiện click vào cả item để SỬA
        holder.itemView.setOnClickListener(v -> {
            if (onCourseListener != null) {
                onCourseListener.onEditClick(holder.getAdapterPosition());
            }
        });

        // Bắt sự kiện long click (nhấn giữ) để XÓA, với các chuỗi tiếng Anh
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirm_delete_title))
                    // Sử dụng string resource với placeholder (%s) để chèn tên khóa học
                    .setMessage(context.getString(R.string.confirm_delete_course_message, course.getCourseName()))
                    .setPositiveButton(context.getString(R.string.delete_button), (dialog, which) -> {
                        if (onCourseListener != null) {
                            onCourseListener.onDeleteClick(holder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton(context.getString(R.string.cancel_button), null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    // ViewHolder class
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCourseName, tvCourseSchedule, tvCoursePrice;

        public CourseViewHolder(View view) {
            super(view);
            // Ánh xạ các TextView từ file list_item_course.xml
            tvCourseName = view.findViewById(R.id.tv_course_name);
            tvCourseSchedule = view.findViewById(R.id.tv_course_schedule);
            tvCoursePrice = view.findViewById(R.id.tv_course_price);
        }
    }
    /**
     * Cập nhật danh sách của Adapter với một danh sách mới đã được lọc.
     * @param filteredList Danh sách khóa học đã được lọc.
     */
    public void filterList(List<Course> filteredList) {
        // Xóa sạch danh sách hiện tại của adapter
        this.courseList.clear();
        // Thêm tất cả các item từ danh sách mới vào
        this.courseList.addAll(filteredList);
        // Báo cho UI vẽ lại
        notifyDataSetChanged();
    }
    /**
     * Lấy khóa học tại một vị trí cụ thể.
     * @param position vị trí trong danh sách
     * @return đối tượng Course
     */
    public Course getCourseAt(int position) {
        if (position >= 0 && position < courseList.size()) {
            return courseList.get(position);
        }
        return null;
    }
}