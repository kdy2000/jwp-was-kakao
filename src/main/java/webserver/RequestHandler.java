package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import controller.Controller;
import controller.Dispatcher;
import controller.LoginController;
import controller.UserController;
import http.*;
import http.request.Request;
import http.response.Response;
import http.response.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;
import utils.IOUtils;


public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final Dispatcher dispatcher = Dispatcher.getInstance();
    private final List<Controller> controllers;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;

        controllers = new ArrayList<>();
        controllers.add(UserController.getInstance());
        controllers.add(LoginController.getInstance());
        controllers.stream().forEach(Controller::registerAll);
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            Request request = receiveRequest(br);
            Response response = prepareResponse(request);

            printResponse(out, response);

        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private Request receiveRequest(BufferedReader br) throws IOException {
        List<String> lines = Arrays.stream(IOUtils.readData(br, 5000).split("\r\n"))
                .map(String::trim)
                .collect(Collectors.toList());
        Request request = new Request(lines);
        return request;
    }

    private Response prepareResponse(Request request) throws IOException, URISyntaxException {
        if (FileIoUtils.pathIsFile(request.getUrlPath())) {
            return FileIoUtils.loadFileFromUrlPath(request.getUrlPath());
        }
        return dispatcher.run(request);
    }

    private void printResponse(OutputStream out, Response response) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.write(response.toBytes(),
                0,
                response.toBytes().length);
        dos.flush();
    }
}
