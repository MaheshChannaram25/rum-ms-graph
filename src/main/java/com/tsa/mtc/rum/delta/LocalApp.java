package com.tsa.mtc.rum.delta;

import com.google.gson.JsonObject;
import com.microsoft.graph.auth.publicClient.UsernamePasswordProvider;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventDeltaCollectionPage;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LocalApp {

    private final static String CSV_LOCATION = "Appointments_2019.csv ";
    private final static int PAGE_SIZE = 200;
    private static final String[] CSV_COLUMNS = new String[]
            {
                    "appointmentId", "hotelId", "hotelName", "opportunityId", "userId", "activityType", "startDateTime",
                    "endDateTime", "appointmentStatus", "durationMins", "durationDays", "durationHours",
                    "isBillable", "location", "activityDetails", "notes", "isTrainerLocal", "originalStartDate",
                    "originalEndDate", "createdBy", "createdDate", "modifiedBy", "modifiedDate", "subject", "eventType"
            };

    // Get bean for writing the logic into csv
    private static StatefulBeanToCsv<Appointment> getBeanWriter(FileWriter writer) {
        // Creating Mapping Strategy
        ColumnPositionMappingStrategy<Appointment> mappingStrategy = new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Appointment.class);
        mappingStrategy.setColumnMapping(CSV_COLUMNS);

        // Creating StatefulBeanToCsv object
        StatefulBeanToCsvBuilder<Appointment> builder = new StatefulBeanToCsvBuilder<>(writer);
        return builder
                .withMappingStrategy(mappingStrategy)
                .withSeparator('|')
                .withApplyQuotesToAll(false)
                .build();
    }

    // Start of function
    public static void main(String[] args) {
        System.out.println("Start time = " + System.currentTimeMillis());
        UsernamePasswordProvider authProvider = new UsernamePasswordProvider(
                "21ed2b33-cbc9-44bc-b51b-6be886d17e81",
                Collections.singletonList("https://graph.microsoft.com/.default"),
                "admin.sugarcrm@tsa-solutions.com",
                "CI6Ikp@XVCJ9");

//        UsernamePasswordProvider authProvider = new UsernamePasswordProvider(
//                        "ed5a7015-2d18-485f-affd-4a25902c5129",
//                        Collections.singletonList("https://graph.microsoft.com/.default"),
//                        "test.dev@tsa-solutions.com",
//                "Passw0rd");
        FileWriter writer = null;

        try {
            writer = new FileWriter(CSV_LOCATION);
            StatefulBeanToCsv<Appointment> csvWriter = getBeanWriter(writer);
            LinkedList<Option> requestOptions = new LinkedList<>();
            requestOptions.add(new HeaderOption("Prefer", "odata.maxpagesize=" + PAGE_SIZE));

            IGraphServiceClient graphClient = GraphServiceClient
                    .builder()
                    .authenticationProvider(authProvider)
                    .buildClient();

            String deltaToken = null;
//            deltaToken = "g3XmoZPpES0cu0h_mPznsAU2FwMWGZPyXE9VmmnHvdJMFxjQBczn9QcI7jnUVF7jds-_FUgzTGsbpoF_75pdjcrhzKshR3YHzj_bWhmptRqzeUECbYi9q3NA40zltMp3DWVE9-CZ5PtrXCGn6cFueoaohQeFYKdsZOCNM4RS2nLyO6zFh1gkUrEF8YvFSBgUfyEVtGSQxUi4HiZn8UbqXYOnXQHlLTetVKIAlz-tfWI.P_N1ElNKNzGiZQk0ZmVnt_2HxRGFbZKT82F8ib3VxYQ";

            if (deltaToken != null) {
                System.out.println("Processing Delta Changes");
                requestOptions.add(new QueryOption("$deltatoken", deltaToken));
            } else {
                System.out.println("Processing Full Data");
                requestOptions.add(new QueryOption("startDateTime", "2019-01-01T00:00:00-00:00"));
                requestOptions.add(new QueryOption("endDateTime", "2020-01-01T00:00:00-00:00"));
            }

            IEventDeltaCollectionPage calendarViewDelta = graphClient.me().calendarView()
                    .delta()
                    .buildRequest(requestOptions)
                    .get();

            JsonObject rawObject = calendarViewDelta.getRawObject();

            String nextLink = Utilities.getLink(rawObject, "@odata.nextLink");
            String deltaLink = Utilities.getLink(rawObject, "@odata.deltaLink");

            System.out.println("nextLink = " + nextLink);
            System.out.println("deltaLink = " + deltaLink);

            List<Event> currentPage = calendarViewDelta.getCurrentPage();
            List<Appointment> appointmentList = Utilities.populateAppointmentData(currentPage);
            csvWriter.write(appointmentList);

            while (null == deltaLink && nextLink != null) {
                requestOptions = new LinkedList<>();
                requestOptions.add(new QueryOption("$skiptoken", Utilities.getToken(nextLink, "$skiptoken=")));

                calendarViewDelta = graphClient.me().calendarView()
                        .delta()
                        .buildRequest(requestOptions)
                        .get();

                rawObject = calendarViewDelta.getRawObject();

                nextLink = Utilities.getLink(rawObject, "@odata.nextLink");
                deltaLink = Utilities.getLink(rawObject, "@odata.deltaLink");

                System.out.println("loop nextLink = " + nextLink);
                System.out.println("loop deltaLink = " + deltaLink);

                currentPage = calendarViewDelta.getCurrentPage();
                appointmentList = Utilities.populateAppointmentData(currentPage);
                csvWriter.write(appointmentList);
            }

            System.out.println("Delta Link for next round = " + deltaLink);
            System.out.println("Delta Token for next round = " + Utilities.getToken(deltaLink, "$deltatoken="));

        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("End time = " + System.currentTimeMillis());
    }
}
