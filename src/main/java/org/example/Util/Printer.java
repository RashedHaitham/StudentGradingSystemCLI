package org.example.Util;

import org.example.Model.CourseGrade;
import org.example.Model.StudentGrades;
import java.util.List;
import java.util.Map;

public class Printer {
    private Printer(){}

    public static String displayTableContent(List<String> columns, List<String[]> tableContent) {
        int columnWidth = 15;
        StringBuilder tableBuilder = new StringBuilder();
        for (String colName : columns) {
            tableBuilder.append(String.format("| %-" + (columnWidth - 2) + "s ", colName));
        }
        tableBuilder.append("|\n");
        drawHorizontalBorder(columns.size(), columnWidth, tableBuilder);
        for (String[] row : tableContent) {
            for (String value : row) {
                tableBuilder.append(String.format("| %-" + (columnWidth - 2) + "s ", value));
            }
            tableBuilder.append("|\n");
            drawHorizontalBorder(columns.size(), columnWidth, tableBuilder);
        }
        return tableBuilder.toString();
    }

    private static void drawHorizontalBorder(int columnCount, int columnWidth, StringBuilder tableBuilder) {
        for (int i = 0; i < columnCount; i++) {
            tableBuilder.append("+").append("-".repeat(columnWidth));
        }
        tableBuilder.append("+\n");
    }

    public static String displayStudentGradesInfo(int studentId, String studentName, List<CourseGrade> courseGrades) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Student Name: ").append(studentName).append("\n");
        sb.append("+---------------------+---------------------+\n");
        sb.append("|      Course         |       Grade         |\n");
        sb.append("+---------------------+---------------------+\n");
        for (CourseGrade gradeInfo : courseGrades) {
            String courseName = gradeInfo.getCourseName();
            double gradeValue = gradeInfo.getGrade();
            sb.append(String.format("|     %-15s | %11s         |\n", courseName, gradeValue));
        }
        sb.append("+---------------------+---------------------+\n");
        return sb.toString();
    }

    public static String displayCourses(int id, String name, Map<Integer, String> courseList) {
        StringBuilder result = new StringBuilder();
        result.append("              User Information:              \n");
        result.append("+---------------------------------------------+\n");
        result.append(String.format("|              UserID: %-8d               |\n", id));
        result.append(String.format("|               Name: %-23s |\n", name));
        result.append("+---------------------------------------------+\n");
        result.append("                   Courses:                    \n");
        result.append("+---------------------------------------------+\n");
        result.append("|       CourseID      |      Course           |\n");
        result.append("|---------------------------------------------|\n");
        if (courseList.isEmpty()) {
            result.append("|               No courses found.             |\n");
        } else {
            for (Map.Entry<Integer, String> course : courseList.entrySet()) {
                result.append(String.format("|       %-6d       |     %-17s |\n", course.getKey(), course.getValue()));
            }
        }
        result.append("+---------------------------------------------+\n");
        return result.toString();
    }

    public static String displayAvailableCourses(Map<Integer, String> availableCourses) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available courses:\n");
        sb.append(String.format("%-15s %-30s%n","Course ID", "Course Name"));
        for (Map.Entry<Integer, String> course : availableCourses.entrySet()) {
            sb.append(String.format(" %-16d %-32s%n", course.getKey(), course.getValue()));
        }
        return sb.toString();
    }

    public static String displayStudents(Map<Integer, String> students) {
        if (students.isEmpty()) {
            return "No students found";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Available students:\n");
        sb.append(String.format("%-15s %-30s%n","Student ID", "Student Name"));
        for (Map.Entry<Integer, String> student : students.entrySet()) {
            sb.append(String.format(" %-16d %-32s%n", student.getKey(), student.getValue()));
        }
        return sb.toString();
    }

    public static String displayStudentsWithGrades(List<StudentGrades> studentGradesList) {
        if (studentGradesList.isEmpty() || studentGradesList.get(0).getCourseGrades().isEmpty()) {
            return "No grades available.";
        }
        StringBuilder result = new StringBuilder();
        int courseId = studentGradesList.get(0).getCourseGrades().get(0).getCourseId();
        String courseName = studentGradesList.get(0).getCourseGrades().get(0).getCourseName();
        result.append("                 Course Information              \n");
        result.append("+------------------------------------------------+\n");
        result.append(String.format("|             Course ID: %-23d |\n", courseId));
        result.append(String.format("|             Course Name: %-21s |\n", courseName));
        result.append("+------------------------------------------------+\n");
        result.append("                     Students                     \n");
        result.append("+---------------+---------------------+----------+\n");
        result.append(String.format("| %-13s | %-19s | %-8s |\n", "Student ID", "Student Name", "Grade"));
        result.append("+---------------+---------------------+----------+\n");
        for (StudentGrades studentGrades : studentGradesList) {
            int studentId = studentGrades.getStudentId();
            String studentName = studentGrades.getStudentName();
            double grade = studentGrades.getCourseGrades().get(0).getGrade();
            result.append(String.format("| %-13d | %-19s | %-8.2f |\n", studentId, studentName, grade));
        }
        // Closing Border
        result.append("+---------------+---------------------+----------+\n");
        return result.toString();
    }
}





