package org.infotex.skondurushkin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.infotex.skondurushkin.factorizer.FactorizerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class FactorizerController {
	static final Logger log = LoggerFactory.getLogger(FactorizerController.class);
	
	ExecutorService executor = Executors.newCachedThreadPool();
	Map<Long, CompletableFuture<FactorizerJob>> futures = new ConcurrentHashMap<>();
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@PostConstruct
	void postConstruct() {
	}
	
	@PreDestroy
	void preDestroy() {
		while(!futures.isEmpty()) {
			try {
				Optional<Map.Entry<Long, CompletableFuture<FactorizerJob>>> ef = futures.entrySet().stream().findFirst();
				if (ef.isPresent()) {
					long key = ef.get().getKey();
					if (FactorizerJob.remove(key))
						futures.remove(key);
				}
				Thread.sleep(50L);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
	}
	
    @MessageMapping("/do")
    @SendTo("/test/notifications")
    public Notification factorize(Command command) throws Exception {
        return processCommand(command);
    }
    
    public void sendNotification(Notification ntf) throws Exception {
        template.convertAndSend("/test/notifications", ntf);
    }
    
    protected Notification processCommand(Command command) {
    	Notification ret = null;
    	if (command.getAction().equals(Command.ACTION_ADD)) {
    		ret = addTask(Long.valueOf(command.getValue()));
    	} else if (command.getAction().equals(Command.ACTION_REMOVE)) {
    		ret = removeTask(Long.valueOf(command.getValue()));
    	} else {
    		// Invalid command
    		ret = Notification.exception(Long.valueOf(command.getValue()), "Unsupported action verb", null);
    	}
    	return ret;
    }
    
    protected Notification addTask(long value) {
    	// 1. Try to reuse current completed or still running jobs
    	FactorizerJob job = FactorizerJob.lookup(value);
		if (job != null) {
			
			return job.isDone() ? Notification.done(value, job.getResult()) 
								: Notification.running(value);
		}
		// Check for recently done jobs
		CompletableFuture<FactorizerJob> future = null;
		if ((future = futures.get(value)) != null) {
			if (future.isDone()) {
				try {
					return Notification.done(value, future.get().getResult());
				} catch (InterruptedException | ExecutionException e) {
					return Notification.exception(value, "Task completed exceptionally", e);
				}
			}
		}
		// Finally supply asynchronous task with completable future
		futures.computeIfAbsent(value, (k) ->  CompletableFuture.supplyAsync(() -> FactorizerJob.create(value).go(), executor))
		       .thenApply(x -> futures.remove(x.getId()));
		
    	return Notification.added(value);
    }
    
    protected Notification removeTask(long value) {
    	return FactorizerJob.remove(value)
    			? Notification.removed(value) 
    			: Notification.exception(value,  "Factorization task not found", null);
    }

}
