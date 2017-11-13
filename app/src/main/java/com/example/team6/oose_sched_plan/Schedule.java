//TODO: Add getSemsters() for viewing entire schedule
package com.example.team6.oose_sched_plan;

import android.content.Context;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class Schedule {
	public Schedule() {
		semesters = new ArrayList<>();
	}

	//------------------------------------------------------------------------------
	//------------------------------ PUBLIC INTERFACE ------------------------------
	//------------------------------------------------------------------------------

	//ADD COURSE
		public void addCourse(Term term, int year, Course course) {
			Semester s = getSemester(term, year);

			if (s.year == -1) { //if semester doesn't exist, add semester then try again
				addSemester(term, year);
				addCourse(term, year, course);
			} else {
				s.add(course);
			}
		}

	//GET SEMESTERS
		public ArrayList<Semester> getSemesters() {return semesters;}


	//REMOVE COURSE
		public void removeCourse(Term term, int year, Course course) {
			Semester s = getSemester(term, year);

			if (s.year != -1) { //only remove course from semester if semester exists
				s.remove(course);
			}
		}

	//GET LIST OF AVAILABLE COURSES FOR SEMESTER
		/*
		 * Returns list of courses that are valid for given semester. Currently code in function calls assumes given semester is current semester (which it will be for semester-by-semester)
		 * but will updated soon so that it is more versatile.
		 */
		public ArrayList<Course> generateAvailableCourses(Term term, int year, DegreePlanAdapter.FeedReaderDbHelper mDbHelper) {
			//Get courses offered in specified semester
			ArrayList<Course> validCourses = Database.queryCoursesInTerm(term, year, mDbHelper); //TODO: Change to queryAllCourses

			//Loop through courses and remove invalid courses
			for(int i = 0; i < validCourses.size(); i++) {
				Course currentCourse = validCourses.get(i);

				//Remove courses that have already been taken
				if(this.contains(currentCourse)) {
					validCourses.remove(currentCourse);
					i--;
					continue; //already removed so don't need to check any other conditions for removal
				}
				
				/* -----DONT DO THIS FOR THE TIME BEING-----
				//Remove courses that don't have requisites met
				//if reqsuities toggle is on
				ReqQueryResult requisiteGroups = Database.queryReqs(currentCourse, mDbHelper);//List of list of courses; Outer list is list of requisite credits for curse, each inner list is course options that meet each requisite credit
				if(requisiteGroups.flag == 4000) {
					//if professional toggle on
					//check for professional program met
				} else { //DISCUSSION: only other options is hours requirement?
					if(this.calculateCreditHours() < requisiteGroups.flag) {
						validCourses.remove(currentCourse);
						i--;
						continue;
					}
				}

				boolean requisitesMet = true; //assume true till proven false
				for(ArrayList<ReqCourseEntry> requisiteGroup : requisiteGroups.reqs) {
					boolean requisiteGroupMet = false;

					//loop through requisite options in group and check if schedule contains any of them
					for(ReqCourseEntry requisite : requisiteGroup) {
						if(requisite.coreq) { //requisite is a coreq
							if(this.meetsCoreq(requisite.course, term, year)) {
								requisiteGroupMet = true;
							}
						} else { //requisite is a prereq
							if(this.meetsPrereq(requisite.course, term, year)) {
								requisiteGroupMet = true;
							}
						}
					}

					if(!requisiteGroupMet) {
						requisitesMet = false;
						break;
					}
				}

				if(!requisitesMet) {
					validCourses.remove(currentCourse); //not all reqs met for course
					i--;
					continue;
				}

				//Remove course if it is an elective and have already met credits for that elective (such as already having 3 technical electives) (also if toggle is on)
				//use crouse.creditCategory
				//Database.queryNumberOfElectiveCourses(currentCourse, major, mDbHelper);
				//this.hasMetElectiveHours(int hours)
				//remove if above function returns false
				*/
			}

			return validCourses; //return list of courses that have passed all checks for validity and not been removed from list
		}

	//GET COURSES THAT ARE ADDED TO A CERTAIN SEMESTER
		public ArrayList<Course> getCoursesInSemester(Term term, int year) {
			Semester s = getSemester(term, year);

			if (s.year != -1) { //oif semester in schedule, return list of its courses
				return s.getCourses();
			}

			//if semester not in schedule, return empty list
			else return new ArrayList<Course>();
		}

	// SAVE TO FILE
	//TODO: Save Major and Settings.
		public void Save(Context context, String filename) {
			try {
				File file = new File(context.getFilesDir(), filename);
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

				//-----Write Data-----
				for(Semester s : semesters) {
					for(Course c : s.getCourses()) {
						bufferedWriter.write(s.toString() + "," + c.toString());
						bufferedWriter.newLine();
					}
				}

				bufferedWriter.close();
				fileWriter.close();

			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

	//LOAD FROM FILE
		public void Load(Context context, String filename, DegreePlanAdapter.FeedReaderDbHelper mDbHelper) {
			try {
				File file = new File(context.getFilesDir(), filename);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader((fileReader));

				//-----Read Data-----
				//TODO: Read major
				String line;
				while((line = bufferedReader.readLine()) != null) {
					String[] tokens = line.split(",");
					Term term = Semester.parseTerm(tokens[0]);
					int year = Semester.parseYear(tokens[0]);
					Department courseDepartment = Course.parseDepartment(tokens[1]);
					int courseNumber = Course.parseNumber(tokens[1]);

					Course tempCourse = Database.queryCourse(courseDepartment, courseNumber, mDbHelper);
					this.addCourse(term, year, tempCourse);
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

	//For use in tester which doesn't have context
		public void Save(String filename) {
			try {
				File file = new File(filename);
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

				//-----Write Data-----

				//TODO: Save major (will be stored in semester)
				for(Semester s : semesters) {
					for(Course c : s.getCourses()) {
						bufferedWriter.write(s.toString() + "," + c.toString());
						bufferedWriter.newLine();
					}
				}

				bufferedWriter.close();
				fileWriter.close();

			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

	//For use in tester doesn't have context or mdbhelper
		public void Load(String filename) {
		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader((fileReader));

			//-----Read Data-----
			//TODO: Read major
			String line;
			while((line = bufferedReader.readLine()) != null) {
				String[] tokens = line.split(",");
				Term term = Semester.parseTerm(tokens[0]);
				int year = Semester.parseYear(tokens[0]);
				Department courseDepartment = Course.parseDepartment(tokens[1]);
				int courseNumber = Course.parseNumber(tokens[1]);

				this.addCourse(term, year, new Course(courseDepartment, courseNumber, "name", "desc", CreditCategory.Required));
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	//MISCELLANEOUS FUNCTIONS THAT MIGHT BE NEEDED
		public int calculateCreditHours() {
			int creditHours = 0;
			for(Semester currentSemester : semesters) {
				creditHours += currentSemester.calculateCreditHours();
			}

			return creditHours;
		}

		//Using in Tester
		public String toString() {
			String result = "";
			for(Semester s : semesters) {
				for(Course c : s.getCourses()) {
					result += s.toString() + "," + c.toString() + "\n";
				}
			}

			return result;
		}

		/*
		* DISCUSSION: how to get access to degree plan to get total credit hours b/c right now student class contains degreePlan and Schedule,
		* so I guess student would be public interface for GUI and student could pass it.
		* If remove student since assume only one student will use this app since it is on phone, have degreeplan as member of schedule and then has direct access.
		* Also, just occurred that Major is better name for DegreePlan?
		*/
		public int calculateRemainingCreditHours(DegreePlan major) {
			return (major.totalCreditHours - this.calculateCreditHours());
		}

		public boolean isValid() {
			//Does have all required courses
			//For all technical electives, does have required number of classes
			//Do all preqreqs, coreqs come before class they are supposed to (this will be true for building semester by semester)

			return false; //REMOVE: just here to remove warning that no value returned
		}

		//Methods on back burner for Iteration 3
		public void removeConflicts() {}





	//---------------------------------------------------------------------
	//------------------------------ PRIVATE ------------------------------
	//---------------------------------------------------------------------
	private ArrayList<Semester> semesters;

	private void addSemester(Term term, int year) {
		semesters.add(new Semester(term, year));
	}

	//returns -1 for semester year if semester not in schedule
	private Semester getSemester(Term term, int year) {
		for(Semester s : semesters) {
			if(s.term == term && s.year == year) {
				return s;
			}
		}
		
		//if semester not in schedule, return error semester
		return new Semester(Term.Fall, -1);
	}
	
	public boolean contains(Course course) {
		boolean containsCourse = false;
		for(Semester currentSemester : semesters) {
			if(currentSemester.contains(course)) {
				containsCourse = true;
				break;
			}
		}
		
		return containsCourse;
	}
	
	//TODO: Adjust for if not doing semester by semester building (get passed term and year and only check up to semester before it)
	private boolean meetsPrereq(Course prereq, Term term, int year) {
		Semester endMarker = new Semester(term, year); //used to tell when to stop searching schedule
		boolean containsPrereq = false;
		//loop through all semesters  except current one since courses in current semester can't satisfy prereq conditions
		for(Semester s : semesters) {
			if(s.isBefore(endMarker)) {
				if (s.contains(prereq)) {
					containsPrereq = true;
					break; //found prereq so stop searching for it in schedule
				}
			} else {
				break; //reached end semester so exit
			}
		}
		
		return containsPrereq;
	}
	
	//TODO: Adjust for if not doing semester by semester building (get passed term and year and only check up to that semester)
	private boolean meetsCoreq(Course coreq, Term term, int year) {
		Semester endMarker = new Semester(term, year); //used to tell when to stop searching schedule
		boolean containsCoreq = false;
		//loop through all semesters  except current one since courses in current semester can't satisfy prereq conditions
		for(Semester s : semesters) {
			if(s.isBefore(endMarker) || s.equals(endMarker)) {
				if (s.contains(coreq)) {
					containsCoreq = true;
					break; //found prereq so stop searching for it in schedule
				}
			} else {
				break; //reached end semester so exit
			}
		}

		return containsCoreq;
	}
}
