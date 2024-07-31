package com.example.model;
package com.example.controller;

import com.example.model.Employee;
import com.example.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee) {
        try {
            Employee savedEmployee = employeeService.saveEmployee(employee);
            return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{employeeId}/tax-deductions")
    public ResponseEntity<?> getTaxDeductions(@PathVariable String employeeId) {
        Optional<Employee> optionalEmployee = employeeService.getEmployeeById(employeeId);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            double yearlySalary = calculateYearlySalary(employee);
            double taxAmount = calculateTax(yearlySalary);
            double cessAmount = calculateCess(yearlySalary);

            return new ResponseEntity<>(new TaxDeductionResponse(employeeId, employee.getFirstName(), employee.getLastName(), yearlySalary, taxAmount, cessAmount), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND);
        }
    }

    private double calculateYearlySalary(Employee employee) {
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(employee.getDoj(), currentDate);
        int monthsWorked = period.getMonths() + (period.getYears() * 12);
        return employee.getSalary() * monthsWorked;
    }

    private double calculateTax(double yearlySalary) {
        if (yearlySalary <= 250000) {
            return 0;
        } else if (yearlySalary <= 500000) {
            return (yearlySalary - 250000) * 0.05;
        } else if (yearlySalary <= 1000000) {
            return 12500 + (yearlySalary - 500000) * 0.10;
        } else {
            return 62500 + (yearlySalary - 1000000) * 0.20;
        }
    }

    private double calculateCess(double yearlySalary) {
        if (yearlySalary > 2500000) {
            return (yearlySalary - 2500000) * 0.02;
        }
        return 0;
    }

    private static class TaxDeductionResponse {
        private String employeeId;
        private String firstName;
        private String lastName;
        private double yearlySalary;
        private double taxAmount;
        private double cessAmount;

        // Constructor, Getters and Setters
    }
}
