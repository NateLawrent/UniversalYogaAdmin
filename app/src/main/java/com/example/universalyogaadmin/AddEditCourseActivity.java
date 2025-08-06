package com.example.universalyogaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Arrays;

public class AddEditCourseActivity extends AppCompatActivity {

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
        setupSpinners();

        currentCourseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (currentCourseId != -1) {
            setTitle("Sửa Khóa học");
            loadCourseData();
        } else {
            setTitle("Thêm Khóa học Mới");
            currentCourse = new Course();
        }

        btnSaveCourse.setOnClickListener(v -> saveCourse());
    }

    private void initViews() {
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
        // Lấy dữ liệu từ các trường UI
        String name = edtCourseName.getText().toString().trim();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String typeOfClass = spinnerTypeOfClass.getSelectedItem().toString();
        String courseSetStr = edtCourseSet.getText().toString().trim();
        String capacityStr = edtCapacity.getText().toString().trim();
        String duration = edtDuration.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        // Kiểm tra các trường bắt buộc
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(courseSetStr) || TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(duration) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gán dữ liệu vào đối tượng Course
        currentCourse.setCourseName(name);
        currentCourse.setDayOfWeek(dayOfWeek);
        currentCourse.setType(typeOfClass);
        currentCourse.setCourseSet(Integer.parseInt(courseSetStr));
        currentCourse.setCapacity(Integer.parseInt(capacityStr));
        currentCourse.setDuration(duration);
        currentCourse.setPrice(Double.parseDouble(priceStr));
        currentCourse.setCourseDescription(description);
        // Không còn imageUrl

        // Lưu vào database
        if (currentCourseId == -1) {
            dbHelper.addCourse(currentCourse);
            Toast.makeText(this, "Lưu khóa học thành công!", Toast.LENGTH_LONG).show();
        } else {
            dbHelper.updateCourse(currentCourse);
            Toast.makeText(this, "Cập nhật khóa học thành công!", Toast.LENGTH_LONG).show();
        }
        finish();
    }
}