package com.example.universalyogaadmin;

public class Course {

    private long id;
    private String courseName;
    private String dayOfWeek;
    private int capacity;
    private int courseSet;
    private String duration;
    private double price;
    private String type;
    private String courseDescription;
    private String imageUrl; // <-- TRƯỜNG MỚI ĐƯỢC THÊM VÀO

    // Constructors
    public Course() {
    }

    // --- GETTERS AND SETTERS ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCourseSet() {
        return courseSet;
    }

    public void setCourseSet(int courseSet) {
        this.courseSet = courseSet;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    // --- GETTER/SETTER MỚI CHO IMAGEURL ---
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}