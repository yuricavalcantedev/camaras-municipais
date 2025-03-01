package com.yuri.development.camaras.municipais.util;

public interface EventConstants {

    // 11xx townhall, 12xx session, 4xxx erros

    int LOGIN_SUCCESS = 1001;
    String LOGIN_SUCCESS_DESCRIPTION = "User [{}] logged in successfully";

    int TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED = 4001;
    String TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED_DESCRIPTION = "The current townhall has not a table configured. " +
            "Configure the table of roles and try it again";

    int TOWNHALL_HAS_SESSION_ALREADY = 4002;
    String TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION = "The current townhall has already a session created for today";

    int LOGIN_UNSUCCESSFUL = 4003;
    String LOGIN_UNSUCCESSFUL_DESCRIPTION = "Username and password combination does not match.";

    int COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION = 4004;
    String COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION_DESCRIPTION = "User {} tried to loggin in but there is no open session for today";

    int TOWNHALL_NOT_FOUND = 4005;
    String TOWNHALL_NOT_FOUND_DESCRIPTION = "Townhall not found";

    int PARLAMENTAR_DELETED = 4006;
    String PARLAMENTAR_DELETED_DESCRIPTION = "Parlamentar deleted because was not found in SAPL";

    int OLD_PARLAMENTAR_DELETION = 4007;

    String OLD_PARLAMENTAR_DELETION_DESCRIPTION = "Parlamentar not deleted -> DataIntegrityException";

    int DEACTIVATE_PARLAMENTAR = 4008;

    String DEACTIVATE_PARLAMENTAR_DESCRIPTION = "Parlamentar deactivated";

    int DEACTIVATE_PARLAMENTAR_ERROR = 4009;

    String DEACTIVATE_PARLAMENTAR_ERROR_DESCRIPTION = "Something wrong happened! Parlamentar NOT deactivated";

    int ERROR_COMMUNICATION_SAPL = 5000;
    String ERROR_COMMUNICATION_SAPL_DESCRIPTION = "There was some error in the communication with SAPL";

    int SAPL_SESSION_NOT_FOUND = 5001;
    String SAPL_SESSION_NOT_FOUND_DESCRIPTION = "There's no SAPL session with the given id";

    int DATABASE_STRUCUTRE_ERROR = 5002;
    String DATABASE_STRUCUTRE_ERROR_DESCRIPTION = "Database structure error. Please, contact the developer.";

    int SAPL_FIND_SESSION = 5003;
    String SAPL_FIND_SESSION_DESCRIPTION = "Searching for session in SAPL";

    int SAPL_PARLAMENTAR_LIST = 5004;
    String SAPL_PARLAMENTAR_LIST_DESCRIPTION = "Parlamentar list retrieved from SAPL";

    int SAPL_FIND_SUBJECT_LIST_PER_PAGE_CODE = 5005;
    String SAPL_FIND_SUBJECT_LIST_PER_PAGE_DESCRIPTION = "Fetching subject list from SAPL";

    int SAPL_FETCH_ORIGINAL_EMENTA_TEXT_URL_CODE = 5006;
    String SAPL_FETCH_ORIGINAL_EMENTA_TEXT_URL_DESCRIPTION = "Fetching original ementa text from SAPL";

    int ERROR_UNEXPECTED_EXCEPTION = 5999;
    String ERROR_UNEXPECTED_EXCEPTION_DESCRIPTION = "Unexpected expection! Message: {}";

    // 12xx Session Events

    int CREATE_SESSION = 1200;
    String CREATE_SESSION_DESCRIPTION = "Session created successfully for townhall ";

    int GET_SESSION_VOTING_INFO_STANDARD_BY_UUID = 1201;
    String GET_SESSION_VOTING_INFO_STANDARD_BY_UUID_DESCRIPTION = "Get session voting default retrieved with success";

    int CREATE_VOTING_FOR_SESSION = 1202;

    String CREATE_VOTING_FOR_SESSION_DESCRIPTION = "Voting was created sucessfully";

    int DELETE_SESSION = 1203;
    String DELETE_SESSION_DESCRIPTION = "Session deleted successfully";

    int GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION = 1204;
    String GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION_DESCRIPTION = "Get parlamentar presence list for session";

    int UPDATE_PARLAMENTAR_PRESENCE = 1205;
    String UPDATE_PARLAMENTAR_PRESENCE_DESCRIPTION = "Parlamentar presence manually updated with success";

    int UPDATE_PARLAMENTAR_PRESENCE_MANUALLY = 1220;
    String UPDATE_PARLAMENTAR_PRESENCE_MANUALLY_DESCRIPTION = "Parlamentar presence manually updated with success";

    int COMPUTE_VOTE = 1206;
    String COMPUTE_VOLTE_DESCRIPTION = "Vote computed successfully for parlamentar {} into session {}";

    int FIND_TODAY_SESSION_BY_TOWNHALL = 1221;

    String FIND_TODAY_SESSION_BY_TOWNHALL_DESCRIPTION = "Session from {} retrieved for parlamentar user with success";

    int TODAY_SESSION_BY_TOWNHALL_NOT_FOUND = 1207;

    String TODAY_SESSION_BY_TOWNHALL_NOT_FOUNDDESCRIPTION = "Session from {} not found for today";

    int PARLAMENTAR_SUBSCRIPTION = 1208;
    String PARLAMENTAR_SUBSCRIPTION_DESCRIPTION = "Parlamentar just subscripted successfully";

    int CLOSE_VOTING_FOR_SESSION = 1209;
    String CLOSE_VOTING_FOR_SESSION_DESCRIPTION = "Voting closed";

    int FIND_SESSION_VOTING_INFO = 1210;
    String FIND_SESSION_VOTING_INFO_DESCRIPTION = "Voting info retrieved";

    int FIND_SUBJECTS_FOR_SESSION = 1211;
    String FIND_SUBJECTS_FOR_SESSION_DESCRIPTION = "Getting all subjects with the desired filter for session";

    int RESET_VOTE = 1212;
    String RESET_VOTE_DESCRIPTION = "Vote reset successfully for parlamentar {} into session {}";

    int DELETE_CONTROL = 1213;
    String DELETE_CONTROL_DESCRIPTION = "Control deleted successfully Id {}";

    int CREATE_CONTROL = 1214;
    String CREATE_CONTROL_DESCRIPTION = "Control created successfully for type {}, command {}, town hall id {}";

    int FIND_CONTROL_BY_TYPE_AND_TOWN_HALL_ID = 1215;
    String FIND_CONTROL_BY_TYPE_AND_TOWN_HALL_ID_DESCRIPTION = "Getting all controls for type {} and town hall id {}";

    int VOTING_RESULT = 1216;
    String VOTING_RESULT_DESCRIPTION = "Townhall {0}, for voting with id {1} and total of votes {2} had the result = {3} with votes -> YES ({4}), NO({5}) and ABS({6})";

    int RETRIEVING_SUBJECT_LIST_FROM_SAPL_CODE = 1300;
    String RETRIEVING_SUBJECT_LIST_FROM_SAPL_DESCRIPTION = "Fetching all subject list for a session in SAPL.";

    int FETCHING_EMENTA_URL_FOR_SUBJECT_LIST = 1310;
    String FETCHING_EMENTA_URL_FOR_SUBJECT_LIST_DESCRIPTION = "Fetching original ementa url for each subject in the list";

    int ADD_SUBJECT_MANUALLY_TO_SESSION = 1311;
    String ADD_SUBJECT_MANUALLY_TO_SESSION_DESCRIPTION = "Adding manual subject to session";

    int REMOVE_SUBJECT_FROM_SESSION = 1312;
    String REMOVE_SUBJECT_FROM_SESSION_DESCRIPTION = "Removing subject from session";

    int PARLAMENTAR_UNSUBSCRIPTION = 1313;
    String PARLAMENTAR_UNSUBSCRIPTION_DESCRIPTION = "Parlamentar just unsubscripted from speaker list.";
}
