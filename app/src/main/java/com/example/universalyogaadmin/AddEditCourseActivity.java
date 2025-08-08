package com.example.universalyogaadmin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditCourseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerDayOfWeek, spinnerTypeOfClass;
    private TextInputEditText edtCourseName, edtCourseSet, edtCapacity, edtDuration, edtPrice, edtDescription;
    private Button btnSaveCourse;

    private DatabaseHelper dbHelper;
    private Course currentCourse;
    private long currentCourseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_course);

        dbHelper = new DatabaseHelper(this);
        initViews();

        // THIẾT LẬP TOOLBAR
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupSpinners();

        currentCourseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (currentCourseId != -1) {
            // Chế độ Sửa
            loadCourseData();
            // Đổi tiêu đề trên Toolbar
            if(getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.title_edit_course));
        } else {
            // Chế độ Thêm mới
            currentCourse = new Course();
            // Đổi tiêu đề trên Toolbar
            if(getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.title_add_course));
        }

        btnSaveCourse.setOnClickListener(v -> saveCourse());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_edit);
        edtCourseName = findViewById(R.id.edt_course_name);
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        spinnerTypeOfClass = findViewById(R.id.spinner_type_of_class);
        edtCourseSet = findViewById(R.id.edt_course_set);
        edtCapacity = findViewById(R.id.edt_capacity);
        edtDuration = findViewById(R.id.edt_duration);
        edtPrice = findViewById(R.id.edt_price);
        edtDescription = findViewById(R.id.edt_description);
        btnSaveCourse = findViewById(R.id.btn_save_course);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng màn hình hiện tại và quay về
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeOfClass.setAdapter(typeAdapter);
    }

    private void loadCourseData() {
        currentCourse = dbHelper.getCourse(currentCourseId);
        if (currentCourse != null) {
            edtCourseName.setText(currentCourse.getCourseName());
            edtCourseSet.setText(String.valueOf(currentCourse.getCourseSet()));
            edtCapacity.setText(String.valueOf(currentCourse.getCapacity()));
            edtDuration.setText(currentCourse.getDuration());
            edtPrice.setText(String.valueOf(currentCourse.getPrice()));
            edtDescription.setText(currentCourse.getCourseDescription());

            // Thiết lập giá trị cho Spinner
            String[] days = getResources().getStringArray(R.array.days_of_week);
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(currentCourse.getDayOfWeek())) {
                    spinnerDayOfWeek.setSelection(i);
                    break;
                }
            }

            String[] types = getResources().getStringArray(R.array.class_types);
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(currentCourse.getType())) {
                    spinnerTypeOfClass.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveCourse() {
        String name = edtCourseName.getText().toString().trim();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String typeOfClass = spinnerTypeOfClass.getSelectedItem().toString();
        String courseSetStr = edtCourseSet.getText().toString().trim();
        String capacityStr = edtCapacity.getText().toString().trim();
        String duration = edtDuration.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(courseSetStr) || TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(duration) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        currentCourse.setCourseName(name);
        currentCourse.setDayOfWeek(dayOfWeek);
        currentCourse.setType(typeOfClass);
        currentCourse.setCourseSet(Integer.parseInt(courseSetStr));
        currentCourse.setCapacity(Integer.parseInt(capacityStr));
        currentCourse.setDuration(duration);
        currentCourse.setPrice(Double.parseDouble(priceStr));
        currentCourse.setCourseDescription(description);

        if (currentCourseId == -1) {
            dbHelper.addCourse(currentCourse);
            Toast.makeText(this, getString(R.string.save_course_success), Toast.LENGTH_LONG).show();
        } else {
            dbHelper.updateCourse(currentCourse);
            Toast.makeText(this, getString(R.string.update_course_success), Toast.LENGTH_LONG).show();
        }
        finish();
    }
}