package domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Dispatcher {
    private static final Dispatcher instance = new Dispatcher();
    private final Map<String, Function<Request, Response>> dispatcher;

    private Dispatcher() {
        this.dispatcher = new HashMap<>();
    }

    public void register(String urlPath, String method, Function<Request, Response> function) {
        dispatcher.put(makeKey(urlPath, method), function);
    }

    public Response run(Request request) {
        return dispatcher
                .get(makeKey(request.getUrlPath(), request.getMethod()))
                .apply(request);
    }

    private String makeKey(String urlPath, String method) {
        return urlPath + ":" + method;
    }

    public static Dispatcher getInstance() {
        return instance;
    }
}
