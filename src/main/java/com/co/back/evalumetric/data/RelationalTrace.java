package com.co.back.evalumetric.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationalTrace<E, A> {

    private static int idCounter = 0;

    public final  int                                id;
    final         Set<E>                             events        = new HashSet<>();
    final         Map<String, Map<E, Set<E>>>        relations     = new HashMap<>();
    final         List<E>                            totalOrder    = new ArrayList<>();
    final         Map<LocalDateTime, Set<E>>         totalPreorder = new HashMap();
    private final A                                  traceClass;
    private String totalOrderStr = null;

    public String getEventClassifier () {

	if ( eventClassifier == null ) {
	    throw new IllegalArgumentException("missing event classifier");
	} else {
	    return eventClassifier;
	}
    }

    private String eventClassifier;

    RelationalTrace ( A traceClass, String eventClassifier ) {

	this.traceClass      = traceClass;
	this.eventClassifier = eventClassifier;
	this.id              = idCounter;
	idCounter++;
    }

    public A getTraceClass () {

	return this.traceClass;
    }

    public Set<E> getEvents () {

	return events;
    }

    public Map<String, Map<E, Set<E>>> getRelations () {

	return relations;
    }

    public List<E> getTotalOrder () {

	return totalOrder;
    }

    public Map<LocalDateTime, Set<E>> getTotalPreorder () {

	return totalPreorder;
    }

    public String getTotalOrderStr () {

	return totalOrderStr;
    }

    public void setTotalOrderStr (String str) {

	totalOrderStr = str;
    }

    @Override
    public String toString () {

	return ( (Integer) id ).toString();
    }
}
