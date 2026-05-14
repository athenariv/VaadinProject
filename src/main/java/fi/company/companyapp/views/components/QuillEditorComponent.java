package fi.company.companyapp.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * Java wrapper for the Quill.js rich text editor web component.
 * Uses a custom LitElement (<quill-editor>) defined in quill-editor.js.
 */
@Tag("quill-editor")
@JsModule("./quill-editor.js")
public class QuillEditorComponent extends Component implements HasSize, HasStyle {

    public QuillEditorComponent() {
        setWidth("100%");
    }

    public QuillEditorComponent(String placeholder) {
        this();
        getElement().setProperty("placeholder", placeholder);
    }

    public void setValue(String html) {
        getElement().setProperty("value", html != null ? html : "");
        getElement().callJsFunction("setValue", html != null ? html : "");
    }

    public String getValue() {
        return getElement().getProperty("value", "");
    }

    public void setReadonly(boolean readonly) {
        getElement().setProperty("readonly", readonly);
    }

    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder", placeholder);
    }

    /**
     * Add a value change listener using Vaadin's event system.
     */
    public void addValueChangeListener(java.util.function.Consumer<String> listener) {
        getElement().addEventListener("value-changed", e -> {
            String value = e.getEventData().getString("event.detail.value");
            listener.accept(value);
        }).addEventData("event.detail.value");
    }
}
