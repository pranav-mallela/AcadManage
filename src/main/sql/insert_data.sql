-- batches
INSERT INTO batches(entry_year, dept) VALUES (2020, 'CSE');
-- students
INSERT INTO students(batch_id, entry_year, dept) VALUES (1, 2020, 'CSE');
--faculty
INSERT INTO faculty(dept) VALUES ('CSE');
-- credentials
INSERT INTO student_credentials VALUES (1, 'pstudent1', '1234');
INSERT INTO faculty_credentials VALUES (1, 'pfaculty1', '1234');
INSERT INTO acad_credentials(username, pass) VALUES ('pacad1', '1234');
--semesters
INSERT INTO upcoming_semester VALUES(2023, 1, 1::BOOLEAN);
-- semester events
INSERT INTO semester_events(event_description, is_open) VALUES ('Before Semester', 1::BOOLEAN);
INSERT INTO semester_events(event_description, is_open) VALUES ('Semester Running', 0::BOOLEAN);
INSERT INTO semester_events(event_description, is_open) VALUES ('After Semester', 0::BOOLEAN);
-- course catalog
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('MA101', 'CALCULUS', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS103', 'PROFESSIONAL ENGLISH COMMUNICATION', 2, 0, 2, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('PH101', 'Physics for Engineers', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS101', 'DISCRETE MATHEMATICAL STRUCTURES', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('MA102', 'LINEAR ALGEBRA, INTEGRAL TRANSFORMS AND SPECIAL FUNCTIONS', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS101', 'HISTORY OF TECHNOLOGY', 1.5, 0.5, 0, 2.5, 1.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE103', 'INTRODUCTION TO COMPUTING AND DATA STRUCTURES', 3, 0, 3, 7, 4.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE105', 'ENGINEERING DRAWING', 0, 0, 3, 1, 1.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE101', 'TECHNOLOGY MUSEUM LAB', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE102', 'WORKSHOP PRACTICE', 0, 0, 4, 2, 2);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CY101', 'CHEMISTRY FOR ENGINEERS', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('PH102', 'Physics for Engineers Lab', 0, 0, 4, 2, 2);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE104', 'INTRODUCTION TO ELECTRICAL ENGINEERING', 2, 0.67, 2, 4.33, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS201', 'Data Structures', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS203', 'Digital Logic Design', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('EE201', 'Signals and Systems', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS201', 'Economics', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE107', 'Tinkering Lab', 0, 0, 3, 1.5, 1.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('MA201', 'Differential Equations', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS202', 'HUMAN GEOGRAPHY AND SOCIAL NEEDS', 1, 0.33, 4, 3.67, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('MA202', 'Probability and Statistics', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE108', 'Basic Electronics', 2, 0.67, 2, 4.33, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE109', 'Introduction to Engineering Products', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS202', 'Programming Paradigms and Pragmatics', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS204', 'Computer Architecture', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS104', 'Professional Ethics', 1, 0.33, 1, 2.17, 1.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('BM101', 'Biology for Engineers ', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS301', 'Databases', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS302', 'Analysis and Design of Algorithms', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS303', 'Operating Systems', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS301', 'Industrial Management', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS304 ', 'Computer Networks', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS305', 'Software Engineering', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS306', 'Theory of Computation', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('GE111', 'Introduction to Environmental Science and Engineering', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('NS101', 'NSS I', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('NS102', 'NSS II', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('NS103', 'NSS III', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('NS104', 'NSS IV', 0, 0, 2, 1, 1);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CP301', 'Development Engineering Project', 0, 0, 6, 3, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CP302', 'Capstone I', 0, 0, 6, 3, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CP303', 'Capstone II', 0, 0, 6, 3, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS517', 'Digital Image Processing and Analysis', 2, 1, 2, 4, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS535', 'Introduction to game theory and mechanism design', 3, 1, 0, 5, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS539', 'IoT', 3, 0, 0, 6, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS505', 'Sound patterns in human language', 3, 0, 0, 6, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('HS507', 'Positive Psychology & Wellbeing', 3, 0, 0, 6, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('MA703', 'Computational PDE', 3, 1, 2, 6, 4);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CY427', 'Interpretive Molecular Spectroscopy', 3, 0, 0, 6, 3);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('II301', 'Industrial Internship and Comprehensive Viva', 0, 0, 7, 3.5, 3.5);
INSERT INTO course_catalog(course_code, course_title, l, t, p, s, c) VALUES ('CS533', 'Reinforcement Learning', 3, 0, 0, 6, 3);
-- core courses
INSERT INTO pc_2020_cse VALUES(1, 'GE103', 'GR');
INSERT INTO pc_2020_cse VALUES(2, 'MA101', 'SC');
INSERT INTO pc_2020_cse VALUES(3, 'HS103', 'HC');
INSERT INTO pc_2020_cse VALUES(4, 'PH101', 'SC');
INSERT INTO pc_2020_cse VALUES(5, 'GE105', 'GR');
INSERT INTO pc_2020_cse VALUES(6, 'CS101', 'PC');
INSERT INTO pc_2020_cse VALUES(7, 'MA102', 'SC');
INSERT INTO pc_2020_cse VALUES(8, 'HS101', 'HC');
INSERT INTO pc_2020_cse VALUES(9, 'GE101', 'GR');
INSERT INTO pc_2020_cse VALUES(10, 'GE102', 'GR');
INSERT INTO pc_2020_cse VALUES(11, 'CY101', 'SC');
INSERT INTO pc_2020_cse VALUES(12, 'PH102', 'SC');
INSERT INTO pc_2020_cse VALUES(13, 'GE104', 'GR');
INSERT INTO pc_2020_cse VALUES(14, 'CS201', 'PC');
INSERT INTO pc_2020_cse VALUES(15, 'CS203', 'PC');
INSERT INTO pc_2020_cse VALUES(16, 'EE201', 'GR');
INSERT INTO pc_2020_cse VALUES(17, 'HS201', 'HC');
INSERT INTO pc_2020_cse VALUES(18, 'GE107', 'GR');
INSERT INTO pc_2020_cse VALUES(19, 'MA201', 'SC');
INSERT INTO pc_2020_cse VALUES(20, 'HS202', 'HC');
INSERT INTO pc_2020_cse VALUES(21, 'MA202', 'SC');
INSERT INTO pc_2020_cse VALUES(22, 'GE108', 'GR');
INSERT INTO pc_2020_cse VALUES(23, 'GE109', 'GR');
INSERT INTO pc_2020_cse VALUES(24, 'CS202', 'PC');
INSERT INTO pc_2020_cse VALUES(25, 'CS204', 'PC');
INSERT INTO pc_2020_cse VALUES(26, 'HS104', 'HC');
INSERT INTO pc_2020_cse VALUES(27, 'GE111', 'GR');
INSERT INTO pc_2020_cse VALUES(28, 'BM101', 'SC');
INSERT INTO pc_2020_cse VALUES(29, 'CS301', 'PC');
INSERT INTO pc_2020_cse VALUES(30, 'CS302', 'PC');
INSERT INTO pc_2020_cse VALUES(31, 'CS303', 'PC');
INSERT INTO pc_2020_cse VALUES(32, 'HS301', 'HC');
INSERT INTO pc_2020_cse VALUES(33, 'CS304', 'PC');
INSERT INTO pc_2020_cse VALUES(34, 'CS305', 'PC');
INSERT INTO pc_2020_cse VALUES(35, 'CS306', 'PC');
-- elective courses
INSERT INTO el_2020_cse VALUES(43, 'CS517', 'PE');
INSERT INTO el_2020_cse VALUES(44, 'CS535', 'PE');
INSERT INTO el_2020_cse VALUES(45, 'CS539', 'PE');
INSERT INTO el_2020_cse VALUES(51, 'CS533', 'PE');
INSERT INTO el_2020_cse VALUES(46, 'HS505', 'HE');
INSERT INTO el_2020_cse VALUES(47, 'HS507', 'HE');
INSERT INTO el_2020_cse VALUES(48, 'MA703', 'SE');
INSERT INTO el_2020_cse VALUES(49, 'CY427', 'SE');
-- extra-curricular and capstone courses
INSERT INTO extra_cap_2020 VALUES(36, 'NS101', 'NN');
INSERT INTO extra_cap_2020 VALUES(37, 'NS102', 'NN');
INSERT INTO extra_cap_2020 VALUES(38, 'NS103', 'NN');
INSERT INTO extra_cap_2020 VALUES(39, 'NS104', 'NN');


INSERT INTO extra_cap_2020 VALUES(40, 'CP301', 'CP');
INSERT INTO extra_cap_2020 VALUES(41, 'CP302', 'CP');
INSERT INTO extra_cap_2020 VALUES(42, 'CP303', 'CP');

INSERT INTO extra_cap_2020 VALUES(50, 'II301', 'II');
-- offerings
INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);
