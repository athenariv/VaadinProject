package fi.company.companyapp.views.department;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.validator.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.Department;
import fi.company.companyapp.service.DepartmentService;
import javax.annotation.security.RolesAllowed;

@Route(value = "departments", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Departments | CompanyApp")
@RolesAllowed({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
public class DepartmentView extends VerticalLayout {

    private final DepartmentService departmentService;
    private final Grid<Department> grid = new Grid<>(Department.class, false);
    private final BeanValidationBinder<Department> binder = new BeanValidationBinder<>(Department.class);

    public DepartmentView(DepartmentService departmentService) {
        this.departmentService = departmentService;
        addClassName("department-view");
        setSizeFull();

        configureGrid();

        HorizontalLayout toolbar = createToolbar();
        add(toolbar, grid);
        setSizeFull();

        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(Department::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Department::getLocation).setHeader("Location").setSortable(true);
        grid.addColumn(Department::getCostCenter).setHeader("Cost Center");
        grid.addColumn(d -> String.format("€%.0f", d.getBudget())).setHeader("Budget");
        grid.addColumn(d -> d.getEmployees() != null ? d.getEmployees().size() : 0).setHeader("Employees");

        grid.addComponentColumn(dept -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editBtn = new Button("Edit", e -> openEditDialog(dept));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            Button deleteBtn = new Button("Delete", e -> {
                departmentService.delete(dept);
                refreshGrid();
                Notification.show("Department deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions");
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Departments");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Department", e -> openEditDialog(new Department()));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName("add-button");

        HorizontalLayout toolbar = new HorizontalLayout(title, addButton);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidthFull();
        toolbar.addClassNames(LumoUtility.Padding.Vertical.SMALL);
        return toolbar;
    }

    private void openEditDialog(Department department) {
        Dialog dialog = new Dialog();
        boolean isNew = department.getId() == null;
        dialog.setHeaderTitle(isNew ? "Add Department" : "Edit Department");

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Name");
        TextArea descField = new TextArea("Description");
        TextField locationField = new TextField("Location");
        NumberField budgetField = new NumberField("Budget (€)");
        TextField costCenterField = new TextField("Cost Center (CC-XXXX)");

        budgetField.setPrefixComponent(new Span("€"));

        binder.forField(nameField)
            .asRequired("Name is required")
            .withValidator(new StringLengthValidator("Name must be 2-100 chars", 2, 100))
            .bind(Department::getName, Department::setName);
        binder.forField(descField)
            .asRequired("Description is required")
            .bind(Department::getDescription, Department::setDescription);
        binder.forField(locationField)
            .asRequired("Location is required")
            .bind(Department::getLocation, Department::setLocation);
        binder.forField(budgetField)
            .asRequired("Budget is required")
            .withConverter(d -> d, d -> d, "Invalid number")
            .bind(Department::getBudget, Department::setBudget);
        binder.forField(costCenterField)
            .asRequired("Cost center is required")
            .withValidator(v -> v.matches("CC-\\d{4}"), "Must match CC-XXXX format")
            .bind(Department::getCostCenter, Department::setCostCenter);

        binder.setBean(department);

        form.add(nameField, locationField, costCenterField, budgetField, descField);
        form.setColspan(descField, 2);

        Button saveBtn = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                departmentService.save(department);
                refreshGrid();
                dialog.close();
                Notification.show("Department saved").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.setWidth("600px");
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(departmentService.findAll());
    }
}
