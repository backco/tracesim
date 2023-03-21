package com.co.back.evalumetric.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RelationalLog<E, A> extends ArrayList<RelationalTrace<E, A>> {

    public  String                             name;
    public  String                             eventClassifier;
    private Map<Object, List<RelationalTrace>> classTraceMap = null;

    @Override
    public RelationalTrace<E, A> get ( int i ) {

	return super.get(i);
    }

    public Map<Object, List<RelationalTrace>> classTraceMap () {

	if ( classTraceMap == null ) {
	    classTraceMap = new HashMap<>();
	    for ( RelationalTrace t : this ) {
                classTraceMap.computeIfAbsent(t.getTraceClass(), v -> new ArrayList<>()).add(t);
	    }
	}

        return classTraceMap;
    }
}
