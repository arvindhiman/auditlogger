package org.example.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Component
@Order(2)
public class AuditLogFilter implements Filter {

    @Autowired
    DynamoDbClient dynamoDbClient;

    String tableName = "AUDIT_LOG";

    @Value( "${app.id}" )
    private String appId;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        long startAt = System.currentTimeMillis();
        HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();

        itemValues.put("app_id", AttributeValue.builder().s(appId).build());
        itemValues.put("timestamp", AttributeValue.builder().n(String.valueOf(startAt)).build());
        itemValues.put("uri", AttributeValue.builder().s(req.getMethod() + " " + req.getRequestURI()).build());
        itemValues.put("request_time", AttributeValue.builder().s(String.valueOf(new Date())).build());

        filterChain.doFilter(req, res);
        long endAt = System.currentTimeMillis();

        String currentUserName = "-";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            currentUserName = authentication.getName();

        }

        itemValues.put("responss_time", AttributeValue.builder().s(String.valueOf(new Date())).build());
        itemValues.put("user", AttributeValue.builder().s(currentUserName).build());
        itemValues.put("status", AttributeValue.builder().s(HttpStatus.resolve(res.getStatus()).toString()).build());
        itemValues.put("time_taken", AttributeValue.builder().s(String.valueOf((endAt - startAt))).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {

            CompletableFuture<PutItemResponse> future
                    = CompletableFuture.supplyAsync(() -> dynamoDbClient.putItem(request));

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    public void destroy() {

    }
}
