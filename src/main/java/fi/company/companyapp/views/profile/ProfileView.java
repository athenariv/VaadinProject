package fi.company.companyapp.views.profile;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.entity.AppUser;
import fi.company.companyapp.security.SecurityService;
import fi.company.companyapp.service.AppUserService;
import fi.company.companyapp.views.components.QuillEditorComponent;
import javax.annotation.security.RolesAllowed;

import java.io.*;
import java.nio.file.*;

@Route(value = "profile", layout = fi.company.companyapp.views.MainLayout.class)
@PageTitle("Profile | CompanyApp")
@RolesAllowed({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
public class ProfileView extends VerticalLayout {

    public ProfileView(SecurityService securityService, AppUserService appUserService) {
        addClassName("profile-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        String username = securityService.getAuthenticatedUsername();
        AppUser user = username != null ?
            appUserService.findByUsername(username).orElse(null) : null;

        if (user == null) {
            add(new Paragraph("Not logged in"));
            return;
        }

        Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.MEDIUM
        );
        card.getStyle().set("max-width", "600px").set("width", "100%");

        H2 title = new H2("My Profile");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        // Profile info
        Paragraph nameInfo = new Paragraph("Name: " + user.getFirstName() + " " + user.getLastName());
        Paragraph emailInfo = new Paragraph("Email: " + user.getEmail());
        Paragraph rolesInfo = new Paragraph("Roles: " + String.join(", ", user.getRoles()));

        nameInfo.addClassNames(LumoUtility.FontSize.MEDIUM);
        emailInfo.addClassNames(LumoUtility.FontSize.MEDIUM);
        rolesInfo.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.SECONDARY);

        // Profile image upload
        H3 imageTitle = new H3("Profile Image");
        imageTitle.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        FileBuffer buffer = new FileBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.addSucceededListener(e -> {
            try {
                Path uploadDir = Paths.get("uploads/profiles");
                Files.createDirectories(uploadDir);
                String filename = user.getId() + "_" + e.getFileName();
                Path targetPath = uploadDir.resolve(filename);
                Files.copy(buffer.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                user.setProfileImagePath(targetPath.toString());
                appUserService.save(user);
                Notification.show("Profile image updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IOException ex) {
                Notification.show("Failed to upload image: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Change password
        H3 passwordTitle = new H3("Change Password");
        passwordTitle.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        PasswordField oldPassword = new PasswordField("Current Password");
        oldPassword.setWidthFull();
        PasswordField newPassword = new PasswordField("New Password");
        newPassword.setWidthFull();
        PasswordField confirmPassword = new PasswordField("Confirm New Password");
        confirmPassword.setWidthFull();

        Button changePasswordBtn = new Button("Change Password", e -> {
            if (!newPassword.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwords do not match").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (appUserService.changePassword(username, oldPassword.getValue(), newPassword.getValue())) {
                Notification.show("Password changed successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                oldPassword.clear();
                newPassword.clear();
                confirmPassword.clear();
            } else {
                Notification.show("Current password is incorrect").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        card.add(title, nameInfo, emailInfo, rolesInfo,
                 imageTitle, upload,
                 passwordTitle, oldPassword, newPassword, confirmPassword, changePasswordBtn);

        // Rich text notes section using Quill.js web component
        H3 notesTitle = new H3("Personal Notes");
        notesTitle.addClassNames(LumoUtility.Margin.Top.LARGE);
        Paragraph notesDesc = new Paragraph("Write rich-text notes visible only to you (uses Quill.js external component).");
        notesDesc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        QuillEditorComponent quillEditor = new QuillEditorComponent("Write your notes here...");
        quillEditor.setWidth("100%");
        quillEditor.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Button saveNotesBtn = new Button("Save Notes", e -> {
            Notification.show("Notes saved (demo — not persisted to DB)").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveNotesBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Div notesCard = new Div(notesTitle, notesDesc, quillEditor, saveNotesBtn);
        notesCard.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.SMALL
        );
        notesCard.getStyle().set("max-width", "600px").set("width", "100%");

        add(card, notesCard);
    }
}
