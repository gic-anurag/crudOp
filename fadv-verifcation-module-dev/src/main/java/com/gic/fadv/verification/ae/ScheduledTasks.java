package com.gic.fadv.verification.ae;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.gic.fadv.verification.ae.model.CaseSpecificCheckPriority;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.ae.service.AllocationEngineService;

@Component
public class ScheduledTasks
{

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private Environment env;

    @Autowired
    AllocationEngineService serv;

//	@Scheduled(cron = "0 * * * * ?" )
//	second, minute, hour, day of month, month, day(s) of week
    //@Scheduled(cron = "${ae.cron}")
    public void scheduleTaskForCSPi()
    {
    	try
        {
            if(env.getProperty("ae.enabled").equalsIgnoreCase("true"))
            {
                logger.info("Cron Task Started :: ", dateTimeFormatter.format(LocalDateTime.now()));
                try
                {
                    updateAllocationTable();
                    updateAllocationTablePriority();
                } catch (Exception e)
                {
                    logger.debug(e.getMessage(), e);
                }
                logger.info("Cron Task Ended :: ", dateTimeFormatter.format(LocalDateTime.now()));
            }
            else
            {
                logger.info("ae.enabled not set to true");
            }
        } catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateAllocationTable()
    {
        try
        {
            List<CaseSpecificRecordDetail> checks = serv.getNewChecks();
            logger.debug("Number of checks: " + checks.size());

            List<CaseSpecificCheckPriority> priorityChecks = checks.stream().map(this::createPriorityCheck)
                    .collect(Collectors.toList());

            logger.debug("Number of priority checks: " + priorityChecks.size());
            serv.savePriorityChecks(priorityChecks);

        } catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return;
        }
    }

    private CaseSpecificCheckPriority createPriorityCheck(CaseSpecificRecordDetail check)
    {
        String defaultPriority = env.getProperty("ae.default.priority");
        return CaseSpecificCheckPriority.builder().checkId(check.getInstructionCheckId()).priority(defaultPriority).build();
    }

    private void updateAllocationTablePriority()
    {
        try
        {
            // delete all isAllocated = true from the priority table
            // apply priority logic on the priority table
        	
        } catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return;
        }
    }

}
