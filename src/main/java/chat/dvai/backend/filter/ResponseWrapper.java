package chat.dvai.backend.filter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private int httpStatus;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        this.httpStatus = response.getStatus();
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.httpStatus = sc;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.httpStatus = sc;
        super.sendError(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        this.httpStatus = sc;
        super.setStatus(sc);
    }

    public int getStatus() {
        return this.httpStatus;
    }
}
