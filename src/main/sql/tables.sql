CREATE TABLE batches(
        batch_id INT GENERATED ALWAYS AS IDENTITY,
        entry_year INT NOT NULL,
        dept VARCHAR(10) NOT NULL,
        PRIMARY KEY(batch_id)
);

CREATE TABLE students(
         batch_id INT NOT NULL,
         student_id INT GENERATED ALWAYS AS IDENTITY,
         entry_year INT NOT NULL,
         dept VARCHAR(10) NOT NULL,
         phone CHAR(10),
         address VARCHAR(100),
         PRIMARY KEY(student_id),
         FOREIGN KEY(batch_id) REFERENCES batches(batch_id)
);

CREATE TABLE course_catalog(
           course_id INT GENERATED ALWAYS AS IDENTITY,
           course_code VARCHAR(10) NOT NULL,
           course_title VARCHAR(100) NOT NULL,
           L NUMERIC(4,2) NOT NULL,
           T NUMERIC(4,2) NOT NULL,
           P NUMERIC(4,2) NOT NULL,
           S NUMERIC(4,2),
           C NUMERIC(4,2),
           PRIMARY KEY(course_id)
);

CREATE TABLE pre_req(
        course_code VARCHAR(10) NOT NULL,
        pre_req_course_id INT,
        pre_req_code VARCHAR(10) NOT NULL,
        PRIMARY KEY(course_code, pre_req_course_id),
        FOREIGN KEY(pre_req_course_id) REFERENCES course_catalog(course_id)
);

CREATE TABLE optional_pre_req(
         pre_req_code VARCHAR(10) NOT NULL,
         option_course_id INT,
         option_code VARCHAR(10) NOT NULL,
         PRIMARY KEY(pre_req_code, option_course_id),
         FOREIGN KEY(option_course_id) REFERENCES course_catalog(course_id)
);

CREATE TABLE faculty(
        faculty_id INT GENERATED ALWAYS AS IDENTITY,
        dept VARCHAR(10) NOT NULL,
        phone VARCHAR(10),
        address VARCHAR(100),
        PRIMARY KEY(faculty_id)
);

CREATE TABLE offerings(
          offering_id INT GENERATED ALWAYS AS IDENTITY,
          faculty_id INT NOT NULL,
          course_id INT NOT NULL,
          year_offered_in INT NOT NULL,
          semester_offered_in INT NOT NULL,
          PRIMARY KEY(offering_id),
          FOREIGN KEY(faculty_id) REFERENCES faculty(faculty_id),
          FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
);

CREATE TABLE student_credentials(
        id INT NOT NULL,
        username VARCHAR(50) NOT NULL,
        pass VARCHAR(50) NOT NULL,
        PRIMARY KEY(username, pass),
        FOREIGN KEY(id) REFERENCES students(student_id)
);

CREATE TABLE faculty_credentials(
        id INT NOT NULL,
        username VARCHAR(50) NOT NULL,
        pass VARCHAR(50) NOT NULL,
        PRIMARY KEY(username, pass),
        FOREIGN KEY(id) REFERENCES faculty(faculty_id)
);

CREATE TABLE acad_credentials(
         id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
         username VARCHAR(50) NOT NULL,
         pass VARCHAR(50) NOT NULL,
         PRIMARY KEY(username, pass)
);

CREATE TABLE current_semester(
         academic_year INT NOT NULL,
         semester INT NOT NULL,
         current BOOLEAN NOT NULL,
         PRIMARY KEY(academic_year, semester)
);

CREATE TABLE upcoming_semester(
          academic_year INT NOT NULL,
          semester INT NOT NULL,
          upcoming BOOLEAN NOT NULL,
          PRIMARY KEY(academic_year, semester)
);

CREATE TABLE offering_constraints(
         offering_id INT,
         course_id INT,
         grade VARCHAR(2),
         PRIMARY KEY(offering_id, course_id),
         FOREIGN KEY(offering_id) REFERENCES offerings(offering_id),
         FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
);

CREATE TABLE optional_offering_constraints(
          offering_id INT,
          course_id INT,
          option_course_id INT,
          grade VARCHAR(2),
          PRIMARY KEY(offering_id, course_id, option_course_id),
          FOREIGN KEY(offering_id) REFERENCES offerings(offering_id),
          FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
);

CREATE TABLE offering_cg_constraints(
        offering_id INT,
        cg NUMERIC(3,2),
        PRIMARY KEY(offering_id),
        FOREIGN KEY(offering_id) REFERENCES offerings(offering_id)
);

CREATE TABLE semester_events(
        event_id INT GENERATED ALWAYS AS IDENTITY,
        event_description VARCHAR(100) NOT NULL,
        is_open BOOLEAN NOT NULL,
        PRIMARY KEY(event_id)
);

CREATE TABLE credit_limits(
          student_id INT NOT NULL,
          academic_year INT NOT NULL,
          semester INT NOT NULL,
          credit_limit NUMERIC(4,2) NOT NULL,
          PRIMARY KEY(student_id, academic_year, semester),
          FOREIGN KEY(student_id) REFERENCES students(student_id)
);