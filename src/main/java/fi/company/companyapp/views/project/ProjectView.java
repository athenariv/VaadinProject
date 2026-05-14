package fi.company.companyapp.views.project;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.*;
import fi.company.companyapp.service.*;
import javax.annotation.security.RolesAllowed;

import java.util.HashSet;

@Route(value = "projects", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Projects | CompanyApp")
@RolesAllowed({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
public class ProjectView extends VerticalLayout {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final Grid<Project> grid = new Grid<>(Project.class, false);

    public ProjectView(ProjectService projectService, EmployeeService employeeService, TaskService taskService) {
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.taskService = taskService;
        addClassName("project-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(Project::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Project::getStatus).setHeader("Status").setSortable(true);
        grid.addColumn(Project::getStartDate).setHeader("Start Date");
        grid.addColumn(Project::getEndDate).setHeader("End Date");
        grid.addColumn(p -> String.format("€%.0f", p.getBudget())).setHeader("Budget");
        // M:N relation - show employees
        grid.addColumn(p -> p.getEmployees() != null ?
            p.getEmployees().stream().map(Employee::getFullName).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)
            : "-").setHeader("Employees").setAutoWidth(true);
        // 1:N relation - show task count
        grid.addColumn(p -> p.getTasks() != null ? p.getTasks().size() + " tasks" : "0 tasks").setHeader("Tasks");

        grid.addComponentColumn(proj -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editBtn = new Button("Edit", e -> openEditDialog(proj));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button tasksBtn = new Button("Tasks", e -> openTasksDialog(proj));
            tasksBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button deleteBtn = new Button("Delete", e -> {
                projectService.delete(proj);
                refreshGrid();
                Notification.show("Project deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            actions.add(editBtn, tasksBtn, deleteBtn);
            return actions;
        }).setHeader("Actions");
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Projects");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Project", e -> openEditDialog(new Project()));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(title, addButton);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void openEditDialog(Project project) {
        boolean isNew = project.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Project" : "Edit Project");
        dialog.setWidth("700px");

        BeanValidationBinder<Project> binder = new BeanValidationBinder<>(Project.class);

        TextField nameField = new TextField("Project Name");
        TextArea descField = new TextArea("Description");
        DatePicker startDateField = new DatePicker("Start Date");
        DatePicker endDateField = new DatePicker("End Date");
        NumberField budgetField = new NumberField("Budget (€)");
        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems("PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "CANCELLED");

        // M:N relation - select employees
        MultiSelectComboBox<Employee> employeeMultiSelect = new MultiSelectComboBox<>("Employees (M:N)");
        employeeMultiSelect.setItems(employeeService.findAll());
        employeeMultiSelect.setItemLabelGenerator(Employee::getFullName);
        if (project.getEmployees() != null) {
            employeeMultiSelect.setValue(project.getEmployees());
        }

        binder.forField(nameField).asRequired().withValidator(new StringLengthValidator("2-150 chars", 2, 150))
            .bind(Project::getName, Project::setName);
        binder.forField(descField).asRequired().bind(Project::getDescription, Project::setDescription);
        binder.forField(startDateField).asRequired().bind(Project::getStartDate, Project::setStartDate);
        binder.forField(endDateField).asRequired().bind(Project::getEndDate, Project::setEndDate);
        binder.forField(budgetField).asRequired()
            .withConverter(d -> d, d -> d, "Invalid number")
            .bind(Project::getBudget, Project::setBudget);
        binder.forField(statusSelect).asRequired().bind(Project::getStatus, Project::setStatus);
        binder.setBean(project);

        FormLayout form = new FormLayout(nameField, statusSelect, startDateField, endDateField,
                                         budgetField, employeeMultiSelect, descField);
        form.setColspan(descField, 2);
        form.setColspan(employeeMultiSelect, 2);

        Button saveBtn = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                project.setEmployees(new HashSet<>(employeeMultiSelect.getValue()));
                projectService.save(project);
                // Update employee projects
                employeeMultiSelect.getValue().forEach(emp -> {
                    if (emp.getProjects() == null) emp.setProjects(new HashSet<>());
                    emp.getProjects().add(project);
                    employeeService.save(emp);
                });
                refreshGrid();
                dialog.close();
                Notification.show("Project saved").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openTasksDialog(Project project) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tasks: " + project.getName());
        dialog.setWidth("800px");

        Grid<Task> taskGrid = new Grid<>(Task.class, false);
        taskGrid.addColumn(Task::getTitle).setHeader("Title").setAutoWidth(true);
        taskGrid.addColumn(Task::getPriority).setHeader("Priority");
        taskGrid.addColumn(Task::getStatus).setHeader("Status");
        taskGrid.addColumn(Task::getDueDate).setHeader("Due Date");
        taskGrid.addColumn(t -> t.getCompletionPercentage() + "%").setHeader("Progress");

        taskGrid.addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editBtn = new Button("Edit", e -> openTaskDialog(task, project, taskGrid));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button deleteBtn = new Button("Delete", e -> {
                taskService.delete(task);
                taskGrid.setItems(taskService.findByProjectId(project.getId()));
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions");

        taskGrid.setItems(taskService.findByProjectId(project.getId()));

        Button addTaskBtn = new Button("Add Task", e -> {
            Task newTask = new Task();
            newTask.setProject(project);
            openTaskDialog(newTask, project, taskGrid);
        });
        addTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        VerticalLayout content = new VerticalLayout(addTaskBtn, taskGrid);
        content.setSizeFull();
        dialog.add(content);
        dialog.setHeight("600px");

        Button closeBtn = new Button("Close", e -> {
            refreshGrid();
            dialog.close();
        });
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private void openTaskDialog(Task task, Project project, Grid<Task> taskGrid) {
        Dialog taskDialog = new Dialog();
        taskDialog.setHeaderTitle(task.getId() == null ? "Add Task" : "Edit Task");
        taskDialog.setWidth("600px");

        BeanValidationBinder<Task> binder = new BeanValidationBinder<>(Task.class);

        TextField titleField = new TextField("Title");
        TextArea descField = new TextArea("Description");
        DatePicker dueDateField = new DatePicker("Due Date");
        Select<String> prioritySelect = new Select<>();
        prioritySelect.setLabel("Priority");
        prioritySelect.setItems("LOW", "MEDIUM", "HIGH", "CRITICAL");
        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems("TODO", "IN_PROGRESS", "REVIEW", "DONE");
        IntegerField completionField = new IntegerField("Completion %");
        completionField.setMin(0);
        completionField.setMax(100);
        NumberField hoursField = new NumberField("Estimated Hours");

        binder.forField(titleField).asRequired().bind(Task::getTitle, Task::setTitle);
        binder.forField(descField).asRequired().bind(Task::getDescription, Task::setDescription);
        binder.forField(dueDateField).asRequired().bind(Task::getDueDate, Task::setDueDate);
        binder.forField(prioritySelect).asRequired().bind(Task::getPriority, Task::setPriority);
        binder.forField(statusSelect).asRequired().bind(Task::getStatus, Task::setStatus);
        binder.forField(completionField).bind(Task::getCompletionPercentage, Task::setCompletionPercentage);
        binder.forField(hoursField).asRequired()
            .withConverter(d -> d, d -> d, "Invalid")
            .bind(Task::getEstimatedHours, Task::setEstimatedHours);
        binder.setBean(task);

        FormLayout form = new FormLayout(titleField, prioritySelect, statusSelect, dueDateField,
                                         completionField, hoursField, descField);
        form.setColspan(descField, 2);

        Button saveBtn = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                task.setProject(project);
                taskService.save(task);
                taskGrid.setItems(taskService.findByProjectId(project.getId()));
                taskDialog.close();
                Notification.show("Task saved").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelBtn = new Button("Cancel", e -> taskDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        taskDialog.add(form);
        taskDialog.getFooter().add(cancelBtn, saveBtn);
        taskDialog.open();
    }

    private void refreshGrid() {
        grid.setItems(projectService.findAll());
    }
}
