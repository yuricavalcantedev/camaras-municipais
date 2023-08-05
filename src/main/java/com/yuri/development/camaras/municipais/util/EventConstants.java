package com.yuri.development.camaras.municipais.util;

public interface EventConstants {

    // 11xx townhall, 12xx session, 4xxx erros

    int LOGIN_SUCCESS = 1001;
    String LOGIN_SUCCESS_DESCRIPTION = "User = {} logged in successfully";

    int TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED = 4001;
    String TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED_DESCRIPTION = "The current townhall has not a table configured. " +
            "Configure the table of roles and try it again";

    int TOWNHALL_HAS_SESSION_ALREADY = 4002;
    String TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION = "The current townhall has already a session created for today";

    int LOGIN_UNSUCCESSFUL = 4003;
    String LOGIN_UNSUCCESSFUL_DESCRIPTION = "Username and password combination does not match.";

    int COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION = 4004;
    String COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION_DESCRIPTION = "User {} tried to loggin in but there is no open session for today";

    int ERROR_COMMUNICATION_SAPL = 5000;
    String ERROR_COMMUNICATION_SAPL_DESCRIPTION = "There was some error in the communication with SAPL";

    int SAPL_SESSION_NOT_FOUND = 5001;
    String SAPL_SESSION_NOT_FOUND_DESCRIPTION = "There's no SAPL session with the given id";

    int ERROR_UNEXPECTED_EXCEPTION = 5999;
    String ERROR_UNEXPECTED_EXCEPTION_DESCRIPTION = "Unexpected expection! Message: {}";

    int GET_SESSION_VOTING_INFO_STANDARD_BY_UUID = 1201;
    String GET_SESSION_VOTING_INFO_STANDARD_BY_UUID_DESCRIPTION = "Session info retrieved with success";

    int CREATE_SESSION = 1202;
    String CREATE_SESSION_DESCRIPTION = "Session was created successfully for townhall {}";

    int DELETE_SESSION = 1203;
    String DELETE_SESSION_DESCRIPTION = "Session was deleted successfully";
}
