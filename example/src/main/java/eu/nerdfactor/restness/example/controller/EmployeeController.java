package eu.nerdfactor.restness.example.controller;

import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.annotation.RestnessSecurity;
import eu.nerdfactor.restness.example.entity.Employee;

/**
 * Base controller for Employees.<br> Uses RestnessController to configure a
 * generated controller based on the controller. Provides information about the
 * entity and id to use in the generated controller.
 * <br>
 * Uses RestnessSecurity to configure Spring security for the generated
 * controller. Matches automatically to the generated controller. Disables the
 * inclusion of base security. Therefore, the methods only require access to the
 * relation object and not to employee.
 */
@RestnessController(value = "/api/employee", entity = Employee.class, id = Integer.class)
@RestnessSecurity(inclusive = false)
public class EmployeeController {


}
