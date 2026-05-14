package fi.company.companyapp.views.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.*;
import fi.company.companyapp.service.*;
import javax.annotation.security.RolesAllowed;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Route(value = "admin", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Admin | CompanyApp")
@RolesAllowed("ROLE_ADMIN")
public class AdminView extends VerticalLayout {

    private final AppUserService appUserService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final Grid<AppUser> userGrid = new Grid<>(AppUser.class, false);

    public AdminView(AppUserService appUserService, EmployeeService employeeService, DepartmentService departmentService) {
        this.appUserService = appUserService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        addClassName("admin-view");
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Admin Panel");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        add(title, createUserManagement(), createExportSection(), createImportSection());
    }

    private Component createUserManagement() {
        H3 sectionTitle = new H3("User Management");
        sectionTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.SMALL);

        configureUserGrid();
        refreshUserGrid();

        Div section = new Div(sectionTitle, userGrid);
        section.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM,
                              LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);
        section.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        section.setWidthFull();
        section.setMaxWidth("70vw");
        userGrid.setWidthFull();
        userGrid.setHeight("300px");
        return section;
    }

    private void configureUserGrid() {
        userGrid.addColumn(AppUser::getUsername).setHeader("Username").setSortable(true);
        userGrid.addColumn(AppUser::getEmail).setHeader("Email").setAutoWidth(true);
        userGrid.addColumn(u -> String.join(", ", u.getRoles())).setHeader("Roles");
        userGrid.addColumn(AppUser::isEnabled).setHeader("Enabled");

        userGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.setPadding(false);
            actions.getStyle().set("flex-shrink", "0");
            Button toggleBtn = new Button(user.isEnabled() ? "Disable" : "Enable", e -> {
                user.setEnabled(!user.isEnabled());
                appUserService.save(user);
                refreshUserGrid();
            });
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            Button deleteBtn = new Button("Delete", e -> {
                appUserService.delete(user);
                refreshUserGrid();
                Notification.show("User deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            actions.add(toggleBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(1);
    }

    private Component createExportSection() {
        H3 sectionTitle = new H3("Export Employees");
        sectionTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.SMALL);

        Anchor exportLink = new Anchor();
        exportLink.getElement().setAttribute("download", true);
        Button exportBtn = new Button("Export to Excel");
        exportBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportBtn.addClickListener(e -> {
            StreamResource resource = new StreamResource("employees.xlsx", () -> {
                try {
                    return generateExcel();
                } catch (Exception ex) {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });
            exportLink.setHref(resource);
            exportLink.getElement().callJsFunction("click");
        });

        exportLink.add(exportBtn);

        Paragraph desc = new Paragraph("Download all employees as Excel file");
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Div section = new Div(sectionTitle, desc, exportLink);
        section.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM,
                              LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);
        section.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        return section;
    }

    private InputStream generateExcel() throws Exception {
        List<Employee> employees = employeeService.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Email", "Job Title", "Department", "Hire Date", "Salary"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emp.getId());
                row.createCell(1).setCellValue(emp.getFirstName());
                row.createCell(2).setCellValue(emp.getLastName());
                row.createCell(3).setCellValue(emp.getEmail());
                row.createCell(4).setCellValue(emp.getJobTitle());
                row.createCell(5).setCellValue(emp.getDepartment() != null ? emp.getDepartment().getName() : "");
                row.createCell(6).setCellValue(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
                row.createCell(7).setCellValue(emp.getSalary());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private Component createImportSection() {
        H3 sectionTitle = new H3("Import CSV Data");
        sectionTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.SMALL);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv", "text/csv");
        upload.setMaxFileSize(5 * 1024 * 1024);

        upload.addSucceededListener(e -> {
            int imported = 0;
            int skipped = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(buffer.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // skip header row
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",", -1);
                    if (parts.length < 5) { skipped++; continue; }
                    try {
                        Employee emp = new Employee();
                        emp.setFirstName(parts[0].trim());
                        emp.setLastName(parts[1].trim());
                        emp.setEmail(parts[2].trim());
                        emp.setJobTitle(parts[3].trim());
                        emp.setSalary(Double.parseDouble(parts[4].trim()));
                        emp.setHireDate(LocalDate.now());
                        if (parts.length >= 6 && !parts[5].isBlank()) {
                            departmentService.findByName(parts[5].trim())
                                .ifPresent(emp::setDepartment);
                        }
                        employeeService.save(emp);
                        imported++;
                    } catch (Exception ex) {
                        skipped++;
                    }
                }
                final int finalImported = imported;
                final int finalSkipped = skipped;
                Notification.show("Imported " + finalImported + " employees, skipped " + finalSkipped + " invalid rows")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IOException ex) {
                Notification.show("Failed to read CSV: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Paragraph desc = new Paragraph("Upload a CSV file to import employees. " +
            "Format: firstName,lastName,email,jobTitle,salary[,departmentName]");
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Div section = new Div(sectionTitle, desc, upload);
        section.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM,
                              LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);
        return section;
    }

    private void refreshUserGrid() {
        userGrid.setItems(appUserService.findAll());
    }
}
