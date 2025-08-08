package com.example.universalyogaadmin;

// ----- ĐÂY LÀ PHẦN IMPORT BỊ THIẾU -----
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// -----------------------------------------

public class MainActivity extends AppCompatActivity implements CourseAdapter.OnCourseListener {

    private Toolbar toolbar;
    private RecyclerView recyclerViewCourses;
    private FloatingActionButton fabAddCourse;
    private SearchView searchView;
    private DatabaseHelper dbHelper;
    private CourseAdapter courseAdapter;
    private List<Course> courseList = new ArrayList<>();
    private FirebaseFirestore db_cloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        db_cloud = FirebaseFirestore.getInstance();

        recyclerViewCourses = findViewById(R.id.recycler_view_courses);
        fabAddCourse = findViewById(R.id.fab_add_course);
        searchView = findViewById(R.id.search_view);

        setupRecyclerView();
        setupSearch();

        fabAddCourse.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditCourseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.setQuery("", false);
        searchView.clearFocus();
        filterCoursesByTeacher("");
    }

    private void setupRecyclerView() {
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this, courseList, this);
        recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCoursesByTeacher(newText);
                return true;
            }
        });
    }

    private void filterCoursesByTeacher(String query) {
        List<Course> filteredList;
        if (query.isEmpty()) {
            filteredList = dbHelper.getAllCourses();
        } else {
            filteredList = dbHelper.searchCoursesByTeacher(query);
        }
        courseAdapter.filterList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (itemId == R.id.action_upload) {
            uploadDataToFirestore();
            return true;
        } else if (itemId == R.id.action_reset) { // THÊM KHỐI LỆNH NÀY
            showResetConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Confirmation")
                .setMessage("All local data (on this device) will be permanently deleted. Cloud data will not be affected. Are you sure?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    dbHelper.resetDatabase();
                    // Tải lại danh sách (bây giờ sẽ là rỗng)
                    filterCoursesByTeacher("");
                    Toast.makeText(this, "Đã reset database.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Lấy tiêu đề từ resource
        builder.setTitle(getString(R.string.filter_dialog_title));

        // Dùng mảng resource mới thay vì mảng hardcode
        builder.setItems(R.array.filter_options, (dialog, item) -> {
            // Lấy lại mảng để so sánh lựa chọn của người dùng
            String[] options = getResources().getStringArray(R.array.filter_options);
            String selectedOption = options[item];

            if (selectedOption.equals(getString(R.string.filter_by_day_of_week))) {
                showDayOfWeekFilterDialog();
            } else if (selectedOption.equals(getString(R.string.filter_by_date))) {
                showDatePickerDialogForFilter();
            } else if (selectedOption.equals(getString(R.string.filter_reset))) {
                searchView.setQuery("", false);
                filterCoursesByTeacher("");
            } else if (selectedOption.equals(getString(R.string.filter_cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showDayOfWeekFilterDialog() {
        final CharSequence[] days = getResources().getTextArray(R.array.days_of_week);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Day of Week");
        builder.setItems(days, (dialog, which) -> {
            String selectedDay = days[which].toString();
            List<Course> filteredList = dbHelper.searchCoursesByDay(selectedDay);
            courseAdapter.filterList(filteredList);
            Toast.makeText(this, "Filtering by: " + selectedDay, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showDatePickerDialogForFilter() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    String formattedDate = sdf.format(selectedDate.getTime());
                    List<Course> filteredList = dbHelper.searchCoursesByDate(formattedDate);
                    courseAdapter.filterList(filteredList);
                    Toast.makeText(this, "Đang lọc theo ngày: " + formattedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void uploadDataToFirestore() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No network connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Starting sync...", Toast.LENGTH_SHORT).show();

        // --- BƯỚC 1: UPLOAD NHỮNG THAY ĐỔI (THÊM MỚI / SỬA) ---
        List<Course> unsyncedCourses = dbHelper.getUnsyncedCourses();
        List<ClassInstance> unsyncedInstances = dbHelper.getUnsyncedInstances();

        Log.d("SYNC", "Found " + unsyncedCourses.size() + " unsynced courses.");
        Log.d("SYNC", "Found " + unsyncedInstances.size() + " unsynced instances.");

        // Upload các course chưa đồng bộ
        for (Course course : unsyncedCourses) {
            Map<String, Object> courseMap = new HashMap<>();
            // ... (code tạo map giữ nguyên)
            courseMap.put("name", course.getCourseName());
            courseMap.put("dayOfWeek", course.getDayOfWeek());
            courseMap.put("capacity", course.getCapacity());
            courseMap.put("courseSet", course.getCourseSet());
            courseMap.put("duration", course.getDuration());
            courseMap.put("price", course.getPrice());
            courseMap.put("type", course.getType());
            courseMap.put("description", course.getCourseDescription());

            db_cloud.collection("courses").document(String.valueOf(course.getId()))
                    .set(courseMap)
                    .addOnSuccessListener(aVoid -> {
                        // Sau khi thành công, cập nhật trạng thái trong SQLite
                        dbHelper.updateCourseSyncStatus(course.getId(), DatabaseHelper.STATUS_SYNCED);
                        Log.d("SYNC_SUCCESS", "Course " + course.getCourseName() + " synced.");
                    })
                    .addOnFailureListener(e -> Log.e("SYNC_ERROR", "Error syncing course " + course.getCourseName(), e));
        }

        // Upload các instance chưa đồng bộ
        for (ClassInstance instance : unsyncedInstances) {
            Map<String, Object> instanceMap = new HashMap<>();
            // ... (code tạo map giữ nguyên)
            instanceMap.put("date", instance.getDate());
            instanceMap.put("teacher", instance.getTeacher());
            instanceMap.put("comments", instance.getComments());
            instanceMap.put("courseId", instance.getCourseId());

            db_cloud.collection("instances").document(String.valueOf(instance.getId()))
                    .set(instanceMap)
                    .addOnSuccessListener(aVoid -> {
                        // Sau khi thành công, cập nhật trạng thái trong SQLite
                        dbHelper.updateInstanceSyncStatus(instance.getId(), DatabaseHelper.STATUS_SYNCED);
                        Log.d("SYNC_SUCCESS", "Instance on " + instance.getDate() + " synced.");
                    })
                    .addOnFailureListener(e -> Log.e("SYNC_ERROR", "Error syncing instance on " + instance.getDate(), e));
        }

        // --- BƯỚC 2: ĐỒNG BỘ VIỆC XÓA (SO SÁNH DỮ LIỆU GIỮA LOCAL VÀ CLOUD) ---
        // Phần này phức tạp hơn và có thể được thêm vào sau nếu cần.
        // Logic cơ bản: Lấy danh sách ID từ cloud, lấy danh sách ID từ local.
        // ID nào có trên cloud mà không có ở local thì xóa nó trên cloud.

        Toast.makeText(this, "Sync Complete!", Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onEditClick(int position) {
        Course selectedCourse = courseAdapter.getCourseAt(position);
        if (selectedCourse != null) {
            Intent intent = new Intent(MainActivity.this, CourseDetailActivity.class);
            intent.putExtra("COURSE_ID", selectedCourse.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        Course selectedCourse = courseAdapter.getCourseAt(position);
        if (selectedCourse != null) {
            dbHelper.deleteCourse(selectedCourse);
            Toast.makeText(this, "Đã xóa khóa học: " + selectedCourse.getCourseName(), Toast.LENGTH_SHORT).show();
            filterCoursesByTeacher(searchView.getQuery().toString());
        }
    }
}