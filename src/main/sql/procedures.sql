-- Function to add S, C columns
CREATE OR REPLACE FUNCTION add_s_c()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
    DECLARE
s DECIMAL;
        c DECIMAL;
BEGIN
        s := 2*(new.l) + (new.p)/2 - new.T;
        c := (new.l) + (new.p)/2;
EXECUTE '
            UPDATE course_catalog
            SET s='||s||', c='||c||'
            WHERE course_id='||new.course_id||';
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER add_s_c_trig
AFTER INSERT ON course_catalog
FOR EACH ROW EXECUTE PROCEDURE add_s_c();

CREATE OR REPLACE FUNCTION create_batch_pc()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE '
            CREATE TABLE pc_'||new.entry_year||'_'||new.dept||'(
                course_id INT,
                course_code VARCHAR(10) NOT NULL,
                course_category VARCHAR(2) NOT NULL,
                PRIMARY KEY(course_id),
                FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
            );
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER create_batch_pc_trig
AFTER INSERT ON batches
FOR EACH ROW EXECUTE PROCEDURE create_batch_pc();

CREATE OR REPLACE FUNCTION create_batch_el()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE '
            CREATE TABLE el_'||new.entry_year||'_'||new.dept||'(
                course_id INT,
                course_code VARCHAR(10) NOT NULL,
                course_category VARCHAR(2) NOT NULL,
                PRIMARY KEY(course_id),
                FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
            );
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER create_batch_el_trig
AFTER INSERT ON batches
FOR EACH ROW EXECUTE PROCEDURE create_batch_el();

CREATE OR REPLACE FUNCTION create_batch_extra_cap()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE '
            CREATE TABLE IF NOT EXISTS extra_cap_'||new.entry_year||'(
                course_id INT,
                course_code VARCHAR(10) NOT NULL,
                course_category VARCHAR(2) NOT NULL,
                PRIMARY KEY(course_id),
                FOREIGN KEY(course_id) REFERENCES course_catalog(course_id)
            );
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER create_batch_extra_cap_trig
AFTER INSERT ON batches
FOR EACH ROW EXECUTE PROCEDURE create_batch_extra_cap();


-- Function to create offerings table
CREATE OR REPLACE FUNCTION create_offering()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE '
            CREATE TABLE IF NOT EXISTS offering_'||new.offering_id||'(
                student_id INT,
                grade VARCHAR(2),
                PRIMARY KEY(student_id),
                FOREIGN KEY(student_id) REFERENCES students(student_id)
            )
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER create_offering_trig
AFTER INSERT ON offerings
FOR EACH ROW EXECUTE PROCEDURE create_offering();


CREATE OR REPLACE FUNCTION delete_offering()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE 'DROP TABLE offering_'||old.offering_id||';';
RETURN old;
END;
$$;
CREATE OR REPLACE TRIGGER delete_offering_trig
AFTER DELETE ON offerings
FOR EACH ROW EXECUTE PROCEDURE delete_offering();

-- Function to create a new student table
CREATE OR REPLACE FUNCTION create_student()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE '
            CREATE TABLE IF NOT EXISTS student_'||new.student_id||'(
                offering_id INT,
                course_code VARCHAR(10) NOT NULL,
                status VARCHAR(10) NOT NULL,
                grade VARCHAR(2),
                PRIMARY KEY(offering_id),
                FOREIGN KEY(offering_id) REFERENCES offerings(offering_id)
            )
        ';
RETURN new;
END;
$$;
CREATE OR REPLACE TRIGGER create_student_trig
AFTER INSERT ON students
FOR EACH ROW EXECUTE PROCEDURE create_student();

CREATE OR REPLACE FUNCTION delete_student()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
EXECUTE 'DROP TABLE student_'||old.student_id||';';
RETURN old;
END;
$$;
CREATE OR REPLACE TRIGGER delete_student_trig
AFTER DELETE ON students
FOR EACH ROW EXECUTE PROCEDURE delete_student();

-- function to find CGPA
create or replace function findCG(id integer)
returns numeric(11,2)
language plpgsql
as
$$
	declare
taken_courses cursor for (
            select course_code, grade
            from FORMAT("student_%l", id)
        );
		row_traverse record;
		cgpa numeric(11,2) default 0;
		total_credits integer default 0;
		credits integer default 0;
begin
open taken_courses;
loop
fetch taken_courses into row_traverse;
		exit when not found;
		credits := (
			select c
			from course_catalog
			where course_code = row_traverse.course_code
        );
		total_credits = total_credits + credits;
		if row_traverse.grade = 'A' then
		cgpa = cgpa + credits*10;
		elsif row_traverse.grade = 'A-' then
		cgpa = cgpa + credits*9;
		elsif row_traverse.grade = 'B' then
		cgpa = cgpa + credits*8;
		elsif row_traverse.grade = 'B-' then
		cgpa = cgpa + credits*7;
		elsif row_traverse.grade = 'C' then
		cgpa = cgpa + credits*6;
		elsif row_traverse.grade = 'C-' then
		cgpa = cgpa + credits*5;
		elsif row_traverse.grade = 'D' then
		cgpa = cgpa + credits*4;
		elsif row_traverse.grade = 'D-' then
		cgpa = cgpa + credits*3;
		elsif row_traverse.grade = 'E' then
		cgpa = cgpa + credits*2;
		elsif row_traverse.grade = 'E-' then
		cgpa = cgpa + credits*1;
else cgpa = cgpa + 0;
end if;
end loop;
		if total_credits = 0 then return 0.0;
else return cgpa/total_credits;
end if;
end
$$;