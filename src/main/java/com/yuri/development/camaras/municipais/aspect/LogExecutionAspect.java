package com.yuri.development.camaras.municipais.aspect;

import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.service.SessionService;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Aspect
@Component
public class LogExecutionAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TownHallService townHallService;

    private final Logger logger = LoggerFactory.getLogger(LogExecutionAspect.class.getName());

    @Around("@annotation(com.yuri.development.camaras.municipais.annotation.HLogger)")
    public Object standardLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        Session session = null;
        StopWatch stopWatch = new StopWatch();

        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String methodName = methodSignature.getName();

        int eventId = methodSignature.getMethod().getAnnotation(HLogger.class).id();
        String description = methodSignature.getMethod().getAnnotation(HLogger.class).description();
        boolean hasUUID = methodSignature.getMethod().getAnnotation(HLogger.class).hasUUID();

        stopWatch.start();
        Object object = proceedingJoinPoint.proceed();
        stopWatch.stop();

        if(hasUUID && proceedingJoinPoint.getArgs().length > 0){
            String uuid = retrieveUUID(proceedingJoinPoint.getArgs());
            session = sessionService.findByUuid(uuid);
        }

        ResponseEntity<?> response = (ResponseEntity<?>) object;
        boolean isResponseNull = response == null;

        //if http status code is not 200, log it as an error
        if(response == null || response.getStatusCode() == HttpStatus.OK){
            logger.info(buildLogMessage(session, methodName, eventId, description, stopWatch.getTotalTimeMillis()));
        }else{
            logger.error(buildLogMessage(session, methodName, eventId, description, stopWatch.getTotalTimeMillis()));
        }
        return object;
    }

    private String retrieveUUID(Object [] args){
        String regexUUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        for (Object arg : args) {
            String uuid = (String) arg;
            if (Pattern.matches(regexUUID, uuid)) {
                return uuid;
            }
        }
        return Strings.EMPTY;
    }

    private String buildLogMessage(Session session, String methodName, int eventId, String description, long duration){

        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("Method: ").append(methodName).append("()");
        sBuilder.append(" -> Event_id= ").append(eventId).append(", Event_description= ").append(description);
        if(session != null){
            sBuilder.append(" -> Townhall: ").append(session.getTownHall().getName());
            sBuilder.append(", Session UUID: ").append(session.getUuid());
        }
        sBuilder.append(" - Duration(ms):").append(duration);

        return sBuilder.toString();
    }
}
