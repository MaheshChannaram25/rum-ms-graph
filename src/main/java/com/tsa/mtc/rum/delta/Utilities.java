package com.tsa.mtc.rum.delta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.graph.models.extensions.Event;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Utilities {

    public static final String DOE_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String EMPTY_VAL = "";
    private static final String VAL_SPACE = "&nbsp;";
    private static final String STARTS_WITH_DEL = "(?i:.*Del.*)";
    private static final String DATA_SEPARATOR = "\\|";
    private static final String START_APT_TAG = "APT DATA START";
    private static final String END_APT_TAG = "APT DATA END";
    private static final String START_META_TAG = "Meta Data Start";
    private static final String END_META_TAG = "Meta";
    private static final int APT_DATA_ARRAY_LENGTH = 11;
    private static final int META_DATA_APPOINTMENT_STATUS = 0;
    private static final int META_DATA_ACTIVITY_TYPE = 1;
    private static final int META_DATA_ACTIVITY_DETAILS = 2;
    private static final int META_DATA_HOTEL_DETAILS = 3;
    private static final int META_DATA_LOCATION = 4;
    private static final int META_DATA_USERNAME = 5;
    private static final int META_DATA_NOTES = 6;
    private static final int META_DATA_MODIFIED_BY = 7;
    private static final int META_DATA_OPPORTUNITY_ID = 8;
    private static final int META_DATA_WAIVE_FEES = 9;
    private static final int META_DATA_MODIFIED_DATE = 10;
    private static final int META_DATA_CREATED_BY = 11;
    private static final String[] EMPTY_ARR_VAL = {};
    private static final int META_DATA_ARRAY_LENGTH = 8;
    public static HashMap<String, Appointment> seriesMasterMap = new HashMap<>();

    public static List<Appointment> populateAppointmentData(List<Event> eventList) {
        List<Appointment> appointmentList = getAppointmentList(eventList);
//        System.out.println(appointmentList.size());
        return appointmentList;
    }

    private static List<Appointment> getAppointmentList(List<Event> eventList) {
        List<Appointment> appointmentList = new ArrayList<>();
        List<Appointment> recurringAppointmentList = new ArrayList<>();

        for (Event event : eventList) {

            JsonObject rawObject = event.getRawObject();
            if (null == rawObject || null == rawObject.get("type")) {
                continue;
            }

            String eventType = rawObject.get("type").getAsString();

            // Populate variables directly from rawObject
            // System.out.println(rawObject);
            if (rawObject.get("body") == null && eventType.equals("occurrence")) {
                Appointment appointment = new Appointment();
                try {

                    appointment.setStartDateTime(getStartOrEndDate(rawObject.get("start").getAsJsonObject().get("dateTime").getAsString())); //DATE_FORMAT.parse(rawObject.get("start").getAsJsonObject().get("dateTime").getAsString()));
                    appointment.setEndDateTime(getStartOrEndDate(rawObject.get("end").getAsJsonObject().get("dateTime").getAsString())); //DATE_FORMAT.parse(rawObject.get("end").getAsJsonObject().get("dateTime").getAsString()));
                    appointment.setAppointmentId(rawObject.get("id").getAsString());
                    appointment.setSeriesMasterId(rawObject.get("seriesMasterId").getAsString());
                    appointment.setEventType("OCCURRENCE");
                    recurringAppointmentList.add(appointment);
                    continue;
                } catch (Exception e) {
                    appointment.setStartDateTime(null);
                    appointment.setEndDateTime(null);
                }
            }
            if (rawObject.get("body") == null) {
                continue;
            }
            JsonObject body = rawObject.get("body").getAsJsonObject();
            JsonElement content = body.get("content");

            String[] contentArray;

            // skipping events that has subject as "Del"
            String subject = event.getRawObject().get("subject").getAsString();

            try {
                // if (!subject.matches(STARTS_WITH_DEL)) {
                String sContent = content.getAsString();
                sContent = (sContent == null) ? EMPTY_VAL
                        : sContent.replaceAll(VAL_SPACE, EMPTY_VAL).replaceAll("&#43;", "+").replaceAll("&amp;", "&")
                        .replaceAll("--", ",").replaceAll("\\r\\n ", " ").replaceAll("\\r\\n", " ");

                if (sContent.contains(START_META_TAG) || sContent.contains(START_APT_TAG)) {
                    contentArray = StringUtils.split(getRequiredContent(sContent), DATA_SEPARATOR);
                } else {
                    contentArray = EMPTY_ARR_VAL;
                }

                if (isRequiredContentValid(sContent, contentArray)) {
                    sContent = sContent.trim();
                    int arraySize = contentArray.length - 1;
                    // Populate data from meta tags
                    if (!sContent.isEmpty()) {
                        Appointment appointment = new Appointment();
                        appointment.setAppointmentId(rawObject.get("id").getAsString());

                        // Location 0 in meta data
                        appointment.setAppointmentStatus(arraySize >= META_DATA_APPOINTMENT_STATUS ? contentArray[META_DATA_APPOINTMENT_STATUS].trim() : null);

                        // Location 1 in meta data
                        appointment.setActivityType(arraySize >= META_DATA_ACTIVITY_TYPE ? contentArray[META_DATA_ACTIVITY_TYPE].trim() : null);

                        appointment.setActivityDetails(arraySize >= META_DATA_ACTIVITY_DETAILS ? contentArray[META_DATA_ACTIVITY_DETAILS].trim() : null);
                        //Billable
                        if (appointment.getActivityDetails() != null) {
                            String activityDetails = contentArray[META_DATA_ACTIVITY_DETAILS].trim();
                            String[] activity_details_arr = activityDetails.split(":");
                            if (activity_details_arr.length == 3) {
                                assert activity_details_arr[2] != null;
                                if (activity_details_arr[2].equals("Billable")) {
                                    //logic added according to old BI-ETL
                                    appointment.setBillable(appointment.getAppointmentStatus().equals("Confirmed") || appointment.getAppointmentStatus().equals("CXL by hotel"));
                                } else
                                    appointment.setBillable(false);
                            }
                        } else {
                            appointment.setBillable(false);
                        }
                        if (null == appointment.getBillable()) {
                            appointment.setBillable(false);
                        }

                        if (arraySize >= META_DATA_HOTEL_DETAILS) {
                            String hotelDetails = contentArray[META_DATA_HOTEL_DETAILS].trim();
                            appointment.setHotelId(getHotelID(hotelDetails));
                            appointment.setHotelName(getHotelName(hotelDetails));
                        } else {
                            appointment.setHotelId(null);
                            appointment.setHotelName(null);
                        }
                        if (arraySize >= META_DATA_USERNAME) {
                            appointment.setUserId(contentArray[META_DATA_USERNAME].trim().equals(EMPTY_VAL) ? EMPTY_VAL : contentArray[META_DATA_USERNAME].trim());
                        } else {
                            appointment.setUserId(null);
                        }
                        if (arraySize >= META_DATA_LOCATION) {
                            appointment.setLocation(contentArray[META_DATA_LOCATION].trim().replaceAll(VAL_SPACE, EMPTY_VAL));
                        } else {
                            appointment.setLocation(null);
                        }
                        if (arraySize >= META_DATA_NOTES) {
                            appointment.setNotes(contentArray[META_DATA_NOTES].replaceAll("<.*?>", "").trim());
                        } else {
                            appointment.setNotes(null);
                        }
                        if (arraySize >= META_DATA_MODIFIED_BY) {
                            appointment.setModifiedBy(contentArray[META_DATA_MODIFIED_BY].trim().isEmpty() ? EMPTY_VAL : contentArray[META_DATA_MODIFIED_BY].trim());
                        } else {
                            appointment.setModifiedBy("");
                        }
                        //Start DateTime and End DateTime
                        String start = rawObject.get("start").getAsJsonObject().get("dateTime").getAsString();
                        appointment.setStartDateTime(getStartOrEndDate(start));
                        String end = rawObject.get("end").getAsJsonObject().get("dateTime").getAsString();
                        appointment.setEndDateTime(getStartOrEndDate(end));

                        //	System.out.println("start date :"+ appointment.getStartDateTime() +"\n End Date : "+ appointment.getEndDateTime());

                        //Duration
                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date dateStart = utcFormat.parse(start);
                        Date dateEnd = utcFormat.parse(end);
                        float durationInMinutes = getDurationInMinutes(dateStart, dateEnd);
                        appointment.setDurationMins(durationInMinutes);
                        appointment.setDurationHours((float) (Math.round(getDurationInHours(durationInMinutes) * 10) / 10.0));  //old BI_ETL logic
                        appointment.setDurationDays((float) (Math.round(getDurationInDays(durationInMinutes) * 10) / 10.0));    //old BI_ETL logic
                        //appointment.setDurationHours(durationInMinutes / 60);
                        //appointment.setDurationDays(durationInMinutes / 60 / 8); //consider 1 day as 8 hours

                        if (contentArray.length > META_DATA_ARRAY_LENGTH) {
                            appointment.setOpportunityId(contentArray[META_DATA_OPPORTUNITY_ID].trim().equals("0") ? EMPTY_VAL : contentArray[META_DATA_OPPORTUNITY_ID].trim());
                            String isTrainerLocal;
                            isTrainerLocal = contentArray[META_DATA_WAIVE_FEES].trim();
                            if (isTrainerLocal.trim().length() > 4) {
                                isTrainerLocal = EMPTY_VAL;
                            }
                            appointment.setIsTrainerLocal(isTrainerLocal);
                        }
                        appointment.setCreatedBy(contentArray.length > APT_DATA_ARRAY_LENGTH ? contentArray[META_DATA_CREATED_BY].trim() : EMPTY_VAL);
                        //CreatedDate
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                        calendar = event.createdDateTime;

                        Date cd = calendar.getTime();
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                        String createdDate = df.format(cd);
                        appointment.setCreatedDate(createdDate);

                        // Modified Date
                        if (arraySize >= META_DATA_MODIFIED_DATE) {
                            String MD = contentArray[META_DATA_MODIFIED_DATE].trim();
                            if (MD.length() > 2) {
                                Date CD = calendar.getTime();
                                DateFormat sgtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                sgtFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                                String sgtCD = sgtFormat.format(CD);
                                Date sgtCreatedDate = parserDate(sgtCD, "yyyy-MM-dd HH:mm:ss");

                                Date mDMetaData;
                                if (MD.lastIndexOf(":") > 13) {
                                    mDMetaData = parserDate(MD, "dd-MM-yyyy HH:mm:ss");
                                } else {
                                    mDMetaData = parserDate(MD, "dd-MM-yyyy HH:mm");
                                }

                                if (mDMetaData.before(sgtCreatedDate)) {
                                    appointment.setModifiedDate(createdDate);
                                } else {
                                    appointment.setModifiedDate(getCsvDateString(mDMetaData));
                                }
                            }
                        } else {
                            appointment.setModifiedDate(null);
                        }
                        //  System.out.println("\ncreatedDate :" + appointment.getCreatedDate() + "|n Modifieddate :"+ appointment.getModifiedDate() + "\nduration in mins :" + appointment.getDurationMins() + "\nHours ;" + appointment.getDurationHours() + "\nDays"+ appointment.getDurationDays());

                        if (subject.matches(STARTS_WITH_DEL)) {
                            appointment.setSubject("DEL");
                        }

                        boolean isCancelled = rawObject.get("isCancelled").getAsBoolean();
                        //     System.out.println("\neventType is Cancelled.."+ isCancelled);
                        if (isCancelled) {
                            appointment.setSubject("CAN");
                        }

                        String seriesType = rawObject.get("type").getAsString();
                        if (seriesType.equals("seriesMaster")) {
                            appointment.setEventType("SERIES_MASTER");
                            seriesMasterMap.put(rawObject.get("id").getAsString(), appointment);
                        } else if (seriesType.equals("exception")) {
                            appointment.setEventType("EXCEPTION");
                        }
                        appointmentList.add(appointment);
                    }
                }
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Appointment recurringEvent : recurringAppointmentList) {
            if (seriesMasterMap.containsKey(recurringEvent.getSeriesMasterId())) {
                Appointment seriesMasterEvent = seriesMasterMap.get(recurringEvent.getSeriesMasterId());
                populateDetailsFromMaster(recurringEvent, seriesMasterEvent);
                appointmentList.add(recurringEvent);
            }
        }

        return appointmentList;
    }


    private static Integer getHotelID(String hotelDetails) {
        if (hotelDetails.contains("[") && hotelDetails.contains("]"))
            return Integer.valueOf(hotelDetails.substring(hotelDetails.indexOf("[") + 1, hotelDetails.indexOf("]")));
        else
            return 0;
    }

    private static String getHotelName(String hotelDetails) {
        if (hotelDetails.contains("[") && hotelDetails.contains("]")) {
            hotelDetails = hotelDetails.substring(0, hotelDetails.indexOf("["));
        }
        return hotelDetails.trim();
    }

    private static Float getDurationInMinutes(Date startDateTime, Date endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0f;
        }
        return (float) ((endDateTime.getTime() - startDateTime.getTime()) / (60 * 1000));
    }

	private static Float getDurationInDays(float timeInMinutes) {
		 float counter = 0;
		  while(true) {
	        if (timeInMinutes >= 0 && timeInMinutes < 480) {
	            return counter + (timeInMinutes / 60) / 8;
	        } else if (timeInMinutes >= 480 && timeInMinutes < 1440) {
	            return counter + 1;
	        } else if (timeInMinutes >= 1440 && timeInMinutes < 1920) {
	            return (float) (counter + 1.5);
	        } else if (timeInMinutes == 1920) {
	            return counter + 2;
	        } else if (timeInMinutes > 1920) {
	            timeInMinutes = timeInMinutes - 1440;
	            counter = counter + 1;
	        }
	      }
    }


    private static Float getDurationInHours(float timeInMinutes) {
        float hours;
        if (timeInMinutes >= 0 && timeInMinutes < 480)
            hours = (timeInMinutes / 60);
        else {
            hours = (getDurationInDays(timeInMinutes)) * 8;
        }
        return hours;
    }

    /*
    public static String getDoeDateString(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat format = new SimpleDateFormat(DOE_DATE_FORMAT);
        return format.format(date);
    }
    */

    public static String getCsvDateString(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private static String getRequiredContent(String sContent) {
        // If APT-START-TAG is present, take content between APT TAG.
        if (sContent.contains(START_APT_TAG)) {
            return sContent.substring(sContent.indexOf(START_APT_TAG) + START_APT_TAG.length(),
                    sContent.lastIndexOf(END_APT_TAG));
        }
        // If APT-START-TAG is not present, take content of START-META-TAG
        return sContent.substring(sContent.indexOf(START_META_TAG) + START_META_TAG.length(),
                sContent.lastIndexOf(END_META_TAG));
    }

    private static boolean isRequiredContentValid(String sContent, String[] contentArray) {
        if (sContent == null || !(sContent.contains(START_META_TAG) || sContent.contains(START_APT_TAG))) {
            return false;
        }
        if (sContent.contains(START_APT_TAG)) {
            return ((contentArray.length == APT_DATA_ARRAY_LENGTH)
                    || (contentArray.length == (APT_DATA_ARRAY_LENGTH + 1)));
        }
        return (contentArray.length == META_DATA_ARRAY_LENGTH);
    }

    private static void populateDetailsFromMaster(Appointment recurringEvent, Appointment seriesMasterEvent) {
        recurringEvent.setHotelId(seriesMasterEvent.getHotelId());
        recurringEvent.setHotelName(seriesMasterEvent.getHotelName());
        recurringEvent.setOpportunityId(seriesMasterEvent.getOpportunityId());
        recurringEvent.setUserId(seriesMasterEvent.getUserId());
        recurringEvent.setActivityType(seriesMasterEvent.getActivityType());
        recurringEvent.setActivityDetails(seriesMasterEvent.getActivityDetails());
        recurringEvent.setAppointmentStatus(seriesMasterEvent.getAppointmentStatus());
        recurringEvent.setDurationMins(seriesMasterEvent.getDurationMins());
        recurringEvent.setDurationDays(seriesMasterEvent.getDurationDays());
        recurringEvent.setDurationHours(seriesMasterEvent.getDurationHours());
        recurringEvent.setIsTrainerLocal(seriesMasterEvent.getIsTrainerLocal());
        recurringEvent.setBillable(seriesMasterEvent.getBillable());
        recurringEvent.setLocation(seriesMasterEvent.getLocation());
        recurringEvent.setNotes(seriesMasterEvent.getNotes());
        recurringEvent.setOriginalStartDate(seriesMasterEvent.getOriginalStartDate());
        recurringEvent.setOriginalEndDate(seriesMasterEvent.getOriginalEndDate());
        recurringEvent.setCreatedBy(seriesMasterEvent.getCreatedBy());
        recurringEvent.setCreatedDate(seriesMasterEvent.getCreatedDate());
        recurringEvent.setModifiedBy(seriesMasterEvent.getModifiedBy());
        recurringEvent.setModifiedDate(seriesMasterEvent.getModifiedDate());
        recurringEvent.setSubject(seriesMasterEvent.getSubject());
    }

    public static String getToken(String nextLink, String keyword) {
        if (nextLink != null)
            return nextLink.substring(nextLink.indexOf(keyword) + keyword.length());
        return null;
    }

    public static String getLink(JsonObject rawObject, String linkName) {
        if (rawObject.get(linkName) != null)
            return rawObject.get(linkName).getAsString();
        else return null;
    }


    public static Date parserDate(String dateStr, String dateFormat) {
        if (dateStr == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(dateFormat);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }


    public static String getStartOrEndDate(String date) {
        try {
            Date date1;
            String sgtDate;

            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            date1 = utcFormat.parse(date);

            DateFormat sgtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sgtFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

            sgtDate = sgtFormat.format(date1);
            return sgtDate;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
