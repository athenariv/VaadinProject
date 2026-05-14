package fi.company.companyapp.views.search;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.*;
import fi.company.companyapp.service.*;
import javax.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "search", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Advanced Search | CompanyApp")
@RolesAllowed({"ROLE_SUPER", "ROLE_ADMIN"})
public class SearchView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final Grid<Employee> resultGrid = new Grid<>(Employee.class, false);

    public SearchView(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        addClassName("search-view");
        setSizeFull();
        setPadding(true);

        add(createHeader(), createSearchForm(), createResultGrid());
    }

    private Component createHeader() {
        H2 title = new H2("Advanced Employee Search");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.SMALL);
        Paragraph desc = new Paragraph("Search using Criteria API with dynamic predicates, LIKE, date range, JOIN, and OR conditions.");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.MEDIUM);
        return new VerticalLayout(title, desc);
    }

    private Component createSearchForm() {
        // Search filters
        TextField nameOrEmailField = new TextField("Name or Email (OR condition)");
        nameOrEmailField.setPlaceholder("Search name OR email...");
        nameOrEmailField.setWidthFull();
        nameOrEmailField.addClassName("search-field");

        TextField jobTitleField = new TextField("Job Title (LIKE)");
        jobTitleField.setPlaceholder("Partial job title...");
        jobTitleField.setWidthFull();

        ComboBox<Department> departmentCombo = new ComboBox<>("Department (JOIN filter)");
        departmentCombo.setItems(departmentService.findAll());
        departmentCombo.setItemLabelGenerator(Department::getName);
        departmentCombo.setClearButtonVisible(true);
        departmentCombo.setWidthFull();

        DatePicker hireDateFrom = new DatePicker("Hire Date From");
        DatePicker hireDateTo = new DatePicker("Hire Date To");

        NumberField minSalary = new NumberField("Min Salary");
        minSalary.setPlaceholder("e.g. 50000");
        NumberField maxSalary = new NumberField("Max Salary");
        maxSalary.setPlaceholder("e.g. 100000");

        // Complex search (OR AND)
        TextField complexNameEmail = new TextField("Name or Email (complex OR)");
        complexNameEmail.setPlaceholder("(firstName OR email) LIKE...");
        complexNameEmail.setWidthFull();

        TextField complexDeptName = new TextField("Department Name (AND)");
        complexDeptName.setPlaceholder("Department name contains...");
        complexDeptName.setWidthFull();

        Button searchBtn = new Button("Search", e -> {
            List<Employee> results = employeeService.advancedSearch(
                nameOrEmailField.getValue(),
                departmentCombo.getValue() != null ? departmentCombo.getValue().getId() : null,
                hireDateFrom.getValue(),
                hireDateTo.getValue(),
                minSalary.getValue(),
                maxSalary.getValue(),
                jobTitleField.getValue()
            );
            resultGrid.setItems(results);
        });
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClassName("search-button");

        Button complexSearchBtn = new Button("Complex Search ((X OR Y) AND Z)", e -> {
            List<Employee> results = employeeService.complexSearch(
                complexNameEmail.getValue(),
                complexDeptName.getValue()
            );
            resultGrid.setItems(results);
        });
        complexSearchBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button clearBtn = new Button("Clear", e -> {
            nameOrEmailField.clear();
            jobTitleField.clear();
            departmentCombo.clear();
            hireDateFrom.clear();
            hireDateTo.clear();
            minSalary.clear();
            maxSalary.clear();
            complexNameEmail.clear();
            complexDeptName.clear();
            resultGrid.setItems(List.of());
        });
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Layout
        Div basicSearchPanel = new Div();
        basicSearchPanel.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.BoxShadow.SMALL
        );
        basicSearchPanel.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H3 basicTitle = new H3("Basic Criteria Search");
        basicTitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.SMALL);

        Div basicGrid = new Div(nameOrEmailField, jobTitleField, departmentCombo,
                                hireDateFrom, hireDateTo, minSalary, maxSalary);
        basicGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "var(--lumo-space-m)");

        HorizontalLayout basicButtons = new HorizontalLayout(searchBtn, clearBtn);

        basicSearchPanel.add(basicTitle, basicGrid, basicButtons);

        // Complex search panel
        Div complexPanel = new Div();
        complexPanel.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.BoxShadow.SMALL
        );

        H3 complexTitle = new H3("Complex Search: (firstName OR email) AND department");
        complexTitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.SMALL);

        Div complexGrid = new Div(complexNameEmail, complexDeptName);
        complexGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "var(--lumo-space-m)");

        complexPanel.add(complexTitle, complexGrid, complexSearchBtn);

        VerticalLayout formLayout = new VerticalLayout(basicSearchPanel, complexPanel);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        return formLayout;
    }

    private Component createResultGrid() {
        resultGrid.addColumn(Employee::getFullName).setHeader("Name").setAutoWidth(true);
        resultGrid.addColumn(Employee::getEmail).setHeader("Email").setAutoWidth(true);
        resultGrid.addColumn(Employee::getJobTitle).setHeader("Job Title");
        resultGrid.addColumn(e -> e.getDepartment() != null ? e.getDepartment().getName() : "-").setHeader("Department");
        resultGrid.addColumn(Employee::getHireDate).setHeader("Hire Date");
        resultGrid.addColumn(e -> String.format("€%.0f", e.getSalary())).setHeader("Salary");
        resultGrid.setSizeFull();
        resultGrid.addClassName("result-grid");

        Div wrapper = new Div(new H3("Results"), resultGrid);
        wrapper.setSizeFull();
        return wrapper;
    }
}
