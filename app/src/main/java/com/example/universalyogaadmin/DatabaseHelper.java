package com.example.universalyogaadmin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // --- CÁC HẰNG SỐ ---
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "YogaCourseManager.db";

    public static final int STATUS_SYNCED = 0;
    public static final int STATUS_UNSYNCED = 1;

    // Bảng COURSES
    private static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSE_ID = "course_id";
    public static final String COLUMN_NAME = "course_name";
    public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_COURSE_SET = "course_set";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_SYNC_STATUS = "sync_status";

    // Bảng CLASS INSTANCES
    private static final String TABLE_INSTANCES = "class_instances";
    public static final String COLUMN_INSTANCE_ID = "instance_id";
    public static final String COLUMN_INSTANCE_DATE = "instance_date";
    public static final String COLUMN_TEACHER = "teacher";
    public static final String COLUMN_COMMENTS = "comments";
    public static final String COLUMN_FOREIGN_COURSE_ID = "course_id";

    // --- CONSTRUCTOR, ONCREATE, ONUPGRADE ---
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
                + COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DAY_OF_WEEK + " TEXT,"
                + COLUMN_CAPACITY + " INTEGER,"
                + COLUMN_COURSE_SET + " INTEGER,"
                + COLUMN_DURATION + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_SYNC_STATUS + " INTEGER DEFAULT " + STATUS_UNSYNCED + ")";
        db.execSQL(CREATE_COURSES_TABLE);

        String CREATE_INSTANCES_TABLE = "CREATE TABLE " + TABLE_INSTANCES + "("
                + COLUMN_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_INSTANCE_DATE + " TEXT,"
                + COLUMN_TEACHER + " TEXT,"
                + COLUMN_COMMENTS + " TEXT,"
                + COLUMN_FOREIGN_COURSE_ID + " INTEGER,"
                + COLUMN_SYNC_STATUS + " INTEGER DEFAULT " + STATUS_UNSYNCED + ","
                + "FOREIGN KEY(" + COLUMN_FOREIGN_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_INSTANCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Luôn xóa và tạo lại để đảm bảo sự đơn giản
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    // --- CÁC PHƯƠNG THỨC CHO COURSE ---

    public void addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getCourseName());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_COURSE_SET, course.getCourseSet());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE, course.getType());
        values.put(COLUMN_DESCRIPTION, course.getCourseDescription());
        values.put(COLUMN_SYNC_STATUS, STATUS_UNSYNCED);
        db.insert(TABLE_COURSES, null, values);
        db.close();
    }

    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getCourseName());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_COURSE_SET, course.getCourseSet());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE, course.getType());
        values.put(COLUMN_DESCRIPTION, course.getCourseDescription());
        values.put(COLUMN_SYNC_STATUS, STATUS_UNSYNCED);
        int rowsAffected = db.update(TABLE_COURSES, values, COLUMN_COURSE_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
        return rowsAffected;
    }

    private Course cursorToCourse(Cursor cursor) {
        Course course = new Course();
        course.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)));
        course.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        course.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)));
        course.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)));
        course.setCourseSet(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_SET)));
        course.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)));
        course.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
        course.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
        course.setCourseDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        return course;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);
        if (cursor.moveToFirst()) {
            do {
                courseList.add(cursorToCourse(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    public void deleteCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COURSES, COLUMN_COURSE_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
    }

    public Course getCourse(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSES, null, COLUMN_COURSE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Course course = null;
        if (cursor != null && cursor.moveToFirst()) {
            course = cursorToCourse(cursor);
            cursor.close();
        }
        db.close();
        return course;
    }

    // --- CÁC PHƯƠNG THỨC CHO CLASS INSTANCE ---

    public void addInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOREIGN_COURSE_ID, instance.getCourseId());
        values.put(COLUMN_INSTANCE_DATE, instance.getDate());
        values.put(COLUMN_TEACHER, instance.getTeacher());
        values.put(COLUMN_COMMENTS, instance.getComments());
        values.put(COLUMN_SYNC_STATUS, STATUS_UNSYNCED);
        db.insert(TABLE_INSTANCES, null, values);
        db.close();
    }

    public int updateInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INSTANCE_DATE, instance.getDate());
        values.put(COLUMN_TEACHER, instance.getTeacher());
        values.put(COLUMN_COMMENTS, instance.getComments());
        values.put(COLUMN_SYNC_STATUS, STATUS_UNSYNCED);
        int rowsAffected = db.update(TABLE_INSTANCES, values, COLUMN_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
        db.close();
        return rowsAffected;
    }

    public List<ClassInstance> getAllInstancesForCourse(long courseId) {
        List<ClassInstance> instanceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INSTANCES, null,
                COLUMN_FOREIGN_COURSE_ID + " = ?",
                new String[]{String.valueOf(courseId)},
                null, null, COLUMN_INSTANCE_DATE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_ID)));
                instance.setCourseId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOREIGN_COURSE_ID)));
                instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_DATE)));
                instance.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
                instance.setComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
                instanceList.add(instance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instanceList;
    }

    public void deleteInstance(long instanceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INSTANCES, COLUMN_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instanceId)});
        db.close();
    }

    public ClassInstance getSingleInstance(long instanceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INSTANCES, null, COLUMN_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instanceId)}, null, null, null);
        ClassInstance instance = null;
        if (cursor != null && cursor.moveToFirst()) {
            instance = new ClassInstance();
            instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_ID)));
            instance.setCourseId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOREIGN_COURSE_ID)));
            instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_DATE)));
            instance.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
            instance.setComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
            cursor.close();
        }
        db.close();
        return instance;
    }

    // --- CÁC PHƯƠNG THỨC TÌM KIẾM, RESET VÀ ĐỒNG BỘ ---

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION + 1);
    }

    public List<Course> searchCoursesByTeacher(String teacherName) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "SELECT DISTINCT c.* FROM " + TABLE_COURSES + " c" +
                " JOIN " + TABLE_INSTANCES + " i ON c." + COLUMN_COURSE_ID + " = i." + COLUMN_FOREIGN_COURSE_ID +
                " WHERE i." + COLUMN_TEACHER + " LIKE ?";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + teacherName + "%"});
        if (cursor.moveToFirst()) {
            do {
                courseList.add(cursorToCourse(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    public List<Course> searchCoursesByDay(String dayOfWeek) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSES, null, COLUMN_DAY_OF_WEEK + " = ?",
                new String[]{dayOfWeek}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                courseList.add(cursorToCourse(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    public List<Course> searchCoursesByDate(String date) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "SELECT DISTINCT c.* FROM " + TABLE_COURSES + " c" +
                " JOIN " + TABLE_INSTANCES + " i ON c." + COLUMN_COURSE_ID + " = i." + COLUMN_FOREIGN_COURSE_ID +
                " WHERE i." + COLUMN_INSTANCE_DATE + " = ?";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{date});
        if (cursor.moveToFirst()) {
            do {
                courseList.add(cursorToCourse(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    public List<Course> getUnsyncedCourses() {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSES, null, COLUMN_SYNC_STATUS + " = ?",
                new String[]{String.valueOf(STATUS_UNSYNCED)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                courseList.add(cursorToCourse(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    public List<ClassInstance> getUnsyncedInstances() {
        List<ClassInstance> instanceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INSTANCES, null, COLUMN_SYNC_STATUS + " = ?",
                new String[]{String.valueOf(STATUS_UNSYNCED)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_ID)));
                instance.setCourseId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOREIGN_COURSE_ID)));
                instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTANCE_DATE)));
                instance.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
                instance.setComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
                instanceList.add(instance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instanceList;
    }

    public void updateCourseSyncStatus(long courseId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, status);
        db.update(TABLE_COURSES, values, COLUMN_COURSE_ID + " = ?", new String[]{String.valueOf(courseId)});
        db.close();
    }

    public void updateInstanceSyncStatus(long instanceId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, status);
        db.update(TABLE_INSTANCES, values, COLUMN_INSTANCE_ID + " = ?", new String[]{String.valueOf(instanceId)});
        db.close();
    }
}