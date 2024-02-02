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

    int ERROR_COMMUNICATION_SAPL = 5000;
    String ERROR_COMMUNICATION_SAPL_DESCRIPTION = "There was some error in the communication with SAPL";

    int SAPL_SESSION_NOT_FOUND = 5001;
    String SAPL_SESSION_NOT_FOUND_DESCRIPTION = "There's no SAPL session with the given id";

    int DATABASE_STRUCUTRE_ERROR = 5002;
    String DATABASE_STRUCUTRE_ERROR_DESCRIPTION = "Database structure error. Please, contact the developer.";

    int ERROR_UNEXPECTED_EXCEPTION = 5999;
    String ERROR_UNEXPECTED_EXCEPTION_DESCRIPTION = "Unexpected expection! Message: {}";

    // 12xx Session Events

    int CREATE_SESSION = 1200;
    String CREATE_SESSION_DESCRIPTION = "Session created successfully for townhall {}";

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

    int UPDATE_PARLAMENTAR_PRESENCE_MANUALLY = 1207;
    String UPDATE_PARLAMENTAR_PRESENCE_MANUALLY_DESCRIPTION = "Parlamentar presence manually updated with success";

    int COMPUTE_VOTE = 1206;
    String COMPUTE_VOLTE_DESCRIPTION = "Vote computed successfully for parlamentar {} into session {}";

    int FIND_TODAY_SESSION_BY_TOWNHALL = 1207;

    String FIND_TODAY_SESSION_BY_TOWNHALL_DESCRIPTION = "Session from {} retrieved for parlamentar user with success";

    int TODAY_SESSION_BY_TOWNHALL_NOT_FOUND = 1207;

    String TODAY_SESSION_BY_TOWNHALL_NOT_FOUNDDESCRIPTION = "Session from {} not found for today";

    int PARLAMENTAR_SUBSCRIPTION = 1208;
    String PARLAMENTAR_SUBSCRIPTION_DESCRIPTION = "Parlamentar just subscripted successful";

    int CLOSE_VOTING_FOR_SESSION = 1209;
    String CLOSE_VOTING_FOR_SESSION_DESCRIPTION = "Voting closed";

    int FIND_SESSION_VOTING_INFO = 1210;
    String FIND_SESSION_VOTING_INFO_DESCRIPTION = "Voting info retrieved";

    int FIND_SUBJECTS_FOR_SESSION = 1211;
    String FIND_SUBJECTS_FOR_SESSION_DESCRIPTION = "Getting all subjects with the desired filter for session";


}
