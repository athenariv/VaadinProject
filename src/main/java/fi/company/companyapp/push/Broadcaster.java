package fi.company.companyapp.push;

import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Server Push broadcaster — allows pushing live messages to all connected Vaadin UIs.
 */
public class Broadcaster {

    private static final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public static Registration register(Consumer<String> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public static void broadcast(String message) {
        for (Consumer<String> listener : listeners) {
            listener.accept(message);
        }
    }
}
