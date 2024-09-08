package eu.nerdfactor.restness.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.nerdfactor.restness.annotation.IdAccessor;
import eu.nerdfactor.restness.annotation.RelationAccessor;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.example.customer.CustomerDao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Basic entity for Employees.<br> Uses no specific suffix. Dto will be assumed
 * to be called EmployeeDto.
 */
@Entity
@Getter
@Setter
public class Employee {

	@Id
	private int id;

	private String email;

	private String name;

	@OneToOne
	@JsonIgnore
	private CustomerDao client;

	@OneToMany
	@JsonIgnore
	private List<Employee> staff;

	@ManyToOne
	@JsonIgnore
	private Employee manager;

	/**
	 * Uses IdAccessor to provide access to the id. This is necessary, because
	 * the getter for the id is not called getId() but uses a different name.
	 */
	@IdAccessor
	public int getPerNo() {
		return id;
	}

	public void setPerNo(int perNo) {
		this.id = perNo;
	}

	/**
	 * Uses RelationAccessor to provide access to add or remove staff
	 * Employees.<br> The RelationAccessor can take care of multiple types of
	 * access in the same method, if it is possible.
	 */
	@RelationAccessor(name = "staff", type = {AccessorType.ADD, AccessorType.REMOVE})
	public void assignEmployee(Employee employee) {
		if (this.staff.contains(employee)) {
			this.staff.remove(employee);
		} else {
			this.staff.add(employee);
		}
	}

	public void addEmployee(Employee employee) {
		this.staff.add(employee);
	}

	public void removeEmployee(Employee employee) {
		this.staff.remove(employee);
	}

}
