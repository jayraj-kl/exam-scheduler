package com.exam.scheduler;

import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.model.Program;
import com.exam.scheduler.model.Room;
import com.exam.scheduler.model.Subject;
import com.exam.scheduler.repository.FacultyRepository;
import com.exam.scheduler.repository.ProgramRepository;
import com.exam.scheduler.repository.RoomRepository;
import com.exam.scheduler.repository.SubjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Golden-master test that pins the observable behaviour of every HTTP endpoint.
 *
 * <p>It seeds an in-memory database, drives the API through a fixed sequence of
 * requests (happy paths and error paths) and records the status code, error
 * reason, relevant headers and raw response body of each call. The transcript
 * must match {@code src/test/resources/characterization/api-snapshot.txt}
 * exactly, so any refactoring that changes a result fails this test.
 *
 * <p>To regenerate the snapshot after an intentional behaviour change, run
 * {@code mvn test -DupdateSnapshot=true} and review the diff.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:characterization;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class ApiCharacterizationTest {

    private static final Path SNAPSHOT = Path.of("src/test/resources/characterization/api-snapshot.txt");
    private static final Path ACTUAL = Path.of("target/api-snapshot.actual.txt");
    private static final ObjectMapper CANONICAL_JSON = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private RoomRepository roomRepository;

    private final StringBuilder transcript = new StringBuilder();

    @Test
    void apiResponsesMatchRecordedSnapshot() throws Exception {
        seed();
        rooms();
        students();
        exams();
        schedules();
        slotsAndFaculty();
        examSlotQueries();
        pages();
        cors();

        String actual = transcript.toString().replace(LocalDate.now().toString(), "<TODAY>");
        if (Boolean.getBoolean("updateSnapshot")) {
            Files.createDirectories(SNAPSHOT.getParent());
            Files.writeString(SNAPSHOT, actual, StandardCharsets.UTF_8);
            return;
        }
        Files.createDirectories(ACTUAL.getParent());
        Files.writeString(ACTUAL, actual, StandardCharsets.UTF_8);
        String expected = Files.readString(SNAPSHOT, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertEquals(expected, actual, "API behaviour changed; compare " + SNAPSHOT + " with " + ACTUAL);
    }

    // ---------------------------------------------------------------- scenarios

    private void seed() {
        Program cs = program("Computer Science", "CSE", "cs");
        Program it = program("Information Technology", "IT", "it");

        subject("Algorithms", "CS101", cs, 70, 60, 10);
        subject("Databases", "CS102", cs, 40, 40, 0);
        subject("Networks", "IT201", it, 20, 0, 20);

        faculty("Head One", true, false, 10);
        faculty("Head Two", true, false, 10);
        faculty("Invigilator Three", false, true, 10);
        faculty("Invigilator Four", false, true, 10);
        faculty("Invigilator Five", false, true, 10);
        faculty("Invigilator Six", false, true, 10);
        faculty("Regular Seven", false, false, 5);

        room("A101", 40, true);
        room("A102", 60, true);
        room("B201", 35, true);
        room("C301", 100, false);
    }

    private void rooms() throws Exception {
        get("/api/rooms");
        get("/api/rooms/1");
        get("/api/rooms/999");
        get("/api/rooms/available");
        post("/api/rooms", "{\"roomNumber\":\"D401\",\"building\":\"D\",\"floor\":\"4\",\"seatingCapacity\":50,"
                + "\"roomType\":\"Lab\",\"isAvailable\":true}");
        put("/api/rooms/5", "{\"roomNumber\":\"D402\",\"building\":\"D\",\"floor\":\"4\",\"seatingCapacity\":55,"
                + "\"roomType\":\"Hall\",\"isAvailable\":true}");
        put("/api/rooms/999", "{\"roomNumber\":\"X\"}");
        put("/api/rooms/5/availability", "{\"isAvailable\":false}");
        put("/api/rooms/5/availability", "{}");
        put("/api/rooms/999/availability", "{\"isAvailable\":true}");
        get("/api/rooms/stats");
        get("/api/rooms/stats/total-rooms");
        get("/api/rooms/stats/total-capacity");
        get("/api/rooms/stats/available-rooms");
        delete("/api/rooms/5");
        delete("/api/rooms/999");
    }

    private void students() throws Exception {
        post("/api/students", "{\"studentId\":\"S001\",\"name\":\"Alice\",\"email\":\"alice@example.com\","
                + "\"phone\":\"111\",\"program\":{\"id\":1},\"semester\":3,\"status\":\"ACTIVE\","
                + "\"enrollmentDate\":\"2028-08-01\",\"address\":\"Street 1\"}");
        post("/api/students", "{\"studentId\":\"S002\",\"name\":\"Bob\",\"email\":\"bob@example.com\","
                + "\"phone\":\"222\",\"program\":{\"id\":2},\"semester\":5,\"status\":\"INACTIVE\"}");
        post("/api/students", "{\"studentId\":\"S003\",\"name\":\"Cara\",\"semester\":3,\"status\":\"ACTIVE\"}");
        get("/api/students");
        get("/api/students/1");
        get("/api/students/999");
        get("/api/students/program/1");
        get("/api/students/status/ACTIVE");
        get("/api/students/semester/3");
        put("/api/students/2", "{\"studentId\":\"S002\",\"name\":\"Bobby\",\"program\":{\"id\":1},"
                + "\"semester\":6,\"status\":\"ACTIVE\"}");
        put("/api/students/999", "{\"name\":\"Nobody\"}");
        get("/api/students/stats/total");
        get("/api/students/stats/by-program");
        get("/api/students/stats/by-semester");
        delete("/api/students/3");
        delete("/api/students/999");
    }

    private void exams() throws Exception {
        get("/api/exams/subjects");
        get("/api/exams/subjects/program/1");
        post("/api/exams", "{\"examName\":\"Midterm\",\"subjectEntity\":{\"id\":1},\"examDate\":\"2030-01-07\","
                + "\"duration\":180,\"startTime\":\"09:00:00\",\"status\":\"SCHEDULED\",\"description\":\"Mid\"}");
        post("/api/exams", "{\"examName\":\"Ghost\",\"subjectEntity\":{\"id\":999}}");
        post("/api/exams", "{\"examName\":\"Plain\",\"subject\":\"Physics\",\"examDate\":\"2030-01-08\","
                + "\"status\":\"DRAFT\"}");
        post("/api/exams", "{\"examName\":\"Named\",\"subject\":\"Custom\",\"subjectEntity\":{\"id\":2}}");
        get("/api/exams");
        get("/api/exams/1");
        get("/api/exams/999");
        get("/api/exams/date/2030-01-07");
        get("/api/exams/status/SCHEDULED");
        get("/api/exams/subject?query=Phy");
        put("/api/exams/2", "{\"examName\":\"Plain2\",\"subjectEntity\":{\"id\":2},\"status\":\"SCHEDULED\"}");
        put("/api/exams/1", "{\"examName\":\"Mid2\",\"subject\":\"X\",\"duration\":90}");
        put("/api/exams/2", "{\"examName\":\"Plain3\",\"subjectEntity\":{\"id\":999}}");
        put("/api/exams/999", "{\"examName\":\"Nope\"}");
        delete("/api/exams/3");
        delete("/api/exams/999");
    }

    private void schedules() throws Exception {
        get("/api/schedule");
        post("/api/schedule/generate?startDate=2030-01-10&endDate=2030-01-07&scheduleName=Backwards", null);
        post("/api/schedule/generate?startDate=2030-01-07&endDate=2030-01-11&scheduleName=CS%20Winter",
                "{\"programIds\":[\"cs\"],\"includeWeekends\":false}");
        post("/api/schedule/generate?startDate=2030-03-02&endDate=2030-03-03&scheduleName=IT%20Weekend",
                "{\"programIds\":[\"2\"],\"includeWeekends\":true}");
        post("/api/schedule/generate?startDate=2030-02-04&endDate=2030-02-08&scheduleName=No%20Body", null);
        post("/api/schedule/generate?startDate=2030-04-01&endDate=2030-04-05&scheduleName=Empty%20Body", "{}");
        post("/api/schedule/generate?startDate=2030-04-01&endDate=2030-04-05&scheduleName=Numeric",
                "{\"programIds\":[1],\"includeWeekends\":false}");
        post("/api/schedule/generate?startDate=2030-04-01&endDate=2030-04-05&scheduleName=Unknown",
                "{\"programIds\":[\"zz\"],\"includeWeekends\":false}");
        get("/api/schedule");
        get("/api/schedule/1");
        get("/api/schedule/999");
        get("/api/schedule/1/slots");
        get("/api/schedule/999/slots");
        get("/api/schedule/1/export");
        get("/api/schedule/999/export");
        post("/api/schedule/1/email", null);
        post("/api/schedule/999/email", null);
    }

    private void slotsAndFaculty() throws Exception {
        put("/api/schedule/slots/1/faculty?facultyId=7", null);
        put("/api/schedule/slots/1/faculty?facultyId=999", null);
        put("/api/schedule/slots/999/faculty?facultyId=1", null);
        put("/api/schedule/slots/1/examHead?facultyId=3", null);
        put("/api/schedule/slots/1/examHead?facultyId=2", null);
        put("/api/schedule/slots/999/examHead?facultyId=2", null);
        post("/api/schedule/slots/1/invigilators?facultyId=1", null);
        post("/api/schedule/slots/1/invigilators?facultyId=6", null);
        post("/api/schedule/slots/1/invigilators?facultyId=6", null);
        post("/api/schedule/slots/999/invigilators?facultyId=6", null);
        delete("/api/schedule/slots/1/invigilators/6");
        delete("/api/schedule/slots/1/invigilators/6");
        delete("/api/schedule/slots/1/invigilators/999");
        delete("/api/schedule/slots/999/invigilators/6");

        get("/api/schedule/faculty/available?date=2030-01-07&startTime=09:00:00&endTime=12:00:00");
        get("/api/schedule/faculty/available?date=2020-01-07&startTime=09:00:00&endTime=12:00:00");
        get("/api/schedule/faculty/available?date=2030-01-07&startTime=12:00:00&endTime=09:00:00");
        post("/api/schedule/faculty/7/availability",
                "{\"date\":\"2030-01-07\",\"startTime\":\"09:00\",\"endTime\":\"12:00\"}");
        post("/api/schedule/faculty/999/availability",
                "{\"date\":\"2030-01-07\",\"startTime\":\"09:00\",\"endTime\":\"12:00\"}");
        post("/api/schedule/faculty/7/availability", "{\"date\":\"not-a-date\"}");
        get("/api/exams/faculty/workload");

        put("/api/schedule/slots/2/allocate", "{}");
        put("/api/schedule/slots/3/allocate", "{\"assignRoom\":false,\"assignInvigilators\":false}");
        put("/api/schedule/slots/3/allocate", "{\"assignRoom\":null}");
        put("/api/schedule/slots/999/allocate", "{}");

        post("/api/schedule/1/slots", "{\"examDate\":\"2030-01-10\",\"startTime\":\"09:00:00\","
                + "\"endTime\":\"12:00:00\",\"studentCount\":10,\"subject\":{\"id\":3}}");
        post("/api/schedule/999/slots", "{\"examDate\":\"2030-01-10\"}");
        get("/api/schedule/1/export");
        post("/api/schedule/1/allocate-all", "{}");
        post("/api/schedule/1/allocate-all", "{\"assignFaculty\":false}");
        post("/api/schedule/1/allocate-all", "{\"assignFaculty\":null}");
        post("/api/schedule/999/allocate-all", "{}");
        get("/api/schedule/1/slots");
        get("/api/schedule/1/export");
        get("/api/exams/faculty/workload");
    }

    private void examSlotQueries() throws Exception {
        get("/api/exams/subject/Algorithms");
        get("/api/exams/subject/CS102");
        get("/api/exams/upcoming");
        get("/api/exams/upcoming/subject/Algorithms");
        get("/api/exams/stats");
        get("/api/exams/stats/range?startDate=2030-01-01&endDate=2030-12-31");
        get("/api/exams/stats/range?startDate=2030-12-31&endDate=2030-01-01");
        get("/api/exams/schedule/1");
        get("/api/exams/schedule/999");
        get("/api/exams/slots/faculty");
        get("/api/rooms/stats");
    }

    private void pages() throws Exception {
        get("/");
        get("/exams");
        get("/students");
        get("/rooms");
        get("/schedule");
    }

    private void cors() throws Exception {
        preflight("/api/rooms", "http://localhost:5173", "DELETE");
        preflight("/api/schedule/generate", "http://localhost:3000", "POST");
        preflight("/api/rooms", "http://example.com", "GET");
        record("GET", "/api/rooms/stats/total-rooms (Origin: http://localhost:5173)", null,
                MockMvcRequestBuilders.get("/api/rooms/stats/total-rooms").header("Origin", "http://localhost:5173"));
    }

    private void preflight(String url, String origin, String method) throws Exception {
        record("OPTIONS", url + " (Origin: " + origin + ", method: " + method + ")", null,
                MockMvcRequestBuilders.options(url)
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", method)
                        .header("Access-Control-Request-Headers", "Content-Type"));
    }

    // ---------------------------------------------------------------- seeding

    private Program program(String name, String department, String code) {
        Program program = new Program();
        program.setName(name);
        program.setDepartment(department);
        program.setCode(code);
        return programRepository.save(program);
    }

    private void subject(String name, String code, Program program, int total, int regular, int backlog) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code);
        subject.setProgram(program);
        subject.setTotalStudents(total);
        subject.setRegularStudents(regular);
        subject.setBacklogStudents(backlog);
        subjectRepository.save(subject);
    }

    private void faculty(String name, boolean examHead, boolean invigilator, int capacity) {
        Faculty faculty = new Faculty();
        faculty.setName(name);
        faculty.setDepartment("CSE");
        faculty.setEmail(name.toLowerCase().replace(' ', '.') + "@example.com");
        faculty.setWorkloadCapacity(capacity);
        faculty.setExamHead(examHead);
        faculty.setInvigilator(invigilator);
        facultyRepository.save(faculty);
    }

    private void room(String number, int capacity, boolean available) {
        Room room = new Room();
        room.setRoomNumber(number);
        room.setBuilding(number.substring(0, 1));
        room.setFloor(number.substring(1, 2));
        room.setSeatingCapacity(capacity);
        room.setRoomType("Classroom");
        room.setIsAvailable(available);
        roomRepository.save(room);
    }

    // ---------------------------------------------------------------- recording

    private void get(String url) throws Exception {
        record("GET", url, null, MockMvcRequestBuilders.get(url));
    }

    private void post(String url, String json) throws Exception {
        record("POST", url, json, MockMvcRequestBuilders.post(url));
    }

    private void put(String url, String json) throws Exception {
        record("PUT", url, json, MockMvcRequestBuilders.put(url));
    }

    private void delete(String url) throws Exception {
        record("DELETE", url, null, MockMvcRequestBuilders.delete(url));
    }

    private void record(String method, String url, String json, MockHttpServletRequestBuilder request)
            throws Exception {
        transcript.append("### ").append(method).append(' ').append(url);
        if (json != null) {
            transcript.append(' ').append(json);
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        transcript.append('\n');

        MvcResult result;
        try {
            result = mvc.perform(request).andReturn();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            line("unhandled", root.getClass().getName() + ": " + root.getMessage());
            transcript.append('\n');
            return;
        }

        MockHttpServletResponse response = result.getResponse();
        line("status", String.valueOf(response.getStatus()));
        line("error", response.getErrorMessage());
        line("content-type", response.getContentType());
        line("content-disposition", response.getHeader("Content-Disposition"));
        line("forwarded", response.getForwardedUrl());
        for (String header : new String[] {"Access-Control-Allow-Origin", "Access-Control-Allow-Methods",
                "Access-Control-Allow-Headers", "Access-Control-Allow-Credentials"}) {
            line(header, response.getHeader(header));
        }
        if (result.getModelAndView() != null) {
            line("model", String.valueOf(result.getModelAndView().getModel().get("pageTitle")));
        }
        byte[] body = response.getContentAsByteArray();
        if (body.length > 0 && "application/octet-stream".equals(response.getContentType())) {
            line("workbook", describeWorkbook(body));
        } else if (body.length > 0 && MediaType.APPLICATION_JSON_VALUE.equals(response.getContentType())) {
            line("body", canonicalJson(response.getContentAsString(StandardCharsets.UTF_8)));
        } else if (body.length > 0) {
            line("body", response.getContentAsString(StandardCharsets.UTF_8));
        }
        transcript.append('\n');
    }

    private void line(String key, String value) {
        if (value != null) {
            transcript.append(key).append(": ").append(value).append('\n');
        }
    }

    /**
     * Re-serialises JSON with object keys sorted. Key order carries no meaning, and for
     * getter-only properties (e.g. {@code Faculty.isExamHead()}) Jackson's order follows
     * reflection order, which can differ between builds. Array order is preserved.
     */
    private static String canonicalJson(String json) throws Exception {
        return CANONICAL_JSON.writeValueAsString(CANONICAL_JSON.readValue(json, Object.class));
    }

    private static String describeWorkbook(byte[] bytes) throws Exception {
        StringBuilder out = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            out.append(sheet.getSheetName());
            for (Row row : sheet) {
                out.append("\n  |");
                for (Cell cell : row) {
                    out.append(' ').append(formatter.formatCellValue(cell)).append(" |");
                }
            }
        }
        return out.toString();
    }
}
