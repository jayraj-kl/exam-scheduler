# Exam Scheduler — Backend

Spring Boot service that generates university exam timetables. It splits each subject's
students into exam slots, places the slots on morning and afternoon sessions across a
date range, and assigns a room, a faculty member, an exam head and invigilators to every
slot. Schedules can be exported to Excel and emailed to the staff involved.

It serves a REST API under `/api` (used by the React frontend in `../frontend`) and a set
of server-rendered JSP pages.

## Tech stack

- Java 17, Spring Boot 3.0 (Web, Data JPA, Validation, Mail)
- MySQL 8 (H2 in-memory for tests)
- Lombok, Apache POI (Excel export), Thymeleaf (email templates), JSP + Bootstrap (web UI)

## Getting started

### Prerequisites

- JDK 17
- Maven 3.8+
- A running MySQL server

### Configure

Settings are in `src/main/resources/application.properties`:

| Property | Default | Notes |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://127.0.0.1:3306/exam_scheduler` | Create the `exam_scheduler` database first |
| `spring.datasource.username` / `password` | `root` / `password` | |
| `spring.mail.username` / `password` | placeholders | Gmail address and app password, used for schedule emails |
| `server.port` | `8081` | The frontend expects `http://localhost:8081/api` |

Hibernate creates and updates the tables automatically (`ddl-auto=update`). Avoid
committing real credentials; override them with environment variables instead, e.g.
`SPRING_DATASOURCE_PASSWORD=...`.

### Run

```bash
mvn spring-boot:run
```

- Web UI: <http://localhost:8081/>
- REST API: <http://localhost:8081/api/...>

CORS is open to any `http://localhost:*` origin for `/api/**`, so the frontend dev server can call the API.

### Seed reference data

Rooms, students and exams can be created through the API. **Programs, subjects and
faculty have no endpoints yet** and must be inserted into the database directly, e.g.:

```sql
INSERT INTO program (name, department, code) VALUES ('Computer Science', 'CSE', 'cs');

INSERT INTO subject (name, code, program_id, total_students, regular_students, backlog_students)
VALUES ('Algorithms', 'CS101', 1, 70, 60, 10);

INSERT INTO faculty (name, department, email, workload_capacity, current_workload, is_exam_head, is_invigilator)
VALUES ('Dr. Rao', 'CSE', 'rao@example.edu', 10, 0, true, false),
       ('Ms. Iyer', 'CSE', 'iyer@example.edu', 10, 0, false, true);
```

Generating a schedule requires at least one exam head, one invigilator and enough
available rooms.

## Architecture

The code follows a layered design. Each layer only calls the layer below it.

```
controller  →  service  →  repository  →  MySQL
   (HTTP)      (business      (Spring
                 logic)      Data JPA)
```

```
com.exam.scheduler
├── ExamSchedulerApplication     Spring Boot entry point
├── config/                      WebConfig (CORS)
├── controller/                  REST + page controllers, HTTP concerns only
│   ├── ExamController           /api/exams     exams, subjects, slot queries & stats
│   ├── RoomController           /api/rooms
│   ├── StudentController        /api/students
│   ├── SchedulerController      /api/schedule  generate, view, export, email, allocate-all
│   ├── ExamSlotController       /api/schedule/slots    manual staff assignment, allocation
│   ├── FacultyController        /api/schedule/faculty  availability
│   ├── WebController            JSP pages (/, /exams, /students, /rooms, /schedule)
│   └── ControllerErrors         shared "unexpected error → HTTP 500" handling
├── service/                     business logic
│   ├── ExamService, SubjectService, RoomService, StudentService, FacultyService
│   ├── ExamSlotService          read-side slot queries and statistics
│   ├── ScheduleService          schedule lookup, slot management, manual assignment
│   ├── scheduling/
│   │   ├── ScheduleGenerationService   the scheduling algorithm
│   │   ├── ResourceAllocationService   room / faculty / invigilator allocation
│   │   └── SchedulingRules             slot size, session times, invigilator ratio
│   ├── export/ExcelExporter     .xlsx export
│   └── notification/EmailService  schedule emails
├── repository/                  Spring Data JPA repositories and custom JPQL queries
├── model/                       JPA entities
├── dto/                         request objects (AllocationOptions)
├── mapper/                      entity → response-map projections
└── exception/                   ResourceNotFoundException (→ 404)
```

### Conventions

- **Controllers** handle only HTTP: routing, parameter binding, status codes and response
  envelopes such as `{"deleted": true}`. They delegate everything else to services.
- **Services** hold the business rules and are injected through their constructors (Lombok `@RequiredArgsConstructor`).
- **Missing entities** raise `ResourceNotFoundException`, which becomes a 404. Endpoints that
  report unexpected failures as a 500 with a descriptive reason use the
  `ControllerErrors.withErrorPrefix(...)` helpers.
- **Transactions.** Generating a schedule runs in one transaction. `ResourceAllocationService.allocateResourcesForSchedule`
  calls the allocation methods on its own instance on purpose, so one slot that cannot be
  allocated is logged and skipped instead of rolling back the whole batch. Keep it that way
  when changing the allocation code.

## Scheduling algorithm

`ScheduleGenerationService.generateExamSchedule`:

1. Select subjects, either all of them or those in the requested programs (given as numeric ids or codes such as `"cs"`).
2. Sort subjects by enrolment, largest first.
3. Split each subject into slots of at most **35** students. Any leftover students get a slot of their own.
4. Place slots on alternating **morning (09:00)** and **afternoon (14:00)** sessions, each lasting 3 hours with
   reporting 30 minutes before the start. Weekends are skipped unless `includeWeekends` is set, and the
   placement wraps back to the start date when it passes the end date.
5. Allocate resources to every slot:
   - **Room:** an available, large-enough room not already used in that session, with the capacity closest to the slot size.
   - **Faculty:** the least-loaded faculty member under capacity with no conflicting exam.
   - **Exam head:** the least-loaded exam head.
   - **Invigilators:** one per 20 students (at least one), least-loaded first.

   Each assignment increases the faculty member's `currentWorkload`.

Allocation fails, and the whole generation rolls back, when no room, faculty, exam head
or invigilator is available.

## API reference

All paths are relative to `http://localhost:8081`.

### Schedules — `/api/schedule`

| Method | Path | Description |
|---|---|---|
| GET | `/api/schedule` | List schedules |
| POST | `/api/schedule/generate?startDate=&endDate=&scheduleName=` | Generate a schedule. Optional body: `{"programIds": ["cs"], "includeWeekends": false}` |
| GET | `/api/schedule/{id}` | Get a schedule |
| GET | `/api/schedule/{id}/slots` | Slots of a schedule |
| POST | `/api/schedule/{id}/slots` | Add a slot to a schedule (201) |
| GET | `/api/schedule/{id}/export` | Download the schedule as `.xlsx` |
| POST | `/api/schedule/{id}/email` | Email each assigned faculty member their slots |
| POST | `/api/schedule/{id}/allocate-all` | Fill missing resources on every slot. Body: `{"assignRoom", "assignFaculty", "assignInvigilators"}`, each defaulting to `true` |
| PUT | `/api/schedule/slots/{slotId}/allocate` | (Re)allocate resources for one slot, same body as above |
| PUT | `/api/schedule/slots/{slotId}/faculty?facultyId=` | Assign faculty |
| PUT | `/api/schedule/slots/{slotId}/examHead?facultyId=` | Assign an exam head (400 if not an exam head) |
| POST | `/api/schedule/slots/{slotId}/invigilators?facultyId=` | Add an invigilator (400 if not an invigilator, 409 if already assigned) |
| DELETE | `/api/schedule/slots/{slotId}/invigilators/{facultyId}` | Remove an invigilator |
| GET | `/api/schedule/faculty/available?date=&startTime=HH:mm:ss&endTime=HH:mm:ss` | Faculty free in a time window |
| POST | `/api/schedule/faculty/{facultyId}/availability` | Add hourly availability. Body: `{"date", "startTime", "endTime"}` |

### Exams — `/api/exams`

| Method | Path | Description |
|---|---|---|
| GET / POST | `/api/exams` | List / create exams |
| GET / PUT / DELETE | `/api/exams/{id}` | Read / update / delete an exam |
| GET | `/api/exams/date/{date}`, `/status/{status}`, `/subject?query=` | Filter exams |
| GET | `/api/exams/subjects`, `/subjects/program/{programId}` | Subjects |
| GET | `/api/exams/subject/{subject}` | Exam slots for a subject name or code |
| GET | `/api/exams/upcoming`, `/upcoming/subject/{subject}` | Exam slots in the next 7 days |
| GET | `/api/exams/schedule/{scheduleId}` | Exam slots in a schedule |
| GET | `/api/exams/stats`, `/stats/range?startDate=&endDate=` | Exam slot statistics |
| GET | `/api/exams/slots/faculty` | All slots with their assigned staff |
| GET | `/api/exams/faculty/workload` | Faculty workload and utilisation |

### Rooms — `/api/rooms`

| Method | Path | Description |
|---|---|---|
| GET / POST | `/api/rooms` | List / create rooms |
| GET / PUT / DELETE | `/api/rooms/{id}` | Read / update / delete a room |
| GET | `/api/rooms/available` | Rooms marked available |
| PUT | `/api/rooms/{id}/availability` | Body: `{"isAvailable": true}` |
| GET | `/api/rooms/stats`, `/stats/total-rooms`, `/stats/total-capacity`, `/stats/available-rooms` | Room statistics |

### Students — `/api/students`

| Method | Path | Description |
|---|---|---|
| GET / POST | `/api/students` | List / create students |
| GET / PUT / DELETE | `/api/students/{id}` | Read / update / delete a student |
| GET | `/api/students/program/{programId}`, `/status/{status}`, `/semester/{semester}` | Filter students |
| GET | `/api/students/stats/total`, `/stats/by-program`, `/stats/by-semester` | Student statistics |

## Testing

```bash
mvn test
```

`ApiCharacterizationTest` is a golden-master test. It boots the application against
in-memory H2, seeds data, sends about 110 requests covering every endpoint (success and
error paths), and compares status codes, error reasons, headers and response bodies
(JSON compared with object keys sorted) with
`src/test/resources/characterization/api-snapshot.txt`. Any change in observable
behaviour fails the test, and the actual output is written to `target/api-snapshot.actual.txt` for diffing.

If you change behaviour on purpose, regenerate the snapshot and review the diff before committing:

```bash
mvn test -DupdateSnapshot=true
git diff src/test/resources/characterization/api-snapshot.txt
```

## Known limitations

The snapshot test pins the following current behaviours:

- **Email doesn't work yet.** The Thymeleaf template `exam-schedule-template` does not exist under
  `src/main/resources/templates/`, so `POST /api/schedule/{id}/email` returns 500.
- **Export fails on incomplete slots.** It returns 500 if any slot is missing a faculty member,
  room, slot type or reporting time (for example, a slot added by hand with `POST /{id}/slots`).
- **A missing schedule returns 500, not 404,** on the `/api/schedule/{id}/...` endpoints.
- **`GET /api/schedule/faculty/available` returns 500, not 404,** when nobody is available.
- **`POST /api/schedule/generate` with a body must include `includeWeekends`.**
- **No create/update API** for programs, subjects or faculty.
