package fi.company.companyapp.views.history;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.Employee;
import fi.company.companyapp.service.EmployeeService;
import javax.annotation.security.RolesAllowed;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;

import java.util.ArrayList;
import java.util.List;

@Route(value = "history", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("History | CompanyApp")
@RolesAllowed({"ROLE_SUPER", "ROLE_ADMIN"})
public class HistoryView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final Grid<RevisionInfo> historyGrid = new Grid<>(RevisionInfo.class, false);

    public HistoryView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        addClassName("history-view");
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Entity Change History");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.SMALL);

        Paragraph desc = new Paragraph("View the history of all changes made to employees (powered by Hibernate Envers).");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.MEDIUM);

        ComboBox<Employee> employeeCombo = new ComboBox<>("Select Employee");
        employeeCombo.setItems(employeeService.findAll());
        employeeCombo.setItemLabelGenerator(Employee::getFullName);
        employeeCombo.setWidthFull();
        employeeCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadHistory(e.getValue().getId());
            }
        });

        configureGrid();

        add(title, desc, employeeCombo, createTimeline(), historyGrid);
    }

    private void configureGrid() {
        historyGrid.addColumn(RevisionInfo::getRevisionNumber).setHeader("Revision #").setSortable(true);
        historyGrid.addColumn(RevisionInfo::getRevisionType).setHeader("Change Type");
        historyGrid.addColumn(RevisionInfo::getEmployeeName).setHeader("Employee Name");
        historyGrid.addColumn(RevisionInfo::getEmail).setHeader("Email");
        historyGrid.addColumn(RevisionInfo::getDepartment).setHeader("Department");
        historyGrid.addColumn(RevisionInfo::getTimestamp).setHeader("When");
        historyGrid.setSizeFull();
        historyGrid.addClassName("history-grid");
    }

    private Component createTimeline() {
        Div timelineDesc = new Div();
        timelineDesc.addClassNames(
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        timelineDesc.getStyle().set("background", "var(--lumo-contrast-5pct)");
        Span timelineLabel = new Span("Select an employee above to view their change history. Each row represents a point in time when the entity was modified.");
        timelineLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        timelineDesc.add(timelineLabel);
        return timelineDesc;
    }

    private void loadHistory(Long employeeId) {
        try {
            Revisions<Integer, Employee> revisions = employeeService.findRevisions(employeeId);
            List<RevisionInfo> infos = new ArrayList<>();
            for (Revision<Integer, Employee> rev : revisions) {
                Employee emp = rev.getEntity();
                RevisionInfo info = new RevisionInfo();
                info.setRevisionNumber(rev.getRevisionNumber().orElse(0));
                info.setRevisionType(rev.getMetadata().getRevisionType().name());
                info.setEmployeeName(emp.getFullName());
                info.setEmail(emp.getEmail());
                info.setDepartment(emp.getDepartment() != null ? emp.getDepartment().getName() : "-");
                info.setTimestamp(rev.getRevisionInstant().map(Object::toString).orElse("-"));
                infos.add(info);
            }
            historyGrid.setItems(infos);
        } catch (Exception e) {
            historyGrid.setItems(List.of());
        }
    }

    // Simple DTO for displaying revision info
    public static class RevisionInfo {
        private int revisionNumber;
        private String revisionType;
        private String employeeName;
        private String email;
        private String department;
        private String timestamp;

        public int getRevisionNumber() { return revisionNumber; }
        public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }
        public String getRevisionType() { return revisionType; }
        public void setRevisionType(String revisionType) { this.revisionType = revisionType; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
