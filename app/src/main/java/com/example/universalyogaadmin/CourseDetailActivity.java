package com.example.universalyogaadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu; // THÊM IMPORT
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

public class CourseDetailActivity extends AppCompatActivity implements ClassInstanceAdapter.OnInstanceListener {

    private Toolbar toolbar;
    private TextView tvDetailCourseName, tvDetailSchedule, tvDetailDescription, tvNoInstances;
    private RecyclerView recyclerViewInstances;
    private FloatingActionButton fabAddInstance;

    private DatabaseHelper dbHelper;
    private Course currentCourse;
    private long courseId = -1;

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

        setupRecyclerView();

        fabAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailActivity.this, AddEditInstanceActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (courseId != -1) {
            loadCourseDetailsAndInstances();
        } else {
            Toast.makeText(this, getString(R.string.error_course_not_found), Toast.LENGTH_SHORT).show();
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

    private void loadCourseDetailsAndInstances() {
        currentCourse = dbHelper.getCourse(courseId);
        if (currentCourse != null) {
            getSupportActionBar().setTitle(currentCourse.getCourseName());
            tvDetailCourseName.setText(currentCourse.getCourseName());
            tvDetailSchedule.setText(getString(R.string.course_schedule_format, currentCourse.getDayOfWeek(), currentCourse.getDuration()));
            tvDetailDescription.setText(currentCourse.getCourseDescription());

            instanceList.clear();
            instanceList.addAll(dbHelper.getAllInstancesForCourse(courseId));
            instanceAdapter.notifyDataSetChanged();

            if (instanceList.isEmpty()) {
                recyclerViewInstances.setVisibility(View.GONE);
                tvNoInstances.setVisibility(View.VISIBLE);
            } else {
                recyclerViewInstances.setVisibility(View.VISIBLE);
                tvNoInstances.setVisibility(View.GONE);
            }
        }
    }

    // --- CÁC PHƯƠNG THỨC MỚI ĐỂ XỬ LÝ MENU ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish(); // Xử lý nút Back
            return true;
        } else if (itemId == R.id.action_edit_course) {
            // Mở màn hình AddEditCourseActivity ở chế độ Sửa
            Intent intent = new Intent(CourseDetailActivity.this, AddEditCourseActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Các phương thức của OnInstanceListener ---

    @Override
    public void onEditInstance(ClassInstance instance) {
        Intent intent = new Intent(this, AddEditInstanceActivity.class);
        intent.putExtra("INSTANCE_ID", instance.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteInstance(final ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_instance_message, instance.getDate()))
                .setPositiveButton(getString(R.string.delete_button), (dialog, which) -> {
                    dbHelper.deleteInstance(instance.getId());
                    Toast.makeText(this, getString(R.string.instance_deleted_success), Toast.LENGTH_SHORT).show();
                    loadCourseDetailsAndInstances();
                })
                .setNegativeButton(getString(R.string.cancel_button), null)
                .show();
    }
}