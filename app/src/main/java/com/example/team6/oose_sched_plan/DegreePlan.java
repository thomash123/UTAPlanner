//DISCUSSION: Just move major to schedule and remove this class?
package com.example.team6.oose_sched_plan;

public class DegreePlan {
	public DegreePlan(String major, int startingYear, int totalCreditHours) {
		this.major = major;
		this.startingYear = startingYear;
		this.totalCreditHours = totalCreditHours;
	}

	public String major; //DISCUSSION: Can this be the enum department or will that not always work? Maybe not since Software Engineering isn't a department
	public int startingYear; //DISCUSSION: As mentioned earlier, could we replace this by just getting first semester year?
	public int totalCreditHours;

	public String toString() {
		return major+","+ String.valueOf(startingYear) + "," + String.valueOf(totalCreditHours);
	}
}
