package com.ikn.ums.msteams.exception;

public class ErrorCodeMessages {

	 public static final String ERR_EVENTS_NOT_FOUND_BATCH_CODE = "EVENTS-NOT-FOUND-1001";
	 public static final String ERR_EVENTS_NOT_FOUND_BATCH_MSG = "Employees not found for batch processing";
	 
	 public static final String ERR_UNKNOWN_BATCH_CODE = "BATCH-UNKNOWN-1002";
	 public static final String ERR_UNKNOWN_BATCH_MSG = "An Unknown Error occured while batch processing";
	 
	 public static final String ERR_EVENT_SERVICE_NOT_FOUND_CODE = "EVENTS-CORE-SERVICE-1003";
	 public static final String ERR_EVENT_SERVICE_NOT_FOUND_MSG = "Requested Employee Service not present.";
	 
	 public static final String ERR_EVENT_SERVICE_EXCEPTION_CODE = "EVENTS_SERVICE_EXCEPTION_CODE-1004";
	 public static final String ERR_EVENT_SERVICE_EXCEPTION_MSG = "Exception Occured in the Employee Service Layer."; 	

	 public static final String ERR_EVENT_EMAIL_ID_EMPTY_CODE = "EVENTS_EMAIL_ID_NOT_FOUND_CODE-1005";
	 public static final String ERR_EVENT_EMAIL_ID_EMPTY_MSG = "User id is empty"; 
	 
	 public static final String ERR_EVENT_GET_UNSUCCESS_CODE = "EVENTS_GET_RETRIVE_CODE-1006";
	 public static final String ERR_EVENT_GET_UNSUCCESS_MSG = "Error Occured While Retrieving Event Details !";
	 
	 public static final String ERR_EVENT_GET_ATT_COUNT_UNSUCCESS_CODE = "EVENTS_ATTENDED_COUNT_RETRIVE_CODE-1007";
	 public static final String ERR_EVENT_GET_ATT_COUNT_UNSUCCESS_MSG = "Error Occured While Retrieving total attended events count !";
	 
	 public static final String MSTEAMS_BATCHPROCESS_UNSUCCESS_CODE = "EVENTS_BATCHPROCESS_CODE-1008";
	 public static final String MSTEAMS_BATCHPROCESS_UNSUCCESS_MSG = "Error Occured While batch processing !";
	 
	 public static final String ERR_EVENT_GET_ORG_COUNT_UNSUCCESS_CODE = "EVENTS_COUNT_RETRIVE_CODE-1009";
	 public static final String ERR_EVENT_GET_ORG_COUNT_UNSUCCESS_MSG = "Error Occured While Retrieving total organized events count !";
	
	 public static final String ERR_EVENT_INVALID_EVENTID_CODE = "EVENTS_INVALID_EVENTID_CODE-1010";
	 public static final String ERR_EVENT_INVALID_EVENTID_MSG = "Invalid Event id !";
	 
	 public static final String ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_CODE = "EVENTS_INVALID_EVENTID_CODE-1010";
	 public static final String ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_MSG = "Error occured while retrieving action items for an event!";
	 
	 public static final String ERR_EVENTS_GET_ALL_UNSUCCESS_CODE = "EVENTS_INVALID_EVENTID_CODE-1010";
	 public static final String ERR_EVENTS_GET_ALL_UNSUCCESS_MSG = "Error occured while retrieving all events for user!";
	 
	 public static final String MSTEAMS_BATCH_PROCESS_SUCCESS_CODE = "MSTEAMS_BATCH_PROCESS_SUCESS_CODE-1011";
	 public static final String MSTEAMS_BATCH_PROCESS_SUCCESS_MSG = "Raw Data Batch processing is Successfull !";
	 
	 public static final String MSTEAMS_BATCH_PROCESS_GET_UNSUCCESS_CODE = "MSTEAMS_BATCH_PROCESS_GET_UNSUCCESS_CODE-1012";
	 public static final String MSTEAMS_BATCH_PROCESS_GET_UNSUCCESS_MSG = "Error occured while fethcing batch process list";
	 
	 public static final String ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_CODE = "MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_CODE-1013";
	 public static final String ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_MSG = "Cron time or expression is empty.";
	 
	 public static final String ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_UPADTE_UNSUCCESS_CODE = "ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_UPADTE_UNSUCCESS_CODE-1014";
	 public static final String ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_UPADTE_UNSUCCESS_MSG = "Error while updating crontime for batch process.";

}
