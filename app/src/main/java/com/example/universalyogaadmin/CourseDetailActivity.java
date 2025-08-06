package com.example.universalyogaadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Bước 1: Implement interface của Adapter
public class CourseDetailActivity extends AppCompatActivity implements ClassInstanceAdapter.OnInstanceListener {

    private Toolbar toolbar;
    private TextView tvDetailCourseName, tvDetailSchedule, tvDetailDescription, tvNoInstances;
    private RecyclerView recyclerViewInstances;
    private FloatingActionButton fabAddInstance;

    private DatabaseHelper dbHelper;
    private Course currentCourse;
    private long courseId = -1;

    // Bước 2: Khai báo Adapter và danh sách
    private ClassInstanceAdapter instanceAdapter;
    private List<ClassInstance> instanceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        dbHelper = new DatabaseHelper(this);
        courseId = getIntent().getLongExtra("COURSE_ID", -1);

        initViews();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bước 3: Cài đặt RecyclerView
        setupRecyclerView();

        // Xử lý sự kiện cho nút +
        fabAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailActivity.this, AddEditInstanceActivity.class);
            intent.putExtra("COURSE_ID", courseId); // Gửi ID của khóa học qua
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (courseId != -1) {
            loadCourseDetailsAndInstances();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy khóa học.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_course_detail);
        tvDetailCourseName = findViewById(R.id.tv_detail_course_name);
        tvDetailSchedule = findViewById(R.id.tv_detail_schedule);
        tvDetailDescription = findViewById(R.id.tv_detail_description);
        tvNoInstances = findViewById(R.id.tv_no_instances);
        recyclerViewInstances = findViewById(R.id.recycler_view_instances);
        fabAddInstance = findViewById(R.id.fab_add_instance);
    }

    private void setupRecyclerView() {
        instanceAdapter = new ClassInstanceAdapter(instanceList, this);
        recyclerViewInstances.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInstances.setAdapter(instanceAdapter);
    }

    /**
     * Tải cả thông tin khóa học và danh sách các buổi học của nó.
     */
    private void loadCourseDetailsAndInstances() {
        currentCourse = dbHelper.getCourse(courseId);
        if (currentCourse != null) {
            // Hiển thị thông tin khóa học
            getSupportActionBar().setTitle(currentCourse.getCourseName());
            tvDetailCourseName.setText(currentCourse.getCourseName());
            tvDetailSchedule.setText(String.format("%s - %s", currentCourse.getDayOfWeek(), currentCourse.getDuration()));
            tvDetailDescription.setText(currentCourse.getCourseDescription());

            // Tải danh sách các buổi học
            instanceList.clear();
            instanceList.addAll(dbHelper.getAllInstancesForCourse(courseId));
            instanceAdapter.notifyDataSetChanged();

            // Hiển thị danh sách hoặc thông báo "chưa có"
            if (instanceList.isEmpty()) {
                recyclerViewInstances.setVisibility(View.GONE);
                tvNoInstances.setVisibility(View.VISIBLE);
            } else {
                recyclerViewInstances.setVisibility(View.VISIBLE);
                tvNoInstances.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Bước 4: Implement các phương thức của OnInstanceListener ---

    @Override
    public void onEditInstance(ClassInstance instance) {
        // Mở màn hình AddEditInstanceActivity ở chế độ "Sửa"
        Intent intent = new Intent(this, AddEditInstanceActivity.class);

        // Gửi cả ID của Buổi học và ID của Khóa học cha
        intent.putExtra("INSTANCE_ID", instance.getId());
        intent.putExtra("COURSE_ID", instance.getCourseId()); // Gửi thêm để kiểm tra ngày

        startActivity(intent);
    }

    @Override
    public void onDeleteInstance(final ClassInstance instance) {
        new AlertDialog.Builder(this)
                // Lấy chuỗi từ file resources
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_instance_message, instance.getDate()))
                .setPositiveButton(getString(R.string.delete_button), (dialog, which) -> {
                    dbHelper.deleteInstance(instance.getId());
                    Toast.makeText(this, getString(R.string.instance_deleted_success), Toast.LENGTH_SHORT).show();
                    loadCourseDetailsAndInstances(); // Tải lại danh sách để cập nhật UI
                })
                .setNegativeButton(getString(R.string.cancel_button), null)
                .show();
    }
}