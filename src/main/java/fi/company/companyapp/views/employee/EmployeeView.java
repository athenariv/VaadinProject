package fi.company.companyapp.views.employee;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.*;
import fi.company.companyapp.service.*;
import javax.annotation.security.RolesAllowed;

@Route(value = "employees", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Employees | CompanyApp")
@RolesAllowed({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
public class EmployeeView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final Grid<Employee> grid = new Grid<>(Employee.class, false);

    public EmployeeView(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        addClassName("employee-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(Employee::getFullName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Employee::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Employee::getJobTitle).setHeader("Job Title").setSortable(true);
        // 1:N relation - show department name
        grid.addColumn(e -> e.getDepartment() != null ? e.getDepartment().getName() : "-")
            .setHeader("Department").setSortable(true);
        grid.addColumn(Employee::getHireDate).setHeader("Hire Date").setSortable(true);
        grid.addColumn(e -> String.format("€%.0f", e.getSalary())).setHeader("Salary");
        // 1:1 relation - show city from detail
        grid.addColumn(e -> e.getDetail() != null ? e.getDetail().getCity() : "-").setHeader("City");
        // M:N relation - show project count
        grid.addColumn(e -> e.getProjects() != null ? e.getProjects().size() + " projects" : "0 projects")
            .setHeader("Projects");

        grid.addComponentColumn(emp -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editBtn = new Button("Edit", e -> openEditDialog(emp));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button deleteBtn = new Button("Delete", e -> {
                employeeService.delete(emp);
                refreshGrid();
                Notification.show("Employee deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions");
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Employees");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Employee", e -> openEditDialog(new Employee()));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(title, addButton);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void openEditDialog(Employee employee) {
        boolean isNew = employee.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Employee" : "Edit Employee");
        dialog.setWidth("700px");

        TabSheet tabSheet = new TabSheet();

        // Basic info form
        BeanValidationBinder<Employee> binder = new BeanValidationBinder<>(Employee.class);

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        EmailField emailField = new EmailField("Email");
        TextField jobTitleField = new TextField("Job Title");
        NumberField salaryField = new NumberField("Salary (€)");
        DatePicker hireDateField = new DatePicker("Hire Date");
        ComboBox<Department> departmentCombo = new ComboBox<>("Department");
        departmentCombo.setItems(departmentService.findAll());
        departmentCombo.setItemLabelGenerator(Department::getName);

        binder.forField(firstNameField).asRequired().withValidator(new StringLengthValidator("2-50 chars", 2, 50))
            .bind(Employee::getFirstName, Employee::setFirstName);
        binder.forField(lastNameField).asRequired().withValidator(new StringLengthValidator("2-50 chars", 2, 50))
            .bind(Employee::getLastName, Employee::setLastName);
        binder.forField(emailField).asRequired().withValidator(new EmailValidator("Invalid email"))
            .bind(Employee::getEmail, Employee::setEmail);
        binder.forField(jobTitleField).asRequired().bind(Employee::getJobTitle, Employee::setJobTitle);
        binder.forField(salaryField).asRequired()
            .withConverter(d -> d, d -> d, "Invalid number")
            .withValidator(s -> s >= 1000 && s <= 500000, "Salary must be 1000-500000")
            .bind(Employee::getSalary, Employee::setSalary);
        binder.forField(hireDateField).asRequired().bind(Employee::getHireDate, Employee::setHireDate);
        binder.forField(departmentCombo).bind(Employee::getDepartment, Employee::setDepartment);
        binder.setBean(employee);

        FormLayout basicForm = new FormLayout(firstNameField, lastNameField, emailField, jobTitleField,
                                              salaryField, hireDateField, departmentCombo);
        basicForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        // Detail form (1:1 relation)
        EmployeeDetail detail = employee.getDetail() != null ? employee.getDetail() : new EmployeeDetail();
        BeanValidationBinder<EmployeeDetail> detailBinder = new BeanValidationBinder<>(EmployeeDetail.class);

        TextField phoneField = new TextField("Phone");
        TextField addressField = new TextField("Address");
        TextField cityField = new TextField("City");
        TextField countryField = new TextField("Country");
        TextField emergencyContactField = new TextField("Emergency Contact");
        TextField emergencyPhoneField = new TextField("Emergency Phone");
        TextArea notesField = new TextArea("Notes");

        detailBinder.forField(phoneField).asRequired().bind(EmployeeDetail::getPhone, EmployeeDetail::setPhone);
        detailBinder.forField(addressField).asRequired().bind(EmployeeDetail::getAddress, EmployeeDetail::setAddress);
        detailBinder.forField(cityField).asRequired().bind(EmployeeDetail::getCity, EmployeeDetail::setCity);
        detailBinder.forField(countryField).asRequired().bind(EmployeeDetail::getCountry, EmployeeDetail::setCountry);
        detailBinder.forField(emergencyContactField).asRequired().bind(EmployeeDetail::getEmergencyContact, EmployeeDetail::setEmergencyContact);
        detailBinder.forField(emergencyPhoneField).asRequired().bind(EmployeeDetail::getEmergencyPhone, EmployeeDetail::setEmergencyPhone);
        detailBinder.forField(notesField).bind(EmployeeDetail::getNotes, EmployeeDetail::setNotes);
        detailBinder.setBean(detail);

        FormLayout detailForm = new FormLayout(phoneField, addressField, cityField, countryField,
                                               emergencyContactField, emergencyPhoneField, notesField);
        detailForm.setColspan(notesField, 2);

        tabSheet.add(new Tab("Basic Info"), basicForm);
        tabSheet.add(new Tab("Details (1:1)"), detailForm);

        Button saveBtn = new Button("Save", e -> {
            boolean basicOk = binder.validate().isOk();
            boolean detailOk = detailBinder.validate().isOk();
            if (basicOk && detailOk) {
                Employee saved = employeeService.save(employee);
                detail.setEmployee(saved);
                saved.setDetail(detail);
                employeeService.save(saved);
                refreshGrid();
                dialog.close();
                Notification.show("Employee saved").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(tabSheet);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(employeeService.findAll());
    }
}
