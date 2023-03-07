# CS305 Mini-Project: AcadManage

## How to compile and run the application: 

Install PostgreSQL and create a database named `acadmanage` with the following credentials: 

    username: acadadmin
    password: adminpass

Copy the contents of the following into the `acadmanage` database. The order of execution is important:
1. `src/main/sql/tables.sql`
2. `src/main/sql/procedures.sql`
3. `src/main/sql/insert_data.sql`

Build using:  `./gradlew build` 

Run using:  `./gradlew -q --console plain run`

## Functionality:
 ### A `Student` can perform the following tasks:
1. Add a course
2. Drop a course
3. View enrolled course details
4. view CGPA
5. Update phone
6. Update address

 ### A `Faculty` can perform the following tasks:
1. Float a course
2. Cancel an offering
3. Upload grades for an offering
4. View offering grades
5. Add prerequisite to an offering
6. Add CGPA cut-off to an offering
7. Update phone
8. Update address

 ### The `Academic Office` can perform the following tasks:
1. Add a course to the catalog
2. Generate a semester transcript for a student
3. View grades of a student
4. View grades of an offering
5. Check graduation eligibility of a student
6. Set a semester event
