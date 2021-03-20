package org.example.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Component
public class DynamoDBAuditService {

    @Autowired
    DynamoDbClient dynamoDbClient;

    String tableName = "AUDIT_LOG";

    @Value( "${app.id}" )
    private String appId;

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void auditRequest(HttpServletRequest req, HttpServletResponse res) {

        long startAt = System.currentTimeMillis();

        HashMap<String,AttributeValue> itemValues = new HashMap<String, AttributeValue>();

        itemValues.put("app_id", AttributeValue.builder().s(appId).build());
        itemValues.put("timestamp", AttributeValue.builder().n(String.valueOf(startAt)).build());
        itemValues.put("requestTime", AttributeValue.builder().s(String.valueOf(new Date())).build());
        itemValues.put("uri", AttributeValue.builder().s(req.getMethod() + " " + req.getRequestURI()).build());
        itemValues.put("status", AttributeValue.builder().s(HttpStatus.resolve(res.getStatus()).toString()).build());

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
//            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
//            System.exit(1);
        }
        long endAt = System.currentTimeMillis();
        System.out.println("logged in: " + (endAt - startAt));

    }

}
