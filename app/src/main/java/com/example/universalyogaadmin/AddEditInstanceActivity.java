package com.example.universalyogaadmin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditInstanceActivity extends AppCompatActivity {

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

        if (getIntent().hasExtra("INSTANCE_ID")) {
            // --- CHẾ ĐỘ SỬA ---
            instanceId = getIntent().getLongExtra("INSTANCE_ID", -1);
            setTitle("Sửa Buổi Học");
            // Chỉ gọi phương thức ở đây
            loadInstanceData();
        } else if (getIntent().hasExtra("COURSE_ID")) {
            // --- CHẾ ĐỘ THÊM MỚI ---
            courseId = getIntent().getLongExtra("COURSE_ID", -1);
            setTitle("Thêm Buổi Học Mới");
        }

        // Tải thông tin khóa học cha
        long parentCourseId = (instanceId != -1) ? currentInstance.getCourseId() : courseId;
        parentCourse = dbHelper.getCourse(parentCourseId);

        if (parentCourse == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy khóa học gốc.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    private void initViews() {
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSaveInstance = findViewById(R.id.btn_save_instance);
        edtTeacherName = findViewById(R.id.edt_teacher_name);
        edtComments = findViewById(R.id.edt_comments);
    }

    // --- ĐỊNH NGHĨA PHƯƠNG THỨC Ở ĐÂY, BÊN NGOÀI onCreate ---
    private void loadInstanceData() {
        currentInstance = dbHelper.getSingleInstance(instanceId);

        if (currentInstance == null) {
            Toast.makeText(this, "Lỗi: Không thể tải dữ liệu buổi học.", Toast.LENGTH_SHORT).show();
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

    private void setupListeners() {
        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveInstance.setOnClickListener(v -> saveInstance());
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

        if (date.equals("Chưa chọn ngày") || TextUtils.isEmpty(teacher)) {
            Toast.makeText(this, "Vui lòng chọn ngày và nhập tên giáo viên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateDayOfWeekCorrect()) {
            // Lấy chuỗi định dạng từ resource và truyền ngày vào
            String errorMessage = getString(R.string.error_invalid_date, parentCourse.getDayOfWeek());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            return;
        }

        if (instanceId == -1) {
            // Lưu mới
            ClassInstance newInstance = new ClassInstance();
            newInstance.setCourseId(courseId);
            newInstance.setDate(date);
            newInstance.setTeacher(teacher);
            newInstance.setComments(comments);
            dbHelper.addInstance(newInstance);
            Toast.makeText(this, "Đã thêm buổi học thành công!", Toast.LENGTH_SHORT).show();
        } else {
            // Cập nhật
            currentInstance.setDate(date);
            currentInstance.setTeacher(teacher);
            currentInstance.setComments(comments);
            dbHelper.updateInstance(currentInstance);
            Toast.makeText(this, "Đã cập nhật buổi học!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean isDateDayOfWeekCorrect() {
        // Sử dụng Locale.ENGLISH để đảm bảo tên ngày là tiếng Anh (Monday, Tuesday...)
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String selectedDayOfWeek = dayFormat.format(selectedDateCalendar.getTime());

        // Bây giờ, cả hai chuỗi sẽ cùng là tiếng Anh và có thể so sánh chính xác
        return selectedDayOfWeek.equalsIgnoreCase(parentCourse.getDayOfWeek());

    }
}