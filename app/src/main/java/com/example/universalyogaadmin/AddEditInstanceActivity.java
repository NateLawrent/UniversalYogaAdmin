package com.example.universalyogaadmin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem; // THÊM IMPORT
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull; // THÊM IMPORT
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // THÊM IMPORT
import com.google.android.material.textfield.TextInputEditText;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditInstanceActivity extends AppCompatActivity {

    private Toolbar toolbar; // THÊM BIẾN TOOLBAR
    private TextView tvSelectedDate;
    private Button btnPickDate, btnSaveInstance;
    private TextInputEditText edtTeacherName, edtComments;

    private DatabaseHelper dbHelper;
    private long courseId = -1;
    private long instanceId = -1;
    private Course parentCourse;
    private ClassInstance currentInstance;

    private Calendar selectedDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_instance);

        dbHelper = new DatabaseHelper(this);
        initViews();

        // THIẾT LẬP TOOLBAR
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Kiểm tra chế độ Sửa hay Thêm mới
        if (getIntent().hasExtra("INSTANCE_ID")) {
            instanceId = getIntent().getLongExtra("INSTANCE_ID", -1);
            loadInstanceData();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.title_edit_instance));
        } else if (getIntent().hasExtra("COURSE_ID")) {
            courseId = getIntent().getLongExtra("COURSE_ID", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.title_add_instance));
        }

        // Tải thông tin khóa học cha
        long parentCourseId = (instanceId != -1 && currentInstance != null) ? currentInstance.getCourseId() : courseId;
        if (parentCourseId != -1) {
            parentCourse = dbHelper.getCourse(parentCourseId);
        }

        if (parentCourse == null) {
            Toast.makeText(this, getString(R.string.error_course_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_edit_instance); // Ánh xạ Toolbar
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSaveInstance = findViewById(R.id.btn_save_instance);
        edtTeacherName = findViewById(R.id.edt_teacher_name);
        edtComments = findViewById(R.id.edt_comments);
    }

    // THÊM PHƯƠNG THỨC NÀY ĐỂ XỬ LÝ NÚT BACK
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng màn hình hiện tại và quay về
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListeners() {
        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveInstance.setOnClickListener(v -> saveInstance());
    }

    private void loadInstanceData() {
        currentInstance = dbHelper.getSingleInstance(instanceId);
        if (currentInstance == null) {
            Toast.makeText(this, "Error: Could not load instance data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        edtTeacherName.setText(currentInstance.getTeacher());
        edtComments.setText(currentInstance.getComments());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            Date date = sdf.parse(currentInstance.getDate());
            if (date != null) {
                selectedDateCalendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        updateDateInView();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateCalendar.set(year, month, dayOfMonth);
                    updateDateInView();
                },
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        tvSelectedDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    private void saveInstance() {
        String date = tvSelectedDate.getText().toString();
        String teacher = edtTeacherName.getText().toString().trim();
        String comments = edtComments.getText().toString().trim();

        if (date.equals(getString(R.string.date_not_selected)) || TextUtils.isEmpty(teacher)) {
            Toast.makeText(this, getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateDayOfWeekCorrect()) {
            String errorMessage = getString(R.string.error_invalid_date, parentCourse.getDayOfWeek());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            return;
        }

        if (instanceId == -1) {
            ClassInstance newInstance = new ClassInstance();
            newInstance.setCourseId(courseId);
            newInstance.setDate(date);
            newInstance.setTeacher(teacher);
            newInstance.setComments(comments);
            dbHelper.addInstance(newInstance);
            Toast.makeText(this, getString(R.string.add_instance_success), Toast.LENGTH_SHORT).show();
        } else {
            currentInstance.setDate(date);
            currentInstance.setTeacher(teacher);
            currentInstance.setComments(comments);
            dbHelper.updateInstance(currentInstance);
            Toast.makeText(this, getString(R.string.update_instance_success), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean isDateDayOfWeekCorrect() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String selectedDayOfWeek = dayFormat.format(selectedDateCalendar.getTime());
        return selectedDayOfWeek.equalsIgnoreCase(parentCourse.getDayOfWeek());
    }
}